package com.peter.emulator.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.peter.emulator.lang.Token.*;

public class Tokenizer {

    protected String working;
    protected int workingI;
    protected int lineN = 1;
    protected int col = 1;
    protected Token workingToken = null;
    public ArrayList<Token> tokens = new ArrayList<>();
    protected boolean slc = false;
    protected boolean mlc = false;
    protected int mlcStart = -1;
    protected int mlcEnd = 0;
    protected boolean id = false;
    protected final String fName;

    // public Tokenizer(String input) {
    //     working = input;
    //     workingI = 0;
    // }

    public Tokenizer(String input, Location location) {
        working = input;
        workingI = 0;
        this.lineN = location.line();
        this.col = location.col();
        fName = location.file();
    }

    public Tokenizer(String input, Location location, boolean id) {
        this(input, location);
        this.id = id;
    }

    public Optional<String> tokenize() {
        while (workingI < working.length()) {
            char c = working.charAt(workingI);
            workingI++;
            try {
                if (c == '\n') {
                    lineN++;
                    col = 1;
                    // System.out.println("\n\\n "+lineN);
                } else if (c == '\r') {
                    // System.out.println("\n\\r "+lineN);
                } else {
                    col++;
                }
                // System.out.print("c"+((int)c));
                if (!ingest(c, new Location(fName, lineN, col))) {
                    return Optional.of("Found unexpected character: '" + c + "' (" + ((int) c) + "); At line " + lineN
                            + " col " + col);
                }
            } catch (TokenizerError e) {
                return Optional.of(e.reason + "; At line " + lineN + " col " + col);
            }
        }
        if (slc || mlc) {
            System.out.println("End was in comment");
        }
        return Optional.empty();
    }

    protected boolean ingest(char c, Location location) {
        if (slc) {
            // if (c == '\n')
            //     System.out.print("\\n");
            // else
                // System.out.print("c"+((int)c));
            if (c == '\n') {
                slc = false;
                // System.out.println("\nEnd of SLC");
            }
            return true;
        } else if (mlc) {
            if (mlcEnd == 0 && c == '*') {
                mlcEnd = 1;
                return true;
            } else if (mlcEnd == 1 && c == '/') {
                mlc = false;
                mlcEnd = 0;
                // System.out.println("\nEnd of MLC");
                return true;
            } else {
                mlcEnd = 0;
            }
            return true;
        }
        if (workingToken != null) {
            if (workingToken.ingest(c, location)) {
                if (workingToken instanceof OperatorToken ot) {
                    if (ot.type == OperatorToken.Type.COMMENT) {
                        slc = true;
                        tokens.remove(tokens.size()-1);
                        workingToken = null;
                        // System.out.println("\nFound comment "+lineN+":"+col);
                    } else if (ot.type == OperatorToken.Type.COMMENT_MULTILINE) {
                        mlc = true;
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
            workingToken = new IdentifierToken(c, location);
        } else if (OperatorToken.Type.contains(c+"") && !id) {
            workingToken = new OperatorToken(c, location);
        } else if (Character.isDigit(c) || c == '-' || c == '+' && !id) {
            workingToken = new NumberToken(c, location);
        } else if (c == '"' || c == '\'' && !id) {
            workingToken = new StringToken(c, location);
        } else if (c == '"' || c == '\'' && !id) {
            workingToken = new StringToken(c, location);
        } else if (c == '{' && !id) {
            workingToken = new BlockToken(location);
        } else if (c == '(') {
            workingToken = new SetToken(')', location);
        } else if (c == '@' && !id) {
            workingToken = new AnnotationToken(location);
        } else if (Character.isWhitespace(c) || c == '\r' || c == '\n') {
            return true;
        } else {
            return false;
        }
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
