package com.levelUp2.project_scaffolding_server.db.entity;

import lombok.Data;


@Data
public class ApiEndpoint {
    private String path;
    private String method;
    private String description;
}