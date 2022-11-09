package net.minecraft.world.level.block.state;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BlockBehaviour implements FeatureElement {
	protected static final Direction[] UPDATE_SHAPE_ORDER = new Direction[]{
		Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.DOWN, Direction.UP
	};
	protected final Material material;
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
	@Nullable
	protected ResourceLocation drops;

	public BlockBehaviour(BlockBehaviour.Properties properties) {
		this.material = properties.material;
		this.hasCollision = properties.hasCollision;
		this.drops = properties.drops;
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

	@Deprecated
	public void updateIndirectNeighbourShapes(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, int i, int j) {
	}

	@Deprecated
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		switch (pathComputationType) {
			case LAND:
				return !blockState.isCollisionShapeFullBlock(blockGetter, blockPos);
			case WATER:
				return blockGetter.getFluidState(blockPos).is(FluidTags.WATER);
			case AIR:
				return !blockState.isCollisionShapeFullBlock(blockGetter, blockPos);
			default:
				return false;
		}
	}

	@Deprecated
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		return blockState;
	}

	@Deprecated
	public boolean skipRendering(BlockState blockState, BlockState blockState2, Direction direction) {
		return false;
	}

	@Deprecated
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		DebugPackets.sendNeighborsUpdatePacket(level, blockPos);
	}

	@Deprecated
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
	}

	@Deprecated
	public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (blockState.hasBlockEntity() && !blockState.is(blockState2.getBlock())) {
			level.removeBlockEntity(blockPos);
		}
	}

	@Deprecated
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		return InteractionResult.PASS;
	}

	@Deprecated
	public boolean triggerEvent(BlockState blockState, Level level, BlockPos blockPos, int i, int j) {
		return false;
	}

	@Deprecated
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Deprecated
	public boolean useShapeForLightOcclusion(BlockState blockState) {
		return false;
	}

	@Deprecated
	public boolean isSignalSource(BlockState blockState) {
		return false;
	}

	@Deprecated
	public PushReaction getPistonPushReaction(BlockState blockState) {
		return this.material.getPushReaction();
	}

	@Deprecated
	public FluidState getFluidState(BlockState blockState) {
		return Fluids.EMPTY.defaultFluidState();
	}

	@Deprecated
	public boolean hasAnalogOutputSignal(BlockState blockState) {
		return false;
	}

	public float getMaxHorizontalOffset() {
		return 0.25F;
	}

	public float getMaxVerticalOffset() {
		return 0.2F;
	}

	@Override
	public FeatureFlagSet requiredFeatures() {
		return this.requiredFeatures;
	}

	@Deprecated
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState;
	}

	@Deprecated
	public BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState;
	}

	@Deprecated
	public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
		return this.material.isReplaceable() && (blockPlaceContext.getItemInHand().isEmpty() || !blockPlaceContext.getItemInHand().is(this.asItem()));
	}

	@Deprecated
	public boolean canBeReplaced(BlockState blockState, Fluid fluid) {
		return this.material.isReplaceable() || !this.material.isSolid();
	}

	@Deprecated
	public List<ItemStack> getDrops(BlockState blockState, LootContext.Builder builder) {
		ResourceLocation resourceLocation = this.getLootTable();
		if (resourceLocation == BuiltInLootTables.EMPTY) {
			return Collections.emptyList();
		} else {
			LootContext lootContext = builder.withParameter(LootContextParams.BLOCK_STATE, blockState).create(LootContextParamSets.BLOCK);
			ServerLevel serverLevel = lootContext.getLevel();
			LootTable lootTable = serverLevel.getServer().getLootTables().get(resourceLocation);
			return lootTable.getRandomItems(lootContext);
		}
	}

	@Deprecated
	public long getSeed(BlockState blockState, BlockPos blockPos) {
		return Mth.getSeed(blockPos);
	}

	@Deprecated
	public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.getShape(blockGetter, blockPos);
	}

	@Deprecated
	public VoxelShape getBlockSupportShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return this.getCollisionShape(blockState, blockGetter, blockPos, CollisionContext.empty());
	}

	@Deprecated
	public VoxelShape getInteractionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return Shapes.empty();
	}

	@Deprecated
	public int getLightBlock(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		if (blockState.isSolidRender(blockGetter, blockPos)) {
			return blockGetter.getMaxLightLevel();
		} else {
			return blockState.propagatesSkylightDown(blockGetter, blockPos) ? 0 : 1;
		}
	}

	@Nullable
	@Deprecated
	public MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
		return null;
	}

	@Deprecated
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return true;
	}

	@Deprecated
	public float getShadeBrightness(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.isCollisionShapeFullBlock(blockGetter, blockPos) ? 0.2F : 1.0F;
	}

	@Deprecated
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return 0;
	}

	@Deprecated
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return Shapes.block();
	}

	@Deprecated
	public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return this.hasCollision ? blockState.getShape(blockGetter, blockPos) : Shapes.empty();
	}

	@Deprecated
	public boolean isCollisionShapeFullBlock(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return Block.isShapeFullBlock(blockState.getCollisionShape(blockGetter, blockPos));
	}

	@Deprecated
	public boolean isOcclusionShapeFullBlock(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return Block.isShapeFullBlock(blockState.getOcclusionShape(blockGetter, blockPos));
	}

	@Deprecated
	public VoxelShape getVisualShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return this.getCollisionShape(blockState, blockGetter, blockPos, collisionContext);
	}

	@Deprecated
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		this.tick(blockState, serverLevel, blockPos, randomSource);
	}

	@Deprecated
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
	}

	@Deprecated
	public float getDestroyProgress(BlockState blockState, Player player, BlockGetter blockGetter, BlockPos blockPos) {
		float f = blockState.getDestroySpeed(blockGetter, blockPos);
		if (f == -1.0F) {
			return 0.0F;
		} else {
			int i = player.hasCorrectToolForDrops(blockState) ? 30 : 100;
			return player.getDestroySpeed(blockState) / f / (float)i;
		}
	}

	@Deprecated
	public void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean bl) {
	}

	@Deprecated
	public void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
	}

	@Deprecated
	public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return 0;
	}

	@Deprecated
	public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
	}

	@Deprecated
	public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return 0;
	}

	public final ResourceLocation getLootTable() {
		if (this.drops == null) {
			ResourceLocation resourceLocation = BuiltInRegistries.BLOCK.getKey(this.asBlock());
			this.drops = resourceLocation.withPrefix("blocks/");
		}

		return this.drops;
	}

	@Deprecated
	public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
	}

	public abstract Item asItem();

	protected abstract Block asBlock();

	public MaterialColor defaultMaterialColor() {
		return (MaterialColor)this.properties.materialColor.apply(this.asBlock().defaultBlockState());
	}

	public float defaultDestroyTime() {
		return this.properties.destroyTime;
	}

	public abstract static class BlockStateBase extends StateHolder<Block, BlockState> {
		private final int lightEmission;
		private final boolean useShapeForLightOcclusion;
		private final boolean isAir;
		private final Material material;
		private final MaterialColor materialColor;
		private final float destroySpeed;
		private final boolean requiresCorrectToolForDrops;
		private final boolean canOcclude;
		private final BlockBehaviour.StatePredicate isRedstoneConductor;
		private final BlockBehaviour.StatePredicate isSuffocating;
		private final BlockBehaviour.StatePredicate isViewBlocking;
		private final BlockBehaviour.StatePredicate hasPostProcess;
		private final BlockBehaviour.StatePredicate emissiveRendering;
		private final BlockBehaviour.OffsetType offsetType;
		private final boolean spawnParticlesOnBreak;
		@Nullable
		protected BlockBehaviour.BlockStateBase.Cache cache;
		private FluidState fluidState = Fluids.EMPTY.defaultFluidState();
		private boolean isRandomlyTicking;

		protected BlockStateBase(Block block, ImmutableMap<Property<?>, Comparable<?>> immutableMap, MapCodec<BlockState> mapCodec) {
			super(block, immutableMap, mapCodec);
			BlockBehaviour.Properties properties = block.properties;
			this.lightEmission = properties.lightEmission.applyAsInt(this.asState());
			this.useShapeForLightOcclusion = block.useShapeForLightOcclusion(this.asState());
			this.isAir = properties.isAir;
			this.material = properties.material;
			this.materialColor = (MaterialColor)properties.materialColor.apply(this.asState());
			this.destroySpeed = properties.destroyTime;
			this.requiresCorrectToolForDrops = properties.requiresCorrectToolForDrops;
			this.canOcclude = properties.canOcclude;
			this.isRedstoneConductor = properties.isRedstoneConductor;
			this.isSuffocating = properties.isSuffocating;
			this.isViewBlocking = properties.isViewBlocking;
			this.hasPostProcess = properties.hasPostProcess;
			this.emissiveRendering = properties.emissiveRendering;
			this.offsetType = (BlockBehaviour.OffsetType)properties.offsetType.apply(this.asState());
			this.spawnParticlesOnBreak = properties.spawnParticlesOnBreak;
		}

		public void initCache() {
			if (!this.getBlock().hasDynamicShape()) {
				this.cache = new BlockBehaviour.BlockStateBase.Cache(this.asState());
			}

			this.fluidState = this.owner.getFluidState(this.asState());
			this.isRandomlyTicking = this.owner.isRandomlyTicking(this.asState());
		}

		public Block getBlock() {
			return this.owner;
		}

		public Holder<Block> getBlockHolder() {
			return this.owner.builtInRegistryHolder();
		}

		public Material getMaterial() {
			return this.material;
		}

		public boolean isValidSpawn(BlockGetter blockGetter, BlockPos blockPos, EntityType<?> entityType) {
			return this.getBlock().properties.isValidSpawn.test(this.asState(), blockGetter, blockPos, entityType);
		}

		public boolean propagatesSkylightDown(BlockGetter blockGetter, BlockPos blockPos) {
			return this.cache != null ? this.cache.propagatesSkylightDown : this.getBlock().propagatesSkylightDown(this.asState(), blockGetter, blockPos);
		}

		public int getLightBlock(BlockGetter blockGetter, BlockPos blockPos) {
			return this.cache != null ? this.cache.lightBlock : this.getBlock().getLightBlock(this.asState(), blockGetter, blockPos);
		}

		public VoxelShape getFaceOcclusionShape(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
			return this.cache != null && this.cache.occlusionShapes != null
				? this.cache.occlusionShapes[direction.ordinal()]
				: Shapes.getFaceShape(this.getOcclusionShape(blockGetter, blockPos), direction);
		}

		public VoxelShape getOcclusionShape(BlockGetter blockGetter, BlockPos blockPos) {
			return this.getBlock().getOcclusionShape(this.asState(), blockGetter, blockPos);
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

		public MaterialColor getMapColor(BlockGetter blockGetter, BlockPos blockPos) {
			return this.materialColor;
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
			return this.getBlock().getPistonPushReaction(this.asState());
		}

		public boolean isSolidRender(BlockGetter blockGetter, BlockPos blockPos) {
			if (this.cache != null) {
				return this.cache.solidRender;
			} else {
				BlockState blockState = this.asState();
				return blockState.canOcclude() ? Block.isShapeFullBlock(blockState.getOcclusionShape(blockGetter, blockPos)) : false;
			}
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

		public Vec3 getOffset(BlockGetter blockGetter, BlockPos blockPos) {
			if (this.offsetType == BlockBehaviour.OffsetType.NONE) {
				return Vec3.ZERO;
			} else {
				Block block = this.getBlock();
				long l = Mth.getSeed(blockPos.getX(), 0, blockPos.getZ());
				float f = block.getMaxHorizontalOffset();
				double d = Mth.clamp(((double)((float)(l & 15L) / 15.0F) - 0.5) * 0.5, (double)(-f), (double)f);
				double e = this.offsetType == BlockBehaviour.OffsetType.XYZ ? ((double)((float)(l >> 4 & 15L) / 15.0F) - 1.0) * (double)block.getMaxVerticalOffset() : 0.0;
				double g = Mth.clamp(((double)((float)(l >> 8 & 15L) / 15.0F) - 0.5) * 0.5, (double)(-f), (double)f);
				return new Vec3(d, e, g);
			}
		}

		public boolean triggerEvent(Level level, BlockPos blockPos, int i, int j) {
			return this.getBlock().triggerEvent(this.asState(), level, blockPos, i, j);
		}

		@Deprecated
		public void neighborChanged(Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
			this.getBlock().neighborChanged(this.asState(), level, blockPos, block, blockPos2, bl);
		}

		public final void updateNeighbourShapes(LevelAccessor levelAccessor, BlockPos blockPos, int i) {
			this.updateNeighbourShapes(levelAccessor, blockPos, i, 512);
		}

		public final void updateNeighbourShapes(LevelAccessor levelAccessor, BlockPos blockPos, int i, int j) {
			this.getBlock();
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (Direction direction : BlockBehaviour.UPDATE_SHAPE_ORDER) {
				mutableBlockPos.setWithOffset(blockPos, direction);
				levelAccessor.neighborShapeChanged(direction.getOpposite(), this.asState(), mutableBlockPos, blockPos, i, j);
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

		public void tick(ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
			this.getBlock().tick(this.asState(), serverLevel, blockPos, randomSource);
		}

		public void randomTick(ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
			this.getBlock().randomTick(this.asState(), serverLevel, blockPos, randomSource);
		}

		public void entityInside(Level level, BlockPos blockPos, Entity entity) {
			this.getBlock().entityInside(this.asState(), level, blockPos, entity);
		}

		public void spawnAfterBreak(ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean bl) {
			this.getBlock().spawnAfterBreak(this.asState(), serverLevel, blockPos, itemStack, bl);
		}

		public List<ItemStack> getDrops(LootContext.Builder builder) {
			return this.getBlock().getDrops(this.asState(), builder);
		}

		public InteractionResult use(Level level, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
			return this.getBlock().use(this.asState(), level, blockHitResult.getBlockPos(), player, interactionHand, blockHitResult);
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

		public BlockState updateShape(Direction direction, BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
			return this.getBlock().updateShape(this.asState(), direction, blockState, levelAccessor, blockPos, blockPos2);
		}

		public boolean isPathfindable(BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
			return this.getBlock().isPathfindable(this.asState(), blockGetter, blockPos, pathComputationType);
		}

		public boolean canBeReplaced(BlockPlaceContext blockPlaceContext) {
			return this.getBlock().canBeReplaced(this.asState(), blockPlaceContext);
		}

		public boolean canBeReplaced(Fluid fluid) {
			return this.getBlock().canBeReplaced(this.asState(), fluid);
		}

		public boolean canBeReplaced() {
			return this.getMaterial().isReplaceable();
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

		public BlockBehaviour.OffsetType getOffsetType() {
			return this.offsetType;
		}

		public boolean shouldSpawnParticlesOnBreak() {
			return this.spawnParticlesOnBreak;
		}

		static final class Cache {
			private static final Direction[] DIRECTIONS = Direction.values();
			private static final int SUPPORT_TYPE_COUNT = SupportType.values().length;
			protected final boolean solidRender;
			final boolean propagatesSkylightDown;
			final int lightBlock;
			@Nullable
			final VoxelShape[] occlusionShapes;
			protected final VoxelShape collisionShape;
			protected final boolean largeCollisionShape;
			private final boolean[] faceSturdy;
			protected final boolean isCollisionShapeFullBlock;

			Cache(BlockState blockState) {
				Block block = blockState.getBlock();
				this.solidRender = blockState.isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
				this.propagatesSkylightDown = block.propagatesSkylightDown(blockState, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
				this.lightBlock = block.getLightBlock(blockState, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
				if (!blockState.canOcclude()) {
					this.occlusionShapes = null;
				} else {
					this.occlusionShapes = new VoxelShape[DIRECTIONS.length];
					VoxelShape voxelShape = block.getOcclusionShape(blockState, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);

					for (Direction direction : DIRECTIONS) {
						this.occlusionShapes[direction.ordinal()] = Shapes.getFaceShape(voxelShape, direction);
					}
				}

				this.collisionShape = block.getCollisionShape(blockState, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
				if (!this.collisionShape.isEmpty() && blockState.getOffsetType() != BlockBehaviour.OffsetType.NONE) {
					throw new IllegalStateException(
						String.format(
							Locale.ROOT, "%s has a collision shape and an offset type, but is not marked as dynamicShape in its properties.", BuiltInRegistries.BLOCK.getKey(block)
						)
					);
				} else {
					this.largeCollisionShape = Arrays.stream(Direction.Axis.values())
						.anyMatch(axis -> this.collisionShape.min(axis) < 0.0 || this.collisionShape.max(axis) > 1.0);
					this.faceSturdy = new boolean[DIRECTIONS.length * SUPPORT_TYPE_COUNT];

					for (Direction direction2 : DIRECTIONS) {
						for (SupportType supportType : SupportType.values()) {
							this.faceSturdy[getFaceSupportIndex(direction2, supportType)] = supportType.isSupporting(
								blockState, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, direction2
							);
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

	public static enum OffsetType {
		NONE,
		XZ,
		XYZ;
	}

	public static class Properties {
		Material material;
		Function<BlockState, MaterialColor> materialColor;
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
		ResourceLocation drops;
		boolean canOcclude = true;
		boolean isAir;
		boolean spawnParticlesOnBreak = true;
		BlockBehaviour.StateArgumentPredicate<EntityType<?>> isValidSpawn = (blockState, blockGetter, blockPos, entityType) -> blockState.isFaceSturdy(
					blockGetter, blockPos, Direction.UP
				)
				&& blockState.getLightEmission() < 14;
		BlockBehaviour.StatePredicate isRedstoneConductor = (blockState, blockGetter, blockPos) -> blockState.getMaterial().isSolidBlocking()
				&& blockState.isCollisionShapeFullBlock(blockGetter, blockPos);
		BlockBehaviour.StatePredicate isSuffocating = (blockState, blockGetter, blockPos) -> this.material.blocksMotion()
				&& blockState.isCollisionShapeFullBlock(blockGetter, blockPos);
		BlockBehaviour.StatePredicate isViewBlocking = this.isSuffocating;
		BlockBehaviour.StatePredicate hasPostProcess = (blockState, blockGetter, blockPos) -> false;
		BlockBehaviour.StatePredicate emissiveRendering = (blockState, blockGetter, blockPos) -> false;
		boolean dynamicShape;
		FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;
		Function<BlockState, BlockBehaviour.OffsetType> offsetType = blockState -> BlockBehaviour.OffsetType.NONE;

		private Properties(Material material, MaterialColor materialColor) {
			this(material, blockState -> materialColor);
		}

		private Properties(Material material, Function<BlockState, MaterialColor> function) {
			this.material = material;
			this.materialColor = function;
		}

		public static BlockBehaviour.Properties of(Material material) {
			return of(material, material.getColor());
		}

		public static BlockBehaviour.Properties of(Material material, DyeColor dyeColor) {
			return of(material, dyeColor.getMaterialColor());
		}

		public static BlockBehaviour.Properties of(Material material, MaterialColor materialColor) {
			return new BlockBehaviour.Properties(material, materialColor);
		}

		public static BlockBehaviour.Properties of(Material material, Function<BlockState, MaterialColor> function) {
			return new BlockBehaviour.Properties(material, function);
		}

		public static BlockBehaviour.Properties copy(BlockBehaviour blockBehaviour) {
			BlockBehaviour.Properties properties = new BlockBehaviour.Properties(blockBehaviour.material, blockBehaviour.properties.materialColor);
			properties.material = blockBehaviour.properties.material;
			properties.destroyTime = blockBehaviour.properties.destroyTime;
			properties.explosionResistance = blockBehaviour.properties.explosionResistance;
			properties.hasCollision = blockBehaviour.properties.hasCollision;
			properties.isRandomlyTicking = blockBehaviour.properties.isRandomlyTicking;
			properties.lightEmission = blockBehaviour.properties.lightEmission;
			properties.materialColor = blockBehaviour.properties.materialColor;
			properties.soundType = blockBehaviour.properties.soundType;
			properties.friction = blockBehaviour.properties.friction;
			properties.speedFactor = blockBehaviour.properties.speedFactor;
			properties.dynamicShape = blockBehaviour.properties.dynamicShape;
			properties.canOcclude = blockBehaviour.properties.canOcclude;
			properties.isAir = blockBehaviour.properties.isAir;
			properties.requiresCorrectToolForDrops = blockBehaviour.properties.requiresCorrectToolForDrops;
			properties.offsetType = blockBehaviour.properties.offsetType;
			properties.spawnParticlesOnBreak = blockBehaviour.properties.spawnParticlesOnBreak;
			properties.requiredFeatures = blockBehaviour.properties.requiredFeatures;
			return properties;
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
			this.drops = BuiltInLootTables.EMPTY;
			return this;
		}

		public BlockBehaviour.Properties dropsLike(Block block) {
			this.drops = block.getLootTable();
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

		public BlockBehaviour.Properties color(MaterialColor materialColor) {
			this.materialColor = blockState -> materialColor;
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
			return this.offsetType(blockState -> offsetType);
		}

		public BlockBehaviour.Properties offsetType(Function<BlockState, BlockBehaviour.OffsetType> function) {
			this.offsetType = function;
			return this;
		}

		public BlockBehaviour.Properties noParticlesOnBreak() {
			this.spawnParticlesOnBreak = false;
			return this;
		}

		public BlockBehaviour.Properties requiredFeatures(FeatureFlag... featureFlags) {
			this.requiredFeatures = FeatureFlags.REGISTRY.subset(featureFlags);
			return this;
		}
	}

	public interface StateArgumentPredicate<A> {
		boolean test(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, A object);
	}

	public interface StatePredicate {
		boolean test(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos);
	}
}
