package net.minecraft.world.level.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction8;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.StemGrownBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.ticks.SavedTick;
import org.slf4j.Logger;

public class UpgradeData {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final UpgradeData EMPTY = new UpgradeData(EmptyBlockGetter.INSTANCE);
	private static final String TAG_INDICES = "Indices";
	private static final Direction8[] DIRECTIONS = Direction8.values();
	private final EnumSet<Direction8> sides = EnumSet.noneOf(Direction8.class);
	private final List<SavedTick<Block>> neighborBlockTicks = Lists.<SavedTick<Block>>newArrayList();
	private final List<SavedTick<Fluid>> neighborFluidTicks = Lists.<SavedTick<Fluid>>newArrayList();
	private final int[][] index;
	static final Map<Block, UpgradeData.BlockFixer> MAP = new IdentityHashMap();
	static final Set<UpgradeData.BlockFixer> CHUNKY_FIXERS = Sets.<UpgradeData.BlockFixer>newHashSet();

	private UpgradeData(LevelHeightAccessor levelHeightAccessor) {
		this.index = new int[levelHeightAccessor.getSectionsCount()][];
	}

	public UpgradeData(CompoundTag compoundTag, LevelHeightAccessor levelHeightAccessor) {
		this(levelHeightAccessor);
		if (compoundTag.contains("Indices", 10)) {
			CompoundTag compoundTag2 = compoundTag.getCompound("Indices");

			for (int i = 0; i < this.index.length; i++) {
				String string = String.valueOf(i);
				if (compoundTag2.contains(string, 11)) {
					this.index[i] = compoundTag2.getIntArray(string);
				}
			}
		}

		int j = compoundTag.getInt("Sides");

		for (Direction8 direction8 : Direction8.values()) {
			if ((j & 1 << direction8.ordinal()) != 0) {
				this.sides.add(direction8);
			}
		}

		loadTicks(
			compoundTag,
			"neighbor_block_ticks",
			stringx -> Registry.BLOCK.getOptional(ResourceLocation.tryParse(stringx)).or(() -> Optional.of(Blocks.AIR)),
			this.neighborBlockTicks
		);
		loadTicks(
			compoundTag,
			"neighbor_fluid_ticks",
			stringx -> Registry.FLUID.getOptional(ResourceLocation.tryParse(stringx)).or(() -> Optional.of(Fluids.EMPTY)),
			this.neighborFluidTicks
		);
	}

	private static <T> void loadTicks(CompoundTag compoundTag, String string, Function<String, Optional<T>> function, List<SavedTick<T>> list) {
		if (compoundTag.contains(string, 9)) {
			for (Tag tag : compoundTag.getList(string, 10)) {
				SavedTick.loadTick((CompoundTag)tag, function).ifPresent(list::add);
			}
		}
	}

	public void upgrade(LevelChunk levelChunk) {
		this.upgradeInside(levelChunk);

		for (Direction8 direction8 : DIRECTIONS) {
			upgradeSides(levelChunk, direction8);
		}

		Level level = levelChunk.getLevel();
		this.neighborBlockTicks.forEach(savedTick -> {
			Block block = savedTick.type() == Blocks.AIR ? level.getBlockState(savedTick.pos()).getBlock() : (Block)savedTick.type();
			level.scheduleTick(savedTick.pos(), block, savedTick.delay(), savedTick.priority());
		});
		this.neighborFluidTicks.forEach(savedTick -> {
			Fluid fluid = savedTick.type() == Fluids.EMPTY ? level.getFluidState(savedTick.pos()).getType() : (Fluid)savedTick.type();
			level.scheduleTick(savedTick.pos(), fluid, savedTick.delay(), savedTick.priority());
		});
		CHUNKY_FIXERS.forEach(blockFixer -> blockFixer.processChunk(level));
	}

