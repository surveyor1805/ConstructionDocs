package ITMO.ConstructionDocs.controllers;

import ITMO.ConstructionDocs.model.dto.request.AsbuiltDocReq;
import ITMO.ConstructionDocs.model.dto.request.AsbuiltDocToCompanyReq;
import ITMO.ConstructionDocs.model.dto.request.AsbuiltDocToProjDocReq;
import ITMO.ConstructionDocs.model.dto.response.AsbuiltDocResp;
import ITMO.ConstructionDocs.service.AsbuiltDocService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.UUID;

import static ITMO.ConstructionDocs.constants.Constants.ASBUILT;

@Tag(name = "AsbuiltDocs")
@RestController
@RequestMapping(ASBUILT)
@RequiredArgsConstructor
public class AsbuiltDocController {

    private final AsbuiltDocService asbuiltDocService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create asbuiltDoc")
    @PreAuthorize("isAuthenticated()")
    public AsbuiltDocResp createAsbuiltDoc(
            @Parameter(description = "File to upload", required = true)
            @RequestPart("file") MultipartFile file,
            AsbuiltDocReq request,
            Authentication authentication) {
        return asbuiltDocService.createAsbuiltDoc(request, file, authentication);
    }

    @PostMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update asbuiltDoc")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DESIGNER') or hasRole('DEVELOPER')")
    public AsbuiltDocResp updateAsbuiltDoc(
            @PathVariable UUID id,
            @Parameter(description = "File to upload", required = true)
            @RequestPart("file") MultipartFile file,
            AsbuiltDocReq request,
            Authentication authentication) {
        return asbuiltDocService.updateAsbuiltDoc(id, request, file, authentication);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete asbuiltDoc")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DESIGNER') or hasRole('DEVELOPER')")
    public void deleteAsbuiltDoc(@PathVariable UUID id, Authentication authentication) {
        asbuiltDocService.deleteAsbuiltDoc(id, authentication);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get asbuiltDoc by id")
    @PreAuthorize("isAuthenticated()")
    public AsbuiltDocResp getAsbuiltDoc(@PathVariable UUID id) {
        return asbuiltDocService.getAsbuiltDoc(id);
    }

    @GetMapping("/all")
    @Operation(summary = "Get list of asbuiltDocs")
    @PreAuthorize("isAuthenticated()")
    public Page<AsbuiltDocResp> getAllAsbuiltDocs(@RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer sizePerPage,
                                                  @RequestParam(defaultValue = "fileName") String sort,
                                                  @RequestParam(defaultValue = "ASC") Sort.Direction order,
                                                  @RequestParam(required = false) String filter) {
        return asbuiltDocService.getAllAsbuiltDocs(page, sizePerPage, sort, order, filter);
    }

    @PostMapping("/setAsbuiltDocToProjectDoc")
    @Operation(summary = "set AsbuiltDoc to ProjectDoc")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DESIGNER') or hasRole('DEVELOPER')")
    public void addAsbuiltDocToProjectDoc(@RequestBody @Valid AsbuiltDocToProjDocReq request, Authentication authentication) {
        asbuiltDocService.addAsbuiltDocToProjectDoc(request, authentication);
    }

    @PostMapping("/setAsbuiltDocToCompany")
    @Operation(summary = "set AsbuiltDoc to Company")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DESIGNER') or hasRole('DEVELOPER')")
    public void addAsbuiltDocToCompany(@RequestBody @Valid AsbuiltDocToCompanyReq request) {
        asbuiltDocService.addAsbuiltDocToCompany(request);
    }

    @GetMapping("/allByCompanyId")
    @Operation(summary = "Get list of asbuiltDocs by Company id")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DESIGNER') or hasRole('DEVELOPER')")
    public Page<AsbuiltDocResp> getAsbuiltDocsByCompanyId(@RequestParam Long companyId,
                                                          @RequestParam(defaultValue = "1") Integer page,
                                                          @RequestParam(defaultValue = "10") Integer sizePerPage,
                                                          @RequestParam(defaultValue = "fileName") String sort,
                                                          @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return asbuiltDocService.getAsbuiltDocsByCompanyId(companyId, page, sizePerPage, sort, order);
    }

    @GetMapping("/allByProjectDocId")
    @Operation(summary = "Get list of asbuiltDocs by ProjectDoc id")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DESIGNER') or hasRole('DEVELOPER')")
    public Page<AsbuiltDocResp> getAsbuiltDocsByProjectDocId(@RequestParam UUID projectDocId,
                                                             @RequestParam(defaultValue = "1") Integer page,
                                                             @RequestParam(defaultValue = "10") Integer sizePerPage,
                                                             @RequestParam(defaultValue = "fileName") String sort,
                                                             @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return asbuiltDocService.getAsbuiltDocsByProjectDocId(projectDocId, page, sizePerPage, sort, order);
    }

    @GetMapping("/allForLastWeek")
    @Operation(summary = "Get list of asbuiltDocs for last week")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DESIGNER') or hasRole('DEVELOPER')")
    public Page<AsbuiltDocResp> getAsbuiltDocsForLastWeek(@RequestParam(defaultValue = "1") Integer page,
                                                          @RequestParam(defaultValue = "10") Integer sizePerPage,
                                                          @RequestParam(defaultValue = "fileName") String sort,
                                                          @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return asbuiltDocService.getAsbuiltDocsForLastWeek(page, sizePerPage, sort, order);
    }

    @GetMapping("/allByProjDocIdForLastWeek")
    @Operation(summary = "Get list of asbuiltDocs by ProjectDoc Id for last week")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DESIGNER') or hasRole('DEVELOPER')")
    public Page<AsbuiltDocResp> getAsbuiltDocsByProjectDocIdForLastWeek(@RequestParam UUID projectDocId,
                                                                        @RequestParam(defaultValue = "1") Integer page,
                                                                        @RequestParam(defaultValue = "10") Integer sizePerPage,
                                                                        @RequestParam(defaultValue = "fileName") String sort,
                                                                        @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return asbuiltDocService.getAsbuiltDocsByProjectDocIdForLastWeek(projectDocId, page, sizePerPage, sort, order);
    }
}
