package ITMO.ConstructionDocs.controllers;

import ITMO.ConstructionDocs.model.dto.request.ProjectReq;
import ITMO.ConstructionDocs.model.dto.response.ProjectResp;
import ITMO.ConstructionDocs.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static ITMO.ConstructionDocs.constants.Constants.PROJECT;

@Tag(name = "Projects")
@RestController
@RequestMapping(PROJECT)
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping()
    @Operation(summary = "Create project")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DESIGNER')")
    public ProjectResp createProject(@RequestBody ProjectReq request) {
        return projectService.createProject(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by id")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DESIGNER')")
    public ProjectResp getProject(@PathVariable Long id) {
        return projectService.getProject(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update project by id")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DESIGNER')")
    public ProjectResp updateProject(@PathVariable Long id, @RequestBody ProjectReq request) {
        return projectService.updateProject(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete project by id")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DESIGNER')")
    public void deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
    }

    @GetMapping("/all")
    @Operation(summary = "Get list of projects")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DESIGNER')")
    public Page<ProjectResp> getAllProjects(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer sizePerPage,
                                            @RequestParam(defaultValue = "name") String sort,
                                            @RequestParam(defaultValue = "ASC") Sort.Direction order,
                                            @RequestParam(required = false) String filter) {
        return projectService.getAllProjects(page, sizePerPage, sort, order, filter);
    }

}
