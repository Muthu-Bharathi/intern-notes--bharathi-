@echo off
rem Starts the local Keycloak (identity provider) on port 8180 and imports the community-library realm
rem (roles, client, test users) the first time. Port 8080 is used by Apache on this machine.
rem Usage:  keycloak.cmd      Admin console: http://localhost:8180
setlocal
cd /d "%~dp0"

rem Every KEY=VALUE line of .env becomes an environment variable (lines starting with # are skipped)
if exist ".env" for /f "usebackq eol=# tokens=1,* delims==" %%A in (".env") do set "%%A=%%B"

if not defined KEYCLOAK_HOME set "KEYCLOAK_HOME=%USERPROFILE%\Downloads\keycloak-26.8.0"
rem .env uses forward slashes (Spring also reads that file and treats \ as an escape character)
set "KEYCLOAK_HOME=%KEYCLOAK_HOME:/=\%"
if not exist "%KEYCLOAK_HOME%\bin\kc.bat" (
    echo [keycloak] kc.bat not found in "%KEYCLOAK_HOME%". Set KEYCLOAK_HOME in .env to your Keycloak folder.
    exit /b 1
)

rem --import-realm reads realm files from data\import; a realm that already exists is left as it is
if not exist "%KEYCLOAK_HOME%\data\import" mkdir "%KEYCLOAK_HOME%\data\import"
copy /y "keycloak\community-library-realm.json" "%KEYCLOAK_HOME%\data\import\" >nul

rem Admin console login, created on first start
set "KC_BOOTSTRAP_ADMIN_USERNAME=%KEYCLOAK_ADMIN%"
set "KC_BOOTSTRAP_ADMIN_PASSWORD=%KEYCLOAK_ADMIN_PASSWORD%"

call "%KEYCLOAK_HOME%\bin\kc.bat" start-dev --http-port=8180 --import-realm %*
