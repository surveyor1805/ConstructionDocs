package ITMO.ConstructionDocs.service;

import ITMO.ConstructionDocs.exceptions.CustomException;
import ITMO.ConstructionDocs.model.db.entity.Company;
import ITMO.ConstructionDocs.model.db.entity.Project;
import ITMO.ConstructionDocs.model.db.entity.ProjectDoc;
import ITMO.ConstructionDocs.model.db.repository.ProjectRepository;
import ITMO.ConstructionDocs.model.dto.request.ProjectReq;
import ITMO.ConstructionDocs.model.dto.response.ProjectResp;
import ITMO.ConstructionDocs.model.enums.CommonStatus;
import ITMO.ConstructionDocs.utils.PaginationUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ObjectMapper objectMapper;
    private final ProjectRepository projectRepository;

    @Autowired
    @Lazy
    private ProjectDocService projectDocService;
    /*private final CompanyService companyService;*/

    public ProjectResp createProject(ProjectReq request) {
        projectRepository.findByNameIgnoreCase(request.getName())
                .ifPresent(project -> {
                    throw new CustomException(String.format("Project with such name: %s already exists", request.getName()), HttpStatus.BAD_REQUEST);
                });

        Project project = objectMapper.convertValue(request, Project.class);
        project.setCreatedAt(LocalDateTime.now());
        project.setStatus(CommonStatus.CREATED);

        Project save = projectRepository.save(project);

        return objectMapper.convertValue(save, ProjectResp.class);
    }

    public ProjectResp getProject(Long id) {
        return objectMapper.convertValue(getProjectFromDB(id), ProjectResp.class);
    }

    public Project getProjectFromDB(Long id) {
        return projectRepository.findById(id).orElseThrow(() -> new CustomException("Project not found", HttpStatus.NOT_FOUND));
    }

    public ProjectResp updateProject(Long id, ProjectReq request) {
        Project project = getProjectFromDB(id);

        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getAddress() != null) {
            project.setAddress(request.getAddress());
        }
        if (request.getFilesRootDirectory() != null) {
            project.setFilesRootDirectory(request.getFilesRootDirectory());
            if (!project.getProjectDocs().isEmpty()) {
                for (ProjectDoc projectDoc : project.getProjectDocs()) {
                    projectDocService.transferProjectDocFileToNewAddress(id, projectDoc.getId());
                }
            }
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }

        project.setUpdatedAt(LocalDateTime.now());
        project.setStatus(CommonStatus.UPDATED);

        Project save = projectRepository.save(project);

        return objectMapper.convertValue(save, ProjectResp.class);
    }

    public void deleteProject(Long id) {
        Project project = getProjectFromDB(id);
        project.setUpdatedAt(LocalDateTime.now());
        project.setStatus(CommonStatus.DELETED);
        projectRepository.save(project);
    }

    public Page<ProjectResp> getAllProjects(Integer page, Integer sizePerPage, String sort, Sort.Direction order, String filter) {
        Pageable pageRequest = PaginationUtil.getPageRequest(page, sizePerPage, sort, order);

        Page<Project> all;
        if (filter == null) {
            all = projectRepository.findAllNotDeleted(pageRequest, CommonStatus.DELETED);
        } else {
            all = projectRepository.findAllNotDeletedAndFiltered(pageRequest, CommonStatus.DELETED, filter.toLowerCase());
        }

        List<ProjectResp> content = all.getContent().stream()
                .map(project -> objectMapper.convertValue(project, ProjectResp.class))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageRequest, all.getTotalElements());
    }

    public Project updateProjectData(Project project) {
        return projectRepository.save(project);
    }

    /*public Page<ProjectResp> getProjectsByCompanyId(Long companyId, Integer page, Integer sizePerPage, String sort, Sort.Direction order) {
        Company company = companyService.getCompanyFromDB(companyId);

        if (company.getStatus().equals(CommonStatus.DELETED)) {
            throw new CustomException(String.format("Company Info with id: %d is DELETED from DataBase", companyId), HttpStatus.NO_CONTENT);
        }

        Pageable pageRequest = PaginationUtil.getPageRequest(page, sizePerPage, sort, order);

        Page<Project> all = projectRepository.findAllByCompanyId(companyId, pageRequest);

        List<ProjectResp> content = all.getContent().stream()
                .map(comment -> objectMapper.convertValue(comment, ProjectResp.class))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageRequest, all.getTotalElements());
    }*/

}
