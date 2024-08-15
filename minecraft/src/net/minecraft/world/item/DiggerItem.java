package net.minecraft.world.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;

public class DiggerItem extends Item {
	protected DiggerItem(ToolMaterial toolMaterial, TagKey<Block> tagKey, float f, float g, Item.Properties properties) {
		super(toolMaterial.applyToolProperties(properties, tagKey, f, g));
	}

	@Override
	public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
		return true;
	}

	@Override
	public void postHurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
		itemStack.hurtAndBreak(2, livingEntity2, EquipmentSlot.MAINHAND);
	}
}
