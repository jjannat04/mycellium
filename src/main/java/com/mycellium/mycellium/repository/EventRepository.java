package com.mycellium.mycellium.repository;

import com.mycellium.mycellium.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    // Crucial for loading ONLY the events published by the currently logged-in organizer
    List<Event> findByOrganizerEmail(String organizerEmail);
    Page<Event> findByCategory(String category, Pageable pageable);
    Page<Event> findByStatus(String status, Pageable pageable);
    Page<Event> findByStatusAndCategory(String status, String category, Pageable pageable);

    @Query(value = """
            select e
            from Event e
            left join Registration r on r.eventId = e.id and r.status = 'REGISTERED'
            where e.status = 'PUBLISHED'
              and (:category is null or e.category = :category)
            group by e
            order by count(r.id) desc
            """,
            countQuery = "select count(e) from Event e where e.status = 'PUBLISHED' and (:category is null or e.category = :category)")
    Page<Event> findAllOrderByPopularity(@Param("category") String category, Pageable pageable);
}
