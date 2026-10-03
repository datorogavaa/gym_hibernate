package com.crm.hibernate.dto;

import java.util.Date;

public class TraineeTrainingCriteria {
    private String traineeUsername;
    private Date fromDate;
    private Date toDate;
    private String trainerName;
    private String trainingType;

    public TraineeTrainingCriteria(String traineeUsername, Date fromDate, Date toDate, String trainerName, String trainingType) {
        this.traineeUsername = traineeUsername;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.trainerName = trainerName;
        this.trainingType = trainingType;
    }

    public String getTraineeUsername() { return traineeUsername; }
    public Date getFromDate() { return fromDate; }
    public Date getToDate() { return toDate; }
    public String getTrainerName() { return trainerName; }
    public String getTrainingType() { return trainingType; }
}