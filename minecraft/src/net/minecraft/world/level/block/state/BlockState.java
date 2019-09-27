package net.minecraft.world.level.block.state;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockState extends AbstractStateHolder<Block, BlockState> implements StateHolder<BlockState> {
	@Nullable
	private BlockState.Cache cache;
	private final int lightEmission;
	private final boolean useShapeForLightOcclusion;

	public BlockState(Block block, ImmutableMap<Property<?>, Comparable<?>> immutableMap) {
		super(block, immutableMap);
		this.lightEmission = block.getLightEmission(this);
		this.useShapeForLightOcclusion = block.useShapeForLightOcclusion(this);
	}

	public void initCache() {
		if (!this.getBlock().hasDynamicShape()) {
			this.cache = new BlockState.Cache(this);
		}
	}

	public Block getBlock() {
		return this.owner;
	}

	public Material getMaterial() {
		return this.getBlock().getMaterial(this);
	}

	public boolean isValidSpawn(BlockGetter blockGetter, BlockPos blockPos, EntityType<?> entityType) {
		return this.getBlock().isValidSpawn(this, blockGetter, blockPos, entityType);
	}

	public boolean propagatesSkylightDown(BlockGetter blockGetter, BlockPos blockPos) {
		return this.cache != null ? this.cache.propagatesSkylightDown : this.getBlock().propagatesSkylightDown(this, blockGetter, blockPos);
	}

	public int getLightBlock(BlockGetter blockGetter, BlockPos blockPos) {
		return this.cache != null ? this.cache.lightBlock : this.getBlock().getLightBlock(this, blockGetter, blockPos);
	}

	public VoxelShape getFaceOcclusionShape(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return this.cache != null && this.cache.occlusionShapes != null
			? this.cache.occlusionShapes[direction.ordinal()]
			: Shapes.getFaceShape(this.getOcclusionShape(blockGetter, blockPos), direction);
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
		return this.getBlock().isAir(this);
	}

	public MaterialColor getMapColor(BlockGetter blockGetter, BlockPos blockPos) {
		return this.getBlock().getMapColor(this, blockGetter, blockPos);
	}

	public BlockState rotate(Rotation rotation) {
		return this.getBlock().rotate(this, rotation);
	}

	public BlockState mirror(Mirror mirror) {
		return this.getBlock().mirror(this, mirror);
	}

	public RenderShape getRenderShape() {
		return this.getBlock().getRenderShape(this);
	}

	@Environment(EnvType.CLIENT)
	public boolean emissiveRendering() {
		return this.getBlock().emissiveRendering(this);
	}

	@Environment(EnvType.CLIENT)
	public float getShadeBrightness(BlockGetter blockGetter, BlockPos blockPos) {
		return this.getBlock().getShadeBrightness(this, blockGetter, blockPos);
	}

	public boolean isRedstoneConductor(BlockGetter blockGetter, BlockPos blockPos) {
		return this.getBlock().isRedstoneConductor(this, blockGetter, blockPos);
	}

	public boolean isSignalSource() {
		return this.getBlock().isSignalSource(this);
	}

	public int getSignal(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return this.getBlock().getSignal(this, blockGetter, blockPos, direction);
	}

	public boolean hasAnalogOutputSignal() {
		return this.getBlock().hasAnalogOutputSignal(this);
	}

	public int getAnalogOutputSignal(Level level, BlockPos blockPos) {
		return this.getBlock().getAnalogOutputSignal(this, level, blockPos);
	}

	public float getDestroySpeed(BlockGetter blockGetter, BlockPos blockPos) {
		return this.getBlock().getDestroySpeed(this, blockGetter, blockPos);
	}

	public float getDestroyProgress(Player player, BlockGetter blockGetter, BlockPos blockPos) {
		return this.getBlock().getDestroyProgress(this, player, blockGetter, blockPos);
	}

	public int getDirectSignal(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return this.getBlock().getDirectSignal(this, blockGetter, blockPos, direction);
	}

	public PushReaction getPistonPushReaction() {
		return this.getBlock().getPistonPushReaction(this);
	}

	public boolean isSolidRender(BlockGetter blockGetter, BlockPos blockPos) {
		return this.cache != null ? this.cache.solidRender : this.getBlock().isSolidRender(this, blockGetter, blockPos);
	}

	public boolean canOcclude() {
		return this.cache != null ? this.cache.canOcclude : this.getBlock().canOcclude(this);
	}

	@Environment(EnvType.CLIENT)
	public boolean skipRendering(BlockState blockState, Direction direction) {
		return this.getBlock().skipRendering(this, blockState, direction);
	}

	public VoxelShape getShape(BlockGetter blockGetter, BlockPos blockPos) {
		return this.getShape(blockGetter, blockPos, CollisionContext.empty());
	}

	public VoxelShape getShape(BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return this.getBlock().getShape(this, blockGetter, blockPos, collisionContext);
	}

	public VoxelShape getCollisionShape(BlockGetter blockGetter, BlockPos blockPos) {
		return this.cache != null ? this.cache.collisionShape : this.getCollisionShape(blockGetter, blockPos, CollisionContext.empty());
	}

	public VoxelShape getCollisionShape(BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return this.getBlock().getCollisionShape(this, blockGetter, blockPos, collisionContext);
	}

	public VoxelShape getOcclusionShape(BlockGetter blockGetter, BlockPos blockPos) {
		return this.getBlock().getOcclusionShape(this, blockGetter, blockPos);
	}

	public VoxelShape getInteractionShape(BlockGetter blockGetter, BlockPos blockPos) {
		return this.getBlock().getInteractionShape(this, blockGetter, blockPos);
	}

	public final boolean entityCanStandOn(BlockGetter blockGetter, BlockPos blockPos, Entity entity) {
		return Block.isFaceFull(this.getCollisionShape(blockGetter, blockPos, CollisionContext.of(entity)), Direction.UP);
	}

	public Vec3 getOffset(BlockGetter blockGetter, BlockPos blockPos) {
		return this.getBlock().getOffset(this, blockGetter, blockPos);
	}

	public boolean triggerEvent(Level level, BlockPos blockPos, int i, int j) {
		return this.getBlock().triggerEvent(this, level, blockPos, i, j);
	}

	public void neighborChanged(Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		this.getBlock().neighborChanged(this, level, blockPos, block, blockPos2, bl);
	}

	public void updateNeighbourShapes(LevelAccessor levelAccessor, BlockPos blockPos, int i) {
		this.getBlock().updateNeighbourShapes(this, levelAccessor, blockPos, i);
	}

	public void updateIndirectNeighbourShapes(LevelAccessor levelAccessor, BlockPos blockPos, int i) {
		this.getBlock().updateIndirectNeighbourShapes(this, levelAccessor, blockPos, i);
	}

	public void onPlace(Level level, BlockPos blockPos, BlockState blockState, boolean bl) {
		this.getBlock().onPlace(this, level, blockPos, blockState, bl);
	}

	public void onRemove(Level level, BlockPos blockPos, BlockState blockState, boolean bl) {
		this.getBlock().onRemove(this, level, blockPos, blockState, bl);
	}

	public void tick(ServerLevel serverLevel, BlockPos blockPos, Random random) {
		this.getBlock().tick(this, serverLevel, blockPos, random);
	}

	public void randomTick(ServerLevel serverLevel, BlockPos blockPos, Random random) {
		this.getBlock().randomTick(this, serverLevel, blockPos, random);
	}

	public void entityInside(Level level, BlockPos blockPos, Entity entity) {
		this.getBlock().entityInside(this, level, blockPos, entity);
	}

	public void spawnAfterBreak(Level level, BlockPos blockPos, ItemStack itemStack) {
		this.getBlock().spawnAfterBreak(this, level, blockPos, itemStack);
	}

	public List<ItemStack> getDrops(LootContext.Builder builder) {
		return this.getBlock().getDrops(this, builder);
	}

	public boolean use(Level level, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
		return this.getBlock().use(this, level, blockHitResult.getBlockPos(), player, interactionHand, blockHitResult);
	}

	public void attack(Level level, BlockPos blockPos, Player player) {
		this.getBlock().attack(this, level, blockPos, player);
	}

	public boolean isViewBlocking(BlockGetter blockGetter, BlockPos blockPos) {
		return this.getBlock().isViewBlocking(this, blockGetter, blockPos);
	}

	public BlockState updateShape(Direction direction, BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
		return this.getBlock().updateShape(this, direction, blockState, levelAccessor, blockPos, blockPos2);
	}

	public boolean isPathfindable(BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return this.getBlock().isPathfindable(this, blockGetter, blockPos, pathComputationType);
	}

	public boolean canBeReplaced(BlockPlaceContext blockPlaceContext) {
		return this.getBlock().canBeReplaced(this, blockPlaceContext);
	}

	public boolean canBeReplaced(Fluid fluid) {
		return this.getBlock().canBeReplaced(this, fluid);
	}

	public boolean canSurvive(LevelReader levelReader, BlockPos blockPos) {
		return this.getBlock().canSurvive(this, levelReader, blockPos);
	}

	public boolean hasPostProcess(BlockGetter blockGetter, BlockPos blockPos) {
		return this.getBlock().hasPostProcess(this, blockGetter, blockPos);
	}

	@Nullable
	public MenuProvider getMenuProvider(Level level, BlockPos blockPos) {
		return this.getBlock().getMenuProvider(this, level, blockPos);
	}

	public boolean is(Tag<Block> tag) {
		return this.getBlock().is(tag);
	}

	public FluidState getFluidState() {
		return this.getBlock().getFluidState(this);
	}

	public boolean isRandomlyTicking() {
		return this.getBlock().isRandomlyTicking(this);
	}

	@Environment(EnvType.CLIENT)
	public long getSeed(BlockPos blockPos) {
		return this.getBlock().getSeed(this, blockPos);
	}

	public SoundType getSoundType() {
		return this.getBlock().getSoundType(this);
	}

	public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Entity entity) {
		this.getBlock().onProjectileHit(level, blockState, blockHitResult, entity);
	}

	public boolean isFaceSturdy(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return this.cache != null ? this.cache.isFaceSturdy[direction.ordinal()] : Block.isFaceSturdy(this, blockGetter, blockPos, direction);
	}

	public boolean isCollisionShapeFullBlock(BlockGetter blockGetter, BlockPos blockPos) {
		return this.cache != null ? this.cache.isCollisionShapeFullBlock : Block.isShapeFullBlock(this.getCollisionShape(blockGetter, blockPos));
	}

	public static <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps, BlockState blockState) {
		ImmutableMap<Property<?>, Comparable<?>> immutableMap = blockState.getValues();
		T object;
		if (immutableMap.isEmpty()) {
			object = dynamicOps.createMap(
				ImmutableMap.of(dynamicOps.createString("Name"), dynamicOps.createString(Registry.BLOCK.getKey(blockState.getBlock()).toString()))
			);
		} else {
			object = dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("Name"),
					dynamicOps.createString(Registry.BLOCK.getKey(blockState.getBlock()).toString()),
					dynamicOps.createString("Properties"),
					dynamicOps.createMap(
						(Map<T, T>)immutableMap.entrySet()
							.stream()
							.map(
								entry -> Pair.of(
										dynamicOps.createString(((Property)entry.getKey()).getName()),
										dynamicOps.createString(StateHolder.getName((Property<T>)entry.getKey(), (Comparable<?>)entry.getValue()))
									)
							)
							.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))
					)
				)
			);
		}

		return new Dynamic<>(dynamicOps, object);
	}

	public static <T> BlockState deserialize(Dynamic<T> dynamic) {
		Block block = Registry.BLOCK.get(new ResourceLocation((String)dynamic.getElement("Name").flatMap(dynamic.getOps()::getStringValue).orElse("minecraft:air")));
		Map<String, String> map = dynamic.get("Properties").asMap(dynamicx -> dynamicx.asString(""), dynamicx -> dynamicx.asString(""));
		BlockState blockState = block.defaultBlockState();
		StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();

		for (Entry<String, String> entry : map.entrySet()) {
			String string = (String)entry.getKey();
			Property<?> property = stateDefinition.getProperty(string);
			if (property != null) {
				blockState = StateHolder.setValueHelper(blockState, property, string, dynamic.toString(), (String)entry.getValue());
			}
		}

		return blockState;
	}

	static final class Cache {
		private static final Direction[] DIRECTIONS = Direction.values();
		private final boolean canOcclude;
		private final boolean solidRender;
		private final boolean propagatesSkylightDown;
		private final int lightBlock;
		private final VoxelShape[] occlusionShapes;
		private final VoxelShape collisionShape;
		private final boolean largeCollisionShape;
		private final boolean[] isFaceSturdy;
		private final boolean isCollisionShapeFullBlock;

		private Cache(BlockState blockState) {
			Block block = blockState.getBlock();
			this.canOcclude = block.canOcclude(blockState);
			this.solidRender = block.isSolidRender(blockState, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
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
			this.largeCollisionShape = Arrays.stream(Direction.Axis.values())
				.anyMatch(axis -> this.collisionShape.min(axis) < 0.0 || this.collisionShape.max(axis) > 1.0);
			this.isFaceSturdy = new boolean[6];

			for (Direction direction2 : DIRECTIONS) {
				this.isFaceSturdy[direction2.ordinal()] = Block.isFaceSturdy(blockState, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, direction2);
			}

			this.isCollisionShapeFullBlock = Block.isShapeFullBlock(blockState.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
		}
	}
}
