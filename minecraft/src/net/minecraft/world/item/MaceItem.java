package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class MaceItem extends Item {
	private static final int DEFAULT_ATTACK_DAMAGE = 6;
	private static final float DEFAULT_ATTACK_SPEED = -2.4F;
	public static final float SMASH_ATTACK_FALL_THRESHOLD = 1.5F;
	private static final ImmutableMultimap<Holder<Attribute>, AttributeModifier> ATTRIBUTES = ImmutableMultimap.<Holder<Attribute>, AttributeModifier>builder()
		.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 6.0, AttributeModifier.Operation.ADD_VALUE))
		.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.4F, AttributeModifier.Operation.ADD_VALUE))
		.build();

	public MaceItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public boolean canAttackBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
		return !player.isCreative();
	}

	@Override
	public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
		return blockState.is(Blocks.COBWEB) ? 15.0F : 1.5F;
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
			if (serverPlayer.ignoreFallDamageAboveY == null || serverPlayer.ignoreFallDamageAboveY > serverPlayer.getY()) {
				serverPlayer.ignoreFallDamageAboveY = serverPlayer.getY();
			}

			if (livingEntity.onGround()) {
				serverPlayer.setSpawnExtraParticlesOnFall(true);
				serverLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.MACE_SMASH_GROUND, SoundSource.NEUTRAL, 1.0F, 1.0F);
			} else {
				serverLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.MACE_SMASH_AIR, SoundSource.NEUTRAL, 1.0F, 1.0F);
			}
		}

		return true;
	}

	@Override
	public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity) {
		if (blockState.getDestroySpeed(level, blockPos) != 0.0F) {
			itemStack.hurtAndBreak(2, livingEntity, EquipmentSlot.MAINHAND);
		}

		return true;
	}

	@Override
	public boolean isCorrectToolForDrops(BlockState blockState) {
		return blockState.is(Blocks.COBWEB);
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
}
