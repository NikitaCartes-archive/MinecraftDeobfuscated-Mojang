package net.minecraft.client.resources.model;

import java.util.List;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedOverrides;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class ItemModel implements UnbakedModel {
	private final ResourceLocation id;
	private List<ItemOverride> overrides = List.of();

	public ItemModel(ResourceLocation resourceLocation) {
		this.id = resourceLocation;
	}

	@Override
	public void resolveDependencies(UnbakedModel.Resolver resolver) {
		if (resolver.resolve(this.id) instanceof BlockModel blockModel) {
			this.overrides = blockModel.getOverrides();
			this.overrides.forEach(itemOverride -> resolver.resolve(itemOverride.model()));
		}
	}

	@Override
	public BakedModel bake(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> function, ModelState modelState) {
		BakedModel bakedModel = modelBaker.bake(this.id, modelState);
		if (this.overrides.isEmpty()) {
			return bakedModel;
		} else {
			BakedOverrides bakedOverrides = new BakedOverrides(modelBaker, this.overrides);
			return new ItemModel.BakedModelWithOverrides(bakedModel, bakedOverrides);
		}
	}

	@Environment(EnvType.CLIENT)
	static class BakedModelWithOverrides extends DelegateBakedModel {
		private final BakedOverrides overrides;

		public BakedModelWithOverrides(BakedModel bakedModel, BakedOverrides bakedOverrides) {
			super(bakedModel);
			this.overrides = bakedOverrides;
		}

		@Override
		public BakedOverrides overrides() {
			return this.overrides;
		}
	}
}
