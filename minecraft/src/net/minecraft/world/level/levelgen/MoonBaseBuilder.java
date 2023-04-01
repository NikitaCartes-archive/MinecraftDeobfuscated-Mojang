package net.minecraft.world.level.levelgen;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class MoonBaseBuilder {
	private static final int MAX_DECORATION_LEVELS = 4;
	private final ServerLevel level;
	private final BlockPos pos;
	private final RandomSource random;
	private final int stepDuration;
	private final int maxBranchDepth;
	private final Map<BlockPos, MoonBaseBuilder.Branch> generatedBranches = new HashMap();
	private final List<MoonBaseBuilder.Branch> pendingBranches = new ArrayList();
	private final List<BlockPos> generatedLeaves = new ArrayList();
	private final Map<BlockPos, Integer> branchDecorations = new HashMap();
	private final Block[] COPPER_BLOCKS = new Block[]{
		Blocks.WAXED_COPPER_BLOCK, Blocks.WAXED_EXPOSED_COPPER, Blocks.WAXED_WEATHERED_COPPER, Blocks.WAXED_OXIDIZED_COPPER
	};
	private final Block[] COPPER_ENDS = new Block[]{
		Blocks.WAXED_CUT_COPPER, Blocks.WAXED_EXPOSED_CUT_COPPER, Blocks.WAXED_WEATHERED_CUT_COPPER, Blocks.WAXED_OXIDIZED_CUT_COPPER
	};

	public MoonBaseBuilder(ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource, int i, int j) {
		this.level = serverLevel;
		this.pos = blockPos;
		this.random = randomSource;
		this.stepDuration = i;
		this.maxBranchDepth = j;
	}

	public void spawn() {
		if (this.stepDuration == 0) {
			this.spawnInstantly();
		} else {
			this.spawnOverTime();
		}
	}

	private void spawnInstantly() {
		this.pendingBranches.addAll(this.spawnTree(this.level, this.pos, this.random, 0, 0, 0));

		while (!this.pendingBranches.isEmpty()) {
			this.runBuildUpStep();
		}

		int i = this.computeBranchDecorations();

		for (int j = 4; j >= i; j--) {
			Iterator<Entry<BlockPos, Integer>> iterator = this.branchDecorations.entrySet().iterator();

			while (iterator.hasNext()) {
				Entry<BlockPos, Integer> entry = (Entry<BlockPos, Integer>)iterator.next();
				if ((Integer)entry.getValue() == j) {
					this.decorateBranch(this.level, (MoonBaseBuilder.Branch)this.generatedBranches.get(entry.getKey()), this.random, j, false);
					iterator.remove();
				}
			}
		}
	}

	private void spawnOverTime() {
		this.pendingBranches.addAll(this.spawnTree(this.level, this.pos, this.random, 0, 0, this.stepDuration));
		this.level.getServer().executeLater(this.stepDuration, this::spawnStep);
	}

	private void spawnStep() {
		if (this.pendingBranches.isEmpty()) {
			this.scheduleDecorations();
		} else {
			this.runBuildUpStep();
			this.level.getServer().executeLater(this.stepDuration, this::spawnStep);
		}
	}

	private void scheduleDecorations() {
		this.computeBranchDecorations();
		this.branchDecorations
			.forEach(
				(blockPos, integer) -> {
					int i = this.stepDuration * (4 - integer) + this.random.nextInt(this.stepDuration);
					this.level
						.getServer()
						.executeLater(i, () -> this.decorateBranch(this.level, (MoonBaseBuilder.Branch)this.generatedBranches.get(blockPos), this.random, integer, true));
				}
			);
	}

	private int computeBranchDecorations() {
		int i = 4;

		for (BlockPos blockPos : this.generatedLeaves) {
			MoonBaseBuilder.Branch branch = (MoonBaseBuilder.Branch)this.generatedBranches.get(blockPos);
			this.branchDecorations.put(branch.pos, 4);
			int j = 4;

			while (this.generatedBranches.containsKey(branch.start)) {
				branch = (MoonBaseBuilder.Branch)this.generatedBranches.get(branch.start);
				i = Math.min(i, --j);
				this.branchDecorations.put(branch.pos, Math.min((Integer)this.branchDecorations.getOrDefault(branch.pos, j), j));
			}
		}

		return i;
	}

	private void runBuildUpStep() {
		List<MoonBaseBuilder.Branch> list = new ArrayList();

		for (MoonBaseBuilder.Branch branch : this.pendingBranches) {
			List<MoonBaseBuilder.Branch> list2 = this.spawnTree(this.level, branch.pos, this.random, branch.depth, branch.rustLevel, this.stepDuration);
			list.addAll(list2);
			if (list2.isEmpty()) {
				this.generatedLeaves.add(branch.pos);
			}

			this.generatedBranches.put(branch.pos, branch);
		}

		this.pendingBranches.clear();
		this.pendingBranches.addAll(list);
	}

	private void decorateBranch(WorldGenLevel worldGenLevel, MoonBaseBuilder.Branch branch, RandomSource randomSource, int i, boolean bl) {
		if (i < 0) {
			this.decorateTrunk(worldGenLevel, branch, randomSource);
			if (bl) {
				worldGenLevel.getLevel().playSound(null, branch.pos, SoundEvents.NETHER_WOOD_PLACE, SoundSource.BLOCKS, 0.6F, 0.6F + 0.2F * randomSource.nextFloat());
			}
		} else {
			if (bl) {
				worldGenLevel.getLevel().playSound(null, branch.pos, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 0.6F, 0.5F + 0.2F * randomSource.nextFloat());
			}

			Direction.Axis axis = branch.direction.getAxis();

			for (int j = 0; j < i; j++) {
				int k = randomSource.nextInt(branch.length);
				Direction direction = Util.getRandom(axis.getOrthogonalDirections(), randomSource);
				BlockPos.MutableBlockPos mutableBlockPos = branch.start.relative(branch.direction, k).relative(direction).mutable();

				for (int l = 0; l < i && worldGenLevel.isEmptyBlock(mutableBlockPos); l++) {
					BlockState blockState = Blocks.COPPER_SPLEAVES.defaultBlockState();
					this.setBlock(worldGenLevel, mutableBlockPos, blockState);
					int m = randomSource.nextInt(6);

					mutableBlockPos.move(switch (m) {
						case 0, 1, 2 -> branch.direction;
						case 3 -> direction;
						case 4 -> direction.getClockWise(axis);
						case 5 -> direction.getCounterClockWise(axis);
						default -> throw new IllegalStateException("Unexpected value: " + m);
					});
				}
			}

			if (i == 4) {
				BlockPos blockPos = branch.pos.relative(branch.direction);
				boolean bl2 = worldGenLevel.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockPos).getY() == blockPos.getY();

				BlockState blockState2 = switch (branch.direction) {
					case UP -> bl2 ? Blocks.CHEST.defaultBlockState() : Blocks.GREEN_SHULKER_BOX.defaultBlockState();
					case DOWN -> {
						switch (randomSource.nextInt(10)) {
							case 0:
								yield Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, Direction.DOWN);
							case 1:
							case 2:
								yield Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, Boolean.valueOf(true));
							default:
								yield Blocks.CHAIN.defaultBlockState().setValue(ChainBlock.AXIS, Direction.Axis.Y);
						}
					}
					default -> Blocks.COPPER_SPLEAVES.defaultBlockState();
				};
				this.setBlock(worldGenLevel, blockPos, blockState2);
				if (worldGenLevel.getBlockEntity(blockPos) instanceof RandomizableContainerBlockEntity randomizableContainerBlockEntity) {
					if (bl2) {
						RandomizableContainerBlockEntity.setLootTable(worldGenLevel, randomSource, blockPos, BuiltInLootTables.MOON_RESUPLY);
						randomizableContainerBlockEntity.setCustomName(Component.translatable("block.minecraft.chest.moon"));
					} else {
						RandomizableContainerBlockEntity.setLootTable(worldGenLevel, randomSource, blockPos, BuiltInLootTables.MOON_LAB);
						randomizableContainerBlockEntity.setCustomName(Component.translatable("block.minecraft.chest.lab"));
					}
				}
			}
		}
	}

	private void decorateTrunk(WorldGenLevel worldGenLevel, MoonBaseBuilder.Branch branch, RandomSource randomSource) {
		Direction.Axis axis = branch.direction.getAxis();

		for (int i = 0; i < branch.length; i++) {
			for (int j = randomSource.nextInt(3); j < 2; j++) {
				Direction direction = Util.getRandom(axis.getOrthogonalDirections(), randomSource);
				BlockPos blockPos = branch.start.relative(branch.direction, i).relative(direction);
				if (worldGenLevel.isEmptyBlock(blockPos)) {
					this.setBlock(
						worldGenLevel,
						blockPos,
						ButtonBlock.getValidAttachedState(Blocks.POLISHED_BLACKSTONE_BUTTON.defaultBlockState(), direction.getOpposite(), branch.direction)
					);
				}
			}
		}
	}

	private List<MoonBaseBuilder.Branch> spawnTree(WorldGenLevel worldGenLevel, BlockPos blockPos, RandomSource randomSource, int i, int j, int k) {
		if (i > this.maxBranchDepth) {
			return List.of();
		} else if (j > 100) {
			return List.of();
		} else {
			Map<Direction, Integer> map = new EnumMap(Direction.class);
			List<Direction> list = new ArrayList(this.getPossibleDirections(i, randomSource).filter(directionx -> {
				int ix = randomSource.nextInt(directionx == Direction.UP ? 3 : 4) + 3;
				map.put(directionx, ix);

				for (int jxx = 2; jxx < ix + 1; jxx++) {
					for (int kx = -1; kx < 2; kx++) {
						for (int lx = -1; lx < 2; lx++) {
							for (int mx = -1; mx < 2; mx++) {
								if (!worldGenLevel.getBlockState(blockPos.relative(directionx, jxx).offset(kx, lx, mx)).isAir()) {
									return false;
								}
							}
						}
					}
				}

				for (int jx = 3; jx < ix + 1; jx++) {
					for (Direction direction2 : Direction.values()) {
						if (!worldGenLevel.getBlockState(blockPos.relative(directionx, jx).relative(direction2, 2)).isAir()) {
							return false;
						}
					}
				}

				return true;
			}).toList());
			Util.shuffle(list, randomSource);

			for (Direction direction : list) {
				int l = (Integer)map.get(direction);

				for (int m = 1; m <= l; m++) {
					boolean bl = m == l || randomSource.nextInt() == 1;
					BlockState blockState = this.getBlockBasedOnRustLevel(j + m, randomSource, bl, direction);
					if (k > 0) {
						worldGenLevel.addFreshEntity(
							new Display.LinearlyInterpolatedBlockAnimator(
								worldGenLevel.getLevel(), blockState, blockPos.relative(direction, m), new Display.KeyFrame(blockPos, 0.99F / (float)m, k)
							)
						);
					} else {
						this.setBlock(worldGenLevel, blockPos.relative(direction, m), blockState);
					}
				}
			}

			if (list.isEmpty()) {
				return List.of();
			} else {
				if (k > 0) {
					worldGenLevel.getLevel().playSound(null, blockPos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.3F, 0.3F + 0.3F * randomSource.nextFloat());
					worldGenLevel.getLevel().playSound(null, blockPos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.8F, 1.5F + 0.5F * randomSource.nextFloat());
				}

				return (List<MoonBaseBuilder.Branch>)list.stream()
					.map(
						directionx -> new MoonBaseBuilder.Branch(
								blockPos,
								blockPos.relative(directionx, (Integer)map.get(directionx)),
								(Integer)map.get(directionx),
								i + 1,
								j + (Integer)map.get(directionx),
								directionx
							)
					)
					.collect(Collectors.toList());
			}
		}
	}

	private void setBlock(WorldGenLevel worldGenLevel, BlockPos blockPos, BlockState blockState) {
		if (worldGenLevel.getBlockState(blockPos).isAir()) {
			worldGenLevel.setBlock(blockPos, blockState, 3);
		}
	}

	private BlockState getBlockBasedOnRustLevel(int i, RandomSource randomSource, boolean bl, Direction direction) {
		int j = i + randomSource.nextInt(20) - randomSource.nextInt(20);
		Block[] blocks = this.COPPER_ENDS;
		Block[] blocks2 = this.COPPER_BLOCKS;
		BlockState blockState;
		if (j < 40) {
			blockState = (bl ? blocks2[0] : blocks[0]).defaultBlockState();
		} else if (j < 60) {
			blockState = (bl ? blocks2[1] : blocks[1]).defaultBlockState();
		} else if (j < 80) {
			blockState = (bl ? blocks2[2] : blocks[2]).defaultBlockState();
		} else {
			blockState = (bl ? blocks2[3] : blocks[3]).defaultBlockState();
		}

		if (blockState.hasProperty(RotatedPillarBlock.AXIS)) {
			blockState = blockState.setValue(RotatedPillarBlock.AXIS, direction.getAxis());
		}

		return blockState;
	}

	private Stream<Direction> getPossibleDirections(int i, RandomSource randomSource) {
		List<Direction> list = new ArrayList();
		float f = i < 4 ? 1.0F : 2.0F / (float)(i + 1);
		float g = (float)Math.sin((double)i * Math.PI / 20.0);
		float h = (float)(i - 10) / 5.0F;
		if (randomSource.nextFloat() < f) {
			list.add(Direction.UP);
		}

		if (randomSource.nextFloat() < g) {
			list.add(Direction.NORTH);
		}

		if (randomSource.nextFloat() < g) {
			list.add(Direction.SOUTH);
		}

		if (randomSource.nextFloat() < g) {
			list.add(Direction.EAST);
		}

		if (randomSource.nextFloat() < g) {
			list.add(Direction.WEST);
		}

		if (randomSource.nextFloat() < h) {
			list.add(Direction.DOWN);
		}

		return list.stream();
	}

	public static Display.KeyFrame[] getFramesForLander(BlockPos blockPos) {
		return new Display.KeyFrame[]{
			new Display.KeyFrame(blockPos.above(50), 2.2F, 50), new Display.KeyFrame(blockPos.above(3), 1.0F, 45), new Display.KeyFrame(blockPos.above(4), 1.5F, 5)
		};
	}

	public static void expandContraption(ServerLevel serverLevel, BlockPos blockPos) {
		serverLevel.explode(null, (double)blockPos.getX(), (double)blockPos.getY() + 50.0, (double)blockPos.getZ(), 15.0F, Level.ExplosionInteraction.NONE);
		Direction[] directions = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

		for (Direction direction : directions) {
			serverLevel.addFreshEntity(
				new Display.LinearlyInterpolatedBlockAnimator(
					serverLevel, Blocks.RAW_COPPER_BLOCK.defaultBlockState(), blockPos.relative(direction), getFramesForLander(blockPos.relative(direction))
				)
			);
		}

		serverLevel.addFreshEntity(
			new Display.LinearlyInterpolatedBlockAnimator(serverLevel, Blocks.RAW_COPPER_BLOCK.defaultBlockState(), blockPos, getFramesForLander(blockPos))
		);

		for (Direction direction : directions) {
			serverLevel.addFreshEntity(
				new Display.LinearlyInterpolatedBlockAnimator(
					serverLevel,
					Blocks.WAXED_CUT_COPPER_STAIRS.defaultBlockState().setValue(StairBlock.FACING, direction.getOpposite()),
					blockPos.above().relative(direction),
					getFramesForLander(blockPos.above().relative(direction))
				)
			);
		}

		serverLevel.addFreshEntity(
			new Display.LinearlyInterpolatedBlockAnimator(
				serverLevel, Blocks.RAW_COPPER_BLOCK.defaultBlockState(), blockPos.above(), getFramesForLander(blockPos.above())
			)
		);
		serverLevel.addFreshEntity(
			new Display.LinearlyInterpolatedBlockAnimator(
				serverLevel, Blocks.WAXED_COPPER_BLOCK.defaultBlockState(), blockPos.above().above(), getFramesForLander(blockPos.above().above())
			)
		);
		serverLevel.getServer()
			.executeLater(
				50,
				() -> {
					for (int i = 0; i < 40; i++) {
						serverLevel.getServer()
							.executeLater(
								i,
								() -> {
									serverLevel.playSound(
										null,
										(double)blockPos.getX(),
										(double)blockPos.getY() + 3.0,
										(double)blockPos.getZ(),
										SoundEvents.AMBIENT_BASALT_DELTAS_MOOD.value(),
										SoundSource.BLOCKS,
										0.2F,
										1.0F
									);
									serverLevel.sendParticles(
										ParticleTypes.SMOKE, (double)blockPos.getX(), (double)blockPos.getY() + 3.0, (double)blockPos.getZ(), 100, 1.0, 0.5, 1.0, 0.3
									);
								}
							);
					}
				}
			);
		serverLevel.getServer()
			.executeLater(
				95,
				() -> {
					for (int i = 0; i < 5; i++) {
						int j = i;
						serverLevel.getServer()
							.executeLater(
								i,
								() -> {
									serverLevel.playSound(
										null,
										(double)blockPos.getX(),
										(double)blockPos.getY() + 3.0 + (double)j,
										(double)blockPos.getZ(),
										SoundEvents.FIRECHARGE_USE,
										SoundSource.BLOCKS,
										0.4F,
										1.4F
									);
									serverLevel.sendParticles(
										ParticleTypes.FLAME, (double)blockPos.getX(), (double)blockPos.getY() + 3.0 + (double)j, (double)blockPos.getZ(), 100, 0.7, 0.5, 0.7, 0.0
									);
								}
							);
					}
				}
			);
		serverLevel.getServer()
			.executeLater(
				100,
				() -> {
					serverLevel.playSound(
						null, (double)blockPos.getX(), (double)blockPos.getY() + 3.0, (double)blockPos.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0F, 1.4F
					);

					for (Direction directionx : directions) {
						serverLevel.addFreshEntity(
							new Display.LinearlyInterpolatedBlockAnimator(
								serverLevel,
								Blocks.LIGHTNING_ROD.defaultBlockState().setValue(LightningRodBlock.FACING, directionx.getOpposite()),
								blockPos.above().above().relative(directionx),
								new Display.KeyFrame(blockPos.above(2), 1.0F, 100)
							)
						);
					}

					BlockState blockState = Blocks.POLISHED_BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
					BlockState blockState2 = Blocks.CHAIN.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
					serverLevel.setBlock(blockPos.north().east(), blockState, 3);
					serverLevel.setBlock(blockPos.north().east().above(), blockState2, 3);
					serverLevel.setBlock(blockPos.north().west(), blockState, 3);
					serverLevel.setBlock(blockPos.south().east(), blockState, 3);
					serverLevel.setBlock(blockPos.south().west().above(), blockState2, 3);
					serverLevel.setBlock(blockPos.south().west(), blockState, 3);
				}
			);
		serverLevel.getServer().executeLater(200, () -> {
			WorldgenRandom worldgenRandom = new WorldgenRandom(serverLevel.random);
			worldgenRandom.setLargeFeatureSeed(serverLevel.getSeed(), blockPos.getX() >> 4, blockPos.getZ() >> 4);
			MoonBaseBuilder moonBaseBuilder = new MoonBaseBuilder(serverLevel, blockPos.above().above(), worldgenRandom, 20, 20);
			moonBaseBuilder.spawn();
		});
	}

	static record Branch(BlockPos start, BlockPos pos, int length, int depth, int rustLevel, Direction direction) {
	}
}
