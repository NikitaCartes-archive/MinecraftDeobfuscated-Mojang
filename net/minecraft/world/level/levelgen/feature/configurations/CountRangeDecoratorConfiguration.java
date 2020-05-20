/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class CountRangeDecoratorConfiguration
implements DecoratorConfiguration {
    public static final Codec<CountRangeDecoratorConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("count")).forGetter(countRangeDecoratorConfiguration -> countRangeDecoratorConfiguration.count), ((MapCodec)Codec.INT.fieldOf("bottom_offset")).withDefault(0).forGetter(countRangeDecoratorConfiguration -> countRangeDecoratorConfiguration.bottomOffset), ((MapCodec)Codec.INT.fieldOf("top_offset")).withDefault(0).forGetter(countRangeDecoratorConfiguration -> countRangeDecoratorConfiguration.topOffset), ((MapCodec)Codec.INT.fieldOf("maximum")).withDefault(0).forGetter(countRangeDecoratorConfiguration -> countRangeDecoratorConfiguration.maximum)).apply((Applicative<CountRangeDecoratorConfiguration, ?>)instance, CountRangeDecoratorConfiguration::new));
    public final int count;
    public final int bottomOffset;
    public final int topOffset;
    public final int maximum;

    public CountRangeDecoratorConfiguration(int i, int j, int k, int l) {
        this.count = i;
        this.bottomOffset = j;
        this.topOffset = k;
        this.maximum = l;
    }
}

