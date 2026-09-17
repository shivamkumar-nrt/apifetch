package com.apiforge.backend.environment.service;

import com.apiforge.backend.environment.dto.EnvVariableDto;
import com.apiforge.backend.environment.dto.EnvironmentDto;
import com.apiforge.backend.environment.entity.EnvVariable;
import com.apiforge.backend.environment.entity.Environment;
import com.apiforge.backend.environment.repository.EnvironmentRepository;
import com.apiforge.backend.workspace.entity.Workspace;
import com.apiforge.backend.workspace.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EnvironmentService {

    private final EnvironmentRepository environmentRepository;
    private final WorkspaceRepository workspaceRepository;

    public EnvironmentService(EnvironmentRepository environmentRepository, WorkspaceRepository workspaceRepository) {
        this.environmentRepository = environmentRepository;
        this.workspaceRepository = workspaceRepository;
    }

    @Transactional(readOnly = true)
    public List<EnvironmentDto> getEnvironmentsByWorkspace(UUID workspaceId) {
        return environmentRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EnvironmentDto createEnvironment(EnvironmentDto dto) {
        Workspace workspace = workspaceRepository.findById(dto.getWorkspaceId())
                .orElseThrow(() -> new RuntimeException("Workspace not found"));
                
        Environment environment = new Environment();
        environment.setWorkspace(workspace);
        environment.setName(dto.getName());
        environment.setDefault(dto.isDefault());
        
        if (dto.getVariables() != null) {
            List<EnvVariable> vars = dto.getVariables().stream().map(v -> {
                EnvVariable ev = new EnvVariable();
                ev.setEnvironment(environment);
                ev.setKeyName(v.getKeyName());
                ev.setSecret(v.isSecret());
                if (v.getValue() != null && !v.isSecret()) {
                    ev.setValueEncrypted(v.getValue().getBytes()); // simple stub for now
                }
                return ev;
            }).collect(Collectors.toList());
            environment.setVariables(vars);
        }
        
        return mapToDto(environmentRepository.save(environment));
    }
    
    @Transactional
    public void deleteEnvironment(UUID id) {
        environmentRepository.deleteById(id);
    }

    private EnvironmentDto mapToDto(Environment environment) {
        EnvironmentDto dto = new EnvironmentDto();
        dto.setId(environment.getId());
        dto.setWorkspaceId(environment.getWorkspace().getId());
        dto.setName(environment.getName());
        dto.setDefault(environment.isDefault());
        
        if (environment.getVariables() != null) {
            dto.setVariables(environment.getVariables().stream().map(v -> {
                EnvVariableDto vDto = new EnvVariableDto();
                vDto.setId(v.getId());
                vDto.setKeyName(v.getKeyName());
                vDto.setSecret(v.isSecret());
                if (v.getValueEncrypted() != null && !v.isSecret()) {
                    vDto.setValue(new String(v.getValueEncrypted())); // stub
                }
                return vDto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }
}
