package net.minecraft.world.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;

public interface EquipmentUser {
	void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack);

	ItemStack getItemBySlot(EquipmentSlot equipmentSlot);

	void setDropChance(EquipmentSlot equipmentSlot, float f);

	default void equip(EquipmentTable equipmentTable, LootParams lootParams) {
		this.equip(equipmentTable.lootTable(), lootParams, equipmentTable.slotDropChances());
	}

	default void equip(ResourceKey<LootTable> resourceKey, LootParams lootParams, Map<EquipmentSlot, Float> map) {
		this.equip(resourceKey, lootParams, 0L, map);
	}

	default void equip(ResourceKey<LootTable> resourceKey, LootParams lootParams, long l, Map<EquipmentSlot, Float> map) {
		if (!resourceKey.equals(BuiltInLootTables.EMPTY)) {
			LootTable lootTable = lootParams.getLevel().getServer().reloadableRegistries().getLootTable(resourceKey);
			if (lootTable != LootTable.EMPTY) {
				List<ItemStack> list = lootTable.getRandomItems(lootParams, l);
				List<EquipmentSlot> list2 = new ArrayList();

				for (ItemStack itemStack : list) {
					EquipmentSlot equipmentSlot = this.resolveSlot(itemStack, list2);
					if (equipmentSlot != null) {
						ItemStack itemStack2 = equipmentSlot.limit(itemStack);
						this.setItemSlot(equipmentSlot, itemStack2);
						Float float_ = (Float)map.get(equipmentSlot);
						if (float_ != null) {
							this.setDropChance(equipmentSlot, float_);
						}

						list2.add(equipmentSlot);
					}
				}
			}
		}
	}

	@Nullable
	default EquipmentSlot resolveSlot(ItemStack itemStack, List<EquipmentSlot> list) {
		if (itemStack.isEmpty()) {
			return null;
		} else {
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
}
