/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.io.OutputStream;
import java.io.PrintStream;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class LoggedPrintStream
extends PrintStream {
    private static final Logger LOGGER = LogUtils.getLogger();
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

