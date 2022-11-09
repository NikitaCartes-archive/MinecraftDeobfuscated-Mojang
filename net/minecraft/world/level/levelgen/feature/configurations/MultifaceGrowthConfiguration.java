/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class MultifaceGrowthConfiguration
implements FeatureConfiguration {
    public static final Codec<MultifaceGrowthConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block")).flatXmap(MultifaceGrowthConfiguration::apply, DataResult::success).orElse((MultifaceBlock)Blocks.GLOW_LICHEN).forGetter(multifaceGrowthConfiguration -> multifaceGrowthConfiguration.placeBlock), ((MapCodec)Codec.intRange(1, 64).fieldOf("search_range")).orElse(10).forGetter(multifaceGrowthConfiguration -> multifaceGrowthConfiguration.searchRange), ((MapCodec)Codec.BOOL.fieldOf("can_place_on_floor")).orElse(false).forGetter(multifaceGrowthConfiguration -> multifaceGrowthConfiguration.canPlaceOnFloor), ((MapCodec)Codec.BOOL.fieldOf("can_place_on_ceiling")).orElse(false).forGetter(multifaceGrowthConfiguration -> multifaceGrowthConfiguration.canPlaceOnCeiling), ((MapCodec)Codec.BOOL.fieldOf("can_place_on_wall")).orElse(false).forGetter(multifaceGrowthConfiguration -> multifaceGrowthConfiguration.canPlaceOnWall), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("chance_of_spreading")).orElse(Float.valueOf(0.5f)).forGetter(multifaceGrowthConfiguration -> Float.valueOf(multifaceGrowthConfiguration.chanceOfSpreading)), ((MapCodec)RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("can_be_placed_on")).forGetter(multifaceGrowthConfiguration -> multifaceGrowthConfiguration.canBePlacedOn)).apply((Applicative<MultifaceGrowthConfiguration, ?>)instance, MultifaceGrowthConfiguration::new));
    public final MultifaceBlock placeBlock;
    public final int searchRange;
    public final boolean canPlaceOnFloor;
    public final boolean canPlaceOnCeiling;
    public final boolean canPlaceOnWall;
    public final float chanceOfSpreading;
    public final HolderSet<Block> canBePlacedOn;
    private final ObjectArrayList<Direction> validDirections;

    private static DataResult<MultifaceBlock> apply(Block block) {
        DataResult<MultifaceBlock> dataResult;
        if (block instanceof MultifaceBlock) {
            MultifaceBlock multifaceBlock = (MultifaceBlock)block;
            dataResult = DataResult.success(multifaceBlock);
        } else {
            dataResult = DataResult.error("Growth block should be a multiface block");
        }
        return dataResult;
    }

    public MultifaceGrowthConfiguration(MultifaceBlock multifaceBlock, int i, boolean bl, boolean bl2, boolean bl3, float f, HolderSet<Block> holderSet) {
        this.placeBlock = multifaceBlock;
        this.searchRange = i;
        this.canPlaceOnFloor = bl;
        this.canPlaceOnCeiling = bl2;
        this.canPlaceOnWall = bl3;
        this.chanceOfSpreading = f;
        this.canBePlacedOn = holderSet;
        this.validDirections = new ObjectArrayList(6);
        if (bl2) {
            this.validDirections.add(Direction.UP);
        }
        if (bl) {
            this.validDirections.add(Direction.DOWN);
        }
        if (bl3) {
            Direction.Plane.HORIZONTAL.forEach(this.validDirections::add);
        }
    }

    public List<Direction> getShuffledDirectionsExcept(RandomSource randomSource, Direction direction) {
        return Util.toShuffledList(this.validDirections.stream().filter(direction2 -> direction2 != direction), randomSource);
    }

    public List<Direction> getShuffledDirections(RandomSource randomSource) {
        return Util.shuffledCopy(this.validDirections, randomSource);
    }
}

