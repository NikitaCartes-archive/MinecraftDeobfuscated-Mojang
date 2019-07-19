/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments.blocks;

import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public class BlockInput
implements Predicate<BlockInWorld> {
    private final BlockState state;
    private final Set<Property<?>> properties;
    @Nullable
    private final CompoundTag tag;

    public BlockInput(BlockState blockState, Set<Property<?>> set, @Nullable CompoundTag compoundTag) {
        this.state = blockState;
        this.properties = set;
        this.tag = compoundTag;
    }

    public BlockState getState() {
        return this.state;
    }

    @Override
    public boolean test(BlockInWorld blockInWorld) {
        BlockState blockState = blockInWorld.getState();
        if (blockState.getBlock() != this.state.getBlock()) {
            return false;
        }
        for (Property<?> property : this.properties) {
            if (blockState.getValue(property) == this.state.getValue(property)) continue;
            return false;
        }
        if (this.tag != null) {
            BlockEntity blockEntity = blockInWorld.getEntity();
            return blockEntity != null && NbtUtils.compareNbt(this.tag, blockEntity.save(new CompoundTag()), true);
        }
        return true;
    }

    public boolean place(ServerLevel serverLevel, BlockPos blockPos, int i) {
        BlockEntity blockEntity;
        if (!serverLevel.setBlock(blockPos, this.state, i)) {
            return false;
        }
        if (this.tag != null && (blockEntity = serverLevel.getBlockEntity(blockPos)) != null) {
            CompoundTag compoundTag = this.tag.copy();
            compoundTag.putInt("x", blockPos.getX());
            compoundTag.putInt("y", blockPos.getY());
            compoundTag.putInt("z", blockPos.getZ());
            blockEntity.load(compoundTag);
        }
        return true;
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((BlockInWorld)object);
    }
}

