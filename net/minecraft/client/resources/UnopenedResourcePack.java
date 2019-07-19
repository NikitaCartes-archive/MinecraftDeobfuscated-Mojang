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
import net.minecraft.server.packs.Pack;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.UnopenedPack;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class UnopenedResourcePack
extends UnopenedPack {
    @Nullable
    private NativeImage icon;
    @Nullable
    private ResourceLocation iconLocation;

    public UnopenedResourcePack(String string, boolean bl, Supplier<Pack> supplier, Pack pack, PackMetadataSection packMetadataSection, UnopenedPack.Position position) {
        super(string, bl, supplier, pack, packMetadataSection, position);
        NativeImage nativeImage = null;
        try (InputStream inputStream = pack.getRootResource("pack.png");){
            nativeImage = NativeImage.read(inputStream);
        } catch (IOException | IllegalArgumentException exception) {
            // empty catch block
        }
        this.icon = nativeImage;
    }

    public UnopenedResourcePack(String string, boolean bl, Supplier<Pack> supplier, Component component, Component component2, PackCompatibility packCompatibility, UnopenedPack.Position position, boolean bl2, @Nullable NativeImage nativeImage) {
        super(string, bl, supplier, component, component2, packCompatibility, position, bl2);
        this.icon = nativeImage;
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

