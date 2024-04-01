package net.minecraft.world.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.XpComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class PotatoOfKnowledgeItem extends Item {
	public PotatoOfKnowledgeItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public int getUseDuration(ItemStack itemStack) {
		return 20;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack itemStack) {
		return UseAnim.EAT;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		player.startUsingItem(interactionHand);
		ItemStack itemStack = player.getItemInHand(interactionHand);
		return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		if (livingEntity instanceof ServerPlayer serverPlayer) {
			int i = itemStack.getOrDefault(DataComponents.XP, XpComponent.DEFAULT).value();
			serverPlayer.giveExperiencePoints(i);
		}

		level.playSound(
			null,
			livingEntity.getX(),
			livingEntity.getY(),
			livingEntity.getZ(),
			SoundEvents.PLAYER_BURP,
			SoundSource.PLAYERS,
			1.0F,
			1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F
		);
		itemStack.consume(1, livingEntity);
		livingEntity.gameEvent(GameEvent.EAT);
		return itemStack;
	}
}
