import json
import re

with open(r'e:\Api Tester\desktop\src\main\java\com\apiforge\studio\controller\MainController.java', 'r', encoding='utf-8') as f:
    content = f.read()

pattern = re.compile(r'([\s\S]*?)(?:@FXML\s+)?(?:(?:public|private|protected)\s+)?(?:[\w<>\[\]]+\s+)+(\w+)\s*\([^)]*\)\s*(?:throws\s+[\w,\s]+)?\s*\{')

def extract_methods(src):
    res = {}
    idx = 0
    while True:
        match = pattern.search(src, idx)
        if not match:
            break
        brace_start = src.find('{', match.start(2))
        name = match.group(2)
        brace_count = 0
        in_string = False
        escape = False
        in_char = False
        in_comment = False
        in_multiline_comment = False
        
        end = -1
        for i in range(brace_start, len(src)):
            c = src[i]
            if in_multiline_comment:
                if c == '/' and src[i-1] == '*':
                    in_multiline_comment = False
                continue
            if in_comment:
                if c == '\n':
                    in_comment = False
                continue
                
            if not in_string and not in_char:
                if c == '/' and i+1 < len(src) and src[i+1] == '/':
                    in_comment = True
                    continue
                if c == '/' and i+1 < len(src) and src[i+1] == '*':
                    in_multiline_comment = True
                    continue
                    
            if not in_comment and not in_multiline_comment:
                if escape:
                    escape = False
                    continue
                if c == '\\':
                    escape = True
                    continue
                if c == '"' and not in_char:
                    in_string = not in_string
                elif c == "'" and not in_string:
                    in_char = not in_char
                    
                if not in_string and not in_char:
                    if c == '{':
                        brace_count += 1
                    elif c == '}':
                        brace_count -= 1
                        if brace_count == 0:
                            end = i + 1
                            break
        if end != -1:
            # find full signature
            sig_start = match.start(0)
            while sig_start < match.start(2) and src[sig_start].isspace():
                sig_start += 1
            # try to include @FXML if it's there
            idx_search = match.start(0)
            prev = src.rfind('@FXML', 0, match.start(2))
            if prev != -1 and prev > src.rfind('}', 0, match.start(2)):
                real_start = prev
            else:
                real_start = src.rfind('\n', 0, match.start(2))
                if real_start != -1: real_start += 1
                else: real_start = 0

            # actually let's just use regex match which includes some prefix, but it can include the previous method's closing brace.
            # let's just find the start by going backwards from the method name
            i = match.start(2)
            while i > 0 and src[i-1] != '}':
                i -= 1
            # now i is just after the previous '}'
            # let's find the first non-whitespace
            while i < match.start(2) and src[i].isspace():
                i += 1
            
            # include @FXML if present
            if '@FXML' in src[i:match.start(2)]:
                pass # i is correct
            
            res[name] = src[i:end]
            idx = end
        else:
            break
    return res

methods = extract_methods(content)
with open('methods.json', 'w') as f:
    json.dump(methods, f)
