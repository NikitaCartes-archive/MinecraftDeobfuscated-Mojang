/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.context;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BlockPlaceContext
extends UseOnContext {
    private final BlockPos relativePos;
    protected boolean replaceClicked = true;

    public BlockPlaceContext(Player player, InteractionHand interactionHand, ItemStack itemStack, BlockHitResult blockHitResult) {
        this(player.level, player, interactionHand, itemStack, blockHitResult);
    }

    public BlockPlaceContext(UseOnContext useOnContext) {
        this(useOnContext.getLevel(), useOnContext.getPlayer(), useOnContext.getHand(), useOnContext.getItemInHand(), useOnContext.getHitResult());
    }

    protected BlockPlaceContext(Level level, @Nullable Player player, InteractionHand interactionHand, ItemStack itemStack, BlockHitResult blockHitResult) {
        super(level, player, interactionHand, itemStack, blockHitResult);
        this.relativePos = blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
        this.replaceClicked = level.getBlockState(blockHitResult.getBlockPos()).canBeReplaced(this);
    }

    public static BlockPlaceContext at(BlockPlaceContext blockPlaceContext, BlockPos blockPos, Direction direction) {
        return new BlockPlaceContext(blockPlaceContext.getLevel(), blockPlaceContext.getPlayer(), blockPlaceContext.getHand(), blockPlaceContext.getItemInHand(), new BlockHitResult(new Vec3((double)blockPos.getX() + 0.5 + (double)direction.getStepX() * 0.5, (double)blockPos.getY() + 0.5 + (double)direction.getStepY() * 0.5, (double)blockPos.getZ() + 0.5 + (double)direction.getStepZ() * 0.5), direction, blockPos, false));
    }

    @Override
    public BlockPos getClickedPos() {
        return this.replaceClicked ? super.getClickedPos() : this.relativePos;
    }

    public boolean canPlace() {
        return this.replaceClicked || this.getLevel().getBlockState(this.getClickedPos()).canBeReplaced(this);
    }

    public boolean replacingClickedOnBlock() {
        return this.replaceClicked;
    }

    public Direction getNearestLookingDirection() {
        return Direction.orderedByNearest(this.getPlayer())[0];
    }

    public Direction[] getNearestLookingDirections() {
        int i;
        Direction[] directions = Direction.orderedByNearest(this.getPlayer());
        if (this.replaceClicked) {
            return directions;
        }
        Direction direction = this.getClickedFace();
        for (i = 0; i < directions.length && directions[i] != direction.getOpposite(); ++i) {
        }
        if (i > 0) {
            System.arraycopy(directions, 0, directions, 1, i);
            directions[0] = direction.getOpposite();
        }
        return directions;
    }
}

