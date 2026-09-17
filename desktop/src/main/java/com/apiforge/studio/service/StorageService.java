package com.apiforge.studio.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.apiforge.studio.model.ApiRequest;
import com.apiforge.studio.model.HistoryEntry;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class StorageService {
    private final ObjectMapper objectMapper;
    private final File historyFile;
    private final File favoritesFile;

    public StorageService() {
        this.objectMapper = new ObjectMapper();
        String userHome = System.getProperty("user.home");
        File appDir = new File(userHome, ".apiforge");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        this.historyFile = new File(appDir, "history.json");
        this.favoritesFile = new File(appDir, "favorites.json");
    }

    public List<HistoryEntry> loadHistory() {
        if (!historyFile.exists()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(historyFile, new TypeReference<List<HistoryEntry>>() {});
        } catch (IOException e) {
            System.err.println("Failed to load history: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void saveHistory(List<HistoryEntry> history) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(historyFile, history);
        } catch (IOException e) {
            System.err.println("Failed to save history: " + e.getMessage());
        }
    }

    public List<ApiRequest> loadFavorites() {
        if (!favoritesFile.exists()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(favoritesFile, new TypeReference<List<ApiRequest>>() {});
        } catch (IOException e) {
            System.err.println("Failed to load favorites: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void saveFavorites(List<ApiRequest> favorites) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(favoritesFile, favorites);
        } catch (IOException e) {
            System.err.println("Failed to save favorites: " + e.getMessage());
        }
    }
}
