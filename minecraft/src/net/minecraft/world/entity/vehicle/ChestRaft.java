package net.minecraft.world.entity.vehicle;

import java.util.function.Supplier;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class ChestRaft extends AbstractChestBoat {
	public ChestRaft(EntityType<? extends ChestRaft> entityType, Level level, Supplier<Item> supplier) {
		super(entityType, level, supplier);
	}

	@Override
	protected double rideHeight(EntityDimensions entityDimensions) {
		return (double)(entityDimensions.height() * 0.8888889F);
	}
}
