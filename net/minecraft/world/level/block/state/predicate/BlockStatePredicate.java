/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.state.predicate;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public class BlockStatePredicate
implements Predicate<BlockState> {
    public static final Predicate<BlockState> ANY = blockState -> true;
    private final StateDefinition<Block, BlockState> definition;
    private final Map<Property<?>, Predicate<Object>> properties = Maps.newHashMap();

    private BlockStatePredicate(StateDefinition<Block, BlockState> stateDefinition) {
        this.definition = stateDefinition;
    }

    public static BlockStatePredicate forBlock(Block block) {
        return new BlockStatePredicate(block.getStateDefinition());
    }

    @Override
    public boolean test(@Nullable BlockState blockState) {
        if (blockState == null || !blockState.getBlock().equals(this.definition.getOwner())) {
            return false;
        }
        if (this.properties.isEmpty()) {
            return true;
        }
        for (Map.Entry<Property<?>, Predicate<Object>> entry : this.properties.entrySet()) {
            if (this.applies(blockState, entry.getKey(), entry.getValue())) continue;
            return false;
        }
        return true;
    }

    protected <T extends Comparable<T>> boolean applies(BlockState blockState, Property<T> property, Predicate<Object> predicate) {
        T comparable = blockState.getValue(property);
        return predicate.test(comparable);
    }

    public <V extends Comparable<V>> BlockStatePredicate where(Property<V> property, Predicate<Object> predicate) {
        if (!this.definition.getProperties().contains(property)) {
            throw new IllegalArgumentException(this.definition + " cannot support property " + property);
        }
        this.properties.put(property, predicate);
        return this;
    }

    @Override
    public /* synthetic */ boolean test(@Nullable Object object) {
        return this.test((BlockState)object);
    }
}

