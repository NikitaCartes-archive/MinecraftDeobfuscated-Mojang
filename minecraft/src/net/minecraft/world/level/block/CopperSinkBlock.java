package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.tags.ItemTags;
import net.minecraft.voting.rules.Rules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class CopperSinkBlock extends AbstractCauldronBlock {
	public CopperSinkBlock(BlockBehaviour.Properties properties) {
		super(properties, CauldronInteraction.EMPTY_SINK);
	}

	@Override
	public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (Rules.COPPER_SINK.get()) {
			if (entity.getY() < (double)blockPos.getY() + 0.375
				&& entity.getBoundingBox().maxY > (double)blockPos.getY() + 0.25
				&& entity instanceof ItemEntity itemEntity
				&& itemEntity.getItem().is(ItemTags.COPPER)) {
				itemEntity.kill();
			}
		}
	}

	@Override
	public boolean isFull(BlockState blockState) {
		return false;
	}
}
