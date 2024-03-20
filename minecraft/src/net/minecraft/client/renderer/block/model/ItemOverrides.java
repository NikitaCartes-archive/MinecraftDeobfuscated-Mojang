package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class ItemOverrides {
	public static final ItemOverrides EMPTY = new ItemOverrides();
	public static final float NO_OVERRIDE = Float.NEGATIVE_INFINITY;
	private final ItemOverrides.BakedOverride[] overrides;
	private final ResourceLocation[] properties;

	private ItemOverrides() {
		this.overrides = new ItemOverrides.BakedOverride[0];
		this.properties = new ResourceLocation[0];
	}

	public ItemOverrides(ModelBaker modelBaker, BlockModel blockModel, List<ItemOverride> list) {
		this.properties = (ResourceLocation[])list.stream()
			.flatMap(ItemOverride::getPredicates)
			.map(ItemOverride.Predicate::getProperty)
			.distinct()
			.toArray(ResourceLocation[]::new);
		Object2IntMap<ResourceLocation> object2IntMap = new Object2IntOpenHashMap<>();

		for (int i = 0; i < this.properties.length; i++) {
			object2IntMap.put(this.properties[i], i);
		}

		List<ItemOverrides.BakedOverride> list2 = Lists.<ItemOverrides.BakedOverride>newArrayList();

		for (int j = list.size() - 1; j >= 0; j--) {
			ItemOverride itemOverride = (ItemOverride)list.get(j);
			BakedModel bakedModel = this.bakeModel(modelBaker, blockModel, itemOverride);
			ItemOverrides.PropertyMatcher[] propertyMatchers = (ItemOverrides.PropertyMatcher[])itemOverride.getPredicates().map(predicate -> {
				int i = object2IntMap.getInt(predicate.getProperty());
				return new ItemOverrides.PropertyMatcher(i, predicate.getValue());
			}).toArray(ItemOverrides.PropertyMatcher[]::new);
			list2.add(new ItemOverrides.BakedOverride(propertyMatchers, bakedModel));
		}

		this.overrides = (ItemOverrides.BakedOverride[])list2.toArray(new ItemOverrides.BakedOverride[0]);
	}

	@Nullable
	private BakedModel bakeModel(ModelBaker modelBaker, BlockModel blockModel, ItemOverride itemOverride) {
		UnbakedModel unbakedModel = modelBaker.getModel(itemOverride.getModel());
		return Objects.equals(unbakedModel, blockModel) ? null : modelBaker.bake(itemOverride.getModel(), BlockModelRotation.X0_Y0);
	}

	@Nullable
	public BakedModel resolve(BakedModel bakedModel, ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i) {
		if (this.overrides.length != 0) {
			int j = this.properties.length;
			float[] fs = new float[j];

			for (int k = 0; k < j; k++) {
				ResourceLocation resourceLocation = this.properties[k];
				ItemPropertyFunction itemPropertyFunction = ItemProperties.getProperty(itemStack, resourceLocation);
				if (itemPropertyFunction != null) {
					fs[k] = itemPropertyFunction.call(itemStack, clientLevel, livingEntity, i);
				} else {
					fs[k] = Float.NEGATIVE_INFINITY;
				}
			}

			for (ItemOverrides.BakedOverride bakedOverride : this.overrides) {
				if (bakedOverride.test(fs)) {
					BakedModel bakedModel2 = bakedOverride.model;
					if (bakedModel2 == null) {
						return bakedModel;
					}

					return bakedModel2;
				}
			}
		}

		return bakedModel;
	}

	@Environment(EnvType.CLIENT)
	static class BakedOverride {
		private final ItemOverrides.PropertyMatcher[] matchers;
		@Nullable
		final BakedModel model;

		BakedOverride(ItemOverrides.PropertyMatcher[] propertyMatchers, @Nullable BakedModel bakedModel) {
			this.matchers = propertyMatchers;
			this.model = bakedModel;
		}

		boolean test(float[] fs) {
			for (ItemOverrides.PropertyMatcher propertyMatcher : this.matchers) {
				float f = fs[propertyMatcher.index];
				if (f < propertyMatcher.value) {
					return false;
				}
			}

			return true;
		}
	}

	@Environment(EnvType.CLIENT)
	static class PropertyMatcher {
		public final int index;
		public final float value;

		PropertyMatcher(int i, float f) {
			this.index = i;
			this.value = f;
		}
	}
}
