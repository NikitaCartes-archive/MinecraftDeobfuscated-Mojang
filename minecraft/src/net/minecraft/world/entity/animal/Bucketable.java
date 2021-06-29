package net.minecraft.world.entity.animal;

import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public interface Bucketable {
	boolean fromBucket();

	void setFromBucket(boolean bl);

	void saveToBucketTag(ItemStack itemStack);

	void loadFromBucketTag(CompoundTag compoundTag);

	ItemStack getBucketItemStack();

	SoundEvent getPickupSound();

	@Deprecated
	static void saveDefaultDataToBucketTag(Mob mob, ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getOrCreateTag();
		if (mob.hasCustomName()) {
			itemStack.setHoverName(mob.getCustomName());
		}

		if (mob.isNoAi()) {
			compoundTag.putBoolean("NoAI", mob.isNoAi());
		}

		if (mob.isSilent()) {
			compoundTag.putBoolean("Silent", mob.isSilent());
		}

		if (mob.isNoGravity()) {
			compoundTag.putBoolean("NoGravity", mob.isNoGravity());
		}

		if (mob.hasGlowingTag()) {
			compoundTag.putBoolean("Glowing", mob.hasGlowingTag());
		}

		if (mob.isInvulnerable()) {
			compoundTag.putBoolean("Invulnerable", mob.isInvulnerable());
		}

		compoundTag.putFloat("Health", mob.getHealth());
	}

	@Deprecated
	static void loadDefaultDataFromBucketTag(Mob mob, CompoundTag compoundTag) {
		if (compoundTag.contains("NoAI")) {
			mob.setNoAi(compoundTag.getBoolean("NoAI"));
		}

		if (compoundTag.contains("Silent")) {
			mob.setSilent(compoundTag.getBoolean("Silent"));
		}

		if (compoundTag.contains("NoGravity")) {
			mob.setNoGravity(compoundTag.getBoolean("NoGravity"));
		}

		if (compoundTag.contains("Glowing")) {
			mob.setGlowingTag(compoundTag.getBoolean("Glowing"));
		}

		if (compoundTag.contains("Invulnerable")) {
			mob.setInvulnerable(compoundTag.getBoolean("Invulnerable"));
		}

		if (compoundTag.contains("Health", 99)) {
			mob.setHealth(compoundTag.getFloat("Health"));
		}
	}

	static <T extends LivingEntity & Bucketable> Optional<InteractionResult> bucketMobPickup(Player player, InteractionHand interactionHand, T livingEntity) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (itemStack.getItem() == Items.WATER_BUCKET && livingEntity.isAlive()) {
			livingEntity.playSound(livingEntity.getPickupSound(), 1.0F, 1.0F);
			ItemStack itemStack2 = livingEntity.getBucketItemStack();
			livingEntity.saveToBucketTag(itemStack2);
			ItemStack itemStack3 = ItemUtils.createFilledResult(itemStack, player, itemStack2, false);
			player.setItemInHand(interactionHand, itemStack3);
			Level level = livingEntity.level;
			if (!level.isClientSide) {
				CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer)player, itemStack2);
			}

			livingEntity.discard();
			return Optional.of(InteractionResult.sidedSuccess(level.isClientSide));
		} else {
			return Optional.empty();
		}
	}
}
