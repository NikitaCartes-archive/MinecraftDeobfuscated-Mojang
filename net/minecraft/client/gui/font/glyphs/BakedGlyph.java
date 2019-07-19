/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BakedGlyph {
    private final ResourceLocation texture;
    private final float u0;
    private final float u1;
    private final float v0;
    private final float v1;
    private final float left;
    private final float right;
    private final float up;
    private final float down;

    public BakedGlyph(ResourceLocation resourceLocation, float f, float g, float h, float i, float j, float k, float l, float m) {
        this.texture = resourceLocation;
        this.u0 = f;
        this.u1 = g;
        this.v0 = h;
        this.v1 = i;
        this.left = j;
        this.right = k;
        this.up = l;
        this.down = m;
    }

    public void render(TextureManager textureManager, boolean bl, float f, float g, BufferBuilder bufferBuilder, float h, float i, float j, float k) {
        int l = 3;
        float m = f + this.left;
        float n = f + this.right;
        float o = this.up - 3.0f;
        float p = this.down - 3.0f;
        float q = g + o;
        float r = g + p;
        float s = bl ? 1.0f - 0.25f * o : 0.0f;
        float t = bl ? 1.0f - 0.25f * p : 0.0f;
        bufferBuilder.vertex(m + s, q, 0.0).uv(this.u0, this.v0).color(h, i, j, k).endVertex();
        bufferBuilder.vertex(m + t, r, 0.0).uv(this.u0, this.v1).color(h, i, j, k).endVertex();
        bufferBuilder.vertex(n + t, r, 0.0).uv(this.u1, this.v1).color(h, i, j, k).endVertex();
        bufferBuilder.vertex(n + s, q, 0.0).uv(this.u1, this.v0).color(h, i, j, k).endVertex();
    }

    @Nullable
    public ResourceLocation getTexture() {
        return this.texture;
    }
}

