package net.minecraft.client.resources.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class SimpleBakedModel implements BakedModel {
	protected final List<BakedQuad> unculledFaces;
	protected final Map<Direction, List<BakedQuad>> culledFaces;
	protected final boolean hasAmbientOcclusion;
	protected final boolean isGui3d;
	protected final boolean usesBlockLight;
	protected final TextureAtlasSprite particleIcon;
	protected final ItemTransforms transforms;

	public SimpleBakedModel(
		List<BakedQuad> list,
		Map<Direction, List<BakedQuad>> map,
		boolean bl,
		boolean bl2,
		boolean bl3,
		TextureAtlasSprite textureAtlasSprite,
		ItemTransforms itemTransforms
	) {
		this.unculledFaces = list;
		this.culledFaces = map;
		this.hasAmbientOcclusion = bl;
		this.isGui3d = bl3;
		this.usesBlockLight = bl2;
		this.particleIcon = textureAtlasSprite;
		this.transforms = itemTransforms;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, RandomSource randomSource) {
		return direction == null ? this.unculledFaces : (List)this.culledFaces.get(direction);
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

	@Environment(EnvType.CLIENT)
	public static class Builder {
		private final ImmutableList.Builder<BakedQuad> unculledFaces = ImmutableList.builder();
		private final EnumMap<Direction, ImmutableList.Builder<BakedQuad>> culledFaces = Maps.newEnumMap(Direction.class);
		private final boolean hasAmbientOcclusion;
		@Nullable
		private TextureAtlasSprite particleIcon;
		private final boolean usesBlockLight;
		private final boolean isGui3d;
		private final ItemTransforms transforms;

		public Builder(BlockModel blockModel, boolean bl) {
			this(blockModel.hasAmbientOcclusion(), blockModel.getGuiLight().lightLikeBlock(), bl, blockModel.getTransforms());
		}

		private Builder(boolean bl, boolean bl2, boolean bl3, ItemTransforms itemTransforms) {
			this.hasAmbientOcclusion = bl;
			this.usesBlockLight = bl2;
			this.isGui3d = bl3;
			this.transforms = itemTransforms;

			for (Direction direction : Direction.values()) {
				this.culledFaces.put(direction, ImmutableList.builder());
			}
		}

		public SimpleBakedModel.Builder addCulledFace(Direction direction, BakedQuad bakedQuad) {
			((ImmutableList.Builder)this.culledFaces.get(direction)).add(bakedQuad);
			return this;
		}

		public SimpleBakedModel.Builder addUnculledFace(BakedQuad bakedQuad) {
			this.unculledFaces.add(bakedQuad);
			return this;
		}

		public SimpleBakedModel.Builder particle(TextureAtlasSprite textureAtlasSprite) {
			this.particleIcon = textureAtlasSprite;
			return this;
		}

		public SimpleBakedModel.Builder item() {
			return this;
		}

		public BakedModel build() {
			if (this.particleIcon == null) {
				throw new RuntimeException("Missing particle!");
			} else {
				Map<Direction, List<BakedQuad>> map = Maps.transformValues(this.culledFaces, ImmutableList.Builder::build);
				return new SimpleBakedModel(
					this.unculledFaces.build(), new EnumMap(map), this.hasAmbientOcclusion, this.usesBlockLight, this.isGui3d, this.particleIcon, this.transforms
				);
			}
		}
	}
}
