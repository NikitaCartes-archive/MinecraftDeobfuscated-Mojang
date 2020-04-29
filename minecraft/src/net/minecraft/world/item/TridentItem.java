package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TridentItem extends Item implements Vanishable {
	private final Multimap<Attribute, AttributeModifier> defaultModifiers;

	public TridentItem(Item.Properties properties) {
		super(properties);
		Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", 8.0, AttributeModifier.Operation.ADDITION));
		builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -2.9F, AttributeModifier.Operation.ADDITION));
		this.defaultModifiers = builder.build();
	}

	@Override
	public boolean canAttackBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
		return !player.isCreative();
	}

	@Override
	public UseAnim getUseAnimation(ItemStack itemStack) {
		return UseAnim.SPEAR;
	}

	@Override
	public int getUseDuration(ItemStack itemStack) {
		return 72000;
	}

	@Override
	public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i) {
		if (livingEntity instanceof Player) {
			Player player = (Player)livingEntity;
			int j = this.getUseDuration(itemStack) - i;
			if (j >= 10) {
				int k = EnchantmentHelper.getRiptide(itemStack);
				if (k <= 0 || player.isInWaterOrRain()) {
					if (!level.isClientSide) {
						itemStack.hurtAndBreak(1, player, playerx -> playerx.broadcastBreakEvent(livingEntity.getUsedItemHand()));
						if (k == 0) {
							ThrownTrident thrownTrident = new ThrownTrident(level, player, itemStack);
							thrownTrident.shootFromRotation(player, player.xRot, player.yRot, 0.0F, 2.5F + (float)k * 0.5F, 1.0F);
							if (player.abilities.instabuild) {
								thrownTrident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
							}

							level.addFreshEntity(thrownTrident);
							level.playSound(null, thrownTrident, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
							if (!player.abilities.instabuild) {
								player.inventory.removeItem(itemStack);
							}
						}
					}

					player.awardStat(Stats.ITEM_USED.get(this));
					if (k > 0) {
						float f = player.yRot;
						float g = player.xRot;
						float h = -Mth.sin(f * (float) (Math.PI / 180.0)) * Mth.cos(g * (float) (Math.PI / 180.0));
						float l = -Mth.sin(g * (float) (Math.PI / 180.0));
						float m = Mth.cos(f * (float) (Math.PI / 180.0)) * Mth.cos(g * (float) (Math.PI / 180.0));
						float n = Mth.sqrt(h * h + l * l + m * m);
						float o = 3.0F * ((1.0F + (float)k) / 4.0F);
						h *= o / n;
						l *= o / n;
						m *= o / n;
						player.push((double)h, (double)l, (double)m);
						player.startAutoSpinAttack(20);
						if (player.isOnGround()) {
							float p = 1.1999999F;
							player.move(MoverType.SELF, new Vec3(0.0, 1.1999999F, 0.0));
						}

						SoundEvent soundEvent;
						if (k >= 3) {
							soundEvent = SoundEvents.TRIDENT_RIPTIDE_3;
						} else if (k == 2) {
							soundEvent = SoundEvents.TRIDENT_RIPTIDE_2;
						} else {
							soundEvent = SoundEvents.TRIDENT_RIPTIDE_1;
						}

						level.playSound(null, player, soundEvent, SoundSource.PLAYERS, 1.0F, 1.0F);
					}
				}
			}
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (itemStack.getDamageValue() >= itemStack.getMaxDamage() - 1) {
			return InteractionResultHolder.fail(itemStack);
		} else if (EnchantmentHelper.getRiptide(itemStack) > 0 && !player.isInWaterOrRain()) {
			return InteractionResultHolder.fail(itemStack);
		} else {
			player.startUsingItem(interactionHand);
			return InteractionResultHolder.consume(itemStack);
		}
	}

	@Override
	public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
		itemStack.hurtAndBreak(1, livingEntity2, livingEntityx -> livingEntityx.broadcastBreakEvent(EquipmentSlot.MAINHAND));
		return true;
	}

	@Override
	public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity) {
		if ((double)blockState.getDestroySpeed(level, blockPos) != 0.0) {
			itemStack.hurtAndBreak(2, livingEntity, livingEntityx -> livingEntityx.broadcastBreakEvent(EquipmentSlot.MAINHAND));
		}

		return true;
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
		return equipmentSlot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(equipmentSlot);
	}

	@Override
	public int getEnchantmentValue() {
		return 1;
	}
}
