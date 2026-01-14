package com.ensate.pfa.controller;

import com.ensate.pfa.entity.User;
import com.ensate.pfa.dto.UserDto;
import com.ensate.pfa.repository.UserRepository;
import com.ensate.pfa.repository.DepartmentRepository;
import com.ensate.pfa.entity.Department;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(user -> {
            UserDto dto = new UserDto();
            dto.setUserId(user.getUserId());
            dto.setEmail(user.getEmail());
            dto.setPassword(user.getPassword());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setRole(user.getRole() != null ? user.getRole().name() : null);
            dto.setPhoneNumber(user.getPhoneNumber());
            dto.setCreatedAt(user.getCreatedAt());
            dto.setDepartmentId(user.getDepartment() != null ? user.getDepartment().getDepartmentId() : null);
            return dto;
        }).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setRole(userDto.getRole() != null ? com.ensate.pfa.entity.enums.Role.valueOf(userDto.getRole()) : null);
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setCreatedAt(System.currentTimeMillis());
        if (userDto.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(userDto.getDepartmentId()).orElse(null);
            user.setDepartment(dept);
        } else {
            user.setDepartment(null);
        }
        User saved = userRepository.save(user);

        UserDto dto = new UserDto();
        dto.setUserId(saved.getUserId());
        dto.setEmail(saved.getEmail());
        dto.setPassword(saved.getPassword());
        dto.setFirstName(saved.getFirstName());
        dto.setLastName(saved.getLastName());
        dto.setRole(saved.getRole() != null ? saved.getRole().name() : null);
        dto.setPhoneNumber(saved.getPhoneNumber());
        dto.setCreatedAt(saved.getCreatedAt());
        dto.setDepartmentId(saved.getDepartment() != null ? saved.getDepartment().getDepartmentId() : null);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        return userRepository.findById(id)
                .map(existing -> {
                    user.setUserId(id);
                    User updated = userRepository.save(user);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
