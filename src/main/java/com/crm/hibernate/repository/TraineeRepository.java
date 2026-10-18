package com.crm.hibernate.repository;

import com.crm.hibernate.entity.Trainee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TraineeRepository extends JpaRepository<Trainee, Long> {

    @Query("select distinct t from Trainee t join fetch t.user u left join fetch t.trainers where u.username = :username")
    Optional<Trainee> findByUsername(@Param("username") String username);
}
