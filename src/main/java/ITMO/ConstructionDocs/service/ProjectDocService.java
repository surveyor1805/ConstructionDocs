package ITMO.ConstructionDocs.service;

import ITMO.ConstructionDocs.exceptions.CustomException;
import ITMO.ConstructionDocs.model.db.entity.AsbuiltDoc;
import ITMO.ConstructionDocs.model.db.entity.Project;
import ITMO.ConstructionDocs.model.db.entity.ProjectDoc;
import ITMO.ConstructionDocs.model.db.entity.User;
import ITMO.ConstructionDocs.model.db.repository.ProjectDocRepository;
import ITMO.ConstructionDocs.model.dto.request.ProjectDocReq;
import ITMO.ConstructionDocs.model.dto.request.ProjectDocToProjectReq;
import ITMO.ConstructionDocs.model.dto.response.ProjectDocResp;
import ITMO.ConstructionDocs.model.enums.CommonStatus;
import ITMO.ConstructionDocs.model.enums.DocStatus;
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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static ITMO.ConstructionDocs.constants.Constants.FILETEMP;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectDocService {
    private final ProjectDocRepository projectDocRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final ProjectService projectService;

    @Autowired
    @Lazy
    private AsbuiltDocService asbuiltDocService;

    public ProjectDoc getProjectDocFromDB(UUID id) {
        return projectDocRepository.findById(id).orElseThrow(() -> new CustomException("ProjectDoc not found", HttpStatus.NOT_FOUND));
    }

    /*public void setProjectDocAddress(ProjectDoc projectDoc, Project project, String fileName) {
        String address = project.getFilesRootDirectory() + "\\designs\\" + fileName;
        projectDoc.setFileAddress(address);
    }*/

    public ProjectDocResp createProjectDoc(ProjectDocReq request, MultipartFile file, Authentication auth) {
        try {
            ProjectDoc projectDoc = objectMapper.convertValue(request, ProjectDoc.class);

            String fileFormat = file.getContentType();
            Long fileSize = file.getSize();
            if (fileSize > 10485760) {
                throw new CustomException("File is too big. No more than 10mb is allowed", HttpStatus.BAD_REQUEST);
            }
            projectDoc.setFileFormat(fileFormat);
            projectDoc.setFileSize(fileSize);

            File directory = new File(FILETEMP);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (!created) {
                    throw new IOException("Error occurred while creating a directory" + FILETEMP);
                }
            }

            String filePath = FILETEMP + request.getFileName();

            file.transferTo(new File(filePath));
            projectDoc.setFileAddress(filePath);
            User currentUser = userService.getCurrentUser(auth);
            projectDoc.setCreatedBy(currentUser);
            projectDoc.setCreatedAt(LocalDateTime.now());

            ProjectDoc savedDoc = projectDocRepository.save(projectDoc);

            return objectMapper.convertValue(savedDoc, ProjectDocResp.class);

        } catch (IOException e) {
            throw new CustomException("Error occurred while saving the file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ProjectDocResp updateProjectDoc(UUID id, ProjectDocReq request, MultipartFile file, Authentication auth) {
        try {
            ProjectDoc projectDoc = getProjectDocFromDB(id);

            if (!projectDoc.getFileName().equals(request.getFileName())) {
                throw new CustomException("File name cannot be changed during update, to add new file use CreateProjectDoc instead", HttpStatus.BAD_REQUEST);
            }

            User currentUser = userService.getCurrentUser(auth);

            String fileFormat = file.getContentType();
            Long fileSize = file.getSize();
            if (fileSize > 10485760) {
                throw new CustomException("File is too big. No more than 10mb is allowed", HttpStatus.BAD_REQUEST);
            }

            if (request.getDocStatus() != null) {
                projectDoc.setDocStatus(request.getDocStatus());
            }
            if (request.getDesignCategory() != null) {
                projectDoc.setDesignCategory(request.getDesignCategory());
            }
            if (request.getDescription() != null) {
                projectDoc.setDescription(request.getDescription());
            }

            projectDoc.setFileFormat(fileFormat);
            projectDoc.setFileSize(fileSize);

            String filePath;
            if (projectDoc.getProject() != null) {
                filePath = projectDoc.getFileAddress();
            } else {
                filePath = FILETEMP + request.getFileName();
            }

            file.transferTo(new File(filePath));
            projectDoc.setFileAddress(filePath);
            projectDoc.setUpdatedBy(currentUser);
            projectDoc.setUpdatedAt(LocalDateTime.now());

            ProjectDoc savedDoc = projectDocRepository.save(projectDoc);

            return objectMapper.convertValue(savedDoc, ProjectDocResp.class);

        } catch (IOException e) {
            throw new CustomException("Error occurred while updating the file", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public void deleteProjectDoc(UUID projectDocId, Authentication auth) {
        try {
            ProjectDoc projectDoc = getProjectDocFromDB(projectDocId);

            projectDoc.setDocStatus(DocStatus.WITHDRAWN);
            User currentUser = userService.getCurrentUser(auth);
            projectDoc.setUpdatedBy(currentUser);
            projectDoc.setUpdatedAt(LocalDateTime.now());

            String filePath = projectDoc.getFileAddress();
            File fileToDelete = new File(filePath);
            if (fileToDelete.exists()) {
                boolean isDeleted = fileToDelete.delete();
                if (!isDeleted) {
                    System.out.println("Failed to delete the file, probably file was deleted before");
                }
            }

            projectDocRepository.save(projectDoc);

        } catch (Exception e) {
            throw new CustomException("Error occurred while deleting the document", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ProjectDocResp getProjectDoc(UUID id) {
        return objectMapper.convertValue(getProjectDocFromDB(id), ProjectDocResp.class);
    }

    public Page<ProjectDocResp> getAllProjectDocs(Integer page, Integer sizePerPage, String sort, Sort.Direction order, String filter) {
        Pageable pageRequest = PaginationUtil.getPageRequest(page, sizePerPage, sort, order);

        Page<ProjectDoc> all;
        if (filter == null) {
            all = projectDocRepository.findAllNotDeleted(pageRequest, DocStatus.WITHDRAWN);
        } else {
            all = projectDocRepository.findAllNotDeletedAndFiltered(pageRequest, DocStatus.WITHDRAWN, filter.toLowerCase());
        }

        List<ProjectDocResp> content = all.getContent().stream()
                .map(comment -> objectMapper.convertValue(comment, ProjectDocResp.class))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageRequest, all.getTotalElements());
    }

    public void addProjectDocToProject(@Valid ProjectDocToProjectReq request, Authentication auth) {
        ProjectDoc projectDoc = getProjectDocFromDB(request.getProjectDocId());
        Project project = projectService.getProjectFromDB(request.getProjectId());

        transferProjectDocFileToNewAddress(project.getId(), projectDoc.getId());

        projectDoc.setUpdatedAt(LocalDateTime.now());
        projectDoc.setUpdatedBy(userService.getCurrentUser(auth));

        project.getProjectDocs().add(projectDoc);
        projectService.updateProjectData(project);

        projectDoc.setProject(project);
        projectDocRepository.save(projectDoc);
    }

    public ProjectDoc updateProjectDocData(ProjectDoc projectDoc) {
        return projectDocRepository.save(projectDoc);
    }

    public Page<ProjectDocResp> getProjectDocsByProjectId(Long projectId, Integer page, Integer sizePerPage, String sort, Sort.Direction order) {
        Project project = projectService.getProjectFromDB(projectId);

        if (project.getStatus().equals(CommonStatus.DELETED)) {
            throw new CustomException(String.format("Project Info with id: %d is DELETED from DataBase", projectId), HttpStatus.NO_CONTENT);
        }

        Pageable pageRequest = PaginationUtil.getPageRequest(page, sizePerPage, sort, order);

        Page<ProjectDoc> all = projectDocRepository.findAllByProjectId(projectId, pageRequest);

        List<ProjectDocResp> content = all.getContent().stream()
                .map(projectDoc -> objectMapper.convertValue(projectDoc, ProjectDocResp.class))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageRequest, all.getTotalElements());
    }

    public Page<ProjectDocResp> getProjectDocsForLastWeekByProjId(Long projectId, Integer page, Integer sizePerPage, String sort, Sort.Direction order) {
        Project project = projectService.getProjectFromDB(projectId);

        if (project.getStatus().equals(CommonStatus.DELETED)) {
            throw new CustomException(String.format("Project Info with id: %d is DELETED from DataBase", projectId), HttpStatus.NO_CONTENT);
        }

        LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);

        Pageable pageRequest = PaginationUtil.getPageRequest(page, sizePerPage, sort, order);

        Page<ProjectDoc> all = projectDocRepository.findAllForLastWeekByProjectId(lastWeek, projectId, pageRequest);

        List<ProjectDocResp> content = all.getContent().stream()
                .map(projectDoc -> objectMapper.convertValue(projectDoc, ProjectDocResp.class))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageRequest, all.getTotalElements());
    }

    public void transferProjectDocFileToNewAddress(Long projectId, UUID projectDocId) {
        Project project = projectService.getProjectFromDB(projectId);
        ProjectDoc projectDoc = getProjectDocFromDB(projectDocId);

        String oldFilePath = projectDoc.getFileAddress();
        String newFilePath = project.getFilesRootDirectory() + File.separator + projectDoc.getFileName();

        File oldFile = new File(oldFilePath);
        File newFile = new File(newFilePath);

        if (!oldFile.exists()) {
            throw new CustomException("ProjectDoc file was not found: " + oldFile.getPath(), HttpStatus.NOT_FOUND);
        }

        File parentDir = newFile.getParentFile();
        if (!parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                throw new CustomException("Error occurred while creating directories for the new file path: " + newFile.getPath(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        if (oldFile.renameTo(newFile)) {
            projectDoc.setFileAddress(newFilePath);
        } else {
            throw new CustomException("Error occurred while transferring the file to new path: " + newFile.getPath(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (!projectDoc.getAsbuiltDocs().isEmpty()) {
            for (AsbuiltDoc asbuiltDoc : projectDoc.getAsbuiltDocs()) {
                asbuiltDocService.transferAsbuiltDocFileToNewAddress(projectDoc.getId(), asbuiltDoc.getId());
            }
        }

        projectDocRepository.save(projectDoc);
    }
}
    

