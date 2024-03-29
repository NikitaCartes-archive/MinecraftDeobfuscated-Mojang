package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.DensityEnchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MaceItem extends Item {
	private static final int DEFAULT_ATTACK_DAMAGE = 6;
	private static final float DEFAULT_ATTACK_SPEED = -2.4F;
	private static final float SMASH_ATTACK_FALL_THRESHOLD = 1.5F;
	private static final float SMASH_ATTACK_HEAVY_THRESHOLD = 5.0F;
	public static final float SMASH_ATTACK_KNOCKBACK_RADIUS = 3.5F;
	private static final float SMASH_ATTACK_KNOCKBACK_POWER = 0.7F;
	private static final float SMASH_ATTACK_FALL_DISTANCE_MULTIPLIER = 3.0F;
	private static final ImmutableMultimap<Holder<Attribute>, AttributeModifier> ATTRIBUTES = ImmutableMultimap.builder()
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
	public int getEnchantmentValue() {
		return 15;
	}

	@Override
	public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
		itemStack.hurtAndBreak(1, livingEntity2, EquipmentSlot.MAINHAND);
		if (livingEntity2 instanceof ServerPlayer serverPlayer && canSmashAttack(serverPlayer)) {
			ServerLevel serverLevel = (ServerLevel)livingEntity2.level();
			if (!serverPlayer.ignoreFallDamageFromCurrentImpulse
				|| serverPlayer.currentImpulseImpactPos == null
				|| serverPlayer.currentImpulseImpactPos.y() > serverPlayer.getY()) {
				serverPlayer.currentImpulseImpactPos = serverPlayer.position();
				serverPlayer.ignoreFallDamageFromCurrentImpulse = true;
			}

			serverPlayer.setDeltaMovement(serverPlayer.getDeltaMovement().with(Direction.Axis.Y, 0.0));
			serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
			if (livingEntity.onGround()) {
				serverPlayer.setSpawnExtraParticlesOnFall(true);
				SoundEvent soundEvent = serverPlayer.fallDistance > 5.0F ? SoundEvents.MACE_SMASH_GROUND_HEAVY : SoundEvents.MACE_SMASH_GROUND;
				serverLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), soundEvent, serverPlayer.getSoundSource(), 1.0F, 1.0F);
			} else {
				serverLevel.playSound(
					null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.MACE_SMASH_AIR, serverPlayer.getSoundSource(), 1.0F, 1.0F
				);
			}

			knockback(serverLevel, serverPlayer, livingEntity);
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
		int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.DENSITY, player);
		float g = DensityEnchantment.calculateDamageAddition(i, player.fallDistance);
		return canSmashAttack(player) ? 3.0F * player.fallDistance + g : 0.0F;
	}

	private static void knockback(Level level, Player player, Entity entity) {
		level.levelEvent(2013, entity.getOnPos(), 750);
		level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(3.5), knockbackPredicate(player, entity)).forEach(livingEntity -> {
			Vec3 vec3 = livingEntity.position().subtract(entity.position());
			double d = getKnockbackPower(player, livingEntity, vec3);
			Vec3 vec32 = vec3.normalize().scale(d);
			if (d > 0.0) {
				livingEntity.push(vec32.x, 0.7F, vec32.z);
			}
		});
	}

	private static Predicate<LivingEntity> knockbackPredicate(Player player, Entity entity) {
		return livingEntity -> {
			boolean bl;
			boolean bl2;
			boolean bl3;
			boolean var10000;
			label44: {
				bl = !livingEntity.isSpectator();
				bl2 = livingEntity != player && livingEntity != entity;
				bl3 = !player.isAlliedTo(livingEntity);
				if (livingEntity instanceof ArmorStand armorStand && armorStand.isMarker()) {
					var10000 = false;
					break label44;
				}

				var10000 = true;
			}

			boolean bl4 = var10000;
			boolean bl5 = entity.distanceToSqr(livingEntity) <= Math.pow(3.5, 2.0);
			return bl && bl2 && bl3 && bl4 && bl5;
		};
	}

	private static double getKnockbackPower(Player player, LivingEntity livingEntity, Vec3 vec3) {
		return (3.5 - vec3.length()) * 0.7F * (double)(player.fallDistance > 5.0F ? 2 : 1) * (1.0 - livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
	}

	public static boolean canSmashAttack(Player player) {
		return player.fallDistance > 1.5F && !player.isFallFlying();
	}
}
