package com.mycellium.mycellium.repository;

import com.mycellium.mycellium.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);
    List<User> findByRole(String role);
    long countByRole(String role);

    @Query("""
            select u.university, count(u)
            from User u
            where u.role = 'STUDENT'
              and u.university is not null
              and u.university <> ''
            group by u.university
            order by count(u) desc, u.university asc
            """)
    List<Object[]> countStudentsByUniversity();

    @Query("""
            select u.university, count(u)
            from User u
            where u.role = 'ORGANIZER'
              and u.university is not null
              and u.university <> ''
            group by u.university
            order by count(u) desc, u.university asc
            """)
    List<Object[]> countOrganizersByUniversity();

    @Query("""
            select count(distinct u.university)
            from User u
            where u.university is not null
              and u.university <> ''
            """)
    long countDistinctUserUniversities();
}
