package net.minecraft.world.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.SnekComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class VenomousPotatoItem extends Item {
	private static final Component VISIBLE_NAME = Component.translatable("item.minecraft.snektato.revealed");

	public VenomousPotatoItem(Item.Properties properties) {
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
	public Component getName(ItemStack itemStack) {
		SnekComponent snekComponent = itemStack.get(DataComponents.SNEK);
		return snekComponent != null && snekComponent.revealed() ? VISIBLE_NAME : super.getName(itemStack);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		player.startUsingItem(interactionHand);
		ItemStack itemStack = player.getItemInHand(interactionHand);
		return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		level.playSound(
			null,
			livingEntity.getX(),
			livingEntity.getY(),
			livingEntity.getZ(),
			SoundEvents.SPIDER_AMBIENT,
			SoundSource.PLAYERS,
			1.0F,
			1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F
		);
		CaveSpider.poisonMethodThatSpidersUse(livingEntity, null);
		livingEntity.gameEvent(GameEvent.EAT);
		livingEntity.hurt(level.damageSources().magic(), 2.0F);
		itemStack.set(DataComponents.SNEK, new SnekComponent(true));
		return itemStack;
	}
}
