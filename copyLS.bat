@echo off
cd .vscode\extensions\emulatorAsm\server
call npm run compile
cd ..\client
call npm run comiple
cd ..\
del emulator-1.0-SNAPSHOT-jar-with-dependencies.jar
cd ..\..\..\emulator
call mvn clean compile assembly:single
cd ..\
copy emulator\target\emulator-1.0-SNAPSHOT-jar-with-dependencies.jar .vscode\extensions\emulatorAsm\.