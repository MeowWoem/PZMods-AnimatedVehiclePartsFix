@echo off
setlocal enabledelayedexpansion

:: ============================================================
:: CONFIGURATION - adjust to your setup
:: ============================================================

:: Folder containing projectzomboid.jar and ZombieBuddy.jar
set "LIBS_DIR=C:\Program Files (x86)\Steam\steamapps\common\ProjectZomboid"

:: Folder containing your .java files (can have package subfolders)
set "SRC_DIR=src"

:: Output folder for compiled .class files
set "OUT_DIR=bin"

:: Name of the final .jar to generate (leave empty "" to skip jar creation)
set "JAR_NAME=../../Contents/mods/AnimatedVehiclePartsFix/42/media/java/AnimatedVehiclePartsFix.jar"

:: Optional: full path to a specific JDK's "bin" folder (e.g. a JDK 25 install).
:: Leave empty "" to just use whatever "javac"/"jar" are found in PATH.
:: Use this if you have multiple JDKs installed and PATH picks the wrong one.
set "JDK_BIN="

:: ============================================================
:: Do not edit below this line
:: ============================================================

set "PZ_JAR=%LIBS_DIR%\projectzomboid.jar"
set "ZB_JAR=%LIBS_DIR%\ZombieBuddy.jar"

if "%JDK_BIN%"=="" (
    set "JAVAC_EXE=javac"
    set "JAR_EXE=jar"
) else (
    set "JAVAC_EXE=%JDK_BIN%\javac.exe"
    set "JAR_EXE=%JDK_BIN%\jar.exe"
)

where !JAVAC_EXE! >nul 2>&1
if errorlevel 1 (
    echo [ERROR] javac not found ^(looked for: !JAVAC_EXE!^).
    echo Install a JDK ^(ideally the same version PZ itself uses^) and either:
    echo   - add its "bin" folder to your PATH, or
    echo   - set JDK_BIN at the top of this script to that "bin" folder.
    pause
    exit /b 1
)

echo Using javac:
!JAVAC_EXE! -version
echo.

if not exist "!PZ_JAR!" (
    echo [ERROR] Not found: !PZ_JAR!
    echo Check the LIBS_DIR variable at the top of this script.
    pause
    exit /b 1
)

if not exist "!ZB_JAR!" (
    echo [ERROR] Not found: !ZB_JAR!
    echo Check the LIBS_DIR variable at the top of this script.
    pause
    exit /b 1
)

if not exist "%SRC_DIR%" (
    echo [ERROR] Source folder not found: %SRC_DIR%
    pause
    exit /b 1
)

if exist "%OUT_DIR%" rmdir /s /q "%OUT_DIR%"
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

echo Looking for .java files in "%SRC_DIR%" ...
set "SOURCES_LIST=%TEMP%\pz_sources_%RANDOM%.txt"
dir /s /b "%SRC_DIR%\*.java" > "%SOURCES_LIST%" 2>nul

set "FILE_COUNT=0"
for /f %%A in ('type "%SOURCES_LIST%" ^| find /c /v ""') do set "FILE_COUNT=%%A"
echo %FILE_COUNT% source file^(s^) found.

if "%FILE_COUNT%"=="0" (
    echo No .java files found in "%SRC_DIR%".
    del "%SOURCES_LIST%" 2>nul
    pause
    exit /b 1
)

echo Compiling...
!JAVAC_EXE! -encoding UTF-8 -cp "!PZ_JAR!;!ZB_JAR!" -d "%OUT_DIR%" "@%SOURCES_LIST%"
set "BUILD_RESULT=%ERRORLEVEL%"

del "%SOURCES_LIST%" 2>nul

if not "%BUILD_RESULT%"=="0" (
    echo.
    echo [FAILED] Compilation failed ^(see errors above^).
    echo If you saw a "wrong version" / "cannot access" error, JDK_BIN above
    echo is probably pointing ^(or PATH resolves^) to the wrong JDK version.
    pause
    exit /b 1
)

echo.
echo [OK] Compilation succeeded. .class files generated in "%OUT_DIR%".

if "%JAR_NAME%"=="" goto :end

where !JAR_EXE! >nul 2>&1
if errorlevel 1 (
    echo.
    echo [ERROR] The "jar" tool was not found ^(looked for: !JAR_EXE!^).
    echo The .jar was not generated.
    pause
    exit /b 1
)

echo.
echo Creating "%JAR_NAME%" from "%OUT_DIR%" ...
pushd "%OUT_DIR%"
!JAR_EXE! --create --file "..\%JAR_NAME%" .
popd
if errorlevel 1 (
    echo [FAILED] Jar creation failed.
    pause
    exit /b 1
)

echo [OK] Jar generated: %JAR_NAME%

:end
pause