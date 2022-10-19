/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.linkfs;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import org.jetbrains.annotations.Nullable;

class LinkFSFileStore
extends FileStore {
    private final String name;

    public LinkFSFileStore(String string) {
        this.name = string;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String type() {
        return "index";
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public long getTotalSpace() {
        return 0L;
    }

    @Override
    public long getUsableSpace() {
        return 0L;
    }

    @Override
    public long getUnallocatedSpace() {
        return 0L;
    }

    @Override
    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> class_) {
        return class_ == BasicFileAttributeView.class;
    }

    @Override
    public boolean supportsFileAttributeView(String string) {
        return "basic".equals(string);
    }

    @Override
    @Nullable
    public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> class_) {
        return null;
    }

    @Override
    public Object getAttribute(String string) throws IOException {
        throw new UnsupportedOperationException();
    }
}

