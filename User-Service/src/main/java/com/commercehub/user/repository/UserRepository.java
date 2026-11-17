
package com.commercehub.user.repository;

import com.commercehub.user.entity.User;
import com.commercehub.user.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<User> findByIdAndDeletedFalse(Long id);

    @Query("""
    SELECT u FROM User u
    WHERE u.deleted = false
      AND (
          COALESCE(:name, '') = ''
          OR LOWER(u.fullName) LIKE
             LOWER(CONCAT('%', :name, '%'))
      )
      AND (
          COALESCE(:email, '') = ''
          OR LOWER(u.email) LIKE
             LOWER(CONCAT('%', :email, '%'))
      )
      AND (
          :status IS NULL OR u.status = :status
      )
    """)
    Page<User> searchUsers(
            @Param("name") String name,
            @Param("email") String email,
            @Param("status") UserStatus status,
            Pageable pageable
    );
}
