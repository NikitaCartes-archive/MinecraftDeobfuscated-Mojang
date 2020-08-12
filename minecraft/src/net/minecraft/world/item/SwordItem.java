package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class SwordItem extends TieredItem implements Vanishable {
	private final Multimap<Attribute, AttributeModifier> defaultModifiers;

	public SwordItem(Tier tier, Item.Properties properties) {
		super(tier, properties);
		Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		WeaponType.SWORD.addCombatAttributes(this.getTier(), builder);
		this.defaultModifiers = builder.build();
	}

	public float getDamage() {
		return WeaponType.SWORD.getDamage(this.getTier());
	}

	@Override
	public boolean canAttackBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
		return !player.isCreative();
	}

	@Override
	public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
		if (blockState.is(Blocks.COBWEB)) {
			return 15.0F;
		} else {
			Material material = blockState.getMaterial();
			return material != Material.PLANT
					&& material != Material.REPLACEABLE_PLANT
					&& material != Material.CORAL
					&& !blockState.is(BlockTags.LEAVES)
					&& material != Material.VEGETABLE
				? 1.0F
				: 1.5F;
		}
	}

	@Override
	public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
		itemStack.hurtAndBreak(1, livingEntity2, livingEntityx -> livingEntityx.broadcastBreakEvent(EquipmentSlot.MAINHAND));
		return true;
	}

	@Override
	public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity) {
		if (blockState.getDestroySpeed(level, blockPos) != 0.0F) {
			itemStack.hurtAndBreak(2, livingEntity, livingEntityx -> livingEntityx.broadcastBreakEvent(EquipmentSlot.MAINHAND));
		}

		return true;
	}

	@Override
	public boolean isCorrectToolForDrops(BlockState blockState) {
		return blockState.is(Blocks.COBWEB);
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
		return equipmentSlot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(equipmentSlot);
	}
}
