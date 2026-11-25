@echo off
setlocal
cd /d "%~dp0..\.."
set "PRIV=%USERPROFILE%\.papeleria"
set "M2=%HOMEDRIVE%%HOMEPATH%\.m2\repository"
set "OUT=%TEMP%\papeleria-migrar"

if defined PAPELERIA_DB_PASSWORD goto clave
if exist "%PRIV%\bank-render.properties" goto clave
echo Falta la contrasena. Crea el archivo %PRIV%\bank-render.properties con una linea password=
goto fin

:clave
set "CP=%OUT%;%M2%\org\postgresql\postgresql\42.7.14\postgresql-42.7.14.jar;%M2%\com\zaxxer\HikariCP\6.2.1\HikariCP-6.2.1.jar;%M2%\org\slf4j\slf4j-api\2.0.16\slf4j-api-2.0.16.jar;%M2%\org\slf4j\slf4j-jdk14\2.0.16\slf4j-jdk14-2.0.16.jar;C:\glassfish8\glassfish\modules\*"
if not exist "%OUT%" mkdir "%OUT%"
javac -encoding UTF-8 -nowarn -d "%OUT%" -cp "%CP%" dao\*.java modelos\*.java extra\config\*.java extra\seguridad\*.java extra\validacion\*.java extra\herramientas\MigrarCifrado.java
if errorlevel 1 goto fin

if defined PAPELERIA_AES_KEY_BASE64 goto ensayo
if not exist "%PRIV%" mkdir "%PRIV%"
if not exist "%PRIV%\clave-aes.txt" java -cp "%CP%" MigrarCifrado --generar-clave > "%PRIV%\clave-aes.txt"
set /p PAPELERIA_AES_KEY_BASE64=<"%PRIV%\clave-aes.txt"
echo La clave de cifrado esta en %PRIV%\clave-aes.txt  - guardala, sin ella no se pueden leer los datos.

:ensayo
echo.
echo === ENSAYO (no guarda nada) ===
java -cp "%CP%" MigrarCifrado
if errorlevel 1 goto fin
echo.
set "RESP="
set /p RESP=Escribe APLICAR para modificar la base, o Enter para salir:
if not "%RESP%"=="APLICAR" goto fin
java -cp "%CP%" MigrarCifrado --aplicar
if errorlevel 1 goto fin
setx PAPELERIA_AES_KEY_BASE64 "%PAPELERIA_AES_KEY_BASE64%" >nul
echo Clave guardada tambien como variable de tu cuenta.

:fin
echo.
pause
