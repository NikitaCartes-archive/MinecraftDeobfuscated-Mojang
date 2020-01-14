package net.minecraft.client.resources.model;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

@Environment(EnvType.CLIENT)
public class MultiPartBakedModel implements BakedModel {
	private final List<Pair<Predicate<BlockState>, BakedModel>> selectors;
	protected final boolean hasAmbientOcclusion;
	protected final boolean isGui3d;
	protected final boolean usesBlockLight;
	protected final TextureAtlasSprite particleIcon;
	protected final ItemTransforms transforms;
	protected final ItemOverrides overrides;
	private final Map<BlockState, BitSet> selectorCache = new Object2ObjectOpenCustomHashMap<>(Util.identityStrategy());

	public MultiPartBakedModel(List<Pair<Predicate<BlockState>, BakedModel>> list) {
		this.selectors = list;
		BakedModel bakedModel = (BakedModel)((Pair)list.iterator().next()).getRight();
		this.hasAmbientOcclusion = bakedModel.useAmbientOcclusion();
		this.isGui3d = bakedModel.isGui3d();
		this.usesBlockLight = bakedModel.usesBlockLight();
		this.particleIcon = bakedModel.getParticleIcon();
		this.transforms = bakedModel.getTransforms();
		this.overrides = bakedModel.getOverrides();
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, Random random) {
		if (blockState == null) {
			return Collections.emptyList();
		} else {
			BitSet bitSet = (BitSet)this.selectorCache.get(blockState);
			if (bitSet == null) {
				bitSet = new BitSet();

				for (int i = 0; i < this.selectors.size(); i++) {
					Pair<Predicate<BlockState>, BakedModel> pair = (Pair<Predicate<BlockState>, BakedModel>)this.selectors.get(i);
					if (pair.getLeft().test(blockState)) {
						bitSet.set(i);
					}
				}

				this.selectorCache.put(blockState, bitSet);
			}

			List<BakedQuad> list = Lists.<BakedQuad>newArrayList();
			long l = random.nextLong();

			for (int j = 0; j < bitSet.length(); j++) {
				if (bitSet.get(j)) {
					list.addAll(((BakedModel)((Pair)this.selectors.get(j)).getRight()).getQuads(blockState, direction, new Random(l)));
				}
			}

			return list;
		}
	}

	@Override
	public boolean useAmbientOcclusion() {
		return this.hasAmbientOcclusion;
	}

	@Override
	public boolean isGui3d() {
		return this.isGui3d;
	}

	@Override
	public boolean usesBlockLight() {
		return this.usesBlockLight;
	}

	@Override
	public boolean isCustomRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return this.particleIcon;
	}

	@Override
	public ItemTransforms getTransforms() {
		return this.transforms;
	}

	@Override
	public ItemOverrides getOverrides() {
		return this.overrides;
	}

	@Environment(EnvType.CLIENT)
	public static class Builder {
		private final List<Pair<Predicate<BlockState>, BakedModel>> selectors = Lists.<Pair<Predicate<BlockState>, BakedModel>>newArrayList();

		public void add(Predicate<BlockState> predicate, BakedModel bakedModel) {
			this.selectors.add(Pair.of(predicate, bakedModel));
		}

		public BakedModel build() {
			return new MultiPartBakedModel(this.selectors);
		}
	}
}
