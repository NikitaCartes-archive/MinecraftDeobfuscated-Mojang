package net.minecraft.world.item;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.Level;

public class ThrowablePotionItem extends PotionItem {
	public ThrowablePotionItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (!level.isClientSide) {
			ThrownPotion thrownPotion = new ThrownPotion(level, player);
			thrownPotion.setItem(itemStack);
			thrownPotion.shootFromRotation(player, player.xRot, player.yRot, -20.0F, 0.5F, 1.0F);
			level.addFreshEntity(thrownPotion);
		}

		player.awardStat(Stats.ITEM_USED.get(this));
		if (!player.abilities.instabuild) {
			itemStack.shrink(1);
		}

		return InteractionResultHolder.success(itemStack);
	}
}
