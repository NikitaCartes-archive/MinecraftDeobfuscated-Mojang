/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class BeehiveDecorator
extends TreeDecorator {
    public static final Codec<BeehiveDecorator> CODEC = ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("probability")).xmap(BeehiveDecorator::new, beehiveDecorator -> Float.valueOf(beehiveDecorator.probability)).codec();
    private static final Direction WORLDGEN_FACING = Direction.SOUTH;
    private static final Direction[] SPAWN_DIRECTIONS = (Direction[])Direction.Plane.HORIZONTAL.stream().filter(direction -> direction != WORLDGEN_FACING.getOpposite()).toArray(Direction[]::new);
    private final float probability;

    public BeehiveDecorator(float f) {
        this.probability = f;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.BEEHIVE;
    }

    @Override
    public void place(TreeDecorator.Context context) {
        RandomSource randomSource = context.random();
        if (randomSource.nextFloat() >= this.probability) {
            return;
        }
        ObjectArrayList<BlockPos> list = context.leaves();
        ObjectArrayList<BlockPos> list2 = context.logs();
        int i = !list.isEmpty() ? Math.max(((BlockPos)list.get(0)).getY() - 1, ((BlockPos)list2.get(0)).getY() + 1) : Math.min(((BlockPos)list2.get(0)).getY() + 1 + randomSource.nextInt(3), ((BlockPos)list2.get(list2.size() - 1)).getY());
        List list3 = list2.stream().filter(blockPos -> blockPos.getY() == i).flatMap(blockPos -> Stream.of(SPAWN_DIRECTIONS).map(blockPos::relative)).collect(Collectors.toList());
        if (list3.isEmpty()) {
            return;
        }
        Collections.shuffle(list3);
        Optional<BlockPos> optional = list3.stream().filter(blockPos -> context.isAir((BlockPos)blockPos) && context.isAir(blockPos.relative(WORLDGEN_FACING))).findFirst();
        if (optional.isEmpty()) {
            return;
        }
        context.setBlock(optional.get(), (BlockState)Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.FACING, WORLDGEN_FACING));
        context.level().getBlockEntity(optional.get(), BlockEntityType.BEEHIVE).ifPresent(beehiveBlockEntity -> {
            int i = 2 + randomSource.nextInt(2);
            for (int j = 0; j < i; ++j) {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putString("id", Registry.ENTITY_TYPE.getKey(EntityType.BEE).toString());
                beehiveBlockEntity.storeBee(compoundTag, randomSource.nextInt(599), false);
            }
        });
    }
}

