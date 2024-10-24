package ITMO.ConstructionDocs.model.db.repository;

import ITMO.ConstructionDocs.model.db.entity.User;
import ITMO.ConstructionDocs.model.enums.CommonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("select u from User u where u.status <>:status")
    Page<User> findAllNotDeleted(Pageable pageRequest, @Param("status") CommonStatus userStatus);

    @Query("select u from User u where u.status <>:status and (lower(u.firstName) like %:filter% or lower(u.lastName) like %:filter% or lower(u.position) like %:filter% or lower(u.company) like %:filter%)")
    Page<User> findAllNotDeletedAndFiltered(Pageable pageRequest, @Param("status") CommonStatus userStatus, String filter);

    Optional<User> findByEmailIgnoreCase(String email);

    @Query("select u from User u where u.company.id =:id")
    Page<User> findAllByCompany(Pageable pageRequest, @Param("id") Long id);
}
