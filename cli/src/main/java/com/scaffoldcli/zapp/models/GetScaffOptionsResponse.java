package com.scaffoldcli.zapp.models;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public class GetScaffOptionsResponse {
    private String id;
    private String name;
    private String descr;
    private Author author;
    private String parent;

    public static Map<String, GetScaffsResponse> fromJson(String json) {
        Gson gson = new Gson();

        Type projectMapType = new TypeToken<Map<String, GetScaffsResponse>>() {
        }.getType();

        return gson.fromJson(json, projectMapType);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    private class Author {
        private String username;
        private String email;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}