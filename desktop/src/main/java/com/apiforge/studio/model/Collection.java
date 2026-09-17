package com.apiforge.studio.model;

import java.util.ArrayList;
import java.util.List;

public class Collection {
    private int id;
    private String name;
    private List<Folder> folders = new ArrayList<>();
    private List<ApiRequest> requests = new ArrayList<>();

    public Collection() {}

    public Collection(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Folder> getFolders() { return folders; }
    public void setFolders(List<Folder> folders) { this.folders = folders; }

    public List<ApiRequest> getRequests() { return requests; }
    public void setRequests(List<ApiRequest> requests) { this.requests = requests; }

    @Override
    public String toString() {
        return name;
    }
}
