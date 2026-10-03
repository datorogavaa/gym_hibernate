package com.crm.hibernate.repository;

import com.crm.hibernate.config.AppConfig;
import com.crm.hibernate.entity.Trainee;
import com.crm.hibernate.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(classes = AppConfig.class)
@Transactional
class TraineeRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private TraineeRepository traineeRepository;

    private void resetIdentitySequences() {
        em.createNativeQuery("ALTER TABLE users ALTER COLUMN id RESTART WITH 1000").executeUpdate();
        em.createNativeQuery("ALTER TABLE trainees ALTER COLUMN id RESTART WITH 1000").executeUpdate();
    }

    @Test
    void savePersistsTraineeAndFindByUsernameReturnsIt() {
        resetIdentitySequences();
        User user = new User("Repo", "Trainee", "repo.trainee", "secret", true);

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setDateOfBirth(new Date());
        trainee.setAddress("Main Street");

        traineeRepository.save(trainee);
        em.flush();
        em.clear();

        Optional<Trainee> found = traineeRepository.findByUsername("repo.trainee");
        assertTrue(found.isPresent());
        assertEquals("repo.trainee", found.orElseThrow().getUser().getUsername());
    }

    @Test
    void deleteRemovesTrainee() {
        resetIdentitySequences();
        User user = new User("Repo", "Delete", "repo.delete", "secret", true);

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        traineeRepository.save(trainee);
        em.flush();
        em.clear();

        traineeRepository.delete(trainee);
        em.flush();
        em.clear();

        assertTrue(traineeRepository.findByUsername("repo.delete").isEmpty());
    }
}