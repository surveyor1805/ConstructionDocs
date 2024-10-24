package ITMO.ConstructionDocs.model.db.repository;

import ITMO.ConstructionDocs.model.db.entity.AsbuiltDoc;
import ITMO.ConstructionDocs.model.enums.DocStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AsbuiltDocRepository extends JpaRepository<AsbuiltDoc, UUID> {
    @Query("select a from AsbuiltDoc a where a.docStatus <>:status")
    Page<AsbuiltDoc> findAllNotDeleted(Pageable pageRequest, @Param("status") DocStatus status);

    @Query("select a from AsbuiltDoc a where a.docStatus <>:status and lower(a.fileName) like %:filter% or " +
            "lower(a.projectDoc.fileName) like %:filter% or " +
            "lower(cast(a.asbuiltCategory as string)) like %:filter%")
    Page<AsbuiltDoc> findAllNotDeletedAndFiltered(Pageable pageRequest, @Param("status") DocStatus status, String filter);

    @Query("select a from AsbuiltDoc a where a.company.id =:id")
    Page<AsbuiltDoc> findAllByCompanyId(@Param("id") Long companyId, Pageable pageRequest);

    @Query("select a from AsbuiltDoc a where a.projectDoc.id =:id")
    Page<AsbuiltDoc> findAllByProjectDocId(@Param("id") UUID projectDocId, Pageable pageRequest);

    @Query("select a from AsbuiltDoc a where a.createdAt >= :lastWeek or a.updatedAt >= :lastWeek")
    Page<AsbuiltDoc> findAllForLastWeek(@Param("lastWeek") LocalDateTime lastWeek, Pageable pageRequest);

    @Query("select a from AsbuiltDoc a where a.createdAt >= :lastWeek or a.updatedAt >= :lastWeek and " +
            "a.projectDoc.id =:id")
    Page<AsbuiltDoc> findAllByProjectDocIdForLastWeek(@Param("id") UUID projectDocId, @Param("lastWeek") LocalDateTime lastWeek, Pageable pageRequest);
}