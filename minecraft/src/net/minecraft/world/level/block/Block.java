package net.minecraft.world.level.block;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.voting.rules.Rules;
import net.minecraft.voting.rules.actual.Goldifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;

public class Block extends BlockBehaviour implements ItemLike {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Holder.Reference<Block> builtInRegistryHolder = BuiltInRegistries.BLOCK.createIntrusiveHolder(this);
	public static final IdMapper<BlockState> BLOCK_STATE_REGISTRY = new IdMapper<>();
	private static final LoadingCache<VoxelShape, Boolean> SHAPE_FULL_BLOCK_CACHE = CacheBuilder.newBuilder()
		.maximumSize(512L)
		.weakKeys()
		.build(new CacheLoader<VoxelShape, Boolean>() {
			public Boolean load(VoxelShape voxelShape) {
				return !Shapes.joinIsNotEmpty(Shapes.block(), voxelShape, BooleanOp.NOT_SAME);
			}
		});
	public static final int UPDATE_NEIGHBORS = 1;
	public static final int UPDATE_CLIENTS = 2;
	public static final int UPDATE_INVISIBLE = 4;
	public static final int UPDATE_IMMEDIATE = 8;
	public static final int UPDATE_KNOWN_SHAPE = 16;
	public static final int UPDATE_SUPPRESS_DROPS = 32;
	public static final int UPDATE_MOVE_BY_PISTON = 64;
	public static final int UPDATE_SUPPRESS_LIGHT = 128;
	public static final int UPDATE_NONE = 4;
	public static final int UPDATE_ALL = 3;
	public static final int UPDATE_ALL_IMMEDIATE = 11;
	public static final float INDESTRUCTIBLE = -1.0F;
	public static final float INSTANT = 0.0F;
	public static final int UPDATE_LIMIT = 512;
	protected final StateDefinition<Block, BlockState> stateDefinition;
	private BlockState defaultBlockState;
	@Nullable
	private String descriptionId;
	@Nullable
	private Item item;
	private static final int CACHE_SIZE = 2048;
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

	public static BlockState pushEntitiesUp(BlockState blockState, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos) {
		VoxelShape voxelShape = Shapes.joinUnoptimized(
				blockState.getCollisionShape(levelAccessor, blockPos), blockState2.getCollisionShape(levelAccessor, blockPos), BooleanOp.ONLY_SECOND
			)
			.move((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
		if (voxelShape.isEmpty()) {
			return blockState2;
		} else {
			for (Entity entity : levelAccessor.getEntities(null, voxelShape.bounds())) {
				double d = Shapes.collide(Direction.Axis.Y, entity.getBoundingBox().move(0.0, 1.0, 0.0), List.of(voxelShape), -1.0);
				entity.teleportRelative(0.0, 1.0 + d, 0.0);
			}

			return blockState2;
		}
	}

	public static VoxelShape box(double d, double e, double f, double g, double h, double i) {
		return Shapes.box(d / 16.0, e / 16.0, f / 16.0, g / 16.0, h / 16.0, i / 16.0);
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
		updateOrDestroy(blockState, blockState2, levelAccessor, blockPos, i, 512);
	}

	public static void updateOrDestroy(BlockState blockState, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, int i, int j) {
		if (blockState2 != blockState) {
			if (blockState2.isAir()) {
				if (!levelAccessor.isClientSide()) {
					levelAccessor.destroyBlock(blockPos, (i & 32) == 0, null, j);
				}
			} else {
				levelAccessor.setBlock(blockPos, blockState2, i & -33, j);
			}
		}
	}

	public Block(BlockBehaviour.Properties properties) {
		super(properties);
		StateDefinition.Builder<Block, BlockState> builder = new StateDefinition.Builder<>(this);
		this.createBlockStateDefinition(builder);
		this.stateDefinition = builder.create(Block::defaultBlockState, BlockState::new);
		this.registerDefaultState(this.stateDefinition.any());
		if (SharedConstants.IS_RUNNING_IN_IDE) {
			String string = this.getClass().getSimpleName();
			if (!string.endsWith("Block")) {
				LOGGER.error("Block classes should end with Block and {} doesn't.", string);
			}
		}
	}

	public static boolean isExceptionForConnection(BlockState blockState) {
		return blockState.getBlock() instanceof LeavesBlock
			|| blockState.is(Blocks.BARRIER)
			|| blockState.is(Blocks.CARVED_PUMPKIN)
			|| blockState.is(Blocks.JACK_O_LANTERN)
			|| blockState.is(Blocks.MELON)
			|| blockState.is(Blocks.PUMPKIN)
			|| blockState.is(BlockTags.SHULKER_BOXES);
	}

	public boolean isRandomlyTicking(BlockState blockState) {
		return this.isRandomlyTicking;
	}

	public static boolean shouldRenderFace(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction, BlockPos blockPos2) {
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
				if (voxelShape.isEmpty()) {
					return true;
				} else {
					VoxelShape voxelShape2 = blockState2.getFaceOcclusionShape(blockGetter, blockPos2, direction.getOpposite());
					boolean bl = Shapes.joinIsNotEmpty(voxelShape, voxelShape2, BooleanOp.ONLY_FIRST);
					if (object2ByteLinkedOpenHashMap.size() == 2048) {
						object2ByteLinkedOpenHashMap.removeLastByte();
					}

					object2ByteLinkedOpenHashMap.putAndMoveToFirst(blockStatePairKey, (byte)(bl ? 1 : 0));
					return bl;
				}
			}
		} else {
			return true;
		}
	}

