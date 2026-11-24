package bloodbuddy.backend.service;

import bloodbuddy.backend.common.PagedResponse;
import bloodbuddy.backend.dto.donor.DonorImportBatchResponse;
import bloodbuddy.backend.dto.donor.DonorImportConfirmRequest;
import bloodbuddy.backend.dto.donor.DonorImportRowResponse;
import bloodbuddy.backend.dto.donor.DonorImportRowUpdateRequest;
import bloodbuddy.backend.entity.BloodDonorDetails;
import bloodbuddy.backend.entity.ConsentForm;
import bloodbuddy.backend.entity.DonorImportBatch;
import bloodbuddy.backend.entity.DonorImportRow;
import bloodbuddy.backend.entity.DonorStatus;
import bloodbuddy.backend.entity.masters.BloodGroup;
import bloodbuddy.backend.enums.ConsentSource;
import bloodbuddy.backend.enums.ConsentType;
import bloodbuddy.backend.enums.DonorImportRowStatus;
import bloodbuddy.backend.enums.DonorImportStatus;
import bloodbuddy.backend.exception.BadRequestException;
import bloodbuddy.backend.exception.ConflictException;
import bloodbuddy.backend.exception.ResourceNotFoundException;
import bloodbuddy.backend.repository.BloodDonorDetailsRepository;
import bloodbuddy.backend.repository.BloodGroupRepository;
import bloodbuddy.backend.repository.DonorImportBatchRepository;
import bloodbuddy.backend.repository.DonorImportRowRepository;
import bloodbuddy.backend.util.MobileNumbers;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Superadmin bulk import of donors from CSV via a staging table:
 * <ol>
 *   <li>{@link #upload}: every CSV row is stored in donor_import_row and validated; the batch is PENDING.</li>
 *   <li>Review: the superadmin lists rows, edits bad ones ({@link #updateRow}) or excludes them
 *       ({@link #setExcluded}); each change re-validates the whole batch.</li>
 *   <li>{@link #confirm}: VALID, non-excluded rows are copied into blood_donor_details and linked back
 *       to their row. Rows matching an existing donor's mobile number are always skipped.</li>
 * </ol>
 * Each superadmin may have only one PENDING batch; {@link #getPending} lets the UI resume it after
 * the admin left the page, and {@link #discard} deletes it (rows and batch record).
 */
@Service
public class DonorImportService {

    /** Template columns, in order. Header matching ignores case, spaces, '_' and '-'. */
    public static final List<String> COLUMNS = List.of(
            "fullName", "mobileNumber", "alternativeMobileNumber", "bloodGroup", "dob", "address",
            "city", "district", "pincode", "lastBloodDonationDate", "remarks");
    // District and pincode are optional: historical donor registers often record only the town.
    private static final Set<String> REQUIRED_COLUMNS = Set.of("fullName", "mobileNumber", "bloodGroup", "city");
    private static final List<String> TEMPLATE_EXAMPLE = List.of(
            "Ravi Kumar", "9876543210", "", "O+", "1990-05-14", "12 MG Road",
            "Bengaluru", "Bengaluru Urban", "560001", "2026-01-10", "Donated at camp");

    public static final int MAX_ROWS = 5000;
    private static final int MAX_TEXT_LENGTH = 255;
    // Staged text columns are wider than donor columns so over-long input can be shown as a row error.
    private static final int MAX_STAGED_LENGTH = 1000;
    private static final int MIN_DONOR_AGE = 18;
    private static final int MAX_DONOR_AGE = 65;

    // Numbers per IN (...) query when looking up existing donors.
    private static final int LOOKUP_CHUNK = 1000;
    private static final int MAX_FILE_NAME_LENGTH = 255;

    private static final Pattern PINCODE = Pattern.compile("[1-9]\\d{5}");
    private static final Pattern BLOOD_GROUP = Pattern.compile("(AB|A|B|O)(.*)");
    // ISO (the template format) first, then the day-first forms spreadsheets commonly save in India.
    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("d-M-uuuu").withResolverStyle(ResolverStyle.STRICT),
            DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT));

    private final DonorImportBatchRepository batchRepository;
    private final DonorImportRowRepository rowRepository;
    private final BloodDonorDetailsRepository donorRepository;
    private final BloodGroupRepository bloodGroupRepository;
    private final ConsentService consentService;

    public DonorImportService(DonorImportBatchRepository batchRepository,
                              DonorImportRowRepository rowRepository,
                              BloodDonorDetailsRepository donorRepository,
                              BloodGroupRepository bloodGroupRepository,
                              ConsentService consentService) {
        this.batchRepository = batchRepository;
        this.rowRepository = rowRepository;
        this.donorRepository = donorRepository;
        this.bloodGroupRepository = bloodGroupRepository;
        this.consentService = consentService;
    }

    /** Header row plus one example row. */
    public String template() {
        return writeCsv(COLUMNS, List.of(TEMPLATE_EXAMPLE));
    }

    // ---- upload & review ----

    @Transactional
    public DonorImportBatchResponse upload(MultipartFile file, String actor) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please upload a non-empty CSV file");
        }
        String fileName = cleanFileName(file.getOriginalFilename());
        if (fileName != null && !fileName.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new BadRequestException("Only .csv files are supported");
        }
        // One unfinished import per superadmin: the UI can't always catch the admin leaving the page,
        // so instead of piling up abandoned batches they must continue or discard the pending one.
        batchRepository.findFirstByUploadedByAndStatusOrderByUploadedAtDesc(actor, DonorImportStatus.PENDING)
                .ifPresent(pending -> {
                    throw new ConflictException("You already have an import in review (batch "
                            + pending.getDonorImportBatchId()
                            + (pending.getFileName() != null ? ", " + pending.getFileName() : "")
                            + "). Confirm or discard it before uploading another file.");
                });
        List<Map<String, String>> records = parse(readContent(file));

        DonorImportBatch batch = new DonorImportBatch();
        batch.setFileName(fileName);
        batch.setStatus(DonorImportStatus.PENDING);
        batch.setUploadedBy(actor);
        batch.setUploadedAt(LocalDateTime.now());
        batch = batchRepository.save(batch);

        List<DonorImportRow> rows = new ArrayList<>(records.size());
        for (Map<String, String> values : records) {
            DonorImportRow row = new DonorImportRow();
            row.setBatch(batch);
            row.setRowNo(Integer.valueOf(values.get(ROW_NO_KEY)));
            setValues(row, values);
            rows.add(row);
        }
        revalidate(batch, rows);
        rowRepository.saveAll(rows);
        return toResponse(batch);
    }

    @Transactional(readOnly = true)
    public PagedResponse<DonorImportBatchResponse> list(Pageable pageable) {
        return PagedResponse.fromPage(batchRepository.findAll(pageable).map(this::toResponse));
    }

    /** The superadmin's unfinished import, if any, so the UI can offer to continue or discard it. */
    @Transactional(readOnly = true)
    public Optional<DonorImportBatchResponse> getPending(String actor) {
        return batchRepository.findFirstByUploadedByAndStatusOrderByUploadedAtDesc(actor, DonorImportStatus.PENDING)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public DonorImportBatchResponse get(Long batchId) {
        return toResponse(requireBatch(batchId));
    }

    /** Staged rows of a batch, optionally filtered by status(es) and excluded flag. */
    @Transactional(readOnly = true)
    public PagedResponse<DonorImportRowResponse> listRows(Long batchId, List<DonorImportRowStatus> statuses,
                                                         Boolean excluded, String search, Pageable pageable) {
        requireBatch(batchId);
        Specification<DonorImportRow> spec = (root, query, cb) ->
                cb.equal(root.get("batch").get("donorImportBatchId"), batchId);
        if (statuses != null && !statuses.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("status").in(statuses));
        }
        if (excluded != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("excluded"), excluded));
        }
        if (StringUtils.hasText(search)) {
            String trimmed = search.trim();
            String term = "%" + trimmed.toLowerCase() + "%";
            // A plain number also matches the spreadsheet row number exactly.
            Integer rowNo = trimmed.matches("\\d{1,9}") ? Integer.valueOf(trimmed) : null;
            spec = spec.and((root, query, cb) -> cb.or(
                    rowNo != null ? cb.equal(root.get("rowNo"), rowNo) : cb.disjunction(),
                    cb.like(cb.lower(root.get("fullName")), term),
                    cb.like(cb.lower(root.get("mobileNumber")), term),
                    cb.like(cb.lower(root.get("alternativeMobileNumber")), term),
                    cb.like(cb.lower(root.get("bloodGroup")), term),
                    cb.like(cb.lower(root.get("address")), term),
                    cb.like(cb.lower(root.get("city")), term),
                    cb.like(cb.lower(root.get("district")), term),
                    cb.like(cb.lower(root.get("pincode")), term),
                    cb.like(cb.lower(root.get("remarks")), term)));
        }
        return PagedResponse.fromPage(rowRepository.findAll(spec, pageable).map(DonorImportService::toRowResponse));
    }

    /** Replace a staged row's values with the superadmin's corrections, then re-validate the batch. */
    @Transactional
    public DonorImportRowResponse updateRow(Long batchId, Long rowId, DonorImportRowUpdateRequest request) {
        DonorImportBatch batch = requirePendingForUpdate(batchId);
        DonorImportRow row = requireRow(batchId, rowId);
        Map<String, String> values = new HashMap<>();
        values.put("fullName", request.getFullName());
        values.put("mobileNumber", request.getMobileNumber());
        values.put("alternativeMobileNumber", request.getAlternativeMobileNumber());
        values.put("bloodGroup", request.getBloodGroup());
        values.put("dob", request.getDob());
        values.put("address", request.getAddress());
        values.put("city", request.getCity());
        values.put("district", request.getDistrict());
        values.put("pincode", request.getPincode());
        values.put("lastBloodDonationDate", request.getLastBloodDonationDate());
        values.put("remarks", request.getRemarks());
        setValues(row, values);
        revalidate(batch, rowRepository.findByBatchDonorImportBatchIdOrderByRowNo(batchId));
        return toRowResponse(row);
    }

    /** Leave a row out of (or put it back into) the import, then re-validate the batch. */
    @Transactional
    public DonorImportRowResponse setExcluded(Long batchId, Long rowId, boolean excluded) {
        DonorImportBatch batch = requirePendingForUpdate(batchId);
        DonorImportRow row = requireRow(batchId, rowId);
        row.setExcluded(excluded);
        revalidate(batch, rowRepository.findByBatchDonorImportBatchIdOrderByRowNo(batchId));
        return toRowResponse(row);
    }

    /** CSV of the ERROR rows (not excluded) with an "errors" column, to fix offline. */
    @Transactional(readOnly = true)
    public String errorReport(Long batchId) {
        DonorImportBatch batch = requireBatch(batchId);
        // Confirm deletes the staged rows, so there is nothing left to report.
        if (batch.getStatus() != DonorImportStatus.PENDING) {
            throw new BadRequestException("The error report is only available while the import is in review");
        }
        List<String> header = new ArrayList<>();
        header.add("rowNumber");
        header.addAll(COLUMNS);
        header.add("errors");

        List<List<String>> records = new ArrayList<>();
        for (DonorImportRow row : rowRepository.findByBatchDonorImportBatchIdOrderByRowNo(batchId)) {
            if (row.isExcluded() || row.getStatus() != DonorImportRowStatus.ERROR) {
                continue;
            }
            Map<String, String> values = getValues(row);
            List<String> record = new ArrayList<>();
            record.add(String.valueOf(row.getRowNo()));
            COLUMNS.forEach(column -> record.add(values.get(column)));
            record.add(row.getErrors() == null ? "" : row.getErrors().replace("\n", "; "));
            records.add(record);
        }
        return writeCsv(header, records);
    }

    // ---- confirm & discard ----

    /**
     * Copies the batch's VALID, non-excluded rows into blood_donor_details in one transaction. Rows are
     * re-validated first since donors may have registered while the batch was under review.
     */
    @Transactional
    public DonorImportBatchResponse confirm(Long batchId, DonorImportConfirmRequest request, String actor) {
        DonorImportBatch batch = requirePendingForUpdate(batchId);
        ConsentForm consentForm = consentService.requireAccepted(ConsentType.DONOR, request.getConsentVersion());

        List<DonorImportRow> rows = rowRepository.findByBatchDonorImportBatchIdOrderByRowNo(batchId);
        Map<DonorImportRow, ParsedRow> parsed = revalidate(batch, rows);
        if (batch.getReadyRows() == 0) {
            throw new BadRequestException("There are no valid rows to import");
        }

        LocalDateTime now = LocalDateTime.now();
        String createdBy = "IMPORT:" + batchId;
        List<BloodDonorDetails> donors = rows.stream()
                .filter(DonorImportService::isReady)
                .map(row -> newDonor(parsed.get(row), consentForm, now, createdBy))
                .toList();
        donorRepository.saveAll(donors);
        int inserted = donors.size();
        int skipped = rows.size() - inserted;

        // blood_donor_details is now the record, so the staged rows (a second, soon stale copy of the
        // donors' personal data) are deleted. Imported donors stay traceable via createdBy.
        rowRepository.deleteAll(rows);

        batch.setStatus(DonorImportStatus.CONFIRMED);
        batch.setInsertedRows(inserted);
        batch.setSkippedRows(skipped);
        batch.setConsentForm(consentForm);
        batch.setConfirmedBy(actor);
        batch.setConfirmedAt(now);
        return toResponse(batch);
    }

    /**
     * Cancels a pending import and deletes it completely: its staged rows and the batch record. The
     * response is the batch as it was, marked DISCARDED.
     */
    @Transactional
    public DonorImportBatchResponse discard(Long batchId) {
        DonorImportBatch batch = requirePendingForUpdate(batchId);
        batch.setStatus(DonorImportStatus.DISCARDED);
        DonorImportBatchResponse response = toResponse(batch);
        rowRepository.deleteByBatchId(batchId);
        batchRepository.delete(batch);
        return response;
    }

    // ---- parsing ----

    // Key under which parse() passes the spreadsheet row number alongside the column values.
    private static final String ROW_NO_KEY = "#rowNo";

    /** Drops any client-side directory part (some browsers send it) and caps the length for the column. */
    private static String cleanFileName(String original) {
        if (original == null || original.isBlank()) {
            return null;
        }
        String name = original.substring(Math.max(original.lastIndexOf('/'), original.lastIndexOf('\\')) + 1).trim();
        return name.length() > MAX_FILE_NAME_LENGTH ? name.substring(name.length() - MAX_FILE_NAME_LENGTH) : name;
    }

    /** Excel saves CSV as UTF-8 (often with a BOM) or as Windows-1252; accept both. */
    private static String readContent(MultipartFile file) {
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Could not read the uploaded file");
        }
        String content;
        try {
            content = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException e) {
            content = new String(bytes, Charset.forName("windows-1252"));
        }
        return content.startsWith("﻿") ? content.substring(1) : content;
    }

    /** One map per non-blank data row: template column → trimmed value, plus {@link #ROW_NO_KEY}. */
    private static List<Map<String, String>> parse(String content) {
        // Empty lines are kept as records so record numbers match spreadsheet row numbers.
        CSVFormat format = CSVFormat.DEFAULT.builder().setIgnoreEmptyLines(false).setTrim(true).get();
        try (CSVParser parser = format.parse(new StringReader(content))) {
            Iterator<CSVRecord> records = parser.iterator();
            if (!records.hasNext()) {
                throw new BadRequestException("The CSV file is empty");
            }
            Map<String, Integer> columnIndex = columnIndex(records.next());

            List<Map<String, String>> rows = new ArrayList<>();
            while (records.hasNext()) {
                CSVRecord record = records.next();
                Map<String, String> values = new LinkedHashMap<>();
                boolean blank = true;
                for (String column : COLUMNS) {
                    Integer i = columnIndex.get(column);
                    String value = i != null && i < record.size() ? record.get(i).trim() : "";
                    // Undo neutralizeFormula() when our error report is re-uploaded without passing through Excel.
                    if (value.startsWith("'")) {
                        value = value.substring(1).trim();
                    }
                    if (value.length() > MAX_STAGED_LENGTH) {
                        throw new BadRequestException("Row " + record.getRecordNumber() + ": " + column
                                + " is longer than " + MAX_STAGED_LENGTH + " characters");
                    }
                    values.put(column, value);
                    blank &= value.isEmpty();
                }
                if (blank) {
                    continue;
                }
                if (rows.size() == MAX_ROWS) {
                    throw new BadRequestException("The file has more than " + MAX_ROWS
                            + " donor rows; split it into smaller files");
                }
                values.put(ROW_NO_KEY, String.valueOf(record.getRecordNumber()));
                rows.add(values);
            }
            if (rows.isEmpty()) {
                throw new BadRequestException("The CSV file has no donor rows");
            }
            return rows;
        } catch (IOException | UncheckedIOException | IllegalStateException e) {
            throw new BadRequestException("Could not parse the CSV file: " + e.getMessage());
        }
    }

    private static Map<String, Integer> columnIndex(CSVRecord header) {
        Map<String, String> columnByKey = COLUMNS.stream()
                .collect(Collectors.toMap(DonorImportService::headerKey, Function.identity()));
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < header.size(); i++) {
            String column = columnByKey.get(headerKey(header.get(i)));
            if (column != null) {
                index.putIfAbsent(column, i);
            }
        }
        List<String> missing = COLUMNS.stream()
                .filter(c -> REQUIRED_COLUMNS.contains(c) && !index.containsKey(c))
                .toList();
        if (!missing.isEmpty()) {
            throw new BadRequestException("Missing required column(s): " + String.join(", ", missing)
                    + ". Download the template for the expected headers.");
        }
        return index;
    }

    private static String headerKey(String header) {
        return header.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    // ---- validation ----

    /** Typed values of a row, produced by validation and used to build the donor on confirm. */
    private static final class ParsedRow {
        final List<String> errors = new ArrayList<>();
        String fullName;
        String mobileNumber;
        String alternativeMobileNumber;
        BloodGroup bloodGroup;
        LocalDate dob;
        String address;
        String city;
        String district;
        String pincode;
        LocalDate lastBloodDonationDate;
        String remarks;
    }

    private static boolean isReady(DonorImportRow row) {
        return !row.isExcluded() && row.getStatus() == DonorImportRowStatus.VALID;
    }

    /**
     * Validates every row of the batch (ordered by row number), sets each row's status/errors and
     * the batch's review counts. The whole batch is re-checked because rows affect each other: a
     * repeated mobile number is only a duplicate of an earlier row that isn't excluded.
     */
    private Map<DonorImportRow, ParsedRow> revalidate(DonorImportBatch batch, List<DonorImportRow> rows) {
        Map<String, BloodGroup> bloodGroups = new LinkedHashMap<>();
        for (BloodGroup group : bloodGroupRepository.findAll()) {
            if (!Boolean.FALSE.equals(group.getIsActive())) {
                bloodGroups.put(bloodGroupKey(group.getBloodGroupName()), group);
            }
        }
        String bloodGroupNames = bloodGroups.values().stream()
                .map(BloodGroup::getBloodGroupName)
                .collect(Collectors.joining(", "));

        // Donor numbers are stored in canonical form, so only this batch's numbers need looking up.
        List<String> batchMobiles = rows.stream()
                .map(row -> MobileNumbers.normalize(row.getMobileNumber()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<String, Long> existingByMobile = new HashMap<>();
        for (int from = 0; from < batchMobiles.size(); from += LOOKUP_CHUNK) {
            List<String> chunk = batchMobiles.subList(from, Math.min(from + LOOKUP_CHUNK, batchMobiles.size()));
            for (Object[] idAndMobile : donorRepository.findIdAndMobileNumberByMobileNumberIn(chunk)) {
                existingByMobile.putIfAbsent((String) idAndMobile[1], (Long) idAndMobile[0]);
            }
        }

        Map<String, Integer> firstRowByMobile = new HashMap<>();
        Map<DonorImportRow, ParsedRow> result = new IdentityHashMap<>();
        LocalDate today = LocalDate.now();
        int ready = 0;
        int errorCount = 0;
        int duplicates = 0;
        int excluded = 0;

        for (DonorImportRow row : rows) {
            ParsedRow parsed = validateFields(row, bloodGroups, bloodGroupNames, today);
            List<String> errors = parsed.errors;
            // The template ships with a sample donor; a filled-in file that still has it must not import it.
            Map<String, String> values = getValues(row);
            if (TEMPLATE_EXAMPLE.equals(COLUMNS.stream().map(column -> nullToEmpty(values.get(column))).toList())) {
                errors.add(0, "This is the template's example row; delete it or exclude it");
            }
            Integer duplicateOfRow = null;
            // Excluded rows don't claim their mobile number, so a later copy can be imported instead.
            if (parsed.mobileNumber != null && !row.isExcluded()) {
                duplicateOfRow = firstRowByMobile.putIfAbsent(parsed.mobileNumber, row.getRowNo());
            }

            row.setExistingDonorId(null);
            row.setDuplicateOfRow(duplicateOfRow);
            if (!errors.isEmpty()) {
                row.setStatus(DonorImportRowStatus.ERROR);
            } else if (duplicateOfRow != null) {
                row.setStatus(DonorImportRowStatus.DUPLICATE_IN_FILE);
                errors.add("Same mobile number as row " + duplicateOfRow);
            } else if (existingByMobile.containsKey(parsed.mobileNumber)) {
                row.setStatus(DonorImportRowStatus.DUPLICATE_IN_DB);
                row.setExistingDonorId(existingByMobile.get(parsed.mobileNumber));
                errors.add("A donor with this mobile number already exists (donor #"
                        + row.getExistingDonorId() + ")");
            } else {
                row.setStatus(DonorImportRowStatus.VALID);
            }
            row.setErrors(errors.isEmpty() ? null : String.join("\n", errors));
            result.put(row, parsed);

            if (row.isExcluded()) {
                excluded++;
            } else {
                switch (row.getStatus()) {
                    case VALID -> ready++;
                    case ERROR -> errorCount++;
                    case DUPLICATE_IN_DB, DUPLICATE_IN_FILE -> duplicates++;
                }
            }
        }

        batch.setTotalRows(rows.size());
        batch.setReadyRows(ready);
        batch.setErrorRows(errorCount);
        batch.setDuplicateRows(duplicates);
        batch.setExcludedRows(excluded);
        return result;
    }

    private static ParsedRow validateFields(DonorImportRow row, Map<String, BloodGroup> bloodGroups,
                                            String bloodGroupNames, LocalDate today) {
        ParsedRow parsed = new ParsedRow();
        List<String> errors = parsed.errors;

        parsed.fullName = text(row.getFullName(), "fullName", true, errors);
        parsed.address = text(row.getAddress(), "address", false, errors);
        parsed.city = text(row.getCity(), "city", true, errors);
        parsed.district = text(row.getDistrict(), "district", false, errors);
        parsed.remarks = text(row.getRemarks(), "remarks", false, errors);

        String mobile = text(row.getMobileNumber(), "mobileNumber", true, errors);
        if (mobile != null) {
            parsed.mobileNumber = MobileNumbers.normalize(mobile);
            if (parsed.mobileNumber == null) {
                errors.add("mobileNumber must be a valid 10-digit Indian mobile number");
            }
        }
        String alternative = text(row.getAlternativeMobileNumber(), "alternativeMobileNumber", false, errors);
        if (alternative != null) {
            parsed.alternativeMobileNumber = MobileNumbers.normalize(alternative);
            if (parsed.alternativeMobileNumber == null) {
                errors.add("alternativeMobileNumber must be a valid 10-digit Indian mobile number");
            }
        }

        String bloodGroup = text(row.getBloodGroup(), "bloodGroup", true, errors);
        if (bloodGroup != null) {
            parsed.bloodGroup = bloodGroups.get(bloodGroupKey(bloodGroup));
            if (parsed.bloodGroup == null) {
                errors.add("Unknown bloodGroup '" + bloodGroup + "'; use one of " + bloodGroupNames);
            }
        }

        String pincode = text(row.getPincode(), "pincode", false, errors);
        if (pincode != null) {
            if (PINCODE.matcher(pincode).matches()) {
                parsed.pincode = pincode;
            } else {
                errors.add("pincode must be a 6-digit Indian pincode");
            }
        }

        parsed.dob = date(row.getDob(), "dob", errors);
        if (parsed.dob != null) {
            int age = Period.between(parsed.dob, today).getYears();
            if (parsed.dob.isAfter(today)) {
                errors.add("dob cannot be in the future");
            } else if (age < MIN_DONOR_AGE || age > MAX_DONOR_AGE) {
                errors.add("Donor must be between " + MIN_DONOR_AGE + " and " + MAX_DONOR_AGE
                        + " years old (age " + age + ")");
            }
        }

        parsed.lastBloodDonationDate = date(row.getLastBloodDonationDate(), "lastBloodDonationDate", errors);
        if (parsed.lastBloodDonationDate != null) {
            if (parsed.lastBloodDonationDate.isAfter(today)) {
                errors.add("lastBloodDonationDate cannot be in the future");
            } else if (parsed.dob != null && parsed.lastBloodDonationDate.isBefore(parsed.dob)) {
                errors.add("lastBloodDonationDate cannot be before dob");
            }
        }
        return parsed;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    /** Returns the trimmed value or null when blank, recording required/length errors. */
    private static String text(String raw, String column, boolean required, List<String> errors) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) {
            if (required) {
                errors.add(column + " is required");
            }
            return null;
        }
        if (value.length() > MAX_TEXT_LENGTH) {
            errors.add(column + " must be at most " + MAX_TEXT_LENGTH + " characters");
            return null;
        }
        return value;
    }

    private static LocalDate date(String raw, String column, List<String> errors) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) {
            return null;
        }
        for (DateTimeFormatter format : DATE_FORMATS) {
            try {
                return LocalDate.parse(value, format);
            } catch (DateTimeParseException ignored) {
                // try the next format
            }
        }
        errors.add(column + " '" + value + "' is not a valid date; use YYYY-MM-DD");
        return null;
    }

    /**
     * Canonical key such as "AB-" for a blood group however it was written: "O+", "o +ve", "O Positive",
     * "B NEGETIVE", "O POSITIVEE", "A P[OSITIVE". The group letters come first; the sign is the first
     * "+" / "-", or else the first letter after them (P = positive, N = negative). Anything else is
     * returned as-is, so it won't match a master group and the row gets an error.
     */
    static String bloodGroupKey(String name) {
        String compact = name.toUpperCase(Locale.ROOT).replaceAll("\\s", "");
        Matcher matcher = BLOOD_GROUP.matcher(compact);
        if (!matcher.matches()) {
            return compact;
        }
        String sign = matcher.group(2).replaceAll("[^A-Z+-]", "");
        if (sign.startsWith("+") || sign.startsWith("P")) {
            return matcher.group(1) + "+";
        }
        if (sign.startsWith("-") || sign.startsWith("N")) {
            return matcher.group(1) + "-";
        }
        return compact;
    }

    // ---- mapping ----

    private static void setValues(DonorImportRow row, Map<String, String> values) {
        row.setFullName(values.get("fullName"));
        row.setMobileNumber(values.get("mobileNumber"));
        row.setAlternativeMobileNumber(values.get("alternativeMobileNumber"));
        row.setBloodGroup(values.get("bloodGroup"));
        row.setDob(values.get("dob"));
        row.setAddress(values.get("address"));
        row.setCity(values.get("city"));
        row.setDistrict(values.get("district"));
        row.setPincode(values.get("pincode"));
        row.setLastBloodDonationDate(values.get("lastBloodDonationDate"));
        row.setRemarks(values.get("remarks"));
    }

    private static Map<String, String> getValues(DonorImportRow row) {
        Map<String, String> values = new HashMap<>();
        values.put("fullName", row.getFullName());
        values.put("mobileNumber", row.getMobileNumber());
        values.put("alternativeMobileNumber", row.getAlternativeMobileNumber());
        values.put("bloodGroup", row.getBloodGroup());
        values.put("dob", row.getDob());
        values.put("address", row.getAddress());
        values.put("city", row.getCity());
        values.put("district", row.getDistrict());
        values.put("pincode", row.getPincode());
        values.put("lastBloodDonationDate", row.getLastBloodDonationDate());
        values.put("remarks", row.getRemarks());
        return values;
    }

    private static BloodDonorDetails newDonor(ParsedRow row, ConsentForm consentForm,
                                              LocalDateTime now, String createdBy) {
        BloodDonorDetails donor = new BloodDonorDetails();
        donor.setFullName(row.fullName);
        donor.setMobileNumber(row.mobileNumber);
        donor.setAlternativeMobileNumber(row.alternativeMobileNumber);
        donor.setBloodGroup(row.bloodGroup);
        donor.setDob(row.dob);
        donor.setAddress(row.address);
        donor.setCity(row.city);
        donor.setDistrict(row.district);
        donor.setPincode(row.pincode);
        donor.setLastBloodDonationDate(row.lastBloodDonationDate);
        donor.setRemarks(row.remarks);
        donor.setStatus(DonorStatus.ACTIVE);
        donor.setConsentAcceptedAt(now);
        donor.setConsentForm(consentForm);
        donor.setConsentSource(ConsentSource.OFFLINE_IMPORT);
        donor.setCreatedAt(now);
        donor.setCreatedBy(createdBy);
        return donor;
    }

    private static DonorImportRowResponse toRowResponse(DonorImportRow row) {
        return DonorImportRowResponse.builder()
                .rowId(row.getDonorImportRowId())
                .rowNumber(row.getRowNo())
                .fullName(row.getFullName())
                .mobileNumber(row.getMobileNumber())
                .alternativeMobileNumber(row.getAlternativeMobileNumber())
                .bloodGroup(row.getBloodGroup())
                .dob(row.getDob())
                .address(row.getAddress())
                .city(row.getCity())
                .district(row.getDistrict())
                .pincode(row.getPincode())
                .lastBloodDonationDate(row.getLastBloodDonationDate())
                .remarks(row.getRemarks())
                .status(row.getStatus())
                .errors(row.getErrors() == null ? List.of() : Arrays.asList(row.getErrors().split("\n")))
                .existingDonorId(row.getExistingDonorId())
                .duplicateOfRow(row.getDuplicateOfRow())
                .excluded(row.isExcluded())
                .build();
    }

    private DonorImportBatchResponse toResponse(DonorImportBatch batch) {
        return DonorImportBatchResponse.builder()
                .batchId(batch.getDonorImportBatchId())
                .fileName(batch.getFileName())
                .status(batch.getStatus())
                .totalRows(batch.getTotalRows())
                .readyRows(batch.getReadyRows())
                .errorRows(batch.getErrorRows())
                .duplicateRows(batch.getDuplicateRows())
                .excludedRows(batch.getExcludedRows())
                .insertedRows(batch.getInsertedRows())
                .skippedRows(batch.getSkippedRows())
                .consentVersion(batch.getConsentForm() != null ? batch.getConsentForm().getVersion() : null)
                .uploadedBy(batch.getUploadedBy())
                .uploadedAt(batch.getUploadedAt())
                .confirmedBy(batch.getConfirmedBy())
                .confirmedAt(batch.getConfirmedAt())
                .build();
    }

    // ---- lookups ----

    private DonorImportBatch requireBatch(Long batchId) {
        return batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Import batch not found: " + batchId));
    }

    private DonorImportBatch requirePendingForUpdate(Long batchId) {
        DonorImportBatch batch = batchRepository.findWithLockByDonorImportBatchId(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Import batch not found: " + batchId));
        if (batch.getStatus() != DonorImportStatus.PENDING) {
            throw new BadRequestException("Import batch " + batchId + " is already " + batch.getStatus());
        }
        return batch;
    }

    private DonorImportRow requireRow(Long batchId, Long rowId) {
        return rowRepository.findByDonorImportRowIdAndBatchDonorImportBatchId(rowId, batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Import row not found: " + rowId));
    }

    private static String writeCsv(List<String> header, List<List<String>> records) {
        StringWriter out = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT)) {
            printer.printRecord(header);
            for (List<String> record : records) {
                printer.printRecord(record.stream().map(DonorImportService::neutralizeFormula).toList());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toString();
    }

    /**
     * CSV injection guard for files the admin opens in Excel: a cell starting with = + - @ (or a tab /
     * carriage return) would run as a formula, so it's prefixed with an apostrophe, which Excel hides.
     */
    private static String neutralizeFormula(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        char first = value.charAt(0);
        return first == '=' || first == '+' || first == '-' || first == '@' || first == '\t' || first == '\r'
                ? "'" + value
                : value;
    }
}
