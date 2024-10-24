package ITMO.ConstructionDocs.service;

import ITMO.ConstructionDocs.model.db.entity.Company;
import ITMO.ConstructionDocs.model.db.entity.Project;
import ITMO.ConstructionDocs.model.db.repository.CompanyRepository;
import ITMO.ConstructionDocs.model.dto.request.CompanyReq;
import ITMO.ConstructionDocs.model.dto.request.CompanyToProjectReq;
import ITMO.ConstructionDocs.model.dto.response.CompanyResp;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {
    @InjectMocks
    CompanyService companyService;

    @Spy
    ObjectMapper objectMapper;

    @Mock
    CompanyRepository companyRepository;

    @Mock
    ProjectService projectService;

    @Test
    void createCompany() {
        CompanyReq companyReq = new CompanyReq();
        companyReq.setName("test");

        Company company = new Company();
        company.setId(1L);

        when(companyRepository.save(any(Company.class))).thenReturn(company);

        CompanyResp companyResp = companyService.createCompany(companyReq);

        assertEquals(company.getId(), companyResp.getId());
    }

    @Test
    void addCompanyToProject() {
        Project project = new Project();
        project.setId(1L);
        project.setCompanies(new ArrayList<>());

        Company company = new Company();
        company.setId(1L);
        company.setProjects(new ArrayList<>());

        CompanyToProjectReq companyToProjectReq = new CompanyToProjectReq();
        companyToProjectReq.setCompanyId(company.getId());
        companyToProjectReq.setProjectId(project.getId());

        when(projectService.getProjectFromDB(project.getId())).thenReturn(project);
        when(companyRepository.findById(company.getId())).thenReturn(java.util.Optional.of(company));

        companyService.addCompanyToProject(companyToProjectReq);

        verify(projectService, times(1)).updateProjectData(project);
        verify(companyRepository, times(1)).save(company);

        assertTrue(company.getProjects().contains(project));
        assertTrue(project.getCompanies().contains(company));
    }

    @Test
    void getCompany() {
        Company company = new Company();
        company.setId(1L);

        when(companyRepository.findById(company.getId())).thenReturn(java.util.Optional.of(company));

        CompanyResp companyResp = companyService.getCompany(company.getId());

        assertEquals(company.getId(), companyResp.getId());
    }

    @Test
    void getCompanyFromDB() {
    }

    @Test
    void updateCompany() {
        CompanyReq companyReq = new CompanyReq();
        companyReq.setName("test");
        companyReq.setAddress("test");

        Company company = new Company();
        company.setId(1L);
        company.setName("oldTest");
        company.setAddress("oldTest");
        company.setTaxpayerIdentificationNumber("oldNumber");

        when(companyRepository.findById(company.getId())).thenReturn(java.util.Optional.of(company));
        when(companyRepository.save(any(Company.class))).thenReturn(company);

        CompanyResp companyResp = companyService.updateCompany(company.getId(), companyReq);
        assertEquals(company.getId(), companyResp.getId());
        assertEquals(companyReq.getName(), companyResp.getName());
        assertEquals(companyReq.getAddress(), companyResp.getAddress());
        assertEquals(company.getTaxpayerIdentificationNumber(), companyResp.getTaxpayerIdentificationNumber());
    }

    @Test
    void deleteCompany() {
        Company company = new Company();
        company.setId(1L);
        company.setName("test");
        company.setAddress("test");
        company.setTaxpayerIdentificationNumber("test");

        when(companyRepository.findById(company.getId())).thenReturn(java.util.Optional.of(company));

        companyService.deleteCompany(company.getId());

        verify(companyRepository, times(1)).save(any(Company.class));
        assertEquals(CommonStatus.DELETED, company.getStatus());
    }

    @Test
    void getAllCompanies() {
        Integer page = 0;
        Integer sizePerPage = 10;
        String sort = "title";
        String filter = "test";
        Sort.Direction order = Sort.Direction.ASC;

        Company company1 = new Company();
        company1.setId(1L);
        company1.setName("test1");
        company1.setAddress("address");

        Company company2 = new Company();
        company2.setId(2L);
        company2.setName("name");
        company2.setAddress("test2");

        Company company3 = new Company();
        company3.setId(3L);
        company3.setName("rest");
        company3.setAddress("rest");

        List<Company> filteredCompanies = new ArrayList<>();

        when(companyRepository.findAllNotDeletedAndFiltered(any(Pageable.class), any(CommonStatus.class), any(String.class)))
                .thenAnswer(invocation -> {
                    List<Company> allCompanies = List.of(company1, company2, company3);

                    filteredCompanies.addAll(allCompanies.stream()
                            .filter(doc -> doc.getName().toLowerCase().contains(filter) ||
                                    doc.getAddress().toLowerCase().contains(filter))
                            .collect(Collectors.toList()));

                    return new PageImpl<>(filteredCompanies, invocation.getArgument(0), filteredCompanies.size());
                });

        Page result = companyService.getAllCompanies(page, sizePerPage, sort, order, filter);
        assertEquals(filteredCompanies.size(), result.getTotalElements());
        assertEquals(filteredCompanies.get(0).getId(), company1.getId());
        assertEquals(filteredCompanies.get(1).getId(), company2.getId());
    }

    @Test
    void updateCompanyData() {
    }

    @Test
    void getCompanyByProjectId() {
        Integer page = 0;
        Integer sizePerPage = 10;
        String sort = "title";
        Sort.Direction order = Sort.Direction.ASC;

        Project project = new Project();
        project.setId(1L);
        project.setStatus(CommonStatus.CREATED);

        Project project2 = new Project();
        project2.setId(2L);

        Company company = new Company();
        company.setId(1L);
        company.setName("test");
        company.setProjects(List.of(project));

        Company company2 = new Company();
        company2.setId(2L);
        company2.setName("test2");
        company2.setProjects(List.of(project2));

        Company company3 = new Company();
        company3.setId(3L);
        company3.setName("rest");
        company3.setProjects(List.of(project, project2));

        List<Company> filteredCompanies = new ArrayList<>();
        when(projectService.getProjectFromDB(project.getId())).thenReturn(project);

        when(companyRepository.findAllByProjectId(anyLong(), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    List<Company> allCompanies = List.of(company, company2, company3);

                    filteredCompanies.addAll(allCompanies.stream()
                            .filter(doc -> doc.getProjects().stream().anyMatch(p -> p.getId() == project.getId()))
                            .collect(Collectors.toList()));

                    return new PageImpl<>(filteredCompanies, invocation.getArgument(1), filteredCompanies.size());
                });

        Page<CompanyResp> result = companyService.getCompanyByProjectId(project.getId(), page, sizePerPage, sort, order);

        assertEquals(filteredCompanies.size(), result.getTotalElements());
        assertTrue(result.getContent().stream().anyMatch(companyResp -> companyResp.getId().equals(company.getId())));
        assertTrue(result.getContent().stream().anyMatch(companyResp -> companyResp.getId().equals(company3.getId())));
    }
}