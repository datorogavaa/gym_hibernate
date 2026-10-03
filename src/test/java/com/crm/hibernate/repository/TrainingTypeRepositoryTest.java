package com.crm.hibernate.repository;

import com.crm.hibernate.config.AppConfig;
import com.crm.hibernate.entity.TrainingType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(classes = AppConfig.class)
@Transactional
class TrainingTypeRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Test
    void findByIdReturnsTrainingTypeWhenExists() {
        em.createNativeQuery("ALTER TABLE training_types ALTER COLUMN id RESTART WITH 1000").executeUpdate();
        TrainingType type = new TrainingType();
        type.setTrainingTypeName("Java");
        em.persist(type);
        em.flush();
        em.clear();

        Optional<TrainingType> result = trainingTypeRepository.findById(type.getId());
        assertTrue(result.isPresent());
        assertEquals("Java", result.orElseThrow().getTrainingTypeName());
    }
}