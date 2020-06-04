/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.AssetIndex;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DefaultClientPackResources
extends VanillaPackResources {
    private final AssetIndex assetIndex;

    public DefaultClientPackResources(AssetIndex assetIndex) {
        super("minecraft", "realms");
        this.assetIndex = assetIndex;
    }

    @Override
    @Nullable
    protected InputStream getResourceAsStream(PackType packType, ResourceLocation resourceLocation) {
        File file;
        if (packType == PackType.CLIENT_RESOURCES && (file = this.assetIndex.getFile(resourceLocation)) != null && file.exists()) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException fileNotFoundException) {
                // empty catch block
            }
        }
        return super.getResourceAsStream(packType, resourceLocation);
    }

    @Override
    public boolean hasResource(PackType packType, ResourceLocation resourceLocation) {
        File file;
        if (packType == PackType.CLIENT_RESOURCES && (file = this.assetIndex.getFile(resourceLocation)) != null && file.exists()) {
            return true;
        }
        return super.hasResource(packType, resourceLocation);
    }

    @Override
    @Nullable
    protected InputStream getResourceAsStream(String string) {
        File file = this.assetIndex.getRootFile(string);
        if (file != null && file.exists()) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException fileNotFoundException) {
                // empty catch block
            }
        }
        return super.getResourceAsStream(string);
    }

    @Override
    public Collection<ResourceLocation> getResources(PackType packType, String string, String string2, int i, Predicate<String> predicate) {
        Collection<ResourceLocation> collection = super.getResources(packType, string, string2, i, predicate);
        collection.addAll(this.assetIndex.getFiles(string2, string, i, predicate));
        return collection;
    }
}

