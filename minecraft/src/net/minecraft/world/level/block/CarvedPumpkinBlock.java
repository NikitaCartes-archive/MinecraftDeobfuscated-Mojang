package net.minecraft.world.level.block;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockMaterialPredicate;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;

public class CarvedPumpkinBlock extends HorizontalDirectionalBlock implements Equipable {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	@Nullable
	private BlockPattern snowGolemBase;
	@Nullable
	private BlockPattern snowGolemFull;
	@Nullable
	private BlockPattern ironGolemBase;
	@Nullable
	private BlockPattern ironGolemFull;
	private static final Predicate<BlockState> PUMPKINS_PREDICATE = blockState -> blockState != null
			&& (blockState.is(Blocks.CARVED_PUMPKIN) || blockState.is(Blocks.JACK_O_LANTERN));

	protected CarvedPumpkinBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState2.is(blockState.getBlock())) {
			this.trySpawnGolem(level, blockPos);
		}
	}

	public boolean canSpawnGolem(LevelReader levelReader, BlockPos blockPos) {
		return this.getOrCreateSnowGolemBase().find(levelReader, blockPos) != null || this.getOrCreateIronGolemBase().find(levelReader, blockPos) != null;
	}

	private void trySpawnGolem(Level level, BlockPos blockPos) {
		BlockPattern.BlockPatternMatch blockPatternMatch = this.getOrCreateSnowGolemFull().find(level, blockPos);
		if (blockPatternMatch != null) {
			SnowGolem snowGolem = EntityType.SNOW_GOLEM.create(level);
			if (snowGolem != null) {
				spawnGolemInWorld(level, blockPatternMatch, snowGolem, blockPatternMatch.getBlock(0, 2, 0).getPos());
			}
		} else {
			BlockPattern.BlockPatternMatch blockPatternMatch2 = this.getOrCreateIronGolemFull().find(level, blockPos);
			if (blockPatternMatch2 != null) {
				IronGolem ironGolem = EntityType.IRON_GOLEM.create(level);
				if (ironGolem != null) {
					ironGolem.setPlayerCreated(true);
					spawnGolemInWorld(level, blockPatternMatch2, ironGolem, blockPatternMatch2.getBlock(1, 2, 0).getPos());
				}
			}
		}
	}

	private static void spawnGolemInWorld(Level level, BlockPattern.BlockPatternMatch blockPatternMatch, Entity entity, BlockPos blockPos) {
		clearPatternBlocks(level, blockPatternMatch);
		entity.moveTo((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.05, (double)blockPos.getZ() + 0.5, 0.0F, 0.0F);
		level.addFreshEntity(entity);

		for (ServerPlayer serverPlayer : level.getEntitiesOfClass(ServerPlayer.class, entity.getBoundingBox().inflate(5.0))) {
			CriteriaTriggers.SUMMONED_ENTITY.trigger(serverPlayer, entity);
		}

		updatePatternBlocks(level, blockPatternMatch);
	}

	public static void clearPatternBlocks(Level level, BlockPattern.BlockPatternMatch blockPatternMatch) {
		for (int i = 0; i < blockPatternMatch.getWidth(); i++) {
			for (int j = 0; j < blockPatternMatch.getHeight(); j++) {
				BlockInWorld blockInWorld = blockPatternMatch.getBlock(i, j, 0);
				level.setBlock(blockInWorld.getPos(), Blocks.AIR.defaultBlockState(), 2);
				level.levelEvent(2001, blockInWorld.getPos(), Block.getId(blockInWorld.getState()));
			}
		}
	}

	public static void updatePatternBlocks(Level level, BlockPattern.BlockPatternMatch blockPatternMatch) {
		for (int i = 0; i < blockPatternMatch.getWidth(); i++) {
			for (int j = 0; j < blockPatternMatch.getHeight(); j++) {
				BlockInWorld blockInWorld = blockPatternMatch.getBlock(i, j, 0);
				level.blockUpdated(blockInWorld.getPos(), Blocks.AIR);
			}
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	private BlockPattern getOrCreateSnowGolemBase() {
		if (this.snowGolemBase == null) {
			this.snowGolemBase = BlockPatternBuilder.start()
				.aisle(" ", "#", "#")
				.where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.SNOW_BLOCK)))
				.build();
		}

		return this.snowGolemBase;
	}

	private BlockPattern getOrCreateSnowGolemFull() {
		if (this.snowGolemFull == null) {
			this.snowGolemFull = BlockPatternBuilder.start()
				.aisle("^", "#", "#")
				.where('^', BlockInWorld.hasState(PUMPKINS_PREDICATE))
				.where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.SNOW_BLOCK)))
				.build();
		}

		return this.snowGolemFull;
	}

	private BlockPattern getOrCreateIronGolemBase() {
		if (this.ironGolemBase == null) {
			this.ironGolemBase = BlockPatternBuilder.start()
				.aisle("~ ~", "###", "~#~")
				.where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.IRON_BLOCK)))
				.where('~', BlockInWorld.hasState(BlockMaterialPredicate.forMaterial(Material.AIR)))
				.build();
		}

		return this.ironGolemBase;
	}

	private BlockPattern getOrCreateIronGolemFull() {
		if (this.ironGolemFull == null) {
			this.ironGolemFull = BlockPatternBuilder.start()
				.aisle("~^~", "###", "~#~")
				.where('^', BlockInWorld.hasState(PUMPKINS_PREDICATE))
				.where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.IRON_BLOCK)))
				.where('~', BlockInWorld.hasState(BlockMaterialPredicate.forMaterial(Material.AIR)))
				.build();
		}

		return this.ironGolemFull;
	}

	@Override
	public EquipmentSlot getEquipmentSlot() {
		return EquipmentSlot.HEAD;
	}
}
