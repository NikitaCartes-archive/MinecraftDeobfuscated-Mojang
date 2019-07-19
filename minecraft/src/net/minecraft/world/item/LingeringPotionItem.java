package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;

public class LingeringPotionItem extends PotionItem {
	public LingeringPotionItem(Item.Properties properties) {
		super(properties);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		PotionUtils.addPotionTooltip(itemStack, list, 0.25F);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		ItemStack itemStack2 = player.abilities.instabuild ? itemStack.copy() : itemStack.split(1);
		level.playSound(null, player.x, player.y, player.z, SoundEvents.LINGERING_POTION_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
		if (!level.isClientSide) {
			ThrownPotion thrownPotion = new ThrownPotion(level, player);
			thrownPotion.setItem(itemStack2);
			thrownPotion.shootFromRotation(player, player.xRot, player.yRot, -20.0F, 0.5F, 1.0F);
			level.addFreshEntity(thrownPotion);
		}

		player.awardStat(Stats.ITEM_USED.get(this));
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
	}
}
