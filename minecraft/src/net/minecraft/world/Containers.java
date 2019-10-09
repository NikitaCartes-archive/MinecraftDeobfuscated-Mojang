package net.minecraft.world;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Containers {
	private static final Random RANDOM = new Random();

	public static void dropContents(Level level, BlockPos blockPos, Container container) {
		dropContents(level, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), container);
	}

	public static void dropContents(Level level, Entity entity, Container container) {
		dropContents(level, entity.getX(), entity.getY(), entity.getZ(), container);
	}

	private static void dropContents(Level level, double d, double e, double f, Container container) {
		for (int i = 0; i < container.getContainerSize(); i++) {
			dropItemStack(level, d, e, f, container.getItem(i));
		}
	}

	public static void dropContents(Level level, BlockPos blockPos, NonNullList<ItemStack> nonNullList) {
		nonNullList.forEach(itemStack -> dropItemStack(level, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), itemStack));
	}

	public static void dropItemStack(Level level, double d, double e, double f, ItemStack itemStack) {
		double g = (double)EntityType.ITEM.getWidth();
		double h = 1.0 - g;
		double i = g / 2.0;
		double j = Math.floor(d) + RANDOM.nextDouble() * h + i;
		double k = Math.floor(e) + RANDOM.nextDouble() * h;
		double l = Math.floor(f) + RANDOM.nextDouble() * h + i;

		while (!itemStack.isEmpty()) {
			ItemEntity itemEntity = new ItemEntity(level, j, k, l, itemStack.split(RANDOM.nextInt(21) + 10));
			float m = 0.05F;
			itemEntity.setDeltaMovement(RANDOM.nextGaussian() * 0.05F, RANDOM.nextGaussian() * 0.05F + 0.2F, RANDOM.nextGaussian() * 0.05F);
			level.addFreshEntity(itemEntity);
		}
	}
}
