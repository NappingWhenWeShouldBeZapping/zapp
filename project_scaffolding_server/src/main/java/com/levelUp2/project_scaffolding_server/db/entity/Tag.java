package com.levelUp2.project_scaffolding_server.db.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "tags")
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
    @Id
    private int id;

    @Column(nullable = false, unique = true)
    private String label;
}
