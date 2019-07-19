package net.minecraft.world.level.storage;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public interface PlayerIO {
	void save(Player player);

	@Nullable
	CompoundTag load(Player player);
}