	private static void upgradeSides(LevelChunk levelChunk, Direction8 direction8) {
		Level level = levelChunk.getLevel();
		if (levelChunk.getUpgradeData().sides.remove(direction8)) {
			Set<Direction> set = direction8.getDirections();
			int i = 0;
			int j = 15;
			boolean bl = set.contains(Direction.EAST);
			boolean bl2 = set.contains(Direction.WEST);
			boolean bl3 = set.contains(Direction.SOUTH);
			boolean bl4 = set.contains(Direction.NORTH);
			boolean bl5 = set.size() == 1;
			ChunkPos chunkPos = levelChunk.getPos();
			int k = chunkPos.getMinBlockX() + (!bl5 || !bl4 && !bl3 ? (bl2 ? 0 : 15) : 1);
			int l = chunkPos.getMinBlockX() + (!bl5 || !bl4 && !bl3 ? (bl2 ? 0 : 15) : 14);
			int m = chunkPos.getMinBlockZ() + (!bl5 || !bl && !bl2 ? (bl4 ? 0 : 15) : 1);
			int n = chunkPos.getMinBlockZ() + (!bl5 || !bl && !bl2 ? (bl4 ? 0 : 15) : 14);
			Direction[] directions = Direction.values();
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (BlockPos blockPos : BlockPos.betweenClosed(k, level.getMinBuildHeight(), m, l, level.getMaxBuildHeight() - 1, n)) {
				BlockState blockState = level.getBlockState(blockPos);
				BlockState blockState2 = blockState;

				for (Direction direction : directions) {
					mutableBlockPos.setWithOffset(blockPos, direction);
					blockState2 = updateState(blockState2, direction, level, blockPos, mutableBlockPos);
				}

				Block.updateOrDestroy(blockState, blockState2, level, blockPos, 18);
			}
		}
	}

	private static BlockState updateState(BlockState blockState, Direction direction, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
		return ((UpgradeData.BlockFixer)MAP.getOrDefault(blockState.getBlock(), UpgradeData.BlockFixers.DEFAULT))
			.updateShape(blockState, direction, levelAccessor.getBlockState(blockPos2), levelAccessor, blockPos, blockPos2);
	}

	private void upgradeInside(LevelChunk levelChunk) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
		ChunkPos chunkPos = levelChunk.getPos();
		LevelAccessor levelAccessor = levelChunk.getLevel();

		for (int i = 0; i < this.index.length; i++) {
			LevelChunkSection levelChunkSection = levelChunk.getSection(i);
			int[] is = this.index[i];
			this.index[i] = null;
			if (is != null && is.length > 0) {
				Direction[] directions = Direction.values();
				PalettedContainer<BlockState> palettedContainer = levelChunkSection.getStates();

				for (int j : is) {
					int k = j & 15;
					int l = j >> 8 & 15;
					int m = j >> 4 & 15;
					mutableBlockPos.set(chunkPos.getMinBlockX() + k, levelChunkSection.bottomBlockY() + l, chunkPos.getMinBlockZ() + m);
					BlockState blockState = palettedContainer.get(j);
					BlockState blockState2 = blockState;

					for (Direction direction : directions) {
						mutableBlockPos2.setWithOffset(mutableBlockPos, direction);
						if (SectionPos.blockToSectionCoord(mutableBlockPos.getX()) == chunkPos.x && SectionPos.blockToSectionCoord(mutableBlockPos.getZ()) == chunkPos.z) {
							blockState2 = updateState(blockState2, direction, levelAccessor, mutableBlockPos, mutableBlockPos2);
						}
					}

					Block.updateOrDestroy(blockState, blockState2, levelAccessor, mutableBlockPos, 18);
				}
			}
		}

