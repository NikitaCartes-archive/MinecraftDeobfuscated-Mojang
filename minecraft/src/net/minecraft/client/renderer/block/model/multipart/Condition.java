package net.minecraft.client.renderer.block.model.multipart;

import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface Condition {
	Condition TRUE = stateDefinition -> blockState -> true;
	Condition FALSE = stateDefinition -> blockState -> false;

	Predicate<BlockState> getPredicate(StateDefinition<Block, BlockState> stateDefinition);
}
