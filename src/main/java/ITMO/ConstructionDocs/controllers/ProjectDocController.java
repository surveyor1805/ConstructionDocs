package ITMO.ConstructionDocs.controllers;

import ITMO.ConstructionDocs.model.dto.request.ProjectDocReq;
import ITMO.ConstructionDocs.model.dto.request.ProjectDocToProjectReq;
import ITMO.ConstructionDocs.model.dto.response.ProjectDocResp;
import ITMO.ConstructionDocs.service.ProjectDocService;
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

import static ITMO.ConstructionDocs.constants.Constants.DESIGN;

@Tag(name = "ProjectDocs")
@RestController
@RequestMapping(DESIGN)
@RequiredArgsConstructor
public class ProjectDocController {

    private final ProjectDocService projectDocService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create projectDoc")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DESIGNER')")
    public ProjectDocResp createProjectDoc(
            @Parameter(description = "File to upload", required = true)
            @RequestPart(value = "file") MultipartFile file,
            ProjectDocReq request,
            Authentication authentication) {
        return projectDocService.createProjectDoc(request, file, authentication);
    }

    @PostMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update projectDoc")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DESIGNER')")
    public ProjectDocResp updateProjectDoc(
            @PathVariable UUID id,
            @Parameter(description = "File to upload", required = true)
            @RequestPart("file") MultipartFile file,
            ProjectDocReq request,
            Authentication authentication) {
        return projectDocService.updateProjectDoc(id, request, file, authentication);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete projectDoc")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DESIGNER')")
    public void deleteProjectDoc(@PathVariable UUID id, Authentication authentication) {
        projectDocService.deleteProjectDoc(id, authentication);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get projectDoc by id")
    @PreAuthorize("isAuthenticated()")
    public ProjectDocResp getProjectDoc(@PathVariable UUID id) {
        return projectDocService.getProjectDoc(id);
    }

    @GetMapping("/all")
    @Operation(summary = "Get list of projectDocs")
    @PreAuthorize("isAuthenticated()")
    public Page<ProjectDocResp> getAllProjectDocs(@RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer sizePerPage,
                                                  @RequestParam(defaultValue = "fileName") String sort,
                                                  @RequestParam(defaultValue = "ASC") Sort.Direction order,
                                                  @RequestParam(required = false) String filter) {
        return projectDocService.getAllProjectDocs(page, sizePerPage, sort, order, filter);
    }

    @PostMapping("/setProjectDocToProject")
    @Operation(summary = "set ProjectDoc to Project")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DESIGNER')")
    public void addProjectDocToProject(@RequestBody @Valid ProjectDocToProjectReq request, Authentication authentication) {
        projectDocService.addProjectDocToProject(request, authentication);
    }

    @GetMapping("/allByProjectId")
    @Operation(summary = "Get list of projectDocs by Project id")
    @PreAuthorize("isAuthenticated()")
    public Page<ProjectDocResp> getProjectDocsByProjectId(@RequestParam Long projectId,
                                                          @RequestParam(defaultValue = "1") Integer page,
                                                          @RequestParam(defaultValue = "10") Integer sizePerPage,
                                                          @RequestParam(defaultValue = "fileName") String sort,
                                                          @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return projectDocService.getProjectDocsByProjectId(projectId, page, sizePerPage, sort, order);
    }

    @GetMapping("/allByProjectIdForLastWeek")
    @Operation(summary = "Get list of projectDocs by Project id for last week")
    @PreAuthorize("isAuthenticated()")
    public Page<ProjectDocResp> getProjectDocsForLastWeekByProjId(@RequestParam Long projectId,
                                                                  @RequestParam(defaultValue = "1") Integer page,
                                                                  @RequestParam(defaultValue = "10") Integer sizePerPage,
                                                                  @RequestParam(defaultValue = "fileName") String sort,
                                                                  @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return projectDocService.getProjectDocsForLastWeekByProjId(projectId, page, sizePerPage, sort, order);
    }
}
