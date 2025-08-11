// src/main/java/com/github/vibek/nic/service/GeographyService.java
package com.github.vibek.nic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.vibek.nic.entity.District;
import com.github.vibek.nic.entity.Subdivision;
import com.github.vibek.nic.repository.DistrictRepository;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GeographyService {

    private final DistrictRepository districtRepository;

    public List<District> getAllDistricts() {
        return districtRepository.findAll();
    }

    public List<Subdivision> getSubdivisionsByDistrictName(String districtName) {
        return districtRepository.findByName(districtName)
                .map(District::getSubdivisions)
                .orElse(Collections.emptyList());
    }
}