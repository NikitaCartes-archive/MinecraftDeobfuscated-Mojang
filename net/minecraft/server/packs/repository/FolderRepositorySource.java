/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.repository;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.FileUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.linkfs.LinkFileSystem;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class FolderRepositorySource
implements RepositorySource {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Path folder;
    private final PackType packType;
    private final PackSource packSource;

    public FolderRepositorySource(Path path, PackType packType, PackSource packSource) {
        this.folder = path;
        this.packType = packType;
        this.packSource = packSource;
    }

    private static String nameFromPath(Path path) {
        return path.getFileName().toString();
    }

    @Override
    public void loadPacks(Consumer<Pack> consumer) {
        try {
            FileUtil.createDirectoriesSafe(this.folder);
            FolderRepositorySource.discoverPacks(this.folder, false, (path, resourcesSupplier) -> {
                String string = FolderRepositorySource.nameFromPath(path);
                Pack pack = Pack.readMetaAndCreate("file/" + string, Component.literal(string), false, resourcesSupplier, this.packType, Pack.Position.TOP, this.packSource);
                if (pack != null) {
                    consumer.accept(pack);
                }
            });
        } catch (IOException iOException) {
            LOGGER.warn("Failed to list packs in {}", (Object)this.folder, (Object)iOException);
        }
    }

    public static void discoverPacks(Path path, boolean bl, BiConsumer<Path, Pack.ResourcesSupplier> biConsumer) throws IOException {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);){
            for (Path path2 : directoryStream) {
                Pack.ResourcesSupplier resourcesSupplier = FolderRepositorySource.detectPackResources(path2, bl);
                if (resourcesSupplier == null) continue;
                biConsumer.accept(path2, resourcesSupplier);
            }
        }
    }

    @Nullable
    public static Pack.ResourcesSupplier detectPackResources(Path path, boolean bl) {
        FileSystem fileSystem;
        BasicFileAttributes basicFileAttributes;
        try {
            basicFileAttributes = Files.readAttributes(path, BasicFileAttributes.class, new LinkOption[0]);
        } catch (NoSuchFileException noSuchFileException) {
            return null;
        } catch (IOException iOException) {
            LOGGER.warn("Failed to read properties of '{}', ignoring", (Object)path, (Object)iOException);
            return null;
        }
        if (basicFileAttributes.isDirectory() && Files.isRegularFile(path.resolve("pack.mcmeta"), new LinkOption[0])) {
            return string -> new PathPackResources(string, path, bl);
        }
        if (basicFileAttributes.isRegularFile() && path.getFileName().toString().endsWith(".zip") && ((fileSystem = path.getFileSystem()) == FileSystems.getDefault() || fileSystem instanceof LinkFileSystem)) {
            File file = path.toFile();
            return string -> new FilePackResources(string, file, bl);
        }
        LOGGER.info("Found non-pack entry '{}', ignoring", (Object)path);
        return null;
    }
}

