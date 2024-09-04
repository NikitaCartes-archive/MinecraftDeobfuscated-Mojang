package net.minecraft.client.resources.model;

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
public abstract class DelegateBakedModel implements BakedModel {
	protected final BakedModel parent;

	public DelegateBakedModel(BakedModel bakedModel) {
		this.parent = bakedModel;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, RandomSource randomSource) {
		return this.parent.getQuads(blockState, direction, randomSource);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return this.parent.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return this.parent.isGui3d();
	}

	@Override
	public boolean usesBlockLight() {
		return this.parent.usesBlockLight();
	}

	@Override
	public boolean isCustomRenderer() {
		return this.parent.isCustomRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return this.parent.getParticleIcon();
	}

	@Override
	public ItemTransforms getTransforms() {
		return this.parent.getTransforms();
	}
}
