package com.apiforge.studio.model;

import java.util.ArrayList;
import java.util.List;

public class Folder {
    private int id;
    private int collectionId;
    private String name;
    private List<ApiRequest> requests = new ArrayList<>();

    public Folder() {}

    public Folder(int id, int collectionId, String name) {
        this.id = id;
        this.collectionId = collectionId;
        this.name = name;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCollectionId() { return collectionId; }
    public void setCollectionId(int collectionId) { this.collectionId = collectionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<ApiRequest> getRequests() { return requests; }
    public void setRequests(List<ApiRequest> requests) { this.requests = requests; }

    @Override
    public String toString() {
        return name;
    }
}
