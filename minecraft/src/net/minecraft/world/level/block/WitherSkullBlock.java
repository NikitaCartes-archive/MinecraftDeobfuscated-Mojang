package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;

public class WitherSkullBlock extends SkullBlock {
	public static final MapCodec<WitherSkullBlock> CODEC = simpleCodec(WitherSkullBlock::new);
	@Nullable
	private static BlockPattern witherPatternFull;
	@Nullable
	private static BlockPattern witherPatternBase;

	@Override
	public MapCodec<WitherSkullBlock> codec() {
		return CODEC;
	}

	protected WitherSkullBlock(BlockBehaviour.Properties properties) {
		super(SkullBlock.Types.WITHER_SKELETON, properties);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
		checkSpawn(level, blockPos);
	}

	public static void checkSpawn(Level level, BlockPos blockPos) {
		if (level.getBlockEntity(blockPos) instanceof SkullBlockEntity skullBlockEntity) {
			checkSpawn(level, blockPos, skullBlockEntity);
		}
	}

	public static void checkSpawn(Level level, BlockPos blockPos, SkullBlockEntity skullBlockEntity) {
		if (!level.isClientSide) {
			BlockState blockState = skullBlockEntity.getBlockState();
			boolean bl = blockState.is(Blocks.WITHER_SKELETON_SKULL) || blockState.is(Blocks.WITHER_SKELETON_WALL_SKULL);
			if (bl && blockPos.getY() >= level.getMinBuildHeight() && level.getDifficulty() != Difficulty.PEACEFUL) {
				BlockPattern.BlockPatternMatch blockPatternMatch = getOrCreateWitherFull().find(level, blockPos);
				if (blockPatternMatch != null) {
					WitherBoss witherBoss = EntityType.WITHER.create(level);
					if (witherBoss != null) {
						CarvedPumpkinBlock.clearPatternBlocks(level, blockPatternMatch);
						BlockPos blockPos2 = blockPatternMatch.getBlock(1, 2, 0).getPos();
						witherBoss.moveTo(
							(double)blockPos2.getX() + 0.5,
							(double)blockPos2.getY() + 0.55,
							(double)blockPos2.getZ() + 0.5,
							blockPatternMatch.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F,
							0.0F
						);
						witherBoss.yBodyRot = blockPatternMatch.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F;
						witherBoss.makeInvulnerable();

						for (ServerPlayer serverPlayer : level.getEntitiesOfClass(ServerPlayer.class, witherBoss.getBoundingBox().inflate(50.0))) {
							CriteriaTriggers.SUMMONED_ENTITY.trigger(serverPlayer, witherBoss);
						}

						level.addFreshEntity(witherBoss);
						CarvedPumpkinBlock.updatePatternBlocks(level, blockPatternMatch);
					}
				}
			}
		}
	}

	public static boolean canSpawnMob(Level level, BlockPos blockPos, ItemStack itemStack) {
		return itemStack.is(Items.WITHER_SKELETON_SKULL)
				&& blockPos.getY() >= level.getMinBuildHeight() + 2
				&& level.getDifficulty() != Difficulty.PEACEFUL
				&& !level.isClientSide
			? getOrCreateWitherBase().find(level, blockPos) != null
			: false;
	}

	private static BlockPattern getOrCreateWitherFull() {
		if (witherPatternFull == null) {
			witherPatternFull = BlockPatternBuilder.start()
				.aisle("^^^", "###", "~#~")
				.where('#', blockInWorld -> blockInWorld.getState().is(BlockTags.WITHER_SUMMON_BASE_BLOCKS))
				.where(
					'^', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_SKULL).or(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_WALL_SKULL)))
				)
				.where('~', blockInWorld -> blockInWorld.getState().isAir())
				.build();
		}

		return witherPatternFull;
	}

	private static BlockPattern getOrCreateWitherBase() {
		if (witherPatternBase == null) {
			witherPatternBase = BlockPatternBuilder.start()
				.aisle("   ", "###", "~#~")
				.where('#', blockInWorld -> blockInWorld.getState().is(BlockTags.WITHER_SUMMON_BASE_BLOCKS))
				.where('~', blockInWorld -> blockInWorld.getState().isAir())
				.build();
		}

		return witherPatternBase;
	}
}
