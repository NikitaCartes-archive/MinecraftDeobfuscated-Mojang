package net.minecraft.world.entity.ai.village.poi;

import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.state.BlockState;

public record PoiType(Set<BlockState> matchingStates, int maxTickets, int validRange) {
	public static final Predicate<Holder<PoiType>> NONE = holder -> false;

	public PoiType(Set<BlockState> matchingStates, int maxTickets, int validRange) {
		matchingStates = Set.copyOf(matchingStates);
		this.matchingStates = matchingStates;
		this.maxTickets = maxTickets;
		this.validRange = validRange;
	}

	public boolean is(BlockState blockState) {
		return this.matchingStates.contains(blockState);
	}
}
