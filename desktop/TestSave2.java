import com.apiforge.studio.service.DatabaseService;
import com.apiforge.studio.model.ApiRequest;
import com.apiforge.studio.model.RequestSettings;
import java.io.File;

public class TestSave2 {
    public static void main(String[] args) {
        DatabaseService db = new DatabaseService();
        ApiRequest req = new ApiRequest();
        req.setMethod("GET");
        req.setUrl("http://example.com");
        // Ensure request settings are not null or simulate UI state
        req.setRequestSettings(new RequestSettings());
        
        int id = db.saveRequest(1, 1, 1, "Test Request 2", req);
        System.out.println("Returned ID: " + id);
    }
}
