import * as path from 'path';
import { workspace, ExtensionContext } from 'vscode';

import {
	LanguageClient,
	LanguageClientOptions,
	ServerOptions,
	TransportKind
} from 'vscode-languageclient/node';

let client: LanguageClient;
let elClient: LanguageClient;

export function activate(context: ExtensionContext) {
	// The server is implemented in node
	const serverModule = context.asAbsolutePath(
		path.join('server', 'out', 'server.js')
    );

	// If the extension is launched in debug mode then the debug server options are used
	// Otherwise the run options are used
	const serverOptions: ServerOptions = {
        run: { module: serverModule, transport: TransportKind.ipc },
		debug: {
			module: serverModule,
			transport: TransportKind.ipc,
        }
	};

	// Options to control the language client
	const clientOptions: LanguageClientOptions = {
		// Register the server for plain text documents
		documentSelector: [{ scheme: 'file', language: 'emulatorasm' }],
		synchronize: {
			// Notify the server about file changes to '.clientrc files contained in the workspace
			fileEvents: workspace.createFileSystemWatcher('**/*.asm')
		}
	};

	// Create the language client and start the client.
	client = new LanguageClient(
		'emulator-asm-client',
		'Emulator Assembly Language Client',
		serverOptions,
		clientOptions
	);

	// Start the client. This will also launch the server
    client.start();
    
    const elServerExecutable = context.asAbsolutePath('emulator-1.0-SNAPSHOT-jar-with-dependencies.jar');
    const elServerOptions: ServerOptions = {
        command: `java`,
        args: ["-jar", elServerExecutable, '-lsp'],
        transport: TransportKind.stdio,
    }
    const elClientOptions: LanguageClientOptions = {
		// Register the server for plain text documents
		documentSelector: [{ scheme: 'file', language: 'emulatorlang' }],
		synchronize: {
			// Notify the server about file changes to '.clientrc files contained in the workspace
            fileEvents: workspace.createFileSystemWatcher('**/*.el'),
		}
    };
    elClient = new LanguageClient(
		'emulator-el-client',
		'EmulatorLang Language Client',
		elServerOptions,
		elClientOptions
	);
	// Start the client. This will also launch the server
    elClient.start();

}

export function deactivate(): Thenable<void> | undefined {
	if (!client) {
		return undefined;
    }
    elClient.stop();
	return client.stop();
}