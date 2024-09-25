package net.minecraft.world.entity.vehicle;

import java.util.function.Supplier;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class Boat extends AbstractBoat {
	public Boat(EntityType<? extends Boat> entityType, Level level, Supplier<Item> supplier) {
		super(entityType, level, supplier);
	}

	@Override
	protected double rideHeight(EntityDimensions entityDimensions) {
		return (double)(entityDimensions.height() / 3.0F);
	}
}
