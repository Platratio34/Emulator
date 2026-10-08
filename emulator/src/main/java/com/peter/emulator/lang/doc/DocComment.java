package com.peter.emulator.lang.doc;

import java.util.ArrayList;
import java.util.HashMap;

import com.peter.emulator.lang.ELFunction;
import com.peter.emulator.lang.tokens.DocCommentToken;

public class DocComment {

    public String desc;

    public ArrayList<String> retDesc = new ArrayList<>();
    public HashMap<String, DocComment> paramDesc = new HashMap<>();
    public ELFunction function = null;

    public DocComment(String desc) {
        this.desc = desc;
    }

    public DocComment(DocCommentToken dcT) {
        String temp = dcT.value;
        desc = "";
        for (int i = 0; i < temp.length(); i++) {
            char c = temp.charAt(i);
            // System.out.println(i);
            switch (c) {
                case '@' -> {
                    String name = "";
                    while (i + 1 < temp.length()) {
                        c = temp.charAt(i + 1);
                        if (Character.isWhitespace(c))
                            break;
                        i++;
                        name += c;
                    }
                    switch (name) {
                        case "param" -> {
                            String pName = "";
                            while (i + 1 < temp.length()) {
                                c = temp.charAt(i + 1);
                                if (!Character.isWhitespace(c))
                                    break;
                                i++;
                            }
                            while (i + 1 < temp.length()) {
                                i++;
                                c = temp.charAt(i);
                                if (Character.isWhitespace(c))
                                    break;
                                pName += c;
                            }

                            String pDesc = "";

                            while (i + 1 < temp.length()) {
                                c = temp.charAt(i + 1);
                                if (c == '\n')
                                    break;
                                i++;
                                pDesc += c;
                            }
                            paramDesc.put(pName, new DocComment(pDesc));
                        }
                        case "returns" -> {
                            String ret = "";
                            while (i + 1 < temp.length()) {
                                c = temp.charAt(i + 1);
                                if (c == '\n')
                                    break;
                                i++;
                                ret += c;
                            }
                            retDesc.add(ret);
                        }
                        default -> {
                            desc += String.format("**`%s%s`**", name.substring(0,1).toUpperCase(), name.substring(1));
                            while (i + 1 < temp.length()) {
                                c = temp.charAt(i + 1);
                                if (c == '\n')
                                    break;
                                i++;
                                desc += c;
                            }
                            desc += "\n";
                        }
                    }
                }
                case '\n' -> {
                    while (i + 1 < temp.length()) {
                        if (c == '\n')
                            desc += "\n";
                        // System.out.println("- "+i);
                        c = temp.charAt(i + 1);
                        if(!Character.isWhitespace(c))
                            break;
                        i++;
                    }
                    continue;
                }
            }
            desc += c;
        }

    }

    public String getMD() {
        String md = desc;
        if (function != null) {
            md += "\n\n**Parameters**\n";
            for (String name : function.paramOrder) {
                md += String.format("\n- `%s` %s", name, paramDesc.getOrDefault(name, EMPTY).getMD());
            }
            if (function.ret != null && !retDesc.isEmpty()) {
                md += "\n\n**Returns**";
                for (String l : retDesc) {
                    md += "\n- " + l;
                }
            }
        }
        return md;
    }

    public static final DocComment EMPTY = new DocComment("");
}
