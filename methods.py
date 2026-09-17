import re

with open('desktop/src/main/java/com/apiforge/studio/controller/MainController.java', 'r', encoding='utf-8') as f:
    content = f.read()

# Find method signatures
methods = re.findall(r'(?:public|private|protected)\s+(?:[\w\<\>\[\]]+\s+)+(\w+)\s*\([^)]*\)\s*(?:throws\s+[\w,\s]+)?\{', content)
print("Methods:", methods)
