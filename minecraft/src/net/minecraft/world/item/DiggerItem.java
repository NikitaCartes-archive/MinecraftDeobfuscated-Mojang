package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class DiggerItem extends TieredItem {
	private final TagKey<Block> blocks;
	protected final float speed;

	protected DiggerItem(Tier tier, TagKey<Block> tagKey, Item.Properties properties) {
		super(tier, properties);
		this.blocks = tagKey;
		this.speed = tier.getSpeed();
	}

	public static ItemAttributeModifiers createAttributes(Tier tier, float f, float g) {
		return ItemAttributeModifiers.builder()
			.add(
				Attributes.ATTACK_DAMAGE,
				new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", (double)(f + tier.getAttackDamageBonus()), AttributeModifier.Operation.ADD_VALUE),
				EquipmentSlotGroup.MAINHAND
			)
			.add(
				Attributes.ATTACK_SPEED,
				new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", (double)g, AttributeModifier.Operation.ADD_VALUE),
				EquipmentSlotGroup.MAINHAND
			)
			.build();
	}

	@Override
	public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
		return blockState.is(this.blocks) ? this.speed : 1.0F;
	}

	@Override
	public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
		itemStack.hurtAndBreak(2, livingEntity2, EquipmentSlot.MAINHAND);
		return true;
	}

	@Override
	public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity) {
		if (!level.isClientSide && blockState.getDestroySpeed(level, blockPos) != 0.0F) {
			itemStack.hurtAndBreak(1, livingEntity, EquipmentSlot.MAINHAND);
		}

		return true;
	}

	@Override
	public boolean isCorrectToolForDrops(BlockState blockState) {
		int i = this.getTier().getLevel();
		if (i < 3 && blockState.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
			return false;
		} else if (i < 2 && blockState.is(BlockTags.NEEDS_IRON_TOOL)) {
			return false;
		} else {
			return i < 1 && blockState.is(BlockTags.NEEDS_STONE_TOOL) ? false : blockState.is(this.blocks);
		}
	}
}
