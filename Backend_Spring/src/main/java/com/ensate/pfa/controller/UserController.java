package com.ensate.pfa.controller;

import com.ensate.pfa.dto.response.ApiResponse;
import com.ensate.pfa.dto.response.UserDTO;
import com.ensate.pfa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;

    /**
     * Récupérer tous les utilisateurs
     * GET /api/users/all
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        List<UserDTO> users = userRepository.findAll().stream()
                .map(user -> UserDTO.builder()
                        .userId(user.getUserId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .password(user.getPassword())
                        .role(user.getRole().name())
                        .phoneNumber(user.getPhoneNumber())
                        .departmentId(user.getDepartment() != null ?
                                user.getDepartment().getDepartmentId() : null)
                        .createdAt(user.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.<List<UserDTO>>builder()
                        .success(true)
                        .message("Utilisateurs récupérés")
                        .data(users)
                        .build()
        );
    }

    /**
     * Récupérer un utilisateur par ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    UserDTO userDTO = UserDTO.builder()
                            .userId(user.getUserId())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .email(user.getEmail())
                            .password(user.getPassword())
                            .role(user.getRole().name())
                            .phoneNumber(user.getPhoneNumber())
                            .departmentId(user.getDepartment() != null ?
                                    user.getDepartment().getDepartmentId() : null)
                            .createdAt(user.getCreatedAt())
                            .build();

                    return ResponseEntity.ok(
                            ApiResponse.<UserDTO>builder()
                                    .success(true)
                                    .message("Utilisateur trouvé")
                                    .data(userDTO)
                                    .build()
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Récupérer les utilisateurs par rôle
     * GET /api/users/by-role/{role}
     */
    @GetMapping("/by-role/{role}")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getUsersByRole(@PathVariable String role) {
        List<UserDTO> users = userRepository.findAll().stream()
                .filter(user -> user.getRole().name().equalsIgnoreCase(role))
                .map(user -> UserDTO.builder()
                        .userId(user.getUserId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .password(user.getPassword())
                        .role(user.getRole().name())
                        .phoneNumber(user.getPhoneNumber())
                        .departmentId(user.getDepartment() != null ?
                                user.getDepartment().getDepartmentId() : null)
                        .createdAt(user.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.<List<UserDTO>>builder()
                        .success(true)
                        .message("Utilisateurs récupérés par rôle")
                        .data(users)
                        .build()
        );
    }

    /**
     * Récupérer les utilisateurs par département
     * GET /api/users/by-department/{departmentId}
     */
    @GetMapping("/by-department/{departmentId}")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getUsersByDepartment(
            @PathVariable Long departmentId) {
        List<UserDTO> users = userRepository.findByDepartmentDepartmentId(departmentId).stream()
                .map(user -> UserDTO.builder()
                        .userId(user.getUserId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .password(user.getPassword())
                        .role(user.getRole().name())
                        .phoneNumber(user.getPhoneNumber())
                        .departmentId(user.getDepartment() != null ?
                                user.getDepartment().getDepartmentId() : null)
                        .createdAt(user.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.<List<UserDTO>>builder()
                        .success(true)
                        .message("Utilisateurs récupérés par département")
                        .data(users)
                        .build()
        );
    }
}