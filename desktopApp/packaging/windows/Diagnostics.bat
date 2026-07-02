@echo off
chcp 65001 >nul
setlocal

set "DIR=%~dp0"
set "EXE=%DIR%Tariff Analyzer.exe"
set "OUT=%DIR%diagnostics_output.txt"

echo ============================================= > "%OUT%"
echo Tariff Analyzer - diagnostics run              >> "%OUT%"
echo Date/time: %DATE% %TIME%                       >> "%OUT%"
echo User: %USERNAME%                                >> "%OUT%"
echo Computer: %COMPUTERNAME%                        >> "%OUT%"
echo OS: %OS%                                         >> "%OUT%"
echo Working dir: %DIR%                               >> "%OUT%"
echo ============================================= >> "%OUT%"

if not exist "%EXE%" (
    echo [ERROR] Not found: %EXE% >> "%OUT%"
    echo File not found: %EXE%
    echo See %OUT%
    pause
    exit /b 1
)

echo Launching: %EXE% >> "%OUT%"
echo. >> "%OUT%"

"%EXE%" >> "%OUT%" 2>&1
set "EXITCODE=%ERRORLEVEL%"

echo. >> "%OUT%"
echo Exit code: %EXITCODE% >> "%OUT%"

echo.
echo Application exited with code %EXITCODE%.
echo Full output saved to: %OUT%
echo.
echo Please send this file, together with the log folder
echo %LOCALAPPDATA%\TariffAnalyzer\logs
echo to the developer.
echo.
pause
