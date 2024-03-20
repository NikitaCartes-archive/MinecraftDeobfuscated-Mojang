package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MaceItem extends Item {
	private static final int DEFAULT_ATTACK_DAMAGE = 6;
	private static final float DEFAULT_ATTACK_SPEED = -2.4F;
	private static final float SMASH_ATTACK_FALL_THRESHOLD = 1.5F;
	private static final float SMASH_ATTACK_KNOCKBACK_RADIUS = 2.5F;
	private static final float SMASH_ATTACK_KNOCKBACK_POWER = 0.6F;
	private static final ImmutableMultimap<Holder<Attribute>, AttributeModifier> ATTRIBUTES = ImmutableMultimap.<Holder<Attribute>, AttributeModifier>builder()
		.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 6.0, AttributeModifier.Operation.ADD_VALUE))
		.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.4F, AttributeModifier.Operation.ADD_VALUE))
		.build();

	public MaceItem(Item.Properties properties) {
		super(properties);
	}

	public static Tool createToolProperties() {
		return new Tool(List.of(), 1.0F, 2);
	}

	@Override
	public boolean canAttackBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
		return !player.isCreative();
	}

	@Override
	public boolean isEnchantable(ItemStack itemStack) {
		return false;
	}

	@Override
	public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
		itemStack.hurtAndBreak(1, livingEntity2, EquipmentSlot.MAINHAND);
		if (livingEntity2 instanceof ServerPlayer serverPlayer && serverPlayer.fallDistance > 1.5F) {
			ServerLevel serverLevel = (ServerLevel)livingEntity2.level();
			if (!serverPlayer.ignoreFallDamageFromCurrentImpulse
				|| serverPlayer.currentImpulseImpactPos == null
				|| serverPlayer.currentImpulseImpactPos.y() > serverPlayer.getY()) {
				serverPlayer.currentImpulseImpactPos = serverPlayer.position();
				serverPlayer.ignoreFallDamageFromCurrentImpulse = true;
			}

			if (livingEntity.onGround()) {
				serverPlayer.setSpawnExtraParticlesOnFall(true);
				SoundEvent soundEvent = serverPlayer.fallDistance > 5.0F ? SoundEvents.MACE_SMASH_GROUND_HEAVY : SoundEvents.MACE_SMASH_GROUND;
				serverLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), soundEvent, SoundSource.NEUTRAL, 1.0F, 1.0F);
			} else {
				serverLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.MACE_SMASH_AIR, SoundSource.NEUTRAL, 1.0F, 1.0F);
			}

			this.knockback(serverLevel, serverPlayer, livingEntity);
		}

		return true;
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
		return (Multimap<Holder<Attribute>, AttributeModifier>)(equipmentSlot == EquipmentSlot.MAINHAND
			? ATTRIBUTES
			: super.getDefaultAttributeModifiers(equipmentSlot));
	}

	@Override
	public boolean isValidRepairItem(ItemStack itemStack, ItemStack itemStack2) {
		return itemStack2.is(Items.BREEZE_ROD);
	}

	@Override
	public float getAttackDamageBonus(Player player, float f) {
		return player.fallDistance > 1.5F ? f * 0.5F * player.fallDistance : 0.0F;
	}

	private void knockback(Level level, Player player, Entity entity) {
		level.getEntitiesOfClass(
				LivingEntity.class,
				entity.getBoundingBox().inflate(2.5),
				livingEntity -> livingEntity != player
						&& livingEntity != entity
						&& !entity.isAlliedTo(livingEntity)
						&& (!(livingEntity instanceof ArmorStand armorStand) || !armorStand.isMarker())
						&& entity.distanceToSqr(livingEntity) <= Math.pow(2.5, 2.0)
			)
			.forEach(
				livingEntity -> {
					Vec3 vec3 = livingEntity.position().subtract(entity.position());
					double d = (2.5 - vec3.length()) * 0.6F * (1.0 - livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
					Vec3 vec32 = vec3.normalize().scale(d);
					if (d > 0.0) {
						livingEntity.push(vec32.x, 0.6F, vec32.z);
						if (level instanceof ServerLevel serverLevel) {
							BlockPos blockPos = livingEntity.getOnPos();
							Vec3 vec33 = blockPos.getCenter().add(0.0, 0.5, 0.0);
							int i = (int)(100.0 * d);
							serverLevel.sendParticles(
								new BlockParticleOption(ParticleTypes.BLOCK, serverLevel.getBlockState(blockPos)), vec33.x, vec33.y, vec33.z, i, 0.3F, 0.3F, 0.3F, 0.15F
							);
						}
					}
				}
			);
	}
}
