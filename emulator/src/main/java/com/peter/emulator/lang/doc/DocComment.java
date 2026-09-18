package com.peter.emulator.lang.doc;

import com.peter.emulator.lang.tokens.DocCommentToken;

public class DocComment {

    public String desc;

    public DocComment(String desc) {
        this.desc = desc;
    }

    public DocComment(DocCommentToken dcT) {
        String temp = dcT.value;
        desc = "";
        for (int i = 0; i < temp.length(); i++) {
            if (temp.charAt(i) == '@') {
                // if(temp.substring(i, i+4).equals(dcT))
            }
        }
    }

    public String getMD() {
        String md = desc;
        return md;
    }
}
