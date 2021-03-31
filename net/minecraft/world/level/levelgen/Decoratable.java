/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.ChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;
import net.minecraft.world.level.levelgen.placement.DepthAverageConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public interface Decoratable<R> {
    public R decorated(ConfiguredDecorator<?> var1);

    default public R rarity(int i) {
        return this.decorated(FeatureDecorator.CHANCE.configured(new ChanceDecoratorConfiguration(i)));
    }

    default public R count(IntProvider intProvider) {
        return this.decorated(FeatureDecorator.COUNT.configured(new CountConfiguration(intProvider)));
    }

    default public R count(int i) {
        return this.count(ConstantInt.of(i));
    }

    default public R countRandom(int i) {
        return this.count(UniformInt.of(0, i));
    }

    default public R range(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2) {
        return this.range(new RangeDecoratorConfiguration(verticalAnchor, verticalAnchor2));
    }

    default public R range(RangeDecoratorConfiguration rangeDecoratorConfiguration) {
        return this.decorated(FeatureDecorator.RANGE.configured(rangeDecoratorConfiguration));
    }

    default public R squared() {
        return this.decorated(FeatureDecorator.SQUARE.configured(NoneDecoratorConfiguration.INSTANCE));
    }

    default public R depthAverage(VerticalAnchor verticalAnchor, int i) {
        return this.decorated(FeatureDecorator.DEPTH_AVERAGE.configured(new DepthAverageConfiguration(verticalAnchor, i)));
    }
}

