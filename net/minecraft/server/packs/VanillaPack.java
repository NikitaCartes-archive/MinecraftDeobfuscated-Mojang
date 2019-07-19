/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractResourcePack;
import net.minecraft.server.packs.FolderResourcePack;
import net.minecraft.server.packs.Pack;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class VanillaPack
implements Pack {
    public static Path generatedDir;
    private static final Logger LOGGER;
    public static Class<?> clientObject;
    private static final Map<PackType, FileSystem> JAR_FILESYSTEM_BY_TYPE;
    public final Set<String> namespaces;

    public VanillaPack(String ... strings) {
        this.namespaces = ImmutableSet.copyOf(strings);
    }

    @Override
    public InputStream getRootResource(String string) throws IOException {
        Path path;
        if (string.contains("/") || string.contains("\\")) {
            throw new IllegalArgumentException("Root resources can only be filenames, not paths (no / allowed!)");
        }
        if (generatedDir != null && Files.exists(path = generatedDir.resolve(string), new LinkOption[0])) {
            return Files.newInputStream(path, new OpenOption[0]);
        }
        return this.getResourceAsStream(string);
    }

    @Override
    public InputStream getResource(PackType packType, ResourceLocation resourceLocation) throws IOException {
        InputStream inputStream = this.getResourceAsStream(packType, resourceLocation);
        if (inputStream != null) {
            return inputStream;
        }
        throw new FileNotFoundException(resourceLocation.getPath());
    }

    @Override
    public Collection<ResourceLocation> getResources(PackType packType, String string, int i, Predicate<String> predicate) {
        URI uRI;
        HashSet<ResourceLocation> set = Sets.newHashSet();
        if (generatedDir != null) {
            try {
                set.addAll(this.getResources(i, "minecraft", generatedDir.resolve(packType.getDirectory()).resolve("minecraft"), string, predicate));
            } catch (IOException iOException) {
                // empty catch block
            }
            if (packType == PackType.CLIENT_RESOURCES) {
                Enumeration<URL> enumeration = null;
                try {
                    enumeration = clientObject.getClassLoader().getResources(packType.getDirectory() + "/minecraft");
                } catch (IOException iOException) {
                    // empty catch block
                }
                while (enumeration != null && enumeration.hasMoreElements()) {
                    try {
                        uRI = ((URL)enumeration.nextElement()).toURI();
                        if (!"file".equals(uRI.getScheme())) continue;
                        set.addAll(this.getResources(i, "minecraft", Paths.get(uRI), string, predicate));
                    } catch (IOException | URISyntaxException uRI2) {}
                }
            }
        }
        try {
            URL uRL = VanillaPack.class.getResource("/" + packType.getDirectory() + "/.mcassetsroot");
            if (uRL == null) {
                LOGGER.error("Couldn't find .mcassetsroot, cannot load vanilla resources");
                return set;
            }
            uRI = uRL.toURI();
            if ("file".equals(uRI.getScheme())) {
                URL uRL2 = new URL(uRL.toString().substring(0, uRL.toString().length() - ".mcassetsroot".length()) + "minecraft");
                if (uRL2 == null) {
                    return set;
                }
                Path path = Paths.get(uRL2.toURI());
                set.addAll(this.getResources(i, "minecraft", path, string, predicate));
            } else if ("jar".equals(uRI.getScheme())) {
                Path path2 = JAR_FILESYSTEM_BY_TYPE.get((Object)packType).getPath("/" + packType.getDirectory() + "/minecraft", new String[0]);
                set.addAll(this.getResources(i, "minecraft", path2, string, predicate));
            } else {
                LOGGER.error("Unsupported scheme {} trying to list vanilla resources (NYI?)", (Object)uRI);
            }
        } catch (FileNotFoundException | NoSuchFileException uRL) {
        } catch (IOException | URISyntaxException exception) {
            LOGGER.error("Couldn't get a list of all vanilla resources", (Throwable)exception);
        }
        return set;
    }

    private Collection<ResourceLocation> getResources(int i, String string, Path path, String string2, Predicate<String> predicate) throws IOException {
        ArrayList<ResourceLocation> list = Lists.newArrayList();
        Iterator iterator = Files.walk(path.resolve(string2), i, new FileVisitOption[0]).iterator();
        while (iterator.hasNext()) {
            Path path2 = (Path)iterator.next();
            if (path2.endsWith(".mcmeta") || !Files.isRegularFile(path2, new LinkOption[0]) || !predicate.test(path2.getFileName().toString())) continue;
            list.add(new ResourceLocation(string, path.relativize(path2).toString().replaceAll("\\\\", "/")));
        }
        return list;
    }

    @Nullable
    protected InputStream getResourceAsStream(PackType packType, ResourceLocation resourceLocation) {
        Path path;
        String string = VanillaPack.createPath(packType, resourceLocation);
        if (generatedDir != null && Files.exists(path = generatedDir.resolve(packType.getDirectory() + "/" + resourceLocation.getNamespace() + "/" + resourceLocation.getPath()), new LinkOption[0])) {
            try {
                return Files.newInputStream(path, new OpenOption[0]);
            } catch (IOException iOException) {
                // empty catch block
            }
        }
        try {
            URL uRL = VanillaPack.class.getResource(string);
            if (VanillaPack.isResourceUrlValid(string, uRL)) {
                return uRL.openStream();
            }
        } catch (IOException iOException) {
            return VanillaPack.class.getResourceAsStream(string);
        }
        return null;
    }

    private static String createPath(PackType packType, ResourceLocation resourceLocation) {
        return "/" + packType.getDirectory() + "/" + resourceLocation.getNamespace() + "/" + resourceLocation.getPath();
    }

    private static boolean isResourceUrlValid(String string, @Nullable URL uRL) throws IOException {
        return uRL != null && (uRL.getProtocol().equals("jar") || FolderResourcePack.validatePath(new File(uRL.getFile()), string));
    }

    @Nullable
    protected InputStream getResourceAsStream(String string) {
        return VanillaPack.class.getResourceAsStream("/" + string);
    }

    @Override
    public boolean hasResource(PackType packType, ResourceLocation resourceLocation) {
        Path path;
        String string = VanillaPack.createPath(packType, resourceLocation);
        if (generatedDir != null && Files.exists(path = generatedDir.resolve(packType.getDirectory() + "/" + resourceLocation.getNamespace() + "/" + resourceLocation.getPath()), new LinkOption[0])) {
            return true;
        }
        try {
            URL uRL = VanillaPack.class.getResource(string);
            return VanillaPack.isResourceUrlValid(string, uRL);
        } catch (IOException iOException) {
            return false;
        }
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        return this.namespaces;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    @Nullable
    public <T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) throws IOException {
        try (InputStream inputStream = this.getRootResource("pack.mcmeta");){
            T t = AbstractResourcePack.getMetadataFromStream(metadataSectionSerializer, inputStream);
            return t;
        } catch (FileNotFoundException | RuntimeException exception) {
            return null;
        }
    }

    @Override
    public String getName() {
        return "Default";
    }

    @Override
    public void close() {
    }

    static {
        LOGGER = LogManager.getLogger();
        JAR_FILESYSTEM_BY_TYPE = Util.make(Maps.newHashMap(), hashMap -> {
            Class<VanillaPack> clazz = VanillaPack.class;
            synchronized (VanillaPack.class) {
                for (PackType packType : PackType.values()) {
                    URL uRL = VanillaPack.class.getResource("/" + packType.getDirectory() + "/.mcassetsroot");
                    try {
                        FileSystem fileSystem;
                        URI uRI = uRL.toURI();
                        if (!"jar".equals(uRI.getScheme())) continue;
                        try {
                            fileSystem = FileSystems.getFileSystem(uRI);
                        } catch (FileSystemNotFoundException fileSystemNotFoundException) {
                            fileSystem = FileSystems.newFileSystem(uRI, Collections.emptyMap());
                        }
                        hashMap.put(packType, fileSystem);
                    } catch (IOException | URISyntaxException exception) {
                        LOGGER.error("Couldn't get a list of all vanilla resources", (Throwable)exception);
                    }
                }
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return;
            }
        });
    }
}

