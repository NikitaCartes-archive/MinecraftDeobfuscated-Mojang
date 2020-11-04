/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.color.block;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.IdMapper;
import net.minecraft.core.Registry;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MaterialColor;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BlockColors {
    private final IdMapper<BlockColor> blockColors = new IdMapper(32);
    private final Map<Block, Set<Property<?>>> coloringStates = Maps.newHashMap();

    public static BlockColors createDefault() {
        BlockColors blockColors = new BlockColors();
        blockColors.register((blockState, blockAndTintGetter, blockPos, i) -> {
            if (blockAndTintGetter == null || blockPos == null) {
                return -1;
            }
            return BiomeColors.getAverageGrassColor(blockAndTintGetter, blockState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER ? blockPos.below() : blockPos);
        }, Blocks.LARGE_FERN, Blocks.TALL_GRASS);
        blockColors.addColoringState(DoublePlantBlock.HALF, Blocks.LARGE_FERN, Blocks.TALL_GRASS);
        blockColors.register((blockState, blockAndTintGetter, blockPos, i) -> {
            if (blockAndTintGetter == null || blockPos == null) {
                return GrassColor.get(0.5, 1.0);
            }
            return BiomeColors.getAverageGrassColor(blockAndTintGetter, blockPos);
        }, Blocks.GRASS_BLOCK, Blocks.FERN, Blocks.GRASS, Blocks.POTTED_FERN);
        blockColors.register((blockState, blockAndTintGetter, blockPos, i) -> FoliageColor.getEvergreenColor(), Blocks.SPRUCE_LEAVES);
        blockColors.register((blockState, blockAndTintGetter, blockPos, i) -> FoliageColor.getBirchColor(), Blocks.BIRCH_LEAVES);
        blockColors.register((blockState, blockAndTintGetter, blockPos, i) -> {
            if (blockAndTintGetter == null || blockPos == null) {
                return FoliageColor.getDefaultColor();
            }
            return BiomeColors.getAverageFoliageColor(blockAndTintGetter, blockPos);
        }, Blocks.OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.VINE);
        blockColors.register((blockState, blockAndTintGetter, blockPos, i) -> {
            if (blockAndTintGetter == null || blockPos == null) {
                return -1;
            }
            return BiomeColors.getAverageWaterColor(blockAndTintGetter, blockPos);
        }, Blocks.WATER, Blocks.BUBBLE_COLUMN, Blocks.WATER_CAULDRON);
        blockColors.register((blockState, blockAndTintGetter, blockPos, i) -> RedStoneWireBlock.getColorForPower(blockState.getValue(RedStoneWireBlock.POWER)), Blocks.REDSTONE_WIRE);
        blockColors.addColoringState(RedStoneWireBlock.POWER, Blocks.REDSTONE_WIRE);
        blockColors.register((blockState, blockAndTintGetter, blockPos, i) -> {
            if (blockAndTintGetter == null || blockPos == null) {
                return -1;
            }
            return BiomeColors.getAverageGrassColor(blockAndTintGetter, blockPos);
        }, Blocks.SUGAR_CANE);
        blockColors.register((blockState, blockAndTintGetter, blockPos, i) -> 14731036, Blocks.ATTACHED_MELON_STEM, Blocks.ATTACHED_PUMPKIN_STEM);
        blockColors.register((blockState, blockAndTintGetter, blockPos, i) -> {
            int j = blockState.getValue(StemBlock.AGE);
            int k = j * 32;
            int l = 255 - j * 8;
            int m = j * 4;
            return k << 16 | l << 8 | m;
        }, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
        blockColors.addColoringState(StemBlock.AGE, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
        blockColors.register((blockState, blockAndTintGetter, blockPos, i) -> {
            if (blockAndTintGetter == null || blockPos == null) {
                return 7455580;
            }
            return 2129968;
        }, Blocks.LILY_PAD);
        return blockColors;
    }

    public int getColor(BlockState blockState, Level level, BlockPos blockPos) {
        BlockColor blockColor = this.blockColors.byId(Registry.BLOCK.getId(blockState.getBlock()));
        if (blockColor != null) {
            return blockColor.getColor(blockState, null, null, 0);
        }
        MaterialColor materialColor = blockState.getMapColor(level, blockPos);
        return materialColor != null ? materialColor.col : -1;
    }

    public int getColor(BlockState blockState, @Nullable BlockAndTintGetter blockAndTintGetter, @Nullable BlockPos blockPos, int i) {
        BlockColor blockColor = this.blockColors.byId(Registry.BLOCK.getId(blockState.getBlock()));
        return blockColor == null ? -1 : blockColor.getColor(blockState, blockAndTintGetter, blockPos, i);
    }

    public void register(BlockColor blockColor, Block ... blocks) {
        for (Block block : blocks) {
            this.blockColors.addMapping(blockColor, Registry.BLOCK.getId(block));
        }
    }

    private void addColoringStates(Set<Property<?>> set, Block ... blocks) {
        for (Block block : blocks) {
            this.coloringStates.put(block, set);
        }
    }

    private void addColoringState(Property<?> property, Block ... blocks) {
        this.addColoringStates(ImmutableSet.of(property), blocks);
    }

    public Set<Property<?>> getColoringProperties(Block block) {
        return this.coloringStates.getOrDefault(block, ImmutableSet.of());
    }
}

