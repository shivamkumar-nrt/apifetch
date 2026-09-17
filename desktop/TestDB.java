import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.io.File;

public class TestDB {
    public static void main(String[] args) throws Exception {
        Class.forName("org.sqlite.JDBC");
        String userHome = System.getProperty("user.home");
        String dbPath = userHome + File.separator + ".apiforge" + File.separator + "apiforge.db";
        String url = "jdbc:sqlite:" + dbPath;
        try (Connection conn = DriverManager.getConnection(url)) {
            String sql = "INSERT INTO saved_requests (workspace_id, collection_id, folder_id, name, protocol, method, url, auth_type, headers, body, auth_username, auth_password, auth_token, api_key_name, api_key_value, pre_request_script, test_script, body_type, form_data, request_settings) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                for (int i = 1; i <= 20; i++) {
                    pstmt.setString(i, "test");
                }
                pstmt.setInt(1, 1); // workspace_id
                pstmt.setInt(2, 1); // collection_id
                pstmt.setInt(3, 1); // folder_id
                pstmt.executeUpdate();
                System.out.println("Success!");
            } catch (Exception e) {
                System.out.println("Error on insert: " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
