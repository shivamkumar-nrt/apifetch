package com.apiforge.studio;
import javafx.scene.control.TextInputDialog;
public class TestNPE {
    public static void main(String[] args) {
        try {
            TextInputDialog t = new TextInputDialog(null);
            System.out.println("No NPE");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
