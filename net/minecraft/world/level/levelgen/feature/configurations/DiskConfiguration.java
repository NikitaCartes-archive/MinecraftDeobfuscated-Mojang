/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class DiskConfiguration
implements FeatureConfiguration {
    public static final Codec<DiskConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockState.CODEC.fieldOf("state")).forGetter(diskConfiguration -> diskConfiguration.state), ((MapCodec)Codec.INT.fieldOf("radius")).withDefault(0).forGetter(diskConfiguration -> diskConfiguration.radius), ((MapCodec)Codec.INT.fieldOf("y_size")).withDefault(0).forGetter(diskConfiguration -> diskConfiguration.ySize), ((MapCodec)BlockState.CODEC.listOf().fieldOf("targets")).forGetter(diskConfiguration -> diskConfiguration.targets)).apply((Applicative<DiskConfiguration, ?>)instance, DiskConfiguration::new));
    public final BlockState state;
    public final int radius;
    public final int ySize;
    public final List<BlockState> targets;

    public DiskConfiguration(BlockState blockState, int i, int j, List<BlockState> list) {
        this.state = blockState;
        this.radius = i;
        this.ySize = j;
        this.targets = list;
    }
}

