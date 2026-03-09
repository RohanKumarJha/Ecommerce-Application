package com.ecommerce.Ecommerce_Application.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roleId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false, unique = true)
    private AppRole roleName;

    public Role(AppRole roleName) {
        this.roleName = roleName;
    }
}