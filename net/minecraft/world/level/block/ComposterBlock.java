/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ComposterBlock
extends Block
implements WorldlyContainerHolder {
    public static final int READY = 8;
    public static final int MIN_LEVEL = 0;
    public static final int MAX_LEVEL = 7;
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_COMPOSTER;
    public static final Object2FloatMap<ItemLike> COMPOSTABLES = new Object2FloatOpenHashMap<ItemLike>();
    private static final int AABB_SIDE_THICKNESS = 2;
    private static final VoxelShape OUTER_SHAPE = Shapes.block();
    private static final VoxelShape[] SHAPES = Util.make(new VoxelShape[9], voxelShapes -> {
        for (int i = 0; i < 8; ++i) {
            voxelShapes[i] = Shapes.join(OUTER_SHAPE, Block.box(2.0, Math.max(2, 1 + i * 2), 2.0, 14.0, 16.0, 14.0), BooleanOp.ONLY_FIRST);
        }
        voxelShapes[8] = voxelShapes[7];
    });

    public static void bootStrap() {
        COMPOSTABLES.defaultReturnValue(-1.0f);
        float f = 0.3f;
        float g = 0.5f;
        float h = 0.65f;
        float i = 0.85f;
        float j = 1.0f;
        ComposterBlock.add(0.3f, Items.JUNGLE_LEAVES);
        ComposterBlock.add(0.3f, Items.OAK_LEAVES);
        ComposterBlock.add(0.3f, Items.SPRUCE_LEAVES);
        ComposterBlock.add(0.3f, Items.DARK_OAK_LEAVES);
        ComposterBlock.add(0.3f, Items.ACACIA_LEAVES);
        ComposterBlock.add(0.3f, Items.CHERRY_LEAVES);
        ComposterBlock.add(0.3f, Items.BIRCH_LEAVES);
        ComposterBlock.add(0.3f, Items.AZALEA_LEAVES);
        ComposterBlock.add(0.3f, Items.MANGROVE_LEAVES);
        ComposterBlock.add(0.3f, Items.OAK_SAPLING);
        ComposterBlock.add(0.3f, Items.SPRUCE_SAPLING);
        ComposterBlock.add(0.3f, Items.BIRCH_SAPLING);
        ComposterBlock.add(0.3f, Items.JUNGLE_SAPLING);
        ComposterBlock.add(0.3f, Items.ACACIA_SAPLING);
        ComposterBlock.add(0.3f, Items.CHERRY_SAPLING);
        ComposterBlock.add(0.3f, Items.DARK_OAK_SAPLING);
        ComposterBlock.add(0.3f, Items.MANGROVE_PROPAGULE);
        ComposterBlock.add(0.3f, Items.BEETROOT_SEEDS);
        ComposterBlock.add(0.3f, Items.DRIED_KELP);
        ComposterBlock.add(0.3f, Items.GRASS);
        ComposterBlock.add(0.3f, Items.KELP);
        ComposterBlock.add(0.3f, Items.MELON_SEEDS);
        ComposterBlock.add(0.3f, Items.PUMPKIN_SEEDS);
        ComposterBlock.add(0.3f, Items.SEAGRASS);
        ComposterBlock.add(0.3f, Items.SWEET_BERRIES);
        ComposterBlock.add(0.3f, Items.GLOW_BERRIES);
        ComposterBlock.add(0.3f, Items.WHEAT_SEEDS);
        ComposterBlock.add(0.3f, Items.MOSS_CARPET);
        ComposterBlock.add(0.3f, Items.PINK_PETALS);
        ComposterBlock.add(0.3f, Items.SMALL_DRIPLEAF);
        ComposterBlock.add(0.3f, Items.HANGING_ROOTS);
        ComposterBlock.add(0.3f, Items.MANGROVE_ROOTS);
        ComposterBlock.add(0.3f, Items.TORCHFLOWER_SEEDS);
        ComposterBlock.add(0.5f, Items.DRIED_KELP_BLOCK);
        ComposterBlock.add(0.5f, Items.TALL_GRASS);
        ComposterBlock.add(0.5f, Items.FLOWERING_AZALEA_LEAVES);
        ComposterBlock.add(0.5f, Items.CACTUS);
        ComposterBlock.add(0.5f, Items.SUGAR_CANE);
        ComposterBlock.add(0.5f, Items.VINE);
        ComposterBlock.add(0.5f, Items.NETHER_SPROUTS);
        ComposterBlock.add(0.5f, Items.WEEPING_VINES);
        ComposterBlock.add(0.5f, Items.TWISTING_VINES);
        ComposterBlock.add(0.5f, Items.MELON_SLICE);
        ComposterBlock.add(0.5f, Items.GLOW_LICHEN);
        ComposterBlock.add(0.65f, Items.SEA_PICKLE);
        ComposterBlock.add(0.65f, Items.LILY_PAD);
        ComposterBlock.add(0.65f, Items.PUMPKIN);
        ComposterBlock.add(0.65f, Items.CARVED_PUMPKIN);
        ComposterBlock.add(0.65f, Items.MELON);
        ComposterBlock.add(0.65f, Items.APPLE);
        ComposterBlock.add(0.65f, Items.BEETROOT);
        ComposterBlock.add(0.65f, Items.CARROT);
        ComposterBlock.add(0.65f, Items.COCOA_BEANS);
        ComposterBlock.add(0.65f, Items.POTATO);
        ComposterBlock.add(0.65f, Items.WHEAT);
        ComposterBlock.add(0.65f, Items.BROWN_MUSHROOM);
        ComposterBlock.add(0.65f, Items.RED_MUSHROOM);
        ComposterBlock.add(0.65f, Items.MUSHROOM_STEM);
        ComposterBlock.add(0.65f, Items.CRIMSON_FUNGUS);
        ComposterBlock.add(0.65f, Items.WARPED_FUNGUS);
        ComposterBlock.add(0.65f, Items.NETHER_WART);
        ComposterBlock.add(0.65f, Items.CRIMSON_ROOTS);
        ComposterBlock.add(0.65f, Items.WARPED_ROOTS);
        ComposterBlock.add(0.65f, Items.SHROOMLIGHT);
        ComposterBlock.add(0.65f, Items.DANDELION);
        ComposterBlock.add(0.65f, Items.POPPY);
        ComposterBlock.add(0.65f, Items.BLUE_ORCHID);
        ComposterBlock.add(0.65f, Items.ALLIUM);
        ComposterBlock.add(0.65f, Items.AZURE_BLUET);
        ComposterBlock.add(0.65f, Items.RED_TULIP);
        ComposterBlock.add(0.65f, Items.ORANGE_TULIP);
        ComposterBlock.add(0.65f, Items.WHITE_TULIP);
        ComposterBlock.add(0.65f, Items.PINK_TULIP);
        ComposterBlock.add(0.65f, Items.OXEYE_DAISY);
        ComposterBlock.add(0.65f, Items.CORNFLOWER);
        ComposterBlock.add(0.65f, Items.LILY_OF_THE_VALLEY);
        ComposterBlock.add(0.65f, Items.WITHER_ROSE);
        ComposterBlock.add(0.65f, Items.FERN);
        ComposterBlock.add(0.65f, Items.SUNFLOWER);
        ComposterBlock.add(0.65f, Items.LILAC);
        ComposterBlock.add(0.65f, Items.ROSE_BUSH);
        ComposterBlock.add(0.65f, Items.PEONY);
        ComposterBlock.add(0.65f, Items.LARGE_FERN);
        ComposterBlock.add(0.65f, Items.SPORE_BLOSSOM);
        ComposterBlock.add(0.65f, Items.AZALEA);
        ComposterBlock.add(0.65f, Items.MOSS_BLOCK);
        ComposterBlock.add(0.65f, Items.BIG_DRIPLEAF);
        ComposterBlock.add(0.85f, Items.HAY_BLOCK);
        ComposterBlock.add(0.85f, Items.BROWN_MUSHROOM_BLOCK);
        ComposterBlock.add(0.85f, Items.RED_MUSHROOM_BLOCK);
        ComposterBlock.add(0.85f, Items.NETHER_WART_BLOCK);
        ComposterBlock.add(0.85f, Items.WARPED_WART_BLOCK);
        ComposterBlock.add(0.85f, Items.FLOWERING_AZALEA);
        ComposterBlock.add(0.85f, Items.BREAD);
        ComposterBlock.add(0.85f, Items.BAKED_POTATO);
        ComposterBlock.add(0.85f, Items.COOKIE);
        ComposterBlock.add(0.85f, Items.TORCHFLOWER);
        ComposterBlock.add(1.0f, Items.CAKE);
        ComposterBlock.add(1.0f, Items.PUMPKIN_PIE);
    }

    private static void add(float f, ItemLike itemLike) {
        COMPOSTABLES.put((ItemLike)itemLike.asItem(), f);
    }

    public ComposterBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(LEVEL, 0));
    }

    public static void handleFill(Level level, BlockPos blockPos, boolean bl) {
        BlockState blockState = level.getBlockState(blockPos);
        level.playLocalSound(blockPos, bl ? SoundEvents.COMPOSTER_FILL_SUCCESS : SoundEvents.COMPOSTER_FILL, SoundSource.BLOCKS, 1.0f, 1.0f, false);
        double d = blockState.getShape(level, blockPos).max(Direction.Axis.Y, 0.5, 0.5) + 0.03125;
        double e = 0.13125f;
        double f = 0.7375f;
        RandomSource randomSource = level.getRandom();
        for (int i = 0; i < 10; ++i) {
            double g = randomSource.nextGaussian() * 0.02;
            double h = randomSource.nextGaussian() * 0.02;
            double j = randomSource.nextGaussian() * 0.02;
            level.addParticle(ParticleTypes.COMPOSTER, (double)blockPos.getX() + (double)0.13125f + (double)0.7375f * (double)randomSource.nextFloat(), (double)blockPos.getY() + d + (double)randomSource.nextFloat() * (1.0 - d), (double)blockPos.getZ() + (double)0.13125f + (double)0.7375f * (double)randomSource.nextFloat(), g, h, j);
        }
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPES[blockState.getValue(LEVEL)];
    }

    @Override
    public VoxelShape getInteractionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return OUTER_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPES[0];
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState.getValue(LEVEL) == 7) {
            level.scheduleTick(blockPos, blockState.getBlock(), 20);
        }
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        int i = blockState.getValue(LEVEL);
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (i < 8 && COMPOSTABLES.containsKey(itemStack.getItem())) {
            if (i < 7 && !level.isClientSide) {
                BlockState blockState2 = ComposterBlock.addItem(player, blockState, level, blockPos, itemStack);
                level.levelEvent(1500, blockPos, blockState != blockState2 ? 1 : 0);
                player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (i == 8) {
            ComposterBlock.extractProduce(player, blockState, level, blockPos);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public static BlockState insertItem(Entity entity, BlockState blockState, ServerLevel serverLevel, ItemStack itemStack, BlockPos blockPos) {
        int i = blockState.getValue(LEVEL);
        if (i < 7 && COMPOSTABLES.containsKey(itemStack.getItem())) {
            BlockState blockState2 = ComposterBlock.addItem(entity, blockState, serverLevel, blockPos, itemStack);
            itemStack.shrink(1);
            return blockState2;
        }
        return blockState;
    }

    public static BlockState extractProduce(Entity entity, BlockState blockState, Level level, BlockPos blockPos) {
        if (!level.isClientSide) {
            Vec3 vec3 = Vec3.atLowerCornerWithOffset(blockPos, 0.5, 1.01, 0.5).offsetRandom(level.random, 0.7f);
            ItemEntity itemEntity = new ItemEntity(level, vec3.x(), vec3.y(), vec3.z(), new ItemStack(Items.BONE_MEAL));
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
        BlockState blockState2 = ComposterBlock.empty(entity, blockState, level, blockPos);
        level.playSound(null, blockPos, SoundEvents.COMPOSTER_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
        return blockState2;
    }

    static BlockState empty(@Nullable Entity entity, BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
        BlockState blockState2 = (BlockState)blockState.setValue(LEVEL, 0);
        levelAccessor.setBlock(blockPos, blockState2, 3);
        levelAccessor.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(entity, blockState2));
        return blockState2;
    }

    static BlockState addItem(@Nullable Entity entity, BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, ItemStack itemStack) {
        int i = blockState.getValue(LEVEL);
        float f = COMPOSTABLES.getFloat(itemStack.getItem());
        if (i == 0 && f > 0.0f || levelAccessor.getRandom().nextDouble() < (double)f) {
            int j = i + 1;
            BlockState blockState2 = (BlockState)blockState.setValue(LEVEL, j);
            levelAccessor.setBlock(blockPos, blockState2, 3);
            levelAccessor.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(entity, blockState2));
            if (j == 7) {
                levelAccessor.scheduleTick(blockPos, blockState.getBlock(), 20);
            }
            return blockState2;
        }
        return blockState;
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (blockState.getValue(LEVEL) == 7) {
            serverLevel.setBlock(blockPos, (BlockState)blockState.cycle(LEVEL), 3);
            serverLevel.playSound(null, blockPos, SoundEvents.COMPOSTER_READY, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        return blockState.getValue(LEVEL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public WorldlyContainer getContainer(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
        int i = blockState.getValue(LEVEL);
        if (i == 8) {
            return new OutputContainer(blockState, levelAccessor, blockPos, new ItemStack(Items.BONE_MEAL));
        }
        if (i < 7) {
            return new InputContainer(blockState, levelAccessor, blockPos);
        }
        return new EmptyContainer();
    }

    static class OutputContainer
    extends SimpleContainer
    implements WorldlyContainer {
        private final BlockState state;
        private final LevelAccessor level;
        private final BlockPos pos;
        private boolean changed;

        public OutputContainer(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, ItemStack itemStack) {
            super(itemStack);
            this.state = blockState;
            this.level = levelAccessor;
            this.pos = blockPos;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int[] getSlotsForFace(Direction direction) {
            int[] nArray;
            if (direction == Direction.DOWN) {
                int[] nArray2 = new int[1];
                nArray = nArray2;
                nArray2[0] = 0;
            } else {
                nArray = new int[]{};
            }
            return nArray;
        }

        @Override
        public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
            return false;
        }

        @Override
        public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
            return !this.changed && direction == Direction.DOWN && itemStack.is(Items.BONE_MEAL);
        }

        @Override
        public void setChanged() {
            ComposterBlock.empty(null, this.state, this.level, this.pos);
            this.changed = true;
        }
    }

    static class InputContainer
    extends SimpleContainer
    implements WorldlyContainer {
        private final BlockState state;
        private final LevelAccessor level;
        private final BlockPos pos;
        private boolean changed;

        public InputContainer(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
            super(1);
            this.state = blockState;
            this.level = levelAccessor;
            this.pos = blockPos;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int[] getSlotsForFace(Direction direction) {
            int[] nArray;
            if (direction == Direction.UP) {
                int[] nArray2 = new int[1];
                nArray = nArray2;
                nArray2[0] = 0;
            } else {
                nArray = new int[]{};
            }
            return nArray;
        }

        @Override
        public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
            return !this.changed && direction == Direction.UP && COMPOSTABLES.containsKey(itemStack.getItem());
        }

        @Override
        public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
            return false;
        }

        @Override
        public void setChanged() {
            ItemStack itemStack = this.getItem(0);
            if (!itemStack.isEmpty()) {
                this.changed = true;
                BlockState blockState = ComposterBlock.addItem(null, this.state, this.level, this.pos, itemStack);
                this.level.levelEvent(1500, this.pos, blockState != this.state ? 1 : 0);
                this.removeItemNoUpdate(0);
            }
        }
    }

    static class EmptyContainer
    extends SimpleContainer
    implements WorldlyContainer {
        public EmptyContainer() {
            super(0);
        }

        @Override
        public int[] getSlotsForFace(Direction direction) {
            return new int[0];
        }

        @Override
        public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
            return false;
        }

        @Override
        public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
            return false;
        }
    }
}

