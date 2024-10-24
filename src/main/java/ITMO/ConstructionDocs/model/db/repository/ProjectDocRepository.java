package ITMO.ConstructionDocs.model.db.repository;

import ITMO.ConstructionDocs.model.db.entity.ProjectDoc;
import ITMO.ConstructionDocs.model.enums.DocStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ProjectDocRepository extends JpaRepository<ProjectDoc, UUID> {
    @Query("select p from ProjectDoc p where p.docStatus <>:status")
    Page<ProjectDoc> findAllNotDeleted(Pageable pageRequest, @Param("status") DocStatus status);

    @Query("select p from ProjectDoc p where p.docStatus <>:status and lower(p.fileName) like %:filter% or " +
            "lower(p.project.name) like %:filter% or " +
            "lower(cast(p.designCategory as string)) like %:filter%")
    Page<ProjectDoc> findAllNotDeletedAndFiltered(Pageable pageRequest, @Param("status") DocStatus status, String filter);

    @Query("select p from ProjectDoc p where p.project.id =:id")
    Page<ProjectDoc> findAllByProjectId(@Param("id") Long projectId, Pageable pageRequest);

    @Query("select p from ProjectDoc p where (p.createdAt >= :lastWeek or p.updatedAt >= :lastWeek) and " +
            "p.project.id =:id")
    Page<ProjectDoc> findAllForLastWeekByProjectId(@Param("lastWeek") LocalDateTime lastWeek, @Param("id") Long projectId, Pageable pageRequest);
}
