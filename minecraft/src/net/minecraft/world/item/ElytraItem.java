package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class ElytraItem extends Item implements Wearable {
	public ElytraItem(Item.Properties properties) {
		super(properties);
		DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
	}

	public static boolean isFlyEnabled(ItemStack itemStack) {
		return itemStack.getDamageValue() < itemStack.getMaxDamage() - 1;
	}

	@Override
	public boolean isValidRepairItem(ItemStack itemStack, ItemStack itemStack2) {
		return itemStack2.is(Items.PHANTOM_MEMBRANE);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(itemStack);
		ItemStack itemStack2 = player.getItemBySlot(equipmentSlot);
		if (itemStack2.isEmpty()) {
			player.setItemSlot(equipmentSlot, itemStack.copy());
			if (!level.isClientSide()) {
				player.awardStat(Stats.ITEM_USED.get(this));
			}

			itemStack.setCount(0);
			return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
		} else {
			return InteractionResultHolder.fail(itemStack);
		}
	}

	@Nullable
	@Override
	public SoundEvent getEquipSound() {
		return SoundEvents.ARMOR_EQUIP_ELYTRA;
	}
}
