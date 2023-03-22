package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public class GlowInkSacItem extends Item implements SignApplicator {
	public GlowInkSacItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public boolean tryApplyToSign(Level level, SignBlockEntity signBlockEntity, boolean bl, Player player) {
		if (signBlockEntity.updateText(signText -> signText.setHasGlowingText(true), bl)) {
			level.playSound(null, signBlockEntity.getBlockPos(), SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
			return true;
		} else {
			return false;
		}
	}
}
