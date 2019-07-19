package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;

public class DyeItem extends Item {
	private static final Map<DyeColor, DyeItem> ITEM_BY_COLOR = Maps.newEnumMap(DyeColor.class);
	private final DyeColor dyeColor;

	public DyeItem(DyeColor dyeColor, Item.Properties properties) {
		super(properties);
		this.dyeColor = dyeColor;
		ITEM_BY_COLOR.put(dyeColor, this);
	}

	@Override
	public boolean interactEnemy(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
		if (livingEntity instanceof Sheep) {
			Sheep sheep = (Sheep)livingEntity;
			if (sheep.isAlive() && !sheep.isSheared() && sheep.getColor() != this.dyeColor) {
				sheep.setColor(this.dyeColor);
				itemStack.shrink(1);
			}

			return true;
		} else {
			return false;
		}
	}

	public DyeColor getDyeColor() {
		return this.dyeColor;
	}

	public static DyeItem byColor(DyeColor dyeColor) {
		return (DyeItem)ITEM_BY_COLOR.get(dyeColor);
	}
}
