@echo off
rem Logs in to Keycloak and stores ONLY the access token in the TOKEN variable of this cmd window.
rem Usage:  token.cmd librarian1        (asks for the password)
rem Then:   curl -i -H "Authorization: Bearer %TOKEN%" http://localhost:8085/api/books

if "%~1"=="" (set /p "KC_USER=Username: ") else (set "KC_USER=%~1")
set /p "KC_PASS=Password for %KC_USER%: "

set "TOKEN="
for /f "delims=" %%i in ('curl -s -X POST http://localhost:8180/realms/community-library/protocol/openid-connect/token -d "grant_type=password" -d "client_id=community-library-app" --data-urlencode "username=%KC_USER%" --data-urlencode "password=%KC_PASS%" ^| powershell -NoProfile -Command "($input | ConvertFrom-Json).access_token"') do set "TOKEN=%%i"
set "KC_PASS="

if not defined TOKEN (
    echo [token] Login failed. Check the username and password, and that Keycloak is running on port 8180.
    exit /b 1
)
echo [token] Logged in as %KC_USER%. TOKEN is set in this window. It expires after a few minutes - run token.cmd again when you get 401.
