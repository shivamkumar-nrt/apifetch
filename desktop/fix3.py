import re

with open(r'e:\Api Tester\desktop\src\main\java\com\apiforge\studio\controller\MainController.java', 'r', encoding='utf-8') as f:
    mc = f.read()

with open(r'e:\Api Tester\desktop\src\main\java\com\apiforge\studio\controller\RequestTabController.java', 'r', encoding='utf-8') as f:
    rtc = f.read()

# Add showMockShare back to MainController
show_mock_share = """
    public void showMockShare(String type) {
        String mockLink = "apiforge://" + type.toLowerCase() + "/" + java.util.UUID.randomUUID().toString().substring(0, 8);
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(mockLink);
        clipboard.setContent(content);
        showInfo("Link Copied", type + " share link copied to clipboard:\\n" + mockLink);
    }
"""
mc = mc.rstrip()
if mc.endswith('}'):
    mc = mc[:-1] + show_mock_share + "\n}\n"
    
# Remove showMockShare from RequestTabController
rtc = re.sub(r'(?:public|private|protected)\s+void\s+showMockShare\([^)]*\)\s*\{[\s\S]*?(?=\s*(?:public|private|protected)\s+|$)', '', rtc)

# Fix historyList in RequestTabController
rtc = re.sub(r'mainController\.getStorageService\(\)\.saveHistory\(historyList\);', '', rtc)

# Fix getBackendUrl() in RequestTabController
rtc = rtc.replace('getBackendUrl()', 'mainController.getBackendUrl()')

with open(r'e:\Api Tester\desktop\src\main\java\com\apiforge\studio\controller\MainController.java', 'w', encoding='utf-8') as f:
    f.write(mc)

with open(r'e:\Api Tester\desktop\src\main\java\com\apiforge\studio\controller\RequestTabController.java', 'w', encoding='utf-8') as f:
    f.write(rtc)
