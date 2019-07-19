package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

@Environment(EnvType.CLIENT)
public class KeyValueCondition implements Condition {
	private static final Splitter PIPE_SPLITTER = Splitter.on('|').omitEmptyStrings();
	private final String key;
	private final String value;

	public KeyValueCondition(String string, String string2) {
		this.key = string;
		this.value = string2;
	}

	@Override
	public Predicate<BlockState> getPredicate(StateDefinition<Block, BlockState> stateDefinition) {
		Property<?> property = stateDefinition.getProperty(this.key);
		if (property == null) {
			throw new RuntimeException(String.format("Unknown property '%s' on '%s'", this.key, stateDefinition.getOwner().toString()));
		} else {
			String string = this.value;
			boolean bl = !string.isEmpty() && string.charAt(0) == '!';
			if (bl) {
				string = string.substring(1);
			}

			List<String> list = PIPE_SPLITTER.splitToList(string);
			if (list.isEmpty()) {
				throw new RuntimeException(String.format("Empty value '%s' for property '%s' on '%s'", this.value, this.key, stateDefinition.getOwner().toString()));
			} else {
				Predicate<BlockState> predicate;
				if (list.size() == 1) {
					predicate = this.getBlockStatePredicate(stateDefinition, property, string);
				} else {
					List<Predicate<BlockState>> list2 = (List<Predicate<BlockState>>)list.stream()
						.map(stringx -> this.getBlockStatePredicate(stateDefinition, property, stringx))
						.collect(Collectors.toList());
					predicate = blockState -> list2.stream().anyMatch(predicatex -> predicatex.test(blockState));
				}

				return bl ? predicate.negate() : predicate;
			}
		}
	}

	private Predicate<BlockState> getBlockStatePredicate(StateDefinition<Block, BlockState> stateDefinition, Property<?> property, String string) {
		Optional<?> optional = property.getValue(string);
		if (!optional.isPresent()) {
			throw new RuntimeException(
				String.format("Unknown value '%s' for property '%s' on '%s' in '%s'", string, this.key, stateDefinition.getOwner().toString(), this.value)
			);
		} else {
			return blockState -> blockState.getValue(property).equals(optional.get());
		}
	}

	public String toString() {
		return MoreObjects.toStringHelper(this).add("key", this.key).add("value", this.value).toString();
	}
}
