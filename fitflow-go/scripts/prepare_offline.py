#!/usr/bin/env python3
"""Build self-contained FitFlow Go HTML with early validation of assets."""
from pathlib import Path
import re
import shutil
import subprocess
import tempfile

root = Path(__file__).resolve().parents[2]
target = root / 'fitflow-go/app/src/main/assets'
html = (root / 'index.html').read_text(encoding='utf-8')

# The original UI and workout logic are kept; only web-only services are removed.
register = r"<script>if\('serviceWorker' in navigator\)\{window\.addEventListener\('load',\(\)=>navigator\.serviceWorker\.register\('\./sw\.js'[^<]*?</script>"
html, count = re.subn(register, '', html, flags=re.S)
assert count == 1, f'Unexpected SW registration count: {count}'
update_block = r"<script>\s*const FITFLOW_BUILD='v[^']+';[\s\S]*?</script>"
html, count = re.subn(update_block, '', html)
assert count == 1, f'Unexpected web update block count: {count}'
html, count = re.subn(r'<button id="checkUpdate"[^>]*>[\s\S]*?</button>', '', html)
assert count == 1, f'Unexpected web update button count: {count}'

html = html.replace('<title>FitFlow</title>', '<title>FitFlow Go</title>')
html = html.replace('<link rel="manifest" href="./manifest.webmanifest">', '')
html = html.replace('FitFlow · v2026.11.05.07', 'FitFlow Go · v2026.11.05.07')
# Internal local Android bridge: origin is the bundled asset-loader domain only.
old_export = "function exportData(){const b=new Blob([localStorage.getItem('rehabLogs')||'[]'],{type:'application/json'}),a=document.createElement('a');a.href=URL.createObjectURL(b);a.download='rehab-history.json';a.click();URL.revokeObjectURL(a.href)}"
new_export = "function exportData(){const json=localStorage.getItem('rehabLogs')||'[]';if(window.FitFlowGo){const bytes=new TextEncoder().encode(json);let text='';for(const b of bytes)text+=String.fromCharCode(b);window.FitFlowGo.saveHistory(btoa(text));return}const blob=new Blob([json],{type:'application/json'}),a=document.createElement('a');a.href=URL.createObjectURL(blob);a.download='rehab-history.json';a.click();setTimeout(()=>URL.revokeObjectURL(a.href),1000)}"
assert old_export in html, 'Export handler has changed, review needed'
html = html.replace(old_export, new_export)

# The website uses browser history for screen navigation, and Android Back can
# call the same navigation logic while retaining exercise timer pause behavior.
html = html.replace(
    "window.addEventListener('popstate',e=>{",
    "window.fitFlowGoBack=function(){const dialog=document.querySelector('dialog[open]');if(dialog){dialog.dispatchEvent(new Event('cancel',{cancelable:true}));if(dialog.open)dialog.close();return true}if(activeScreen!=='home'){history.back();return true}return false};window.addEventListener('popstate',e=>{",
)
assert 'window.fitFlowGoBack=function()' in html, 'Back bridge missing'
assert "return false};window.addEventListener('popstate'" in html, 'Back bridge must allow exiting home'

# No remote calls, service worker updates, or web-only manifest in APK.
assert 'navigator.serviceWorker' not in html
assert 'fetch(' not in html
assert 'manifest.webmanifest' not in html
assert '<script src=' not in html
assert 'http://' not in html and 'https://' not in html

# Check every exercise image and equipment image against the repository.
exercise = re.findall(r'\["[ASM]\d+","[^"]+","[^"]+","([^"]+\.png)"\]', html)
equipment_section = html.split('const EQUIPMENT={', 1)
assert len(equipment_section) == 2, 'Missing equipment definition'
equipment_body = equipment_section[1].split('};', 1)[0]
equipment = re.findall(r"image:'([^']+\.png)'", equipment_body)
assert len(exercise) >= 25, f'Only {len(exercise)} exercise assets identified'
assert len(equipment) == 5, f'Unexpected equipment count: {len(equipment)}'
required = {f'assets/{name}' for name in exercise}
required |= {f'assets/Equipment/{name}' for name in equipment}
required |= {
    'assets/Logo/FitFlow-Logo.png',
    'assets/Logo/FitFlow-Logo_icon.png',
    'assets/LACS-Banner.png',
    'assets/HATI-Banner.png',
    'assets/SARYU-Banner.png',
    'assets/UI/weightlifting.png',
}
missing = sorted(str(p) for p in required if not (root / p).is_file())
assert not missing, f'Missing offline images: {missing}'
assert len(set(exercise)) == len(exercise), 'Duplicate exercise image entries'
for file in required:
    assert (root / file).read_bytes()[:8] == b'\x89PNG\r\n\x1a\n', f'Invalid PNG: {file}'

# Validate all eight inline JS scripts before Gradle compiles Java.
script_blocks = re.findall(r'<script(?:\s[^>]*)?>([\s\S]*?)</script>', html)
assert len(script_blocks) >= 7, 'Unexpected script structure'
with tempfile.TemporaryDirectory(prefix='fitflow-go-js-') as folder:
    for number, code in enumerate(script_blocks, start=1):
        script_file = Path(folder) / f'inline-{number}.js'
        script_file.write_text(code, encoding='utf-8')
        subprocess.run(['node', '--check', str(script_file)], check=True)

# Android launcher icon uses the same graphic as the web app.
resource_dir = root / 'fitflow-go/app/src/main/res/drawable'
resource_dir.mkdir(parents=True, exist_ok=True)
shutil.copyfile(root / 'assets/Logo/FitFlow-Logo_icon.png', resource_dir / 'fitflow_go_icon.png')


target.mkdir(parents=True, exist_ok=True)
shutil.copytree(root / 'assets', target / 'assets', dirs_exist_ok=True)
(target / 'index.html').write_text(html, encoding='utf-8')
print(f'FitFlow Go bundle validated: {len(exercise)} exercise images, {len(equipment)} equipment images, {len(required)} required assets.')
