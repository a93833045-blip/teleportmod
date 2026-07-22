@echo off
chcp 65001 >nul
title Сборка TeleportMod
echo ============================================
echo    Сборка мода TeleportMod
echo ============================================
echo.

:: Проверка наличия Java
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [ОШИБКА] Java не найдена. Установите Java 21.
    pause
    exit /b 1
)

:: Проверка gradlew
if not exist gradlew.bat (
    echo [ОШИБКА] gradlew.bat не найден. Запустите в корневой папке проекта.
    pause
    exit /b 1
)

echo [1] Инициализация... 
call gradlew --version >nul
if %errorlevel% neq 0 (
    echo [ОШИБКА] Не удалось запустить gradle. Проверьте интернет-соединение.
    pause
    exit /b 1
)
echo [OK] Gradle готов.

echo [2] Очистка и сборка...
echo    Лог сборки будет сохранён в build_full.log
echo    Подождите, это может занять несколько минут...
echo.

:: Запуск сборки с полным выводом в лог (перезапись)
call gradlew clean build > build_full.log 2>&1

if %errorlevel% equ 0 (
    echo.
    echo ============================================
    echo    СБОРКА УСПЕШНО ЗАВЕРШЕНА!
    echo    Мод: teleportmod-%mod_version%.jar (если версия указана)
    echo    Итоговый файл в папке build/libs/
    echo ============================================
    echo.
    echo Результат сборки: SUCCESS > build_result.txt
    echo Дата: %date% %time% >> build_result.txt
) else (
    echo.
    echo ============================================
    echo    ОШИБКА СБОРКИ!
    echo    Смотрите подробности в файле build_full.log
    echo ============================================
    echo.
    echo Результат сборки: FAILED > build_result.txt
    echo Дата: %date% %time% >> build_result.txt
)

echo.
echo [3] ГОТОВО
pause