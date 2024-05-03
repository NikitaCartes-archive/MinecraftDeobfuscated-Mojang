package net.minecraft.world.item.enchantment;

import javax.annotation.Nullable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public record EnchantedItemInUse(ItemStack itemStack, @Nullable EquipmentSlot inSlot, @Nullable LivingEntity owner, Runnable onBreak) {
	public EnchantedItemInUse(ItemStack itemStack, EquipmentSlot equipmentSlot, LivingEntity livingEntity) {
		this(itemStack, equipmentSlot, livingEntity, () -> livingEntity.broadcastBreakEvent(equipmentSlot));
	}
}
