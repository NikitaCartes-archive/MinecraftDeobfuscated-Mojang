package net.minecraft.world.item;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.dimension.end.TheEndDimension;
import net.minecraft.world.phys.AABB;

public class EndCrystalItem extends Item {
	public EndCrystalItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		BlockState blockState = level.getBlockState(blockPos);
		if (!blockState.is(Blocks.OBSIDIAN) && !blockState.is(Blocks.BEDROCK)) {
			return InteractionResult.FAIL;
		} else {
			BlockPos blockPos2 = blockPos.above();
			if (!level.isEmptyBlock(blockPos2)) {
				return InteractionResult.FAIL;
			} else {
				double d = (double)blockPos2.getX();
				double e = (double)blockPos2.getY();
				double f = (double)blockPos2.getZ();
				List<Entity> list = level.getEntities(null, new AABB(d, e, f, d + 1.0, e + 2.0, f + 1.0));
				if (!list.isEmpty()) {
					return InteractionResult.FAIL;
				} else {
					if (!level.isClientSide) {
						EndCrystal endCrystal = new EndCrystal(level, d + 0.5, e, f + 0.5);
						endCrystal.setShowBottom(false);
						level.addFreshEntity(endCrystal);
						if (level.getDimension() instanceof TheEndDimension) {
							EndDragonFight endDragonFight = ((TheEndDimension)level.getDimension()).getDragonFight();
							endDragonFight.tryRespawn();
						}
					}

					useOnContext.getItemInHand().shrink(1);
					return InteractionResult.SUCCESS;
				}
			}
		}
	}

	@Override
	public boolean isFoil(ItemStack itemStack) {
		return true;
	}
}
