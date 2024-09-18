package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class MinecartItem extends Item {
	private final EntityType<? extends AbstractMinecart> type;

	public MinecartItem(EntityType<? extends AbstractMinecart> entityType, Item.Properties properties) {
		super(properties);
		this.type = entityType;
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		BlockState blockState = level.getBlockState(blockPos);
		if (!blockState.is(BlockTags.RAILS)) {
			return InteractionResult.FAIL;
		} else {
			ItemStack itemStack = useOnContext.getItemInHand();
			RailShape railShape = blockState.getBlock() instanceof BaseRailBlock
				? blockState.getValue(((BaseRailBlock)blockState.getBlock()).getShapeProperty())
				: RailShape.NORTH_SOUTH;
			double d = 0.0;
			if (railShape.isSlope()) {
				d = 0.5;
			}

			Vec3 vec3 = new Vec3((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.0625 + d, (double)blockPos.getZ() + 0.5);
			AbstractMinecart abstractMinecart = AbstractMinecart.createMinecart(
				level, vec3.x, vec3.y, vec3.z, this.type, EntitySpawnReason.DISPENSER, itemStack, useOnContext.getPlayer()
			);
			if (abstractMinecart == null) {
				return InteractionResult.FAIL;
			} else {
				if (AbstractMinecart.useExperimentalMovement(level)) {
					for (Entity entity : level.getEntities(null, abstractMinecart.getBoundingBox())) {
						if (entity instanceof AbstractMinecart) {
							return InteractionResult.FAIL;
						}
					}
				}

				if (level instanceof ServerLevel serverLevel) {
					serverLevel.addFreshEntity(abstractMinecart);
					serverLevel.gameEvent(GameEvent.ENTITY_PLACE, blockPos, GameEvent.Context.of(useOnContext.getPlayer(), serverLevel.getBlockState(blockPos.below())));
				}

				itemStack.shrink(1);
				return InteractionResult.SUCCESS;
			}
		}
	}
}
