package net.minecraft.world.item;

import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public class BowItem extends ProjectileWeaponItem {
	public BowItem(Item.Properties properties) {
		super(properties);
		this.addProperty(new ResourceLocation("pull"), (itemStack, level, livingEntity) -> {
			if (livingEntity == null) {
				return 0.0F;
			} else {
				return livingEntity.getUseItem().getItem() != Items.BOW ? 0.0F : (float)(itemStack.getUseDuration() - livingEntity.getUseItemRemainingTicks()) / 20.0F;
			}
		});
		this.addProperty(
			new ResourceLocation("pulling"),
			(itemStack, level, livingEntity) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack ? 1.0F : 0.0F
		);
	}

	@Override
	public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i) {
		if (livingEntity instanceof Player) {
			Player player = (Player)livingEntity;
			boolean bl = player.abilities.instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, itemStack) > 0;
			ItemStack itemStack2 = player.getProjectile(itemStack);
			if (!itemStack2.isEmpty() || bl) {
				if (itemStack2.isEmpty()) {
					itemStack2 = new ItemStack(Items.ARROW);
				}

				int j = this.getUseDuration(itemStack) - i;
				float f = getPowerForTime(j);
				if (!((double)f < 0.1)) {
					boolean bl2 = bl && itemStack2.getItem() == Items.ARROW;
					if (!level.isClientSide) {
						ArrowItem arrowItem = (ArrowItem)(itemStack2.getItem() instanceof ArrowItem ? itemStack2.getItem() : Items.ARROW);
						AbstractArrow abstractArrow = arrowItem.createArrow(level, itemStack2, player);
						abstractArrow.shootFromRotation(player, player.xRot, player.yRot, 0.0F, f * 3.0F, 1.0F);
						if (f == 1.0F) {
							abstractArrow.setCritArrow(true);
						}

						int k = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, itemStack);
						if (k > 0) {
							abstractArrow.setBaseDamage(abstractArrow.getBaseDamage() + (double)k * 0.5 + 0.5);
						}

						int l = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, itemStack);
						if (l > 0) {
							abstractArrow.setKnockback(l);
						}

						if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, itemStack) > 0) {
							abstractArrow.setSecondsOnFire(100);
						}

						itemStack.hurtAndBreak(1, player, player2 -> player2.broadcastBreakEvent(player.getUsedItemHand()));
						if (bl2 || player.abilities.instabuild && (itemStack2.getItem() == Items.SPECTRAL_ARROW || itemStack2.getItem() == Items.TIPPED_ARROW)) {
							abstractArrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
						}

						level.addFreshEntity(abstractArrow);
					}

					level.playSound(
						null,
						player.getX(),
						player.getY(),
						player.getZ(),
						SoundEvents.ARROW_SHOOT,
						SoundSource.PLAYERS,
						1.0F,
						1.0F / (random.nextFloat() * 0.4F + 1.2F) + f * 0.5F
					);
					if (!bl2 && !player.abilities.instabuild) {
						itemStack2.shrink(1);
						if (itemStack2.isEmpty()) {
							player.inventory.removeItem(itemStack2);
						}
					}

					player.awardStat(Stats.ITEM_USED.get(this));
				}
			}
		}
	}

	public static float getPowerForTime(int i) {
		float f = (float)i / 20.0F;
		f = (f * f + f * 2.0F) / 3.0F;
		if (f > 1.0F) {
			f = 1.0F;
		}

		return f;
	}

	@Override
	public int getUseDuration(ItemStack itemStack) {
		return 72000;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack itemStack) {
		return UseAnim.BOW;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		boolean bl = !player.getProjectile(itemStack).isEmpty();
		if (player.abilities.instabuild || bl) {
			player.startUsingItem(interactionHand);
			return InteractionResultHolder.successNoSwing(itemStack);
		} else {
			return bl ? InteractionResultHolder.pass(itemStack) : InteractionResultHolder.fail(itemStack);
		}
	}

	@Override
	public Predicate<ItemStack> getAllSupportedProjectiles() {
		return ARROW_ONLY;
	}
}
