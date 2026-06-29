package com.mycellium.mycellium.repository;

import com.mycellium.mycellium.model.Notification;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop5ByUserEmailOrderByCreatedAtDesc(String userEmail);
    long countByUserEmailAndReadStatus(String userEmail, Boolean readStatus);

    @Transactional
    @Modifying
    @Query("update Notification n set n.readStatus = true where n.userEmail = :userEmail")
    void markAllRead(@Param("userEmail") String userEmail);
}
