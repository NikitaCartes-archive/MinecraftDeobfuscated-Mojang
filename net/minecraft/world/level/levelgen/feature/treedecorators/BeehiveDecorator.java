/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.LevelAccessor;
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
    private final float probability;

    public BeehiveDecorator(float f) {
        super(TreeDecoratorType.BEEHIVE);
        this.probability = f;
    }

    public <T> BeehiveDecorator(Dynamic<T> dynamic) {
        this(dynamic.get("probability").asFloat(0.0f));
    }

    @Override
    public void place(LevelAccessor levelAccessor, Random random, List<BlockPos> list, List<BlockPos> list2, Set<BlockPos> set, BoundingBox boundingBox) {
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
        if (!Feature.isAir(levelAccessor, blockPos22) || !Feature.isAir(levelAccessor, blockPos22.relative(Direction.SOUTH))) {
            return;
        }
        BlockState blockState = (BlockState)Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.FACING, Direction.SOUTH);
        this.setBlock(levelAccessor, blockPos22, blockState, set, boundingBox);
        BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos22);
        if (blockEntity instanceof BeehiveBlockEntity) {
            BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
            int j = 2 + random.nextInt(2);
            for (int k = 0; k < j; ++k) {
                Bee bee = new Bee((EntityType<? extends Bee>)EntityType.BEE, levelAccessor.getLevel());
                beehiveBlockEntity.addOccupantWithPresetTicks(bee, false, random.nextInt(599));
            }
        }
    }

    @Override
    public <T> T serialize(DynamicOps<T> dynamicOps) {
        return new Dynamic<T>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("type"), dynamicOps.createString(Registry.TREE_DECORATOR_TYPES.getKey(this.type).toString()), dynamicOps.createString("probability"), dynamicOps.createFloat(this.probability)))).getValue();
    }
}

