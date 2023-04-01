package net.minecraft.world.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class BottleOfVoidItem extends Item {
	public BottleOfVoidItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		livingEntity.gameEvent(GameEvent.DRINK);
		if (!level.isClientSide && livingEntity instanceof Player player) {
			player.hurt(player.damageSources().outOfWorld(), 8.0F);
			MobEffectInstance mobEffectInstance = removeRandomEffect(player);
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);
			if (mobEffectInstance != null) {
				ItemStack itemStack2 = PotionUtils.setCustomEffects(new ItemStack(Items.POTION), List.of(mobEffectInstance));
				return ItemUtils.createFilledResult(itemStack, player, itemStack2);
			} else {
				return removeTransform(player);
			}
		} else {
			return itemStack;
		}
	}

	@Nullable
	private static MobEffectInstance removeRandomEffect(LivingEntity livingEntity) {
		ArrayList<MobEffectInstance> arrayList = new ArrayList(livingEntity.getActiveEffects());
		if (!arrayList.isEmpty()) {
			MobEffectInstance mobEffectInstance = Util.getRandom(arrayList, livingEntity.getRandom());
			livingEntity.removeEffect(mobEffectInstance.getEffect());
			return new MobEffectInstance(mobEffectInstance);
		} else {
			return null;
		}
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
		return player.getActiveEffects().isEmpty() && player.getTransform().entity() == null
			? InteractionResultHolder.fail(player.getItemInHand(interactionHand))
			: ItemUtils.startUsingInstantly(level, player, interactionHand);
	}

	public static ItemStack removeTransform(LivingEntity livingEntity) {
		LivingEntity livingEntity2 = livingEntity;
		if (livingEntity.getTransform().entity() instanceof LivingEntity livingEntity3) {
			livingEntity.updateTransform(entityTransformType -> entityTransformType.withEntity(Optional.empty()));
			livingEntity = livingEntity3;
		}

		CompoundTag compoundTag = new CompoundTag();
		livingEntity.saveAsPassenger(compoundTag);
		ItemStack itemStack = Items.BOTTLE_OF_ENTITY.getDefaultInstance();
		CompoundTag compoundTag2 = new CompoundTag();
		compoundTag2.put("entityTag", compoundTag);
		itemStack.setTag(compoundTag2);
		if (livingEntity2 == livingEntity) {
			livingEntity.hurt(livingEntity.damageSources().outOfWorld(), 1.0F);
		}

		return itemStack;
	}
}
