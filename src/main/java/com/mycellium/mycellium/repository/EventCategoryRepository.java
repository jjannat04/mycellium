package com.mycellium.mycellium.repository;

import com.mycellium.mycellium.model.EventCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventCategoryRepository extends JpaRepository<EventCategory, Long> {
    EventCategory findByNameIgnoreCase(String name);
    List<EventCategory> findAllByOrderByNameAsc();
}
