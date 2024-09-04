package net.minecraft.world.item;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class FoodOnAStickItem<T extends Entity & ItemSteerable> extends Item {
	private final EntityType<T> canInteractWith;
	private final int consumeItemDamage;

	public FoodOnAStickItem(EntityType<T> entityType, int i, Item.Properties properties) {
		super(properties);
		this.canInteractWith = entityType;
		this.consumeItemDamage = i;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (level.isClientSide) {
			return InteractionResult.PASS;
		} else {
			Entity entity = player.getControlledVehicle();
			if (player.isPassenger() && entity instanceof ItemSteerable itemSteerable && entity.getType() == this.canInteractWith && itemSteerable.boost()) {
				EquipmentSlot equipmentSlot = LivingEntity.getSlotForHand(interactionHand);
				ItemStack itemStack2 = itemStack.hurtAndConvertOnBreak(this.consumeItemDamage, Items.FISHING_ROD, player, equipmentSlot);
				return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(itemStack2);
			}

			player.awardStat(Stats.ITEM_USED.get(this));
			return InteractionResult.PASS;
		}
	}
}