	public static boolean canSupportRigidBlock(BlockGetter blockGetter, BlockPos blockPos) {
		return Rules.BUTTONS_ON_THINGS.get() && blockGetter.getBlockState(blockPos).isFaceSturdy(blockGetter, blockPos, Direction.UP, SupportType.CENTER)
			? true
			: blockGetter.getBlockState(blockPos).isFaceSturdy(blockGetter, blockPos, Direction.UP, SupportType.RIGID);
	}

	public static boolean canSupportCenter(LevelReader levelReader, BlockPos blockPos, Direction direction) {
		BlockState blockState = levelReader.getBlockState(blockPos);
		return direction == Direction.DOWN && blockState.is(BlockTags.UNSTABLE_BOTTOM_CENTER)
			? false
			: blockState.isFaceSturdy(levelReader, blockPos, direction, SupportType.CENTER);
	}

	public static boolean isFaceFull(VoxelShape voxelShape, Direction direction) {
		VoxelShape voxelShape2 = voxelShape.getFaceShape(direction);
		return isShapeFullBlock(voxelShape2);
	}

	public static boolean isShapeFullBlock(VoxelShape voxelShape) {
		return SHAPE_FULL_BLOCK_CACHE.getUnchecked(voxelShape);
	}

	public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return !isShapeFullBlock(blockState.getShape(blockGetter, blockPos)) && blockState.getFluidState().isEmpty();
	}

	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
	}

	public void destroy(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
	}

	public static List<ItemStack> getDrops(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, @Nullable BlockEntity blockEntity) {
		LootContext.Builder builder = new LootContext.Builder(serverLevel)
			.withRandom(serverLevel.random)
			.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos))
			.withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
			.withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);
		return blockState.getDrops(builder);
	}

	public static List<ItemStack> getDrops(
		BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack itemStack
	) {
		LootContext.Builder builder = new LootContext.Builder(serverLevel)
			.withRandom(serverLevel.random)
			.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos))
			.withParameter(LootContextParams.TOOL, itemStack)
			.withOptionalParameter(LootContextParams.THIS_ENTITY, entity)
			.withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);
		return blockState.getDrops(builder);
	}

	public static void dropResources(BlockState blockState, LootContext.Builder builder) {
		ServerLevel serverLevel = builder.getLevel();
		BlockPos blockPos = BlockPos.containing(builder.getParameter(LootContextParams.ORIGIN));
		blockState.getDrops(builder).forEach(itemStack -> popResource(serverLevel, blockPos, itemStack));
		blockState.spawnAfterBreak(serverLevel, blockPos, ItemStack.EMPTY, true);
	}

	public static void dropResources(BlockState blockState, Level level, BlockPos blockPos) {
		if (level instanceof ServerLevel) {
			getDrops(blockState, (ServerLevel)level, blockPos, null).forEach(itemStack -> popResource(level, blockPos, itemStack));
			blockState.spawnAfterBreak((ServerLevel)level, blockPos, ItemStack.EMPTY, true);
		}
	}

	public static void dropResources(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, @Nullable BlockEntity blockEntity) {
		if (levelAccessor instanceof ServerLevel) {
			getDrops(blockState, (ServerLevel)levelAccessor, blockPos, blockEntity).forEach(itemStack -> popResource((ServerLevel)levelAccessor, blockPos, itemStack));
			blockState.spawnAfterBreak((ServerLevel)levelAccessor, blockPos, ItemStack.EMPTY, true);
		}
	}

	public static void dropResources(BlockState blockState, Level level, BlockPos blockPos, @Nullable BlockEntity blockEntity, Entity entity, ItemStack itemStack) {
		if (level instanceof ServerLevel) {
			getDrops(blockState, (ServerLevel)level, blockPos, blockEntity, entity, itemStack).forEach(itemStackx -> popResource(level, blockPos, itemStackx));
			blockState.spawnAfterBreak((ServerLevel)level, blockPos, itemStack, true);
		}
	}

	public static void popResource(Level level, BlockPos blockPos, ItemStack itemStack) {
		double d = (double)EntityType.ITEM.getHeight() / 2.0;
		double e = (double)blockPos.getX() + 0.5 + Mth.nextDouble(level.random, -0.25, 0.25);
		double f = (double)blockPos.getY() + 0.5 + Mth.nextDouble(level.random, -0.25, 0.25) - d;
		double g = (double)blockPos.getZ() + 0.5 + Mth.nextDouble(level.random, -0.25, 0.25);
		popResource(level, () -> new ItemEntity(level, e, f, g, itemStack), itemStack);
	}

	public static void popResourceFromFace(Level level, BlockPos blockPos, Direction direction, ItemStack itemStack) {
		int i = direction.getStepX();
		int j = direction.getStepY();
		int k = direction.getStepZ();
		double d = (double)EntityType.ITEM.getWidth() / 2.0;
		double e = (double)EntityType.ITEM.getHeight() / 2.0;
		double f = (double)blockPos.getX() + 0.5 + (i == 0 ? Mth.nextDouble(level.random, -0.25, 0.25) : (double)i * (0.5 + d));
		double g = (double)blockPos.getY() + 0.5 + (j == 0 ? Mth.nextDouble(level.random, -0.25, 0.25) : (double)j * (0.5 + e)) - e;
		double h = (double)blockPos.getZ() + 0.5 + (k == 0 ? Mth.nextDouble(level.random, -0.25, 0.25) : (double)k * (0.5 + d));
		double l = i == 0 ? Mth.nextDouble(level.random, -0.1, 0.1) : (double)i * 0.1;
		double m = j == 0 ? Mth.nextDouble(level.random, 0.0, 0.1) : (double)j * 0.1 + 0.1;
		double n = k == 0 ? Mth.nextDouble(level.random, -0.1, 0.1) : (double)k * 0.1;
		popResource(level, () -> new ItemEntity(level, f, g, h, itemStack, l, m, n), itemStack);
	}

	private static void popResource(Level level, Supplier<ItemEntity> supplier, ItemStack itemStack) {
		if (!level.isClientSide && !itemStack.isEmpty() && level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
			ItemEntity itemEntity = (ItemEntity)supplier.get();
			itemEntity.setDefaultPickUpDelay();
			level.addFreshEntity(itemEntity);
		}
	}

	protected void popExperience(ServerLevel serverLevel, BlockPos blockPos, int i) {
		if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
			ExperienceOrb.award(serverLevel, Vec3.atCenterOf(blockPos), i);
		}
	}

	public float getExplosionResistance() {
		return this.explosionResistance;
	}

	public void wasExploded(Level level, BlockPos blockPos, Explosion explosion) {
	}

	public void stepOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity) {
	}

	public void playerStepOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity) {
		if (Rules.MIDAS_TOUCH.get() && !blockState.isAir()) {
			BlockEntity blockEntity = level.getBlockEntity(blockPos);
			if (blockEntity != null) {
				return;
			}

			BlockState blockState2 = Goldifier.apply(blockState);
			if (blockState2 != blockState) {
				level.setBlock(blockPos, blockState2, 3);
			}
		}
	}

	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.defaultBlockState();
	}

	public void playerDestroy(Level level, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, ItemStack itemStack) {
		player.awardStat(Stats.BLOCK_MINED.get(this));
		player.causeFoodExhaustion(0.005F);
		dropResources(blockState, level, blockPos, blockEntity, player, itemStack);
		if (Rules.ULTRA_REALISTIC_MODE.get() && player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
			player.hurt(player.damageSources().generic(), 2.0F);
		}
	}

	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
	}

	public boolean isPossibleToRespawnInThis() {
		return !this.material.isSolid() && !this.material.isLiquid();
	}

	public MutableComponent getName() {
		return Component.translatable(this.getDescriptionId());
	}

	public String getDescriptionId() {
		if (this.descriptionId == null) {
			this.descriptionId = Util.makeDescriptionId("block", BuiltInRegistries.BLOCK.getKey(this));
		}

		return this.descriptionId;
	}

	public void fallOn(Level level, BlockState blockState, BlockPos blockPos, Entity entity, float f) {
		entity.causeFallDamage(f, 1.0F, entity.damageSources().fall());
	}

	public void updateEntityAfterFallOn(BlockGetter blockGetter, Entity entity) {
		entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0, 0.0, 1.0));
	}

	public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
		return new ItemStack(this);
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

	protected void spawnDestroyParticles(Level level, Player player, BlockPos blockPos, BlockState blockState) {
		level.levelEvent(player, 2001, blockPos, getId(blockState));
	}

	public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
		this.spawnDestroyParticles(level, player, blockPos, blockState);
		if (blockState.is(BlockTags.GUARDED_BY_PIGLINS)) {
			PiglinAi.angerNearbyPiglins(player, false);
		}

		level.gameEvent(GameEvent.BLOCK_DESTROY, blockPos, GameEvent.Context.of(player, blockState));
	}

	public void handlePrecipitation(BlockState blockState, Level level, BlockPos blockPos, Biome.Precipitation precipitation) {
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

	public final BlockState withPropertiesOf(BlockState blockState) {
		BlockState blockState2 = this.defaultBlockState();

		for (Property<?> property : blockState.getBlock().getStateDefinition().getProperties()) {
			if (blockState2.hasProperty(property)) {
				blockState2 = copyProperty(blockState, blockState2, property);
			}
		}

		return blockState2;
	}

	private static <T extends Comparable<T>> BlockState copyProperty(BlockState blockState, BlockState blockState2, Property<T> property) {
		return blockState2.setValue(property, blockState.getValue(property));
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
		return "Block{" + BuiltInRegistries.BLOCK.getKey(this) + "}";
	}

	public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter blockGetter, List<Component> list, TooltipFlag tooltipFlag) {
	}

	@Override
	protected Block asBlock() {
		return this;
	}

	protected ImmutableMap<BlockState, VoxelShape> getShapeForEachState(Function<BlockState, VoxelShape> function) {
		return (ImmutableMap<BlockState, VoxelShape>)this.stateDefinition
			.getPossibleStates()
			.stream()
			.collect(ImmutableMap.toImmutableMap(Function.identity(), function));
	}

	@Deprecated
	public Holder.Reference<Block> builtInRegistryHolder() {
		return this.builtInRegistryHolder;
	}

	protected void tryDropExperience(ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, IntProvider intProvider) {
		if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemStack) == 0) {
			int i = intProvider.sample(serverLevel.random);
			if (i > 0) {
				this.popExperience(serverLevel, blockPos, i);
			}
		}
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
			} else {
				return !(object instanceof Block.BlockStatePairKey blockStatePairKey)
					? false
					: this.first == blockStatePairKey.first && this.second == blockStatePairKey.second && this.direction == blockStatePairKey.direction;
			}
		}

		public int hashCode() {
			int i = this.first.hashCode();
			i = 31 * i + this.second.hashCode();
			return 31 * i + this.direction.hashCode();
		}
	}
}
