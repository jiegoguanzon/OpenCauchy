@echo off
setlocal

set USER_COM=
echo Please specify the communications port that LED Blinker 3000
echo is connected on. It will be updated to firmware v3001.
ezbl_comm.exe -enum
echo.
set /p USER_COM=Enter nothing to abort: 
if "%USER_COM%"=="" goto UserAbort
ezbl_comm.exe -com=%USER_COM% -baud=230400 -timeout=1100 -log="update_log.txt" -artifact="ex_app_led_blink.production.bl2"
goto End

:UserAbort
echo Firmware update aborted.

:End
pause
endlocal
@echo on