/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.resources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@FunctionalInterface
public interface IoSupplier<T> {
    public static IoSupplier<InputStream> create(Path path) {
        return () -> Files.newInputStream(path, new OpenOption[0]);
    }

    public static IoSupplier<InputStream> create(ZipFile zipFile, ZipEntry zipEntry) {
        return () -> zipFile.getInputStream(zipEntry);
    }

    public T get() throws IOException;
}

