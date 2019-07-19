package net.minecraft.core;

import net.minecraft.world.level.Level;

public interface Location extends Position {
	Level getLevel();
}
