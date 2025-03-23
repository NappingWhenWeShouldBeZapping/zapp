package com.scaffoldcli.zapp.models;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class GetScaffsResponse {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("descr")
    private String description;

    @SerializedName("author")
    private Author author;

    @SerializedName("parent")
    private String parent;

    public static List<GetScaffsResponse> fromJson(String json) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<GetScaffsResponse>>() {
        }.getType();
        return gson.fromJson(json, listType);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Author getAuthor() {
        return author;
    }

    public String getParent() {
        return parent;
    }

    public class Author {
        @SerializedName("username")
        private String username;

        @SerializedName("email")
        private String email;

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }
    }
}

