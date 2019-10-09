/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class VinesFeature
extends Feature<NoneFeatureConfiguration> {
    private static final Direction[] DIRECTIONS = Direction.values();

    public VinesFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
        super(function);
    }

    @Override
    public boolean place(LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(blockPos);
        block0: for (int i = blockPos.getY(); i < 256; ++i) {
            mutableBlockPos.set(blockPos);
            mutableBlockPos.move(random.nextInt(4) - random.nextInt(4), 0, random.nextInt(4) - random.nextInt(4));
            mutableBlockPos.setY(i);
            if (!levelAccessor.isEmptyBlock(mutableBlockPos)) continue;
            for (Direction direction : DIRECTIONS) {
                if (direction == Direction.DOWN || !VineBlock.isAcceptableNeighbour(levelAccessor, mutableBlockPos, direction)) continue;
                levelAccessor.setBlock(mutableBlockPos, (BlockState)Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(direction), true), 2);
                continue block0;
            }
        }
        return true;
    }
}

