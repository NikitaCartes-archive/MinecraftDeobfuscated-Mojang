/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ItemOverrides {
    public static final ItemOverrides EMPTY = new ItemOverrides();
    private final BakedOverride[] overrides;
    private final ResourceLocation[] properties;

    private ItemOverrides() {
        this.overrides = new BakedOverride[0];
        this.properties = new ResourceLocation[0];
    }

    public ItemOverrides(ModelBaker modelBaker, BlockModel blockModel, List<ItemOverride> list) {
        this.properties = (ResourceLocation[])list.stream().flatMap(ItemOverride::getPredicates).map(ItemOverride.Predicate::getProperty).distinct().toArray(ResourceLocation[]::new);
        Object2IntOpenHashMap<ResourceLocation> object2IntMap = new Object2IntOpenHashMap<ResourceLocation>();
        for (int i = 0; i < this.properties.length; ++i) {
            object2IntMap.put(this.properties[i], i);
        }
        ArrayList<BakedOverride> list2 = Lists.newArrayList();
        for (int j = list.size() - 1; j >= 0; --j) {
            ItemOverride itemOverride = list.get(j);
            BakedModel bakedModel = this.bakeModel(modelBaker, blockModel, itemOverride);
            PropertyMatcher[] propertyMatchers = (PropertyMatcher[])itemOverride.getPredicates().map(predicate -> {
                int i = object2IntMap.getInt(predicate.getProperty());
                return new PropertyMatcher(i, predicate.getValue());
            }).toArray(PropertyMatcher[]::new);
            list2.add(new BakedOverride(propertyMatchers, bakedModel));
        }
        this.overrides = list2.toArray(new BakedOverride[0]);
    }

    @Nullable
    private BakedModel bakeModel(ModelBaker modelBaker, BlockModel blockModel, ItemOverride itemOverride) {
        UnbakedModel unbakedModel = modelBaker.getModel(itemOverride.getModel());
        if (Objects.equals(unbakedModel, blockModel)) {
            return null;
        }
        return modelBaker.bake(itemOverride.getModel(), BlockModelRotation.X0_Y0);
    }

    @Nullable
    public BakedModel resolve(BakedModel bakedModel, ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i) {
        if (this.overrides.length != 0) {
            Item item = itemStack.getItem();
            int j = this.properties.length;
            float[] fs = new float[j];
            for (int k = 0; k < j; ++k) {
                ResourceLocation resourceLocation = this.properties[k];
                ItemPropertyFunction itemPropertyFunction = ItemProperties.getProperty(item, resourceLocation);
                fs[k] = itemPropertyFunction != null ? itemPropertyFunction.call(itemStack, clientLevel, livingEntity, i) : Float.NEGATIVE_INFINITY;
            }
            for (BakedOverride bakedOverride : this.overrides) {
                if (!bakedOverride.test(fs)) continue;
                BakedModel bakedModel2 = bakedOverride.model;
                if (bakedModel2 == null) {
                    return bakedModel;
                }
                return bakedModel2;
            }
        }
        return bakedModel;
    }

    @Environment(value=EnvType.CLIENT)
    static class BakedOverride {
        private final PropertyMatcher[] matchers;
        @Nullable
        final BakedModel model;

        BakedOverride(PropertyMatcher[] propertyMatchers, @Nullable BakedModel bakedModel) {
            this.matchers = propertyMatchers;
            this.model = bakedModel;
        }

        boolean test(float[] fs) {
            for (PropertyMatcher propertyMatcher : this.matchers) {
                float f = fs[propertyMatcher.index];
                if (!(f < propertyMatcher.value)) continue;
                return false;
            }
            return true;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class PropertyMatcher {
        public final int index;
        public final float value;

        PropertyMatcher(int i, float f) {
            this.index = i;
            this.value = f;
        }
    }
}

