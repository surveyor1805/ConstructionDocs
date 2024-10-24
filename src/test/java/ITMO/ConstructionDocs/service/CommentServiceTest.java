package ITMO.ConstructionDocs.service;

import ITMO.ConstructionDocs.model.db.entity.AsbuiltDoc;
import ITMO.ConstructionDocs.model.db.entity.Comment;
import ITMO.ConstructionDocs.model.db.entity.ProjectDoc;
import ITMO.ConstructionDocs.model.db.entity.User;
import ITMO.ConstructionDocs.model.db.repository.CommentRepository;
import ITMO.ConstructionDocs.model.dto.request.CommentReq;
import ITMO.ConstructionDocs.model.dto.request.CommentToProjDocReq;
import ITMO.ConstructionDocs.model.dto.response.CommentResp;
import ITMO.ConstructionDocs.model.enums.CommonStatus;
import ITMO.ConstructionDocs.model.enums.DocStatus;
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
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Spy
    private ObjectMapper objectMapper;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserService userService;

    @Mock
    private ProjectDocService projectDocService;

    @Mock
    private AsbuiltDocService asbuiltDocService;

    @Mock
    private Authentication auth;


    @Test
    void createComment() {
        CommentReq commentReq = new CommentReq();
        commentReq.setTitle("test");
        commentReq.setText("test");

        Comment comment = new Comment();
        comment.setId(1L);

        User user = new User();
        user.setId(1L);

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentResp commentResp = commentService.createComment(commentReq, auth);

        assertEquals(comment.getId(), commentResp.getId());
        assertEquals(comment.getTitle(), commentResp.getTitle());
        assertEquals(comment.getText(), commentResp.getText());
    }

    @Test
    void getComment() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setTitle("test");
        comment.setText("test");
        when(commentRepository.findById(comment.getId())).thenReturn(java.util.Optional.of(comment));

        CommentResp commentResp = commentService.getComment(comment.getId());

        assertEquals(comment.getId(), commentResp.getId());
    }

    @Test
    void getCommentFromDB() {
    }

    @Test
    void updateComment() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setTitle("test");
        comment.setText("test");

        User user = new User();
        user.setId(1L);
        comment.setCreatedBy(user);

        CommentReq commentReq = new CommentReq();
        commentReq.setTitle("newTitle");
        commentReq.setText("newText");

        when(commentRepository.findById(comment.getId())).thenReturn(java.util.Optional.of(comment));
        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentResp commentResp = commentService.updateComment(1L, commentReq, auth);

        assertEquals(comment.getId(), commentResp.getId());
        assertEquals(comment.getTitle(), commentResp.getTitle());
        assertEquals(comment.getText(), commentResp.getText());
    }

    @Test
    void deleteComment() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setTitle("test");
        comment.setText("test");

        User user = new User();
        user.setId(1L);
        comment.setCreatedBy(user);

        when(commentRepository.findById(comment.getId())).thenReturn(java.util.Optional.of(comment));
        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        commentService.deleteComment(comment.getId(), auth);

        verify(commentRepository, times(1)).save(any(Comment.class));
        assertEquals(comment.getStatus(), CommonStatus.DELETED);
    }

    @Test
    void getAllComments() {
        Integer page = 0;
        Integer sizePerPage = 10;
        String sort = "title";
        String filter = "test";
        Sort.Direction order = Sort.Direction.ASC;

        Comment comment1 = new Comment();
        comment1.setId(1L);
        comment1.setTitle("test1");
        comment1.setText("test1");

        Comment comment2 = new Comment();
        comment2.setId(2L);
        comment2.setTitle("test2");
        comment2.setText("test2");

        Comment comment3 = new Comment();
        comment3.setId(3L);
        comment3.setTitle("rest3");
        comment3.setText("rest3");

        List<Comment> filteredComments = new ArrayList<>();

        when(commentRepository.findAllNotDeletedAndFiltered(any(Pageable.class), any(CommonStatus.class), any(String.class)))
                .thenAnswer(invocation -> {
                    List<Comment> allComments = List.of(comment1, comment2, comment3);

                    filteredComments.addAll(allComments.stream()
                            .filter(doc -> doc.getTitle().toLowerCase().contains(filter) ||
                                    doc.getText().toLowerCase().contains(filter))
                            .collect(Collectors.toList()));

                    return new PageImpl<>(filteredComments, invocation.getArgument(0), filteredComments.size());
                });

        Page result = commentService.getAllComments(page, sizePerPage, sort, order, filter);
        assertEquals(filteredComments.size(), result.getTotalElements());
        assertEquals(filteredComments.get(0).getId(), comment1.getId());
        assertEquals(filteredComments.get(1).getId(), comment2.getId());
    }

    @Test
    void addCommentToProjectDoc() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setTitle("test");
        comment.setText("test");

        ProjectDoc projectDoc = new ProjectDoc();
        projectDoc.setId(UUID.randomUUID());
        projectDoc.setDocStatus(DocStatus.INITIAL);
        projectDoc.setComments(new ArrayList<>());

        CommentToProjDocReq commentToProjDocReq = new CommentToProjDocReq();
        commentToProjDocReq.setCommentId(comment.getId());
        commentToProjDocReq.setProjectDocId(projectDoc.getId());

        when(commentRepository.findById(comment.getId())).thenReturn(java.util.Optional.of(comment));
        when(projectDocService.getProjectDocFromDB(projectDoc.getId())).thenReturn(projectDoc);

        commentService.addCommentToProjectDoc(commentToProjDocReq);
        verify(projectDocService, times(1)).updateProjectDocData(projectDoc);
        verify(commentRepository, times(1)).save(comment);
        assertEquals(comment.getProjectDoc().getId(), projectDoc.getId());
        assertEquals(projectDoc.getComments().size(), 1);
    }

    @Test
    void addCommentToAsbuiltDoc() {
    }

    @Test
    void getCommentsByProjectDocId() {
        Integer page = 0;
        Integer sizePerPage = 10;
        String sort = "title";
        Sort.Direction order = Sort.Direction.ASC;

        ProjectDoc projectDoc = new ProjectDoc();
        projectDoc.setId(UUID.randomUUID());
        ProjectDoc projectDoc1 = new ProjectDoc();
        projectDoc1.setId(UUID.randomUUID());

        Comment comment1 = new Comment();
        comment1.setId(1L);
        comment1.setTitle("test1");
        comment1.setText("test1");
        comment1.setProjectDoc(projectDoc);

        Comment comment2 = new Comment();
        comment2.setId(2L);
        comment2.setTitle("test2");
        comment2.setText("test2");
        comment2.setProjectDoc(projectDoc1);

        Comment comment3 = new Comment();
        comment3.setId(3L);
        comment3.setTitle("rest3");
        comment3.setText("rest3");
        comment3.setProjectDoc(projectDoc);

        List<Comment> filteredComments = new ArrayList<>();

        when(commentRepository.findAllByProjectDocId(any(UUID.class), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    List<Comment> allComments = List.of(comment1, comment2, comment3);

                    filteredComments.addAll(allComments.stream()
                            .filter(doc -> doc.getProjectDoc().getId() == projectDoc.getId())
                            .collect(Collectors.toList()));

                    return new PageImpl<>(filteredComments, invocation.getArgument(1), filteredComments.size());
                });

        Page result = commentService.getCommentsByProjectDocId(projectDoc.getId(), page, sizePerPage, sort, order);
        assertEquals(filteredComments.size(), result.getTotalElements());
        assertEquals(filteredComments.get(0).getId(), comment1.getId());
        assertEquals(filteredComments.get(1).getId(), comment3.getId());
    }

    @Test
    void getCommentsByAsbuiltDocId() {
        Integer page = 0;
        Integer sizePerPage = 10;
        String sort = "title";
        Sort.Direction order = Sort.Direction.ASC;

        AsbuiltDoc asbuiltDoc = new AsbuiltDoc();
        asbuiltDoc.setId(UUID.randomUUID());
        AsbuiltDoc asbuiltDoc1 = new AsbuiltDoc();
        asbuiltDoc1.setId(UUID.randomUUID());

        Comment comment1 = new Comment();
        comment1.setId(1L);
        comment1.setTitle("test1");
        comment1.setText("test1");
        comment1.setAsbuiltDoc(asbuiltDoc1);

        Comment comment2 = new Comment();
        comment2.setId(2L);
        comment2.setTitle("test2");
        comment2.setText("test2");
        comment2.setAsbuiltDoc(asbuiltDoc1);

        Comment comment3 = new Comment();
        comment3.setId(3L);
        comment3.setTitle("rest3");
        comment3.setText("rest3");
        comment3.setAsbuiltDoc(asbuiltDoc);

        List<Comment> filteredComments = new ArrayList<>();

        when(commentRepository.findAllByAsbuiltDocId(any(UUID.class), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    List<Comment> allComments = List.of(comment1, comment2, comment3);

                    filteredComments.addAll(allComments.stream()
                            .filter(doc -> doc.getAsbuiltDoc().getId() == asbuiltDoc.getId())
                            .collect(Collectors.toList()));

                    return new PageImpl<>(filteredComments, invocation.getArgument(1), filteredComments.size());
                });

        Page result = commentService.getCommentsByAsbuiltDocId(asbuiltDoc.getId(), page, sizePerPage, sort, order);
        assertEquals(filteredComments.size(), result.getTotalElements());
        assertEquals(filteredComments.get(0).getId(), comment3.getId());
    }

    @Test
    void getCommentsForLastWeek() {
        Integer page = 0;
        Integer sizePerPage = 10;
        String sort = "title";
        Sort.Direction order = Sort.Direction.ASC;

        Comment comment1 = new Comment();
        comment1.setId(1L);
        comment1.setCreatedAt(LocalDateTime.now().minusDays(10));

        Comment comment2 = new Comment();
        comment2.setId(2L);
        comment2.setCreatedAt(LocalDateTime.now().minusDays(20));

        Comment comment3 = new Comment();
        comment3.setId(3L);
        comment3.setCreatedAt(LocalDateTime.now().minusDays(5));

        List<Comment> filteredComments = new ArrayList<>();

        when(commentRepository.findAllForLastWeek(any(LocalDateTime.class), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    List<Comment> allComments = List.of(comment1, comment2, comment3);

                    filteredComments.addAll(allComments.stream()
                            .filter(doc -> doc.getCreatedAt().isAfter(LocalDateTime.now().minusWeeks(1)))
                            .collect(Collectors.toList()));

                    return new PageImpl<>(filteredComments, invocation.getArgument(1), filteredComments.size());
                });

        Page<CommentResp> result = commentService.getCommentsForLastWeek(page, sizePerPage, sort, order);
        assertEquals(1, result.getTotalElements());
        assertEquals(comment3.getId(), result.getContent().get(0).getId());

    }

    @Test
    void getCommentsForLastWeekByUser() {
    }
}