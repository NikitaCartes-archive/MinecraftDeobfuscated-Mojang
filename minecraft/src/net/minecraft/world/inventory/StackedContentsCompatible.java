package net.minecraft.world.inventory;

import net.minecraft.world.entity.player.StackedItemContents;

@FunctionalInterface
public interface StackedContentsCompatible {
	void fillStackedContents(StackedItemContents stackedItemContents);
}
