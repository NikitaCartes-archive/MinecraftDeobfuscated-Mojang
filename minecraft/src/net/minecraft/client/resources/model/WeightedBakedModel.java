package net.minecraft.client.resources.model;

import com.google.common.collect.Lists;
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
import net.minecraft.util.WeighedRandom;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class WeightedBakedModel implements BakedModel {
	private final int totalWeight;
	private final List<WeightedBakedModel.WeightedModel> list;
	private final BakedModel wrapped;

	public WeightedBakedModel(List<WeightedBakedModel.WeightedModel> list) {
		this.list = list;
		this.totalWeight = WeighedRandom.getTotalWeight(list);
		this.wrapped = ((WeightedBakedModel.WeightedModel)list.get(0)).model;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, Random random) {
		return WeighedRandom.getWeightedItem(this.list, Math.abs((int)random.nextLong()) % this.totalWeight).model.getQuads(blockState, direction, random);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return this.wrapped.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return this.wrapped.isGui3d();
	}

	@Override
	public boolean usesBlockLight() {
		return this.wrapped.usesBlockLight();
	}

	@Override
	public boolean isCustomRenderer() {
		return this.wrapped.isCustomRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return this.wrapped.getParticleIcon();
	}

	@Override
	public ItemTransforms getTransforms() {
		return this.wrapped.getTransforms();
	}

	@Override
	public ItemOverrides getOverrides() {
		return this.wrapped.getOverrides();
	}

	@Environment(EnvType.CLIENT)
	public static class Builder {
		private final List<WeightedBakedModel.WeightedModel> list = Lists.<WeightedBakedModel.WeightedModel>newArrayList();

		public WeightedBakedModel.Builder add(@Nullable BakedModel bakedModel, int i) {
			if (bakedModel != null) {
				this.list.add(new WeightedBakedModel.WeightedModel(bakedModel, i));
			}

			return this;
		}

		@Nullable
		public BakedModel build() {
			if (this.list.isEmpty()) {
				return null;
			} else {
				return (BakedModel)(this.list.size() == 1 ? ((WeightedBakedModel.WeightedModel)this.list.get(0)).model : new WeightedBakedModel(this.list));
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static class WeightedModel extends WeighedRandom.WeighedRandomItem {
		protected final BakedModel model;

		public WeightedModel(BakedModel bakedModel, int i) {
			super(i);
			this.model = bakedModel;
		}
	}
}
