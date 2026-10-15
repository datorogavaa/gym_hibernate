package com.crm.hibernate.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "trainings")
public class Training {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainee_id", nullable = false)
    private Trainee trainee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @Column(name = "training_name", nullable = false)
    private String trainingName;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "training_type_id", nullable = false)
    private TrainingType trainingType;

    @Temporal(TemporalType.DATE)
    @Column(name = "training_date", nullable = false)
    private Date trainingDate;

    @Column(name = "training_duration", nullable = false)
    private Double trainingDuration;

    public Training() {}



    @Override
    public String toString() {
        return "Training{" +
                "id=" + id +
                ", trainee=" + trainee +
                ", trainer=" + trainer +
                ", trainingName='" + trainingName + '\'' +
                ", trainingType=" + trainingType +
                ", trainingDate=" + trainingDate +
                ", trainingDuration=" + trainingDuration +
                '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Training training)) return false;
        return id != null && id.equals(training.id);
    }
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}