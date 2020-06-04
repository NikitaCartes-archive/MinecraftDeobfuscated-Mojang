/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ResourcePack
extends Pack {
    @Nullable
    private NativeImage icon;
    @Nullable
    private ResourceLocation iconLocation;

    public ResourcePack(String string, boolean bl, Supplier<PackResources> supplier, PackResources packResources, PackMetadataSection packMetadataSection, Pack.Position position, PackSource packSource) {
        super(string, bl, supplier, packResources, packMetadataSection, position, packSource);
        this.icon = ResourcePack.readIcon(packResources);
    }

    public ResourcePack(String string, boolean bl, Supplier<PackResources> supplier, Component component, Component component2, PackCompatibility packCompatibility, Pack.Position position, boolean bl2, PackSource packSource, @Nullable NativeImage nativeImage) {
        super(string, bl, supplier, component, component2, packCompatibility, position, bl2, packSource);
        this.icon = nativeImage;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Nullable
    public static NativeImage readIcon(PackResources packResources) {
        try (InputStream inputStream = packResources.getRootResource("pack.png");){
            NativeImage nativeImage = NativeImage.read(inputStream);
            return nativeImage;
        } catch (IOException | IllegalArgumentException exception) {
            return null;
        }
    }

    public void bindIcon(TextureManager textureManager) {
        if (this.iconLocation == null) {
            this.iconLocation = this.icon == null ? new ResourceLocation("textures/misc/unknown_pack.png") : textureManager.register("texturepackicon", new DynamicTexture(this.icon));
        }
        textureManager.bind(this.iconLocation);
    }

    @Override
    public void close() {
        super.close();
        if (this.icon != null) {
            this.icon.close();
            this.icon = null;
        }
    }
}

