package com.mycellium.mycellium.repository;

import com.mycellium.mycellium.model.EventSegment;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventSegmentRepository extends JpaRepository<EventSegment, Long> {
    List<EventSegment> findByEventIdOrderByCreatedAtAsc(Long eventId);

    @Transactional
    void deleteByEventId(Long eventId);
}
