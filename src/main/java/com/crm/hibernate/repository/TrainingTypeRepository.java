package com.crm.hibernate.repository;

import com.crm.hibernate.entity.TrainingType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class TrainingTypeRepository {
    @PersistenceContext
    private EntityManager em;

    public Optional<TrainingType> findById(Long id) {
        return Optional.ofNullable(em.find(TrainingType.class, id));
    }
}