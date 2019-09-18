/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.block.model;

import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

@Environment(value=EnvType.CLIENT)
public class BreakingQuad
extends BakedQuad {
    private final TextureAtlasSprite breakingIcon;

    public BreakingQuad(BakedQuad bakedQuad, TextureAtlasSprite textureAtlasSprite) {
        super(Arrays.copyOf(bakedQuad.getVertices(), bakedQuad.getVertices().length), bakedQuad.tintIndex, FaceBakery.calculateFacing(bakedQuad.getVertices()), bakedQuad.getSprite());
        this.breakingIcon = textureAtlasSprite;
        this.calculateBreakingUVs();
    }

    private void calculateBreakingUVs() {
        for (int i = 0; i < 4; ++i) {
            int j = 8 * i;
            this.vertices[j + 4] = Float.floatToRawIntBits(this.breakingIcon.getU(this.sprite.getUOffset(Float.intBitsToFloat(this.vertices[j + 4]))));
            this.vertices[j + 4 + 1] = Float.floatToRawIntBits(this.breakingIcon.getV(this.sprite.getVOffset(Float.intBitsToFloat(this.vertices[j + 4 + 1]))));
        }
    }
}

