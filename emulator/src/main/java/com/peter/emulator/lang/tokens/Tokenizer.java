package com.peter.emulator.lang.tokens;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.peter.emulator.lang.ELSymbol;
import com.peter.emulator.lang.Location;
import com.peter.emulator.lang.ProgramUnit;

public class Tokenizer {

    protected String working;
    protected int workingI;
    protected int lineN = 1;
    protected int col = 1;
    protected Token workingToken = null;
    public ArrayList<Token> tokens = new ArrayList<>();
    protected Location slc = null;
    protected Location mlc = null;
    protected int mlcStart = -1;
    protected int mlcEnd = 0;
    protected boolean id = false;
    protected final String fName;
    public final ProgramUnit unit;

    // public Tokenizer(String input) {
    //     working = input;
    //     workingI = 0;
    // }

    public Tokenizer(String input, Location location, ProgramUnit unit) {
        working = input;
        workingI = 0;
        this.lineN = location.line();
        this.col = location.col();
        fName = location.file();
        this.unit = unit;
    }

    public Tokenizer(String input, Location location, boolean id, ProgramUnit unit) {
        this(input, location, unit);
        this.id = id;
    }

    public Optional<String> tokenize() {
        while (workingI < working.length()) {
            char c = working.charAt(workingI);
            workingI++;
            try {
                switch (c) {
                    case '\n' -> {
                        // if(mlc != null) {
                        //     unit.addSymbol(ELSymbol.Type.COMMENT_BLOCK, mlc.span(new Location(fName, lineN, col)));
                        //     mlc = new Location(fName, lineN+1, 1);
                        // }
                        lineN++;
                        col = 1;
                        // System.out.println("\n\\n "+lineN);
                    }
                    case '\r' -> { }
                    default -> col++;
                }
                // System.out.println("\n\\r "+lineN);
                // System.out.print("c"+((int)c));
                if (!ingest(c, new Location(fName, lineN, col))) {
                    return Optional.of("Found unexpected character: '" + c + "' (" + ((int) c) + "); At line " + lineN
                            + " col " + col);
                }
            } catch (TokenizerError e) {
                return Optional.of(e.reason + "; At line " + lineN + " col " + col);
            }
        }
        if (slc != null || mlc != null) {
            System.out.println("End was in comment");
        }
        return Optional.empty();
    }

    private int lastCol = 0;
    public boolean ingest(char c, Location location) {
        if (slc != null) {
            // if (c == '\n')
            //     System.out.print("\\n");
            // else
                // System.out.print("c"+((int)c));
            if (c == '\n') {
                unit.addSymbol(ELSymbol.Type.COMMENT_LINE, slc.span(location));
                slc = null;
                // System.out.println("\nEnd of SLC");
            }
            return true;
        } else if (mlc != null) {
            if(c == '\n') {
                unit.addSymbol(ELSymbol.Type.COMMENT_BLOCK, mlc.span(new Location(location.file(), location.line()-1, lastCol)));
                mlc = location;
            } else if (mlcEnd == 0 && c == '*') {
                mlcEnd = 1;
                return true;
            } else if (mlcEnd == 1 && c == '/') {
                unit.addSymbol(ELSymbol.Type.COMMENT_BLOCK, mlc.span(location));
                mlc = null;
                mlcEnd = 0;
                // System.out.println("\nEnd of MLC");
                return true;
            } else {
                mlcEnd = 0;
            }
            lastCol = location.col();
            return true;
        }
        if (workingToken != null) {
            Token tkn = workingToken.ingest(c, location);
            if (tkn != null) {
                if(workingToken != tkn) {
                    tokens.removeLast();
                    tokens.add(tkn);
                    workingToken = tkn;
                }
                if (workingToken instanceof OperatorToken ot) {
                    if (ot.type == OperatorToken.Type.COMMENT) {
                        slc = ot.startLocation;
                        tokens.remove(tokens.size()-1);
                        workingToken = null;
                        // System.out.println("\nFound comment "+lineN+":"+col);
                    } else if (ot.type == OperatorToken.Type.COMMENT_MULTILINE) {
                        mlc = ot.startLocation;
                        tokens.remove(tokens.size()-1);
                        workingToken = null;
                        // System.out.println("\nFound multi-line comment "+lineN+":"+col);
                    }
                }
                return true;
            }
            workingToken = null;
        }
        if (IdentifierToken.validStart(c)) {
            workingToken = new IdentifierToken(c, location, unit);
        } else if (OperatorToken.Type.contains(c+"") && !id) {
            workingToken = new OperatorToken(c, location, unit);
        } else if (Character.isDigit(c) || c == '-' || c == '+' && !id) {
            workingToken = new NumberToken(c, location);
        } else if (c == '"' || c == '\'' && !id) {
            workingToken = new StringToken(c, location);
        } else if (c == '"' || c == '\'' && !id) {
            workingToken = new StringToken(c, location);
        } else if (c == '{' && !id) {
            workingToken = new BlockToken(location, unit);
        } else if (c == '(') {
            workingToken = new SetToken(SetToken.BracketType.PARENTHESES, location, unit);
        } else if (c == '@' && !id) {
            workingToken = new AnnotationToken(location, unit);
        } else return Character.isWhitespace(c) || c == '\r' || c == '\n';
        tokens.add(workingToken);
        return true;
    }

    public static void printTokens(List<Token> tokens) {
        printTokens(tokens, "");
    }

    public static void printTokens(List<Token> tokens, String prefix) {
        for (Token t : tokens) {
            System.out.println(prefix+t);
            if (t.subTokens != null) {
                printTokens(t.subTokens,prefix+"\t");
            }
        }
    }
}
