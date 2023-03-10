/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;

public class MinecartItem
extends Item {
    private static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new DefaultDispenseItemBehavior(){
        private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

        @Override
        public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
            double g;
            RailShape railShape;
            Direction direction = blockSource.getBlockState().getValue(DispenserBlock.FACING);
            ServerLevel level = blockSource.getLevel();
            double d = blockSource.x() + (double)direction.getStepX() * 1.125;
            double e = Math.floor(blockSource.y()) + (double)direction.getStepY();
            double f = blockSource.z() + (double)direction.getStepZ() * 1.125;
            BlockPos blockPos = blockSource.getPos().relative(direction);
            BlockState blockState = level.getBlockState(blockPos);
            RailShape railShape2 = railShape = blockState.getBlock() instanceof BaseRailBlock ? blockState.getValue(((BaseRailBlock)blockState.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
            if (blockState.is(BlockTags.RAILS)) {
                g = railShape.isAscending() ? 0.6 : 0.1;
            } else if (blockState.isAir() && level.getBlockState(blockPos.below()).is(BlockTags.RAILS)) {
                RailShape railShape22;
                BlockState blockState2 = level.getBlockState(blockPos.below());
                RailShape railShape3 = railShape22 = blockState2.getBlock() instanceof BaseRailBlock ? blockState2.getValue(((BaseRailBlock)blockState2.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
                g = direction == Direction.DOWN || !railShape22.isAscending() ? -0.9 : -0.4;
            } else {
                return this.defaultDispenseItemBehavior.dispense(blockSource, itemStack);
            }
            AbstractMinecart abstractMinecart = AbstractMinecart.createMinecart(level, d, e + g, f, ((MinecartItem)itemStack.getItem()).type);
            if (itemStack.hasCustomHoverName()) {
                abstractMinecart.setCustomName(itemStack.getHoverName());
            }
            level.addFreshEntity(abstractMinecart);
            itemStack.shrink(1);
            return itemStack;
        }

        @Override
        protected void playSound(BlockSource blockSource) {
            blockSource.getLevel().levelEvent(1000, blockSource.getPos(), 0);
        }
    };
    final AbstractMinecart.Type type;

    public MinecartItem(AbstractMinecart.Type type, Item.Properties properties) {
        super(properties);
        this.type = type;
        DispenserBlock.registerBehavior(this, DISPENSE_ITEM_BEHAVIOR);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        BlockPos blockPos;
        Level level = useOnContext.getLevel();
        BlockState blockState = level.getBlockState(blockPos = useOnContext.getClickedPos());
        if (!blockState.is(BlockTags.RAILS)) {
            return InteractionResult.FAIL;
        }
        ItemStack itemStack = useOnContext.getItemInHand();
        if (!level.isClientSide) {
            RailShape railShape = blockState.getBlock() instanceof BaseRailBlock ? blockState.getValue(((BaseRailBlock)blockState.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
            double d = 0.0;
            if (railShape.isAscending()) {
                d = 0.5;
            }
            AbstractMinecart abstractMinecart = AbstractMinecart.createMinecart(level, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.0625 + d, (double)blockPos.getZ() + 0.5, this.type);
            if (itemStack.hasCustomHoverName()) {
                abstractMinecart.setCustomName(itemStack.getHoverName());
            }
            level.addFreshEntity(abstractMinecart);
            level.gameEvent(GameEvent.ENTITY_PLACE, blockPos, GameEvent.Context.of(useOnContext.getPlayer(), level.getBlockState(blockPos.below())));
        }
        itemStack.shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}

