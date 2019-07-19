/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
import net.minecraft.server.packs.AbstractResourcePack;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.ResourcePackFileNotFoundException;
import org.apache.commons.io.IOUtils;

public class FileResourcePack
extends AbstractResourcePack {
    public static final Splitter SPLITTER = Splitter.on('/').omitEmptyStrings().limit(3);
    private ZipFile zipFile;

    public FileResourcePack(File file) {
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
    public Collection<ResourceLocation> getResources(PackType packType, String string, int i, Predicate<String> predicate) {
        ZipFile zipFile;
        try {
            zipFile = this.getOrCreateZipFile();
        } catch (IOException iOException) {
            return Collections.emptySet();
        }
        Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
        ArrayList<ResourceLocation> list = Lists.newArrayList();
        String string2 = packType.getDirectory() + "/";
        while (enumeration.hasMoreElements()) {
            String[] strings;
            String string4;
            int j;
            String string3;
            ZipEntry zipEntry = enumeration.nextElement();
            if (zipEntry.isDirectory() || !zipEntry.getName().startsWith(string2) || (string3 = zipEntry.getName().substring(string2.length())).endsWith(".mcmeta") || (j = string3.indexOf(47)) < 0 || !(string4 = string3.substring(j + 1)).startsWith(string + "/") || (strings = string4.substring(string.length() + 2).split("/")).length < i + 1 || !predicate.test(string4)) continue;
            String string5 = string3.substring(0, j);
            list.add(new ResourceLocation(string5, string4));
        }
        return list;
    }
}

