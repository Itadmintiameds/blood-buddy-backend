package bloodbuddy.backend.controller;

import bloodbuddy.backend.common.ApiResponse;
import bloodbuddy.backend.common.PagedResponse;
import bloodbuddy.backend.dto.donor.DonorImportBatchResponse;
import bloodbuddy.backend.dto.donor.DonorImportConfirmRequest;
import bloodbuddy.backend.dto.donor.DonorImportRowResponse;
import bloodbuddy.backend.dto.donor.DonorImportRowUpdateRequest;
import bloodbuddy.backend.enums.DonorImportRowStatus;
import bloodbuddy.backend.security.CustomUserDetails;
import bloodbuddy.backend.service.DonorImportService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Bulk donor import from CSV. The upload is staged row by row for the superadmin to review (edit or
 * exclude rows); only confirm copies the valid rows into the donor table.
 */
@RestController
@RequestMapping("/donors/import")
@PreAuthorize("hasRole('SUPERADMIN')")
public class DonorImportController {

    private static final MediaType TEXT_CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);

    private final DonorImportService donorImportService;

    public DonorImportController(DonorImportService donorImportService) {
        this.donorImportService = donorImportService;
    }

    @GetMapping("/template")
    public ResponseEntity<String> template() {
        return csv("donor-import-template.csv", donorImportService.template());
    }

    /** Stage and validate every row of the CSV as a new PENDING batch. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DonorImportBatchResponse>> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success("File uploaded for review",
                donorImportService.upload(file, principal.getUsername())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<DonorImportBatchResponse>>> listBatches(
            @PageableDefault(sort = "uploadedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Import batches fetched", donorImportService.list(pageable)));
    }

    /** The current superadmin's unfinished import (no data when there is none), to offer resume/discard. */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<DonorImportBatchResponse>> getPending(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return donorImportService.getPending(principal.getUsername())
                .map(batch -> ResponseEntity.ok(ApiResponse.success("Pending import found", batch)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.success("No pending import")));
    }

    @GetMapping("/{batchId}")
    public ResponseEntity<ApiResponse<DonorImportBatchResponse>> getBatch(@PathVariable Long batchId) {
        return ResponseEntity.ok(ApiResponse.success("Import batch fetched", donorImportService.get(batchId)));
    }

    /**
     * Review table: staged rows, filterable by status (repeatable, e.g. status=DUPLICATE_IN_DB&status=DUPLICATE_IN_FILE),
     * excluded flag and search (case-insensitive substring across the row's text values; a plain number also matches the row number). Confirmed imports have no rows (they are deleted once the donors are added).
     */
    @GetMapping("/{batchId}/rows")
    public ResponseEntity<ApiResponse<PagedResponse<DonorImportRowResponse>>> listRows(
            @PathVariable Long batchId,
            @RequestParam(required = false) List<DonorImportRowStatus> status,
            @RequestParam(required = false) Boolean excluded,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 50, sort = "rowNo") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Import rows fetched",
                donorImportService.listRows(batchId, status, excluded, search, pageable)));
    }

    /** Correct a staged row's values; the batch is re-validated. */
    @PutMapping("/{batchId}/rows/{rowId}")
    public ResponseEntity<ApiResponse<DonorImportRowResponse>> updateRow(
            @PathVariable Long batchId,
            @PathVariable Long rowId,
            @Valid @RequestBody DonorImportRowUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Row updated",
                donorImportService.updateRow(batchId, rowId, request)));
    }

    @PatchMapping("/{batchId}/rows/{rowId}/exclude")
    public ResponseEntity<ApiResponse<DonorImportRowResponse>> excludeRow(
            @PathVariable Long batchId, @PathVariable Long rowId) {
        return ResponseEntity.ok(ApiResponse.success("Row excluded",
                donorImportService.setExcluded(batchId, rowId, true)));
    }

    @PatchMapping("/{batchId}/rows/{rowId}/include")
    public ResponseEntity<ApiResponse<DonorImportRowResponse>> includeRow(
            @PathVariable Long batchId, @PathVariable Long rowId) {
        return ResponseEntity.ok(ApiResponse.success("Row included",
                donorImportService.setExcluded(batchId, rowId, false)));
    }

    /** ERROR rows with an extra "errors" column, to fix offline. */
    @GetMapping("/{batchId}/errors")
    public ResponseEntity<String> errorReport(@PathVariable Long batchId) {
        return csv("donor-import-" + batchId + "-errors.csv", donorImportService.errorReport(batchId));
    }

    /** Copy the ready rows into the donor table; the superadmin attests offline consent for them. */
    @PostMapping("/{batchId}/confirm")
    public ResponseEntity<ApiResponse<DonorImportBatchResponse>> confirm(
            @PathVariable Long batchId,
            @Valid @RequestBody DonorImportConfirmRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success("Donors imported",
                donorImportService.confirm(batchId, request, principal.getUsername())));
    }

    @PatchMapping("/{batchId}/discard")
    public ResponseEntity<ApiResponse<DonorImportBatchResponse>> discard(@PathVariable Long batchId) {
        return ResponseEntity.ok(ApiResponse.success("Import batch discarded", donorImportService.discard(batchId)));
    }

    private static ResponseEntity<String> csv(String fileName, String body) {
        return ResponseEntity.ok()
                .contentType(TEXT_CSV)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(fileName).build().toString())
                .body(body);
    }
}
