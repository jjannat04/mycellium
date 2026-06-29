package com.mycellium.mycellium.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "registrations")
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventId;
    private Long segmentId;
    private String studentEmail;
    private String teamName;
    private Integer teamSize;
    private String teamLeaderName;
    private String teamLeaderEmail;
    private String transactionId;
    private String registrationScope;
    private String registrationDate;
    private String status;

    public Registration() {
        this.registrationDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        this.status = "REGISTERED";
    }

    public Registration(Long eventId, String studentEmail) {
        this();
        this.eventId = eventId;
        this.studentEmail = studentEmail;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public Long getSegmentId() { return segmentId; }
    public void setSegmentId(Long segmentId) { this.segmentId = segmentId; }
    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public Integer getTeamSize() { return teamSize; }
    public void setTeamSize(Integer teamSize) { this.teamSize = teamSize; }
    public String getTeamLeaderName() { return teamLeaderName; }
    public void setTeamLeaderName(String teamLeaderName) { this.teamLeaderName = teamLeaderName; }
    public String getTeamLeaderEmail() { return teamLeaderEmail; }
    public void setTeamLeaderEmail(String teamLeaderEmail) { this.teamLeaderEmail = teamLeaderEmail; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getRegistrationScope() { return registrationScope; }
    public void setRegistrationScope(String registrationScope) { this.registrationScope = registrationScope; }
    public String getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
