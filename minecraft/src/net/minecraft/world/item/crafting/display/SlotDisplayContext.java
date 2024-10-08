package net.minecraft.world.item.crafting.display;

import net.minecraft.core.HolderLookup;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.FuelValues;

public class SlotDisplayContext {
	public static final ContextKey<FuelValues> FUEL_VALUES = ContextKey.vanilla("fuel_values");
	public static final ContextKey<HolderLookup.Provider> REGISTRIES = ContextKey.vanilla("registries");
	public static final ContextKeySet CONTEXT = new ContextKeySet.Builder().optional(FUEL_VALUES).optional(REGISTRIES).build();

	public static ContextMap fromLevel(Level level) {
		return new ContextMap.Builder().withParameter(FUEL_VALUES, level.fuelValues()).withParameter(REGISTRIES, level.registryAccess()).create(CONTEXT);
	}
}
