@echo off
rem Loads the credentials in .env as environment variables for this cmd session, then starts the app.
rem Usage:  run.cmd
rem Keycloak must be running first:  docker compose up -d
setlocal
cd /d "%~dp0"

if not exist ".env" (
    echo [run] .env not found. Copy .env.example to .env and fill in the values.
    exit /b 1
)

rem Every KEY=VALUE line becomes an environment variable (lines starting with # are skipped)
for /f "usebackq eol=# tokens=1,* delims==" %%A in (".env") do set "%%A=%%B"

if not defined DB_PASSWORD (
    echo [run] DB_PASSWORD is not set. Add it to .env or set it as a Windows environment variable.
    exit /b 1
)

call "%~dp0mvnw.cmd" spring-boot:run %*
