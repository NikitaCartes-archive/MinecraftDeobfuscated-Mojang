/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class VanillaPackResources
implements PackResources,
ResourceProvider {
    public static Path generatedDir;
    private static final Logger LOGGER;
    public static Class<?> clientObject;
    private static final Map<PackType, FileSystem> JAR_FILESYSTEM_BY_TYPE;
    public final PackMetadataSection packMetadata;
    public final Set<String> namespaces;

    public VanillaPackResources(PackMetadataSection packMetadataSection, String ... strings) {
        this.packMetadata = packMetadataSection;
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
    public Collection<ResourceLocation> getResources(PackType packType, String string, String string2, int i, Predicate<String> predicate) {
        URI uRI;
        HashSet<ResourceLocation> set = Sets.newHashSet();
        if (generatedDir != null) {
            try {
                VanillaPackResources.getResources(set, i, string, generatedDir.resolve(packType.getDirectory()), string2, predicate);
            } catch (IOException iOException) {
                // empty catch block
            }
            if (packType == PackType.CLIENT_RESOURCES) {
                Enumeration<URL> enumeration = null;
                try {
                    enumeration = clientObject.getClassLoader().getResources(packType.getDirectory() + "/");
                } catch (IOException iOException) {
                    // empty catch block
                }
                while (enumeration != null && enumeration.hasMoreElements()) {
                    try {
                        uRI = enumeration.nextElement().toURI();
                        if (!"file".equals(uRI.getScheme())) continue;
                        VanillaPackResources.getResources(set, i, string, Paths.get(uRI), string2, predicate);
                    } catch (IOException | URISyntaxException uRI2) {}
                }
            }
        }
        try {
            URL uRL = VanillaPackResources.class.getResource("/" + packType.getDirectory() + "/.mcassetsroot");
            if (uRL == null) {
                LOGGER.error("Couldn't find .mcassetsroot, cannot load vanilla resources");
                return set;
            }
            uRI = uRL.toURI();
            if ("file".equals(uRI.getScheme())) {
                URL uRL2 = new URL(uRL.toString().substring(0, uRL.toString().length() - ".mcassetsroot".length()));
                Path path = Paths.get(uRL2.toURI());
                VanillaPackResources.getResources(set, i, string, path, string2, predicate);
            } else if ("jar".equals(uRI.getScheme())) {
                Path path2 = JAR_FILESYSTEM_BY_TYPE.get((Object)packType).getPath("/" + packType.getDirectory(), new String[0]);
                VanillaPackResources.getResources(set, i, "minecraft", path2, string2, predicate);
            } else {
                LOGGER.error("Unsupported scheme {} trying to list vanilla resources (NYI?)", (Object)uRI);
            }
        } catch (FileNotFoundException | NoSuchFileException uRL) {
        } catch (IOException | URISyntaxException exception) {
            LOGGER.error("Couldn't get a list of all vanilla resources", (Throwable)exception);
        }
        return set;
    }

    private static void getResources(Collection<ResourceLocation> collection, int i, String string, Path path3, String string2, Predicate<String> predicate) throws IOException {
        Path path22 = path3.resolve(string);
        try (Stream<Path> stream = Files.walk(path22.resolve(string2), i, new FileVisitOption[0]);){
            stream.filter(path -> !path.endsWith(".mcmeta") && Files.isRegularFile(path, new LinkOption[0]) && predicate.test(path.getFileName().toString())).map(path2 -> new ResourceLocation(string, path22.relativize((Path)path2).toString().replaceAll("\\\\", "/"))).forEach(collection::add);
        }
    }

    @Nullable
    protected InputStream getResourceAsStream(PackType packType, ResourceLocation resourceLocation) {
        Path path;
        String string = VanillaPackResources.createPath(packType, resourceLocation);
        if (generatedDir != null && Files.exists(path = generatedDir.resolve(packType.getDirectory() + "/" + resourceLocation.getNamespace() + "/" + resourceLocation.getPath()), new LinkOption[0])) {
            try {
                return Files.newInputStream(path, new OpenOption[0]);
            } catch (IOException iOException) {
                // empty catch block
            }
        }
        try {
            URL uRL = VanillaPackResources.class.getResource(string);
            if (VanillaPackResources.isResourceUrlValid(string, uRL)) {
                return uRL.openStream();
            }
        } catch (IOException iOException) {
            return VanillaPackResources.class.getResourceAsStream(string);
        }
        return null;
    }

    private static String createPath(PackType packType, ResourceLocation resourceLocation) {
        return "/" + packType.getDirectory() + "/" + resourceLocation.getNamespace() + "/" + resourceLocation.getPath();
    }

    private static boolean isResourceUrlValid(String string, @Nullable URL uRL) throws IOException {
        return uRL != null && (uRL.getProtocol().equals("jar") || FolderPackResources.validatePath(new File(uRL.getFile()), string));
    }

    @Nullable
    protected InputStream getResourceAsStream(String string) {
        return VanillaPackResources.class.getResourceAsStream("/" + string);
    }

    @Override
    public boolean hasResource(PackType packType, ResourceLocation resourceLocation) {
        Path path;
        String string = VanillaPackResources.createPath(packType, resourceLocation);
        if (generatedDir != null && Files.exists(path = generatedDir.resolve(packType.getDirectory() + "/" + resourceLocation.getNamespace() + "/" + resourceLocation.getPath()), new LinkOption[0])) {
            return true;
        }
        try {
            URL uRL = VanillaPackResources.class.getResource(string);
            return VanillaPackResources.isResourceUrlValid(string, uRL);
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
            T object;
            if (inputStream != null && (object = AbstractPackResources.getMetadataFromStream(metadataSectionSerializer, inputStream)) != null) {
                T t = object;
                return t;
            }
        } catch (FileNotFoundException | RuntimeException exception) {
            // empty catch block
        }
        if (metadataSectionSerializer != PackMetadataSection.SERIALIZER) return null;
        return (T)this.packMetadata;
    }

    @Override
    public String getName() {
        return "Default";
    }

    @Override
    public void close() {
    }

    @Override
    public Resource getResource(final ResourceLocation resourceLocation) throws IOException {
        return new Resource(){
            @Nullable
            InputStream inputStream;

            @Override
            public void close() throws IOException {
                if (this.inputStream != null) {
                    this.inputStream.close();
                }
            }

            @Override
            public ResourceLocation getLocation() {
                return resourceLocation;
            }

            @Override
            public InputStream getInputStream() {
                try {
                    this.inputStream = VanillaPackResources.this.getResource(PackType.CLIENT_RESOURCES, resourceLocation);
                } catch (IOException iOException) {
                    throw new UncheckedIOException("Could not get client resource from vanilla pack", iOException);
                }
                return this.inputStream;
            }

            @Override
            public boolean hasMetadata() {
                return false;
            }

            @Override
            @Nullable
            public <T> T getMetadata(MetadataSectionSerializer<T> metadataSectionSerializer) {
                return null;
            }

            @Override
            public String getSourceName() {
                return resourceLocation.toString();
            }
        };
    }

    static {
        LOGGER = LogManager.getLogger();
        JAR_FILESYSTEM_BY_TYPE = Util.make(Maps.newHashMap(), hashMap -> {
            Class<VanillaPackResources> clazz = VanillaPackResources.class;
            synchronized (VanillaPackResources.class) {
                for (PackType packType : PackType.values()) {
                    URL uRL = VanillaPackResources.class.getResource("/" + packType.getDirectory() + "/.mcassetsroot");
                    try {
                        FileSystem fileSystem;
                        URI uRI = uRL.toURI();
                        if (!"jar".equals(uRI.getScheme())) continue;
                        try {
                            fileSystem = FileSystems.getFileSystem(uRI);
                        } catch (Exception exception) {
                            fileSystem = FileSystems.newFileSystem(uRI, Collections.emptyMap());
                        }
                        hashMap.put(packType, fileSystem);
                    } catch (IOException | URISyntaxException exception2) {
                        LOGGER.error("Couldn't get a list of all vanilla resources", (Throwable)exception2);
                    }
                }
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return;
            }
        });
    }
}