		for (int ix = 0; ix < this.index.length; ix++) {
			if (this.index[ix] != null) {
				LOGGER.warn("Discarding update data for section {} for chunk ({} {})", levelAccessor.getSectionYFromSectionIndex(ix), chunkPos.x, chunkPos.z);
			}

			this.index[ix] = null;
		}
	}

	public boolean isEmpty() {
		for (int[] is : this.index) {
			if (is != null) {
				return false;
			}
		}

		return this.sides.isEmpty();
	}

	public CompoundTag write() {
		CompoundTag compoundTag = new CompoundTag();
		CompoundTag compoundTag2 = new CompoundTag();

		for (int i = 0; i < this.index.length; i++) {
			String string = String.valueOf(i);
			if (this.index[i] != null && this.index[i].length != 0) {
				compoundTag2.putIntArray(string, this.index[i]);
			}
		}

		if (!compoundTag2.isEmpty()) {
			compoundTag.put("Indices", compoundTag2);
		}

		int ix = 0;

		for (Direction8 direction8 : this.sides) {
			ix |= 1 << direction8.ordinal();
		}

		compoundTag.putByte("Sides", (byte)ix);
		if (!this.neighborBlockTicks.isEmpty()) {
			ListTag listTag = new ListTag();
			this.neighborBlockTicks.forEach(savedTick -> listTag.add(savedTick.save(block -> Registry.BLOCK.getKey(block).toString())));
			compoundTag.put("neighbor_block_ticks", listTag);
		}

		if (!this.neighborFluidTicks.isEmpty()) {
			ListTag listTag = new ListTag();
			this.neighborFluidTicks.forEach(savedTick -> listTag.add(savedTick.save(fluid -> Registry.FLUID.getKey(fluid).toString())));
			compoundTag.put("neighbor_fluid_ticks", listTag);
		}

		return compoundTag;
	}

	public interface BlockFixer {
		BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2);

		default void processChunk(LevelAccessor levelAccessor) {
		}
	}

	static enum BlockFixers implements UpgradeData.BlockFixer {
		BLACKLIST(
			Blocks.OBSERVER,
			Blocks.NETHER_PORTAL,
			Blocks.WHITE_CONCRETE_POWDER,
			Blocks.ORANGE_CONCRETE_POWDER,
			Blocks.MAGENTA_CONCRETE_POWDER,
			Blocks.LIGHT_BLUE_CONCRETE_POWDER,
			Blocks.YELLOW_CONCRETE_POWDER,
			Blocks.LIME_CONCRETE_POWDER,
			Blocks.PINK_CONCRETE_POWDER,
			Blocks.GRAY_CONCRETE_POWDER,
			Blocks.LIGHT_GRAY_CONCRETE_POWDER,
			Blocks.CYAN_CONCRETE_POWDER,
			Blocks.PURPLE_CONCRETE_POWDER,
			Blocks.BLUE_CONCRETE_POWDER,
			Blocks.BROWN_CONCRETE_POWDER,
			Blocks.GREEN_CONCRETE_POWDER,
			Blocks.RED_CONCRETE_POWDER,
			Blocks.BLACK_CONCRETE_POWDER,
			Blocks.ANVIL,
			Blocks.CHIPPED_ANVIL,
			Blocks.DAMAGED_ANVIL,
			Blocks.DRAGON_EGG,
			Blocks.GRAVEL,
			Blocks.SAND,
			Blocks.RED_SAND,
			Blocks.OAK_SIGN,
			Blocks.SPRUCE_SIGN,
			Blocks.BIRCH_SIGN,
			Blocks.ACACIA_SIGN,
			Blocks.JUNGLE_SIGN,
			Blocks.DARK_OAK_SIGN,
			Blocks.OAK_WALL_SIGN,
			Blocks.SPRUCE_WALL_SIGN,
			Blocks.BIRCH_WALL_SIGN,
			Blocks.ACACIA_WALL_SIGN,
			Blocks.JUNGLE_WALL_SIGN,
			Blocks.DARK_OAK_WALL_SIGN
		) {
			@Override
			public BlockState updateShape(
				BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
			) {
				return blockState;
			}
		},
		DEFAULT {
			@Override
			public BlockState updateShape(
				BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
			) {
				return blockState.updateShape(direction, levelAccessor.getBlockState(blockPos2), levelAccessor, blockPos, blockPos2);
			}
		},
		CHEST(Blocks.CHEST, Blocks.TRAPPED_CHEST) {
			@Override
			public BlockState updateShape(
				BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
			) {
				if (blockState2.is(blockState.getBlock())
					&& direction.getAxis().isHorizontal()
					&& blockState.getValue(ChestBlock.TYPE) == ChestType.SINGLE
					&& blockState2.getValue(ChestBlock.TYPE) == ChestType.SINGLE) {
					Direction direction2 = blockState.getValue(ChestBlock.FACING);
					if (direction.getAxis() != direction2.getAxis() && direction2 == blockState2.getValue(ChestBlock.FACING)) {
						ChestType chestType = direction == direction2.getClockWise() ? ChestType.LEFT : ChestType.RIGHT;
						levelAccessor.setBlock(blockPos2, blockState2.setValue(ChestBlock.TYPE, chestType.getOpposite()), 18);
						if (direction2 == Direction.NORTH || direction2 == Direction.EAST) {
							BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos);
							BlockEntity blockEntity2 = levelAccessor.getBlockEntity(blockPos2);
							if (blockEntity instanceof ChestBlockEntity && blockEntity2 instanceof ChestBlockEntity) {
								ChestBlockEntity.swapContents((ChestBlockEntity)blockEntity, (ChestBlockEntity)blockEntity2);
							}
						}

						return blockState.setValue(ChestBlock.TYPE, chestType);
					}
				}

				return blockState;
			}
		},
		LEAVES(true, Blocks.ACACIA_LEAVES, Blocks.BIRCH_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES) {
			private final ThreadLocal<List<ObjectSet<BlockPos>>> queue = ThreadLocal.withInitial(() -> Lists.newArrayListWithCapacity(7));

			@Override
			public BlockState updateShape(
				BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
			) {
				BlockState blockState3 = blockState.updateShape(direction, levelAccessor.getBlockState(blockPos2), levelAccessor, blockPos, blockPos2);
				if (blockState != blockState3) {
					int i = (Integer)blockState3.getValue(BlockStateProperties.DISTANCE);
					List<ObjectSet<BlockPos>> list = (List<ObjectSet<BlockPos>>)this.queue.get();
					if (list.isEmpty()) {
						for (int j = 0; j < 7; j++) {
							list.add(new ObjectOpenHashSet());
						}
					}

					((ObjectSet)list.get(i)).add(blockPos.immutable());
				}

				return blockState;
			}

			@Override
			public void processChunk(LevelAccessor levelAccessor) {
				BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
				List<ObjectSet<BlockPos>> list = (List<ObjectSet<BlockPos>>)this.queue.get();

				for (int i = 2; i < list.size(); i++) {
					int j = i - 1;
					ObjectSet<BlockPos> objectSet = (ObjectSet<BlockPos>)list.get(j);
					ObjectSet<BlockPos> objectSet2 = (ObjectSet<BlockPos>)list.get(i);

					for (BlockPos blockPos : objectSet) {
						BlockState blockState = levelAccessor.getBlockState(blockPos);
						if ((Integer)blockState.getValue(BlockStateProperties.DISTANCE) >= j) {
							levelAccessor.setBlock(blockPos, blockState.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(j)), 18);
							if (i != 7) {
								for (Direction direction : DIRECTIONS) {
									mutableBlockPos.setWithOffset(blockPos, direction);
									BlockState blockState2 = levelAccessor.getBlockState(mutableBlockPos);
									if (blockState2.hasProperty(BlockStateProperties.DISTANCE) && (Integer)blockState.getValue(BlockStateProperties.DISTANCE) > i) {
										objectSet2.add(mutableBlockPos.immutable());
									}
								}
							}
						}
					}
				}

				list.clear();
			}
		},
		STEM_BLOCK(Blocks.MELON_STEM, Blocks.PUMPKIN_STEM) {
			@Override
			public BlockState updateShape(
				BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
			) {
				if ((Integer)blockState.getValue(StemBlock.AGE) == 7) {
					StemGrownBlock stemGrownBlock = ((StemBlock)blockState.getBlock()).getFruit();
					if (blockState2.is(stemGrownBlock)) {
						return stemGrownBlock.getAttachedStem().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, direction);
					}
				}

				return blockState;
			}
		};

		public static final Direction[] DIRECTIONS = Direction.values();

		BlockFixers(Block... blocks) {
			this(false, blocks);
		}

		BlockFixers(boolean bl, Block... blocks) {
			for (Block block : blocks) {
				UpgradeData.MAP.put(block, this);
			}

			if (bl) {
				UpgradeData.CHUNKY_FIXERS.add(this);
			}
		}
	}
}
