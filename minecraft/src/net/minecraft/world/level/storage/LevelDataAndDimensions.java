package net.minecraft.world.level.storage;

import net.minecraft.world.level.levelgen.WorldDimensions;

public record LevelDataAndDimensions(WorldData worldData, WorldDimensions.Complete dimensions) {
}
