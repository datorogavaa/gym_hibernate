package com.crm.hibernate.dto;

import java.util.Date;

public class TrainerTrainingCriteria {
    private String trainerUsername;
    private Date fromDate;
    private Date toDate;
    private String traineeName;

    public TrainerTrainingCriteria(String trainerUsername, Date fromDate, Date toDate, String traineeName) {
        this.trainerUsername = trainerUsername;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.traineeName = traineeName;
    }

    public String getTrainerUsername() { return trainerUsername; }
    public Date getFromDate() { return fromDate; }
    public Date getToDate() { return toDate; }
    public String getTraineeName() { return traineeName; }
}