package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

public record DebugStickState(Map<Holder<Block>, Property<?>> properties) {
	public static final DebugStickState EMPTY = new DebugStickState(Map.of());
	public static final Codec<DebugStickState> CODEC = Codec.dispatchedMap(
			BuiltInRegistries.BLOCK.holderByNameCodec(), holder -> Codec.STRING.comapFlatMap(string -> {
					Property<?> property = ((Block)holder.value()).getStateDefinition().getProperty(string);
					return property != null ? DataResult.success(property) : DataResult.error(() -> "No property on " + holder.getRegisteredName() + " with name: " + string);
				}, Property::getName)
		)
		.xmap(DebugStickState::new, DebugStickState::properties);

	public DebugStickState withProperty(Holder<Block> holder, Property<?> property) {
		return new DebugStickState(Util.copyAndPut(this.properties, holder, property));
	}
}
