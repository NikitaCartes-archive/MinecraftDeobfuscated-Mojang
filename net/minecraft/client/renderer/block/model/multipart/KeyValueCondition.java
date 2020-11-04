/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.multipart.Condition;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

@Environment(value=EnvType.CLIENT)
public class KeyValueCondition
implements Condition {
    private static final Splitter PIPE_SPLITTER = Splitter.on('|').omitEmptyStrings();
    private final String key;
    private final String value;

    public KeyValueCondition(String string, String string2) {
        this.key = string;
        this.value = string2;
    }

    @Override
    public Predicate<BlockState> getPredicate(StateDefinition<Block, BlockState> stateDefinition) {
        Predicate<BlockState> predicate;
        List<String> list;
        boolean bl;
        Property<?> property = stateDefinition.getProperty(this.key);
        if (property == null) {
            throw new RuntimeException(String.format("Unknown property '%s' on '%s'", this.key, stateDefinition.getOwner()));
        }
        String string2 = this.value;
        boolean bl2 = bl = !string2.isEmpty() && string2.charAt(0) == '!';
        if (bl) {
            string2 = string2.substring(1);
        }
        if ((list = PIPE_SPLITTER.splitToList(string2)).isEmpty()) {
            throw new RuntimeException(String.format("Empty value '%s' for property '%s' on '%s'", this.value, this.key, stateDefinition.getOwner()));
        }
        if (list.size() == 1) {
            predicate = this.getBlockStatePredicate(stateDefinition, property, string2);
        } else {
            List list2 = list.stream().map(string -> this.getBlockStatePredicate(stateDefinition, property, (String)string)).collect(Collectors.toList());
            predicate = blockState -> list2.stream().anyMatch(predicate -> predicate.test(blockState));
        }
        return bl ? predicate.negate() : predicate;
    }

    private Predicate<BlockState> getBlockStatePredicate(StateDefinition<Block, BlockState> stateDefinition, Property<?> property, String string) {
        Optional<?> optional = property.getValue(string);
        if (!optional.isPresent()) {
            throw new RuntimeException(String.format("Unknown value '%s' for property '%s' on '%s' in '%s'", string, this.key, stateDefinition.getOwner(), this.value));
        }
        return blockState -> blockState.getValue(property).equals(optional.get());
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).add("key", this.key).add("value", this.value).toString();
    }
}

