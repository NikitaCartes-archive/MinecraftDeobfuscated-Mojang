package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

public class CatSitOnBlockGoal extends MoveToBlockGoal {
	private final Cat cat;

	public CatSitOnBlockGoal(Cat cat, double d) {
		super(cat, d, 8);
		this.cat = cat;
	}

	@Override
	public boolean canUse() {
		return this.cat.isTame() && !this.cat.isSitting() && super.canUse();
	}

	@Override
	public void start() {
		super.start();
		this.cat.getSitGoal().wantToSit(false);
	}

	@Override
	public void stop() {
		super.stop();
		this.cat.setSitting(false);
	}

	@Override
	public void tick() {
		super.tick();
		this.cat.getSitGoal().wantToSit(false);
		if (!this.isReachedTarget()) {
			this.cat.setSitting(false);
		} else if (!this.cat.isSitting()) {
			this.cat.setSitting(true);
		}
	}

	@Override
	protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
		if (!levelReader.isEmptyBlock(blockPos.above())) {
			return false;
		} else {
			BlockState blockState = levelReader.getBlockState(blockPos);
			Block block = blockState.getBlock();
			if (block == Blocks.CHEST) {
				return ChestBlockEntity.getOpenCount(levelReader, blockPos) < 1;
			} else {
				return block == Blocks.FURNACE && blockState.getValue(FurnaceBlock.LIT)
					? true
					: block.is(BlockTags.BEDS) && blockState.getValue(BedBlock.PART) != BedPart.HEAD;
			}
		}
	}
}
