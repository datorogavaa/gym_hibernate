package com.crm.hibernate.repository;

import com.crm.hibernate.entity.Trainer;
import com.crm.hibernate.entity.TrainingType;
import com.crm.hibernate.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Trainer> typedQuery;

    @InjectMocks
    private TrainerRepository trainerRepository;

    @Test
    void savePersistsAndFindByUsernameReturnsTrainer() {
        TrainingType specialization = new TrainingType();
        specialization.setTrainingTypeName("Java");
        User user = new User("Repo", "Trainer", "repo.trainer.save", "secret", true);

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(specialization);

        when(em.createQuery(anyString(), eq(Trainer.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(trainer);

        trainerRepository.save(trainer);
        Optional<Trainer> found = trainerRepository.findByUsername("repo.trainer.save");

        verify(em).persist(trainer);
        assertTrue(found.isPresent());
        assertEquals("repo.trainer.save", found.orElseThrow().getUser().getUsername());
    }

    @Test
    void findByUsernamesReturnsMatchingTrainers() {
        TrainingType specialization = new TrainingType();
        specialization.setTrainingTypeName("Python");
        User user = new User("Repo", "Lookup", "repo.trainer.lookup", "secret", true);

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(specialization);

        when(em.createQuery(anyString(), eq(Trainer.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of(trainer));

        List<Trainer> result = trainerRepository.findByUsernames(Set.of("repo.trainer.lookup"));

        assertEquals(1, result.size());
        assertEquals("repo.trainer.lookup", result.get(0).getUser().getUsername());
    }
}