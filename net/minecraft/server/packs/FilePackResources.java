/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class FilePackResources
extends AbstractPackResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Splitter SPLITTER = Splitter.on('/').omitEmptyStrings().limit(3);
    private final File file;
    @Nullable
    private ZipFile zipFile;
    private boolean failedToLoad;

    public FilePackResources(String string, File file, boolean bl) {
        super(string, bl);
        this.file = file;
    }

    @Nullable
    private ZipFile getOrCreateZipFile() {
        if (this.failedToLoad) {
            return null;
        }
        if (this.zipFile == null) {
            try {
                this.zipFile = new ZipFile(this.file);
            } catch (IOException iOException) {
                LOGGER.error("Failed to open pack {}", (Object)this.file, (Object)iOException);
                this.failedToLoad = true;
                return null;
            }
        }
        return this.zipFile;
    }

    private static String getPathFromLocation(PackType packType, ResourceLocation resourceLocation) {
        return String.format(Locale.ROOT, "%s/%s/%s", packType.getDirectory(), resourceLocation.getNamespace(), resourceLocation.getPath());
    }

    @Override
    @Nullable
    public IoSupplier<InputStream> getRootResource(String ... strings) {
        return this.getResource(String.join((CharSequence)"/", strings));
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation resourceLocation) {
        return this.getResource(FilePackResources.getPathFromLocation(packType, resourceLocation));
    }

    @Nullable
    private IoSupplier<InputStream> getResource(String string) {
        ZipFile zipFile = this.getOrCreateZipFile();
        if (zipFile == null) {
            return null;
        }
        ZipEntry zipEntry = zipFile.getEntry(string);
        if (zipEntry == null) {
            return null;
        }
        return IoSupplier.create(zipFile, zipEntry);
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        ZipFile zipFile = this.getOrCreateZipFile();
        if (zipFile == null) {
            return Set.of();
        }
        Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
        HashSet<String> set = Sets.newHashSet();
        while (enumeration.hasMoreElements()) {
            ArrayList<String> list;
            ZipEntry zipEntry = enumeration.nextElement();
            String string = zipEntry.getName();
            if (!string.startsWith(packType.getDirectory() + "/") || (list = Lists.newArrayList(SPLITTER.split(string))).size() <= 1) continue;
            String string2 = (String)list.get(1);
            if (string2.equals(string2.toLowerCase(Locale.ROOT))) {
                set.add(string2);
                continue;
            }
            LOGGER.warn("Ignored non-lowercase namespace: {} in {}", (Object)string2, (Object)this.file);
        }
        return set;
    }

    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

    @Override
    public void close() {
        if (this.zipFile != null) {
            IOUtils.closeQuietly((Closeable)this.zipFile);
            this.zipFile = null;
        }
    }

    @Override
    public void listResources(PackType packType, String string, String string2, PackResources.ResourceOutput resourceOutput) {
        ZipFile zipFile = this.getOrCreateZipFile();
        if (zipFile == null) {
            return;
        }
        Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
        String string3 = packType.getDirectory() + "/" + string + "/";
        String string4 = string3 + string2 + "/";
        while (enumeration.hasMoreElements()) {
            String string5;
            ZipEntry zipEntry = enumeration.nextElement();
            if (zipEntry.isDirectory() || !(string5 = zipEntry.getName()).startsWith(string4)) continue;
            String string6 = string5.substring(string3.length());
            ResourceLocation resourceLocation = ResourceLocation.tryBuild(string, string6);
            if (resourceLocation != null) {
                resourceOutput.accept(resourceLocation, IoSupplier.create(zipFile, zipEntry));
                continue;
            }
            LOGGER.warn("Invalid path in datapack: {}:{}, ignoring", (Object)string, (Object)string6);
        }
    }
}

