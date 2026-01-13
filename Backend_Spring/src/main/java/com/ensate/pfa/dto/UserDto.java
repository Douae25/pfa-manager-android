package com.ensate.pfa.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;

@Data
public class UserDto {
    @JsonProperty("user_id")
    private Long userId;
    private String email;
    private String password;
    @JsonProperty("first_name")
    @JsonAlias({"firstName"})
    private String firstName;

    @JsonProperty("last_name")
    @JsonAlias({"lastName"})
    private String lastName;

    private String role;

    @JsonProperty("phone_number")
    @JsonAlias({"phoneNumber"})
    private String phoneNumber;

    @JsonProperty("created_at")
    private Long createdAt;

    @JsonProperty("department_id")
    @JsonAlias({"departmentId"})
    private Long departmentId;
}
