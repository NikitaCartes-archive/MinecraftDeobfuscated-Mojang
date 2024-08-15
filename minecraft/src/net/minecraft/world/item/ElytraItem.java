package net.minecraft.world.item;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class ElytraItem extends Item implements Equipable {
	public ElytraItem(Item.Properties properties) {
		super(properties);
		DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
	}

	public static boolean isFlyEnabled(ItemStack itemStack) {
		return itemStack.getDamageValue() < itemStack.getMaxDamage() - 1;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
		return this.swapWithEquipmentSlot(this, level, player, interactionHand);
	}

	@Override
	public Holder<SoundEvent> getEquipSound() {
		return SoundEvents.ARMOR_EQUIP_ELYTRA;
	}

	@Override
	public EquipmentSlot getEquipmentSlot() {
		return EquipmentSlot.CHEST;
	}
}
