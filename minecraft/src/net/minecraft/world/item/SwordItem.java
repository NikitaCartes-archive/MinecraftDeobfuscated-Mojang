package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SwordItem extends TieredItem {
	public SwordItem(Tier tier, Item.Properties properties) {
		super(tier, properties);
	}

	public static ItemAttributeModifiers createAttributes(Tier tier, int i, float f) {
		return ItemAttributeModifiers.builder()
			.add(
				Attributes.ATTACK_DAMAGE,
				new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", (double)((float)i + tier.getAttackDamageBonus()), AttributeModifier.Operation.ADD_VALUE),
				EquipmentSlotGroup.MAINHAND
			)
			.add(
				Attributes.ATTACK_SPEED,
				new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", (double)f, AttributeModifier.Operation.ADD_VALUE),
				EquipmentSlotGroup.MAINHAND
			)
			.build();
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
			return blockState.is(BlockTags.SWORD_EFFICIENT) ? 1.5F : 1.0F;
		}
	}

	@Override
	public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
		itemStack.hurtAndBreak(1, livingEntity2, EquipmentSlot.MAINHAND);
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
}
