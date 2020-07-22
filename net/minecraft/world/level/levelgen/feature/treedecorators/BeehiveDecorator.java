/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class BeehiveDecorator
extends TreeDecorator {
    public static final Codec<BeehiveDecorator> CODEC = ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("probability")).xmap(BeehiveDecorator::new, beehiveDecorator -> Float.valueOf(beehiveDecorator.probability)).codec();
    private final float probability;

    public BeehiveDecorator(float f) {
        this.probability = f;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.BEEHIVE;
    }

    @Override
    public void place(WorldGenLevel worldGenLevel, Random random, List<BlockPos> list, List<BlockPos> list2, Set<BlockPos> set, BoundingBox boundingBox) {
        if (random.nextFloat() >= this.probability) {
            return;
        }
        Direction direction = BeehiveBlock.getRandomOffset(random);
        int i = !list2.isEmpty() ? Math.max(list2.get(0).getY() - 1, list.get(0).getY()) : Math.min(list.get(0).getY() + 1 + random.nextInt(3), list.get(list.size() - 1).getY());
        List list3 = list.stream().filter(blockPos -> blockPos.getY() == i).collect(Collectors.toList());
        if (list3.isEmpty()) {
            return;
        }
        BlockPos blockPos2 = (BlockPos)list3.get(random.nextInt(list3.size()));
        BlockPos blockPos22 = blockPos2.relative(direction);
        if (!Feature.isAir(worldGenLevel, blockPos22) || !Feature.isAir(worldGenLevel, blockPos22.relative(Direction.SOUTH))) {
            return;
        }
        BlockState blockState = (BlockState)Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.FACING, Direction.SOUTH);
        this.setBlock(worldGenLevel, blockPos22, blockState, set, boundingBox);
        BlockEntity blockEntity = worldGenLevel.getBlockEntity(blockPos22);
        if (blockEntity instanceof BeehiveBlockEntity) {
            BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
            int j = 2 + random.nextInt(2);
            for (int k = 0; k < j; ++k) {
                Bee bee = new Bee((EntityType<? extends Bee>)EntityType.BEE, (Level)worldGenLevel.getLevel());
                beehiveBlockEntity.addOccupantWithPresetTicks(bee, false, random.nextInt(599));
            }
        }
    }
}

