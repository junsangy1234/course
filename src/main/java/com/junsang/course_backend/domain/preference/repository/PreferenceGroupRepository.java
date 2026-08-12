package com.junsang.course_backend.domain.preference.repository;

import com.junsang.course_backend.domain.preference.entity.PreferenceGroup;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreferenceGroupRepository extends JpaRepository<PreferenceGroup, Long> {
    List<PreferenceGroup> findByActiveTrueOrderByDisplayOrderAsc();
}
