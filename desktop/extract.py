
import re

with open(r'e:\Api Tester\desktop\src\main\resources\com\apiforge\studio\request_tab.fxml', encoding='utf-8') as f:
    data = f.read()

fields = set(re.findall(r'fx:id="([^"]+)"', data))
methods = set(re.findall(r'onAction="#([^"]+)"', data))

print('Fields:')
for field in sorted(fields):
    print(field)

print('\nMethods:')
for method in sorted(methods):
    print(method)

