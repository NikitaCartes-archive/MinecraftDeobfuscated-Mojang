/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;
import net.minecraft.FileUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class StructureManager
implements ResourceManagerReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<ResourceLocation, StructureTemplate> structureRepository = Maps.newHashMap();
    private final DataFixer fixerUpper;
    private final MinecraftServer server;
    private final Path generatedDir;

    public StructureManager(MinecraftServer minecraftServer, LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer dataFixer) {
        this.server = minecraftServer;
        this.fixerUpper = dataFixer;
        this.generatedDir = levelStorageAccess.getLevelPath(LevelResource.GENERATED_DIR).normalize();
        minecraftServer.getResources().registerReloadListener(this);
    }

    public StructureTemplate getOrCreate(ResourceLocation resourceLocation) {
        StructureTemplate structureTemplate = this.get(resourceLocation);
        if (structureTemplate == null) {
            structureTemplate = new StructureTemplate();
            this.structureRepository.put(resourceLocation, structureTemplate);
        }
        return structureTemplate;
    }

    @Nullable
    public StructureTemplate get(ResourceLocation resourceLocation2) {
        return this.structureRepository.computeIfAbsent(resourceLocation2, resourceLocation -> {
            StructureTemplate structureTemplate = this.loadFromGenerated((ResourceLocation)resourceLocation);
            return structureTemplate != null ? structureTemplate : this.loadFromResource((ResourceLocation)resourceLocation);
        });
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.structureRepository.clear();
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Nullable
    private StructureTemplate loadFromResource(ResourceLocation resourceLocation) {
        ResourceLocation resourceLocation2 = new ResourceLocation(resourceLocation.getNamespace(), "structures/" + resourceLocation.getPath() + ".nbt");
        try (Resource resource = this.server.getResources().getResource(resourceLocation2);){
            StructureTemplate structureTemplate = this.readStructure(resource.getInputStream());
            return structureTemplate;
        } catch (FileNotFoundException fileNotFoundException) {
            return null;
        } catch (Throwable throwable6) {
            LOGGER.error("Couldn't load structure {}: {}", (Object)resourceLocation, (Object)throwable6.toString());
            return null;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Nullable
    private StructureTemplate loadFromGenerated(ResourceLocation resourceLocation) {
        if (!this.generatedDir.toFile().isDirectory()) {
            return null;
        }
        Path path = this.createAndValidatePathToStructure(resourceLocation, ".nbt");
        try (FileInputStream inputStream = new FileInputStream(path.toFile());){
            StructureTemplate structureTemplate = this.readStructure(inputStream);
            return structureTemplate;
        } catch (FileNotFoundException fileNotFoundException) {
            return null;
        } catch (IOException iOException) {
            LOGGER.error("Couldn't load structure from {}", (Object)path, (Object)iOException);
            return null;
        }
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
        StructureTemplate structureTemplate = this.structureRepository.get(resourceLocation);
        if (structureTemplate == null) {
            return false;
        }
        Path path = this.createAndValidatePathToStructure(resourceLocation, ".nbt");
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

    public Path createPathToStructure(ResourceLocation resourceLocation, String string) {
        try {
            Path path = this.generatedDir.resolve(resourceLocation.getNamespace());
            Path path2 = path.resolve("structures");
            return FileUtil.createPathToResource(path2, resourceLocation.getPath(), string);
        } catch (InvalidPathException invalidPathException) {
            throw new ResourceLocationException("Invalid resource path: " + resourceLocation, invalidPathException);
        }
    }

    private Path createAndValidatePathToStructure(ResourceLocation resourceLocation, String string) {
        if (resourceLocation.getPath().contains("//")) {
            throw new ResourceLocationException("Invalid resource path: " + resourceLocation);
        }
        Path path = this.createPathToStructure(resourceLocation, string);
        if (!(path.startsWith(this.generatedDir) && FileUtil.isPathNormalized(path) && FileUtil.isPathPortable(path))) {
            throw new ResourceLocationException("Invalid resource path: " + path);
        }
        return path;
    }

    public void remove(ResourceLocation resourceLocation) {
        this.structureRepository.remove(resourceLocation);
    }
}

