import java.sql.*;
public class CheckDb {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:sqlite:apiforge_studio.db";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, collection_id, folder_id FROM saved_requests;")) {
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + ", Name: " + rs.getString("name") + ", Col: " + rs.getInt("collection_id") + ", Fold: " + rs.getInt("folder_id"));
            }
        }
    }
}
