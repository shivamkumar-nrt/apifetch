import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.io.File;

public class TestDBColumns {
    public static void main(String[] args) {
        String dbPath = System.getProperty("user.home") + "/.apiforge/apiforge.db";
        String url = "jdbc:sqlite:" + dbPath;
        try (Connection conn = DriverManager.getConnection(url)) {
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT * FROM saved_requests LIMIT 1");
                ResultSetMetaData rsmd = rs.getMetaData();
                System.out.println("Columns in saved_requests:");
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    System.out.println(rsmd.getColumnName(i) + " - " + rsmd.getColumnTypeName(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
