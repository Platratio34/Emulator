#bash
cd .vscode/extensions/emulatorAsm/server
npm run compile; cd ..
cd client; npm run compile; cd ..
rm emulator-1.0-SNAPSHOT-jar-with-dependencies.jar
cd ../../../emulator
mvn clean compile assembly:single
cd ..
cp emulator/target/emulator-1.0-SNAPSHOT-jar-with-dependencies.jar .vscode/extensions/emulatorAsm/.