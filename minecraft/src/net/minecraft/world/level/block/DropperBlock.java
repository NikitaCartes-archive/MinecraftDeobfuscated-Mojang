package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSourceImpl;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class DropperBlock extends DispenserBlock {
	private static final DispenseItemBehavior DISPENSE_BEHAVIOUR = new DefaultDispenseItemBehavior();

	public DropperBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected DispenseItemBehavior getDispenseMethod(ItemStack itemStack) {
		return DISPENSE_BEHAVIOUR;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new DropperBlockEntity(blockPos, blockState);
	}

	@Override
	protected void dispenseFrom(ServerLevel serverLevel, BlockPos blockPos) {
		BlockSourceImpl blockSourceImpl = new BlockSourceImpl(serverLevel, blockPos);
		DispenserBlockEntity dispenserBlockEntity = blockSourceImpl.getEntity();
		int i = dispenserBlockEntity.getRandomSlot(serverLevel.random);
		if (i < 0) {
			serverLevel.levelEvent(1001, blockPos, 0);
		} else {
			ItemStack itemStack = dispenserBlockEntity.getItem(i);
			if (!itemStack.isEmpty()) {
				Direction direction = serverLevel.getBlockState(blockPos).getValue(FACING);
				Container container = HopperBlockEntity.getContainerAt(serverLevel, blockPos.relative(direction));
				ItemStack itemStack2;
				if (container == null) {
					itemStack2 = DISPENSE_BEHAVIOUR.dispense(blockSourceImpl, itemStack);
				} else {
					itemStack2 = HopperBlockEntity.addItem(dispenserBlockEntity, container, itemStack.copy().split(1), direction.getOpposite());
					if (itemStack2.isEmpty()) {
						itemStack2 = itemStack.copy();
						itemStack2.shrink(1);
					} else {
						itemStack2 = itemStack.copy();
					}
				}

				dispenserBlockEntity.setItem(i, itemStack2);
			}
		}
	}
}
