package net.minecraft.client.resources.model;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class BuiltInModel implements BakedModel {
	private final ItemTransforms itemTransforms;
	private final ItemOverrides overrides;
	private final TextureAtlasSprite particleTexture;
	private final boolean usesBlockLight;

	public BuiltInModel(ItemTransforms itemTransforms, ItemOverrides itemOverrides, TextureAtlasSprite textureAtlasSprite, boolean bl) {
		this.itemTransforms = itemTransforms;
		this.overrides = itemOverrides;
		this.particleTexture = textureAtlasSprite;
		this.usesBlockLight = bl;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, Random random) {
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

	@Override
	public ItemOverrides getOverrides() {
		return this.overrides;
	}
}
