/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Containers {
    public static void dropContents(Level level, BlockPos blockPos, Container container) {
        Containers.dropContents(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), container);
    }

    public static void dropContents(Level level, Entity entity, Container container) {
        Containers.dropContents(level, entity.getX(), entity.getY(), entity.getZ(), container);
    }

    private static void dropContents(Level level, double d, double e, double f, Container container) {
        for (int i = 0; i < container.getContainerSize(); ++i) {
            Containers.dropItemStack(level, d, e, f, container.getItem(i));
        }
    }

    public static void dropContents(Level level, BlockPos blockPos, NonNullList<ItemStack> nonNullList) {
        nonNullList.forEach(itemStack -> Containers.dropItemStack(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack));
    }

    public static void dropItemStack(Level level, double d, double e, double f, ItemStack itemStack) {
        double g = EntityType.ITEM.getWidth();
        double h = 1.0 - g;
        double i = g / 2.0;
        double j = Math.floor(d) + level.random.nextDouble() * h + i;
        double k = Math.floor(e) + level.random.nextDouble() * h;
        double l = Math.floor(f) + level.random.nextDouble() * h + i;
        while (!itemStack.isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(level, j, k, l, itemStack.split(level.random.nextInt(21) + 10));
            float m = 0.05f;
            itemEntity.setDeltaMovement(level.random.triangle(0.0, 0.11485000171139836), level.random.triangle(0.2, 0.11485000171139836), level.random.triangle(0.0, 0.11485000171139836));
            level.addFreshEntity(itemEntity);
        }
    }
}

