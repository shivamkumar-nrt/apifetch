import re

with open('desktop/src/main/java/com/apiforge/studio/controller/MainController.java', 'r', encoding='utf-8') as f:
    content = f.read()

match = re.search(r'private void loadRequestIntoUI\(ApiRequest req\)\s*\{(.*?)\n    \}', content, re.DOTALL)
if match:
    print(match.group(0)[:500])
else:
    print("Not found")
