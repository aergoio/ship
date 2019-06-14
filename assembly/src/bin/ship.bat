@echo off
rem This is ship script for windows

for %%i in ("%~dp0..") do set "SHIP_HOME=%%~fi"
rem echo HOME = %SHIP_HOME%

set SHIP_LIB=%SHIP_HOME%\lib
set SHIP_CONF=%SHIP_HOME%\conf

rem Build java properties options
set _JVM_OPTIONS=-Dio.netty.tryReflectionSetAccessible=true -Dlogback.verbose=true -Dlogging.config="%SHIP_CONF%/ship-logger.xml" -Dlogback.configurationFile="%SHIP_CONF%/ship-logger.xml" -Dship.lib="%SHIP_LIB%" -Dship.conf="%SHIP_CONF%"

rem echo JVM_OPTIONS = %_JVM_OPTIONS%
set _MAIN_CLASS=ship.exec.LoaderLauncher

FOR /f "tokens=*" %%G IN ('dir /s/b "%SHIP_LIB%\bootstrap-*.jar"') DO java -classpath "%%G" %_JVM_OPTIONS% %_MAIN_CLASS% %*
