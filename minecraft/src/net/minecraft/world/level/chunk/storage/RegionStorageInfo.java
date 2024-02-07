package net.minecraft.world.level.chunk.storage;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record RegionStorageInfo(String level, ResourceKey<Level> dimension, String type) {
	public RegionStorageInfo withTypeSuffix(String string) {
		return new RegionStorageInfo(this.level, this.dimension, this.type + string);
	}
}
