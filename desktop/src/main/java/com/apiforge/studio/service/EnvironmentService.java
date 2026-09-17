package com.apiforge.studio.service;

import com.apiforge.studio.model.EnvVariable;
import com.apiforge.studio.model.Environment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnvironmentService {

    private final Connection connection;

    public EnvironmentService(Connection connection) {
        this.connection = connection;
    }

    public List<Environment> getEnvironments(int workspaceId) {
        List<Environment> envs = new ArrayList<>();
        String query = "SELECT * FROM environments WHERE workspace_id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, workspaceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Environment env = new Environment();
                    env.setId(String.valueOf(rs.getInt("id")));
                    env.setWorkspaceId(rs.getInt("workspace_id"));
                    env.setName(rs.getString("name"));
                    env.setDefault(rs.getBoolean("is_default"));
                    env.setVariables(getVariables(rs.getInt("id")));
                    envs.add(env);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load environments: " + e.getMessage());
        }
        return envs;
    }

    private List<EnvVariable> getVariables(int envId) {
        List<EnvVariable> vars = new ArrayList<>();
        String query = "SELECT * FROM env_variables WHERE environment_id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, envId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    EnvVariable v = new EnvVariable();
                    v.setId(String.valueOf(rs.getInt("id")));
                    v.setEnvironmentId(rs.getInt("environment_id"));
                    v.setKey(rs.getString("key"));
                    v.setValue(rs.getString("value"));
                    v.setSecret(rs.getBoolean("is_secret"));
                    vars.add(v);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load variables: " + e.getMessage());
        }
        return vars;
    }

    public Map<String, String> getEnvironmentVariablesAsMap(int envId) {
        Map<String, String> map = new HashMap<>();
        List<EnvVariable> vars = getVariables(envId);
        for (EnvVariable v : vars) {
            map.put(v.getKey(), v.getValue());
        }
        return map;
    }

    public int createEnvironment(int workspaceId, String name) {
        String query = "INSERT INTO environments (workspace_id, name, is_default) VALUES (?, ?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, workspaceId);
            pstmt.setString(2, name);
            pstmt.setBoolean(3, false);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            System.err.println("Failed to create environment: " + e.getMessage());
        }
        return -1;
    }

    public void addVariable(int envId, String key, String value, boolean isSecret) {
        String query = "INSERT INTO env_variables (environment_id, key, value, is_secret) VALUES (?, ?, ?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, envId);
            pstmt.setString(2, key);
            pstmt.setString(3, value);
            pstmt.setBoolean(4, isSecret);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Failed to add variable: " + e.getMessage());
        }
    }
}
