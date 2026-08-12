package com.junsang.course_backend.domain.place.controller;

import com.junsang.course_backend.domain.place.dto.request.PlaceCandidateSearchRequest;
import com.junsang.course_backend.domain.place.dto.response.PlaceCandidateResponse;
import com.junsang.course_backend.domain.place.service.PlaceSearchService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/places")
public class PlaceSearchController {

    private final PlaceSearchService placeSearchService;

    public PlaceSearchController(PlaceSearchService placeSearchService) {
        this.placeSearchService = placeSearchService;
    }

    @GetMapping("/candidates")
    public List<PlaceCandidateResponse> searchCandidates(
            @Valid @ModelAttribute PlaceCandidateSearchRequest request
    ) {
        return placeSearchService.searchCandidates(
                request.getQuery(),
                request.getType(),
                request.getLatitude(),
                request.getLongitude(),
                request.getRadius(),
                request.getSize()
        );
    }
}
