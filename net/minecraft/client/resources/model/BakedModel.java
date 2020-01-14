/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.model;

import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface BakedModel {
    public List<BakedQuad> getQuads(@Nullable BlockState var1, @Nullable Direction var2, Random var3);

    public boolean useAmbientOcclusion();

    public boolean isGui3d();

    public boolean usesBlockLight();

    public boolean isCustomRenderer();

    public TextureAtlasSprite getParticleIcon();

    public ItemTransforms getTransforms();

    public ItemOverrides getOverrides();
}

