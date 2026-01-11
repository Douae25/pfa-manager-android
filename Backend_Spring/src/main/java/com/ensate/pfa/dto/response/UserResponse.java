package com.ensate.pfa.dto.response;

import com.ensate.pfa.entity.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private String phoneNumber;
    private Long departmentId;
    private String departmentName;
    private Long createdAt;
}
