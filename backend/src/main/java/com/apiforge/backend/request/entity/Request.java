package com.apiforge.backend.request.entity;

import com.apiforge.backend.collection.entity.Collection;
import com.apiforge.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "requests")
public class Request extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;

    @Column(name = "folder_id")
    private UUID folderId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 10)
    private String method = "GET";

    @Column(columnDefinition = "TEXT", nullable = false)
    private String url;

    @Column(columnDefinition = "JSON")
    private String headers;

    @Column(columnDefinition = "JSON")
    private String body;

    @Column(name = "body_type", length = 50)
    private String bodyType;

    @Column(name = "form_data", columnDefinition = "JSON")
    private String formData;

    @Column(name = "request_settings", columnDefinition = "JSON")
    private String requestSettings;

    @Column(nullable = false, length = 20)
    private String protocol = "rest";

    @Column(name = "pre_request_script", columnDefinition = "TEXT")
    private String preRequestScript;

    @Column(name = "test_script", columnDefinition = "TEXT")
    private String testScript;

    @Column(name = "order_index", nullable = false)
    private int orderIndex = 0;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Collection getCollection() { return collection; }
    public void setCollection(Collection collection) { this.collection = collection; }
    public UUID getFolderId() { return folderId; }
    public void setFolderId(UUID folderId) { this.folderId = folderId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getHeaders() { return headers; }
    public void setHeaders(String headers) { this.headers = headers; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getBodyType() { return bodyType; }
    public void setBodyType(String bodyType) { this.bodyType = bodyType; }
    public String getFormData() { return formData; }
    public void setFormData(String formData) { this.formData = formData; }
    public String getRequestSettings() { return requestSettings; }
    public void setRequestSettings(String requestSettings) { this.requestSettings = requestSettings; }
    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    public String getPreRequestScript() { return preRequestScript; }
    public void setPreRequestScript(String preRequestScript) { this.preRequestScript = preRequestScript; }
    public String getTestScript() { return testScript; }
    public void setTestScript(String testScript) { this.testScript = testScript; }
    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    public ZonedDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(ZonedDateTime deletedAt) { this.deletedAt = deletedAt; }
}
