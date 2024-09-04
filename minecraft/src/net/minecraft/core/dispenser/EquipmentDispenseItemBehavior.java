package net.minecraft.core.dispenser;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;

public class EquipmentDispenseItemBehavior extends DefaultDispenseItemBehavior {
	public static final EquipmentDispenseItemBehavior INSTANCE = new EquipmentDispenseItemBehavior();

	@Override
	protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
		return dispenseEquipment(blockSource, itemStack) ? itemStack : super.execute(blockSource, itemStack);
	}

	public static boolean dispenseEquipment(BlockSource blockSource, ItemStack itemStack) {
		BlockPos blockPos = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING));
		List<LivingEntity> list = blockSource.level()
			.getEntitiesOfClass(LivingEntity.class, new AABB(blockPos), livingEntityx -> livingEntityx.canEquipWithDispenser(itemStack));
		if (list.isEmpty()) {
			return false;
		} else {
			LivingEntity livingEntity = (LivingEntity)list.getFirst();
			EquipmentSlot equipmentSlot = livingEntity.getEquipmentSlotForItem(itemStack);
			ItemStack itemStack2 = itemStack.split(1);
			livingEntity.setItemSlot(equipmentSlot, itemStack2);
			if (livingEntity instanceof Mob mob) {
				mob.setDropChance(equipmentSlot, 2.0F);
				mob.setPersistenceRequired();
			}

			return true;
		}
	}
}
