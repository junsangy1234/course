package com.junsang.course_backend.domain.activity.repository;

import com.junsang.course_backend.domain.activity.entity.ActivitySearchTerm;
import com.junsang.course_backend.domain.place.entity.PlaceProvider;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivitySearchTermRepository extends JpaRepository<ActivitySearchTerm, Long> {

    @Query("""
    SELECT term
    FROM ActivitySearchTerm term
    WHERE term.activityCategory.id = :activityCategoryId
      AND term.provider = :provider
      AND term.active = TRUE
    ORDER BY term.fallback ASC, term.searchPriority ASC
    """)
    List<ActivitySearchTerm> findActiveSearchTermsForCategoryAndProvider(
            @Param("activityCategoryId") Long activityCategoryId,
            @Param("provider") PlaceProvider provider
    );
}
