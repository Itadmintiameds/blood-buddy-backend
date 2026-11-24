package bloodbuddy.backend.service;

import bloodbuddy.backend.dto.donor.DonorImportBatchResponse;
import bloodbuddy.backend.dto.donor.DonorImportConfirmRequest;
import bloodbuddy.backend.dto.donor.DonorImportRowResponse;
import bloodbuddy.backend.dto.donor.DonorImportRowUpdateRequest;
import bloodbuddy.backend.entity.BloodDonorDetails;
import bloodbuddy.backend.entity.ConsentForm;
import bloodbuddy.backend.entity.DonorImportBatch;
import bloodbuddy.backend.entity.DonorImportRow;
import bloodbuddy.backend.entity.masters.BloodGroup;
import bloodbuddy.backend.enums.ConsentSource;
import bloodbuddy.backend.enums.ConsentType;
import bloodbuddy.backend.enums.DonorImportRowStatus;
import bloodbuddy.backend.enums.DonorImportStatus;
import bloodbuddy.backend.exception.BadRequestException;
import bloodbuddy.backend.exception.ConflictException;
import bloodbuddy.backend.repository.BloodDonorDetailsRepository;
import bloodbuddy.backend.repository.BloodGroupRepository;
import bloodbuddy.backend.repository.DonorImportBatchRepository;
import bloodbuddy.backend.repository.DonorImportRowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Exercises upload → review → confirm against in-memory stand-ins for the repositories. */
class DonorImportServiceTest {

    private static final String HEADER =
            "Full Name,mobile_number,alternativeMobileNumber,bloodGroup,dob,address,city,district,pincode,lastBloodDonationDate,remarks\n";

    private DonorImportBatchRepository batchRepository;
    private DonorImportRowRepository rowRepository;
    private BloodDonorDetailsRepository donorRepository;
    private ConsentService consentService;
    private DonorImportService service;
    private BloodGroup oPositive;

    // What the mocked repositories "store".
    private DonorImportBatch storedBatch;
    private final List<DonorImportRow> storedRows = new ArrayList<>();
    private final List<BloodDonorDetails> savedDonors = new ArrayList<>();

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        batchRepository = mock(DonorImportBatchRepository.class);
        rowRepository = mock(DonorImportRowRepository.class);
        donorRepository = mock(BloodDonorDetailsRepository.class);
        BloodGroupRepository bloodGroupRepository = mock(BloodGroupRepository.class);
        consentService = mock(ConsentService.class);
        service = new DonorImportService(batchRepository, rowRepository, donorRepository,
                bloodGroupRepository, consentService);

        oPositive = new BloodGroup(1L, "O+", true);
        when(bloodGroupRepository.findAll()).thenReturn(List.of(oPositive, new BloodGroup(2L, "AB-", true)));
        // One existing donor, stored in canonical form.
        when(donorRepository.findIdAndMobileNumberByMobileNumberIn(any())).thenAnswer(inv -> {
            List<Object[]> found = new ArrayList<>();
            if (((java.util.Collection<String>) inv.getArgument(0)).contains("9900011111")) {
                found.add(new Object[]{42L, "9900011111"});
            }
            return found;
        });

