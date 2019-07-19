/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

@Environment(value=EnvType.CLIENT)
public interface SpriteSet {
    public TextureAtlasSprite get(int var1, int var2);

    public TextureAtlasSprite get(Random var1);
}

