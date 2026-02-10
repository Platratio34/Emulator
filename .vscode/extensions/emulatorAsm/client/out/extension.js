"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.activate = activate;
exports.deactivate = deactivate;
const path = require("path");
const vscode_1 = require("vscode");
const node_1 = require("vscode-languageclient/node");
let client;
let elClient;
function activate(context) {
    // The server is implemented in node
    const serverModule = context.asAbsolutePath(path.join('server', 'out', 'server.js'));
    // If the extension is launched in debug mode then the debug server options are used
    // Otherwise the run options are used
    const serverOptions = {
        run: { module: serverModule, transport: node_1.TransportKind.ipc },
        debug: {
            module: serverModule,
            transport: node_1.TransportKind.ipc,
        }
    };
    // Options to control the language client
    const clientOptions = {
        // Register the server for plain text documents
        documentSelector: [{ scheme: 'file', language: 'emulatorasm' }],
        synchronize: {
            // Notify the server about file changes to '.clientrc files contained in the workspace
            fileEvents: vscode_1.workspace.createFileSystemWatcher('**/*.asm')
        }
    };
    // Create the language client and start the client.
    client = new node_1.LanguageClient('emulator-asm-client', 'Emulator Assembly Language Client', serverOptions, clientOptions);
    // Start the client. This will also launch the server
    client.start();
    const elServerExecutable = context.asAbsolutePath('emulator-1.0-SNAPSHOT-jar-with-dependencies.jar');
    const elServerOptions = {
        command: `java`,
        args: ["-jar", elServerExecutable, '-lsp'],
        transport: node_1.TransportKind.stdio,
    };
    const elClientOptions = {
        // Register the server for plain text documents
        documentSelector: [{ scheme: 'file', language: 'emulatorlang' }],
        synchronize: {
            // Notify the server about file changes to '.clientrc files contained in the workspace
            fileEvents: vscode_1.workspace.createFileSystemWatcher('**/*.el'),
        }
    };
    elClient = new node_1.LanguageClient('emulator-el-client', 'EmulatorLang Language Client', elServerOptions, elClientOptions);
    // Start the client. This will also launch the server
    elClient.start();
}
function deactivate() {
    if (!client) {
        return undefined;
    }
    elClient.stop();
    return client.stop();
}
//# sourceMappingURL=extension.js.map