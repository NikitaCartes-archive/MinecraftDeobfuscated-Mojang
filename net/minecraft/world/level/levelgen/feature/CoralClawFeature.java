/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.CoralFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CoralClawFeature
extends CoralFeature {
    public CoralClawFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
        super(function);
    }

    @Override
    protected boolean placeFeature(LevelAccessor levelAccessor, Random random, BlockPos blockPos, BlockState blockState) {
        if (!this.placeCoralBlock(levelAccessor, random, blockPos, blockState)) {
            return false;
        }
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int i = random.nextInt(2) + 2;
        ArrayList<Direction> list = Lists.newArrayList(direction, direction.getClockWise(), direction.getCounterClockWise());
        Collections.shuffle(list, random);
        List list2 = list.subList(0, i);
        block0: for (Direction direction2 : list2) {
            int l;
            int k;
            Direction direction3;
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(blockPos);
            int j = random.nextInt(2) + 1;
            mutableBlockPos.move(direction2);
            if (direction2 == direction) {
                direction3 = direction;
                k = random.nextInt(3) + 2;
            } else {
                mutableBlockPos.move(Direction.UP);
                Direction[] directions = new Direction[]{direction2, Direction.UP};
                direction3 = directions[random.nextInt(directions.length)];
                k = random.nextInt(3) + 3;
            }
            for (l = 0; l < j && this.placeCoralBlock(levelAccessor, random, mutableBlockPos, blockState); ++l) {
                mutableBlockPos.move(direction3);
            }
            mutableBlockPos.move(direction3.getOpposite());
            mutableBlockPos.move(Direction.UP);
            for (l = 0; l < k; ++l) {
                mutableBlockPos.move(direction);
                if (!this.placeCoralBlock(levelAccessor, random, mutableBlockPos, blockState)) continue block0;
                if (!(random.nextFloat() < 0.25f)) continue;
                mutableBlockPos.move(Direction.UP);
            }
        }
        return true;
    }
}

