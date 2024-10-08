package net.minecraft.world.item.crafting.display;

import net.minecraft.core.Registry;

public class SlotDisplays {
	public static SlotDisplay.Type<?> bootstrap(Registry<SlotDisplay.Type<?>> registry) {
		Registry.register(registry, "empty", SlotDisplay.Empty.TYPE);
		Registry.register(registry, "any_fuel", SlotDisplay.AnyFuel.TYPE);
		Registry.register(registry, "item", SlotDisplay.ItemSlotDisplay.TYPE);
		Registry.register(registry, "item_stack", SlotDisplay.ItemStackSlotDisplay.TYPE);
		Registry.register(registry, "tag", SlotDisplay.TagSlotDisplay.TYPE);
		Registry.register(registry, "smithing_trim", SlotDisplay.SmithingTrimDemoSlotDisplay.TYPE);
		Registry.register(registry, "with_remainder", SlotDisplay.WithRemainder.TYPE);
		return Registry.register(registry, "composite", SlotDisplay.Composite.TYPE);
	}
}
