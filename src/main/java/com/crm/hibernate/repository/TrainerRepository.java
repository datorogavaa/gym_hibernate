package com.crm.hibernate.repository;

import com.crm.hibernate.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    @Query("select distinct tr from Trainer tr " +
            "join fetch tr.user u join fetch tr.specialization " +
            "left join fetch tr.trainees trainees left join fetch trainees.user " +
            "where u.username = :username")
    Optional<Trainer> findByUsername(@Param("username") String username);

    @Query("select distinct tr from Trainer tr join fetch tr.user " +
            "where not exists (select 1 from Trainee t join t.trainers ttr where ttr.id = tr.id)")
    List<Trainer> findNotAssignedToTrainee(String traineeUsername);

    @Query("select tr from Trainer tr join tr.user u where u.username in :usernames")
    List<Trainer> findByUsernames(@Param("usernames") Set<String> usernames);
}
