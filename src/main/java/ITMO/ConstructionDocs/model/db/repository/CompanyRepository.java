package ITMO.ConstructionDocs.model.db.repository;

import ITMO.ConstructionDocs.model.db.entity.Company;
import ITMO.ConstructionDocs.model.enums.CommonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    @Query("select c from Company c where c.status <>:status")
    Page<Company> findAllNotDeleted(Pageable pageRequest, @Param("status") CommonStatus companyStatus);

    @Query("select c from Company c where c.status <>:status and (lower(c.name) like %:filter% or lower(c.address) like %:filter%)")
    Page<Company> findAllNotDeletedAndFiltered(Pageable pageRequest, @Param("status") CommonStatus companyStatus, String filter);

    Optional<Company> findByNameIgnoreCase(String name);

    @Query("select c from Company c join c.projects p where p.id =:id")
    Page<Company> findAllByProjectId(@Param("id") Long projectId, Pageable pageRequest);
}
