package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class PotionItem extends Item {
	public PotionItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack getDefaultInstance() {
		return PotionUtils.setPotion(super.getDefaultInstance(), Potions.WATER);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		Player player = livingEntity instanceof Player ? (Player)livingEntity : null;
		if (player instanceof ServerPlayer) {
			CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)player, itemStack);
		}

		if (!level.isClientSide) {
			for (MobEffectInstance mobEffectInstance : PotionUtils.getMobEffects(itemStack)) {
				if (mobEffectInstance.getEffect().isInstantenous()) {
					mobEffectInstance.getEffect().applyInstantenousEffect(player, player, livingEntity, mobEffectInstance.getAmplifier(), 1.0);
				} else {
					livingEntity.addEffect(new MobEffectInstance(mobEffectInstance));
				}
			}
		}

		if (player != null) {
			player.awardStat(Stats.ITEM_USED.get(this));
			if (!player.getAbilities().instabuild) {
				itemStack.shrink(1);
			}
		}

		if (player == null || !player.getAbilities().instabuild) {
			if (itemStack.isEmpty()) {
				return new ItemStack(Items.GLASS_BOTTLE);
			}

			if (player != null) {
				player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
			}
		}

		level.gameEvent(livingEntity, GameEvent.DRINKING_FINISH, livingEntity.eyeBlockPosition());
		return itemStack;
	}

	@Override
	public int getUseDuration(ItemStack itemStack) {
		return 32;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack itemStack) {
		return UseAnim.DRINK;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		return ItemUtils.startUsingInstantly(level, player, interactionHand);
	}

	@Override
	public String getDescriptionId(ItemStack itemStack) {
		return PotionUtils.getPotion(itemStack).getName(this.getDescriptionId() + ".effect.");
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		PotionUtils.addPotionTooltip(itemStack, list, 1.0F);
	}

	@Override
	public boolean isFoil(ItemStack itemStack) {
		return super.isFoil(itemStack) || !PotionUtils.getMobEffects(itemStack).isEmpty();
	}

	@Override
	public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList) {
		if (this.allowdedIn(creativeModeTab)) {
			for (Potion potion : Registry.POTION) {
				if (potion != Potions.EMPTY) {
					nonNullList.add(PotionUtils.setPotion(new ItemStack(this), potion));
				}
			}
		}
	}
}
