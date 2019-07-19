/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server;

import java.io.OutputStream;
import java.io.PrintStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class LoggedPrintStream
extends PrintStream {
    protected static final Logger LOGGER = LogManager.getLogger();
    protected final String name;

    public LoggedPrintStream(String string, OutputStream outputStream) {
        super(outputStream);
        this.name = string;
    }

    @Override
    public void println(@Nullable String string) {
        this.logLine(string);
    }

    @Override
    public void println(Object object) {
        this.logLine(String.valueOf(object));
    }

    protected void logLine(@Nullable String string) {
        LOGGER.info("[{}]: {}", (Object)this.name, (Object)string);
    }
}

