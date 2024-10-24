package ITMO.ConstructionDocs.service;

import ITMO.ConstructionDocs.exceptions.CustomException;
import ITMO.ConstructionDocs.model.db.entity.Project;
import ITMO.ConstructionDocs.model.db.entity.ProjectDoc;
import ITMO.ConstructionDocs.model.db.entity.User;
import ITMO.ConstructionDocs.model.db.repository.ProjectDocRepository;
import ITMO.ConstructionDocs.model.dto.request.ProjectDocReq;
import ITMO.ConstructionDocs.model.dto.response.ProjectDocResp;
import ITMO.ConstructionDocs.model.enums.CommonStatus;
import ITMO.ConstructionDocs.model.enums.DesignCategory;
import ITMO.ConstructionDocs.model.enums.DocStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectDocServiceTest {
    @InjectMocks
    private ProjectDocService projectDocService;

    @Mock
    private ProjectDocRepository projectDocRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserService userService;

    @Spy
    private ObjectMapper objectMapper;

    @Mock
    private MultipartFile file;

    @Mock
    private Authentication auth;

    @Test
    void getProjectDocFromDB() {
    }

    @Test
    void createProjectDoc() throws IOException {
        ProjectDocReq projectDocReq = new ProjectDocReq();
        projectDocReq.setFileName("test.txt");

        when(file.getContentType()).thenReturn("txt");
        when(file.getSize()).thenReturn(5000L);
        doNothing().when(file).transferTo(any(File.class));

        User user = new User();
        user.setId(1L);
        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);

        ProjectDoc savedDoc = new ProjectDoc();
        savedDoc.setId(UUID.randomUUID());
        when(projectDocRepository.save(any(ProjectDoc.class))).thenReturn(savedDoc);

        ProjectDocResp projectDocResp = projectDocService.createProjectDoc(projectDocReq, file, auth);

        assertEquals(savedDoc.getId(), projectDocResp.getId());
    }

    @Test
    void updateProjectDoc() throws IOException {
        UUID projectDocId = UUID.randomUUID();
        ProjectDocReq projectDocReq = new ProjectDocReq();
        projectDocReq.setFileName("test.txt");
        projectDocReq.setDocStatus(DocStatus.APPROVED);
        projectDocReq.setDesignCategory(DesignCategory.ARCHITECTURAL);
        projectDocReq.setDescription("test description");

        ProjectDoc existingDoc = new ProjectDoc();
        existingDoc.setId(projectDocId);
        existingDoc.setFileName("test.txt");
        existingDoc.setDocStatus(DocStatus.INITIAL);
        existingDoc.setDesignCategory(DesignCategory.DETAIL);
        existingDoc.setFileAddress("test/path/test.txt");
        existingDoc.setDescription("old test description");

        when(projectDocRepository.findById(projectDocId)).thenReturn(Optional.of(existingDoc));

        when(file.getContentType()).thenReturn("txt");
        when(file.getSize()).thenReturn(5000L);
        doNothing().when(file).transferTo(any(File.class));

        User user = new User();
        user.setId(1L);
        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);

        when(projectDocRepository.save(any(ProjectDoc.class))).thenReturn(existingDoc);

        ProjectDocResp projectDocResp = projectDocService.updateProjectDoc(projectDocId, projectDocReq, file, auth);

        assertEquals(existingDoc.getId(), projectDocResp.getId());
        assertEquals(existingDoc.getFileName(), projectDocResp.getFileName());
        assertEquals(existingDoc.getDocStatus(), projectDocResp.getDocStatus());
        assertEquals(existingDoc.getDesignCategory(), projectDocResp.getDesignCategory());
        assertEquals(existingDoc.getFileAddress(), projectDocResp.getFileAddress());
        assertEquals(existingDoc.getDescription(), projectDocResp.getDescription());
    }

    @Test
    void updateProjectDoc_fileSizeIsTooBig() {
        UUID projectDocId = UUID.randomUUID();
        ProjectDocReq projectDocReq = new ProjectDocReq();
        projectDocReq.setFileName("test.txt");

        ProjectDoc existingDoc = new ProjectDoc();
        existingDoc.setId(projectDocId);
        existingDoc.setFileName("test.txt");

        when(projectDocRepository.findById(projectDocId)).thenReturn(Optional.of(existingDoc));

        when(file.getContentType()).thenReturn("txt");
        when(file.getSize()).thenReturn(50000000L);

        assertThrows(CustomException.class, () -> projectDocService.updateProjectDoc(projectDocId, projectDocReq, file, auth));

    }

    @Test
    void deleteProjectDoc() {
        ProjectDoc existingDoc = new ProjectDoc();
        existingDoc.setId(UUID.randomUUID());
        existingDoc.setFileName("test.txt");
        existingDoc.setDocStatus(DocStatus.INITIAL);
        existingDoc.setDesignCategory(DesignCategory.ARCHITECTURAL);
        existingDoc.setFileAddress("test/path/test.txt");
        when(projectDocRepository.findById(any(UUID.class))).thenReturn(Optional.of(existingDoc));

        User user = new User();
        user.setId(1L);
        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);

        projectDocService.deleteProjectDoc(existingDoc.getId(), auth);

        verify(projectDocRepository, times(1)).save(any(ProjectDoc.class));
        Assert.assertEquals(DocStatus.WITHDRAWN, existingDoc.getDocStatus());
    }

    @Test
    void getProjectDoc() {
        ProjectDoc projectDoc = new ProjectDoc();
        projectDoc.setId(UUID.randomUUID());

        when(projectDocRepository.findById(projectDoc.getId())).thenReturn(Optional.of(projectDoc));

        ProjectDocResp result = projectDocService.getProjectDoc(projectDoc.getId());

        Assert.assertEquals(projectDoc.getId(), result.getId());
    }

    @Test
    void getAllProjectDocs() {
        Integer page = 0;
        Integer sizePerPage = 10;
        String sort = "designCategory";
        String filter = "arch";
        Sort.Direction order = Sort.Direction.ASC;

        ProjectDoc projectDoc1 = new ProjectDoc();
        projectDoc1.setFileName("arch.txt");
        projectDoc1.setId(UUID.randomUUID());
        projectDoc1.setDesignCategory(DesignCategory.DETAIL);

        ProjectDoc projectDoc2 = new ProjectDoc();
        projectDoc2.setFileName("test2.txt");
        projectDoc2.setId(UUID.randomUUID());
        projectDoc2.setDesignCategory(DesignCategory.ELECTRICAL);

        ProjectDoc projectDoc3 = new ProjectDoc();
        projectDoc3.setFileName("test3.txt");
        projectDoc3.setId(UUID.randomUUID());
        projectDoc3.setDesignCategory(DesignCategory.ARCHITECTURAL);

        List<ProjectDoc> filteredDocs = new ArrayList<>();

        when(projectDocRepository.findAllNotDeletedAndFiltered(any(Pageable.class), any(DocStatus.class), any(String.class)))
                .thenAnswer(invocation -> {
                    List<ProjectDoc> allDocs = List.of(projectDoc1, projectDoc2, projectDoc3);

                    filteredDocs.addAll(allDocs.stream()
                            .filter(doc -> doc.getFileName().toLowerCase().contains(filter) ||
                                    doc.getDesignCategory().toString().toLowerCase().contains(filter))
                            .collect(Collectors.toList()));

                    return new PageImpl<>(filteredDocs, invocation.getArgument(0), filteredDocs.size());
                });

        Page result = projectDocService.getAllProjectDocs(page, sizePerPage, sort, order, filter);
        assertEquals(filteredDocs.size(), result.getTotalElements());
        assertEquals(filteredDocs.get(0).getId(), projectDoc1.getId());
        assertEquals(filteredDocs.get(1).getId(), projectDoc3.getId());
    }

    @Test
    void addProjectDocToProject() {
    }

    @Test
    void getProjectDocsByProjectId() {
        Integer page = 0;
        Integer sizePerPage = 10;
        String sort = "fileName";
        Sort.Direction order = Sort.Direction.ASC;

        Project project = new Project();
        project.setId(1L);
        project.setName("test");
        project.setStatus(CommonStatus.UPDATED);

        ProjectDoc projectDoc1 = new ProjectDoc();
        projectDoc1.setId(UUID.randomUUID());
        projectDoc1.setDocStatus(DocStatus.INITIAL);
        projectDoc1.setProject(project);

        ProjectDoc projectDoc2 = new ProjectDoc();
        projectDoc2.setId(UUID.randomUUID());
        projectDoc2.setDocStatus(DocStatus.WITHDRAWN);
        projectDoc2.setProject(project);

        ProjectDoc projectDoc3 = new ProjectDoc();
        projectDoc3.setId(UUID.randomUUID());
        projectDoc3.setDocStatus(DocStatus.APPROVED);
        projectDoc3.setProject(new Project());

        when(projectService.getProjectFromDB(anyLong())).thenReturn(project);
        List<ProjectDoc> allDocs = List.of(projectDoc1, projectDoc2, projectDoc3);
        List<ProjectDoc> filteredProjectDocs = new ArrayList<>();

        when(projectDocRepository.findAllByProjectId(anyLong(), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    filteredProjectDocs.addAll(allDocs.stream()
                            .filter(doc -> Objects.equals(doc.getProject().getId(), project.getId()))
                            .collect(Collectors.toList()));

                    return new PageImpl<>(filteredProjectDocs, invocation.getArgument(1), filteredProjectDocs.size());
                });

        Page<ProjectDocResp> result = projectDocService.getProjectDocsByProjectId(project.getId(), page, sizePerPage, sort, order);

        assertEquals(2, result.getTotalElements());
        assertTrue(result.stream().anyMatch(doc -> doc.getId().equals(projectDoc1.getId())));
        assertTrue(result.stream().anyMatch(doc -> doc.getId().equals(projectDoc2.getId())));
    }

    @Test
    void getProjectDocsForLastWeekByProjId() {
        Integer page = 0;
        Integer sizePerPage = 10;
        String sort = "fileName";
        Sort.Direction order = Sort.Direction.ASC;

        Project project = new Project();
        project.setId(1L);
        project.setName("test");
        project.setStatus(CommonStatus.UPDATED);

        ProjectDoc projectDoc1 = new ProjectDoc();
        projectDoc1.setId(UUID.randomUUID());
        projectDoc1.setDocStatus(DocStatus.INITIAL);
        projectDoc1.setProject(project);
        projectDoc1.setCreatedAt(LocalDateTime.now().minusDays(5));

        ProjectDoc projectDoc2 = new ProjectDoc();
        projectDoc2.setId(UUID.randomUUID());
        projectDoc2.setDocStatus(DocStatus.WITHDRAWN);
        projectDoc2.setProject(project);
        projectDoc2.setCreatedAt(LocalDateTime.now().minusDays(15));

        ProjectDoc projectDoc3 = new ProjectDoc();
        projectDoc3.setId(UUID.randomUUID());
        projectDoc3.setDocStatus(DocStatus.APPROVED);
        projectDoc3.setProject(new Project());
        projectDoc3.setCreatedAt(LocalDateTime.now().minusDays(5));

        when(projectService.getProjectFromDB(anyLong())).thenReturn(project);
        List<ProjectDoc> allDocs = List.of(projectDoc1, projectDoc2, projectDoc3);
        List<ProjectDoc> filteredProjectDocs = new ArrayList<>();

        when(projectDocRepository.findAllForLastWeekByProjectId(any(LocalDateTime.class), anyLong(), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    filteredProjectDocs.addAll(allDocs.stream()
                            .filter(doc -> Objects.equals(doc.getProject().getId(), project.getId()))
                            .filter(doc -> doc.getCreatedAt().isAfter(LocalDateTime.now().minusDays(7)))
                            .collect(Collectors.toList()));

                    return new PageImpl<>(filteredProjectDocs, invocation.getArgument(2), filteredProjectDocs.size());
                });

        Page<ProjectDocResp> result = projectDocService.getProjectDocsForLastWeekByProjId(project.getId(), page, sizePerPage, sort, order);

        assertEquals(1, result.getTotalElements());
        assertTrue(result.stream().anyMatch(doc -> doc.getId().equals(projectDoc1.getId())));
    }
}