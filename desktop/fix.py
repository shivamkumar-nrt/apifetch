import re

with open(r'e:\Api Tester\desktop\src\main\java\com\apiforge\studio\controller\MainController.java', 'r', encoding='utf-8') as f:
    mc = f.read()

with open(r'e:\Api Tester\desktop\src\main\java\com\apiforge\studio\controller\RequestTabController.java', 'r', encoding='utf-8') as f:
    rtc = f.read()

# Make methods public in MainController
for m in ['void showError', 'void showInfo', 'void promptSaveRequestToCollection', 'void postHistoryToBackend', 'String getBackendUrl', 'void addHistoryEntry']:
    mc = re.sub(r'(?:private|protected)\s+' + m, 'public ' + m, mc)

# showMockShare uses lastResponseBody and responseWebView, which are in tab. Let's move it to RequestTabController.
# I will extract showMockShare from MainController and add it to RequestTabController.
match = re.search(r'([\s\S]*?)((?:@FXML\s+)?(?:public|private|protected)\s+void\s+showMockShare\([^)]*\)\s*\{)', mc)
if match:
    # use our brace matcher to extract
    brace_start = mc.find('{', match.start(2))
    brace_count = 0
    end = -1
    for i in range(brace_start, len(mc)):
        if mc[i] == '{': brace_count += 1
        elif mc[i] == '}':
            brace_count -= 1
            if brace_count == 0:
                end = i + 1
                break
    showMockShare_code = mc[match.start(2):end]
    mc = mc[:match.start(2)] + mc[end:]
    rtc = rtc.rstrip()
    if rtc.endswith('}'):
        rtc = rtc[:-1]
    
    # fix variables in showMockShare_code
    showMockShare_code = showMockShare_code.replace('showInfo(', 'mainController.showInfo(')
    showMockShare_code = showMockShare_code.replace('showError(', 'mainController.showError(')
    # wait, showMockShare also needs updateRequestFromUI(), which is in RequestTabController. 
    showMockShare_code = showMockShare_code.replace('updateRequestFromUI', 'this.updateRequestFromUI')
    rtc += "\n" + showMockShare_code + "\n}\n"

# In RequestTabController, fix `mainController.getTheme().getValue()` -> `mainController.getTheme()`
rtc = rtc.replace('mainController.getTheme().getValue()', 'mainController.getTheme()')
rtc = rtc.replace('mainController.getTheme()()', 'mainController.getTheme()')

# In RequestTabController, fix `mainController.getEnvironmentComboBox().getValue()`
rtc = rtc.replace('mainController.getEnvironmentComboBox()', 'mainController.getEnvironmentComboBox()')

# loadExampleIntoUI needs to be public in RequestTabController
rtc = re.sub(r'private\s+void\s+loadExampleIntoUI', 'public void loadExampleIntoUI', rtc)

with open(r'e:\Api Tester\desktop\src\main\java\com\apiforge\studio\controller\MainController.java', 'w', encoding='utf-8') as f:
    f.write(mc)

with open(r'e:\Api Tester\desktop\src\main\java\com\apiforge\studio\controller\RequestTabController.java', 'w', encoding='utf-8') as f:
    f.write(rtc)
