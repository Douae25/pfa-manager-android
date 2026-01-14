package com.ensate.pfa.controller;

import com.ensate.pfa.entity.Department;
import com.ensate.pfa.dto.DepartmentDto;
import com.ensate.pfa.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentRepository departmentRepository;

    @GetMapping
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll().stream().map(dep -> {
            DepartmentDto dto = new DepartmentDto();
            dto.setDepartmentId(dep.getDepartmentId());
            dto.setName(dep.getName());
            // Enlève le suffixe 2 si présent
            String code = dep.getCode() != null ? dep.getCode().name() : null;
            if (code != null && code.endsWith("2")) code = code.substring(0, code.length() - 1);
            dto.setCode(code);
            return dto;
        }).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Long id) {
        return departmentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Department> createDepartment(@RequestBody Department department) {
        Department saved = departmentRepository.save(department);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Department> updateDepartment(@PathVariable Long id, @RequestBody Department department) {
        return departmentRepository.findById(id)
                .map(existing -> {
                    department.setDepartmentId(id);
                    Department updated = departmentRepository.save(department);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        if (departmentRepository.existsById(id)) {
            departmentRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
