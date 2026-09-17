import com.apiforge.studio.service.DatabaseService;
import com.apiforge.studio.model.ApiRequest;

public class TestSave5 {
    public static void main(String[] args) {
        DatabaseService db = new DatabaseService();
        ApiRequest currentRequest = new ApiRequest();
        currentRequest.setMethod("GET");
        
        int id = db.saveRequest(5, 11, 35, "Test Request 5", currentRequest);
        System.out.println("Returned ID: " + id);
    }
}
