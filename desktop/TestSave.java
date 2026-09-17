import com.apiforge.studio.service.DatabaseService;
import com.apiforge.studio.model.ApiRequest;
import java.io.File;

public class TestSave {
    public static void main(String[] args) {
        DatabaseService db = new DatabaseService();
        ApiRequest req = new ApiRequest();
        req.setMethod("GET");
        req.setUrl("http://example.com");
        
        int id = db.saveRequest(1, 1, 1, "My Test Request", req);
        System.out.println("Returned ID: " + id);
    }
}
