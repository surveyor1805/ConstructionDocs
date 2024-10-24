package ITMO.ConstructionDocs.service;

import ITMO.ConstructionDocs.exceptions.CustomException;
import ITMO.ConstructionDocs.model.db.entity.AsbuiltDoc;
import ITMO.ConstructionDocs.model.db.entity.Comment;
import ITMO.ConstructionDocs.model.db.entity.ProjectDoc;
import ITMO.ConstructionDocs.model.db.entity.User;
import ITMO.ConstructionDocs.model.db.repository.CommentRepository;
import ITMO.ConstructionDocs.model.dto.request.CommentReq;
import ITMO.ConstructionDocs.model.dto.request.CommentToAsbuiltReq;
import ITMO.ConstructionDocs.model.dto.request.CommentToProjDocReq;
import ITMO.ConstructionDocs.model.dto.response.CommentResp;
import ITMO.ConstructionDocs.model.enums.CommonStatus;
import ITMO.ConstructionDocs.utils.PaginationUtil;
import ITMO.ConstructionDocs.utils.RightsValidatorUtil;
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

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
    private final ObjectMapper objectMapper;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final ProjectDocService projectDocService;
    private final AsbuiltDocService asbuiltDocService;

    public CommentResp createComment(CommentReq request, Authentication auth) {

        Comment comment = objectMapper.convertValue(request, Comment.class);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setCreatedBy(userService.getCurrentUser(auth));
        comment.setStatus(CommonStatus.CREATED);

        Comment save = commentRepository.save(comment);

        return objectMapper.convertValue(save, CommentResp.class);
    }

    public CommentResp getComment(Long id) {
        return objectMapper.convertValue(getCommentFromDB(id), CommentResp.class);
    }

    public Comment getCommentFromDB(Long id) {
        return commentRepository.findById(id).orElseThrow(() -> new CustomException("Comment not found", HttpStatus.NOT_FOUND));
    }

    public CommentResp updateComment(Long id, CommentReq request, Authentication auth) {
        Comment comment = getCommentFromDB(id);

        User createdBy = comment.getCreatedBy();
        User currentUser = userService.getCurrentUser(auth);
        RightsValidatorUtil.validateUser(createdBy, currentUser);
        if (request.getTitle() != null) {
            comment.setTitle(request.getTitle());
        }
        if (request.getText() != null) {
            comment.setText(request.getText());
        }

        comment.setUpdatedAt(LocalDateTime.now());
        comment.setUpdatedBy(currentUser);
        comment.setStatus(CommonStatus.UPDATED);

        Comment save = commentRepository.save(comment);

        return objectMapper.convertValue(save, CommentResp.class);
    }

    public void deleteComment(Long id, Authentication auth) {
        Comment comment = getCommentFromDB(id);
        User createdBy = comment.getCreatedBy();
        User currentUser = userService.getCurrentUser(auth);
        RightsValidatorUtil.validateUser(createdBy, currentUser);
        comment.setUpdatedAt(LocalDateTime.now());
        comment.setStatus(CommonStatus.DELETED);
        commentRepository.save(comment);
    }

    public Page<CommentResp> getAllComments(Integer page, Integer sizePerPage, String sort, Sort.Direction order, String filter) {
        Pageable pageRequest = PaginationUtil.getPageRequest(page, sizePerPage, sort, order);

        Page<Comment> all;
        if (filter == null) {
            all = commentRepository.findAllNotDeleted(pageRequest, CommonStatus.DELETED);
        } else {
            all = commentRepository.findAllNotDeletedAndFiltered(pageRequest, CommonStatus.DELETED, filter.toLowerCase());
        }

        List<CommentResp> content = all.getContent().stream()
                .map(comment -> objectMapper.convertValue(comment, CommentResp.class))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageRequest, all.getTotalElements());
    }

    public void addCommentToProjectDoc(@Valid CommentToProjDocReq request) {
        Comment comment = getCommentFromDB(request.getCommentId());
        ProjectDoc projectDoc = projectDocService.getProjectDocFromDB(request.getProjectDocId());

        projectDoc.getComments().add(comment);
        projectDocService.updateProjectDocData(projectDoc);

        comment.setProjectDoc(projectDoc);
        commentRepository.save(comment);
    }

    public void addCommentToAsbuiltDoc(@Valid CommentToAsbuiltReq request) {
        Comment comment = getCommentFromDB(request.getCommentId());
        AsbuiltDoc asbuiltDoc = asbuiltDocService.getAsbuiltDocFromDB(request.getAsbuiltDocId());

        asbuiltDoc.getComments().add(comment);
        asbuiltDocService.updateAsbuiltDocData(asbuiltDoc);

        comment.setAsbuiltDoc(asbuiltDoc);
        commentRepository.save(comment);
    }

    public Page<CommentResp> getCommentsByProjectDocId(UUID projectDocId, Integer page, Integer sizePerPage, String sort, Sort.Direction order) {
        projectDocService.getProjectDocFromDB(projectDocId);

        Pageable pageRequest = PaginationUtil.getPageRequest(page, sizePerPage, sort, order);

        Page<Comment> all = commentRepository.findAllByProjectDocId(projectDocId, pageRequest);

        List<CommentResp> content = all.getContent().stream()
                .map(comment -> objectMapper.convertValue(comment, CommentResp.class))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageRequest, all.getTotalElements());
    }

    public Page<CommentResp> getCommentsByAsbuiltDocId(UUID asbuiltDocId, Integer page, Integer sizePerPage, String sort, Sort.Direction order) {
        asbuiltDocService.getAsbuiltDocFromDB(asbuiltDocId);

        Pageable pageRequest = PaginationUtil.getPageRequest(page, sizePerPage, sort, order);

        Page<Comment> all = commentRepository.findAllByAsbuiltDocId(asbuiltDocId, pageRequest);

        List<CommentResp> content = all.getContent().stream()
                .map(comment -> objectMapper.convertValue(comment, CommentResp.class))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageRequest, all.getTotalElements());
    }

    public Page<CommentResp> getCommentsForLastWeek(Integer page, Integer sizePerPage, String sort, Sort.Direction order) {
        Pageable pageRequest = PaginationUtil.getPageRequest(page, sizePerPage, sort, order);

        LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);

        Page<Comment> all = commentRepository.findAllForLastWeek(lastWeek, pageRequest);

        List<CommentResp> content = all.getContent().stream()
                .map(comment -> objectMapper.convertValue(comment, CommentResp.class))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageRequest, all.getTotalElements());
    }

    public Page<CommentResp> getCommentsForLastWeekByUser(Long userId, Integer page, Integer sizePerPage, String sort, Sort.Direction order) {
        userService.getUserFromDB(userId);

        Pageable pageRequest = PaginationUtil.getPageRequest(page, sizePerPage, sort, order);

        LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);

        Page<Comment> all = commentRepository.findAllForLastWeekByUser(userId, lastWeek, pageRequest);

        List<CommentResp> content = all.getContent().stream()
                .map(comment -> objectMapper.convertValue(comment, CommentResp.class))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageRequest, all.getTotalElements());
    }
}
