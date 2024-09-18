package net.minecraft.world.level.block.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.DependantName;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BlockBehaviour implements FeatureElement {
	protected static final Direction[] UPDATE_SHAPE_ORDER = new Direction[]{
		Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.DOWN, Direction.UP
	};
	protected final boolean hasCollision;
	protected final float explosionResistance;
	protected final boolean isRandomlyTicking;
	protected final SoundType soundType;
	protected final float friction;
	protected final float speedFactor;
	protected final float jumpFactor;
	protected final boolean dynamicShape;
	protected final FeatureFlagSet requiredFeatures;
	protected final BlockBehaviour.Properties properties;
	protected final Optional<ResourceKey<LootTable>> drops;
	protected final String descriptionId;

	public BlockBehaviour(BlockBehaviour.Properties properties) {
		this.hasCollision = properties.hasCollision;
		this.drops = properties.effectiveDrops();
		this.descriptionId = properties.effectiveDescriptionId();
		this.explosionResistance = properties.explosionResistance;
		this.isRandomlyTicking = properties.isRandomlyTicking;
		this.soundType = properties.soundType;
		this.friction = properties.friction;
		this.speedFactor = properties.speedFactor;
		this.jumpFactor = properties.jumpFactor;
		this.dynamicShape = properties.dynamicShape;
		this.requiredFeatures = properties.requiredFeatures;
		this.properties = properties;
	}

	public BlockBehaviour.Properties properties() {
		return this.properties;
	}

	protected abstract MapCodec<? extends Block> codec();

	protected static <B extends Block> RecordCodecBuilder<B, BlockBehaviour.Properties> propertiesCodec() {
		return BlockBehaviour.Properties.CODEC.fieldOf("properties").forGetter(BlockBehaviour::properties);
	}

	public static <B extends Block> MapCodec<B> simpleCodec(Function<BlockBehaviour.Properties, B> function) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(propertiesCodec()).apply(instance, function));
	}

	protected void updateIndirectNeighbourShapes(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, int i, int j) {
	}

	protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
		switch (pathComputationType) {
			case LAND:
				return !blockState.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
			case WATER:
				return blockState.getFluidState().is(FluidTags.WATER);
			case AIR:
				return !blockState.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
			default:
				return false;
		}
	}

	protected BlockState updateShape(
		BlockState blockState,
		LevelReader levelReader,
		ScheduledTickAccess scheduledTickAccess,
		BlockPos blockPos,
		Direction direction,
		BlockPos blockPos2,
		BlockState blockState2,
		RandomSource randomSource
	) {
		return blockState;
	}

	protected boolean skipRendering(BlockState blockState, BlockState blockState2, Direction direction) {
		return false;
	}

	protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
	}

	protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
	}

	protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (blockState.hasBlockEntity() && !blockState.is(blockState2.getBlock())) {
			level.removeBlockEntity(blockPos);
		}
	}

	protected void onExplosionHit(
		BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Explosion explosion, BiConsumer<ItemStack, BlockPos> biConsumer
	) {
		if (!blockState.isAir() && explosion.getBlockInteraction() != Explosion.BlockInteraction.TRIGGER_BLOCK) {
			Block block = blockState.getBlock();
			boolean bl = explosion.getIndirectSourceEntity() instanceof Player;
			if (block.dropFromExplosion(explosion)) {
				BlockEntity blockEntity = blockState.hasBlockEntity() ? serverLevel.getBlockEntity(blockPos) : null;
				LootParams.Builder builder = new LootParams.Builder(serverLevel)
					.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos))
					.withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
					.withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity)
					.withOptionalParameter(LootContextParams.THIS_ENTITY, explosion.getDirectSourceEntity());
				if (explosion.getBlockInteraction() == Explosion.BlockInteraction.DESTROY_WITH_DECAY) {
					builder.withParameter(LootContextParams.EXPLOSION_RADIUS, explosion.radius());
				}

				blockState.spawnAfterBreak(serverLevel, blockPos, ItemStack.EMPTY, bl);
				blockState.getDrops(builder).forEach(itemStack -> biConsumer.accept(itemStack, blockPos));
			}

			serverLevel.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
			block.wasExploded(serverLevel, blockPos, explosion);
		}
	}

	protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		return InteractionResult.PASS;
	}

	protected InteractionResult useItemOn(
		ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		return InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	protected boolean triggerEvent(BlockState blockState, Level level, BlockPos blockPos, int i, int j) {
		return false;
	}

	protected RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	protected boolean useShapeForLightOcclusion(BlockState blockState) {
		return false;
	}

	protected boolean isSignalSource(BlockState blockState) {
		return false;
	}

	protected FluidState getFluidState(BlockState blockState) {
		return Fluids.EMPTY.defaultFluidState();
	}

	protected boolean hasAnalogOutputSignal(BlockState blockState) {
		return false;
	}

	protected float getMaxHorizontalOffset() {
		return 0.25F;
	}

	protected float getMaxVerticalOffset() {
		return 0.2F;
	}

	@Override
	public FeatureFlagSet requiredFeatures() {
		return this.requiredFeatures;
	}

	protected BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState;
	}

	protected BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState;
	}

	protected boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
		return blockState.canBeReplaced() && (blockPlaceContext.getItemInHand().isEmpty() || !blockPlaceContext.getItemInHand().is(this.asItem()));
	}

	protected boolean canBeReplaced(BlockState blockState, Fluid fluid) {
		return blockState.canBeReplaced() || !blockState.isSolid();
	}

	protected List<ItemStack> getDrops(BlockState blockState, LootParams.Builder builder) {
		if (this.drops.isEmpty()) {
			return Collections.emptyList();
		} else {
			LootParams lootParams = builder.withParameter(LootContextParams.BLOCK_STATE, blockState).create(LootContextParamSets.BLOCK);
			ServerLevel serverLevel = lootParams.getLevel();
			LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable((ResourceKey<LootTable>)this.drops.get());
			return lootTable.getRandomItems(lootParams);
		}
	}

	protected long getSeed(BlockState blockState, BlockPos blockPos) {
		return Mth.getSeed(blockPos);
	}

	protected VoxelShape getOcclusionShape(BlockState blockState) {
		return blockState.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
	}

	protected VoxelShape getBlockSupportShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return this.getCollisionShape(blockState, blockGetter, blockPos, CollisionContext.empty());
	}

	protected VoxelShape getInteractionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return Shapes.empty();
	}

	protected int getLightBlock(BlockState blockState) {
		if (blockState.isSolidRender()) {
			return 15;
		} else {
			return blockState.propagatesSkylightDown() ? 0 : 1;
		}
	}

	@Nullable
	protected MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
		return null;
	}

	protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return true;
	}

	protected float getShadeBrightness(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.isCollisionShapeFullBlock(blockGetter, blockPos) ? 0.2F : 1.0F;
	}

	protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return 0;
	}

	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return Shapes.block();
	}

	protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return this.hasCollision ? blockState.getShape(blockGetter, blockPos) : Shapes.empty();
	}

	protected boolean isCollisionShapeFullBlock(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return Block.isShapeFullBlock(blockState.getCollisionShape(blockGetter, blockPos));
	}

	protected VoxelShape getVisualShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return this.getCollisionShape(blockState, blockGetter, blockPos, collisionContext);
	}

	protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
	}

	protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
	}

	protected float getDestroyProgress(BlockState blockState, Player player, BlockGetter blockGetter, BlockPos blockPos) {
		float f = blockState.getDestroySpeed(blockGetter, blockPos);
		if (f == -1.0F) {
			return 0.0F;
		} else {
			int i = player.hasCorrectToolForDrops(blockState) ? 30 : 100;
			return player.getDestroySpeed(blockState) / f / (float)i;
		}
	}

	protected void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean bl) {
	}

	protected void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
	}

	protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return 0;
	}

	protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
	}

	protected VoxelShape getEntityInsideCollisionShape(BlockState blockState, Level level, BlockPos blockPos) {
		return Shapes.block();
	}

	protected int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return 0;
	}

	public final Optional<ResourceKey<LootTable>> getLootTable() {
		return this.drops;
	}

	public final String getDescriptionId() {
		return this.descriptionId;
	}

	protected void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
	}

	protected boolean propagatesSkylightDown(BlockState blockState) {
		return !Block.isShapeFullBlock(blockState.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)) && blockState.getFluidState().isEmpty();
	}

	protected boolean isRandomlyTicking(BlockState blockState) {
		return this.isRandomlyTicking;
	}

	protected SoundType getSoundType(BlockState blockState) {
		return this.soundType;
	}

	public abstract Item asItem();

	protected abstract Block asBlock();

	public MapColor defaultMapColor() {
		return (MapColor)this.properties.mapColor.apply(this.asBlock().defaultBlockState());
	}

	public float defaultDestroyTime() {
		return this.properties.destroyTime;
	}

	public abstract static class BlockStateBase extends StateHolder<Block, BlockState> {
		private static final Direction[] DIRECTIONS = Direction.values();
		private static final VoxelShape[] EMPTY_OCCLUSION_SHAPES = Util.make(
			new VoxelShape[DIRECTIONS.length], voxelShapes -> Arrays.fill(voxelShapes, Shapes.empty())
		);
		private static final VoxelShape[] FULL_BLOCK_OCCLUSION_SHAPES = Util.make(
			new VoxelShape[DIRECTIONS.length], voxelShapes -> Arrays.fill(voxelShapes, Shapes.block())
		);
		private final int lightEmission;
		private final boolean useShapeForLightOcclusion;
		private final boolean isAir;
		private final boolean ignitedByLava;
		@Deprecated
		private final boolean liquid;
		@Deprecated
		private boolean legacySolid;
		private final PushReaction pushReaction;
		private final MapColor mapColor;
		private final float destroySpeed;
		private final boolean requiresCorrectToolForDrops;
		private final boolean canOcclude;
		private final BlockBehaviour.StatePredicate isRedstoneConductor;
		private final BlockBehaviour.StatePredicate isSuffocating;
		private final BlockBehaviour.StatePredicate isViewBlocking;
		private final BlockBehaviour.StatePredicate hasPostProcess;
		private final BlockBehaviour.StatePredicate emissiveRendering;
		@Nullable
		private final BlockBehaviour.OffsetFunction offsetFunction;
		private final boolean spawnTerrainParticles;
		private final NoteBlockInstrument instrument;
		private final boolean replaceable;
		@Nullable
		private BlockBehaviour.BlockStateBase.Cache cache;
		private FluidState fluidState = Fluids.EMPTY.defaultFluidState();
		private boolean isRandomlyTicking;
		private boolean solidRender;
		private VoxelShape occlusionShape;
		private VoxelShape[] occlusionShapesByFace;
		private boolean propagatesSkylightDown;
		private int lightBlock;

		protected BlockStateBase(Block block, Reference2ObjectArrayMap<Property<?>, Comparable<?>> reference2ObjectArrayMap, MapCodec<BlockState> mapCodec) {
			super(block, reference2ObjectArrayMap, mapCodec);
			BlockBehaviour.Properties properties = block.properties;
			this.lightEmission = properties.lightEmission.applyAsInt(this.asState());
			this.useShapeForLightOcclusion = block.useShapeForLightOcclusion(this.asState());
			this.isAir = properties.isAir;
			this.ignitedByLava = properties.ignitedByLava;
			this.liquid = properties.liquid;
			this.pushReaction = properties.pushReaction;
			this.mapColor = (MapColor)properties.mapColor.apply(this.asState());
			this.destroySpeed = properties.destroyTime;
			this.requiresCorrectToolForDrops = properties.requiresCorrectToolForDrops;
			this.canOcclude = properties.canOcclude;
			this.isRedstoneConductor = properties.isRedstoneConductor;
			this.isSuffocating = properties.isSuffocating;
			this.isViewBlocking = properties.isViewBlocking;
			this.hasPostProcess = properties.hasPostProcess;
			this.emissiveRendering = properties.emissiveRendering;
			this.offsetFunction = properties.offsetFunction;
			this.spawnTerrainParticles = properties.spawnTerrainParticles;
			this.instrument = properties.instrument;
			this.replaceable = properties.replaceable;
		}

		private boolean calculateSolid() {
			if (this.owner.properties.forceSolidOn) {
				return true;
			} else if (this.owner.properties.forceSolidOff) {
				return false;
			} else if (this.cache == null) {
				return false;
			} else {
				VoxelShape voxelShape = this.cache.collisionShape;
				if (voxelShape.isEmpty()) {
					return false;
				} else {
					AABB aABB = voxelShape.bounds();
					return aABB.getSize() >= 0.7291666666666666 ? true : aABB.getYsize() >= 1.0;
				}
			}
		}

		public void initCache() {
			this.fluidState = this.owner.getFluidState(this.asState());
			this.isRandomlyTicking = this.owner.isRandomlyTicking(this.asState());
			if (!this.getBlock().hasDynamicShape()) {
				this.cache = new BlockBehaviour.BlockStateBase.Cache(this.asState());
			}

			this.legacySolid = this.calculateSolid();
			this.occlusionShape = this.canOcclude ? this.owner.getOcclusionShape(this.asState()) : Shapes.empty();
			this.solidRender = Block.isShapeFullBlock(this.occlusionShape);
			if (this.occlusionShape.isEmpty()) {
				this.occlusionShapesByFace = EMPTY_OCCLUSION_SHAPES;
			} else if (this.solidRender) {
				this.occlusionShapesByFace = FULL_BLOCK_OCCLUSION_SHAPES;
			} else {
				this.occlusionShapesByFace = new VoxelShape[DIRECTIONS.length];

				for (Direction direction : DIRECTIONS) {
					this.occlusionShapesByFace[direction.ordinal()] = this.occlusionShape.getFaceShape(direction);
				}
			}

			this.propagatesSkylightDown = this.owner.propagatesSkylightDown(this.asState());
			this.lightBlock = this.owner.getLightBlock(this.asState());
		}

		public Block getBlock() {
			return this.owner;
		}

		public Holder<Block> getBlockHolder() {
			return this.owner.builtInRegistryHolder();
		}

		@Deprecated
		public boolean blocksMotion() {
			Block block = this.getBlock();
			return block != Blocks.COBWEB && block != Blocks.BAMBOO_SAPLING && this.isSolid();
		}

		@Deprecated
		public boolean isSolid() {
			return this.legacySolid;
		}

		public boolean isValidSpawn(BlockGetter blockGetter, BlockPos blockPos, EntityType<?> entityType) {
			return this.getBlock().properties.isValidSpawn.test(this.asState(), blockGetter, blockPos, entityType);
		}

		public boolean propagatesSkylightDown() {
			return this.propagatesSkylightDown;
		}

		public int getLightBlock() {
			return this.lightBlock;
		}

		public VoxelShape getFaceOcclusionShape(Direction direction) {
			return this.occlusionShapesByFace[direction.ordinal()];
		}

		public VoxelShape getOcclusionShape() {
			return this.occlusionShape;
		}

		public boolean hasLargeCollisionShape() {
			return this.cache == null || this.cache.largeCollisionShape;
		}

		public boolean useShapeForLightOcclusion() {
			return this.useShapeForLightOcclusion;
		}

		public int getLightEmission() {
			return this.lightEmission;
		}

		public boolean isAir() {
			return this.isAir;
		}

		public boolean ignitedByLava() {
			return this.ignitedByLava;
		}

		@Deprecated
		public boolean liquid() {
			return this.liquid;
		}

		public MapColor getMapColor(BlockGetter blockGetter, BlockPos blockPos) {
			return this.mapColor;
		}

		public BlockState rotate(Rotation rotation) {
			return this.getBlock().rotate(this.asState(), rotation);
		}

		public BlockState mirror(Mirror mirror) {
			return this.getBlock().mirror(this.asState(), mirror);
		}

		public RenderShape getRenderShape() {
			return this.getBlock().getRenderShape(this.asState());
		}

		public boolean emissiveRendering(BlockGetter blockGetter, BlockPos blockPos) {
			return this.emissiveRendering.test(this.asState(), blockGetter, blockPos);
		}

		public float getShadeBrightness(BlockGetter blockGetter, BlockPos blockPos) {
			return this.getBlock().getShadeBrightness(this.asState(), blockGetter, blockPos);
		}

		public boolean isRedstoneConductor(BlockGetter blockGetter, BlockPos blockPos) {
			return this.isRedstoneConductor.test(this.asState(), blockGetter, blockPos);
		}

		public boolean isSignalSource() {
			return this.getBlock().isSignalSource(this.asState());
		}

		public int getSignal(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
			return this.getBlock().getSignal(this.asState(), blockGetter, blockPos, direction);
		}

		public boolean hasAnalogOutputSignal() {
			return this.getBlock().hasAnalogOutputSignal(this.asState());
		}

		public int getAnalogOutputSignal(Level level, BlockPos blockPos) {
			return this.getBlock().getAnalogOutputSignal(this.asState(), level, blockPos);
		}

		public float getDestroySpeed(BlockGetter blockGetter, BlockPos blockPos) {
			return this.destroySpeed;
		}

		public float getDestroyProgress(Player player, BlockGetter blockGetter, BlockPos blockPos) {
			return this.getBlock().getDestroyProgress(this.asState(), player, blockGetter, blockPos);
		}

		public int getDirectSignal(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
			return this.getBlock().getDirectSignal(this.asState(), blockGetter, blockPos, direction);
		}

		public PushReaction getPistonPushReaction() {
			return this.pushReaction;
		}

		public boolean isSolidRender() {
			return this.solidRender;
		}

		public boolean canOcclude() {
			return this.canOcclude;
		}

		public boolean skipRendering(BlockState blockState, Direction direction) {
			return this.getBlock().skipRendering(this.asState(), blockState, direction);
		}

		public VoxelShape getShape(BlockGetter blockGetter, BlockPos blockPos) {
			return this.getShape(blockGetter, blockPos, CollisionContext.empty());
		}

		public VoxelShape getShape(BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
			return this.getBlock().getShape(this.asState(), blockGetter, blockPos, collisionContext);
		}

		public VoxelShape getCollisionShape(BlockGetter blockGetter, BlockPos blockPos) {
			return this.cache != null ? this.cache.collisionShape : this.getCollisionShape(blockGetter, blockPos, CollisionContext.empty());
		}

		public VoxelShape getCollisionShape(BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
			return this.getBlock().getCollisionShape(this.asState(), blockGetter, blockPos, collisionContext);
		}

		public VoxelShape getBlockSupportShape(BlockGetter blockGetter, BlockPos blockPos) {
			return this.getBlock().getBlockSupportShape(this.asState(), blockGetter, blockPos);
		}

		public VoxelShape getVisualShape(BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
			return this.getBlock().getVisualShape(this.asState(), blockGetter, blockPos, collisionContext);
		}

		public VoxelShape getInteractionShape(BlockGetter blockGetter, BlockPos blockPos) {
			return this.getBlock().getInteractionShape(this.asState(), blockGetter, blockPos);
		}

		public final boolean entityCanStandOn(BlockGetter blockGetter, BlockPos blockPos, Entity entity) {
			return this.entityCanStandOnFace(blockGetter, blockPos, entity, Direction.UP);
		}

		public final boolean entityCanStandOnFace(BlockGetter blockGetter, BlockPos blockPos, Entity entity, Direction direction) {
			return Block.isFaceFull(this.getCollisionShape(blockGetter, blockPos, CollisionContext.of(entity)), direction);
		}

		public Vec3 getOffset(BlockPos blockPos) {
			BlockBehaviour.OffsetFunction offsetFunction = this.offsetFunction;
			return offsetFunction != null ? offsetFunction.evaluate(this.asState(), blockPos) : Vec3.ZERO;
		}

		public boolean hasOffsetFunction() {
			return this.offsetFunction != null;
		}

		public boolean triggerEvent(Level level, BlockPos blockPos, int i, int j) {
			return this.getBlock().triggerEvent(this.asState(), level, blockPos, i, j);
		}

		public void handleNeighborChanged(Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
			DebugPackets.sendNeighborsUpdatePacket(level, blockPos);
			this.getBlock().neighborChanged(this.asState(), level, blockPos, block, orientation, bl);
		}

		public final void updateNeighbourShapes(LevelAccessor levelAccessor, BlockPos blockPos, int i) {
			this.updateNeighbourShapes(levelAccessor, blockPos, i, 512);
		}

		public final void updateNeighbourShapes(LevelAccessor levelAccessor, BlockPos blockPos, int i, int j) {
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (Direction direction : BlockBehaviour.UPDATE_SHAPE_ORDER) {
				mutableBlockPos.setWithOffset(blockPos, direction);
				levelAccessor.neighborShapeChanged(direction.getOpposite(), mutableBlockPos, blockPos, this.asState(), i, j);
			}
		}

		public final void updateIndirectNeighbourShapes(LevelAccessor levelAccessor, BlockPos blockPos, int i) {
			this.updateIndirectNeighbourShapes(levelAccessor, blockPos, i, 512);
		}

		public void updateIndirectNeighbourShapes(LevelAccessor levelAccessor, BlockPos blockPos, int i, int j) {
			this.getBlock().updateIndirectNeighbourShapes(this.asState(), levelAccessor, blockPos, i, j);
		}

		public void onPlace(Level level, BlockPos blockPos, BlockState blockState, boolean bl) {
			this.getBlock().onPlace(this.asState(), level, blockPos, blockState, bl);
		}

		public void onRemove(Level level, BlockPos blockPos, BlockState blockState, boolean bl) {
			this.getBlock().onRemove(this.asState(), level, blockPos, blockState, bl);
		}

		public void onExplosionHit(ServerLevel serverLevel, BlockPos blockPos, Explosion explosion, BiConsumer<ItemStack, BlockPos> biConsumer) {
			this.getBlock().onExplosionHit(this.asState(), serverLevel, blockPos, explosion, biConsumer);
		}

		public void tick(ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
			this.getBlock().tick(this.asState(), serverLevel, blockPos, randomSource);
		}

		public void randomTick(ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
			this.getBlock().randomTick(this.asState(), serverLevel, blockPos, randomSource);
		}

		public void entityInside(Level level, BlockPos blockPos, Entity entity) {
			this.getBlock().entityInside(this.asState(), level, blockPos, entity);
		}

		public VoxelShape getEntityInsideCollisionShape(Level level, BlockPos blockPos) {
			return this.getBlock().getEntityInsideCollisionShape(this.asState(), level, blockPos);
		}

		public void spawnAfterBreak(ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean bl) {
			this.getBlock().spawnAfterBreak(this.asState(), serverLevel, blockPos, itemStack, bl);
		}

		public List<ItemStack> getDrops(LootParams.Builder builder) {
			return this.getBlock().getDrops(this.asState(), builder);
		}

		public InteractionResult useItemOn(ItemStack itemStack, Level level, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
			return this.getBlock().useItemOn(itemStack, this.asState(), level, blockHitResult.getBlockPos(), player, interactionHand, blockHitResult);
		}

		public InteractionResult useWithoutItem(Level level, Player player, BlockHitResult blockHitResult) {
			return this.getBlock().useWithoutItem(this.asState(), level, blockHitResult.getBlockPos(), player, blockHitResult);
		}

		public void attack(Level level, BlockPos blockPos, Player player) {
			this.getBlock().attack(this.asState(), level, blockPos, player);
		}

		public boolean isSuffocating(BlockGetter blockGetter, BlockPos blockPos) {
			return this.isSuffocating.test(this.asState(), blockGetter, blockPos);
		}

		public boolean isViewBlocking(BlockGetter blockGetter, BlockPos blockPos) {
			return this.isViewBlocking.test(this.asState(), blockGetter, blockPos);
		}

		public BlockState updateShape(
			LevelReader levelReader,
			ScheduledTickAccess scheduledTickAccess,
			BlockPos blockPos,
			Direction direction,
			BlockPos blockPos2,
			BlockState blockState,
			RandomSource randomSource
		) {
			return this.getBlock().updateShape(this.asState(), levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState, randomSource);
		}

		public boolean isPathfindable(PathComputationType pathComputationType) {
			return this.getBlock().isPathfindable(this.asState(), pathComputationType);
		}

		public boolean canBeReplaced(BlockPlaceContext blockPlaceContext) {
			return this.getBlock().canBeReplaced(this.asState(), blockPlaceContext);
		}

		public boolean canBeReplaced(Fluid fluid) {
			return this.getBlock().canBeReplaced(this.asState(), fluid);
		}

		public boolean canBeReplaced() {
			return this.replaceable;
		}

		public boolean canSurvive(LevelReader levelReader, BlockPos blockPos) {
			return this.getBlock().canSurvive(this.asState(), levelReader, blockPos);
		}

		public boolean hasPostProcess(BlockGetter blockGetter, BlockPos blockPos) {
			return this.hasPostProcess.test(this.asState(), blockGetter, blockPos);
		}

		@Nullable
		public MenuProvider getMenuProvider(Level level, BlockPos blockPos) {
			return this.getBlock().getMenuProvider(this.asState(), level, blockPos);
		}

		public boolean is(TagKey<Block> tagKey) {
			return this.getBlock().builtInRegistryHolder().is(tagKey);
		}

		public boolean is(TagKey<Block> tagKey, Predicate<BlockBehaviour.BlockStateBase> predicate) {
			return this.is(tagKey) && predicate.test(this);
		}

		public boolean is(HolderSet<Block> holderSet) {
			return holderSet.contains(this.getBlock().builtInRegistryHolder());
		}

		public boolean is(Holder<Block> holder) {
			return this.is(holder.value());
		}

		public Stream<TagKey<Block>> getTags() {
			return this.getBlock().builtInRegistryHolder().tags();
		}

		public boolean hasBlockEntity() {
			return this.getBlock() instanceof EntityBlock;
		}

		@Nullable
		public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockEntityType<T> blockEntityType) {
			return this.getBlock() instanceof EntityBlock ? ((EntityBlock)this.getBlock()).getTicker(level, this.asState(), blockEntityType) : null;
		}

		public boolean is(Block block) {
			return this.getBlock() == block;
		}

		public boolean is(ResourceKey<Block> resourceKey) {
			return this.getBlock().builtInRegistryHolder().is(resourceKey);
		}

		public FluidState getFluidState() {
			return this.fluidState;
		}

		public boolean isRandomlyTicking() {
			return this.isRandomlyTicking;
		}

		public long getSeed(BlockPos blockPos) {
			return this.getBlock().getSeed(this.asState(), blockPos);
		}

		public SoundType getSoundType() {
			return this.getBlock().getSoundType(this.asState());
		}

		public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
			this.getBlock().onProjectileHit(level, blockState, blockHitResult, projectile);
		}

		public boolean isFaceSturdy(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
			return this.isFaceSturdy(blockGetter, blockPos, direction, SupportType.FULL);
		}

		public boolean isFaceSturdy(BlockGetter blockGetter, BlockPos blockPos, Direction direction, SupportType supportType) {
			return this.cache != null ? this.cache.isFaceSturdy(direction, supportType) : supportType.isSupporting(this.asState(), blockGetter, blockPos, direction);
		}

		public boolean isCollisionShapeFullBlock(BlockGetter blockGetter, BlockPos blockPos) {
			return this.cache != null ? this.cache.isCollisionShapeFullBlock : this.getBlock().isCollisionShapeFullBlock(this.asState(), blockGetter, blockPos);
		}

		protected abstract BlockState asState();

		public boolean requiresCorrectToolForDrops() {
			return this.requiresCorrectToolForDrops;
		}

		public boolean shouldSpawnTerrainParticles() {
			return this.spawnTerrainParticles;
		}

		public NoteBlockInstrument instrument() {
			return this.instrument;
		}

		static final class Cache {
			private static final Direction[] DIRECTIONS = Direction.values();
			private static final int SUPPORT_TYPE_COUNT = SupportType.values().length;
			protected final VoxelShape collisionShape;
			protected final boolean largeCollisionShape;
			private final boolean[] faceSturdy;
			protected final boolean isCollisionShapeFullBlock;

			Cache(BlockState blockState) {
				Block block = blockState.getBlock();
				this.collisionShape = block.getCollisionShape(blockState, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
				if (!this.collisionShape.isEmpty() && blockState.hasOffsetFunction()) {
					throw new IllegalStateException(
						String.format(
							Locale.ROOT, "%s has a collision shape and an offset type, but is not marked as dynamicShape in its properties.", BuiltInRegistries.BLOCK.getKey(block)
						)
					);
				} else {
					this.largeCollisionShape = Arrays.stream(Direction.Axis.values())
						.anyMatch(axis -> this.collisionShape.min(axis) < 0.0 || this.collisionShape.max(axis) > 1.0);
					this.faceSturdy = new boolean[DIRECTIONS.length * SUPPORT_TYPE_COUNT];

					for (Direction direction : DIRECTIONS) {
						for (SupportType supportType : SupportType.values()) {
							this.faceSturdy[getFaceSupportIndex(direction, supportType)] = supportType.isSupporting(blockState, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, direction);
						}
					}

					this.isCollisionShapeFullBlock = Block.isShapeFullBlock(blockState.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
				}
			}

			public boolean isFaceSturdy(Direction direction, SupportType supportType) {
				return this.faceSturdy[getFaceSupportIndex(direction, supportType)];
			}

			private static int getFaceSupportIndex(Direction direction, SupportType supportType) {
				return direction.ordinal() * SUPPORT_TYPE_COUNT + supportType.ordinal();
			}
		}
	}

	@FunctionalInterface
	public interface OffsetFunction {
		Vec3 evaluate(BlockState blockState, BlockPos blockPos);
	}

	public static enum OffsetType {
		NONE,
		XZ,
		XYZ;
	}

	public static class Properties {
		public static final Codec<BlockBehaviour.Properties> CODEC = Codec.unit((Supplier<BlockBehaviour.Properties>)(() -> of()));
		Function<BlockState, MapColor> mapColor = blockState -> MapColor.NONE;
		boolean hasCollision = true;
		SoundType soundType = SoundType.STONE;
		ToIntFunction<BlockState> lightEmission = blockState -> 0;
		float explosionResistance;
		float destroyTime;
		boolean requiresCorrectToolForDrops;
		boolean isRandomlyTicking;
		float friction = 0.6F;
		float speedFactor = 1.0F;
		float jumpFactor = 1.0F;
		@Nullable
		private ResourceKey<Block> id;
		private DependantName<Block, Optional<ResourceKey<LootTable>>> drops = resourceKey -> Optional.of(
				ResourceKey.create(Registries.LOOT_TABLE, resourceKey.location().withPrefix("blocks/"))
			);
		private DependantName<Block, String> descriptionId = resourceKey -> Util.makeDescriptionId("block", resourceKey.location());
		boolean canOcclude = true;
		boolean isAir;
		boolean ignitedByLava;
		@Deprecated
		boolean liquid;
		@Deprecated
		boolean forceSolidOff;
		boolean forceSolidOn;
		PushReaction pushReaction = PushReaction.NORMAL;
		boolean spawnTerrainParticles = true;
		NoteBlockInstrument instrument = NoteBlockInstrument.HARP;
		boolean replaceable;
		BlockBehaviour.StateArgumentPredicate<EntityType<?>> isValidSpawn = (blockState, blockGetter, blockPos, entityType) -> blockState.isFaceSturdy(
					blockGetter, blockPos, Direction.UP
				)
				&& blockState.getLightEmission() < 14;
		BlockBehaviour.StatePredicate isRedstoneConductor = (blockState, blockGetter, blockPos) -> blockState.isCollisionShapeFullBlock(blockGetter, blockPos);
		BlockBehaviour.StatePredicate isSuffocating = (blockState, blockGetter, blockPos) -> blockState.blocksMotion()
				&& blockState.isCollisionShapeFullBlock(blockGetter, blockPos);
		BlockBehaviour.StatePredicate isViewBlocking = this.isSuffocating;
		BlockBehaviour.StatePredicate hasPostProcess = (blockState, blockGetter, blockPos) -> false;
		BlockBehaviour.StatePredicate emissiveRendering = (blockState, blockGetter, blockPos) -> false;
		boolean dynamicShape;
		FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;
		@Nullable
		BlockBehaviour.OffsetFunction offsetFunction;

		private Properties() {
		}

		public static BlockBehaviour.Properties of() {
			return new BlockBehaviour.Properties();
		}

		public static BlockBehaviour.Properties ofFullCopy(BlockBehaviour blockBehaviour) {
			BlockBehaviour.Properties properties = ofLegacyCopy(blockBehaviour);
			BlockBehaviour.Properties properties2 = blockBehaviour.properties;
			properties.jumpFactor = properties2.jumpFactor;
			properties.isRedstoneConductor = properties2.isRedstoneConductor;
			properties.isValidSpawn = properties2.isValidSpawn;
			properties.hasPostProcess = properties2.hasPostProcess;
			properties.isSuffocating = properties2.isSuffocating;
			properties.isViewBlocking = properties2.isViewBlocking;
			properties.drops = properties2.drops;
			properties.descriptionId = properties2.descriptionId;
			return properties;
		}

		@Deprecated
		public static BlockBehaviour.Properties ofLegacyCopy(BlockBehaviour blockBehaviour) {
			BlockBehaviour.Properties properties = new BlockBehaviour.Properties();
			BlockBehaviour.Properties properties2 = blockBehaviour.properties;
			properties.destroyTime = properties2.destroyTime;
			properties.explosionResistance = properties2.explosionResistance;
			properties.hasCollision = properties2.hasCollision;
			properties.isRandomlyTicking = properties2.isRandomlyTicking;
			properties.lightEmission = properties2.lightEmission;
			properties.mapColor = properties2.mapColor;
			properties.soundType = properties2.soundType;
			properties.friction = properties2.friction;
			properties.speedFactor = properties2.speedFactor;
			properties.dynamicShape = properties2.dynamicShape;
			properties.canOcclude = properties2.canOcclude;
			properties.isAir = properties2.isAir;
			properties.ignitedByLava = properties2.ignitedByLava;
			properties.liquid = properties2.liquid;
			properties.forceSolidOff = properties2.forceSolidOff;
			properties.forceSolidOn = properties2.forceSolidOn;
			properties.pushReaction = properties2.pushReaction;
			properties.requiresCorrectToolForDrops = properties2.requiresCorrectToolForDrops;
			properties.offsetFunction = properties2.offsetFunction;
			properties.spawnTerrainParticles = properties2.spawnTerrainParticles;
			properties.requiredFeatures = properties2.requiredFeatures;
			properties.emissiveRendering = properties2.emissiveRendering;
			properties.instrument = properties2.instrument;
			properties.replaceable = properties2.replaceable;
			return properties;
		}

		public BlockBehaviour.Properties mapColor(DyeColor dyeColor) {
			this.mapColor = blockState -> dyeColor.getMapColor();
			return this;
		}

		public BlockBehaviour.Properties mapColor(MapColor mapColor) {
			this.mapColor = blockState -> mapColor;
			return this;
		}

		public BlockBehaviour.Properties mapColor(Function<BlockState, MapColor> function) {
			this.mapColor = function;
			return this;
		}

		public BlockBehaviour.Properties noCollission() {
			this.hasCollision = false;
			this.canOcclude = false;
			return this;
		}

		public BlockBehaviour.Properties noOcclusion() {
			this.canOcclude = false;
			return this;
		}

		public BlockBehaviour.Properties friction(float f) {
			this.friction = f;
			return this;
		}

		public BlockBehaviour.Properties speedFactor(float f) {
			this.speedFactor = f;
			return this;
		}

		public BlockBehaviour.Properties jumpFactor(float f) {
			this.jumpFactor = f;
			return this;
		}

		public BlockBehaviour.Properties sound(SoundType soundType) {
			this.soundType = soundType;
			return this;
		}

		public BlockBehaviour.Properties lightLevel(ToIntFunction<BlockState> toIntFunction) {
			this.lightEmission = toIntFunction;
			return this;
		}

		public BlockBehaviour.Properties strength(float f, float g) {
			return this.destroyTime(f).explosionResistance(g);
		}

		public BlockBehaviour.Properties instabreak() {
			return this.strength(0.0F);
		}

		public BlockBehaviour.Properties strength(float f) {
			this.strength(f, f);
			return this;
		}

		public BlockBehaviour.Properties randomTicks() {
			this.isRandomlyTicking = true;
			return this;
		}

		public BlockBehaviour.Properties dynamicShape() {
			this.dynamicShape = true;
			return this;
		}

		public BlockBehaviour.Properties noLootTable() {
			this.drops = DependantName.fixed(Optional.empty());
			return this;
		}

		public BlockBehaviour.Properties overrideLootTable(Optional<ResourceKey<LootTable>> optional) {
			this.drops = DependantName.fixed(optional);
			return this;
		}

		protected Optional<ResourceKey<LootTable>> effectiveDrops() {
			return this.drops.get((ResourceKey<Block>)Objects.requireNonNull(this.id, "Block id not set"));
		}

		public BlockBehaviour.Properties ignitedByLava() {
			this.ignitedByLava = true;
			return this;
		}

		public BlockBehaviour.Properties liquid() {
			this.liquid = true;
			return this;
		}

		public BlockBehaviour.Properties forceSolidOn() {
			this.forceSolidOn = true;
			return this;
		}

		@Deprecated
		public BlockBehaviour.Properties forceSolidOff() {
			this.forceSolidOff = true;
			return this;
		}

		public BlockBehaviour.Properties pushReaction(PushReaction pushReaction) {
			this.pushReaction = pushReaction;
			return this;
		}

		public BlockBehaviour.Properties air() {
			this.isAir = true;
			return this;
		}

		public BlockBehaviour.Properties isValidSpawn(BlockBehaviour.StateArgumentPredicate<EntityType<?>> stateArgumentPredicate) {
			this.isValidSpawn = stateArgumentPredicate;
			return this;
		}

		public BlockBehaviour.Properties isRedstoneConductor(BlockBehaviour.StatePredicate statePredicate) {
			this.isRedstoneConductor = statePredicate;
			return this;
		}

		public BlockBehaviour.Properties isSuffocating(BlockBehaviour.StatePredicate statePredicate) {
			this.isSuffocating = statePredicate;
			return this;
		}

		public BlockBehaviour.Properties isViewBlocking(BlockBehaviour.StatePredicate statePredicate) {
			this.isViewBlocking = statePredicate;
			return this;
		}

		public BlockBehaviour.Properties hasPostProcess(BlockBehaviour.StatePredicate statePredicate) {
			this.hasPostProcess = statePredicate;
			return this;
		}

		public BlockBehaviour.Properties emissiveRendering(BlockBehaviour.StatePredicate statePredicate) {
			this.emissiveRendering = statePredicate;
			return this;
		}

		public BlockBehaviour.Properties requiresCorrectToolForDrops() {
			this.requiresCorrectToolForDrops = true;
			return this;
		}

		public BlockBehaviour.Properties destroyTime(float f) {
			this.destroyTime = f;
			return this;
		}

		public BlockBehaviour.Properties explosionResistance(float f) {
			this.explosionResistance = Math.max(0.0F, f);
			return this;
		}

		public BlockBehaviour.Properties offsetType(BlockBehaviour.OffsetType offsetType) {
			this.offsetFunction = switch (offsetType) {
				case NONE -> null;
				case XZ -> (blockState, blockPos) -> {
				Block block = blockState.getBlock();
				long l = Mth.getSeed(blockPos.getX(), 0, blockPos.getZ());
				float f = block.getMaxHorizontalOffset();
				double d = Mth.clamp(((double)((float)(l & 15L) / 15.0F) - 0.5) * 0.5, (double)(-f), (double)f);
				double e = Mth.clamp(((double)((float)(l >> 8 & 15L) / 15.0F) - 0.5) * 0.5, (double)(-f), (double)f);
				return new Vec3(d, 0.0, e);
			};
				case XYZ -> (blockState, blockPos) -> {
				Block block = blockState.getBlock();
				long l = Mth.getSeed(blockPos.getX(), 0, blockPos.getZ());
				double d = ((double)((float)(l >> 4 & 15L) / 15.0F) - 1.0) * (double)block.getMaxVerticalOffset();
				float f = block.getMaxHorizontalOffset();
				double e = Mth.clamp(((double)((float)(l & 15L) / 15.0F) - 0.5) * 0.5, (double)(-f), (double)f);
				double g = Mth.clamp(((double)((float)(l >> 8 & 15L) / 15.0F) - 0.5) * 0.5, (double)(-f), (double)f);
				return new Vec3(e, d, g);
			};
			};
			return this;
		}

		public BlockBehaviour.Properties noTerrainParticles() {
			this.spawnTerrainParticles = false;
			return this;
		}

		public BlockBehaviour.Properties requiredFeatures(FeatureFlag... featureFlags) {
			this.requiredFeatures = FeatureFlags.REGISTRY.subset(featureFlags);
			return this;
		}

		public BlockBehaviour.Properties instrument(NoteBlockInstrument noteBlockInstrument) {
			this.instrument = noteBlockInstrument;
			return this;
		}

		public BlockBehaviour.Properties replaceable() {
			this.replaceable = true;
			return this;
		}

		public BlockBehaviour.Properties setId(ResourceKey<Block> resourceKey) {
			this.id = resourceKey;
			return this;
		}

		public BlockBehaviour.Properties overrideDescription(String string) {
			this.descriptionId = DependantName.fixed(string);
			return this;
		}

		protected String effectiveDescriptionId() {
			return this.descriptionId.get((ResourceKey<Block>)Objects.requireNonNull(this.id, "Block id not set"));
		}
	}

	@FunctionalInterface
	public interface StateArgumentPredicate<A> {
		boolean test(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, A object);
	}

	@FunctionalInterface
	public interface StatePredicate {
		boolean test(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos);
	}
}
