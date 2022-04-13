/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data;

import java.io.IOException;
import java.nio.file.Path;

public interface CachedOutput {
    public void writeIfNeeded(Path var1, String var2) throws IOException;

    public void writeIfNeeded(Path var1, byte[] var2, String var3) throws IOException;
}

