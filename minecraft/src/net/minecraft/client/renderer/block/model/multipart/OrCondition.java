package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.collect.Streams;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

@Environment(EnvType.CLIENT)
public class OrCondition implements Condition {
	public static final String TOKEN = "OR";
	private final Iterable<? extends Condition> conditions;

	public OrCondition(Iterable<? extends Condition> iterable) {
		this.conditions = iterable;
	}

	@Override
	public Predicate<BlockState> getPredicate(StateDefinition<Block, BlockState> stateDefinition) {
		return Util.anyOf(Streams.stream(this.conditions).map(condition -> condition.getPredicate(stateDefinition)).toList());
	}
}
