import re
import json

with open(r'e:\Api Tester\desktop\src\main\java\com\apiforge\studio\controller\MainController.java', 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Extract Fields
# We can find the fields block which is from `public class MainController {` to the first method (`@FXML public void initialize() {` or `public void initialize() {`)
class_start = content.find('public class MainController {')
init_method = content.find('public void initialize() {')
if init_method == -1:
    init_method = content.find('void initialize()')

fields_str = content[class_start + len('public class MainController {'):init_method]

# Let's write fields_str to a file to look at it
with open('fields.txt', 'w', encoding='utf-8') as f:
    f.write(fields_str)

