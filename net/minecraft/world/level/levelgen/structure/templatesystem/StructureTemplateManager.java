/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.FileUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class StructureTemplateManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String STRUCTURE_DIRECTORY_NAME = "structures";
    private static final String TEST_STRUCTURES_DIR = "gameteststructures";
    private static final String STRUCTURE_FILE_EXTENSION = ".nbt";
    private static final String STRUCTURE_TEXT_FILE_EXTENSION = ".snbt";
    private final Map<ResourceLocation, Optional<StructureTemplate>> structureRepository = Maps.newConcurrentMap();
    private final DataFixer fixerUpper;
    private ResourceManager resourceManager;
    private final Path generatedDir;
    private final List<Source> sources;

    public StructureTemplateManager(ResourceManager resourceManager, LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer dataFixer) {
        this.resourceManager = resourceManager;
        this.fixerUpper = dataFixer;
        this.generatedDir = levelStorageAccess.getLevelPath(LevelResource.GENERATED_DIR).normalize();
        ImmutableList.Builder builder = ImmutableList.builder();
        builder.add(new Source(this::loadFromGenerated, this::listGenerated));
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            builder.add(new Source(this::loadFromTestStructures, this::listTestStructures));
        }
        builder.add(new Source(this::loadFromResource, this::listResources));
        this.sources = builder.build();
    }

    public StructureTemplate getOrCreate(ResourceLocation resourceLocation) {
        Optional<StructureTemplate> optional = this.get(resourceLocation);
        if (optional.isPresent()) {
            return optional.get();
        }
        StructureTemplate structureTemplate = new StructureTemplate();
        this.structureRepository.put(resourceLocation, Optional.of(structureTemplate));
        return structureTemplate;
    }

    public Optional<StructureTemplate> get(ResourceLocation resourceLocation) {
        return this.structureRepository.computeIfAbsent(resourceLocation, this::tryLoad);
    }

    public Stream<ResourceLocation> listTemplates() {
        return this.sources.stream().flatMap(source -> source.lister().get()).distinct();
    }

    private Optional<StructureTemplate> tryLoad(ResourceLocation resourceLocation) {
        for (Source source : this.sources) {
            try {
                Optional<StructureTemplate> optional = source.loader().apply(resourceLocation);
                if (!optional.isPresent()) continue;
                return optional;
            } catch (Exception exception) {
            }
        }
        return Optional.empty();
    }

    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        this.structureRepository.clear();
    }

    private Optional<StructureTemplate> loadFromResource(ResourceLocation resourceLocation) {
        ResourceLocation resourceLocation2 = new ResourceLocation(resourceLocation.getNamespace(), "structures/" + resourceLocation.getPath() + STRUCTURE_FILE_EXTENSION);
        return this.load(() -> this.resourceManager.open(resourceLocation2), throwable -> LOGGER.error("Couldn't load structure {}", (Object)resourceLocation, throwable));
    }

    private Stream<ResourceLocation> listResources() {
        return this.resourceManager.listResources(STRUCTURE_DIRECTORY_NAME, resourceLocation -> true).keySet().stream().map(resourceLocation -> new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath().substring(STRUCTURE_DIRECTORY_NAME.length() + 1, resourceLocation.getPath().length() - STRUCTURE_FILE_EXTENSION.length())));
    }

    private Optional<StructureTemplate> loadFromTestStructures(ResourceLocation resourceLocation) {
        return this.loadFromSnbt(resourceLocation, Paths.get(TEST_STRUCTURES_DIR, new String[0]));
    }

    private Stream<ResourceLocation> listTestStructures() {
        return this.listFolderContents(Paths.get(TEST_STRUCTURES_DIR, new String[0]), "minecraft", STRUCTURE_TEXT_FILE_EXTENSION);
    }

    private Optional<StructureTemplate> loadFromGenerated(ResourceLocation resourceLocation) {
        if (!Files.isDirectory(this.generatedDir, new LinkOption[0])) {
            return Optional.empty();
        }
        Path path = StructureTemplateManager.createAndValidatePathToStructure(this.generatedDir, resourceLocation, STRUCTURE_FILE_EXTENSION);
        return this.load(() -> new FileInputStream(path.toFile()), throwable -> LOGGER.error("Couldn't load structure from {}", (Object)path, throwable));
    }

    private Stream<ResourceLocation> listGenerated() {
        if (!Files.isDirectory(this.generatedDir, new LinkOption[0])) {
            return Stream.empty();
        }
        try {
            return Files.list(this.generatedDir).filter(path -> Files.isDirectory(path, new LinkOption[0])).flatMap(path -> this.listGeneratedInNamespace((Path)path));
        } catch (IOException iOException) {
            return Stream.empty();
        }
    }

    private Stream<ResourceLocation> listGeneratedInNamespace(Path path) {
        Path path2 = path.resolve(STRUCTURE_DIRECTORY_NAME);
        return this.listFolderContents(path2, path.getFileName().toString(), STRUCTURE_FILE_EXTENSION);
    }

    private Stream<ResourceLocation> listFolderContents(Path path3, String string2, String string22) {
        if (!Files.isDirectory(path3, new LinkOption[0])) {
            return Stream.empty();
        }
        int i = string22.length();
        Function<String, String> function = string -> string.substring(0, string.length() - i);
        try {
            return Files.walk(path3, new FileVisitOption[0]).filter(path -> path.toString().endsWith(string22)).mapMulti((path2, consumer) -> {
                try {
                    consumer.accept(new ResourceLocation(string2, (String)function.apply(this.relativize(path3, (Path)path2))));
                } catch (ResourceLocationException resourceLocationException) {
                    LOGGER.error("Invalid location while listing pack contents", resourceLocationException);
                }
            });
        } catch (IOException iOException) {
            LOGGER.error("Failed to list folder contents", iOException);
            return Stream.empty();
        }
    }

    private String relativize(Path path, Path path2) {
        return path.relativize(path2).toString().replace(File.separator, "/");
    }

    private Optional<StructureTemplate> loadFromSnbt(ResourceLocation resourceLocation, Path path) {
        Optional<StructureTemplate> optional;
        block10: {
            if (!Files.isDirectory(path, new LinkOption[0])) {
                return Optional.empty();
            }
            Path path2 = FileUtil.createPathToResource(path, resourceLocation.getPath(), STRUCTURE_TEXT_FILE_EXTENSION);
            BufferedReader bufferedReader = Files.newBufferedReader(path2);
            try {
                String string = IOUtils.toString(bufferedReader);
                optional = Optional.of(this.readStructure(NbtUtils.snbtToStructure(string)));
                if (bufferedReader == null) break block10;
            } catch (Throwable throwable) {
                try {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                } catch (NoSuchFileException noSuchFileException) {
                    return Optional.empty();
                } catch (CommandSyntaxException | IOException exception) {
                    LOGGER.error("Couldn't load structure from {}", (Object)path2, (Object)exception);
                    return Optional.empty();
                }
            }
            bufferedReader.close();
        }
        return optional;
    }

    private Optional<StructureTemplate> load(InputStreamOpener inputStreamOpener, Consumer<Throwable> consumer) {
        Optional<StructureTemplate> optional;
        block9: {
            InputStream inputStream = inputStreamOpener.open();
            try {
                optional = Optional.of(this.readStructure(inputStream));
                if (inputStream == null) break block9;
            } catch (Throwable throwable) {
                try {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                } catch (FileNotFoundException fileNotFoundException) {
                    return Optional.empty();
                } catch (Throwable throwable3) {
                    consumer.accept(throwable3);
                    return Optional.empty();
                }
            }
            inputStream.close();
        }
        return optional;
    }

    private StructureTemplate readStructure(InputStream inputStream) throws IOException {
        CompoundTag compoundTag = NbtIo.readCompressed(inputStream);
        return this.readStructure(compoundTag);
    }

    public StructureTemplate readStructure(CompoundTag compoundTag) {
        if (!compoundTag.contains("DataVersion", 99)) {
            compoundTag.putInt("DataVersion", 500);
        }
        StructureTemplate structureTemplate = new StructureTemplate();
        structureTemplate.load(NbtUtils.update(this.fixerUpper, DataFixTypes.STRUCTURE, compoundTag, compoundTag.getInt("DataVersion")));
        return structureTemplate;
    }

    public boolean save(ResourceLocation resourceLocation) {
        Optional<StructureTemplate> optional = this.structureRepository.get(resourceLocation);
        if (!optional.isPresent()) {
            return false;
        }
        StructureTemplate structureTemplate = optional.get();
        Path path = StructureTemplateManager.createAndValidatePathToStructure(this.generatedDir, resourceLocation, STRUCTURE_FILE_EXTENSION);
        Path path2 = path.getParent();
        if (path2 == null) {
            return false;
        }
        try {
            Files.createDirectories(Files.exists(path2, new LinkOption[0]) ? path2.toRealPath(new LinkOption[0]) : path2, new FileAttribute[0]);
        } catch (IOException iOException) {
            LOGGER.error("Failed to create parent directory: {}", (Object)path2);
            return false;
        }
        CompoundTag compoundTag = structureTemplate.save(new CompoundTag());
        try (FileOutputStream outputStream = new FileOutputStream(path.toFile());){
            NbtIo.writeCompressed(compoundTag, outputStream);
        } catch (Throwable throwable) {
            return false;
        }
        return true;
    }

    public Path getPathToGeneratedStructure(ResourceLocation resourceLocation, String string) {
        return StructureTemplateManager.createPathToStructure(this.generatedDir, resourceLocation, string);
    }

    public static Path createPathToStructure(Path path, ResourceLocation resourceLocation, String string) {
        try {
            Path path2 = path.resolve(resourceLocation.getNamespace());
            Path path3 = path2.resolve(STRUCTURE_DIRECTORY_NAME);
            return FileUtil.createPathToResource(path3, resourceLocation.getPath(), string);
        } catch (InvalidPathException invalidPathException) {
            throw new ResourceLocationException("Invalid resource path: " + resourceLocation, invalidPathException);
        }
    }

    private static Path createAndValidatePathToStructure(Path path, ResourceLocation resourceLocation, String string) {
        if (resourceLocation.getPath().contains("//")) {
            throw new ResourceLocationException("Invalid resource path: " + resourceLocation);
        }
        Path path2 = StructureTemplateManager.createPathToStructure(path, resourceLocation, string);
        if (!(path2.startsWith(path) && FileUtil.isPathNormalized(path2) && FileUtil.isPathPortable(path2))) {
            throw new ResourceLocationException("Invalid resource path: " + path2);
        }
        return path2;
    }

    public void remove(ResourceLocation resourceLocation) {
        this.structureRepository.remove(resourceLocation);
    }

    record Source(Function<ResourceLocation, Optional<StructureTemplate>> loader, Supplier<Stream<ResourceLocation>> lister) {
    }

    @FunctionalInterface
    static interface InputStreamOpener {
        public InputStream open() throws IOException;
    }
}

