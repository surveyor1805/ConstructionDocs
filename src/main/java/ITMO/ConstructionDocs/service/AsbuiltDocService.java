package ITMO.ConstructionDocs.service;

import ITMO.ConstructionDocs.exceptions.CustomException;
import ITMO.ConstructionDocs.model.db.entity.AsbuiltDoc;
import ITMO.ConstructionDocs.model.db.entity.Company;
import ITMO.ConstructionDocs.model.db.entity.ProjectDoc;
import ITMO.ConstructionDocs.model.db.entity.User;
import ITMO.ConstructionDocs.model.db.repository.AsbuiltDocRepository;
import ITMO.ConstructionDocs.model.dto.request.AsbuiltDocReq;
import ITMO.ConstructionDocs.model.dto.request.AsbuiltDocToCompanyReq;
import ITMO.ConstructionDocs.model.dto.request.AsbuiltDocToProjDocReq;
import ITMO.ConstructionDocs.model.dto.response.AsbuiltDocResp;
import ITMO.ConstructionDocs.model.enums.CommonStatus;
import ITMO.ConstructionDocs.model.enums.DocStatus;
import ITMO.ConstructionDocs.utils.PaginationUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class AsbuiltDocService {
    private final AsbuiltDocRepository asbuiltDocRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final ProjectDocService projectDocService;
    private final CompanyService companyService;

    public AsbuiltDoc getAsbuiltDocFromDB(UUID id) {
        return asbuiltDocRepository.findById(id).orElseThrow(() -> new CustomException("AsbuiltDoc not found", HttpStatus.NOT_FOUND));
    }

    public AsbuiltDocResp createAsbuiltDoc(AsbuiltDocReq request, MultipartFile file, Authentication auth) {
        try {
            AsbuiltDoc asbuiltDoc = objectMapper.convertValue(request, AsbuiltDoc.class);

            String fileFormat = file.getContentType();
            Long fileSize = file.getSize();
            if (fileSize > 10485760) {
                throw new CustomException("File is too big. No more than 10mb is allowed", HttpStatus.BAD_REQUEST);
            }
            asbuiltDoc.setFileFormat(fileFormat);
            asbuiltDoc.setFileSize(fileSize);

            File directory = new File(FILETEMP);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (!created) {
                    throw new IOException("Error occurred while creating a directory" + FILETEMP);
                }
            }

            String filePath = FILETEMP + request.getFileName();

            file.transferTo(new File(filePath));
            asbuiltDoc.setFileAddress(filePath);
            User currentUser = userService.getCurrentUser(auth);
            asbuiltDoc.setCreatedBy(currentUser);
            asbuiltDoc.setCreatedAt(LocalDateTime.now());

            AsbuiltDoc savedDoc = asbuiltDocRepository.save(asbuiltDoc);

            return objectMapper.convertValue(savedDoc, AsbuiltDocResp.class);

        } catch (IOException e) {
            throw new CustomException("Error occurred while saving the file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public AsbuiltDocResp updateAsbuiltDoc(UUID id, AsbuiltDocReq request, MultipartFile file, Authentication auth) {
        try {
            AsbuiltDoc asbuiltDoc = getAsbuiltDocFromDB(id);

            if (!asbuiltDoc.getFileName().equals(request.getFileName())) {
                throw new CustomException("File name cannot be changed during update, to add new file use CreateAsbuiltDoc instead", HttpStatus.BAD_REQUEST);
            }

            User currentUser = userService.getCurrentUser(auth);

            String fileFormat = file.getContentType();
            Long fileSize = file.getSize();
            if (fileSize > 10485760) {
                throw new CustomException("File is too big. No more than 10mb is allowed", HttpStatus.BAD_REQUEST);
            }

            if (request.getDocStatus() != null) {
                asbuiltDoc.setDocStatus(request.getDocStatus());
            }

            if (request.getAsbuiltCategory() != null) {
                asbuiltDoc.setAsbuiltCategory(request.getAsbuiltCategory());
            }

            if (request.getDescription() != null) {
                asbuiltDoc.setDescription(request.getDescription());
            }

            asbuiltDoc.setFileFormat(fileFormat);
            asbuiltDoc.setFileSize(fileSize);

            String filePath;
            if (asbuiltDoc.getProjectDoc() != null) {
                filePath = asbuiltDoc.getFileAddress();
            } else {
                filePath = FILETEMP + request.getFileName();
            }

            file.transferTo(new File(filePath));
            asbuiltDoc.setFileAddress(filePath);
            asbuiltDoc.setUpdatedBy(currentUser);
            asbuiltDoc.setUpdatedAt(LocalDateTime.now());

            AsbuiltDoc savedDoc = asbuiltDocRepository.save(asbuiltDoc);

            return objectMapper.convertValue(savedDoc, AsbuiltDocResp.class);

        } catch (IOException e) {
            throw new CustomException("Error occurred while updating the file", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public void deleteAsbuiltDoc(UUID id, Authentication auth) {
        try {
            AsbuiltDoc asbuiltDoc = getAsbuiltDocFromDB(id);

            asbuiltDoc.setDocStatus(DocStatus.WITHDRAWN);
            User currentUser = userService.getCurrentUser(auth);
            asbuiltDoc.setUpdatedBy(currentUser);
            asbuiltDoc.setUpdatedAt(LocalDateTime.now());

            String filePath = asbuiltDoc.getFileAddress();
            File fileToDelete = new File(filePath);
            if (fileToDelete.exists()) {
                boolean isDeleted = fileToDelete.delete();
                if (!isDeleted) {
                    System.out.println("Failed to delete the file, probably file was deleted before");
                }
            }

            asbuiltDocRepository.save(asbuiltDoc);

        } catch (Exception e) {
            throw new CustomException("Error occurred while deleting the document", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public AsbuiltDocResp getAsbuiltDoc(UUID id) {
        return objectMapper.convertValue(getAsbuiltDocFromDB(id), AsbuiltDocResp.class);
    }

    public Page<AsbuiltDocResp> getAllAsbuiltDocs(Integer page, Integer sizePerPage, String sort, Sort.Direction order, String filter) {
        Pageable pageRequest = PaginationUtil.getPageRequest(page, sizePerPage, sort, order);

        Page<AsbuiltDoc> all;
        if (filter == null) {
            all = asbuiltDocRepository.findAllNotDeleted(pageRequest, DocStatus.WITHDRAWN);
        } else {
            all = asbuiltDocRepository.findAllNotDeletedAndFiltered(pageRequest, DocStatus.WITHDRAWN, filter.toLowerCase());
        }

        List<AsbuiltDocResp> content = all.getContent().stream()
                .map(comment -> objectMapper.convertValue(comment, AsbuiltDocResp.class))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageRequest, all.getTotalElements());
    }

    public void addAsbuiltDocToProjectDoc(@Valid AsbuiltDocToProjDocReq request, Authentication auth) {
        AsbuiltDoc asbuiltDoc = getAsbuiltDocFromDB(request.getAsbuiltDocId());
        ProjectDoc projectDoc = projectDocService.getProjectDocFromDB(request.getProjectDocId());

        transferAsbuiltDocFileToNewAddress(projectDoc.getId(), asbuiltDoc.getId());

        asbuiltDoc.setUpdatedAt(LocalDateTime.now());
        asbuiltDoc.setUpdatedBy(userService.getCurrentUser(auth));

        projectDoc.getAsbuiltDocs().add(asbuiltDoc);
        projectDocService.updateProjectDocData(projectDoc);

        asbuiltDoc.setProjectDoc(projectDoc);
        asbuiltDocRepository.save(asbuiltDoc);
    }

    public void addAsbuiltDocToCompany(@Valid AsbuiltDocToCompanyReq request) {
        AsbuiltDoc asbuiltDoc = getAsbuiltDocFromDB(request.getAsbuiltDocId());
        Company company = companyService.getCompanyFromDB(request.getCompanyId());

        company.getAsbuiltDocs().add(asbuiltDoc);
        companyService.updateCompanyData(company);

        asbuiltDoc.setCompany(company);
        asbuiltDocRepository.save(asbuiltDoc);
    }

    public AsbuiltDoc updateAsbuiltDocData(AsbuiltDoc asbuiltDoc) {
        return asbuiltDocRepository.save(asbuiltDoc);
    }

    public Page<AsbuiltDocResp> getAsbuiltDocsByCompanyId(Long companyId, Integer page, Integer sizePerPage, String sort, Sort.Direction order) {
        Company company = companyService.getCompanyFromDB(companyId);

        if (company.getStatus().equals(CommonStatus.DELETED)) {
            throw new CustomException(String.format("Company Info with id: %d is DELETED from DataBase", companyId), HttpStatus.NO_CONTENT);
        }

        Pageable pageRequest = PaginationUtil.getPageRequest(page, sizePerPage, sort, order);

        Page<AsbuiltDoc> all = asbuiltDocRepository.findAllByCompanyId(companyId, pageRequest);

        List<AsbuiltDocResp> content = all.getContent().stream()
                .map(asbuiltDoc -> objectMapper.convertValue(asbuiltDoc, AsbuiltDocResp.class))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageRequest, all.getTotalElements());
    }

    public Page<AsbuiltDocResp> getAsbuiltDocsByProjectDocId(UUID projectDocId, Integer page, Integer sizePerPage, String sort, Sort.Direction order) {
        ProjectDoc projectDoc = projectDocService.getProjectDocFromDB(projectDocId);

        if (projectDoc.getDocStatus().equals(DocStatus.WITHDRAWN)) {
            throw new CustomException(String.format("ProjectDoc Info with id: %d is WITHDRAWN from DataBase", projectDocId), HttpStatus.NO_CONTENT);
        }

        Pageable pageRequest = PaginationUtil.getPageRequest(page, sizePerPage, sort, order);

        Page<AsbuiltDoc> all = asbuiltDocRepository.findAllByProjectDocId(projectDocId, pageRequest);

        List<AsbuiltDocResp> content = all.getContent().stream()
                .map(asbuiltDoc -> objectMapper.convertValue(asbuiltDoc, AsbuiltDocResp.class))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageRequest, all.getTotalElements());
    }

    public Page<AsbuiltDocResp> getAsbuiltDocsForLastWeek(Integer page, Integer sizePerPage, String sort, Sort.Direction order) {
        Pageable pageRequest = PaginationUtil.getPageRequest(page, sizePerPage, sort, order);

        LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);

        Page<AsbuiltDoc> all = asbuiltDocRepository.findAllForLastWeek(lastWeek, pageRequest);

        List<AsbuiltDocResp> content = all.getContent().stream()
                .map(asbuiltDoc -> objectMapper.convertValue(asbuiltDoc, AsbuiltDocResp.class))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageRequest, all.getTotalElements());
    }

    public Page<AsbuiltDocResp> getAsbuiltDocsByProjectDocIdForLastWeek(UUID projectDocId, Integer page, Integer sizePerPage, String sort, Sort.Direction order) {
        ProjectDoc projectDoc = projectDocService.getProjectDocFromDB(projectDocId);

        if (projectDoc.getDocStatus().equals(DocStatus.WITHDRAWN)) {
            throw new CustomException(String.format("ProjectDoc Info with id: %d is WITHDRAWN from DataBase", projectDocId), HttpStatus.NO_CONTENT);
        }

        LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);

        Pageable pageRequest = PaginationUtil.getPageRequest(page, sizePerPage, sort, order);

        Page<AsbuiltDoc> all = asbuiltDocRepository.findAllByProjectDocIdForLastWeek(projectDocId, lastWeek, pageRequest);

        List<AsbuiltDocResp> content = all.getContent().stream()
                .map(asbuiltDoc -> objectMapper.convertValue(asbuiltDoc, AsbuiltDocResp.class))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageRequest, all.getTotalElements());
    }

    public void transferAsbuiltDocFileToNewAddress(UUID projectDocId, UUID asbuiltDocId) {
        ProjectDoc projectDoc = projectDocService.getProjectDocFromDB(projectDocId);
        AsbuiltDoc asbuiltDoc = getAsbuiltDocFromDB(asbuiltDocId);

        String oldFilePath = asbuiltDoc.getFileAddress();
        String newFilePath = projectDoc.getFileAddress() + "-ABDs" + File.separator + asbuiltDoc.getFileName();

        File oldFile = new File(oldFilePath);
        File newFile = new File(newFilePath);

        if (!oldFile.exists()) {
            throw new CustomException("AsbuiltDoc file was not found: " + oldFile.getPath(), HttpStatus.NOT_FOUND);
        }

        File parentDir = newFile.getParentFile();
        if (!parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                throw new CustomException("Error occurred while creating directories for the new file path: " + newFile.getPath(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        if (oldFile.renameTo(newFile)) {
            asbuiltDoc.setFileAddress(newFilePath);
        } else {
            throw new CustomException("Error occurred while transferring the file to new path: " + newFile.getPath(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        asbuiltDocRepository.save(asbuiltDoc);
    }

}

