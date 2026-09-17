import re
with open(r'e:\Api Tester\desktop\src\main\java\com\apiforge\studio\controller\MainController.java', 'r', encoding='utf-8') as f:
    lines = f.readlines()

# We can find the fields and methods line ranges.
def find_block(start_regex):
    for i, line in enumerate(lines):
        if re.search(start_regex, line):
            print(f"Found {start_regex} at line {i+1}")

