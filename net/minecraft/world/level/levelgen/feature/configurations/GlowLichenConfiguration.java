/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class GlowLichenConfiguration
implements FeatureConfiguration {
    public static final Codec<GlowLichenConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.intRange(1, 64).fieldOf("search_range")).orElse(10).forGetter(glowLichenConfiguration -> glowLichenConfiguration.searchRange), ((MapCodec)Codec.BOOL.fieldOf("can_place_on_floor")).orElse(false).forGetter(glowLichenConfiguration -> glowLichenConfiguration.canPlaceOnFloor), ((MapCodec)Codec.BOOL.fieldOf("can_place_on_ceiling")).orElse(false).forGetter(glowLichenConfiguration -> glowLichenConfiguration.canPlaceOnCeiling), ((MapCodec)Codec.BOOL.fieldOf("can_place_on_wall")).orElse(false).forGetter(glowLichenConfiguration -> glowLichenConfiguration.canPlaceOnWall), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("chance_of_spreading")).orElse(Float.valueOf(0.5f)).forGetter(glowLichenConfiguration -> Float.valueOf(glowLichenConfiguration.chanceOfSpreading)), ((MapCodec)BlockState.CODEC.listOf().fieldOf("can_be_placed_on")).forGetter(glowLichenConfiguration -> new ArrayList<BlockState>(glowLichenConfiguration.canBePlacedOn))).apply((Applicative<GlowLichenConfiguration, ?>)instance, GlowLichenConfiguration::new));
    public final int searchRange;
    public final boolean canPlaceOnFloor;
    public final boolean canPlaceOnCeiling;
    public final boolean canPlaceOnWall;
    public final float chanceOfSpreading;
    public final List<BlockState> canBePlacedOn;
    public final List<Direction> validDirections;

    public GlowLichenConfiguration(int i, boolean bl, boolean bl2, boolean bl3, float f, List<BlockState> list) {
        this.searchRange = i;
        this.canPlaceOnFloor = bl;
        this.canPlaceOnCeiling = bl2;
        this.canPlaceOnWall = bl3;
        this.chanceOfSpreading = f;
        this.canBePlacedOn = list;
        ArrayList<Direction> list2 = Lists.newArrayList();
        if (bl2) {
            list2.add(Direction.UP);
        }
        if (bl) {
            list2.add(Direction.DOWN);
        }
        if (bl3) {
            Direction.Plane.HORIZONTAL.forEach(list2::add);
        }
        this.validDirections = Collections.unmodifiableList(list2);
    }

    public boolean canBePlacedOn(Block block) {
        return this.canBePlacedOn.stream().anyMatch(blockState -> blockState.is(block));
    }
}

