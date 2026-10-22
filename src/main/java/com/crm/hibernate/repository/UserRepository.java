package com.crm.hibernate.repository;

import com.crm.hibernate.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    List<User> findByUsernameStartingWith(String baseUsername);
    @Query("select count(u) from User u where u.username like concat(:baseUsername, '%')")
    long countByBaseUsername(@Param("baseUsername") String baseUsername);
}
