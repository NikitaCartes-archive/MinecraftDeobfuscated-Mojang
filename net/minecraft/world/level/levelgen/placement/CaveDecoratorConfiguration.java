/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

public class CaveDecoratorConfiguration
implements DecoratorConfiguration {
    public static final Codec<CaveDecoratorConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)CaveSurface.CODEC.fieldOf("surface")).forGetter(caveDecoratorConfiguration -> caveDecoratorConfiguration.surface), ((MapCodec)Codec.INT.fieldOf("floor_to_ceiling_search_range")).forGetter(caveDecoratorConfiguration -> caveDecoratorConfiguration.floorToCeilingSearchRange), ((MapCodec)Codec.BOOL.fieldOf("allow_water")).forGetter(caveDecoratorConfiguration -> caveDecoratorConfiguration.allowWater)).apply((Applicative<CaveDecoratorConfiguration, ?>)instance, CaveDecoratorConfiguration::new));
    public final CaveSurface surface;
    public final int floorToCeilingSearchRange;
    public final boolean allowWater;

    public CaveDecoratorConfiguration(CaveSurface caveSurface, int i, boolean bl) {
        this.surface = caveSurface;
        this.floorToCeilingSearchRange = i;
        this.allowWater = bl;
    }
}

