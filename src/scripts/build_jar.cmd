@echo off
echo ========================================
echo Building MolChemView JAR
echo ========================================

REM Создаем папку для сборки
if not exist build mkdir build
if not exist classes mkdir classes

REM Удаляем старые файлы
del /q classes\*.class 2>nul
del /q build\*.jar 2>nul

REM Компилируем все Java файлы
echo Compiling Java files...
javac -d classes -sourcepath . Main.java MainMenu.java MyPanel.java Molecule.java MoleculeBuilder.java

if %errorlevel% neq 0 (
    echo Compilation failed!
    exit /b 1
)

echo Compilation successful!

REM Создаем манифест с указанием главного класса
echo Manifest-Version: 1.0 > manifest.txt
echo Main-Class: Main >> manifest.txt
echo. >> manifest.txt

REM Создаем JAR файл
echo Creating JAR file...
jar cvfm build/MolChemView.jar manifest.txt -C classes .

if %errorlevel% neq 0 (
    echo JAR creation failed!
    exit /b 1
)

REM Удаляем временные файлы
del manifest.txt
rmdir /s /q classes

echo ========================================
echo Build complete!
echo JAR created: build/MolChemView.jar
echo ========================================