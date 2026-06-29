package com.mycellium.mycellium.model;

import jakarta.persistence.*;

@Entity
@Table(name = "registration_members")
public class RegistrationMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long registrationId;
    private String memberName;
    private String memberEmail;

    public RegistrationMember() {}

    public RegistrationMember(Long registrationId, String memberName, String memberEmail) {
        this.registrationId = registrationId;
        this.memberName = memberName;
        this.memberEmail = memberEmail;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRegistrationId() { return registrationId; }
    public void setRegistrationId(Long registrationId) { this.registrationId = registrationId; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public String getMemberEmail() { return memberEmail; }
    public void setMemberEmail(String memberEmail) { this.memberEmail = memberEmail; }
}
