/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core.dispenser;

import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class DefaultDispenseItemBehavior
implements DispenseItemBehavior {
    @Override
    public final ItemStack dispense(BlockSource blockSource, ItemStack itemStack) {
        ItemStack itemStack2 = this.execute(blockSource, itemStack);
        this.playSound(blockSource);
        this.playAnimation(blockSource, blockSource.getBlockState().getValue(DispenserBlock.FACING));
        return itemStack2;
    }

    protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
        Direction direction = blockSource.getBlockState().getValue(DispenserBlock.FACING);
        Position position = DispenserBlock.getDispensePosition(blockSource);
        ItemStack itemStack2 = itemStack.split(1);
        DefaultDispenseItemBehavior.spawnItem(blockSource.getLevel(), itemStack2, 6, direction, position);
        return itemStack;
    }

    public static void spawnItem(Level level, ItemStack itemStack, int i, Direction direction, Position position) {
        double d = position.x();
        double e = position.y();
        double f = position.z();
        e = direction.getAxis() == Direction.Axis.Y ? (e -= 0.125) : (e -= 0.15625);
        ItemEntity itemEntity = new ItemEntity(level, d, e, f, itemStack);
        double g = level.random.nextDouble() * 0.1 + 0.2;
        itemEntity.setDeltaMovement(level.random.triangle((double)direction.getStepX() * g, 0.0172275 * (double)i), level.random.triangle(0.2, 0.0172275 * (double)i), level.random.triangle((double)direction.getStepZ() * g, 0.0172275 * (double)i));
        level.addFreshEntity(itemEntity);
    }

    protected void playSound(BlockSource blockSource) {
        blockSource.getLevel().levelEvent(1000, blockSource.getPos(), 0);
    }

    protected void playAnimation(BlockSource blockSource, Direction direction) {
        blockSource.getLevel().levelEvent(2000, blockSource.getPos(), direction.get3DDataValue());
    }
}

