package com.ensate.pfa.entity;

import com.ensate.pfa.entity.enums.DepartmentCode;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    private Long departmentId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false)
    private DepartmentCode code;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<User> users;
}
