package ITMO.ConstructionDocs.service;

import ITMO.ConstructionDocs.exceptions.CustomException;
import ITMO.ConstructionDocs.model.db.entity.AsbuiltDoc;
import ITMO.ConstructionDocs.model.db.entity.Company;
import ITMO.ConstructionDocs.model.db.entity.ProjectDoc;
import ITMO.ConstructionDocs.model.db.entity.User;
import ITMO.ConstructionDocs.model.db.repository.AsbuiltDocRepository;
import ITMO.ConstructionDocs.model.dto.request.AsbuiltDocReq;
import ITMO.ConstructionDocs.model.dto.request.AsbuiltDocToCompanyReq;
import ITMO.ConstructionDocs.model.dto.response.AsbuiltDocResp;
import ITMO.ConstructionDocs.model.enums.AsbuiltCategory;
import ITMO.ConstructionDocs.model.enums.CommonStatus;
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

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsbuiltDocServiceTest {
    @InjectMocks
    private AsbuiltDocService asbuiltDocService;

    @Mock
    private AsbuiltDocRepository asbuiltDocRepository;

    @Mock
    private UserService userService;

    @Spy
    private ObjectMapper objectMapper;

    @Mock
    private ProjectDocService projectDocService;

    @Mock
    private CompanyService companyService;

    @Mock
    private MultipartFile file;

    @Mock
    private Authentication auth;

    @Test
    void getAsbuiltDocFromDB() {
    }

    @Test
    void createAsbuiltDoc() throws IOException {
        AsbuiltDocReq asbuiltDocReq = new AsbuiltDocReq();
        asbuiltDocReq.setFileName("test.txt");

        when(file.getContentType()).thenReturn("txt");
        when(file.getSize()).thenReturn(5000L);
        doNothing().when(file).transferTo(any(File.class));

        User user = new User();
        user.setId(1L);
        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);

        AsbuiltDoc savedDoc = new AsbuiltDoc();
        savedDoc.setId(UUID.randomUUID());
        when(asbuiltDocRepository.save(any(AsbuiltDoc.class))).thenReturn(savedDoc);

        AsbuiltDocResp asbuiltDocResp = asbuiltDocService.createAsbuiltDoc(asbuiltDocReq, file, auth);

        assertEquals(savedDoc.getId(), asbuiltDocResp.getId());
    }

    @Test
    void updateAsbuiltDoc() throws IOException {
        UUID asbuiltDocId = UUID.randomUUID();
        AsbuiltDocReq asbuiltDocReq = new AsbuiltDocReq();
        asbuiltDocReq.setFileName("test.txt");
        asbuiltDocReq.setDocStatus(DocStatus.APPROVED);
        asbuiltDocReq.setAsbuiltCategory(AsbuiltCategory.DRAWING);
        asbuiltDocReq.setDescription("test description");

        AsbuiltDoc existingDoc = new AsbuiltDoc();
        existingDoc.setId(asbuiltDocId);
        existingDoc.setFileName("test.txt");
        existingDoc.setDocStatus(DocStatus.INITIAL);
        existingDoc.setAsbuiltCategory(AsbuiltCategory.CERTIFICATE);
        existingDoc.setFileAddress("test/path/test.txt");
        existingDoc.setDescription("old test description");

        when(asbuiltDocRepository.findById(asbuiltDocId)).thenReturn(Optional.of(existingDoc));

        when(file.getContentType()).thenReturn("txt");
        when(file.getSize()).thenReturn(5000L);
        doNothing().when(file).transferTo(any(File.class));

        User user = new User();
        user.setId(1L);
        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);

        when(asbuiltDocRepository.save(any(AsbuiltDoc.class))).thenReturn(existingDoc);

        AsbuiltDocResp asbuiltDocResp = asbuiltDocService.updateAsbuiltDoc(asbuiltDocId, asbuiltDocReq, file, auth);

        assertEquals(existingDoc.getId(), asbuiltDocResp.getId());
        assertEquals(existingDoc.getFileName(), asbuiltDocResp.getFileName());
        assertEquals(existingDoc.getDocStatus(), asbuiltDocResp.getDocStatus());
        assertEquals(existingDoc.getAsbuiltCategory(), asbuiltDocResp.getAsbuiltCategory());
        assertEquals(existingDoc.getFileAddress(), asbuiltDocResp.getFileAddress());
        assertEquals(existingDoc.getDescription(), asbuiltDocResp.getDescription());
    }

    @Test
    void updateAsbuiltDoc_fileSizeIsTooBig() {
        UUID asbuiltDocId = UUID.randomUUID();
        AsbuiltDocReq asbuiltDocReq = new AsbuiltDocReq();
        asbuiltDocReq.setFileName("test.txt");

        AsbuiltDoc existingDoc = new AsbuiltDoc();
        existingDoc.setId(asbuiltDocId);
        existingDoc.setFileName("test.txt");

        when(asbuiltDocRepository.findById(asbuiltDocId)).thenReturn(Optional.of(existingDoc));

        when(file.getContentType()).thenReturn("txt");
        when(file.getSize()).thenReturn(50000000L);

        assertThrows(CustomException.class, () -> asbuiltDocService.updateAsbuiltDoc(asbuiltDocId, asbuiltDocReq, file, auth));

    }

    @Test
    void deleteAsbuiltDoc() {
        AsbuiltDoc existingDoc = new AsbuiltDoc();
        existingDoc.setId(UUID.randomUUID());
        existingDoc.setFileName("test.txt");
        existingDoc.setDocStatus(DocStatus.INITIAL);
        existingDoc.setAsbuiltCategory(AsbuiltCategory.CERTIFICATE);
        existingDoc.setFileAddress("test/path/test.txt");
        when(asbuiltDocRepository.findById(any(UUID.class))).thenReturn(Optional.of(existingDoc));

        User user = new User();
        user.setId(1L);
        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);

        asbuiltDocService.deleteAsbuiltDoc(existingDoc.getId(), auth);

        verify(asbuiltDocRepository, times(1)).save(any(AsbuiltDoc.class));
        Assert.assertEquals(DocStatus.WITHDRAWN, existingDoc.getDocStatus());
    }

    @Test
    void getAsbuiltDoc() {
        AsbuiltDoc asbuiltDoc = new AsbuiltDoc();
        asbuiltDoc.setId(UUID.randomUUID());

        when(asbuiltDocRepository.findById(asbuiltDoc.getId())).thenReturn(Optional.of(asbuiltDoc));

        AsbuiltDocResp result = asbuiltDocService.getAsbuiltDoc(asbuiltDoc.getId());

        Assert.assertEquals(asbuiltDoc.getId(), result.getId());
    }

    @Test
    void getAllAsbuiltDocs() {
        Integer page = 0;
        Integer sizePerPage = 10;
        String sort = "asbuiltCategory";
        String filter = "draw";
        Sort.Direction order = Sort.Direction.ASC;

        AsbuiltDoc asbuiltDoc1 = new AsbuiltDoc();
        asbuiltDoc1.setId(UUID.randomUUID());
        asbuiltDoc1.setFileName("test1.txt");
        asbuiltDoc1.setDocStatus(DocStatus.INITIAL);
        asbuiltDoc1.setAsbuiltCategory(AsbuiltCategory.CERTIFICATE);

        AsbuiltDoc asbuiltDoc2 = new AsbuiltDoc();
        asbuiltDoc2.setId(UUID.randomUUID());
        asbuiltDoc2.setFileName("test2.txt");
        asbuiltDoc2.setDocStatus(DocStatus.APPROVED);
        asbuiltDoc2.setAsbuiltCategory(AsbuiltCategory.DRAWING);

        AsbuiltDoc asbuiltDoc3 = new AsbuiltDoc();
        asbuiltDoc3.setId(UUID.randomUUID());
        asbuiltDoc3.setFileName("test3.txt");
        asbuiltDoc3.setDocStatus(DocStatus.CHECKED);
        asbuiltDoc3.setAsbuiltCategory(AsbuiltCategory.DRAWING);

        List<AsbuiltDoc> filteredDocs = new ArrayList<>();

        when(asbuiltDocRepository.findAllNotDeletedAndFiltered(any(Pageable.class), any(DocStatus.class), any(String.class)))
                .thenAnswer(invocation -> {
                    List<AsbuiltDoc> allDocs = List.of(asbuiltDoc1, asbuiltDoc2, asbuiltDoc3);

                    filteredDocs.addAll(allDocs.stream()
                            .filter(doc -> doc.getFileName().toLowerCase().contains(filter) ||
                                    doc.getAsbuiltCategory().toString().toLowerCase().contains(filter))
                            .collect(Collectors.toList()));

                    return new PageImpl<>(filteredDocs, invocation.getArgument(0), filteredDocs.size());
                });

        Page result = asbuiltDocService.getAllAsbuiltDocs(page, sizePerPage, sort, order, filter);
        assertEquals(filteredDocs.size(), result.getTotalElements());
        assertEquals(filteredDocs.get(0).getId(), asbuiltDoc2.getId());
        assertEquals(filteredDocs.get(1).getId(), asbuiltDoc3.getId());
    }

    /*@Test*/
    /*void addAsbuiltDocToProjectDoc() {
        AsbuiltDocToProjDocReq asbuiltDocToProjDocReq = new AsbuiltDocToProjDocReq();

        AsbuiltDoc asbuiltDoc = new AsbuiltDoc();
        asbuiltDoc.setId(UUID.randomUUID());
        asbuiltDoc.setFileName("test.txt");
        asbuiltDoc.setFileAddress("test/abd/test.txt");

        ProjectDoc projectDoc = new ProjectDoc();
        projectDoc.setId(UUID.randomUUID());
        projectDoc.setDocStatus(DocStatus.INITIAL);
        projectDoc.setAsbuiltDocs(new ArrayList<>());
        projectDoc.setFileAddress("test/abd/test.txt");

        asbuiltDocToProjDocReq.setAsbuiltDocId(asbuiltDoc.getId());
        asbuiltDocToProjDocReq.setProjectDocId(projectDoc.getId());

        when(asbuiltDocRepository.findById(asbuiltDoc.getId())).thenReturn(Optional.of(asbuiltDoc));
        when(projectDocService.getProjectDocFromDB(projectDoc.getId())).thenReturn(projectDoc);

        asbuiltDocService.addAsbuiltDocToProjectDoc(asbuiltDocToProjDocReq);

        verify(projectDocService, times(1)).updateProjectDocData(projectDoc);
        verify(asbuiltDocRepository, times(2)).save(asbuiltDoc);

        assertTrue(projectDoc.getAsbuiltDocs().stream().anyMatch(doc -> Objects.equals(doc.getId(), asbuiltDoc.getId())));
        assertEquals(asbuiltDoc.getProjectDoc(), projectDoc);
    }*/

    @Test
    void addAsbuiltDocToCompany() {
        Company company = new Company();
        company.setId(1L);
        company.setAsbuiltDocs(new ArrayList<>());

        AsbuiltDoc asbuiltDoc = new AsbuiltDoc();
        asbuiltDoc.setId(UUID.randomUUID());
        asbuiltDoc.setFileName("test.txt");
        asbuiltDoc.setFileAddress("test/abd/test.txt");

        when(asbuiltDocRepository.findById(asbuiltDoc.getId())).thenReturn(Optional.of(asbuiltDoc));
        when(companyService.getCompanyFromDB(company.getId())).thenReturn(company);

        AsbuiltDocToCompanyReq asbuiltDocToCompanyReq = new AsbuiltDocToCompanyReq();
        asbuiltDocToCompanyReq.setAsbuiltDocId(asbuiltDoc.getId());
        asbuiltDocToCompanyReq.setCompanyId(company.getId());

        asbuiltDocService.addAsbuiltDocToCompany(asbuiltDocToCompanyReq);
        verify(companyService, times(1)).updateCompanyData(company);
        verify(asbuiltDocRepository, times(1)).save(asbuiltDoc);

        assertEquals(company.getAsbuiltDocs().size(), 1);
        assertTrue(company.getAsbuiltDocs().contains(asbuiltDoc));
        assertEquals(asbuiltDoc.getCompany(), company);
    }

    @Test
    void getAsbuiltDocsByCompanyId() {
        Integer page = 0;
        Integer sizePerPage = 10;
        String sort = "title";
        Sort.Direction order = Sort.Direction.ASC;

        AsbuiltDoc asbuiltDoc1 = new AsbuiltDoc();
        asbuiltDoc1.setId(UUID.randomUUID());
        asbuiltDoc1.setFileName("test1.txt");

        AsbuiltDoc asbuiltDoc2 = new AsbuiltDoc();
        asbuiltDoc2.setId(UUID.randomUUID());
        asbuiltDoc2.setFileName("test2.txt");

        AsbuiltDoc asbuiltDoc3 = new AsbuiltDoc();
        asbuiltDoc3.setId(UUID.randomUUID());
        asbuiltDoc3.setFileName("test3.txt");

        Company company = new Company();
        company.setId(1L);
        company.setAsbuiltDocs(List.of(asbuiltDoc1, asbuiltDoc2));
        company.setStatus(CommonStatus.CREATED);

        Company company2 = new Company();
        company2.setId(2L);
        company2.setAsbuiltDocs(List.of(asbuiltDoc3));
        company2.setStatus(CommonStatus.CREATED);

        asbuiltDoc1.setCompany(company);
        asbuiltDoc2.setCompany(company);
        asbuiltDoc3.setCompany(company2);

        when(companyService.getCompanyFromDB(company.getId())).thenReturn(company);
        List<AsbuiltDoc> allDocs = List.of(asbuiltDoc1, asbuiltDoc2, asbuiltDoc3);
        List<AsbuiltDoc> filteredAsbuiltDocs = new ArrayList<>();

        when(asbuiltDocRepository.findAllByCompanyId(anyLong(), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    filteredAsbuiltDocs.addAll(allDocs.stream()
                            .filter(doc -> Objects.equals(doc.getCompany().getId(), company.getId()))
                            .collect(Collectors.toList()));

                    return new PageImpl<>(filteredAsbuiltDocs, invocation.getArgument(1), filteredAsbuiltDocs.size());
                });

        Page<AsbuiltDocResp> result = asbuiltDocService.getAsbuiltDocsByCompanyId(company.getId(), page, sizePerPage, sort, order);

        assertEquals(company.getAsbuiltDocs().size(), result.getTotalElements());
        assertEquals(asbuiltDoc1.getCompany().getId(), filteredAsbuiltDocs.get(0).getCompany().getId());
    }

    @Test
    void getAsbuiltDocsByProjectDocId() {
        Integer page = 0;
        Integer sizePerPage = 10;
        String sort = "title";
        Sort.Direction order = Sort.Direction.ASC;

        AsbuiltDoc asbuiltDoc1 = new AsbuiltDoc();
        asbuiltDoc1.setId(UUID.randomUUID());
        asbuiltDoc1.setFileName("test1.txt");

        AsbuiltDoc asbuiltDoc2 = new AsbuiltDoc();
        asbuiltDoc2.setId(UUID.randomUUID());
        asbuiltDoc2.setFileName("test2.txt");

        AsbuiltDoc asbuiltDoc3 = new AsbuiltDoc();
        asbuiltDoc3.setId(UUID.randomUUID());
        asbuiltDoc3.setFileName("test3.txt");

        ProjectDoc projectDoc1 = new ProjectDoc();
        projectDoc1.setId(UUID.randomUUID());
        projectDoc1.setDocStatus(DocStatus.INITIAL);
        projectDoc1.setAsbuiltDocs(List.of(asbuiltDoc1, asbuiltDoc3));

        ProjectDoc projectDoc2 = new ProjectDoc();
        projectDoc2.setId(UUID.randomUUID());

        asbuiltDoc1.setProjectDoc(projectDoc1);
        asbuiltDoc2.setProjectDoc(projectDoc2);
        asbuiltDoc3.setProjectDoc(projectDoc1);

        when(projectDocService.getProjectDocFromDB(any(UUID.class))).thenReturn(projectDoc1);
        List<AsbuiltDoc> allDocs = List.of(asbuiltDoc1, asbuiltDoc2, asbuiltDoc3);
        List<AsbuiltDoc> filteredAsbuiltDocs = new ArrayList<>();

        when(asbuiltDocRepository.findAllByProjectDocId(any(UUID.class), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    filteredAsbuiltDocs.addAll(allDocs.stream()
                            .filter(doc -> Objects.equals(doc.getProjectDoc().getId(), projectDoc1.getId()))
                            .collect(Collectors.toList()));

                    return new PageImpl<>(filteredAsbuiltDocs, invocation.getArgument(1), filteredAsbuiltDocs.size());
                });

        Page<AsbuiltDocResp> result = asbuiltDocService.getAsbuiltDocsByProjectDocId(projectDoc1.getId(), page, sizePerPage, sort, order);

        assertEquals(projectDoc1.getAsbuiltDocs().size(), result.getTotalElements());
        assertEquals(asbuiltDoc1.getProjectDoc().getId(), filteredAsbuiltDocs.get(0).getProjectDoc().getId());
    }

    @Test
    void getAsbuiltDocsForLastWeek() {
        Integer page = 0;
        Integer sizePerPage = 10;
        String sort = "title";
        Sort.Direction order = Sort.Direction.ASC;

        AsbuiltDoc asbuiltDoc1 = new AsbuiltDoc();
        asbuiltDoc1.setId(UUID.randomUUID());
        asbuiltDoc1.setFileName("test1.txt");
        asbuiltDoc1.setCreatedAt(LocalDateTime.now().minusDays(10));

        AsbuiltDoc asbuiltDoc2 = new AsbuiltDoc();
        asbuiltDoc2.setId(UUID.randomUUID());
        asbuiltDoc2.setFileName("test2.txt");
        asbuiltDoc2.setCreatedAt(LocalDateTime.now().minusDays(5));

        AsbuiltDoc asbuiltDoc3 = new AsbuiltDoc();
        asbuiltDoc3.setId(UUID.randomUUID());
        asbuiltDoc3.setFileName("test3.txt");
        asbuiltDoc3.setCreatedAt(LocalDateTime.now().minusDays(1));

        List<AsbuiltDoc> filteredAsbuiltDocs = new ArrayList<>();

        when(asbuiltDocRepository.findAllForLastWeek(any(LocalDateTime.class), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    List<AsbuiltDoc> allAsbuiltDocs = List.of(asbuiltDoc1, asbuiltDoc2, asbuiltDoc3);

                    filteredAsbuiltDocs.addAll(allAsbuiltDocs.stream()
                            .filter(doc -> doc.getCreatedAt().isAfter(LocalDateTime.now().minusWeeks(1)))
                            .collect(Collectors.toList()));

                    return new PageImpl<>(filteredAsbuiltDocs, invocation.getArgument(1), filteredAsbuiltDocs.size());
                });

        Page<AsbuiltDocResp> result = asbuiltDocService.getAsbuiltDocsForLastWeek(page, sizePerPage, sort, order);
        assertEquals(2, result.getTotalElements());
        assertTrue(result.stream().anyMatch(doc -> doc.getId().equals(asbuiltDoc2.getId())));
        assertTrue(result.stream().anyMatch(doc -> doc.getId().equals(asbuiltDoc3.getId())));
    }

    @Test
    void getAsbuiltDocsByProjectDocIdForLastWeek() {
        Integer page = 0;
        Integer sizePerPage = 10;
        String sort = "title";
        Sort.Direction order = Sort.Direction.ASC;

        AsbuiltDoc asbuiltDoc1 = new AsbuiltDoc();
        asbuiltDoc1.setId(UUID.randomUUID());
        asbuiltDoc1.setFileName("test1.txt");
        asbuiltDoc1.setCreatedAt(LocalDateTime.now().minusDays(10));

        AsbuiltDoc asbuiltDoc2 = new AsbuiltDoc();
        asbuiltDoc2.setId(UUID.randomUUID());
        asbuiltDoc2.setFileName("test2.txt");
        asbuiltDoc2.setCreatedAt(LocalDateTime.now().minusDays(5));

        AsbuiltDoc asbuiltDoc3 = new AsbuiltDoc();
        asbuiltDoc3.setId(UUID.randomUUID());
        asbuiltDoc3.setFileName("test3.txt");
        asbuiltDoc3.setCreatedAt(LocalDateTime.now().minusDays(1));

        ProjectDoc projectDoc1 = new ProjectDoc();
        projectDoc1.setId(UUID.randomUUID());
        projectDoc1.setDocStatus(DocStatus.INITIAL);
        projectDoc1.setAsbuiltDocs(List.of(asbuiltDoc1, asbuiltDoc3));

        ProjectDoc projectDoc2 = new ProjectDoc();
        projectDoc2.setId(UUID.randomUUID());

        asbuiltDoc1.setProjectDoc(projectDoc1);
        asbuiltDoc2.setProjectDoc(projectDoc2);
        asbuiltDoc3.setProjectDoc(projectDoc1);

        when(projectDocService.getProjectDocFromDB(any(UUID.class))).thenReturn(projectDoc1);
        List<AsbuiltDoc> allDocs = List.of(asbuiltDoc1, asbuiltDoc2, asbuiltDoc3);
        List<AsbuiltDoc> filteredAsbuiltDocs = new ArrayList<>();

        when(asbuiltDocRepository.findAllByProjectDocId(any(UUID.class), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    filteredAsbuiltDocs.addAll(allDocs.stream()
                            .filter(doc -> Objects.equals(doc.getProjectDoc().getId(), projectDoc1.getId()))
                            .filter(doc -> doc.getCreatedAt().isAfter(LocalDateTime.now().minusWeeks(1)))
                            .collect(Collectors.toList()));

                    return new PageImpl<>(filteredAsbuiltDocs, invocation.getArgument(1), filteredAsbuiltDocs.size());
                });

        Page<AsbuiltDocResp> result = asbuiltDocService.getAsbuiltDocsByProjectDocId(projectDoc1.getId(), page, sizePerPage, sort, order);

        assertEquals(1, result.getTotalElements());
        assertEquals(asbuiltDoc3.getProjectDoc().getId(), filteredAsbuiltDocs.get(0).getProjectDoc().getId());
    }
}