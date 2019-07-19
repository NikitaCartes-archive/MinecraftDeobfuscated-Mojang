/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.metadata.texture;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSectionSerializer;

@Environment(value=EnvType.CLIENT)
public class TextureMetadataSection {
    public static final TextureMetadataSectionSerializer SERIALIZER = new TextureMetadataSectionSerializer();
    private final boolean blur;
    private final boolean clamp;

    public TextureMetadataSection(boolean bl, boolean bl2) {
        this.blur = bl;
        this.clamp = bl2;
    }

    public boolean isBlur() {
        return this.blur;
    }

    public boolean isClamp() {
        return this.clamp;
    }
}

