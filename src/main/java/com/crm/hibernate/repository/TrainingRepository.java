package com.crm.hibernate.repository;

import com.crm.hibernate.entity.Training;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingRepository extends JpaRepository<Training, Long>, TrainingRepositoryCustom {
}
