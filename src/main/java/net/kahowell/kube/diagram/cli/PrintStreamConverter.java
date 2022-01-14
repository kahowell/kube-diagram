package net.kahowell.kube.diagram.cli;

import picocli.CommandLine;

import java.io.FileOutputStream;
import java.io.PrintStream;

public class PrintStreamConverter implements CommandLine.ITypeConverter<PrintStream> {
    @Override
    public PrintStream convert(String s) throws Exception {
        return new PrintStream(new FileOutputStream(s));
    }
}
