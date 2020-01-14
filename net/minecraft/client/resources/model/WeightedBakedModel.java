/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.model;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.WeighedRandom;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class WeightedBakedModel
implements BakedModel {
    private final int totalWeight;
    private final List<WeightedModel> list;
    private final BakedModel wrapped;

    public WeightedBakedModel(List<WeightedModel> list) {
        this.list = list;
        this.totalWeight = WeighedRandom.getTotalWeight(list);
        this.wrapped = list.get((int)0).model;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, Random random) {
        return WeighedRandom.getWeightedItem(this.list, (int)(Math.abs((int)((int)random.nextLong())) % this.totalWeight)).model.getQuads(blockState, direction, random);
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

    @Environment(value=EnvType.CLIENT)
    static class WeightedModel
    extends WeighedRandom.WeighedRandomItem {
        protected final BakedModel model;

        public WeightedModel(BakedModel bakedModel, int i) {
            super(i);
            this.model = bakedModel;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private final List<WeightedModel> list = Lists.newArrayList();

        public Builder add(@Nullable BakedModel bakedModel, int i) {
            if (bakedModel != null) {
                this.list.add(new WeightedModel(bakedModel, i));
            }
            return this;
        }

        @Nullable
        public BakedModel build() {
            if (this.list.isEmpty()) {
                return null;
            }
            if (this.list.size() == 1) {
                return this.list.get((int)0).model;
            }
            return new WeightedBakedModel(this.list);
        }
    }
}

