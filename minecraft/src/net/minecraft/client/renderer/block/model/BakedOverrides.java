package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class BakedOverrides {
	public static final BakedOverrides EMPTY = new BakedOverrides();
	public static final float NO_OVERRIDE = Float.NEGATIVE_INFINITY;
	private final BakedOverrides.BakedOverride[] overrides;
	private final ResourceLocation[] properties;

	private BakedOverrides() {
		this.overrides = new BakedOverrides.BakedOverride[0];
		this.properties = new ResourceLocation[0];
	}

	public BakedOverrides(ModelBaker modelBaker, List<ItemOverride> list) {
		this.properties = (ResourceLocation[])list.stream()
			.flatMap(itemOverridex -> itemOverridex.predicates().stream())
			.map(ItemOverride.Predicate::property)
			.distinct()
			.toArray(ResourceLocation[]::new);
		Object2IntMap<ResourceLocation> object2IntMap = new Object2IntOpenHashMap<>();

		for (int i = 0; i < this.properties.length; i++) {
			object2IntMap.put(this.properties[i], i);
		}

		List<BakedOverrides.BakedOverride> list2 = Lists.<BakedOverrides.BakedOverride>newArrayList();

		for (int j = list.size() - 1; j >= 0; j--) {
			ItemOverride itemOverride = (ItemOverride)list.get(j);
			BakedModel bakedModel = modelBaker.bake(itemOverride.model(), BlockModelRotation.X0_Y0);
			BakedOverrides.PropertyMatcher[] propertyMatchers = (BakedOverrides.PropertyMatcher[])itemOverride.predicates().stream().map(predicate -> {
				int i = object2IntMap.getInt(predicate.property());
				return new BakedOverrides.PropertyMatcher(i, predicate.value());
			}).toArray(BakedOverrides.PropertyMatcher[]::new);
			list2.add(new BakedOverrides.BakedOverride(propertyMatchers, bakedModel));
		}

		this.overrides = (BakedOverrides.BakedOverride[])list2.toArray(new BakedOverrides.BakedOverride[0]);
	}

	@Nullable
	public BakedModel findOverride(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i) {
		int j = this.properties.length;
		if (j != 0) {
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

			for (BakedOverrides.BakedOverride bakedOverride : this.overrides) {
				if (bakedOverride.test(fs)) {
					return bakedOverride.model;
				}
			}
		}

		return null;
	}

	@Environment(EnvType.CLIENT)
	static record BakedOverride(BakedOverrides.PropertyMatcher[] matchers, @Nullable BakedModel model) {

		boolean test(float[] fs) {
			for (BakedOverrides.PropertyMatcher propertyMatcher : this.matchers) {
				float f = fs[propertyMatcher.index];
				if (f < propertyMatcher.value) {
					return false;
				}
			}

			return true;
		}
	}

	@Environment(EnvType.CLIENT)
	static record PropertyMatcher(int index, float value) {
	}
}
