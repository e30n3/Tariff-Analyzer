@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

set "DIR=%~dp0"
set "OUT=%DIR%diagnostics_output.txt"

echo ============================================= > "%OUT%"
echo Tariff Analyzer - Advanced Diagnostics        >> "%OUT%"
echo Date/time: %DATE% %TIME%                      >> "%OUT%"
echo User: %USERNAME%                                >> "%OUT%"
echo Computer: %COMPUTERNAME%                        >> "%OUT%"
echo OS: %OS%                                         >> "%OUT%"
echo Working dir: %DIR%                               >> "%OUT%"
echo ============================================= >> "%OUT%"

:: Поиск EXE (в текущей папке или в подпапке приложения)
set "EXE="
if exist "%DIR%Tariff Analyzer.exe" set "EXE=%DIR%Tariff Analyzer.exe"
if not defined EXE if exist "%DIR%Tariff Analyzer\Tariff Analyzer.exe" set "EXE=%DIR%Tariff Analyzer\Tariff Analyzer.exe"

if not defined EXE (
    echo [ERROR] Tariff Analyzer.exe not found! >> "%OUT%"
    echo ОШИБКА: Исполняемый файл приложения не найден в %DIR%
    echo Проверьте, что вы полностью распаковали архив.
    echo.
    echo Содержимое папки: >> "%OUT%"
    dir /b /s >> "%OUT%"
    pause
    exit /b 1
)

echo Found EXE: %EXE% >> "%OUT%"
echo Попытка запуска приложения...
echo Вывод будет дублироваться в diagnostics_output.txt

:: Запуск и захват вывода
"%EXE%" >> "%OUT%" 2>&1
set "EXITCODE=%ERRORLEVEL%"

if %EXITCODE% NEQ 0 (
    echo. >> "%OUT%"
    echo [INFO] Normal launch failed (code !EXITCODE!). Trying Software Rendering... >> "%OUT%"
    echo Обычный запуск не удался (код !EXITCODE!). Пробуем режим совместимости (Software Rendering)...

    :: Попытка запуска с отключенным GPU (частая проблема в банках/VDI)
    set "skiko.renderApi=SOFTWARE"
    "%EXE%" >> "%OUT%" 2>&1
    set "EXITCODE=!ERRORLEVEL!"
)

echo.
echo Работа завершена. Код выхода: %EXITCODE%
echo Код выхода записан в %OUT%
echo.
echo Если окно приложения так и не появилось, отправьте файл diagnostics_output.txt разработчику.
pause
