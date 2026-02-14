@echo off
cd .vscode\extensions\emulatorAsm\server
IF "%1"=="-c" (
    call npm run compile
    cd ..\client
    call npm run compile
)
cd ..\
del emulator-1.0-SNAPSHOT-jar-with-dependencies.jar
cd ..\..\..\emulator
call mvn clean compile assembly:single
cd ..\
copy emulator\target\emulator-1.0-SNAPSHOT-jar-with-dependencies.jar .vscode\extensions\emulatorAsm\.