package com.crm.hibernate.repository;

import com.crm.hibernate.entity.Trainee;
import com.crm.hibernate.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Trainee> typedQuery;

    @InjectMocks
    private TraineeRepository traineeRepository;

    @Test
    void savePersistsTraineeAndFindByUsernameReturnsIt() {
        User user = new User("Repo", "Trainee", "repo.trainee.save", "secret", true);
        Trainee trainee = new Trainee();
        trainee.setUser(user);

        when(em.createQuery(anyString(), eq(Trainee.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(trainee);

        traineeRepository.save(trainee);
        Optional<Trainee> found = traineeRepository.findByUsername("repo.trainee.save");

        verify(em).persist(trainee);
        assertTrue(found.isPresent());
        assertEquals("repo.trainee.save", found.orElseThrow().getUser().getUsername());
    }

    @Test
    void deleteRemovesTrainee() {
        User user = new User("Repo", "Trainee", "repo.trainee.delete", "secret", true);
        Trainee trainee = new Trainee();
        trainee.setUser(user);

        when(em.contains(trainee)).thenReturn(false);
        when(em.merge(trainee)).thenReturn(trainee);

        traineeRepository.delete(trainee);

        verify(em).remove(trainee);
    }
}