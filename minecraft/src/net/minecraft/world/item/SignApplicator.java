package net.minecraft.world.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;

public interface SignApplicator {
	boolean tryApplyToSign(Level level, SignBlockEntity signBlockEntity, boolean bl, Player player);

	default boolean canApplyToSign(SignText signText, Player player) {
		return signText.hasMessage(player);
	}
}
