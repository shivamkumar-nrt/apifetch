import com.apiforge.studio.service.DatabaseService;
import com.apiforge.studio.model.ApiRequest;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TestSave3 {
    public static void main(String[] args) {
        DatabaseService db = new DatabaseService();
        ApiRequest currentRequest = new ApiRequest();
        
        currentRequest.setUrl("http://example.com");
        currentRequest.setMethod("GET");
        currentRequest.setProtocol("REST");
        currentRequest.setBody("");
        currentRequest.setPreRequestScript("");
        currentRequest.setTestScript("");
        currentRequest.setBodyType("none");
        
        Map<String, String> fData = new HashMap<>();
        currentRequest.setFormData(fData);
        
        Map<String, Boolean> reqSettings = new HashMap<>();
        reqSettings.put("followRedirects", true);
        currentRequest.setRequestSettings(reqSettings);
        
        Map<String, String> authConfig = new HashMap<>();
        authConfig.put("username", "");
        authConfig.put("password", "");
        authConfig.put("token", "");
        authConfig.put("key", "");
        authConfig.put("value", "");
        currentRequest.setAuthConfig(authConfig);
        
        int id = db.saveRequest(1, 1, 1, "Test Request 3", currentRequest);
        System.out.println("Returned ID: " + id);
    }
}
