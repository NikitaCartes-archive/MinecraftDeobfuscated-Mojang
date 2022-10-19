/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class PathPackResources
extends AbstractPackResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Joiner PATH_JOINER = Joiner.on("/");
    private final Path root;

    public PathPackResources(String string, Path path) {
        super(string);
        this.root = path;
    }

    @Override
    @Nullable
    public IoSupplier<InputStream> getRootResource(String ... strings) {
        FileUtil.validatePath(strings);
        Path path = FileUtil.resolvePath(this.root, List.of(strings));
        if (Files.exists(path, new LinkOption[0])) {
            return IoSupplier.create(path);
        }
        return null;
    }

    public static boolean validatePath(Path path) {
        return true;
    }

    @Override
    @Nullable
    public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation resourceLocation) {
        Path path = this.root.resolve(packType.getDirectory()).resolve(resourceLocation.getNamespace());
        return PathPackResources.getResource(resourceLocation, path);
    }

    public static IoSupplier<InputStream> getResource(ResourceLocation resourceLocation, Path path) {
        return FileUtil.decomposePath(resourceLocation.getPath()).get().map(list -> {
            Path path2 = FileUtil.resolvePath(path, list);
            return PathPackResources.returnFileIfExists(path2);
        }, partialResult -> {
            LOGGER.error("Invalid path {}: {}", (Object)resourceLocation, (Object)partialResult.message());
            return null;
        });
    }

    @Nullable
    private static IoSupplier<InputStream> returnFileIfExists(Path path) {
        if (Files.exists(path, new LinkOption[0]) && PathPackResources.validatePath(path)) {
            return IoSupplier.create(path);
        }
        return null;
    }

    @Override
    public void listResources(PackType packType, String string, String string2, PackResources.ResourceOutput resourceOutput) {
        FileUtil.decomposePath(string2).get().ifLeft(list -> {
            Path path = this.root.resolve(packType.getDirectory()).resolve(string);
            PathPackResources.listPath(string, path, list, resourceOutput);
        }).ifRight(partialResult -> LOGGER.error("Invalid path {}: {}", (Object)string2, (Object)partialResult.message()));
    }

    public static void listPath(String string, Path path3, List<String> list, PackResources.ResourceOutput resourceOutput) {
        Path path22 = FileUtil.resolvePath(path3, list);
        try (Stream<Path> stream2 = Files.find(path22, Integer.MAX_VALUE, (path, basicFileAttributes) -> basicFileAttributes.isRegularFile(), new FileVisitOption[0]);){
            stream2.forEach(path2 -> {
                String string2 = PATH_JOINER.join(path3.relativize((Path)path2));
                ResourceLocation resourceLocation = ResourceLocation.tryBuild(string, string2);
                if (resourceLocation == null) {
                    Util.logAndPauseIfInIde(String.format(Locale.ROOT, "Invalid path in pack: %s:%s, ignoring", string, string2));
                } else {
                    resourceOutput.accept(resourceLocation, IoSupplier.create(path2));
                }
            });
        } catch (NoSuchFileException stream2) {
        } catch (IOException iOException) {
            LOGGER.error("Failed to list path {}", (Object)path22, (Object)iOException);
        }
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        HashSet<String> set = Sets.newHashSet();
        Path path = this.root.resolve(packType.getDirectory());
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);){
            for (Path path2 : directoryStream) {
                String string = path2.getFileName().toString();
                if (string.equals(string.toLowerCase(Locale.ROOT))) {
                    set.add(string);
                    continue;
                }
                LOGGER.warn("Ignored non-lowercase namespace: {} in {}", (Object)string, (Object)this.root);
            }
        } catch (IOException iOException) {
            LOGGER.error("Failed to list path {}", (Object)path, (Object)iOException);
        }
        return set;
    }

    @Override
    public void close() {
    }
}

