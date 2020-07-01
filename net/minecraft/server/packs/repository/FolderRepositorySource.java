/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.repository;

import java.io.File;
import java.io.FileFilter;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

public class FolderRepositorySource
implements RepositorySource {
    private static final FileFilter RESOURCEPACK_FILTER = file -> {
        boolean bl = file.isFile() && file.getName().endsWith(".zip");
        boolean bl2 = file.isDirectory() && new File(file, "pack.mcmeta").isFile();
        return bl || bl2;
    };
    private final File folder;
    private final PackSource packSource;

    public FolderRepositorySource(File file, PackSource packSource) {
        this.folder = file;
        this.packSource = packSource;
    }

    @Override
    public void loadPacks(Consumer<Pack> consumer, Pack.PackConstructor packConstructor) {
        File[] files;
        if (!this.folder.isDirectory()) {
            this.folder.mkdirs();
        }
        if ((files = this.folder.listFiles(RESOURCEPACK_FILTER)) == null) {
            return;
        }
        for (File file : files) {
            String string = "file/" + file.getName();
            Pack pack = Pack.create(string, false, this.createSupplier(file), packConstructor, Pack.Position.TOP, this.packSource);
            if (pack == null) continue;
            consumer.accept(pack);
        }
    }

    private Supplier<PackResources> createSupplier(File file) {
        if (file.isDirectory()) {
            return () -> new FolderPackResources(file);
        }
        return () -> new FilePackResources(file);
    }
}

