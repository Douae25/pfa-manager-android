package com.ensate.pfa.controller;

import com.ensate.pfa.dto.response.ApiResponse;
import com.ensate.pfa.dto.response.DepartmentDTO;
import com.ensate.pfa.entity.Department;
import com.ensate.pfa.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentDTO>>> getAllDepartments() {
        List<DepartmentDTO> departments = departmentRepository.findAll().stream()
                .map(dept -> DepartmentDTO.builder()
                        .departmentId(dept.getDepartmentId())
                        .name(dept.getName())
                        .code(dept.getCode().name())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.<List<DepartmentDTO>>builder()
                        .success(true)
                        .message("Départements récupérés")
                        .data(departments)
                        .build()
        );
    }
}