package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public interface LootContextUser {
	default Set<LootContextParam<?>> getReferencedContextParams() {
		return ImmutableSet.of();
	}

	default void validate(
		LootTableProblemCollector lootTableProblemCollector,
		Function<ResourceLocation, LootTable> function,
		Set<ResourceLocation> set,
		LootContextParamSet lootContextParamSet
	) {
		lootContextParamSet.validateUser(lootTableProblemCollector, this);
	}
}
