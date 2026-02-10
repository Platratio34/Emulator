"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const node_1 = require("vscode-languageserver/node");
const vscode_languageserver_textdocument_1 = require("vscode-languageserver-textdocument");
// Create a connection for the server, using Node's IPC as a transport.
// Also include all preview / proposed LSP features.
const connection = (0, node_1.createConnection)(node_1.ProposedFeatures.all);
// Create a simple text document manager.
const documents = new node_1.TextDocuments(vscode_languageserver_textdocument_1.TextDocument);
let hasConfigurationCapability = false;
let hasWorkspaceFolderCapability = false;
let hasDiagnosticRelatedInformationCapability = false;
connection.onInitialize((params) => {
    const capabilities = params.capabilities;
    // Does the client support the `workspace/configuration` request?
    // If not, we fall back using global settings.
    hasConfigurationCapability = !!(capabilities.workspace && !!capabilities.workspace.configuration);
    hasWorkspaceFolderCapability = !!(capabilities.workspace && !!capabilities.workspace.workspaceFolders);
    hasDiagnosticRelatedInformationCapability = !!(capabilities.textDocument &&
        capabilities.textDocument.publishDiagnostics &&
        capabilities.textDocument.publishDiagnostics.relatedInformation);
    const result = {
        capabilities: {
            textDocumentSync: node_1.TextDocumentSyncKind.Incremental,
            // Tell the client that this server supports code completion.
            completionProvider: {
                resolveProvider: true
            },
            diagnosticProvider: {
                interFileDependencies: false,
                workspaceDiagnostics: false
            },
            hoverProvider: true
        }
    };
    if (hasWorkspaceFolderCapability) {
        result.capabilities.workspace = {
            workspaceFolders: {
                supported: true
            }
        };
    }
    return result;
});
connection.onInitialized(() => {
    if (hasConfigurationCapability) {
        // Register for all configuration changes.
        connection.client.register(node_1.DidChangeConfigurationNotification.type, undefined);
    }
    if (hasWorkspaceFolderCapability) {
        connection.workspace.onDidChangeWorkspaceFolders(_event => {
            connection.console.log('Workspace folder change event received.');
        });
    }
});
// The global settings, used when the `workspace/configuration` request is not supported by the client.
// Please note that this is not the case when using this server with the client provided in this example
// but could happen with other clients.
const defaultSettings = { maxNumberOfProblems: 1000 };
let globalSettings = defaultSettings;
// Cache the settings of all open documents
const documentSettings = new Map();
connection.onDidChangeConfiguration(change => {
    if (hasConfigurationCapability) {
        // Reset all cached document settings
        documentSettings.clear();
    }
    else {
        globalSettings = ((change.settings.emulatorAsmServer || defaultSettings));
    }
    // Refresh the diagnostics since the `maxNumberOfProblems` could have changed.
    // We could optimize things here and re-fetch the setting first can compare it
    // to the existing setting, but this is out of scope for this example.
    connection.languages.diagnostics.refresh();
});
function getDocumentSettings(resource) {
    if (!hasConfigurationCapability) {
        return Promise.resolve(globalSettings);
    }
    let result = documentSettings.get(resource);
    if (!result) {
        result = connection.workspace.getConfiguration({
            scopeUri: resource,
            section: 'emulatorAsmServer'
        });
        documentSettings.set(resource, result);
    }
    return result;
}
// Only keep settings for open documents
documents.onDidClose(e => {
    documentSettings.delete(e.document.uri);
});
connection.languages.diagnostics.on(async (params) => {
    const document = documents.get(params.textDocument.uri);
    if (document !== undefined) {
        return {
            kind: node_1.DocumentDiagnosticReportKind.Full,
            items: await validateTextDocument(document)
        };
    }
    else {
        // We don't know the document. We can either try to read it from disk
        // or we don't report problems for it.
        return {
            kind: node_1.DocumentDiagnosticReportKind.Full,
            items: []
        };
    }
});
// The content of a text document has changed. This event is emitted
// when the text document first opened or when its content has changed.
documents.onDidChangeContent(change => {
    validateTextDocument(change.document);
});
const DIAGNOSTICS_SOURCE = "emulator-asm";
async function validateTextDocument(textDocument) {
    // In this simple example we get the settings for every validate run.
    const settings = await getDocumentSettings(textDocument.uri);
    // The validator creates diagnostics for all uppercase words length 2 and more
    const text = textDocument.getText();
    let m;
    let problems = 0;
    const diagnostics = [];
    const lines = text.split('\n');
    const addDiag = (severity, start, end, message) => {
        const diagnostic = {
            severity: severity,
            range: {
                start: textDocument.positionAt(start),
                end: textDocument.positionAt(end)
            },
            message: message,
            source: DIAGNOSTICS_SOURCE
        };
        diagnostics.push(diagnostic);
    };
    const addError = (start, end, message) => {
        addDiag(node_1.DiagnosticSeverity.Error, start, end, message);
        problems++;
    };
    const addWarning = (start, end, message) => {
        addDiag(node_1.DiagnosticSeverity.Warning, start, end, message);
        problems++;
    };
    const addInfo = (start, end, message) => {
        addDiag(node_1.DiagnosticSeverity.Information, start, end, message);
    };
    const addHint = (start, end, message) => {
        addDiag(node_1.DiagnosticSeverity.Hint, start, end, message);
    };
    let numLines = 0;
    let nextCPos = 0;
    for (const lineN in lines) {
        const _line = lines[lineN];
        const cPos = nextCPos;
        nextCPos = cPos + _line.length + 1;
        // addInfo(cPos, _line.length, "Test")
    }
    // for (const lineN in lines) {
    //     const _line = lines[lineN];
    //     const cPos = nextCPos;
    //     nextCPos = cPos + _line.length + 1;
    //     const line = _line.trimEnd();
    //     const eol = cPos + line.length;
    //     const m = linePattern.exec(line)
    // 	numLines++;
    //     if (!m) {
    //         addError(cPos, eol, `Failure parsing line ${numLines}: "${line}" (${line.charCodeAt(0)})`);
    //         continue;
    //     }
    //     if (numLines > 1024) {
    //         addWarning(cPos, eol + m[0].length, `Diagnostics disabled beyond this point: Too many lines`)
    // 		break;
    //     }
    // 	if(m[1]) { // File option
    // 		const optLine = m[1]
    // 		const m2 = fileOptionPattern.exec(optLine);
    //         if (m2 == null) {
    //             addError(cPos, eol, `Malformed file option: "${line}"`)
    // 			continue;
    // 		}
    //         if (m2[5] != ';') {
    //             addError(cPos + m.index + m[0].length, cPos + m.index + m[0].length + 1, `Malformed line: missing ';'`)
    // 			continue;
    //         }
    //         const optionName = m2[1]
    //         const optionDef = fileOptions[optionName]
    //         if (!optionDef) {
    //             addError(cPos, eol, `Invalid file option: ${m2[1]}`)
    //             continue;
    //         }
    //         if (definedFileOptions[optionName]) {
    //             if (optionDef.multiple != true) {
    //                 addError(cPos, eol, `Duplicate option definition`)
    //             }
    //         }
    //         definedFileOptions[optionName] = true
    //         if (optionDef.str) {
    //             if (!(m2[3] || m2[4])) {
    //                 addError(cPos, eol, `File option '${optionName}' must be a string; Was '${m2[2]}'`)
    //                 continue;
    //             }
    //         } else if (optionDef.flags) {
    //             if (!m2[2]) {
    //                 addError(cPos, eol, `File option '${optionName}' must be an option set; Was '${m2[3] ?? m2[4]}'`)
    //             }
    //         } else {
    //             if (m2[2] || m2[3] || m2[4]) {
    //                 addError(cPos, eol, `File option '${optionName}' can't have a value; Had '${m2[2] ?? m2[3] ?? m2[4]}'`)
    //             }
    //         }
    //         continue;
    // 	} else if(m[2]) { // Token line
    // 		const tokenLine = m[2]
    // 		const m2 = tokenPattern.exec(tokenLine);
    //         if (m2 == null) {
    //             addError(cPos, eol, `Bad token line`)
    // 			continue;
    // 		}
    //         const typeName = m2[1]
    //         const tokenDef = tokenTypes[typeName]
    //         if (!tokenDef) {
    //             addError(cPos, cPos + m[1].length, `Invalid Token Type`)
    // 			continue;
    //         }
    //         const tokenE = m2[2]
    //         const tokenT = m2[3]
    // 		const options = m2[4]
    //         if (!m2.indices) {
    //             addWarning(cPos, eol, `Unable to debug line: no indices`)
    //             connection.console.error(`Tried to debug line but was missing indices`)
    // 			continue;
    // 		}
    // 		const optionStart = cPos + m2.indices[4][0];
    // 		let m3;
    // 		let semicolon = false;
    //         let numFlags = 0
    //         const flags: {[name: string]: TokenFlagExistence} = {}
    // 		while((m3 = tokenFlagPattern.exec(options))) {
    //             if (!m3.indices) {
    //                 connection.console.error(`Error getting indices for regex`)
    // 				continue;
    //             }
    //             const flagStart = optionStart + m3.indices[1][0];
    //             const flagEnd = optionStart + m3.indices[1][1];
    // 			if(semicolon) break;
    // 			if(m3[4] == ';') {
    // 				semicolon = true;
    //             }
    //             if (!m3[2]) {
    //                 addError(flagStart, flagEnd, `Invalid flag definition: "${m3[1]}"`)
    //                 break;
    //             }
    // 			numFlags++;
    //             if (numFlags > 16) {
    //                 addWarning(cPos, eol, `Too many flags, diagnostics disabled on this line: ${options}`)
    // 				break;
    // 			}
    //             const flagName = m3[2]
    //             if (!flagName) {
    //                 addError(flagStart, flagEnd, `Unnamed flag`)
    //                 continue;
    //             }
    //             const flagVal = m3[3]
    //             const isArray = flagVal == undefined ? false : flagVal.charAt(0) == '['
    //             const flagArr: { start: number, end: number, el: string }[] = []
    //             if (flags[flagName] != undefined) {
    //                 addWarning(flagStart, flagEnd, `Duplicate flag ${flagName}`)
    //                 continue;
    //             }
    //             if (isArray) {
    //                 flags[flagName] = {
    //                     value: [],
    //                     s: flagStart,
    //                     e: flagEnd,
    //                 };
    //                 for (const i in flagArr) {
    //                     flags[flagName].value.push(flagArr[i].el)
    //                 }
    //             } else {
    //                 flags[flagName] = {
    //                     value: [flagVal],
    //                     s: flagStart,
    //                     e: flagEnd,
    //                 }
    //             }
    //             const valStart = optionStart + (m3.indices[3]?? m3.indices[2])[0]
    //             const valEnd = optionStart + (m3.indices[3]?? m3.indices[2])[1]
    //             if (isArray) {
    //                 let listM;
    //                 while (listM = listPattern.exec(flagVal.substring(1, flagVal.length - 1))) {
    //                     if (!listM.indices) {
    //                         connection.console.error(`Error getting indices for regex`)
    //                         break;
    //                     }
    //                     flagArr.push({
    //                         start: valStart + 1 + listM.indices[1][0],
    //                         end: valStart + 1 + listM.indices[1][1],
    //                         el: listM[1]
    //                     })
    //                 }
    //             }
    //             const flagDef = tokenFlags[flagName];
    // 			if(flagName == undefined) {
    //                 if (semicolon) {
    //                     addInfo(flagEnd-1, flagEnd, `??`)
    // 					break;
    //                 }
    //                 addError(flagStart, flagEnd, `???: "${m3[0]}"`)
    // 				continue;
    // 			}
    // 			if(flagDef) {
    //                 if (flagDef.type && flagDef.type != typeName) {
    //                     addError(valStart, valEnd, `Flag '${flagName}' only applies to ${flagDef.type} tokens: was applied to ${typeName}`)
    // 					continue;
    // 				}
    //                 if ((flagDef.value || flagDef.arr) && !(flagVal)) {
    //                     addError(flagStart, flagEnd, `Flag '${flagName}' must have a value`)
    // 					continue;
    //                 } else if ((flagDef.value && !flagDef.arr) && isArray) {
    //                     addError(valStart, valEnd, `Flag '${flagName}' can't have an array value, had "${flagVal}"`)
    // 					continue;
    //                 } else if ((!flagDef.value && flagDef.arr) && !isArray) {
    //                     addError(valStart, valEnd, `Flag '${flagName}' must have an array value, had "${flagVal}"`)
    // 					continue;
    //                 } else if ((!flagDef.value && !flagDef.arr) && flagVal) {
    //                     addError(valStart, valEnd, `Flag '${flagName}' can't have a value, had "${flagVal}"`)
    // 					continue;
    //                 } else if (flagDef.str && flagVal.charAt(0) != '"') {
    //                     addError(valStart, valEnd, `Flag ${flagName} must have a string literal value, had ${flagVal}`)
    //                 }
    //             } else if(flagName.startsWith("meta-")) {
    //                 if (isArray) {
    //                     addError(valStart, valEnd, `Meta flags can't have an array value`)
    // 					continue;
    // 				}
    // 			} else {
    //                 addWarning(flagStart, flagEnd, `Unknown flag: '${flagName}'`)
    // 				continue;
    //             }
    //             if (flagName == 'glyphs') {
    //                 for (const i in flagArr) {
    //                     const v = flagArr[i]
    //                     if (v.el.charAt(0) != '\\' && v.el != '-') {
    //                         addError(v.start, v.end, `Unknown glyph: ${v.el}`)
    //                     }
    //                 }
    //             } else if (flagName == 'suffix-group') {
    //                 if (isArray) {
    //                     for (const i in flagArr) {
    //                         const v = flagArr[i]
    //                         suffixGroups.use(v.el, v.start, v.end)
    //                     }
    //                 } else {
    //                     suffixGroups.use(flagVal, valStart, valEnd)
    //                 }
    //             } else if (flagName == 'group-suffix') {
    //                 if (isArray) {
    //                     for (const i in flagArr) {
    //                         suffixGroups.define(flagArr[i].el)
    //                     }
    //                 } else {
    //                     suffixGroups.define(flagVal)
    //                 }
    //             } else if (flagName == 'base-group') {
    //                 if (isArray) {
    //                     for (const i in flagArr) {
    //                         const v = flagArr[i]
    //                         baseGroups.use(v.el, v.start, v.end)
    //                     }
    //                 } else {
    //                     baseGroups.use(flagVal, valStart, valEnd)
    //                 }
    //             } else if (flagName == 'group-base') {
    //                 if (isArray) {
    //                     for (const i in flagArr) {
    //                         baseGroups.define(flagArr[i].el)
    //                     }
    //                 } else {
    //                     baseGroups.define(flagVal)
    //                 }
    //             } else if (flagName == 'prefix-group') {
    //                 if (isArray) {
    //                     for (const i in flagArr) {
    //                         const v = flagArr[i]
    //                         prefixGroups.use(v.el, v.start, v.end)
    //                     }
    //                 } else {
    //                     prefixGroups.use(flagVal, valStart, valEnd)
    //                 }
    //             } else if (flagName == 'group-prefix') {
    //                 if (isArray) {
    //                     for (const i in flagArr) {
    //                         prefixGroups.define(flagArr[i].el)
    //                     }
    //                 } else {
    //                     prefixGroups.define(flagVal)
    //                 }
    //             }
    //         }
    //         if (!(semicolon || /^\s*;/.exec(options))) {
    //             addError(cPos + m[0].length, cPos + m[0].length + 1, `Malformed line: missing ';' (${options})`)
    //         }
    //         if (!flags['meta-usage']) {
    //             if (flags['e-synonym-group']) {
    //                 addInfo(flags['e-synonym-group'].s, flags['e-synonym-group'].e, `Token was marked as synonym. Consider adding a meta-usage flag`)
    //             }
    //             if (flags['t-synonym-group']) {
    //                 addInfo(flags['t-synonym-group'].s, flags['t-synonym-group'].e, `Token was marked as synonym. Consider adding a meta-usage flag`)
    //             }
    //         }
    //         if (tokenDef.flags) {
    //             for (const name in tokenDef.flags) {
    //                 const flag = tokenDef.flags[name]
    //                 if (!flags[name]) {
    //                     if(flag == 2)
    //                         addError(cPos, eol, `Missing required flag ${name}`)
    //                     else if(flag == 1)
    //                         addWarning(cPos, eol, `Missing expected flag ${name}`)
    //                     else if(flag == 0)
    //                         addHint(cPos, eol, `Missing suggested flag ${name}`)
    //                 }
    //             }
    //         }
    //         tokensE.add(tokenE, cPos + m2.indices[2][0], cPos + m2.indices[2][1], flags['t-synonym-group']?.value)
    //         tokensT.add(tokenT, cPos + m2.indices[3][0], cPos + m2.indices[3][1], flags['e-synonym-group']?.value)
    // 		if(numFlags > 16) {
    // 			continue;
    // 		}
    //         continue;
    //     } else { // Other lines
    //         if (commentedLinePattern.exec(line)) {
    //             addInfo(cPos, eol, `Commented line: "${m[0]}"`)
    // 			continue;
    //         }
    //         if (line == '\n' || line.length == 0) {
    // 			continue;
    //         }
    //         addError(cPos, eol, `Unknown line type: "${line}" ${m[0].charCodeAt(0)}`)
    //         continue;
    //     }
    // }
    return diagnostics;
}
connection.onDidChangeWatchedFiles(_change => {
    // Monitored files have change in VSCode
    connection.console.log('We received a file change event');
});
// This handler provides the initial list of the completion items.
connection.onCompletion((position) => {
    // The pass parameter contains the position of the text document in
    // which code complete got requested. For the example we ignore this
    // info and always provide the same completion items.
    const textDocument = documents.get(position.textDocument.uri);
    if (textDocument == null) {
        return [];
    }
    const line = textDocument.getText().split('\n')[position.position.line];
    // connection.console.log(`Line: '${line}'`)
    // const kwRegex = /^[A-Z_]+|^\-[\w-]*/
    // const tokenOption = /^([A-Z_]+)\s+"[^\"]*"\s+"[^"]*"\s+/
    // const eol = /;.*\n?\r?$/
    // const foFlagPre = /^-([\w-]+)\s*=/
    // var m;
    // if (m = eol.exec(line)) {
    //     if (position.position.character > m.index) {
    //         return [];
    //     }
    // }
    // if (m = foFlagPre.exec(line)) {
    //     if (position.position.character >= m[0].length) {
    //         const foId = m[1]
    //         if (!fileOptions[foId] || !fileOptions[foId].flags) return [];
    //         var rts: CompletionItem[] = []
    //         for (const fId in fileOptions[foId].flags) {
    //             rts.push({
    //                 label: '-'+fId,
    //                 kind: CompletionItemKind.Constant,
    //                 data: 'fileOptionFlag-'+foId
    //             })
    //         }
    //         return rts
    //     }
    // }
    // if (m = tokenOption.exec(line)) {
    //     if (position.position.character >= m[0].length) {
    //         var rts: CompletionItem[] = []
    //         for (const name in tokenFlags) {
    //             const flag = tokenFlags[name]
    //             const label = '-' + name
    //             const o: CompletionItem = {
    //                 label: label,
    //                 kind: CompletionItemKind.Field,
    //                 data: 'tokenFlag'
    //             }
    //             if (flag.arr) {
    //                 o.insertText = label + '=[]'
    //             } else if (flag.str) {
    //                 o.insertText = label + '=""'
    //             } else if (flag.value) {
    //                 o.insertText = label + '='
    //             }
    //             if (flag.type && flag.type != m[1]) {
    //                 continue
    //             }
    //             rts.push(o)
    //         }
    //         return rts
    //     }
    // } else if (position.position.character == 0 || kwRegex.test(line)) {
    //     var rts: CompletionItem[] = [];
    //     for (const id in fileOptions) {
    //         const o: CompletionItem = {
    //             label: '-'+id,
    //             kind: CompletionItemKind.Property,
    //             data: 'fileOption'
    //         }
    //         rts.push(o)
    //     }
    //     for (const id in tokenTypes) {
    //         const o: CompletionItem = {
    //             label: id,
    //             kind: CompletionItemKind.Keyword,
    //             data: 'tokenType'
    //         }
    //         rts.push(o)
    //     }
    //     return rts;
    // }
    return [];
});
// This handler resolves additional information for the item selected in
// the completion list.
connection.onCompletionResolve((item) => {
    // const id = item.label
    // const isFlag = id.startsWith('-')
    // const flagId = id.substring(1)
    // const type: string = item.data
    // if (type === 'tokenFlag' && isFlag && tokenFlags[flagId]) {
    //     item.documentation = tokenFlags[flagId].desc ?? ''
    // } else if (type === 'tokenType' && tokenTypes[id]) {
    //     item.detail = tokenTypes[id].name ?? item.label
    //     item.documentation = tokenTypes[id].desc ?? ''
    // } else if (type === 'fileOption' && isFlag && fileOptions[flagId]) {
    //     item.detail = fileOptions[flagId].name ?? flagId
    //     item.documentation = fileOptions[flagId].desc ?? ''
    // } else if (type.startsWith('fileOptionFlag-') && isFlag) {
    //     const fOId = type.substring(15)
    //     const fO = fileOptions[fOId]
    //     connection.console.log(`Test: ${fOId}`)
    //     if (fO) {
    //         if (fO.flags && fO.flags[flagId]) {
    //             const flag = fO.flags[flagId]
    //             item.detail = flag.name ?? flagId
    //             item.documentation = flag.desc ?? ''
    //         }
    //     }
    // }
    return item;
});
const compilerLines = {
    "define": { name: "Define", desc: "Defines a compiler alise for a constant value", usage: "`#define [name] [value]`" },
    "syscall": { name: "Syscall Map", desc: "Maps a system call to specified index", usage: "`#syscall [index] [function]`" },
    "function": { name: "Function", desc: "Defines a function", usage: "`#function [name] ({[reg] [type] [name]}...)`" },
    "endfunction": { name: "End Function", desc: "Marks a function as ended. This should be after **all** function code", usage: "`#endfunction ([return])`" },
    "include": { name: "Include", desc: "Include an OBJ file", usage: "`#include [file]`" }
};
const asmLines = {
    "HALT": { name: "Halt", desc: "Halts the CPU. **Privileged**", usage: "`HALT`" },
    "LOAD": {
        name: "Load", desc: "Load a value into a register. Equivalent to `r[rg] = [value]`", usage: "`LOAD [rg] [value]`", sub: {
            "MEM": { name: "Load Memory", desc: "Load a value into a register from memory. Equivalent to `r[rg] = mem[r[ra]]`", usage: "`LOAD MEM [rg] [ra]`" },
        }
    },
    "COPY": {
        name: "Copy", desc: "Copy a value between registers. Equivalent to `r[rd] = r[rs]`", usage: "`COPY [rs] [rd]`", sub: {
            "MEM": { name: "Copy Memory", desc: "Copy a value between memory locations. Equivalent to `mem[r[rd]] = mem[r[rs]]`", usage: "`COPY MEM [rs] [rd]`" },
        }
    },
    "STORE": {
        name: "Store", desc: "Store a value from a register into memory. Equivalent to `mem[r[ra]] = r[rg]` or `mem[r[ra]] = [value]`", usage: "`STORE <[rg]|[value]> [ra]`"
    },
    "ADD": {
        name: "Addition", desc: "Add two registers. Equivalent to `r[rd] = r[ra] + r[rb]`", usage: "`ADD [rd] [ra] [rb]`"
    },
    "SUB": {
        name: "Subtraction", desc: "Subtract two registers. Equivalent to `r[rd] = r[ra] - r[rb]`", usage: "`SUB [rd] [ra] [rb]`"
    },
    "INC": {
        name: "Increment", desc: "Increment a register. Equivalent to `r[rg] = r[rg] + [value ?? 1]`", usage: "`INC [rg] ([value])`"
    },
    "STACK": {
        name: "Stack", desc: "Stack operation", usage: "`STACK <PUSH|POP> [rg] | STACK <INC|DEC> ([amount])`", sub: {
            "PUSH": { name: "Stack Push", desc: "Push the value of a register to the stack", usage: "`STACK PUSH [rg]`" },
            "POP": { name: "Stack Pop", desc: "Pop a value from the stack into a register", usage: "`STACK POP [rg]`" },
            "INC": { name: "Stack Increment", desc: "Increment the stack pointer", usage: "`STACK INC ([amount])`" },
            "DEC": { name: "Stack Decrement", desc: "Decrement the stack pointer", usage: "`STACK DEC ([amount])`" }
        }
    },
    "SYSCALL": {
        name: "System Call", desc: "Make a system call to the specified function", usage: "`SYSCALL [functionName|functionIndex]`"
    },
    "SYSRETURN": {
        name: "System Call Return", desc: "Return from a system call. **Privileged**", usage: "`SYSRETURN`"
    },
    "INTERRUPT": {
        name: "Interrupt", desc: "Trigger an interrupt", usage: "`INTERRUPT <RET|[code]>`", sub: {
            "RET": { name: "Interrupt return", desc: "Return from an interrupt, resetting registers", usage: "INTERRUPT RET" }
        }
    },
    "GOTO": {
        name: "Goto", desc: "Unconditional goto", usage: "`GOTO (<PUSH|POP>) (<EQ|LEQ|GT|NEQ> [rg]) <[:label]|[ra]>`", sub: {
            "PUSH": {
                name: "Goto Push", desc: "Unconditional goto that pushes the current program pointer to the stack", usage: "`GOTO PUSH (<EQ|LEQ|GT|NEQ> [rg]) <[:label]|[ra]>`", sub: {
                    "EQ": {
                        name: "Goto Push Equals Zero", desc: "Conditional goto on `r[rg] == 0` that pushes the current program pointer to the stack", usage: "`GOTO PUSH EQ [rg] <[:label]|[ra]>`"
                    },
                    "LEQ": {
                        name: "Goto Push Less than or Equals Zero", desc: "Conditional goto on `r[rg] <= 0` that pushes the current program pointer to the stack", usage: "`GOTO PUSH LEQ [rg] <[:label]|[ra]>`"
                    },
                    "GT": {
                        name: "Goto Push Greater than Zero", desc: "Conditional goto on `r[rg] > 0` that pushes the current program pointer to the stack", usage: "`GOTO PUSH GT [rg] <[:label]|[ra]>`"
                    },
                    "NEQ": {
                        name: "Goto Push Not Equals Zero", desc: "Conditional goto on `r[rg] != 0` that pushes the current program pointer to the stack", usage: "`GOTO PUSH NEQ [rg] <[:label]|[ra]>`"
                    }
                }
            },
            "POP": {
                name: "Goto Pop", desc: "Unconditional goto that returns the address on the top of the stack", usage: "`GOTO POP (<EQ|LEQ|GT|NEQ> [rg])`", sub: {
                    "EQ": {
                        name: "Goto Pop Equals Zero", desc: "Conditional goto on `r[rg] == 0` that returns the address on the top of the stack", usage: "`GOTO POP EQ [rg]`"
                    },
                    "LEQ": {
                        name: "Goto Pop Less than or Equals Zero", desc: "Conditional goto on `r[rg] <= 0` that returns the address on the top of the stack", usage: "`GOTO POP LEQ [rg]`"
                    },
                    "GT": {
                        name: "Goto Pop Greater than Zero", desc: "Conditional goto on `r[rg] > 0` that returns the address on the top of the stack", usage: "`GOTO POP GT [rg]`"
                    },
                    "NEQ": {
                        name: "Goto Pop Not Equals Zero", desc: "Conditional goto on `r[rg] != 0` that returns the address on the top of the stack", usage: "`GOTO POP NEQ [rg]`"
                    }
                }
            },
            "EQ": {
                name: "Goto Equals Zero", desc: "Conditional goto on `r[rg] == 0`", usage: "`GOTO EQ [rg] <[:label]|[ra]>`"
            },
            "LEQ": {
                name: "Goto Less than or Equals Zero", desc: "Conditional goto on `r[rg] <= 0`", usage: "`GOTO LEQ [rg] <[:label]|[ra]>`"
            },
            "GT": {
                name: "Goto Greater than Zero", desc: "Conditional goto on `r[rg] > 0`", usage: "`GOTO GT [rg] <[:label]|[ra]>`"
            },
            "NEQ": {
                name: "Goto Not Equals Zero", desc: "Conditional goto on `r[rg] != 0`", usage: "`GOTO NEQ [rg] <[:label]|[ra]>`"
            }
        }
    }
};
connection.onHover((params, token, workProgress, resultProgress) => {
    const position = params.position;
    const textDocument = documents.get(params.textDocument.uri);
    if (textDocument == null) {
        return undefined;
    }
    const line = textDocument.getText().split('\n')[position.line];
    if (line.startsWith("//") || line.match("^\\s*$")) {
        return undefined;
    }
    else if (line.startsWith(":")) {
        return {
            contents: `### Label\n\nCompiler reference label for GOTO instructions\n\n#### Usage\n\n\`:[label name]\``,
            range: {
                start: { line: position.line, character: 0 },
                end: { line: position.line, character: line.length }
            }
        };
    }
    else if (line.startsWith("#")) {
        const instr = line.split(" ")[0].substring(1);
        if (compilerLines[instr]) {
            let asmLine = compilerLines[instr];
            return {
                contents: `### ${asmLine.name}\n\n${asmLine.desc}\n\n#### Usage\n\n${asmLine.usage}`,
                range: {
                    start: { line: position.line, character: 0 },
                    end: { line: position.line, character: line.length }
                }
            };
        }
    }
    else {
        const parts = line.split(" ");
        if (asmLines[parts[0]]) {
            let asmLine = asmLines[parts[0]];
            let nI = 1;
            while (nI < parts.length && asmLine.sub) {
                if (asmLine.sub[parts[nI]]) {
                    asmLine = asmLine.sub[parts[nI]];
                    nI++;
                }
                else {
                    break;
                }
            }
            return {
                contents: `### ${asmLine.name}\n\n${asmLine.desc}\n\n#### Usage\n\n${asmLine.usage}`,
                range: {
                    start: { line: position.line, character: 0 },
                    end: { line: position.line, character: line.length }
                }
            };
        }
    }
    // const kwRegex = /^([A-Z_]+)|^\-([\w-]*)/
    // const tokenOption = /^([A-Z_]+)\s+"[^\"]*"\s+"[^"]*"\s+/
    // let m;
    // if ((m = kwRegex.exec(line))) {
    //     if (m[0].length > position.character) {
    //         if (m[1]) { // token
    //             const tokenDef = tokenTypes[m[1]]
    //             if (tokenDef) {
    //                 let options = "";
    //                 if (tokenDef.flags) {
    //                     options = "\n#### Options:\n"
    //                     for (const flag in tokenDef.flags) {
    //                         options += `- \`${flag}\`: `
    //                         switch (tokenDef.flags[flag]) {
    //                             case 2:
    //                                 options += '**Required**'
    //                                 break;
    //                             case 1:
    //                                 options += '**Expected**'
    //                                 break;
    //                             case 0:
    //                                 options += 'Suggested'
    //                                 break;
    //                             case -1:
    //                                 options += '*Optional*'
    //                                 break;
    //                             default:
    //                                 break;
    //                         }
    //                         options += '\n'
    //                     }
    //                 }
    //                 return {
    //                     contents: `### ${tokenDef.name}\n\n${tokenDef.desc ?? ""}` + options,
    //                     range: {
    //                         start: { line: position.line, character: 0 },
    //                         end: { line: position.line, character: m[1].length }
    //                     }
    //                 }
    //             }
    //             return {
    //                 contents: `Unknown token type: ${m[0]}`
    //             }
    //         } else { // file option
    //             const foDef = fileOptions[m[2]]
    //             if (foDef) {
    //                 return {
    //                     contents: `### ${foDef.name}\n\n${foDef.desc ?? ""}`,
    //                     range: {
    //                         start: { line: position.line, character: 0 },
    //                         end: { line: position.line, character: m[2].length + 1 }
    //                     }
    //                 }
    //             }
    //             return {
    //                 contents: `Unknown file option: ${m[0]}`
    //             }
    //         }
    //     }
    // }
    // if ((m = tokenOption.exec(line))) { // token option flag
    //     if(m[0].length < position.character) {
    //         const optionStart = m[0].length
    //         const options = line.substring(optionStart)
    //         let semicolon = false;
    //         while ((m = tokenFlagPattern.exec(options))) {
    //             if (!m.indices) {
    //                 connection.console.error(`Error getting indices for regex`)
    //                 continue;
    //             }
    //             const flagStart = optionStart + m.indices[1][0];
    //             const flagEnd = optionStart + m.indices[1][1];
    //             if (semicolon) return undefined;
    //             if (m[4] == ';') {
    //                 semicolon = true;
    //             }
    //             const flagName = m[2];
    //             if (!flagName) { // Not a valid token option here
    //                 connection.console.warn(`Unknown token option: "${m[1]}" (@ ${params.textDocument.uri}:${position.line}:${position.character})`)
    //                 continue;
    //             }
    //             const flagDef = tokenFlags[flagName]
    //             if (optionStart + m.indices[2][0] - 1 <= position.character && position.character <= optionStart + m.indices[2][1]) {
    //                 if (flagDef) {
    //                     // connection.console.debug(`Testing option: ${flagName} (${position.line}:${position.character})`)
    //                     let valOptions = "\n\n"
    //                     if (flagDef.value && !flagDef.str && !flagDef.arr) {
    //                         valOptions += `Must have a literal value`
    //                     } else if (!flagDef.value && !flagDef.str && flagDef.arr) {
    //                         valOptions += `Must have an array value`
    //                     } else if (flagDef.value && flagDef.str && !flagDef.arr) {
    //                         valOptions += `Must have an string value`
    //                     } else if (flagDef.value && flagDef.str && !flagDef.arr) {
    //                         valOptions += `Must have an string value`
    //                     } else if (flagDef.value || flagDef.str || flagDef.arr) {
    //                         valOptions += `Must have a value (may be array)`
    //                     }
    //                     return {
    //                         contents: `### ${fixName(flagName)}\n\n${flagDef.desc ?? ""}` + valOptions,
    //                         range: {
    //                             start: { line: position.line, character: optionStart + m.indices[2][0] - 1 },
    //                             end: { line: position.line, character: optionStart + m.indices[2][1] }
    //                         }
    //                     }
    //                 } else if (flagName.startsWith('meta-')) {
    //                     return {
    //                         contents: `### ${fixName(flagName)}`,
    //                         range: {
    //                             start: { line: position.line, character: optionStart + m.indices[2][0] - 1 },
    //                             end: { line: position.line, character: optionStart + m.indices[2][1] }
    //                         }
    //                     }
    //                 } else {
    //                     return {
    //                         contents: `Unknown token option: ${flagName}`,
    //                         range: {
    //                             start: { line: position.line, character: optionStart + m.indices[2][0] - 1 },
    //                             end: { line: position.line, character: optionStart + m.indices[2][1] }
    //                         }
    //                     }
    //                 }
    //             }
    //             if (optionStart + m.index + m[0].length > position.character) return;
    //         }
    //     }
    // }
    return undefined;
});
// Make the text document manager listen on the connection
// for open, change and close text document events
documents.listen(connection);
// Listen on the connection
connection.listen();
//# sourceMappingURL=server.js.map