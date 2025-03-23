package com.levelUp2.project_scaffolding_server.db.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "var_types")
@NoArgsConstructor
@AllArgsConstructor
public class VarType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(nullable = false, unique = true)
    private String name;
}
