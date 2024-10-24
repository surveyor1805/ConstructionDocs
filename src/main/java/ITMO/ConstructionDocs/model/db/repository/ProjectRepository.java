package ITMO.ConstructionDocs.model.db.repository;

import ITMO.ConstructionDocs.model.db.entity.Project;
import ITMO.ConstructionDocs.model.enums.CommonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query("select p from Project p where p.status <>:status")
    Page<Project> findAllNotDeleted(Pageable pageRequest, @Param("status") CommonStatus projectStatus);

    @Query("select p from Project p where p.status <>:status and (lower(p.name) like %:filter% or lower(p.address) like %:filter% or lower(p.filesRootDirectory) like %:filter%)")
    Page<Project> findAllNotDeletedAndFiltered(Pageable pageRequest, @Param("status") CommonStatus projectStatus, String filter);

    Optional<Project> findByNameIgnoreCase(String name);

    /*@Query("select p from Project p join p.companies c where c.id =:id")
    Page<Project> findAllByCompanyId(@Param("id") Long companyId, Pageable pageRequest);*/
}
