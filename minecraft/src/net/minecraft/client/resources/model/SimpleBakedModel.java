package net.minecraft.client.resources.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
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
	protected final ItemOverrides overrides;

	public SimpleBakedModel(
		List<BakedQuad> list,
		Map<Direction, List<BakedQuad>> map,
		boolean bl,
		boolean bl2,
		boolean bl3,
		TextureAtlasSprite textureAtlasSprite,
		ItemTransforms itemTransforms,
		ItemOverrides itemOverrides
	) {
		this.unculledFaces = list;
		this.culledFaces = map;
		this.hasAmbientOcclusion = bl;
		this.isGui3d = bl3;
		this.usesBlockLight = bl2;
		this.particleIcon = textureAtlasSprite;
		this.transforms = itemTransforms;
		this.overrides = itemOverrides;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, Random random) {
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

	@Override
	public ItemOverrides getOverrides() {
		return this.overrides;
	}

	@Environment(EnvType.CLIENT)
	public static class Builder {
		private final List<BakedQuad> unculledFaces = Lists.<BakedQuad>newArrayList();
		private final Map<Direction, List<BakedQuad>> culledFaces = Maps.newEnumMap(Direction.class);
		private final ItemOverrides overrides;
		private final boolean hasAmbientOcclusion;
		private TextureAtlasSprite particleIcon;
		private final boolean usesBlockLight;
		private final boolean isGui3d;
		private final ItemTransforms transforms;

		public Builder(BlockModel blockModel, ItemOverrides itemOverrides, boolean bl) {
			this(blockModel.hasAmbientOcclusion(), blockModel.getGuiLight().lightLikeBlock(), bl, blockModel.getTransforms(), itemOverrides);
		}

		private Builder(boolean bl, boolean bl2, boolean bl3, ItemTransforms itemTransforms, ItemOverrides itemOverrides) {
			for (Direction direction : Direction.values()) {
				this.culledFaces.put(direction, Lists.newArrayList());
			}

			this.overrides = itemOverrides;
			this.hasAmbientOcclusion = bl;
			this.usesBlockLight = bl2;
			this.isGui3d = bl3;
			this.transforms = itemTransforms;
		}

		public SimpleBakedModel.Builder addCulledFace(Direction direction, BakedQuad bakedQuad) {
			((List)this.culledFaces.get(direction)).add(bakedQuad);
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
				return new SimpleBakedModel(
					this.unculledFaces, this.culledFaces, this.hasAmbientOcclusion, this.usesBlockLight, this.isGui3d, this.particleIcon, this.transforms, this.overrides
				);
			}
		}
	}
}
