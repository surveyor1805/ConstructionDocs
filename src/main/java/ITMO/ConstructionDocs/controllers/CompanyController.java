package ITMO.ConstructionDocs.controllers;

import ITMO.ConstructionDocs.model.dto.request.CompanyReq;
import ITMO.ConstructionDocs.model.dto.request.CompanyToProjectReq;
import ITMO.ConstructionDocs.model.dto.request.UserToCompanyReq;
import ITMO.ConstructionDocs.model.dto.response.CompanyResp;
import ITMO.ConstructionDocs.model.dto.response.ProjectResp;
import ITMO.ConstructionDocs.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static ITMO.ConstructionDocs.constants.Constants.COMPANY;

@Tag(name = "Companies")
@RestController
@RequestMapping(COMPANY)
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping()
    @Operation(summary = "Create company")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DESIGNER') or hasRole('DEVELOPER')")
    public CompanyResp createCompany(@RequestBody CompanyReq request) {
        return companyService.createCompany(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get company by id")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DESIGNER') or hasRole('DEVELOPER')")
    public CompanyResp getCompany(@PathVariable Long id) {
        return companyService.getCompany(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update company by id")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DESIGNER') or hasRole('DEVELOPER')")
    public CompanyResp updateCompany(@PathVariable Long id, @RequestBody CompanyReq request) {
        return companyService.updateCompany(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete company by id")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DESIGNER') or hasRole('DEVELOPER')")
    public void deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
    }

    @GetMapping("/all")
    @Operation(summary = "Get list of companies")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DESIGNER') or hasRole('DEVELOPER')")
    public Page<CompanyResp> getAllCompanies(@RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer sizePerPage,
                                             @RequestParam(defaultValue = "name") String sort,
                                             @RequestParam(defaultValue = "ASC") Sort.Direction order,
                                             @RequestParam(required = false) String filter) {
        return companyService.getAllCompanies(page, sizePerPage, sort, order, filter);
    }

    @GetMapping("/allByProjectId")
    @Operation(summary = "Get list of companies by Project id")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DESIGNER') or hasRole('DEVELOPER')")
    public Page<CompanyResp> getCompanyByProjectId(@RequestParam Long projectId,
                                                   @RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "10") Integer sizePerPage,
                                                   @RequestParam(defaultValue = "name") String sort,
                                                   @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return companyService.getCompanyByProjectId(projectId, page, sizePerPage, sort, order);
    }

    @PostMapping("/setCompanyToProject")
    @Operation(summary = "set Company to Project")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DESIGNER') or hasRole('DEVELOPER')")
    public void addCompanyToProject(@RequestBody @Valid CompanyToProjectReq request) {
        companyService.addCompanyToProject(request);
    }
}
