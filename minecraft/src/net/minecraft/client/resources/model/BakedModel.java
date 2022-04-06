package net.minecraft.client.resources.model;

import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public interface BakedModel {
	List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, RandomSource randomSource);

	boolean useAmbientOcclusion();

	boolean isGui3d();

	boolean usesBlockLight();

	boolean isCustomRenderer();

	TextureAtlasSprite getParticleIcon();

	ItemTransforms getTransforms();

	ItemOverrides getOverrides();
}
