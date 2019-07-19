/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class EmptyGlyph
extends BakedGlyph {
    public EmptyGlyph() {
        super(new ResourceLocation(""), 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    }

    @Override
    public void render(TextureManager textureManager, boolean bl, float f, float g, BufferBuilder bufferBuilder, float h, float i, float j, float k) {
    }

    @Override
    @Nullable
    public ResourceLocation getTexture() {
        return null;
    }
}

