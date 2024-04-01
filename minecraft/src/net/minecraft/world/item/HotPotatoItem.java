package net.minecraft.world.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.InventoryHeatComponent;

public class HotPotatoItem extends Item {
	public static final int ONE_MILLION = 1000000;
	public static final int DAMAGE_POTATO_HEAT = 20;
	public static final int MAX_POTATO_HEAT = 200;

	public HotPotatoItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		livingEntity.setRemainingFireTicks(livingEntity.getRemainingFireTicks() + 1000000);
		return super.finishUsingItem(itemStack, level, livingEntity);
	}

	@Override
	public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean bl) {
		super.inventoryTick(itemStack, level, entity, i, bl);
		InventoryHeatComponent inventoryHeatComponent = itemStack.get(DataComponents.INVENTORY_HEAT);
		if (inventoryHeatComponent != null && entity.getUUID() == inventoryHeatComponent.owner() && i == inventoryHeatComponent.slot()) {
			if (inventoryHeatComponent.heat() < 200) {
				itemStack.set(DataComponents.INVENTORY_HEAT, new InventoryHeatComponent(entity.getUUID(), i, inventoryHeatComponent.heat() + 1));
			}

			int j = Mth.lerpDiscrete((float)(inventoryHeatComponent.heat() - 20) / 180.0F, 0, 5);
			if (j > 0) {
				entity.hurt(entity.damageSources().potatoHeat(), (float)j);
			}
		} else {
			itemStack.set(DataComponents.INVENTORY_HEAT, new InventoryHeatComponent(entity.getUUID(), i, 0));
		}
	}
}
