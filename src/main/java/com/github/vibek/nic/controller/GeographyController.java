// src/main/java/com/github/vibek/nic/controller/GeographyController.java
package com.github.vibek.nic.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.vibek.nic.service.GeographyService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/geography")
@RequiredArgsConstructor
public class GeographyController {

    private final GeographyService geographyService;

    // Simple record to avoid exposing the full entity with nested lists
    private record DistrictDto(Long id, String name) {}
    private record SubdivisionDto(Long id, String name) {}

    @GetMapping("/districts")
    public ResponseEntity<List<DistrictDto>> getAllDistricts() {
        List<DistrictDto> districtDtos = geographyService.getAllDistricts().stream()
                .map(d -> new DistrictDto(d.getId(), d.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(districtDtos);
    }

    @GetMapping("/districts/{districtName}/subdivisions")
    public ResponseEntity<List<SubdivisionDto>> getSubdivisionsByDistrict(@PathVariable String districtName) {
        List<SubdivisionDto> subdivisionDtos = geographyService.getSubdivisionsByDistrictName(districtName).stream()
                .map(s -> new SubdivisionDto(s.getId(), s.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(subdivisionDtos);
    }
}