package com.mycellium.mycellium.repository;

import com.mycellium.mycellium.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);
}