package net.minecraft.client.resources.model;

import com.google.common.collect.Lists;
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
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class WeightedBakedModel implements BakedModel {
	private final int totalWeight;
	private final List<WeightedEntry.Wrapper<BakedModel>> list;
	private final BakedModel wrapped;

	public WeightedBakedModel(List<WeightedEntry.Wrapper<BakedModel>> list) {
		this.list = list;
		this.totalWeight = WeightedRandom.getTotalWeight(list);
		this.wrapped = (BakedModel)((WeightedEntry.Wrapper)list.get(0)).getData();
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, Random random) {
		return (List<BakedQuad>)WeightedRandom.getWeightedItem(this.list, Math.abs((int)random.nextLong()) % this.totalWeight)
			.map(wrapper -> ((BakedModel)wrapper.getData()).getQuads(blockState, direction, random))
			.orElse(Collections.emptyList());
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
		private final List<WeightedEntry.Wrapper<BakedModel>> list = Lists.<WeightedEntry.Wrapper<BakedModel>>newArrayList();

		public WeightedBakedModel.Builder add(@Nullable BakedModel bakedModel, int i) {
			if (bakedModel != null) {
				this.list.add(WeightedEntry.wrap(bakedModel, i));
			}

			return this;
		}

		@Nullable
		public BakedModel build() {
			if (this.list.isEmpty()) {
				return null;
			} else {
				return (BakedModel)(this.list.size() == 1 ? (BakedModel)((WeightedEntry.Wrapper)this.list.get(0)).getData() : new WeightedBakedModel(this.list));
			}
		}
	}
}
