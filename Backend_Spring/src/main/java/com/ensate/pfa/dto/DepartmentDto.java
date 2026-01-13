package com.ensate.pfa.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class DepartmentDto {
    @JsonProperty("department_id")
    private Long departmentId;
    private String name;
    private String code;
}
