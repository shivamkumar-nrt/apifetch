import re

with open(r'e:\Api Tester\desktop\src\main\java\com\apiforge\studio\controller\MainController.java', 'r', encoding='utf-8') as f:
    mc = f.read()

with open(r'e:\Api Tester\desktop\src\main\java\com\apiforge\studio\controller\RequestTabController.java', 'r', encoding='utf-8') as f:
    rtc = f.read()

# MainController additions
mc = mc.replace("public void initialize() {", """public RequestTabController getActiveTabController() {
        Tab selected = mainTabPane.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getUserData() instanceof RequestTabController) {
            return (RequestTabController) selected.getUserData();
        }
        return null;
    }

    @FXML
    public void initialize() {""")

mc = mc.replace('String body = lastResponseBody != null ? lastResponseBody : "{}";', 'RequestTabController tc = getActiveTabController();\n        String body = (tc != null && tc.getLastResponseBody() != null) ? tc.getLastResponseBody() : "{}";')
mc = mc.replace('int status = lastResponseStatus != 0 ? lastResponseStatus : 200;', 'int status = (tc != null && tc.getLastResponseStatus() != 0) ? tc.getLastResponseStatus() : 200;')

mc = mc.replace('updateRequestFromUI();', 'RequestTabController tc_save = getActiveTabController();\n        if (tc_save != null) tc_save.updateRequestFromUI();\n        ApiRequest currentRequest = tc_save != null ? tc_save.getCurrentRequest() : new ApiRequest();')

# onThemeChanged in MainController
# Replace the part that updates the webviews with a call to tabs
theme_replacement = """String theme = themeComboBox.getValue();
        boolean isLight = "Light Mode".equals(theme);
        if (!isLight) {
            if (!rootContainer.getStyleClass().contains("dark-theme")) {
                rootContainer.getStyleClass().add("dark-theme");
            }
        } else {
            rootContainer.getStyleClass().remove("dark-theme");
        }
        
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getUserData() instanceof RequestTabController) {
                ((RequestTabController) tab.getUserData()).applyTheme(isLight);
            }
        }
"""
mc = re.sub(r'String theme = themeComboBox\.getValue\(\);[\s\S]*?(?=\s*\}\s*(?:@FXML|public|private))', theme_replacement, mc)


# RequestTabController additions
rtc_methods = """
    public String getLastResponseBody() { return lastResponseBody; }
    public int getLastResponseStatus() { return lastResponseStatus; }
    public ApiRequest getCurrentRequest() { return currentRequest; }
    
    public void applyTheme(boolean isLight) {
        if (lastResponseBody != null) {
            responseWebView.getEngine().loadContent(mainController.generateRichVisualizerHtml(lastResponseBody, isLight));
            responsePrettyWebView.getEngine().loadContent(mainController.generateHighlightedJsonHtml(mainController.formatJson(lastResponseBody), isLight));
        } else {
            String bg = isLight ? "#ffffff" : "#1e1e1e";
            String html = "<html><body style='background-color:" + bg + "; margin:0;'></body></html>";
            responseWebView.getEngine().loadContent(html);
            if (responsePrettyWebView != null) {
                responsePrettyWebView.getEngine().loadContent(html);
            }
        }
    }
"""

rtc = rtc.replace("public class RequestTabController {", "public class RequestTabController {" + rtc_methods)

# Wait, generateRichVisualizerHtml and generateHighlightedJsonHtml and formatJson were moved to RequestTabController!
# So they are NOT in mainController.
rtc = rtc.replace("mainController.generateRichVisualizerHtml", "generateRichVisualizerHtml")
rtc = rtc.replace("mainController.generateHighlightedJsonHtml", "generateHighlightedJsonHtml")
rtc = rtc.replace("mainController.formatJson", "formatJson")

# Also, updateRequestFromUI is in RequestTabController, we just need to make it public.
rtc = rtc.replace("private void updateRequestFromUI()", "public void updateRequestFromUI()")

# Let's save back
with open(r'e:\Api Tester\desktop\src\main\java\com\apiforge\studio\controller\MainController.java', 'w', encoding='utf-8') as f:
    f.write(mc)

with open(r'e:\Api Tester\desktop\src\main\java\com\apiforge\studio\controller\RequestTabController.java', 'w', encoding='utf-8') as f:
    f.write(rtc)
