package ITMO.ConstructionDocs.controllers;

import ITMO.ConstructionDocs.model.dto.request.CommentReq;
import ITMO.ConstructionDocs.model.dto.request.CommentToAsbuiltReq;
import ITMO.ConstructionDocs.model.dto.request.CommentToProjDocReq;
import ITMO.ConstructionDocs.model.dto.response.CommentResp;
import ITMO.ConstructionDocs.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

import static ITMO.ConstructionDocs.constants.Constants.COMMENT;

@Tag(name = "Comments")
@RestController
@RequestMapping(COMMENT)
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping()
    @Operation(summary = "Create comment")
    @PreAuthorize("isAuthenticated()")
    public CommentResp createComment(@RequestBody CommentReq request, Authentication authentication) {
        return commentService.createComment(request, authentication);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get comment by id")
    @PreAuthorize("isAuthenticated()")
    public CommentResp getComment(@PathVariable Long id) {
        return commentService.getComment(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update comment by id")
    @PreAuthorize("isAuthenticated()")
    public CommentResp updateComment(@PathVariable Long id, @RequestBody CommentReq request, Authentication authentication) {
        return commentService.updateComment(id, request, authentication);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete comment by id")
    @PreAuthorize("isAuthenticated()")
    public void deleteComment(@PathVariable Long id, Authentication authentication) {
        commentService.deleteComment(id, authentication);
    }

    @GetMapping("/all")
    @Operation(summary = "Get list of comments")
    @PreAuthorize("isAuthenticated()")
    public Page<CommentResp> getAllComments(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer sizePerPage,
                                            @RequestParam(defaultValue = "title") String sort,
                                            @RequestParam(defaultValue = "ASC") Sort.Direction order,
                                            @RequestParam(required = false) String filter) {
        return commentService.getAllComments(page, sizePerPage, sort, order, filter);
    }

    @PostMapping("/setCommentToProjectDoc")
    @Operation(summary = "set Comment to ProjectDoc")
    @PreAuthorize("isAuthenticated()")
    public void addCommentToProjectDoc(@RequestBody @Valid CommentToProjDocReq request) {
        commentService.addCommentToProjectDoc(request);
    }

    @PostMapping("/setCommentToAsbuiltDoc")
    @Operation(summary = "set Comment to AsbuiltDoc")
    @PreAuthorize("isAuthenticated()")
    public void addCommentToAsbuiltDoc(@RequestBody @Valid CommentToAsbuiltReq request) {
        commentService.addCommentToAsbuiltDoc(request);
    }

    @GetMapping("/allByProjectDocId")
    @Operation(summary = "Get list of comments by ProjectDoc id")
    @PreAuthorize("isAuthenticated()")
    public Page<CommentResp> getCommentsByProjectDocId(@RequestParam UUID projectDocId,
                                                       @RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer sizePerPage,
                                                       @RequestParam(defaultValue = "title") String sort,
                                                       @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return commentService.getCommentsByProjectDocId(projectDocId, page, sizePerPage, sort, order);
    }

    @GetMapping("/allByAsbuiltDocId")
    @Operation(summary = "Get list of comments by AsbuiltDoc id")
    @PreAuthorize("isAuthenticated()")
    public Page<CommentResp> getCommentsByAsbuiltDocId(@RequestParam UUID asbuiltDocId,
                                                       @RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer sizePerPage,
                                                       @RequestParam(defaultValue = "title") String sort,
                                                       @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return commentService.getCommentsByAsbuiltDocId(asbuiltDocId, page, sizePerPage, sort, order);
    }

    @GetMapping("/allForLastWeek")
    @Operation(summary = "Get list of comments for last week")
    @PreAuthorize("isAuthenticated()")
    public Page<CommentResp> getCommentsForLastWeek(@RequestParam(defaultValue = "1") Integer page,
                                                    @RequestParam(defaultValue = "10") Integer sizePerPage,
                                                    @RequestParam(defaultValue = "title") String sort,
                                                    @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return commentService.getCommentsForLastWeek(page, sizePerPage, sort, order);
    }

    @GetMapping("/allForLastWeekByUser")
    @Operation(summary = "Get list of comments for last week by user id")
    @PreAuthorize("isAuthenticated()")
    public Page<CommentResp> getCommentsForLastWeekByUser(@RequestParam Long userId,
                                                          @RequestParam(defaultValue = "1") Integer page,
                                                          @RequestParam(defaultValue = "10") Integer sizePerPage,
                                                          @RequestParam(defaultValue = "title") String sort,
                                                          @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return commentService.getCommentsForLastWeekByUser(userId, page, sizePerPage, sort, order);
    }
}
