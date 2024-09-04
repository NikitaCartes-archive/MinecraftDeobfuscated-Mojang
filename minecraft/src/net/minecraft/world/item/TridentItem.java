package net.minecraft.world.item;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TridentItem extends Item implements ProjectileItem {
	public static final int THROW_THRESHOLD_TIME = 10;
	public static final float BASE_DAMAGE = 8.0F;
	public static final float SHOOT_POWER = 2.5F;

	public TridentItem(Item.Properties properties) {
		super(properties);
	}

	public static ItemAttributeModifiers createAttributes() {
		return ItemAttributeModifiers.builder()
			.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 8.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
			.add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.9F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
			.build();
	}

	public static Tool createToolProperties() {
		return new Tool(List.of(), 1.0F, 2);
	}

	@Override
	public boolean canAttackBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
		return !player.isCreative();
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
		return ItemUseAnimation.SPEAR;
	}

	@Override
	public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
		return 72000;
	}

	@Override
	public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i) {
		if (livingEntity instanceof Player player) {
			int j = this.getUseDuration(itemStack, livingEntity) - i;
			if (j < 10) {
				return false;
			} else {
				float f = EnchantmentHelper.getTridentSpinAttackStrength(itemStack, player);
				if (f > 0.0F && !player.isInWaterOrRain()) {
					return false;
				} else if (itemStack.nextDamageWillBreak()) {
					return false;
				} else {
					Holder<SoundEvent> holder = (Holder<SoundEvent>)EnchantmentHelper.pickHighestLevel(itemStack, EnchantmentEffectComponents.TRIDENT_SOUND)
						.orElse(SoundEvents.TRIDENT_THROW);
					if (level instanceof ServerLevel serverLevel) {
						itemStack.hurtWithoutBreaking(1, player);
						if (f == 0.0F) {
							ThrownTrident thrownTrident = Projectile.spawnProjectileFromRotation(ThrownTrident::new, serverLevel, itemStack, player, 0.0F, 2.5F, 1.0F);
							if (player.hasInfiniteMaterials()) {
								thrownTrident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
							} else {
								player.getInventory().removeItem(itemStack);
							}

							level.playSound(null, thrownTrident, holder.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
							return true;
						}
					}

					player.awardStat(Stats.ITEM_USED.get(this));
					if (f > 0.0F) {
						float g = player.getYRot();
						float h = player.getXRot();
						float k = -Mth.sin(g * (float) (Math.PI / 180.0)) * Mth.cos(h * (float) (Math.PI / 180.0));
						float l = -Mth.sin(h * (float) (Math.PI / 180.0));
						float m = Mth.cos(g * (float) (Math.PI / 180.0)) * Mth.cos(h * (float) (Math.PI / 180.0));
						float n = Mth.sqrt(k * k + l * l + m * m);
						k *= f / n;
						l *= f / n;
						m *= f / n;
						player.push((double)k, (double)l, (double)m);
						player.startAutoSpinAttack(20, 8.0F, itemStack);
						if (player.onGround()) {
							float o = 1.1999999F;
							player.move(MoverType.SELF, new Vec3(0.0, 1.1999999F, 0.0));
						}

						level.playSound(null, player, holder.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
						return true;
					} else {
						return false;
					}
				}
			}
		} else {
			return false;
		}
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (itemStack.nextDamageWillBreak()) {
			return InteractionResult.FAIL;
		} else if (EnchantmentHelper.getTridentSpinAttackStrength(itemStack, player) > 0.0F && !player.isInWaterOrRain()) {
			return InteractionResult.FAIL;
		} else {
			player.startUsingItem(interactionHand);
			return InteractionResult.CONSUME;
		}
	}

	@Override
	public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
		return true;
	}

	@Override
	public void postHurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
		itemStack.hurtAndBreak(1, livingEntity2, EquipmentSlot.MAINHAND);
	}

	@Override
	public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
		ThrownTrident thrownTrident = new ThrownTrident(level, position.x(), position.y(), position.z(), itemStack.copyWithCount(1));
		thrownTrident.pickup = AbstractArrow.Pickup.ALLOWED;
		return thrownTrident;
	}
}
