package net.minecraft.world.entity;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;

public interface EquipmentUser {
	void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack);

	ItemStack getItemBySlot(EquipmentSlot equipmentSlot);

	void setDropChance(EquipmentSlot equipmentSlot, float f);

	default void equip(ResourceLocation resourceLocation, LootParams lootParams) {
		this.equip(resourceLocation, lootParams, 0L);
	}

	default void equip(ResourceLocation resourceLocation, LootParams lootParams, long l) {
		ResourceKey<LootTable> resourceKey = ResourceKey.create(Registries.LOOT_TABLE, resourceLocation);
		if (!resourceKey.equals(BuiltInLootTables.EMPTY)) {
			LootTable lootTable = lootParams.getLevel().getServer().reloadableRegistries().getLootTable(resourceKey);
			if (lootTable != LootTable.EMPTY) {
				List<ItemStack> list = lootTable.getRandomItems(lootParams, l);
				List<EquipmentSlot> list2 = new ArrayList();

				for (ItemStack itemStack : list) {
					EquipmentSlot equipmentSlot = this.resolveSlot(itemStack, list2);
					if (equipmentSlot != null) {
						ItemStack itemStack2 = equipmentSlot.isArmor() ? itemStack.copyWithCount(1) : itemStack;
						this.setItemSlot(equipmentSlot, itemStack2);
						this.setDropChance(equipmentSlot, 0.085F);
						list2.add(equipmentSlot);
					}
				}
			}
		}
	}

	@Nullable
	default EquipmentSlot resolveSlot(ItemStack itemStack, List<EquipmentSlot> list) {
		Equipable equipable = Equipable.get(itemStack);
		if (equipable != null) {
			EquipmentSlot equipmentSlot = equipable.getEquipmentSlot();
			if (!list.contains(equipmentSlot)) {
				return equipmentSlot;
			}
		} else if (!list.contains(EquipmentSlot.MAINHAND)) {
			return EquipmentSlot.MAINHAND;
		}

		return null;
	}
}
