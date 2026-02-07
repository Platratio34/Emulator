@echo off
cd emulator
echo test
call mvn clean compile assembly:single
echo test2
cd ..\
del .vscode\extensions\emulatorAsm\emulator-1.0-SNAPSHOT-jar-with-dependencies.jar
copy emulator\target\emulator-1.0-SNAPSHOT-jar-with-dependencies.jar .vscode\extensions\emulatorAsm\.