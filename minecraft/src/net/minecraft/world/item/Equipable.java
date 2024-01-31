package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public interface Equipable {
	EquipmentSlot getEquipmentSlot();

	default Holder<SoundEvent> getEquipSound() {
		return SoundEvents.ARMOR_EQUIP_GENERIC;
	}

	default InteractionResultHolder<ItemStack> swapWithEquipmentSlot(Item item, Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(itemStack);
		ItemStack itemStack2 = player.getItemBySlot(equipmentSlot);
		if ((!EnchantmentHelper.hasBindingCurse(itemStack2) || player.isCreative()) && !ItemStack.matches(itemStack, itemStack2)) {
			if (!level.isClientSide()) {
				player.awardStat(Stats.ITEM_USED.get(item));
			}

			ItemStack itemStack3 = itemStack2.isEmpty() ? itemStack : itemStack2.copyAndClear();
			ItemStack itemStack4 = player.isCreative() ? itemStack.copy() : itemStack.copyAndClear();
			player.setItemSlot(equipmentSlot, itemStack4);
			return InteractionResultHolder.sidedSuccess(itemStack3, level.isClientSide());
		} else {
			return InteractionResultHolder.fail(itemStack);
		}
	}

	@Nullable
	static Equipable get(ItemStack itemStack) {
		Item equipable2 = itemStack.getItem();
		if (equipable2 instanceof Equipable) {
			return (Equipable)equipable2;
		} else {
			if (itemStack.getItem() instanceof BlockItem blockItem) {
				Block var6 = blockItem.getBlock();
				if (var6 instanceof Equipable) {
					return (Equipable)var6;
				}
			}

			return null;
		}
	}
}
