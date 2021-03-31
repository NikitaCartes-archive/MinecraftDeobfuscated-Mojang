/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractPackResources
implements PackResources {
    private static final Logger LOGGER = LogManager.getLogger();
    protected final File file;

    public AbstractPackResources(File file) {
        this.file = file;
    }

    private static String getPathFromLocation(PackType packType, ResourceLocation resourceLocation) {
        return String.format("%s/%s/%s", packType.getDirectory(), resourceLocation.getNamespace(), resourceLocation.getPath());
    }

    protected static String getRelativePath(File file, File file2) {
        return file.toURI().relativize(file2.toURI()).getPath();
    }

    @Override
    public InputStream getResource(PackType packType, ResourceLocation resourceLocation) throws IOException {
        return this.getResource(AbstractPackResources.getPathFromLocation(packType, resourceLocation));
    }

    @Override
    public boolean hasResource(PackType packType, ResourceLocation resourceLocation) {
        return this.hasResource(AbstractPackResources.getPathFromLocation(packType, resourceLocation));
    }

    protected abstract InputStream getResource(String var1) throws IOException;

    @Override
    public InputStream getRootResource(String string) throws IOException {
        if (string.contains("/") || string.contains("\\")) {
            throw new IllegalArgumentException("Root resources can only be filenames, not paths (no / allowed!)");
        }
        return this.getResource(string);
    }

    protected abstract boolean hasResource(String var1);

    protected void logWarning(String string) {
        LOGGER.warn("ResourcePack: ignored non-lowercase namespace: {} in {}", (Object)string, (Object)this.file);
    }

    @Override
    @Nullable
    public <T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) throws IOException {
        try (InputStream inputStream = this.getResource("pack.mcmeta");){
            T t = AbstractPackResources.getMetadataFromStream(metadataSectionSerializer, inputStream);
            return t;
        }
    }

    @Nullable
    public static <T> T getMetadataFromStream(MetadataSectionSerializer<T> metadataSectionSerializer, InputStream inputStream) {
        JsonObject jsonObject;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));){
            jsonObject = GsonHelper.parse(bufferedReader);
        } catch (JsonParseException | IOException exception) {
            LOGGER.error("Couldn't load {} metadata", (Object)metadataSectionSerializer.getMetadataSectionName(), (Object)exception);
            return null;
        }
        if (!jsonObject.has(metadataSectionSerializer.getMetadataSectionName())) {
            return null;
        }
        try {
            return metadataSectionSerializer.fromJson(GsonHelper.getAsJsonObject(jsonObject, metadataSectionSerializer.getMetadataSectionName()));
        } catch (JsonParseException jsonParseException) {
            LOGGER.error("Couldn't load {} metadata", (Object)metadataSectionSerializer.getMetadataSectionName(), (Object)jsonParseException);
            return null;
        }
    }

    @Override
    public String getName() {
        return this.file.getName();
    }
}

