package net.minecraft.client.resources.model;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class BuiltInModel implements BakedModel {
	private final ItemTransforms itemTransforms;
	private final TextureAtlasSprite particleTexture;
	private final boolean usesBlockLight;

	public BuiltInModel(ItemTransforms itemTransforms, TextureAtlasSprite textureAtlasSprite, boolean bl) {
		this.itemTransforms = itemTransforms;
		this.particleTexture = textureAtlasSprite;
		this.usesBlockLight = bl;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, RandomSource randomSource) {
		return Collections.emptyList();
	}

	@Override
	public boolean useAmbientOcclusion() {
		return false;
	}

	@Override
	public boolean isGui3d() {
		return true;
	}

	@Override
	public boolean usesBlockLight() {
		return this.usesBlockLight;
	}

	@Override
	public boolean isCustomRenderer() {
		return true;
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return this.particleTexture;
	}

	@Override
	public ItemTransforms getTransforms() {
		return this.itemTransforms;
	}
}
