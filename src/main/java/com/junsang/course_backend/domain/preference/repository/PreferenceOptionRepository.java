package com.junsang.course_backend.domain.preference.repository;

import com.junsang.course_backend.domain.preference.entity.PreferenceOption;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreferenceOptionRepository extends JpaRepository<PreferenceOption, Long> {
    @Query("""
            SELECT option
            FROM PreferenceOption option
            JOIN FETCH option.group group
            WHERE option.active = TRUE
              AND group.active = TRUE
            ORDER BY group.displayOrder ASC, option.displayOrder ASC
            """)
    List<PreferenceOption> findAllActiveWithGroupOrderByGroupAndOption();
}
