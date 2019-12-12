package net.minecraft.world.level.block;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.IdMapper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
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
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Block implements ItemLike {
	protected static final Logger LOGGER = LogManager.getLogger();
	public static final IdMapper<BlockState> BLOCK_STATE_REGISTRY = new IdMapper<>();
	private static final Direction[] UPDATE_SHAPE_ORDER = new Direction[]{
		Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.DOWN, Direction.UP
	};
	private static final LoadingCache<VoxelShape, Boolean> SHAPE_FULL_BLOCK_CACHE = CacheBuilder.newBuilder()
		.maximumSize(512L)
		.weakKeys()
		.build(new CacheLoader<VoxelShape, Boolean>() {
			public Boolean load(VoxelShape voxelShape) {
				return !Shapes.joinIsNotEmpty(Shapes.block(), voxelShape, BooleanOp.NOT_SAME);
			}
		});
	private static final VoxelShape RIGID_SUPPORT_SHAPE = Shapes.join(Shapes.block(), box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0), BooleanOp.ONLY_FIRST);
	private static final VoxelShape CENTER_SUPPORT_SHAPE = box(7.0, 0.0, 7.0, 9.0, 10.0, 9.0);
	protected final int lightEmission;
	protected final float destroySpeed;
	protected final float explosionResistance;
	protected final boolean isTicking;
	protected final SoundType soundType;
	protected final Material material;
	protected final MaterialColor materialColor;
	private final float friction;
	private final float speedFactor;
	private final float jumpFactor;
	protected final StateDefinition<Block, BlockState> stateDefinition;
	private BlockState defaultBlockState;
	protected final boolean hasCollision;
	private final boolean dynamicShape;
	private final boolean canOcclude;
	@Nullable
	private ResourceLocation drops;
	@Nullable
	private String descriptionId;
	@Nullable
	private Item item;
	private static final ThreadLocal<Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>> OCCLUSION_CACHE = ThreadLocal.withInitial(() -> {
		Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2ByteLinkedOpenHashMap = new Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>(2048, 0.25F) {
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
		} else {
			int i = BLOCK_STATE_REGISTRY.getId(blockState);
			return i == -1 ? 0 : i;
		}
	}

	public static BlockState stateById(int i) {
		BlockState blockState = BLOCK_STATE_REGISTRY.byId(i);
		return blockState == null ? Blocks.AIR.defaultBlockState() : blockState;
	}

	public static Block byItem(@Nullable Item item) {
		return item instanceof BlockItem ? ((BlockItem)item).getBlock() : Blocks.AIR;
	}

	public static BlockState pushEntitiesUp(BlockState blockState, BlockState blockState2, Level level, BlockPos blockPos) {
		VoxelShape voxelShape = Shapes.joinUnoptimized(
				blockState.getCollisionShape(level, blockPos), blockState2.getCollisionShape(level, blockPos), BooleanOp.ONLY_SECOND
			)
			.move((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());

		for (Entity entity : level.getEntities(null, voxelShape.bounds())) {
			double d = Shapes.collide(Direction.Axis.Y, entity.getBoundingBox().move(0.0, 1.0, 0.0), Stream.of(voxelShape), -1.0);
			entity.teleportTo(entity.getX(), entity.getY() + 1.0 + d, entity.getZ());
		}

		return blockState2;
	}

	public static VoxelShape box(double d, double e, double f, double g, double h, double i) {
		return Shapes.box(d / 16.0, e / 16.0, f / 16.0, g / 16.0, h / 16.0, i / 16.0);
	}

	@Deprecated
	public boolean isValidSpawn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, EntityType<?> entityType) {
		return blockState.isFaceSturdy(blockGetter, blockPos, Direction.UP) && this.lightEmission < 14;
	}

	@Deprecated
	public boolean isAir(BlockState blockState) {
		return false;
	}

	@Deprecated
	public int getLightEmission(BlockState blockState) {
		return this.lightEmission;
	}

	@Deprecated
	public Material getMaterial(BlockState blockState) {
		return this.material;
	}

	@Deprecated
	public MaterialColor getMapColor(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return this.materialColor;
	}

	@Deprecated
	public void updateNeighbourShapes(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, int i) {
		try (BlockPos.PooledMutableBlockPos pooledMutableBlockPos = BlockPos.PooledMutableBlockPos.acquire()) {
			for (Direction direction : UPDATE_SHAPE_ORDER) {
				pooledMutableBlockPos.set(blockPos).move(direction);
				BlockState blockState2 = levelAccessor.getBlockState(pooledMutableBlockPos);
				BlockState blockState3 = blockState2.updateShape(direction.getOpposite(), blockState, levelAccessor, pooledMutableBlockPos, blockPos);
				updateOrDestroy(blockState2, blockState3, levelAccessor, pooledMutableBlockPos, i);
			}
		}
	}

	public boolean is(Tag<Block> tag) {
		return tag.contains(this);
	}

	public static BlockState updateFromNeighbourShapes(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
		BlockState blockState2 = blockState;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (Direction direction : UPDATE_SHAPE_ORDER) {
			mutableBlockPos.set(blockPos).move(direction);
			blockState2 = blockState2.updateShape(direction, levelAccessor.getBlockState(mutableBlockPos), levelAccessor, blockPos, mutableBlockPos);
		}

		return blockState2;
	}

	public static void updateOrDestroy(BlockState blockState, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, int i) {
		if (blockState2 != blockState) {
			if (blockState2.isAir()) {
				if (!levelAccessor.isClientSide()) {
					levelAccessor.destroyBlock(blockPos, (i & 32) == 0);
				}
			} else {
				levelAccessor.setBlock(blockPos, blockState2, i & -33);
			}
		}
	}

	@Deprecated
	public void updateIndirectNeighbourShapes(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, int i) {
	}

	@Deprecated
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		return blockState;
	}

	@Deprecated
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState;
	}

	@Deprecated
	public BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState;
	}

	public Block(Block.Properties properties) {
		StateDefinition.Builder<Block, BlockState> builder = new StateDefinition.Builder<>(this);
		this.createBlockStateDefinition(builder);
		this.material = properties.material;
		this.materialColor = properties.materialColor;
		this.hasCollision = properties.hasCollision;
		this.soundType = properties.soundType;
		this.lightEmission = properties.lightEmission;
		this.explosionResistance = properties.explosionResistance;
		this.destroySpeed = properties.destroyTime;
		this.isTicking = properties.isTicking;
		this.friction = properties.friction;
		this.speedFactor = properties.speedFactor;
		this.jumpFactor = properties.jumpFactor;
		this.dynamicShape = properties.dynamicShape;
		this.drops = properties.drops;
		this.canOcclude = properties.canOcclude;
		this.stateDefinition = builder.create(BlockState::new);
		this.registerDefaultState(this.stateDefinition.any());
	}

	public static boolean isExceptionForConnection(Block block) {
		return block instanceof LeavesBlock
			|| block == Blocks.BARRIER
			|| block == Blocks.CARVED_PUMPKIN
			|| block == Blocks.JACK_O_LANTERN
			|| block == Blocks.MELON
			|| block == Blocks.PUMPKIN
			|| block.is(BlockTags.SHULKER_BOXES);
	}

	@Deprecated
	public boolean isRedstoneConductor(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.getMaterial().isSolidBlocking() && blockState.isCollisionShapeFullBlock(blockGetter, blockPos) && !blockState.isSignalSource();
	}

	@Deprecated
	public boolean isSuffocating(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return this.material.blocksMotion() && blockState.isCollisionShapeFullBlock(blockGetter, blockPos);
	}

	@Deprecated
	@Environment(EnvType.CLIENT)
	public boolean isViewBlocking(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.isSuffocating(blockGetter, blockPos);
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
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Deprecated
	public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
		return this.material.isReplaceable() && (blockPlaceContext.getItemInHand().isEmpty() || blockPlaceContext.getItemInHand().getItem() != this.asItem());
	}

	@Deprecated
	public boolean canBeReplaced(BlockState blockState, Fluid fluid) {
		return this.material.isReplaceable() || !this.material.isSolid();
	}

	@Deprecated
	public float getDestroySpeed(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return this.destroySpeed;
	}

	public boolean isRandomlyTicking(BlockState blockState) {
		return this.isTicking;
	}

	public boolean isEntityBlock() {
		return this instanceof EntityBlock;
	}

	@Deprecated
	public boolean hasPostProcess(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return false;
	}

	@Deprecated
	@Environment(EnvType.CLIENT)
	public boolean emissiveRendering(BlockState blockState) {
		return false;
	}

	@Environment(EnvType.CLIENT)
	public static boolean shouldRenderFace(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		BlockPos blockPos2 = blockPos.relative(direction);
		BlockState blockState2 = blockGetter.getBlockState(blockPos2);
		if (blockState.skipRendering(blockState2, direction)) {
			return false;
		} else if (blockState2.canOcclude()) {
			Block.BlockStatePairKey blockStatePairKey = new Block.BlockStatePairKey(blockState, blockState2, direction);
			Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2ByteLinkedOpenHashMap = (Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>)OCCLUSION_CACHE.get();
			byte b = object2ByteLinkedOpenHashMap.getAndMoveToFirst(blockStatePairKey);
			if (b != 127) {
				return b != 0;
			} else {
				VoxelShape voxelShape = blockState.getFaceOcclusionShape(blockGetter, blockPos, direction);
				VoxelShape voxelShape2 = blockState2.getFaceOcclusionShape(blockGetter, blockPos2, direction.getOpposite());
				boolean bl = Shapes.joinIsNotEmpty(voxelShape, voxelShape2, BooleanOp.ONLY_FIRST);
				if (object2ByteLinkedOpenHashMap.size() == 2048) {
					object2ByteLinkedOpenHashMap.removeLastByte();
				}

				object2ByteLinkedOpenHashMap.putAndMoveToFirst(blockStatePairKey, (byte)(bl ? 1 : 0));
				return bl;
			}
		} else {
			return true;
		}
	}

	@Deprecated
	public final boolean canOcclude(BlockState blockState) {
		return this.canOcclude;
	}

	@Deprecated
	@Environment(EnvType.CLIENT)
	public boolean skipRendering(BlockState blockState, BlockState blockState2, Direction direction) {
		return false;
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
	public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.getShape(blockGetter, blockPos);
	}

	@Deprecated
	public VoxelShape getInteractionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return Shapes.empty();
	}

	public static boolean canSupportRigidBlock(BlockGetter blockGetter, BlockPos blockPos) {
		BlockState blockState = blockGetter.getBlockState(blockPos);
		return !blockState.is(BlockTags.LEAVES)
			&& !Shapes.joinIsNotEmpty(blockState.getCollisionShape(blockGetter, blockPos).getFaceShape(Direction.UP), RIGID_SUPPORT_SHAPE, BooleanOp.ONLY_SECOND);
	}

	public static boolean canSupportCenter(LevelReader levelReader, BlockPos blockPos, Direction direction) {
		BlockState blockState = levelReader.getBlockState(blockPos);
		return !blockState.is(BlockTags.LEAVES)
			&& !Shapes.joinIsNotEmpty(blockState.getCollisionShape(levelReader, blockPos).getFaceShape(direction), CENTER_SUPPORT_SHAPE, BooleanOp.ONLY_SECOND);
	}

	public static boolean isFaceSturdy(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return !blockState.is(BlockTags.LEAVES) && isFaceFull(blockState.getCollisionShape(blockGetter, blockPos), direction);
	}

	public static boolean isFaceFull(VoxelShape voxelShape, Direction direction) {
		VoxelShape voxelShape2 = voxelShape.getFaceShape(direction);
		return isShapeFullBlock(voxelShape2);
	}

	public static boolean isShapeFullBlock(VoxelShape voxelShape) {
		return SHAPE_FULL_BLOCK_CACHE.getUnchecked(voxelShape);
	}

	@Deprecated
	public final boolean isSolidRender(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.canOcclude() ? isShapeFullBlock(blockState.getOcclusionShape(blockGetter, blockPos)) : false;
	}

	public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return !isShapeFullBlock(blockState.getShape(blockGetter, blockPos)) && blockState.getFluidState().isEmpty();
	}

	@Deprecated
	public int getLightBlock(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		if (blockState.isSolidRender(blockGetter, blockPos)) {
			return blockGetter.getMaxLightLevel();
		} else {
			return blockState.propagatesSkylightDown(blockGetter, blockPos) ? 0 : 1;
		}
	}

	@Deprecated
	public boolean useShapeForLightOcclusion(BlockState blockState) {
		return false;
	}

	@Deprecated
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		this.tick(blockState, serverLevel, blockPos, random);
	}

	@Deprecated
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
	}

	@Environment(EnvType.CLIENT)
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
	}

	public void destroy(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
	}

	@Deprecated
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		DebugPackets.sendNeighborsUpdatePacket(level, blockPos);
	}

	public int getTickDelay(LevelReader levelReader) {
		return 10;
	}

	@Nullable
	@Deprecated
	public MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
		return null;
	}

	@Deprecated
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
	}

	@Deprecated
	public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (this.isEntityBlock() && blockState.getBlock() != blockState2.getBlock()) {
			level.removeBlockEntity(blockPos);
		}
	}

	@Deprecated
	public float getDestroyProgress(BlockState blockState, Player player, BlockGetter blockGetter, BlockPos blockPos) {
		float f = blockState.getDestroySpeed(blockGetter, blockPos);
		if (f == -1.0F) {
			return 0.0F;
		} else {
			int i = player.canDestroy(blockState) ? 30 : 100;
			return player.getDestroySpeed(blockState) / f / (float)i;
		}
	}

	@Deprecated
	public void spawnAfterBreak(BlockState blockState, Level level, BlockPos blockPos, ItemStack itemStack) {
	}

	public ResourceLocation getLootTable() {
		if (this.drops == null) {
			ResourceLocation resourceLocation = Registry.BLOCK.getKey(this);
			this.drops = new ResourceLocation(resourceLocation.getNamespace(), "blocks/" + resourceLocation.getPath());
		}

		return this.drops;
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

	public static List<ItemStack> getDrops(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, @Nullable BlockEntity blockEntity) {
		LootContext.Builder builder = new LootContext.Builder(serverLevel)
			.withRandom(serverLevel.random)
			.withParameter(LootContextParams.BLOCK_POS, blockPos)
			.withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
			.withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);
		return blockState.getDrops(builder);
	}

	public static List<ItemStack> getDrops(
		BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack itemStack
	) {
		LootContext.Builder builder = new LootContext.Builder(serverLevel)
			.withRandom(serverLevel.random)
			.withParameter(LootContextParams.BLOCK_POS, blockPos)
			.withParameter(LootContextParams.TOOL, itemStack)
			.withOptionalParameter(LootContextParams.THIS_ENTITY, entity)
			.withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);
		return blockState.getDrops(builder);
	}

	public static void dropResources(BlockState blockState, Level level, BlockPos blockPos) {
		if (level instanceof ServerLevel) {
			getDrops(blockState, (ServerLevel)level, blockPos, null).forEach(itemStack -> popResource(level, blockPos, itemStack));
		}

		blockState.spawnAfterBreak(level, blockPos, ItemStack.EMPTY);
	}

	public static void dropResources(BlockState blockState, Level level, BlockPos blockPos, @Nullable BlockEntity blockEntity) {
		if (level instanceof ServerLevel) {
			getDrops(blockState, (ServerLevel)level, blockPos, blockEntity).forEach(itemStack -> popResource(level, blockPos, itemStack));
		}

		blockState.spawnAfterBreak(level, blockPos, ItemStack.EMPTY);
	}

	public static void dropResources(BlockState blockState, Level level, BlockPos blockPos, @Nullable BlockEntity blockEntity, Entity entity, ItemStack itemStack) {
		if (level instanceof ServerLevel) {
			getDrops(blockState, (ServerLevel)level, blockPos, blockEntity, entity, itemStack).forEach(itemStackx -> popResource(level, blockPos, itemStackx));
		}

		blockState.spawnAfterBreak(level, blockPos, itemStack);
	}

	public static void popResource(Level level, BlockPos blockPos, ItemStack itemStack) {
		if (!level.isClientSide && !itemStack.isEmpty() && level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
			float f = 0.5F;
			double d = (double)(level.random.nextFloat() * 0.5F) + 0.25;
			double e = (double)(level.random.nextFloat() * 0.5F) + 0.25;
			double g = (double)(level.random.nextFloat() * 0.5F) + 0.25;
			ItemEntity itemEntity = new ItemEntity(level, (double)blockPos.getX() + d, (double)blockPos.getY() + e, (double)blockPos.getZ() + g, itemStack);
			itemEntity.setDefaultPickUpDelay();
			level.addFreshEntity(itemEntity);
		}
	}

	protected void popExperience(Level level, BlockPos blockPos, int i) {
		if (!level.isClientSide && level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
			while (i > 0) {
				int j = ExperienceOrb.getExperienceValue(i);
				i -= j;
				level.addFreshEntity(new ExperienceOrb(level, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, j));
			}
		}
	}

	public float getExplosionResistance() {
		return this.explosionResistance;
	}

	public void wasExploded(Level level, BlockPos blockPos, Explosion explosion) {
	}

	@Deprecated
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return true;
	}

	@Deprecated
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		return InteractionResult.PASS;
	}

	public void stepOn(Level level, BlockPos blockPos, Entity entity) {
	}

	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.defaultBlockState();
	}

	@Deprecated
	public void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
	}

	@Deprecated
	public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return 0;
	}

	@Deprecated
	public boolean isSignalSource(BlockState blockState) {
		return false;
	}

	@Deprecated
	public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
	}

	@Deprecated
	public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return 0;
	}

	public void playerDestroy(Level level, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, ItemStack itemStack) {
		player.awardStat(Stats.BLOCK_MINED.get(this));
		player.causeFoodExhaustion(0.005F);
		dropResources(blockState, level, blockPos, blockEntity, player, itemStack);
	}

	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
	}

	public boolean isPossibleToRespawnInThis() {
		return !this.material.isSolid() && !this.material.isLiquid();
	}

	@Environment(EnvType.CLIENT)
	public Component getName() {
		return new TranslatableComponent(this.getDescriptionId());
	}

	public String getDescriptionId() {
		if (this.descriptionId == null) {
			this.descriptionId = Util.makeDescriptionId("block", Registry.BLOCK.getKey(this));
		}

		return this.descriptionId;
	}

	@Deprecated
	public boolean triggerEvent(BlockState blockState, Level level, BlockPos blockPos, int i, int j) {
		return false;
	}

	@Deprecated
	public PushReaction getPistonPushReaction(BlockState blockState) {
		return this.material.getPushReaction();
	}

	@Deprecated
	@Environment(EnvType.CLIENT)
	public float getShadeBrightness(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.isCollisionShapeFullBlock(blockGetter, blockPos) ? 0.2F : 1.0F;
	}

	public void fallOn(Level level, BlockPos blockPos, Entity entity, float f) {
		entity.causeFallDamage(f, 1.0F);
	}

	public void updateEntityAfterFallOn(BlockGetter blockGetter, Entity entity) {
		entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0, 0.0, 1.0));
	}

	@Environment(EnvType.CLIENT)
	public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
		return new ItemStack(this);
	}

	public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList) {
		nonNullList.add(new ItemStack(this));
	}

	@Deprecated
	public FluidState getFluidState(BlockState blockState) {
		return Fluids.EMPTY.defaultFluidState();
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

	@Deprecated
	@Environment(EnvType.CLIENT)
	public long getSeed(BlockState blockState, BlockPos blockPos) {
		return Mth.getSeed(blockPos);
	}

	public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Entity entity) {
	}

	public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
		level.levelEvent(player, 2001, blockPos, getId(blockState));
	}

	public void handleRain(Level level, BlockPos blockPos) {
	}

	public boolean dropFromExplosion(Explosion explosion) {
		return true;
	}

	@Deprecated
	public boolean hasAnalogOutputSignal(BlockState blockState) {
		return false;
	}

	@Deprecated
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return 0;
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

	public Block.OffsetType getOffsetType() {
		return Block.OffsetType.NONE;
	}

	@Deprecated
	public Vec3 getOffset(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		Block.OffsetType offsetType = this.getOffsetType();
		if (offsetType == Block.OffsetType.NONE) {
			return Vec3.ZERO;
		} else {
			long l = Mth.getSeed(blockPos.getX(), 0, blockPos.getZ());
			return new Vec3(
				((double)((float)(l & 15L) / 15.0F) - 0.5) * 0.5,
				offsetType == Block.OffsetType.XYZ ? ((double)((float)(l >> 4 & 15L) / 15.0F) - 1.0) * 0.2 : 0.0,
				((double)((float)(l >> 8 & 15L) / 15.0F) - 0.5) * 0.5
			);
		}
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

	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter blockGetter, List<Component> list, TooltipFlag tooltipFlag) {
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
			} else if (!(object instanceof Block.BlockStatePairKey)) {
				return false;
			} else {
				Block.BlockStatePairKey blockStatePairKey = (Block.BlockStatePairKey)object;
				return this.first == blockStatePairKey.first && this.second == blockStatePairKey.second && this.direction == blockStatePairKey.direction;
			}
		}

		public int hashCode() {
			int i = this.first.hashCode();
			i = 31 * i + this.second.hashCode();
			return 31 * i + this.direction.hashCode();
		}
	}

	public static enum OffsetType {
		NONE,
		XZ,
		XYZ;
	}

	public static class Properties {
		private Material material;
		private MaterialColor materialColor;
		private boolean hasCollision = true;
		private SoundType soundType = SoundType.STONE;
		private int lightEmission;
		private float explosionResistance;
		private float destroyTime;
		private boolean isTicking;
		private float friction = 0.6F;
		private float speedFactor = 1.0F;
		private float jumpFactor = 1.0F;
		private ResourceLocation drops;
		private boolean canOcclude = true;
		private boolean dynamicShape;

		private Properties(Material material, MaterialColor materialColor) {
			this.material = material;
			this.materialColor = materialColor;
		}

		public static Block.Properties of(Material material) {
			return of(material, material.getColor());
		}

		public static Block.Properties of(Material material, DyeColor dyeColor) {
			return of(material, dyeColor.getMaterialColor());
		}

		public static Block.Properties of(Material material, MaterialColor materialColor) {
			return new Block.Properties(material, materialColor);
		}

		public static Block.Properties copy(Block block) {
			Block.Properties properties = new Block.Properties(block.material, block.materialColor);
			properties.material = block.material;
			properties.destroyTime = block.destroySpeed;
			properties.explosionResistance = block.explosionResistance;
			properties.hasCollision = block.hasCollision;
			properties.isTicking = block.isTicking;
			properties.lightEmission = block.lightEmission;
			properties.materialColor = block.materialColor;
			properties.soundType = block.soundType;
			properties.friction = block.getFriction();
			properties.speedFactor = block.getSpeedFactor();
			properties.dynamicShape = block.dynamicShape;
			properties.canOcclude = block.canOcclude;
			return properties;
		}

		public Block.Properties noCollission() {
			this.hasCollision = false;
			this.canOcclude = false;
			return this;
		}

		public Block.Properties noOcclusion() {
			this.canOcclude = false;
			return this;
		}

		public Block.Properties friction(float f) {
			this.friction = f;
			return this;
		}

		public Block.Properties speedFactor(float f) {
			this.speedFactor = f;
			return this;
		}

		public Block.Properties jumpFactor(float f) {
			this.jumpFactor = f;
			return this;
		}

		protected Block.Properties sound(SoundType soundType) {
			this.soundType = soundType;
			return this;
		}

		protected Block.Properties lightLevel(int i) {
			this.lightEmission = i;
			return this;
		}

		public Block.Properties strength(float f, float g) {
			this.destroyTime = f;
			this.explosionResistance = Math.max(0.0F, g);
			return this;
		}

		protected Block.Properties instabreak() {
			return this.strength(0.0F);
		}

		protected Block.Properties strength(float f) {
			this.strength(f, f);
			return this;
		}

		protected Block.Properties randomTicks() {
			this.isTicking = true;
			return this;
		}

		protected Block.Properties dynamicShape() {
			this.dynamicShape = true;
			return this;
		}

		protected Block.Properties noDrops() {
			this.drops = BuiltInLootTables.EMPTY;
			return this;
		}

		public Block.Properties dropsLike(Block block) {
			this.drops = block.getLootTable();
			return this;
		}
	}
}
