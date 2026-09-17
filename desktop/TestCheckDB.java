import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestCheckDB {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:" + System.getProperty("user.home") + "/.apiforge/apiforge.db";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
             
            ResultSet ws = stmt.executeQuery("SELECT * FROM workspaces");
            while(ws.next()) System.out.println("Workspace: " + ws.getInt("id") + " - " + ws.getString("name"));
            
            ResultSet cols = stmt.executeQuery("SELECT * FROM collections");
            while(cols.next()) System.out.println("Collection: " + cols.getInt("id") + " (ws: " + cols.getInt("workspace_id") + ") - " + cols.getString("name"));
            
            ResultSet folds = stmt.executeQuery("SELECT * FROM folders");
            while(folds.next()) System.out.println("Folder: " + folds.getInt("id") + " (col: " + folds.getInt("collection_id") + ") - " + folds.getString("name"));
            
        } catch (Exception e) { e.printStackTrace(); }
    }
}
