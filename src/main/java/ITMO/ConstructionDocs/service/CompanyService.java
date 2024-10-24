package ITMO.ConstructionDocs.service;

import ITMO.ConstructionDocs.exceptions.CustomException;
import ITMO.ConstructionDocs.model.db.entity.Company;
import ITMO.ConstructionDocs.model.db.entity.Project;
import ITMO.ConstructionDocs.model.db.repository.CompanyRepository;
import ITMO.ConstructionDocs.model.dto.request.CommentToProjDocReq;
import ITMO.ConstructionDocs.model.dto.request.CompanyReq;
import ITMO.ConstructionDocs.model.dto.request.CompanyToProjectReq;
import ITMO.ConstructionDocs.model.dto.response.CompanyResp;
import ITMO.ConstructionDocs.model.dto.response.ProjectResp;
import ITMO.ConstructionDocs.model.enums.CommonStatus;
import ITMO.ConstructionDocs.utils.PaginationUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {
    private final ObjectMapper objectMapper;
    private final CompanyRepository companyRepository;
    private final ProjectService projectService;

    public CompanyResp createCompany(CompanyReq request) {
        companyRepository.findByNameIgnoreCase(request.getName())
                .ifPresent(company -> {
                    throw new CustomException(String.format("Company with such name: %s already exists", request.getName()), HttpStatus.BAD_REQUEST);
                });

        Company company = objectMapper.convertValue(request, Company.class);
        company.setCreatedAt(LocalDateTime.now());
        company.setStatus(CommonStatus.CREATED);

        Company save = companyRepository.save(company);

        return objectMapper.convertValue(save, CompanyResp.class);
    }

    public CompanyResp getCompany(Long id) {
        return objectMapper.convertValue(getCompanyFromDB(id), CompanyResp.class);
    }

    public Company getCompanyFromDB(Long id) {
        return companyRepository.findById(id).orElseThrow(() -> new CustomException("Company not found", HttpStatus.NOT_FOUND));
    }

    public CompanyResp updateCompany(Long id, CompanyReq request) {
        Company company = getCompanyFromDB(id);

        if (request.getName() != null) {
            company.setName(request.getName());
        }
        if (request.getAddress() != null) {
            company.setAddress(request.getAddress());
        }
        if (request.getDescription() != null) {
            company.setDescription(request.getDescription());
        }
        if (request.getRegistrationNumber() != null) {
            company.setRegistrationNumber(request.getRegistrationNumber());
        }
        if (request.getTaxpayerIdentificationNumber() != null) {
            company.setTaxpayerIdentificationNumber(request.getTaxpayerIdentificationNumber());
        }

        company.setUpdatedAt(LocalDateTime.now());
        company.setStatus(CommonStatus.UPDATED);

        Company save = companyRepository.save(company);

        return objectMapper.convertValue(save, CompanyResp.class);
    }

    public void deleteCompany(Long id) {
        Company company = getCompanyFromDB(id);
        company.setUpdatedAt(LocalDateTime.now());
        company.setStatus(CommonStatus.DELETED);
        companyRepository.save(company);
    }

    public Page<CompanyResp> getAllCompanies(Integer page, Integer sizePerPage, String sort, Sort.Direction order, String filter) {
        Pageable pageRequest = PaginationUtil.getPageRequest(page, sizePerPage, sort, order);

        Page<Company> all;
        if (filter == null) {
            all = companyRepository.findAllNotDeleted(pageRequest, CommonStatus.DELETED);
        } else {
            all = companyRepository.findAllNotDeletedAndFiltered(pageRequest, CommonStatus.DELETED, filter.toLowerCase());
        }

        List<CompanyResp> content = all.getContent().stream()
                .map(company -> objectMapper.convertValue(company, CompanyResp.class))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageRequest, all.getTotalElements());
    }

    public Company updateCompanyData(Company company) {
        return companyRepository.save(company);
    }

    public Page<CompanyResp> getCompanyByProjectId(Long projectId, Integer page, Integer sizePerPage, String sort, Sort.Direction order) {
        Project project = projectService.getProjectFromDB(projectId);

        if (project.getStatus().equals(CommonStatus.DELETED)) {
            throw new CustomException(String.format("Project Info with id: %d is DELETED from DataBase", projectId), HttpStatus.NO_CONTENT);
        }

        Pageable pageRequest = PaginationUtil.getPageRequest(page, sizePerPage, sort, order);

        Page<Company> all = companyRepository.findAllByProjectId(projectId, pageRequest);

        List<CompanyResp> content = all.getContent().stream()
                .map(company -> objectMapper.convertValue(company, CompanyResp.class))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageRequest, all.getTotalElements());
    }

    public void addCompanyToProject(@Valid CompanyToProjectReq companyToProjectReq) {
        Company company = getCompanyFromDB(companyToProjectReq.getCompanyId());
        Project project = projectService.getProjectFromDB(companyToProjectReq.getProjectId());

        project.getCompanies().add(company);
        projectService.updateProjectData(project);

        company.getProjects().add(project);
        companyRepository.save(company);
    }
}
