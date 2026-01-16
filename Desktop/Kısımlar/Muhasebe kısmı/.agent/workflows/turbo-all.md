---
description: Tüm komutları otomatik onaylayan workflow
---

// turbo-all

# Otomatik Onay Workflow'u

Bu workflow aktif olduğunda, aşağıdaki tüm komutlar otomatik olarak çalıştırılır.

## Python İşlemleri
// turbo
1. Python dosyaları çalıştırma: `python script.py`
// turbo
2. Python modül çalıştırma: `python -m module_name`
// turbo
3. Pytest testleri: `pytest`

## Pip Paket Yönetimi
// turbo
4. Pip paket yükleme: `pip install package_name`
// turbo
5. Pip paket güncelleme: `pip install --upgrade package_name`
// turbo
6. Requirements yükleme: `pip install -r requirements.txt`
// turbo
7. Pip paket listesi: `pip list`
// turbo
8. Pip freeze: `pip freeze`

## Dosya/Klasör İşlemleri
// turbo
9. Klasör oluşturma: `mkdir folder_name`
// turbo
10. Dosya kopyalama: `copy source dest`
// turbo
11. Dosya taşıma: `move source dest`
// turbo
12. Klasör listeleme: `dir` veya `ls`
// turbo
13. Dosya içeriği görüntüleme: `type file` veya `cat file`
// turbo
14. Dosya silme: `del file` veya `rm file`
// turbo
15. Klasör silme: `rmdir folder` veya `rd folder`
// turbo
16. PowerShell silme: `Remove-Item path`
// turbo
17. Zorla silme: `del /f /q` veya `rm -rf`

## Git Komutları
// turbo
14. Git status: `git status`
// turbo
15. Git add: `git add .`
// turbo
16. Git commit: `git commit -m "message"`
// turbo
17. Git push: `git push`
// turbo
18. Git pull: `git pull`
// turbo
19. Git log: `git log`
// turbo
20. Git diff: `git diff`
// turbo
21. Git branch: `git branch`
// turbo
22. Git checkout: `git checkout branch_name`

## NPM/Node İşlemleri
// turbo
23. NPM install: `npm install`
// turbo
24. NPM run: `npm run script_name`
// turbo
25. NPM start: `npm start`
// turbo
26. NPX komutları: `npx command`

## Genel Komutlar
// turbo
27. Dizin değiştirme: `cd path`
// turbo
28. Ortam değişkenleri: `set` veya `echo`
// turbo
29. Dosya arama: `find` veya `where`
// turbo
30. Process listeleme: `tasklist`

> **Not:** Bu workflow, `// turbo-all` annotation'ı sayesinde yukarıdaki TÜM komutları otomatik onaylar.
