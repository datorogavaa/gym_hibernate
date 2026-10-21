package com.crm.hibernate.repository;

import com.crm.hibernate.dto.TraineeTrainingCriteria;
import com.crm.hibernate.dto.TrainerTrainingCriteria;
import com.crm.hibernate.entity.Training;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TrainingRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private CriteriaQuery<Training> criteriaQuery;

    @Mock
    private Root<Training> root;

    @Mock
    private TypedQuery<Training> typedQuery;

    @Mock
    private Predicate predicate;

    @Mock
    private Path<Object> path;

    @InjectMocks
    private TrainingRepository trainingRepository;

    private Join<Object, Object> joinAndFetchMock;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // იქმნება Combo-Mock, რომელიც არის ორივე: Join და Fetch
        joinAndFetchMock = Mockito.mock(Join.class, Mockito.withSettings().extraInterfaces(Fetch.class));
    }

    @Test
    void savePersistsTraining() {
        Training training = new Training();

        trainingRepository.save(training);

        verify(em).persist(training);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByTraineeCriteriaReturnsMatchingTrainings() {
        TraineeTrainingCriteria criteria = new TraineeTrainingCriteria(
                "john.doe",
                new Date(),
                new Date(),
                "Bob",
                "Java"
        );

        Training training = new Training();
        training.setTrainingName("Java Fundamentals");

        setupCriteriaMocks();

        when(typedQuery.getResultList()).thenReturn(List.of(training));

        List<Training> result = trainingRepository.findByTraineeCriteria(criteria);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Java Fundamentals", result.get(0).getTrainingName());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByTrainerCriteriaReturnsMatchingTrainings() {
        TrainerTrainingCriteria criteria = new TrainerTrainingCriteria(
                "trainer.bob",
                new Date(),
                new Date(),
                "John"
        );

        Training training = new Training();
        training.setTrainingName("Spring Masterclass");

        setupCriteriaMocks();

        when(typedQuery.getResultList()).thenReturn(List.of(training));

        List<Training> result = trainingRepository.findByTrainerCriteria(criteria);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Spring Masterclass", result.get(0).getTrainingName());
    }

    @SuppressWarnings("unchecked")
    private void setupCriteriaMocks() {
        when(em.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Training.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Training.class)).thenReturn(root);

        when(root.fetch(anyString(), any(JoinType.class))).thenReturn((Fetch) joinAndFetchMock);
        when(((Fetch<?, ?>) joinAndFetchMock).fetch(anyString(), any(JoinType.class))).thenReturn((Fetch) joinAndFetchMock);

        when(joinAndFetchMock.get(anyString())).thenReturn(path);
        when(root.get(anyString())).thenReturn(path);

        when(criteriaBuilder.equal(any(), any())).thenReturn(predicate);
        when(criteriaBuilder.greaterThanOrEqualTo(any(), any(Date.class))).thenReturn(predicate);
        when(criteriaBuilder.lessThanOrEqualTo(any(), any(Date.class))).thenReturn(predicate);

        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(criteriaQuery.distinct(true)).thenReturn(criteriaQuery);
        when(criteriaQuery.where(any(Predicate[].class))).thenReturn(criteriaQuery);

        when(em.createQuery(criteriaQuery)).thenReturn(typedQuery);
    }
}