/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.IdMapper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class Block
extends BlockBehaviour
implements ItemLike {
    protected static final Logger LOGGER = LogManager.getLogger();
    public static final IdMapper<BlockState> BLOCK_STATE_REGISTRY = new IdMapper();
    private static final LoadingCache<VoxelShape, Boolean> SHAPE_FULL_BLOCK_CACHE = CacheBuilder.newBuilder().maximumSize(512L).weakKeys().build(new CacheLoader<VoxelShape, Boolean>(){

        @Override
        public Boolean load(VoxelShape voxelShape) {
            return !Shapes.joinIsNotEmpty(Shapes.block(), voxelShape, BooleanOp.NOT_SAME);
        }

        @Override
        public /* synthetic */ Object load(Object object) throws Exception {
            return this.load((VoxelShape)object);
        }
    });
    private static final VoxelShape RIGID_SUPPORT_SHAPE = Shapes.join(Shapes.block(), Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0), BooleanOp.ONLY_FIRST);
    private static final VoxelShape CENTER_SUPPORT_SHAPE = Block.box(7.0, 0.0, 7.0, 9.0, 10.0, 9.0);
    protected final StateDefinition<Block, BlockState> stateDefinition;
    private BlockState defaultBlockState;
    @Nullable
    private String descriptionId;
    @Nullable
    private Item item;
    private static final ThreadLocal<Object2ByteLinkedOpenHashMap<BlockStatePairKey>> OCCLUSION_CACHE = ThreadLocal.withInitial(() -> {
        Object2ByteLinkedOpenHashMap<BlockStatePairKey> object2ByteLinkedOpenHashMap = new Object2ByteLinkedOpenHashMap<BlockStatePairKey>(2048, 0.25f){

            @Override
            protected void rehash(int i) {
            }
        };
        object2ByteLinkedOpenHashMap.defaultReturnValue((byte)127);
        return object2ByteLinkedOpenHashMap;
    });

    public static int getId(@Nullable BlockState blockState) {
        if (blockState == null) {
            return 0;
        }
        int i = BLOCK_STATE_REGISTRY.getId(blockState);
        return i == -1 ? 0 : i;
    }

    public static BlockState stateById(int i) {
        BlockState blockState = BLOCK_STATE_REGISTRY.byId(i);
        return blockState == null ? Blocks.AIR.defaultBlockState() : blockState;
    }

    public static Block byItem(@Nullable Item item) {
        if (item instanceof BlockItem) {
            return ((BlockItem)item).getBlock();
        }
        return Blocks.AIR;
    }

    public static BlockState pushEntitiesUp(BlockState blockState, BlockState blockState2, Level level, BlockPos blockPos) {
        VoxelShape voxelShape = Shapes.joinUnoptimized(blockState.getCollisionShape(level, blockPos), blockState2.getCollisionShape(level, blockPos), BooleanOp.ONLY_SECOND).move(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        List<Entity> list = level.getEntities(null, voxelShape.bounds());
        for (Entity entity : list) {
            double d = Shapes.collide(Direction.Axis.Y, entity.getBoundingBox().move(0.0, 1.0, 0.0), Stream.of(voxelShape), -1.0);
            entity.teleportTo(entity.getX(), entity.getY() + 1.0 + d, entity.getZ());
        }
        return blockState2;
    }

    public static VoxelShape box(double d, double e, double f, double g, double h, double i) {
        return Shapes.box(d / 16.0, e / 16.0, f / 16.0, g / 16.0, h / 16.0, i / 16.0);
    }

    public boolean is(Tag<Block> tag) {
        return tag.contains(this);
    }

    public boolean is(Block block) {
        return this == block;
    }

    public static BlockState updateFromNeighbourShapes(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
        BlockState blockState2 = blockState;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Direction direction : UPDATE_SHAPE_ORDER) {
            mutableBlockPos.setWithOffset(blockPos, direction);
            blockState2 = blockState2.updateShape(direction, levelAccessor.getBlockState(mutableBlockPos), levelAccessor, blockPos, mutableBlockPos);
        }
        return blockState2;
    }

    public static void updateOrDestroy(BlockState blockState, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, int i) {
        Block.updateOrDestroy(blockState, blockState2, levelAccessor, blockPos, i, 512);
    }

    public static void updateOrDestroy(BlockState blockState, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, int i, int j) {
        if (blockState2 != blockState) {
            if (blockState2.isAir()) {
                if (!levelAccessor.isClientSide()) {
                    levelAccessor.destroyBlock(blockPos, (i & 0x20) == 0, null, j);
                }
            } else {
                levelAccessor.setBlock(blockPos, blockState2, i & 0xFFFFFFDF, j);
            }
        }
    }

    public Block(BlockBehaviour.Properties properties) {
        super(properties);
        StateDefinition.Builder<Block, BlockState> builder = new StateDefinition.Builder<Block, BlockState>(this);
        this.createBlockStateDefinition(builder);
        this.stateDefinition = builder.create(Block::defaultBlockState, BlockState::new);
        this.registerDefaultState(this.stateDefinition.any());
    }

    public static boolean isExceptionForConnection(Block block) {
        return block instanceof LeavesBlock || block == Blocks.BARRIER || block == Blocks.CARVED_PUMPKIN || block == Blocks.JACK_O_LANTERN || block == Blocks.MELON || block == Blocks.PUMPKIN || block.is(BlockTags.SHULKER_BOXES);
    }

    public boolean isRandomlyTicking(BlockState blockState) {
        return this.isRandomlyTicking;
    }

    @Environment(value=EnvType.CLIENT)
    public static boolean shouldRenderFace(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        BlockPos blockPos2 = blockPos.relative(direction);
        BlockState blockState2 = blockGetter.getBlockState(blockPos2);
        if (blockState.skipRendering(blockState2, direction)) {
            return false;
        }
        if (blockState2.canOcclude()) {
            BlockStatePairKey blockStatePairKey = new BlockStatePairKey(blockState, blockState2, direction);
            Object2ByteLinkedOpenHashMap<BlockStatePairKey> object2ByteLinkedOpenHashMap = OCCLUSION_CACHE.get();
            byte b = object2ByteLinkedOpenHashMap.getAndMoveToFirst(blockStatePairKey);
            if (b != 127) {
                return b != 0;
            }
            VoxelShape voxelShape = blockState.getFaceOcclusionShape(blockGetter, blockPos, direction);
            VoxelShape voxelShape2 = blockState2.getFaceOcclusionShape(blockGetter, blockPos2, direction.getOpposite());
            boolean bl = Shapes.joinIsNotEmpty(voxelShape, voxelShape2, BooleanOp.ONLY_FIRST);
            if (object2ByteLinkedOpenHashMap.size() == 2048) {
                object2ByteLinkedOpenHashMap.removeLastByte();
            }
            object2ByteLinkedOpenHashMap.putAndMoveToFirst(blockStatePairKey, (byte)(bl ? 1 : 0));
            return bl;
        }
        return true;
    }

    public static boolean canSupportRigidBlock(BlockGetter blockGetter, BlockPos blockPos) {
        BlockState blockState = blockGetter.getBlockState(blockPos);
        return blockState.isCollisionShapeFullBlock(blockGetter, blockPos) && blockState.isFaceSturdy(blockGetter, blockPos, Direction.UP) || !Shapes.joinIsNotEmpty(blockState.getBlockSupportShape(blockGetter, blockPos).getFaceShape(Direction.UP), RIGID_SUPPORT_SHAPE, BooleanOp.ONLY_SECOND);
    }

    public static boolean canSupportCenter(LevelReader levelReader, BlockPos blockPos, Direction direction) {
        BlockState blockState = levelReader.getBlockState(blockPos);
        if (direction == Direction.DOWN && blockState.is(BlockTags.UNSTABLE_BOTTOM_CENTER)) {
            return false;
        }
        return !Shapes.joinIsNotEmpty(blockState.getBlockSupportShape(levelReader, blockPos).getFaceShape(direction), CENTER_SUPPORT_SHAPE, BooleanOp.ONLY_SECOND);
    }

    public static boolean isFaceSturdy(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return Block.isFaceFull(blockState.getBlockSupportShape(blockGetter, blockPos), direction);
    }

    public static boolean isFaceFull(VoxelShape voxelShape, Direction direction) {
        VoxelShape voxelShape2 = voxelShape.getFaceShape(direction);
        return Block.isShapeFullBlock(voxelShape2);
    }

    public static boolean isShapeFullBlock(VoxelShape voxelShape) {
        return SHAPE_FULL_BLOCK_CACHE.getUnchecked(voxelShape);
    }

    public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return !Block.isShapeFullBlock(blockState.getShape(blockGetter, blockPos)) && blockState.getFluidState().isEmpty();
    }

    @Environment(value=EnvType.CLIENT)
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
    }

    public void destroy(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
    }

    public static List<ItemStack> getDrops(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, @Nullable BlockEntity blockEntity) {
        LootContext.Builder builder = new LootContext.Builder(serverLevel).withRandom(serverLevel.random).withParameter(LootContextParams.BLOCK_POS, blockPos).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);
        return blockState.getDrops(builder);
    }

    public static List<ItemStack> getDrops(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack itemStack) {
        LootContext.Builder builder = new LootContext.Builder(serverLevel).withRandom(serverLevel.random).withParameter(LootContextParams.BLOCK_POS, blockPos).withParameter(LootContextParams.TOOL, itemStack).withOptionalParameter(LootContextParams.THIS_ENTITY, entity).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);
        return blockState.getDrops(builder);
    }

    public static void dropResources(BlockState blockState, Level level, BlockPos blockPos) {
        if (level instanceof ServerLevel) {
            Block.getDrops(blockState, (ServerLevel)level, blockPos, null).forEach(itemStack -> Block.popResource(level, blockPos, itemStack));
            blockState.spawnAfterBreak((ServerLevel)level, blockPos, ItemStack.EMPTY);
        }
    }

    public static void dropResources(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, @Nullable BlockEntity blockEntity) {
        if (levelAccessor instanceof ServerLevel) {
            Block.getDrops(blockState, (ServerLevel)levelAccessor, blockPos, blockEntity).forEach(itemStack -> Block.popResource((ServerLevel)levelAccessor, blockPos, itemStack));
            blockState.spawnAfterBreak((ServerLevel)levelAccessor, blockPos, ItemStack.EMPTY);
        }
    }

    public static void dropResources(BlockState blockState, Level level, BlockPos blockPos, @Nullable BlockEntity blockEntity, Entity entity, ItemStack itemStack2) {
        if (level instanceof ServerLevel) {
            Block.getDrops(blockState, (ServerLevel)level, blockPos, blockEntity, entity, itemStack2).forEach(itemStack -> Block.popResource(level, blockPos, itemStack));
            blockState.spawnAfterBreak((ServerLevel)level, blockPos, itemStack2);
        }
    }

    public static void popResource(Level level, BlockPos blockPos, ItemStack itemStack) {
        if (level.isClientSide || itemStack.isEmpty() || !level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            return;
        }
        float f = 0.5f;
        double d = (double)(level.random.nextFloat() * 0.5f) + 0.25;
        double e = (double)(level.random.nextFloat() * 0.5f) + 0.25;
        double g = (double)(level.random.nextFloat() * 0.5f) + 0.25;
        ItemEntity itemEntity = new ItemEntity(level, (double)blockPos.getX() + d, (double)blockPos.getY() + e, (double)blockPos.getZ() + g, itemStack);
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
    }

    protected void popExperience(ServerLevel serverLevel, BlockPos blockPos, int i) {
        if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            while (i > 0) {
                int j = ExperienceOrb.getExperienceValue(i);
                i -= j;
                serverLevel.addFreshEntity(new ExperienceOrb(serverLevel, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, j));
            }
        }
    }

    public float getExplosionResistance() {
        return this.explosionResistance;
    }

    public void wasExploded(Level level, BlockPos blockPos, Explosion explosion) {
    }

    public void stepOn(Level level, BlockPos blockPos, Entity entity) {
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return this.defaultBlockState();
    }

    public void playerDestroy(Level level, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, ItemStack itemStack) {
        player.awardStat(Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005f);
        Block.dropResources(blockState, level, blockPos, blockEntity, player, itemStack);
    }

    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
    }

    public boolean isPossibleToRespawnInThis() {
        return !this.material.isSolid() && !this.material.isLiquid();
    }

    @Environment(value=EnvType.CLIENT)
    public MutableComponent getName() {
        return new TranslatableComponent(this.getDescriptionId());
    }

    public String getDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("block", Registry.BLOCK.getKey(this));
        }
        return this.descriptionId;
    }

    public void fallOn(Level level, BlockPos blockPos, Entity entity, float f) {
        entity.causeFallDamage(f, 1.0f);
    }

    public void updateEntityAfterFallOn(BlockGetter blockGetter, Entity entity) {
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0, 0.0, 1.0));
    }

    @Environment(value=EnvType.CLIENT)
    public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        return new ItemStack(this);
    }

    public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList) {
        nonNullList.add(new ItemStack(this));
    }

    public float getFriction() {
        return this.friction;
    }

    public float getSpeedFactor() {
        return this.speedFactor;
    }

    public float getJumpFactor() {
        return this.jumpFactor;
    }

    public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        level.levelEvent(player, 2001, blockPos, Block.getId(blockState));
        if (this.is(BlockTags.GUARDED_BY_PIGLINS)) {
            PiglinAi.angerNearbyPiglins(player, false);
        }
    }

    public void handleRain(Level level, BlockPos blockPos) {
    }

    public boolean dropFromExplosion(Explosion explosion) {
        return true;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    }

    public StateDefinition<Block, BlockState> getStateDefinition() {
        return this.stateDefinition;
    }

    protected final void registerDefaultState(BlockState blockState) {
        this.defaultBlockState = blockState;
    }

    public final BlockState defaultBlockState() {
        return this.defaultBlockState;
    }

    public SoundType getSoundType(BlockState blockState) {
        return this.soundType;
    }

    @Override
    public Item asItem() {
        if (this.item == null) {
            this.item = Item.byBlock(this);
        }
        return this.item;
    }

    public boolean hasDynamicShape() {
        return this.dynamicShape;
    }

    public String toString() {
        return "Block{" + Registry.BLOCK.getKey(this) + "}";
    }

    @Environment(value=EnvType.CLIENT)
    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter blockGetter, List<Component> list, TooltipFlag tooltipFlag) {
    }

    @Override
    protected Block asBlock() {
        return this;
    }

    public static final class BlockStatePairKey {
        private final BlockState first;
        private final BlockState second;
        private final Direction direction;

        public BlockStatePairKey(BlockState blockState, BlockState blockState2, Direction direction) {
            this.first = blockState;
            this.second = blockState2;
            this.direction = direction;
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof BlockStatePairKey)) {
                return false;
            }
            BlockStatePairKey blockStatePairKey = (BlockStatePairKey)object;
            return this.first == blockStatePairKey.first && this.second == blockStatePairKey.second && this.direction == blockStatePairKey.direction;
        }

        public int hashCode() {
            int i = this.first.hashCode();
            i = 31 * i + this.second.hashCode();
            i = 31 * i + this.direction.hashCode();
            return i;
        }
    }
}

