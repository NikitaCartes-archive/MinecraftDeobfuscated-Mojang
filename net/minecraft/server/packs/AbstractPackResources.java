/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class AbstractPackResources
implements PackResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String name;
    private final boolean isBuiltin;

    protected AbstractPackResources(String string, boolean bl) {
        this.name = string;
        this.isBuiltin = bl;
    }

    @Override
    @Nullable
    public <T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) throws IOException {
        IoSupplier<InputStream> ioSupplier = this.getRootResource("pack.mcmeta");
        if (ioSupplier == null) {
            return null;
        }
        try (InputStream inputStream = ioSupplier.get();){
            T t = AbstractPackResources.getMetadataFromStream(metadataSectionSerializer, inputStream);
            return t;
        }
    }

    @Nullable
    public static <T> T getMetadataFromStream(MetadataSectionSerializer<T> metadataSectionSerializer, InputStream inputStream) {
        JsonObject jsonObject;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));){
            jsonObject = GsonHelper.parse(bufferedReader);
        } catch (Exception exception) {
            LOGGER.error("Couldn't load {} metadata", (Object)metadataSectionSerializer.getMetadataSectionName(), (Object)exception);
            return null;
        }
        if (!jsonObject.has(metadataSectionSerializer.getMetadataSectionName())) {
            return null;
        }
        try {
            return metadataSectionSerializer.fromJson(GsonHelper.getAsJsonObject(jsonObject, metadataSectionSerializer.getMetadataSectionName()));
        } catch (Exception exception) {
            LOGGER.error("Couldn't load {} metadata", (Object)metadataSectionSerializer.getMetadataSectionName(), (Object)exception);
            return null;
        }
    }

    @Override
    public String packId() {
        return this.name;
    }

    @Override
    public boolean isBuiltin() {
        return this.isBuiltin;
    }
}

