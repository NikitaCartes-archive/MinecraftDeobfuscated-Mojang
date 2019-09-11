package net.minecraft.world.item;

import com.google.common.collect.Lists;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CrossbowItem extends ProjectileWeaponItem {
	private boolean startSoundPlayed = false;
	private boolean midLoadSoundPlayed = false;

	public CrossbowItem(Item.Properties properties) {
		super(properties);
		this.addProperty(new ResourceLocation("pull"), (itemStack, level, livingEntity) -> {
			if (livingEntity == null || itemStack.getItem() != this) {
				return 0.0F;
			} else {
				return isCharged(itemStack) ? 0.0F : (float)(itemStack.getUseDuration() - livingEntity.getUseItemRemainingTicks()) / (float)getChargeDuration(itemStack);
			}
		});
		this.addProperty(
			new ResourceLocation("pulling"),
			(itemStack, level, livingEntity) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack && !isCharged(itemStack)
					? 1.0F
					: 0.0F
		);
		this.addProperty(new ResourceLocation("charged"), (itemStack, level, livingEntity) -> livingEntity != null && isCharged(itemStack) ? 1.0F : 0.0F);
		this.addProperty(
			new ResourceLocation("firework"),
			(itemStack, level, livingEntity) -> livingEntity != null && isCharged(itemStack) && containsChargedProjectile(itemStack, Items.FIREWORK_ROCKET)
					? 1.0F
					: 0.0F
		);
	}

	@Override
	public Predicate<ItemStack> getSupportedHeldProjectiles() {
		return ARROW_OR_FIREWORK;
	}

	@Override
	public Predicate<ItemStack> getAllSupportedProjectiles() {
		return ARROW_ONLY;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (isCharged(itemStack)) {
			performShooting(level, player, interactionHand, itemStack, getShootingPower(itemStack), 1.0F);
			setCharged(itemStack, false);
			return InteractionResultHolder.successNoSwing(itemStack);
		} else if (!player.getProjectile(itemStack).isEmpty()) {
			if (!isCharged(itemStack)) {
				this.startSoundPlayed = false;
				this.midLoadSoundPlayed = false;
				player.startUsingItem(interactionHand);
			}

			return InteractionResultHolder.successNoSwing(itemStack);
		} else {
			return InteractionResultHolder.fail(itemStack);
		}
	}

	@Override
	public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i) {
		int j = this.getUseDuration(itemStack) - i;
		float f = getPowerForTime(j, itemStack);
		if (f >= 1.0F && !isCharged(itemStack) && tryLoadProjectiles(livingEntity, itemStack)) {
			setCharged(itemStack, true);
			SoundSource soundSource = livingEntity instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
			level.playSound(
				null, livingEntity.x, livingEntity.y, livingEntity.z, SoundEvents.CROSSBOW_LOADING_END, soundSource, 1.0F, 1.0F / (random.nextFloat() * 0.5F + 1.0F) + 0.2F
			);
		}
	}

	private static boolean tryLoadProjectiles(LivingEntity livingEntity, ItemStack itemStack) {
		int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, itemStack);
		int j = i == 0 ? 1 : 3;
		boolean bl = livingEntity instanceof Player && ((Player)livingEntity).abilities.instabuild;
		ItemStack itemStack2 = livingEntity.getProjectile(itemStack);
		ItemStack itemStack3 = itemStack2.copy();

		for (int k = 0; k < j; k++) {
			if (k > 0) {
				itemStack2 = itemStack3.copy();
			}

			if (itemStack2.isEmpty() && bl) {
				itemStack2 = new ItemStack(Items.ARROW);
				itemStack3 = itemStack2.copy();
			}

			if (!loadProjectile(livingEntity, itemStack, itemStack2, k > 0, bl)) {
				return false;
			}
		}

		return true;
	}

	private static boolean loadProjectile(LivingEntity livingEntity, ItemStack itemStack, ItemStack itemStack2, boolean bl, boolean bl2) {
		if (itemStack2.isEmpty()) {
			return false;
		} else {
			boolean bl3 = bl2 && itemStack2.getItem() instanceof ArrowItem;
			ItemStack itemStack3;
			if (!bl3 && !bl2 && !bl) {
				itemStack3 = itemStack2.split(1);
				if (itemStack2.isEmpty() && livingEntity instanceof Player) {
					((Player)livingEntity).inventory.removeItem(itemStack2);
				}
			} else {
				itemStack3 = itemStack2.copy();
			}

			addChargedProjectile(itemStack, itemStack3);
			return true;
		}
	}

	public static boolean isCharged(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTag();
		return compoundTag != null && compoundTag.getBoolean("Charged");
	}

	public static void setCharged(ItemStack itemStack, boolean bl) {
		CompoundTag compoundTag = itemStack.getOrCreateTag();
		compoundTag.putBoolean("Charged", bl);
	}

	private static void addChargedProjectile(ItemStack itemStack, ItemStack itemStack2) {
		CompoundTag compoundTag = itemStack.getOrCreateTag();
		ListTag listTag;
		if (compoundTag.contains("ChargedProjectiles", 9)) {
			listTag = compoundTag.getList("ChargedProjectiles", 10);
		} else {
			listTag = new ListTag();
		}

		CompoundTag compoundTag2 = new CompoundTag();
		itemStack2.save(compoundTag2);
		listTag.add(compoundTag2);
		compoundTag.put("ChargedProjectiles", listTag);
	}

	private static List<ItemStack> getChargedProjectiles(ItemStack itemStack) {
		List<ItemStack> list = Lists.<ItemStack>newArrayList();
		CompoundTag compoundTag = itemStack.getTag();
		if (compoundTag != null && compoundTag.contains("ChargedProjectiles", 9)) {
			ListTag listTag = compoundTag.getList("ChargedProjectiles", 10);
			if (listTag != null) {
				for (int i = 0; i < listTag.size(); i++) {
					CompoundTag compoundTag2 = listTag.getCompound(i);
					list.add(ItemStack.of(compoundTag2));
				}
			}
		}

		return list;
	}

	private static void clearChargedProjectiles(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTag();
		if (compoundTag != null) {
			ListTag listTag = compoundTag.getList("ChargedProjectiles", 9);
			listTag.clear();
			compoundTag.put("ChargedProjectiles", listTag);
		}
	}

	private static boolean containsChargedProjectile(ItemStack itemStack, Item item) {
		return getChargedProjectiles(itemStack).stream().anyMatch(itemStackx -> itemStackx.getItem() == item);
	}

	private static void shootProjectile(
		Level level,
		LivingEntity livingEntity,
		InteractionHand interactionHand,
		ItemStack itemStack,
		ItemStack itemStack2,
		float f,
		boolean bl,
		float g,
		float h,
		float i
	) {
		if (!level.isClientSide) {
			boolean bl2 = itemStack2.getItem() == Items.FIREWORK_ROCKET;
			Projectile projectile;
			if (bl2) {
				projectile = new FireworkRocketEntity(level, itemStack2, livingEntity.x, livingEntity.y + (double)livingEntity.getEyeHeight() - 0.15F, livingEntity.z, true);
			} else {
				projectile = getArrow(level, livingEntity, itemStack, itemStack2);
				if (bl || i != 0.0F) {
					((AbstractArrow)projectile).pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
				}
			}

			if (livingEntity instanceof CrossbowAttackMob) {
				CrossbowAttackMob crossbowAttackMob = (CrossbowAttackMob)livingEntity;
				crossbowAttackMob.shootProjectile(crossbowAttackMob.getTarget(), itemStack, projectile, i);
			} else {
				Vec3 vec3 = livingEntity.getUpVector(1.0F);
				Quaternion quaternion = new Quaternion(new Vector3f(vec3), i, true);
				Vec3 vec32 = livingEntity.getViewVector(1.0F);
				Vector3f vector3f = new Vector3f(vec32);
				vector3f.transform(quaternion);
				projectile.shoot((double)vector3f.x(), (double)vector3f.y(), (double)vector3f.z(), g, h);
			}

			itemStack.hurtAndBreak(bl2 ? 3 : 1, livingEntity, livingEntityx -> livingEntityx.broadcastBreakEvent(interactionHand));
			level.addFreshEntity((Entity)projectile);
			level.playSound(null, livingEntity.x, livingEntity.y, livingEntity.z, SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, f);
		}
	}

	private static AbstractArrow getArrow(Level level, LivingEntity livingEntity, ItemStack itemStack, ItemStack itemStack2) {
		ArrowItem arrowItem = (ArrowItem)(itemStack2.getItem() instanceof ArrowItem ? itemStack2.getItem() : Items.ARROW);
		AbstractArrow abstractArrow = arrowItem.createArrow(level, itemStack2, livingEntity);
		if (livingEntity instanceof Player) {
			abstractArrow.setCritArrow(true);
		}

		abstractArrow.setSoundEvent(SoundEvents.CROSSBOW_HIT);
		abstractArrow.setShotFromCrossbow(true);
		int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, itemStack);
		if (i > 0) {
			abstractArrow.setPierceLevel((byte)i);
		}

		return abstractArrow;
	}

	public static void performShooting(Level level, LivingEntity livingEntity, InteractionHand interactionHand, ItemStack itemStack, float f, float g) {
		List<ItemStack> list = getChargedProjectiles(itemStack);
		float[] fs = getShotPitches(livingEntity.getRandom());

		for (int i = 0; i < list.size(); i++) {
			ItemStack itemStack2 = (ItemStack)list.get(i);
			boolean bl = livingEntity instanceof Player && ((Player)livingEntity).abilities.instabuild;
			if (!itemStack2.isEmpty()) {
				if (i == 0) {
					shootProjectile(level, livingEntity, interactionHand, itemStack, itemStack2, fs[i], bl, f, g, 0.0F);
				} else if (i == 1) {
					shootProjectile(level, livingEntity, interactionHand, itemStack, itemStack2, fs[i], bl, f, g, -10.0F);
				} else if (i == 2) {
					shootProjectile(level, livingEntity, interactionHand, itemStack, itemStack2, fs[i], bl, f, g, 10.0F);
				}
			}
		}

		onCrossbowShot(level, livingEntity, itemStack);
	}

	private static float[] getShotPitches(Random random) {
		boolean bl = random.nextBoolean();
		return new float[]{1.0F, getRandomShotPitch(bl), getRandomShotPitch(!bl)};
	}

	private static float getRandomShotPitch(boolean bl) {
		float f = bl ? 0.63F : 0.43F;
		return 1.0F / (random.nextFloat() * 0.5F + 1.8F) + f;
	}

	private static void onCrossbowShot(Level level, LivingEntity livingEntity, ItemStack itemStack) {
		if (livingEntity instanceof ServerPlayer) {
			ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
			if (!level.isClientSide) {
				CriteriaTriggers.SHOT_CROSSBOW.trigger(serverPlayer, itemStack);
			}

			serverPlayer.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
		}

		clearChargedProjectiles(itemStack);
	}

	@Override
	public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int i) {
		if (!level.isClientSide) {
			int j = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, itemStack);
			SoundEvent soundEvent = this.getStartSound(j);
			SoundEvent soundEvent2 = j == 0 ? SoundEvents.CROSSBOW_LOADING_MIDDLE : null;
			float f = (float)(itemStack.getUseDuration() - i) / (float)getChargeDuration(itemStack);
			if (f < 0.2F) {
				this.startSoundPlayed = false;
				this.midLoadSoundPlayed = false;
			}

			if (f >= 0.2F && !this.startSoundPlayed) {
				this.startSoundPlayed = true;
				level.playSound(null, livingEntity.x, livingEntity.y, livingEntity.z, soundEvent, SoundSource.PLAYERS, 0.5F, 1.0F);
			}

			if (f >= 0.5F && soundEvent2 != null && !this.midLoadSoundPlayed) {
				this.midLoadSoundPlayed = true;
				level.playSound(null, livingEntity.x, livingEntity.y, livingEntity.z, soundEvent2, SoundSource.PLAYERS, 0.5F, 1.0F);
			}
		}
	}

	@Override
	public int getUseDuration(ItemStack itemStack) {
		return getChargeDuration(itemStack) + 3;
	}

	public static int getChargeDuration(ItemStack itemStack) {
		int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, itemStack);
		return i == 0 ? 25 : 25 - 5 * i;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack itemStack) {
		return UseAnim.CROSSBOW;
	}

	private SoundEvent getStartSound(int i) {
		switch (i) {
			case 1:
				return SoundEvents.CROSSBOW_QUICK_CHARGE_1;
			case 2:
				return SoundEvents.CROSSBOW_QUICK_CHARGE_2;
			case 3:
				return SoundEvents.CROSSBOW_QUICK_CHARGE_3;
			default:
				return SoundEvents.CROSSBOW_LOADING_START;
		}
	}

	private static float getPowerForTime(int i, ItemStack itemStack) {
		float f = (float)i / (float)getChargeDuration(itemStack);
		if (f > 1.0F) {
			f = 1.0F;
		}

		return f;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		List<ItemStack> list2 = getChargedProjectiles(itemStack);
		if (isCharged(itemStack) && !list2.isEmpty()) {
			ItemStack itemStack2 = (ItemStack)list2.get(0);
			list.add(new TranslatableComponent("item.minecraft.crossbow.projectile").append(" ").append(itemStack2.getDisplayName()));
			if (tooltipFlag.isAdvanced() && itemStack2.getItem() == Items.FIREWORK_ROCKET) {
				List<Component> list3 = Lists.<Component>newArrayList();
				Items.FIREWORK_ROCKET.appendHoverText(itemStack2, level, list3, tooltipFlag);
				if (!list3.isEmpty()) {
					for (int i = 0; i < list3.size(); i++) {
						list3.set(i, new TextComponent("  ").append((Component)list3.get(i)).withStyle(ChatFormatting.GRAY));
					}

					list.addAll(list3);
				}
			}
		}
	}

	private static float getShootingPower(ItemStack itemStack) {
		return itemStack.getItem() == Items.CROSSBOW && containsChargedProjectile(itemStack, Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
	}
}
