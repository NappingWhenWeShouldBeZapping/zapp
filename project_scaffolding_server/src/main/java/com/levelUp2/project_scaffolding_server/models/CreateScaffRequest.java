package com.levelUp2.project_scaffolding_server.models;

import java.util.List;

public class CreateScaffRequest {
    private String scaffName;
    private List<Insertion> insertions;
    private String scaffDescr;
    private String parentId;

    public String getScaffName() {
        return scaffName;
    }

    public void setScaffName(String scaffName) {
        this.scaffName = scaffName;
    }

    public List<Insertion> getInsertions() {
        return insertions;
    }

    public void setInsertions(List<Insertion> insertions) {
        this.insertions = insertions;
    }

    public String getScaffDescr() {
        return scaffDescr;
    }

    public void setScaffDescr(String scaffDescr) {
        this.scaffDescr = scaffDescr;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public static class Insertion {
        private String relativePath;
        private String content;

        public String getRelativePath() {
            return relativePath;
        }

        public void setRelativePath(String relativePath) {
            this.relativePath = relativePath;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
