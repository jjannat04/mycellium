package com.mycellium.mycellium.repository;

import com.mycellium.mycellium.model.Registration;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByStudentEmail(String studentEmail);
    List<Registration> findByStudentEmailAndStatus(String studentEmail, String status);
    List<Registration> findByEventId(Long eventId);
    boolean existsByEventIdAndStudentEmail(Long eventId, String studentEmail);

    @Query("""
            select count(r) > 0
            from Registration r
            where r.eventId = :eventId
              and r.studentEmail = :studentEmail
              and r.status = 'REGISTERED'
              and ((:segmentId is null and r.segmentId is null) or r.segmentId = :segmentId)
            """)
    boolean existsActiveRegistration(@Param("eventId") Long eventId,
                                     @Param("segmentId") Long segmentId,
                                     @Param("studentEmail") String studentEmail);

    long countByEventIdAndStatus(Long eventId, String status);
    long countBySegmentIdAndStatus(Long segmentId, String status);
    long countByStudentEmailAndStatus(String studentEmail, String status);
    long countByStatus(String status);

    @Query("""
            select r.segmentId
            from Registration r
            where r.eventId = :eventId
              and r.studentEmail = :studentEmail
              and r.status = 'REGISTERED'
              and r.segmentId is not null
            """)
    List<Long> findActiveSegmentIds(@Param("eventId") Long eventId,
                                    @Param("studentEmail") String studentEmail);

    @Transactional
    void deleteByEventId(Long eventId);

    @Query("""
            select r
            from Registration r
            where r.eventId in :eventIds
            order by r.eventId asc, r.segmentId asc, r.id desc
            """)
    List<Registration> findByEventIds(@Param("eventIds") List<Long> eventIds);
}
