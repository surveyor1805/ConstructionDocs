package ITMO.ConstructionDocs.model.db.repository;

import ITMO.ConstructionDocs.model.db.entity.AsbuiltDoc;
import ITMO.ConstructionDocs.model.db.entity.Comment;
import ITMO.ConstructionDocs.model.enums.CommonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("select c from Comment c where c.status <>:status")
    Page<Comment> findAllNotDeleted(Pageable pageRequest, @Param("status") CommonStatus commentStatus);

    @Query("select c from Comment c where c.status <>:status and lower(c.title) like %:filter% or lower(c.text) like %:filter%")
    Page<Comment> findAllNotDeletedAndFiltered(Pageable pageRequest, @Param("status") CommonStatus commentStatus, String filter);

    @Query("select c from Comment c where c.projectDoc.id =:id")
    Page<Comment> findAllByProjectDocId(@Param("id") UUID projectDocId, Pageable pageRequest);

    @Query("select c from Comment c where c.asbuiltDoc.id =:id")
    Page<Comment> findAllByAsbuiltDocId(@Param("id") UUID projectDocId, Pageable pageRequest);

    @Query("select c from Comment c where (c.createdAt >= :lastWeek or c.updatedAt >= :lastWeek)")
    Page<Comment> findAllForLastWeek(@Param("lastWeek") LocalDateTime lastWeek, Pageable pageRequest);

    @Query("select c from Comment c where (c.createdAt >= :lastWeek or c.updatedAt >= :lastWeek) and " +
            "(c.createdBy.id =:userId or c.updatedBy.id =:userId)")
    Page<Comment> findAllForLastWeekByUser(@Param("userId") Long userId, @Param("lastWeek") LocalDateTime lastWeek, Pageable pageRequest);
}
