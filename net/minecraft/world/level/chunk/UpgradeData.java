/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction8;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UpgradeData {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final UpgradeData EMPTY = new UpgradeData(EmptyBlockGetter.INSTANCE);
    private static final Direction8[] DIRECTIONS = Direction8.values();
    private final EnumSet<Direction8> sides = EnumSet.noneOf(Direction8.class);
    private final int[][] index;
    private static final Map<Block, BlockFixer> MAP = new IdentityHashMap<Block, BlockFixer>();
    private static final Set<BlockFixer> CHUNKY_FIXERS = Sets.newHashSet();

    private UpgradeData(LevelHeightAccessor levelHeightAccessor) {
        this.index = new int[levelHeightAccessor.getSectionsCount()][];
    }

    public UpgradeData(CompoundTag compoundTag, LevelHeightAccessor levelHeightAccessor) {
        this(levelHeightAccessor);
        if (compoundTag.contains("Indices", 10)) {
            CompoundTag compoundTag2 = compoundTag.getCompound("Indices");
            for (int i = 0; i < this.index.length; ++i) {
                String string = String.valueOf(i);
                if (!compoundTag2.contains(string, 11)) continue;
                this.index[i] = compoundTag2.getIntArray(string);
            }
        }
        int j = compoundTag.getInt("Sides");
        for (Direction8 direction8 : Direction8.values()) {
            if ((j & 1 << direction8.ordinal()) == 0) continue;
            this.sides.add(direction8);
        }
    }

    public void upgrade(LevelChunk levelChunk) {
        this.upgradeInside(levelChunk);
        for (Direction8 direction8 : DIRECTIONS) {
            UpgradeData.upgradeSides(levelChunk, direction8);
        }
        Level level = levelChunk.getLevel();
        CHUNKY_FIXERS.forEach(blockFixer -> blockFixer.processChunk(level));
    }

    private static void upgradeSides(LevelChunk levelChunk, Direction8 direction8) {
        Level level = levelChunk.getLevel();
        if (!levelChunk.getUpgradeData().sides.remove((Object)direction8)) {
            return;
        }
        Set<Direction> set = direction8.getDirections();
        boolean i = false;
        int j = 15;
        boolean bl = set.contains(Direction.EAST);
        boolean bl2 = set.contains(Direction.WEST);
        boolean bl3 = set.contains(Direction.SOUTH);
        boolean bl4 = set.contains(Direction.NORTH);
        boolean bl5 = set.size() == 1;
        ChunkPos chunkPos = levelChunk.getPos();
        int k = chunkPos.getMinBlockX() + (bl5 && (bl4 || bl3) ? 1 : (bl2 ? 0 : 15));
        int l = chunkPos.getMinBlockX() + (bl5 && (bl4 || bl3) ? 14 : (bl2 ? 0 : 15));
        int m = chunkPos.getMinBlockZ() + (bl5 && (bl || bl2) ? 1 : (bl4 ? 0 : 15));
        int n = chunkPos.getMinBlockZ() + (bl5 && (bl || bl2) ? 14 : (bl4 ? 0 : 15));
        Direction[] directions = Direction.values();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (BlockPos blockPos : BlockPos.betweenClosed(k, level.getMinBuildHeight(), m, l, level.getMaxBuildHeight() - 1, n)) {
            BlockState blockState;
            BlockState blockState2 = blockState = level.getBlockState(blockPos);
            for (Direction direction : directions) {
                mutableBlockPos.setWithOffset(blockPos, direction);
                blockState2 = UpgradeData.updateState(blockState2, direction, level, blockPos, mutableBlockPos);
            }
            Block.updateOrDestroy(blockState, blockState2, level, blockPos, 18);
        }
    }

    private static BlockState updateState(BlockState blockState, Direction direction, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        return MAP.getOrDefault(blockState.getBlock(), BlockFixers.DEFAULT).updateShape(blockState, direction, levelAccessor.getBlockState(blockPos2), levelAccessor, blockPos, blockPos2);
    }

    private void upgradeInside(LevelChunk levelChunk) {
        int i;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
        ChunkPos chunkPos = levelChunk.getPos();
        Level levelAccessor = levelChunk.getLevel();
        for (i = 0; i < this.index.length; ++i) {
            LevelChunkSection levelChunkSection = levelChunk.getSections()[i];
            int[] is = this.index[i];
            this.index[i] = null;
            if (levelChunkSection == null || is == null || is.length <= 0) continue;
            Direction[] directions = Direction.values();
            PalettedContainer<BlockState> palettedContainer = levelChunkSection.getStates();
            for (int j : is) {
                BlockState blockState;
                int k = j & 0xF;
                int l = j >> 8 & 0xF;
                int m = j >> 4 & 0xF;
                mutableBlockPos.set(chunkPos.getMinBlockX() + k, levelChunkSection.bottomBlockY() + l, chunkPos.getMinBlockZ() + m);
                BlockState blockState2 = blockState = palettedContainer.get(j);
                for (Direction direction : directions) {
                    mutableBlockPos2.setWithOffset(mutableBlockPos, direction);
                    if (SectionPos.blockToSectionCoord(mutableBlockPos.getX()) != chunkPos.x || SectionPos.blockToSectionCoord(mutableBlockPos.getZ()) != chunkPos.z) continue;
                    blockState2 = UpgradeData.updateState(blockState2, direction, levelAccessor, mutableBlockPos, mutableBlockPos2);
                }
                Block.updateOrDestroy(blockState, blockState2, levelAccessor, mutableBlockPos, 18);
            }
        }
        for (i = 0; i < this.index.length; ++i) {
            if (this.index[i] != null) {
                LOGGER.warn("Discarding update data for section {} for chunk ({} {})", (Object)levelAccessor.getSectionYFromSectionIndex(i), (Object)chunkPos.x, (Object)chunkPos.z);
            }
            this.index[i] = null;
        }
    }

    public boolean isEmpty() {
        for (int[] is : this.index) {
            if (is == null) continue;
            return false;
        }
        return this.sides.isEmpty();
    }

    public CompoundTag write() {
        int i;
        CompoundTag compoundTag = new CompoundTag();
        CompoundTag compoundTag2 = new CompoundTag();
        for (i = 0; i < this.index.length; ++i) {
            String string = String.valueOf(i);
            if (this.index[i] == null || this.index[i].length == 0) continue;
            compoundTag2.putIntArray(string, this.index[i]);
        }
        if (!compoundTag2.isEmpty()) {
            compoundTag.put("Indices", compoundTag2);
        }
        i = 0;
        for (Direction8 direction8 : this.sides) {
            i |= 1 << direction8.ordinal();
        }
        compoundTag.putByte("Sides", (byte)i);
        return compoundTag;
    }

    static enum BlockFixers implements BlockFixer
    {
        BLACKLIST(new Block[]{Blocks.OBSERVER, Blocks.NETHER_PORTAL, Blocks.WHITE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE_POWDER, Blocks.LIME_CONCRETE_POWDER, Blocks.PINK_CONCRETE_POWDER, Blocks.GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.CYAN_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE_POWDER, Blocks.BROWN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE_POWDER, Blocks.RED_CONCRETE_POWDER, Blocks.BLACK_CONCRETE_POWDER, Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL, Blocks.DRAGON_EGG, Blocks.GRAVEL, Blocks.SAND, Blocks.RED_SAND, Blocks.OAK_SIGN, Blocks.SPRUCE_SIGN, Blocks.BIRCH_SIGN, Blocks.ACACIA_SIGN, Blocks.JUNGLE_SIGN, Blocks.DARK_OAK_SIGN, Blocks.OAK_WALL_SIGN, Blocks.SPRUCE_WALL_SIGN, Blocks.BIRCH_WALL_SIGN, Blocks.ACACIA_WALL_SIGN, Blocks.JUNGLE_WALL_SIGN, Blocks.DARK_OAK_WALL_SIGN}){

            @Override
            public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
                return blockState;
            }
        }
        ,
        DEFAULT(new Block[0]){

            @Override
            public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
                return blockState.updateShape(direction, levelAccessor.getBlockState(blockPos2), levelAccessor, blockPos, blockPos2);
            }
        }
        ,
        CHEST(new Block[]{Blocks.CHEST, Blocks.TRAPPED_CHEST}){

            @Override
            public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
                if (blockState2.is(blockState.getBlock()) && direction.getAxis().isHorizontal() && blockState.getValue(ChestBlock.TYPE) == ChestType.SINGLE && blockState2.getValue(ChestBlock.TYPE) == ChestType.SINGLE) {
                    Direction direction2 = blockState.getValue(ChestBlock.FACING);
                    if (direction.getAxis() != direction2.getAxis() && direction2 == blockState2.getValue(ChestBlock.FACING)) {
                        ChestType chestType = direction == direction2.getClockWise() ? ChestType.LEFT : ChestType.RIGHT;
                        levelAccessor.setBlock(blockPos2, (BlockState)blockState2.setValue(ChestBlock.TYPE, chestType.getOpposite()), 18);
                        if (direction2 == Direction.NORTH || direction2 == Direction.EAST) {
                            BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos);
                            BlockEntity blockEntity2 = levelAccessor.getBlockEntity(blockPos2);
                            if (blockEntity instanceof ChestBlockEntity && blockEntity2 instanceof ChestBlockEntity) {
                                ChestBlockEntity.swapContents((ChestBlockEntity)blockEntity, (ChestBlockEntity)blockEntity2);
                            }
                        }
                        return (BlockState)blockState.setValue(ChestBlock.TYPE, chestType);
                    }
                }
                return blockState;
            }
        }
        ,
        LEAVES(true, new Block[]{Blocks.ACACIA_LEAVES, Blocks.BIRCH_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES}){
            private final ThreadLocal<List<ObjectSet<BlockPos>>> queue = ThreadLocal.withInitial(() -> Lists.newArrayListWithCapacity(7));

            @Override
            public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
                BlockState blockState3 = blockState.updateShape(direction, levelAccessor.getBlockState(blockPos2), levelAccessor, blockPos, blockPos2);
                if (blockState != blockState3) {
                    int i = blockState3.getValue(BlockStateProperties.DISTANCE);
                    List<ObjectSet<BlockPos>> list = this.queue.get();
                    if (list.isEmpty()) {
                        for (int j = 0; j < 7; ++j) {
                            list.add(new ObjectOpenHashSet());
                        }
                    }
                    list.get(i).add(blockPos.immutable());
                }
                return blockState;
            }

            @Override
            public void processChunk(LevelAccessor levelAccessor) {
                BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
                List<ObjectSet<BlockPos>> list = this.queue.get();
                for (int i = 2; i < list.size(); ++i) {
                    int j = i - 1;
                    ObjectSet<BlockPos> objectSet = list.get(j);
                    ObjectSet<BlockPos> objectSet2 = list.get(i);
                    for (BlockPos blockPos : objectSet) {
                        BlockState blockState = levelAccessor.getBlockState(blockPos);
                        if (blockState.getValue(BlockStateProperties.DISTANCE) < j) continue;
                        levelAccessor.setBlock(blockPos, (BlockState)blockState.setValue(BlockStateProperties.DISTANCE, j), 18);
                        if (i == 7) continue;
                        for (Direction direction : DIRECTIONS) {
                            mutableBlockPos.setWithOffset(blockPos, direction);
                            BlockState blockState2 = levelAccessor.getBlockState(mutableBlockPos);
                            if (!blockState2.hasProperty(BlockStateProperties.DISTANCE) || blockState.getValue(BlockStateProperties.DISTANCE) <= i) continue;
                            objectSet2.add(mutableBlockPos.immutable());
                        }
                    }
                }
                list.clear();
            }
        }
        ,
        STEM_BLOCK(new Block[]{Blocks.MELON_STEM, Blocks.PUMPKIN_STEM}){

            @Override
            public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
                StemGrownBlock stemGrownBlock;
                if (blockState.getValue(StemBlock.AGE) == 7 && blockState2.is(stemGrownBlock = ((StemBlock)blockState.getBlock()).getFruit())) {
                    return (BlockState)stemGrownBlock.getAttachedStem().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, direction);
                }
                return blockState;
            }
        };

        public static final Direction[] DIRECTIONS;

        private BlockFixers(Block ... blocks) {
            this(false, blocks);
        }

        private BlockFixers(boolean bl, Block ... blocks) {
            for (Block block : blocks) {
                MAP.put(block, this);
            }
            if (bl) {
                CHUNKY_FIXERS.add(this);
            }
        }

        static {
            DIRECTIONS = Direction.values();
        }
    }

    public static interface BlockFixer {
        public BlockState updateShape(BlockState var1, Direction var2, BlockState var3, LevelAccessor var4, BlockPos var5, BlockPos var6);

        default public void processChunk(LevelAccessor levelAccessor) {
        }
    }
}

