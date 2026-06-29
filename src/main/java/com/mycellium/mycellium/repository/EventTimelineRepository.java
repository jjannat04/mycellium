package com.mycellium.mycellium.repository;

import com.mycellium.mycellium.model.EventTimeline;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventTimelineRepository extends JpaRepository<EventTimeline, Long> {
    List<EventTimeline> findByEventIdOrderByDisplayOrderAscTimelineDateAsc(Long eventId);

    @Transactional
    void deleteByEventId(Long eventId);
}
