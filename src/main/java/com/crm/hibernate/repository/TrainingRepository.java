package com.crm.hibernate.repository;

import com.crm.hibernate.entity.Trainee;
import com.crm.hibernate.entity.Training;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingRepository extends JpaRepository<Training, Long>, TrainingRepositoryCustom {
    List<Training> findByTrainee(Trainee trainee);

}
