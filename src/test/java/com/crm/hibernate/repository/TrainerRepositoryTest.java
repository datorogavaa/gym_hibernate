package com.crm.hibernate.repository;

import com.crm.hibernate.config.AppConfig;
import com.crm.hibernate.entity.Trainer;
import com.crm.hibernate.entity.TrainingType;
import com.crm.hibernate.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(classes = AppConfig.class)
@Transactional
class TrainerRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private TrainerRepository trainerRepository;

    private void resetIdentitySequences() {
        em.createNativeQuery("ALTER TABLE training_types ALTER COLUMN id RESTART WITH 1000").executeUpdate();
        em.createNativeQuery("ALTER TABLE users ALTER COLUMN id RESTART WITH 1000").executeUpdate();
        em.createNativeQuery("ALTER TABLE trainers ALTER COLUMN id RESTART WITH 1000").executeUpdate();
    }

    @Test
    void savePersistsAndFindByUsernameReturnsTrainer() {
        resetIdentitySequences();
        TrainingType specialization = new TrainingType();
        specialization.setTrainingTypeName("Java");
        User user = new User("Repo", "Trainer", "repo.trainer.save", "secret", true);

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(specialization);

        em.persist(specialization);
        trainerRepository.save(trainer);
        em.flush();
        em.clear();

        Optional<Trainer> found = trainerRepository.findByUsername("repo.trainer.save");
        assertTrue(found.isPresent());
        assertEquals("repo.trainer.save", found.orElseThrow().getUser().getUsername());
    }

    @Test
    void findByUsernamesReturnsMatchingTrainers() {
        resetIdentitySequences();
        TrainingType specialization = new TrainingType();
        specialization.setTrainingTypeName("Python");
        User user = new User("Repo", "Lookup", "repo.trainer.lookup", "secret", true);

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(specialization);

        em.persist(specialization);
        trainerRepository.save(trainer);
        em.flush();
        em.clear();

        List<Trainer> result = trainerRepository.findByUsernames(Set.of("repo.trainer.lookup"));
        assertEquals(1, result.size());
        assertEquals("repo.trainer.lookup", result.get(0).getUser().getUsername());
    }
}