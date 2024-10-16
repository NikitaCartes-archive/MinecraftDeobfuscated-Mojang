package net.minecraft.world.item;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MaceItem extends Item {
	private static final int DEFAULT_ATTACK_DAMAGE = 5;
	private static final float DEFAULT_ATTACK_SPEED = -3.4F;
	public static final float SMASH_ATTACK_FALL_THRESHOLD = 1.5F;
	private static final float SMASH_ATTACK_HEAVY_THRESHOLD = 5.0F;
	public static final float SMASH_ATTACK_KNOCKBACK_RADIUS = 3.5F;
	private static final float SMASH_ATTACK_KNOCKBACK_POWER = 0.7F;

	public MaceItem(Item.Properties properties) {
		super(properties);
	}

	public static ItemAttributeModifiers createAttributes() {
		return ItemAttributeModifiers.builder()
			.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 5.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
			.add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -3.4F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
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
	public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
		if (canSmashAttack(livingEntity2)) {
			ServerLevel serverLevel = (ServerLevel)livingEntity2.level();
			livingEntity2.setDeltaMovement(livingEntity2.getDeltaMovement().with(Direction.Axis.Y, 0.01F));
			if (livingEntity2 instanceof ServerPlayer serverPlayer) {
				serverPlayer.currentImpulseImpactPos = this.calculateImpactPosition(serverPlayer);
				serverPlayer.setIgnoreFallDamageFromCurrentImpulse(true);
				serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
			}

			if (livingEntity.onGround()) {
				if (livingEntity2 instanceof ServerPlayer serverPlayer) {
					serverPlayer.setSpawnExtraParticlesOnFall(true);
				}

				SoundEvent soundEvent = livingEntity2.fallDistance > 5.0F ? SoundEvents.MACE_SMASH_GROUND_HEAVY : SoundEvents.MACE_SMASH_GROUND;
				serverLevel.playSound(null, livingEntity2.getX(), livingEntity2.getY(), livingEntity2.getZ(), soundEvent, livingEntity2.getSoundSource(), 1.0F, 1.0F);
			} else {
				serverLevel.playSound(
					null, livingEntity2.getX(), livingEntity2.getY(), livingEntity2.getZ(), SoundEvents.MACE_SMASH_AIR, livingEntity2.getSoundSource(), 1.0F, 1.0F
				);
			}

			knockback(serverLevel, livingEntity2, livingEntity);
		}

		return true;
	}

	private Vec3 calculateImpactPosition(ServerPlayer serverPlayer) {
		return serverPlayer.isIgnoringFallDamageFromCurrentImpulse()
				&& serverPlayer.currentImpulseImpactPos != null
				&& serverPlayer.currentImpulseImpactPos.y <= serverPlayer.position().y
			? serverPlayer.currentImpulseImpactPos
			: serverPlayer.position();
	}

	@Override
	public void postHurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
		itemStack.hurtAndBreak(1, livingEntity2, EquipmentSlot.MAINHAND);
		if (canSmashAttack(livingEntity2)) {
			livingEntity2.resetFallDistance();
		}
	}

	@Override
	public float getAttackDamageBonus(Entity entity, float f, DamageSource damageSource) {
		if (damageSource.getDirectEntity() instanceof LivingEntity livingEntity) {
			if (!canSmashAttack(livingEntity)) {
				return 0.0F;
			} else {
				float g = 3.0F;
				float h = 8.0F;
				float i = livingEntity.fallDistance;
				float j;
				if (i <= 3.0F) {
					j = 4.0F * i;
				} else if (i <= 8.0F) {
					j = 12.0F + 2.0F * (i - 3.0F);
				} else {
					j = 22.0F + i - 8.0F;
				}

				return livingEntity.level() instanceof ServerLevel serverLevel
					? j + EnchantmentHelper.modifyFallBasedDamage(serverLevel, livingEntity.getWeaponItem(), entity, damageSource, 0.0F) * i
					: j;
			}
		} else {
			return 0.0F;
		}
	}

	private static void knockback(Level level, Entity entity, Entity entity2) {
		level.levelEvent(2013, entity2.getOnPos(), 750);
		level.getEntitiesOfClass(LivingEntity.class, entity2.getBoundingBox().inflate(3.5), knockbackPredicate(entity, entity2)).forEach(livingEntity -> {
			Vec3 vec3 = livingEntity.position().subtract(entity2.position());
			double d = getKnockbackPower(entity, livingEntity, vec3);
			Vec3 vec32 = vec3.normalize().scale(d);
			if (d > 0.0) {
				livingEntity.push(vec32.x, 0.7F, vec32.z);
				if (livingEntity instanceof ServerPlayer serverPlayer) {
					serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
				}
			}
		});
	}

	private static Predicate<LivingEntity> knockbackPredicate(Entity entity, Entity entity2) {
		return livingEntity -> {
			boolean bl;
			boolean bl2;
			boolean bl3;
			boolean var10000;
			label62: {
				bl = !livingEntity.isSpectator();
				bl2 = livingEntity != entity && livingEntity != entity2;
				bl3 = !entity.isAlliedTo(livingEntity);
				if (livingEntity instanceof TamableAnimal tamableAnimal && tamableAnimal.isTame() && entity.getUUID().equals(tamableAnimal.getOwnerUUID())) {
					var10000 = true;
					break label62;
				}

				var10000 = false;
			}

			boolean bl4;
			label55: {
				bl4 = !var10000;
				if (livingEntity instanceof ArmorStand armorStand && armorStand.isMarker()) {
					var10000 = false;
					break label55;
				}

				var10000 = true;
			}

			boolean bl5 = var10000;
			boolean bl6 = entity2.distanceToSqr(livingEntity) <= Math.pow(3.5, 2.0);
			return bl && bl2 && bl3 && bl4 && bl5 && bl6;
		};
	}

	private static double getKnockbackPower(Entity entity, LivingEntity livingEntity, Vec3 vec3) {
		return (3.5 - vec3.length()) * 0.7F * (double)(entity.fallDistance > 5.0F ? 2 : 1) * (1.0 - livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
	}

	public static boolean canSmashAttack(LivingEntity livingEntity) {
		return livingEntity.fallDistance > 1.5F && !livingEntity.isFallFlying();
	}

	@Nullable
	@Override
	public DamageSource getDamageSource(LivingEntity livingEntity) {
		return canSmashAttack(livingEntity) ? livingEntity.damageSources().mace(livingEntity) : super.getDamageSource(livingEntity);
	}
}
