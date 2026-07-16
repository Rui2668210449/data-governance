package com.governance.service;

import com.governance.entity.DataStandard;
import com.governance.repository.DataStandardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StandardService {
    private final DataStandardRepository dataStandardRepository;

    public List<DataStandard> listStandards() {
        return dataStandardRepository.findAll();
    }
}
