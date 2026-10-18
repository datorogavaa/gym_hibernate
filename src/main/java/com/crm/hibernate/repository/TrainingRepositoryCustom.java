package com.crm.hibernate.repository;

import com.crm.hibernate.dto.TraineeTrainingCriteria;
import com.crm.hibernate.dto.TrainerTrainingCriteria;
import com.crm.hibernate.entity.Training;

import java.util.List;

public interface TrainingRepositoryCustom {
    List<Training> findByTraineeCriteria(TraineeTrainingCriteria criteria);

    List<Training> findByTrainerCriteria(TrainerTrainingCriteria criteria);
}
