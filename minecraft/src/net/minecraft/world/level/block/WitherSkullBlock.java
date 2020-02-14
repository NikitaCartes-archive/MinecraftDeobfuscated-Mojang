package net.minecraft.world.level.block;

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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockMaterialPredicate;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.material.Material;

public class WitherSkullBlock extends SkullBlock {
	@Nullable
	private static BlockPattern witherPatternFull;
	@Nullable
	private static BlockPattern witherPatternBase;

	protected WitherSkullBlock(Block.Properties properties) {
		super(SkullBlock.Types.WITHER_SKELETON, properties);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
		super.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack);
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (blockEntity instanceof SkullBlockEntity) {
			checkSpawn(level, blockPos, (SkullBlockEntity)blockEntity);
		}
	}

	public static void checkSpawn(Level level, BlockPos blockPos, SkullBlockEntity skullBlockEntity) {
		if (!level.isClientSide) {
			Block block = skullBlockEntity.getBlockState().getBlock();
			boolean bl = block == Blocks.WITHER_SKELETON_SKULL || block == Blocks.WITHER_SKELETON_WALL_SKULL;
			if (bl && blockPos.getY() >= 2 && level.getDifficulty() != Difficulty.PEACEFUL) {
				BlockPattern blockPattern = getOrCreateWitherFull();
				BlockPattern.BlockPatternMatch blockPatternMatch = blockPattern.find(level, blockPos);
				if (blockPatternMatch != null) {
					for (int i = 0; i < blockPattern.getWidth(); i++) {
						for (int j = 0; j < blockPattern.getHeight(); j++) {
							BlockInWorld blockInWorld = blockPatternMatch.getBlock(i, j, 0);
							level.setBlock(blockInWorld.getPos(), Blocks.AIR.defaultBlockState(), 2);
							level.levelEvent(2001, blockInWorld.getPos(), Block.getId(blockInWorld.getState()));
						}
					}

					WitherBoss witherBoss = EntityType.WITHER.create(level);
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

					for (int k = 0; k < blockPattern.getWidth(); k++) {
						for (int l = 0; l < blockPattern.getHeight(); l++) {
							level.blockUpdated(blockPatternMatch.getBlock(k, l, 0).getPos(), Blocks.AIR);
						}
					}
				}
			}
		}
	}

	public static boolean canSpawnMob(Level level, BlockPos blockPos, ItemStack itemStack) {
		return itemStack.getItem() == Items.WITHER_SKELETON_SKULL && blockPos.getY() >= 2 && level.getDifficulty() != Difficulty.PEACEFUL && !level.isClientSide
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
				.where('~', BlockInWorld.hasState(BlockMaterialPredicate.forMaterial(Material.AIR)))
				.build();
		}

		return witherPatternFull;
	}

	private static BlockPattern getOrCreateWitherBase() {
		if (witherPatternBase == null) {
			witherPatternBase = BlockPatternBuilder.start()
				.aisle("   ", "###", "~#~")
				.where('#', blockInWorld -> blockInWorld.getState().is(BlockTags.WITHER_SUMMON_BASE_BLOCKS))
				.where('~', BlockInWorld.hasState(BlockMaterialPredicate.forMaterial(Material.AIR)))
				.build();
		}

		return witherPatternBase;
	}
}
