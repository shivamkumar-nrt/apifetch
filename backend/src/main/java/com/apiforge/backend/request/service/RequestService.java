package com.apiforge.backend.request.service;

import com.apiforge.backend.collection.entity.Collection;
import com.apiforge.backend.collection.repository.CollectionRepository;
import com.apiforge.backend.request.dto.RequestDto;
import com.apiforge.backend.request.entity.Request;
import com.apiforge.backend.request.repository.RequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RequestService {

    private final RequestRepository requestRepository;
    private final CollectionRepository collectionRepository;

    public RequestService(RequestRepository requestRepository, CollectionRepository collectionRepository) {
        this.requestRepository = requestRepository;
        this.collectionRepository = collectionRepository;
    }

    @Transactional(readOnly = true)
    public List<RequestDto> getRequestsByCollection(UUID collectionId) {
        return requestRepository.findByCollectionIdOrderByOrderIndexAsc(collectionId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public RequestDto createRequest(RequestDto dto) {
        Collection collection = collectionRepository.findById(dto.getCollectionId())
                .orElseThrow(() -> new RuntimeException("Collection not found"));
                
        Request request = new Request();
        request.setCollection(collection);
        request.setFolderId(dto.getFolderId());
        request.setName(dto.getName());
        request.setMethod(dto.getMethod());
        request.setUrl(dto.getUrl());
        request.setHeaders(dto.getHeaders());
        request.setBody(dto.getBody());
        request.setBodyType(dto.getBodyType());
        request.setFormData(dto.getFormData());
        request.setRequestSettings(dto.getRequestSettings());
        request.setProtocol(dto.getProtocol());
        request.setPreRequestScript(dto.getPreRequestScript());
        request.setTestScript(dto.getTestScript());
        request.setOrderIndex(dto.getOrderIndex());
        
        return mapToDto(requestRepository.save(request));
    }
    
    @Transactional
    public RequestDto updateRequest(UUID id, RequestDto dto) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
                
        request.setName(dto.getName());
        request.setMethod(dto.getMethod());
        request.setUrl(dto.getUrl());
        request.setHeaders(dto.getHeaders());
        request.setBody(dto.getBody());
        request.setBodyType(dto.getBodyType());
        request.setFormData(dto.getFormData());
        request.setRequestSettings(dto.getRequestSettings());
        request.setProtocol(dto.getProtocol());
        request.setPreRequestScript(dto.getPreRequestScript());
        request.setTestScript(dto.getTestScript());
        request.setOrderIndex(dto.getOrderIndex());
        
        return mapToDto(requestRepository.save(request));
    }
    
    @Transactional
    public void deleteRequest(UUID id) {
        requestRepository.deleteById(id);
    }

    private RequestDto mapToDto(Request request) {
        RequestDto dto = new RequestDto();
        dto.setId(request.getId());
        dto.setCollectionId(request.getCollection().getId());
        dto.setFolderId(request.getFolderId());
        dto.setName(request.getName());
        dto.setMethod(request.getMethod());
        dto.setUrl(request.getUrl());
        dto.setHeaders(request.getHeaders());
        dto.setBody(request.getBody());
        dto.setBodyType(request.getBodyType());
        dto.setFormData(request.getFormData());
        dto.setRequestSettings(request.getRequestSettings());
        dto.setProtocol(request.getProtocol());
        dto.setPreRequestScript(request.getPreRequestScript());
        dto.setTestScript(request.getTestScript());
        dto.setOrderIndex(request.getOrderIndex());
        return dto;
    }
}
