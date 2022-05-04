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
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.ResourcePackFileNotFoundException;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class FilePackResources
extends AbstractPackResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Splitter SPLITTER = Splitter.on('/').omitEmptyStrings().limit(3);
    @Nullable
    private ZipFile zipFile;

    public FilePackResources(File file) {
        super(file);
    }

    private ZipFile getOrCreateZipFile() throws IOException {
        if (this.zipFile == null) {
            this.zipFile = new ZipFile(this.file);
        }
        return this.zipFile;
    }

    @Override
    protected InputStream getResource(String string) throws IOException {
        ZipFile zipFile = this.getOrCreateZipFile();
        ZipEntry zipEntry = zipFile.getEntry(string);
        if (zipEntry == null) {
            throw new ResourcePackFileNotFoundException(this.file, string);
        }
        return zipFile.getInputStream(zipEntry);
    }

    @Override
    public boolean hasResource(String string) {
        try {
            return this.getOrCreateZipFile().getEntry(string) != null;
        } catch (IOException iOException) {
            return false;
        }
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        ZipFile zipFile;
        try {
            zipFile = this.getOrCreateZipFile();
        } catch (IOException iOException) {
            return Collections.emptySet();
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
            this.logWarning(string2);
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
    public Collection<ResourceLocation> getResources(PackType packType, String string, String string2, Predicate<ResourceLocation> predicate) {
        ZipFile zipFile;
        try {
            zipFile = this.getOrCreateZipFile();
        } catch (IOException iOException) {
            return Collections.emptySet();
        }
        Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
        ArrayList<ResourceLocation> list = Lists.newArrayList();
        String string3 = packType.getDirectory() + "/" + string + "/";
        String string4 = string3 + string2 + "/";
        while (enumeration.hasMoreElements()) {
            String string5;
            ZipEntry zipEntry = enumeration.nextElement();
            if (zipEntry.isDirectory() || (string5 = zipEntry.getName()).endsWith(".mcmeta") || !string5.startsWith(string4)) continue;
            String string6 = string5.substring(string3.length());
            ResourceLocation resourceLocation = ResourceLocation.tryBuild(string, string6);
            if (resourceLocation == null) {
                LOGGER.warn("Invalid path in datapack: {}:{}, ignoring", (Object)string, (Object)string6);
                continue;
            }
            if (!predicate.test(resourceLocation)) continue;
            list.add(resourceLocation);
        }
        return list;
    }
}

