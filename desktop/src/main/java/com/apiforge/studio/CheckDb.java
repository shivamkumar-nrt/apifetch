package com.apiforge.studio;
import java.sql.*;
import java.io.File;
public class CheckDb {
    public static void main(String[] args) throws Exception {
        String userHome = System.getProperty("user.home");
        File appDir = new File(userHome, ".apiforge");
        String url = "jdbc:sqlite:" + new File(appDir, "apiforge.db").getAbsolutePath();
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT count(*) FROM saved_requests;")) {
            while (rs.next()) {
                System.out.println("TOTAL REQ: " + rs.getInt(1));
            }
        }
    }
}
