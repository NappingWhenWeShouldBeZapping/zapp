package com.scaffoldcli.zapp.models;

import lombok.Data;


@Data
public class ApiEndpoint {
    private String path;
    private String method;
    private String description;
}