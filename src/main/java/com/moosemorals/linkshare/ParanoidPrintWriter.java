package com.moosemorals.linkshare;

import java.io.PrintWriter;
import java.io.Writer;

public class ParanoidPrintWriter extends PrintWriter {


    public ParanoidPrintWriter(Writer writer) {
        super(writer);
    }
}
