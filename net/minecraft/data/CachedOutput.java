/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data;

import com.google.common.hash.HashCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

public interface CachedOutput {
    public static final CachedOutput NO_CACHE = (path, bs, hashCode) -> {
        Files.createDirectories(path.getParent(), new FileAttribute[0]);
        Files.write(path, bs, new OpenOption[0]);
    };

    public void writeIfNeeded(Path var1, byte[] var2, HashCode var3) throws IOException;
}

