package net.minecraft.world.inventory.tooltip;

import net.minecraft.world.item.component.BundleContents;

public record BundleTooltip(BundleContents contents) implements TooltipComponent {
}
