@echo off
echo ========================================
echo Building MolChemView JAR
echo ========================================

if not exist build mkdir build
if not exist classes mkdir classes

del /q classes\*.class 2>nul
del /q build\*.jar 2>nul

echo Compiling Java files...
javac -encoding UTF-8 -d classes src/Main.java src/MainMenu.java src/MyPanel.java src/Molecule.java src/MoleculeBuilder.java

if %errorlevel% neq 0 (
    echo Compilation failed!
    exit /b 1
)

echo Copying resources...
if exist src/background.jpg copy src\background.jpg classes\

echo Creating manifest...
echo Manifest-Version: 1.0 > manifest.txt
echo Main-Class: Main >> manifest.txt
echo. >> manifest.txt

echo Creating JAR file...
jar cvfm build/MolChemView.jar manifest.txt -C classes .

if %errorlevel% neq 0 (
    echo JAR creation failed!
    exit /b 1
)

echo Cleaning up...
del manifest.txt
rmdir /s /q classes

echo ========================================
echo Build complete!
echo JAR created: build/MolChemView.jar
echo ========================================
dir build\*.jar