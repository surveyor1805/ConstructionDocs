package ITMO.ConstructionDocs.service;

import ITMO.ConstructionDocs.model.db.entity.Project;
import ITMO.ConstructionDocs.model.db.repository.ProjectRepository;
import ITMO.ConstructionDocs.model.dto.request.ProjectReq;
import ITMO.ConstructionDocs.model.dto.response.ProjectResp;
import ITMO.ConstructionDocs.model.enums.CommonStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {
    @InjectMocks
    ProjectService projectService;

    @Spy
    ObjectMapper objectMapper;

    @Mock
    ProjectRepository projectRepository;

    @Test
    void createProject() {
        ProjectReq projectReq = new ProjectReq();
        projectReq.setName("test");

        Project project = new Project();
        project.setId(1L);

        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectResp projectResp = projectService.createProject(projectReq);

        assertEquals(project.getId(), projectResp.getId());
    }

    @Test
    void getProject() {
        Project project = new Project();
        project.setId(1L);

        when(projectRepository.findById(project.getId())).thenReturn(java.util.Optional.of(project));

        ProjectResp projectResp = projectService.getProject(project.getId());

        assertEquals(project.getId(), projectResp.getId());
    }

    @Test
    void getProjectFromDB() {
    }

    @Test
    void updateProject() {
        ProjectReq projectReq = new ProjectReq();
        projectReq.setAddress("test");
        projectReq.setDescription("test");

        Project project = new Project();
        project.setId(1L);
        project.setName("oldTest");
        project.setAddress("oldTest");
        project.setDescription("oldTest");

        when(projectRepository.findById(project.getId())).thenReturn(java.util.Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectResp projectResp = projectService.updateProject(project.getId(), projectReq);
        assertEquals(project.getId(), projectResp.getId());
        assertEquals(project.getName(), projectResp.getName());
        assertEquals(projectReq.getAddress(), projectResp.getAddress());
        assertEquals(projectReq.getDescription(), projectResp.getDescription());
    }

    @Test
    void deleteProject() {
        Project project = new Project();
        project.setId(1L);
        project.setName("test");
        project.setAddress("test");
        project.setDescription("test");

        when(projectRepository.findById(project.getId())).thenReturn(java.util.Optional.of(project));
        projectService.deleteProject(project.getId());
        verify(projectRepository, times(1)).save(any(Project.class));
        assertEquals(project.getStatus(), CommonStatus.DELETED);
    }

    @Test
    void getAllProjects() {
        Integer page = 0;
        Integer sizePerPage = 10;
        String sort = "name";
        String filter = "test";
        Sort.Direction order = Sort.Direction.ASC;

        Project project1 = new Project();
        project1.setId(1L);
        project1.setName("test1");
        project1.setAddress("address");

        Project project2 = new Project();
        project2.setId(2L);
        project2.setName("name");
        project2.setAddress("address");

        Project project3 = new Project();
        project3.setId(3L);
        project3.setName("rest");
        project3.setAddress("test");

        List<Project> filteredProjects = new ArrayList<>();

        when(projectRepository.findAllNotDeletedAndFiltered(any(Pageable.class), any(CommonStatus.class), any(String.class)))
                .thenAnswer(invocation -> {
                    List<Project> allProjects = List.of(project1, project2, project3);

                    filteredProjects.addAll(allProjects.stream()
                            .filter(doc -> doc.getName().toLowerCase().contains(filter) ||
                                    doc.getAddress().toLowerCase().contains(filter))
                            .collect(Collectors.toList()));

                    return new PageImpl<>(filteredProjects, invocation.getArgument(0), filteredProjects.size());
                });

        Page<ProjectResp> result = projectService.getAllProjects(page, sizePerPage, sort, order, filter);
        assertEquals(filteredProjects.size(), result.getTotalElements());
        assertEquals(filteredProjects.get(0).getId(), project1.getId());
        assertEquals(filteredProjects.get(1).getId(), project3.getId());
    }

    @Test
    void updateProjectData() {
    }
}