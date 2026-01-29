package com.peter.emulator.assembly;

public class AssemblerError {

    public String reason;
    public int lineN;
    public int col;
    public String line;
    public String source;

    public AssemblerError(String reason, int lineN, int col, String line, String source) {
        this.reason = reason;
        this.lineN = lineN;
        this.col = col;
        this.line = line;
        this.source = source;
    }

    public String getPrintString() {
        String errLine = "";
        for (int i = 0; i < col; i++) {
            errLine += " ";
        }
        errLine += "^";
        return String.format("Assembler Error: %s;\n\tAt %s:%d%s\n\t%s\n\t%s", reason, source, lineN+1, col >= 0 ? (":"+col) : "", line, errLine);
    }
}
