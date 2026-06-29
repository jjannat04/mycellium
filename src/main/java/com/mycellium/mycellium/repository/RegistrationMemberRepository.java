package com.mycellium.mycellium.repository;

import com.mycellium.mycellium.model.RegistrationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RegistrationMemberRepository extends JpaRepository<RegistrationMember, Long> {
    List<RegistrationMember> findByRegistrationId(Long registrationId);
}
