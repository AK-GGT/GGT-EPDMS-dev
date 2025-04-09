package de.iai.ilcd.webgui.util;

import com.okworx.ilcd.validation.util.IUpdateEventListener;

import java.io.PrintWriter;

public class StatusLogUpdateEventListener implements IUpdateEventListener {

    private PrintWriter writer;

    @Override
    public void updateProgress(double percentFinished) {
        writer.print(".");
        writer.flush();

    }

    @Override
    public void updateStatus(String statusMessage) {
        writer.println(statusMessage);
        writer.flush();

    }

    public PrintWriter getWriter() {
        return writer;
    }

    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }

}