        when(batchRepository.save(any())).thenAnswer(inv -> {
            storedBatch = inv.getArgument(0);
            storedBatch.setDonorImportBatchId(7L);
            return storedBatch;
        });
        when(batchRepository.findWithLockByDonorImportBatchId(7L)).thenAnswer(inv -> Optional.of(storedBatch));
        when(batchRepository.findById(7L)).thenAnswer(inv -> Optional.ofNullable(storedBatch));
        when(batchRepository.findFirstByUploadedByAndStatusOrderByUploadedAtDesc("admin", DonorImportStatus.PENDING))
                .thenAnswer(inv -> Optional.ofNullable(storedBatch)
                        .filter(b -> b.getStatus() == DonorImportStatus.PENDING));
        when(rowRepository.deleteByBatchId(7L)).thenAnswer(inv -> {
            int count = storedRows.size();
            storedRows.clear();
            return count;
        });
        doAnswer(inv -> {
            storedBatch = null;
            return null;
        }).when(batchRepository).delete(any());
        when(rowRepository.saveAll(any())).thenAnswer(inv -> {
            long id = 100;
            for (DonorImportRow row : (Iterable<DonorImportRow>) inv.getArgument(0)) {
                row.setDonorImportRowId(id++);
                storedRows.add(row);
            }
            return storedRows;
        });
        when(rowRepository.findByBatchDonorImportBatchIdOrderByRowNo(7L)).thenAnswer(inv -> storedRows);
        when(rowRepository.findByDonorImportRowIdAndBatchDonorImportBatchId(anyLong(), eq(7L))).thenAnswer(inv ->
                storedRows.stream().filter(r -> r.getDonorImportRowId().equals(inv.getArgument(0))).findFirst());
        when(donorRepository.saveAll(any())).thenAnswer(inv -> {
            List<BloodDonorDetails> donors = new ArrayList<>();
            for (BloodDonorDetails donor : (Iterable<BloodDonorDetails>) inv.getArgument(0)) {
                donor.setBloodDonorDetailsId(500L + savedDonors.size());
                savedDonors.add(donor);
                donors.add(donor);
            }
            return donors;
        });
        doAnswer(inv -> {
            storedRows.removeAll((List<DonorImportRow>) List.copyOf(
                    (java.util.Collection<DonorImportRow>) inv.getArgument(0)));
            return null;
        }).when(rowRepository).deleteAll(any());
        when(consentService.requireAccepted(ConsentType.DONOR, "v1"))
                .thenReturn(new ConsentForm(1L, ConsentType.DONOR, "v1", "t", "c", true, null));
    }

    private DonorImportBatchResponse upload(String csv) {
        return service.upload(new MockMultipartFile("file", "donors.csv", "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)), "admin");
    }

    private DonorImportRow row(int rowNo) {
        return storedRows.stream().filter(r -> r.getRowNo() == rowNo).findFirst().orElseThrow();
    }

    private DonorImportConfirmRequest confirmRequest() {
        DonorImportConfirmRequest request = new DonorImportConfirmRequest();
        request.setConsentConfirmed(true);
        request.setConsentVersion("v1");
        return request;
    }

    @Test
    void uploadStagesAndClassifiesRows() {
        String dob = LocalDate.now().minusYears(30).toString();
        DonorImportBatchResponse batch = upload("﻿" + HEADER
                + "Ravi,+91 98765 43210,,o +ve," + dob + ",,Bengaluru,Urban,560001,,\n"
                + "\n"
                + "Asha,9876543210,,AB-,,,Mysuru,Mysuru,570001,,\n"
                + "Old Donor,09900011111,,O Positive,,,Mysuru,Mysuru,570001,,\n"
                + "Bad,12345,,X+,2015-01-01,,,Mysuru,0123,2999-01-01,\n");

        assertThat(storedRows).extracting(DonorImportRow::getRowNo).containsExactly(2, 4, 5, 6);
        assertThat(storedRows).extracting(DonorImportRow::getStatus).containsExactly(
                DonorImportRowStatus.VALID,
                DonorImportRowStatus.DUPLICATE_IN_FILE,
                DonorImportRowStatus.DUPLICATE_IN_DB,
                DonorImportRowStatus.ERROR);
        assertThat(row(4).getDuplicateOfRow()).isEqualTo(2);
        assertThat(row(5).getExistingDonorId()).isEqualTo(42L);
        assertThat(row(6).getErrors().split("\n")).hasSize(6);
        // Staged values are kept exactly as uploaded.
        assertThat(row(2).getMobileNumber()).isEqualTo("+91 98765 43210");

        assertThat(batch.getStatus()).isEqualTo(DonorImportStatus.PENDING);
        assertThat(batch.getTotalRows()).isEqualTo(4);
        assertThat(batch.getReadyRows()).isEqualTo(1);
        assertThat(batch.getErrorRows()).isEqualTo(1);
        assertThat(batch.getDuplicateRows()).isEqualTo(2);
    }

    @Test
    void bloodGroupSpellingsFromRealRegistersAreRecognised() {
        assertThat(List.of("O+", "o +ve", "O POSITIVE", "O POSITIVEE", "A P[OSITIVE", "B NEGATIIVE",
                "AB NEGETIVE", "O-NEGETIVE", "ab-", "B POS"))
                .extracting(DonorImportService::bloodGroupKey)
                .containsExactly("O+", "O+", "O+", "O+", "A+", "B-", "AB-", "O-", "AB-", "B+");
        assertThat(DonorImportService.bloodGroupKey("X+")).isEqualTo("X+");
        assertThat(DonorImportService.bloodGroupKey("O")).isEqualTo("O");
    }

    @Test
    void districtAndPincodeAreOptional() {
        upload("fullName,mobileNumber,bloodGroup,city\nDinesha,9663994014,O POSITIVE,Mysuru\n");

        assertThat(row(2).getStatus()).isEqualTo(DonorImportRowStatus.VALID);
    }

    @Test
    void templateExampleRowIsNotImportable() {
        upload(service.template() + "Asha,9876543211,,AB-,,,Mysuru,Mysuru,570001,,\n");

        assertThat(row(2).getStatus()).isEqualTo(DonorImportRowStatus.ERROR);
        assertThat(row(2).getErrors()).startsWith("This is the template's example row");
        assertThat(row(3).getStatus()).isEqualTo(DonorImportRowStatus.VALID);
    }

    @Test
    void uploadRejectsMissingRequiredColumns() {
        assertThatThrownBy(() -> upload("fullName,mobileNumber\nRavi,9876543210\n"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("bloodGroup");
    }

    @Test
    void editingAndExcludingRowsRevalidatesTheBatch() {
        upload(HEADER
                + "Ravi,9876543210,,O+,,,Bengaluru,Urban,560001,,\n"
                + "Asha,9876543210,,AB-,,,Mysuru,Mysuru,570001,,\n"
                + "Bad,12345,,O+,,,Mysuru,Mysuru,570001,,\n");
        assertThat(storedBatch.getReadyRows()).isEqualTo(1);

        // Fixing the bad mobile makes row 4 importable.
        DonorImportRowUpdateRequest fix = new DonorImportRowUpdateRequest();
        fix.setFullName("Bad");
        fix.setMobileNumber("9123456780");
        fix.setBloodGroup("O+");
        fix.setCity("Mysuru");
        fix.setDistrict("Mysuru");
        fix.setPincode("570001");
        DonorImportRowResponse fixed = service.updateRow(7L, row(4).getDonorImportRowId(), fix);
        assertThat(fixed.getStatus()).isEqualTo(DonorImportRowStatus.VALID);
        assertThat(fixed.getErrors()).isEmpty();

        // Excluding row 2 frees its mobile number, so the repeat in row 3 becomes importable.
        service.setExcluded(7L, row(2).getDonorImportRowId(), true);
        assertThat(row(3).getStatus()).isEqualTo(DonorImportRowStatus.VALID);
        assertThat(storedBatch.getReadyRows()).isEqualTo(2);
        assertThat(storedBatch.getExcludedRows()).isEqualTo(1);
    }

    @Test
    void confirmInsertsReadyRowsAndDeletesStagedRows() {
        upload(HEADER
                + "Ravi,9876543210,,O+,,,Bengaluru,Urban,560001,,\n"
                + "Old Donor,9900011111,,O+,,,Mysuru,Mysuru,570001,,\n"
                + "Bad,12345,,O+,,,Mysuru,Mysuru,570001,,\n");
        Long rowId = row(2).getDonorImportRowId();

        DonorImportBatchResponse result = service.confirm(7L, confirmRequest(), "admin");

        assertThat(result.getStatus()).isEqualTo(DonorImportStatus.CONFIRMED);
        assertThat(result.getInsertedRows()).isEqualTo(1);
        assertThat(result.getSkippedRows()).isEqualTo(2);
        assertThat(result.getConsentVersion()).isEqualTo("v1");

        assertThat(savedDonors).hasSize(1);
        BloodDonorDetails donor = savedDonors.get(0);
        assertThat(donor.getMobileNumber()).isEqualTo("9876543210");
        assertThat(donor.getBloodGroup()).isSameAs(oPositive);
        assertThat(donor.getConsentSource()).isEqualTo(ConsentSource.OFFLINE_IMPORT);
        assertThat(donor.getCreatedBy()).isEqualTo("IMPORT:7");

        // The donors table is now the only copy: every staged row is gone.
        assertThat(storedRows).isEmpty();
        assertThatThrownBy(() -> service.errorReport(7L)).isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> service.confirm(7L, confirmRequest(), "admin"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("CONFIRMED");
        assertThatThrownBy(() -> service.setExcluded(7L, rowId, true))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void onlyOnePendingImportPerAdminAndDiscardDeletesTheImport() {
        String csv = HEADER + "Ravi,9876543210,,O+,,,Bengaluru,Urban,560001,,\n";
        upload(csv);
        assertThat(service.getPending("admin")).isPresent();

        assertThatThrownBy(() -> upload(csv))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("batch 7");

        DonorImportBatchResponse discarded = service.discard(7L);
        assertThat(discarded.getStatus()).isEqualTo(DonorImportStatus.DISCARDED);
        assertThat(discarded.getTotalRows()).isEqualTo(1);
        assertThat(storedRows).isEmpty();
        assertThat(storedBatch).isNull();
        assertThat(service.getPending("admin")).isEmpty();

        // With the pending batch gone, a new upload is accepted.
        assertThat(upload(csv).getStatus()).isEqualTo(DonorImportStatus.PENDING);
    }

    @Test
    void confirmFailsWhenNothingIsReady() {
        upload(HEADER + "Bad,12345,,O+,,,Mysuru,Mysuru,570001,,\n");
        assertThatThrownBy(() -> service.confirm(7L, confirmRequest(), "admin"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("no valid rows");
    }
}
