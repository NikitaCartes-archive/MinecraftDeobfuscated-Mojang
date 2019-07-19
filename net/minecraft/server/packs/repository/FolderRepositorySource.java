/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.repository;

import java.io.File;
import java.io.FileFilter;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.server.packs.FileResourcePack;
import net.minecraft.server.packs.FolderResourcePack;
import net.minecraft.server.packs.Pack;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.repository.UnopenedPack;

public class FolderRepositorySource
implements RepositorySource {
    private static final FileFilter RESOURCEPACK_FILTER = file -> {
        boolean bl = file.isFile() && file.getName().endsWith(".zip");
        boolean bl2 = file.isDirectory() && new File(file, "pack.mcmeta").isFile();
        return bl || bl2;
    };
    private final File folder;

    public FolderRepositorySource(File file) {
        this.folder = file;
    }

    @Override
    public <T extends UnopenedPack> void loadPacks(Map<String, T> map, UnopenedPack.UnopenedPackConstructor<T> unopenedPackConstructor) {
        File[] files;
        if (!this.folder.isDirectory()) {
            this.folder.mkdirs();
        }
        if ((files = this.folder.listFiles(RESOURCEPACK_FILTER)) == null) {
            return;
        }
        for (File file : files) {
            String string = "file/" + file.getName();
            T unopenedPack = UnopenedPack.create(string, false, this.createSupplier(file), unopenedPackConstructor, UnopenedPack.Position.TOP);
            if (unopenedPack == null) continue;
            map.put(string, unopenedPack);
        }
    }

    private Supplier<Pack> createSupplier(File file) {
        if (file.isDirectory()) {
            return () -> new FolderResourcePack(file);
        }
        return () -> new FileResourcePack(file);
    }
}

