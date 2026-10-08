#bash
if [ " $@ " == " -c " ]; then
    cd .vscode/extensions/emulatorAsm/server
    npm run compile; cd ..
    cd client; npm run compile; cd ..
    cd ../../../emulator
else
    cd emulator
fi
mvn clean compile assembly:single
cd ..
cp emulator/target/emulator-1.0-SNAPSHOT-jar-with-dependencies.jar .vscode/extensions/emulatorAsm/emulator-1.0-SNAPSHOT-jar-with-dependencies.jar.new