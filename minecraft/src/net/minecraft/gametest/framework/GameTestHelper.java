package net.minecraft.gametest.framework;

import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.LongStream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class GameTestHelper {
	private final GameTestInfo testInfo;
	private boolean finalCheckAdded;

	public GameTestHelper(GameTestInfo gameTestInfo) {
		this.testInfo = gameTestInfo;
	}

	public ServerLevel getLevel() {
		return this.testInfo.getLevel();
	}

	public BlockState getBlockState(BlockPos blockPos) {
		return this.getLevel().getBlockState(this.absolutePos(blockPos));
	}

	@Nullable
	public BlockEntity getBlockEntity(BlockPos blockPos) {
		return this.getLevel().getBlockEntity(this.absolutePos(blockPos));
	}

	public void killAllEntities() {
		this.killAllEntitiesOfClass(Entity.class);
	}

	public void killAllEntitiesOfClass(Class class_) {
		AABB aABB = this.getBounds();
		List<Entity> list = this.getLevel().getEntitiesOfClass(class_, aABB.inflate(1.0), entity -> !(entity instanceof Player));
		list.forEach(Entity::kill);
	}

	public ItemEntity spawnItem(Item item, float f, float g, float h) {
		ServerLevel serverLevel = this.getLevel();
		Vec3 vec3 = this.absoluteVec(new Vec3((double)f, (double)g, (double)h));
		ItemEntity itemEntity = new ItemEntity(serverLevel, vec3.x, vec3.y, vec3.z, new ItemStack(item, 1));
		itemEntity.setDeltaMovement(0.0, 0.0, 0.0);
		serverLevel.addFreshEntity(itemEntity);
		return itemEntity;
	}

	public ItemEntity spawnItem(Item item, BlockPos blockPos) {
		return this.spawnItem(item, (float)blockPos.getX(), (float)blockPos.getY(), (float)blockPos.getZ());
	}

	public <E extends Entity> E spawn(EntityType<E> entityType, BlockPos blockPos) {
		return this.spawn(entityType, Vec3.atBottomCenterOf(blockPos));
	}

	public <E extends Entity> E spawn(EntityType<E> entityType, Vec3 vec3) {
		ServerLevel serverLevel = this.getLevel();
		E entity = entityType.create(serverLevel);
		if (entity == null) {
			throw new NullPointerException("Failed to create entity " + entityType.builtInRegistryHolder().key().location());
		} else {
			if (entity instanceof Mob mob) {
				mob.setPersistenceRequired();
			}

			Vec3 vec32 = this.absoluteVec(vec3);
			entity.moveTo(vec32.x, vec32.y, vec32.z, entity.getYRot(), entity.getXRot());
			serverLevel.addFreshEntity(entity);
			return entity;
		}
	}

	public <E extends Entity> E spawn(EntityType<E> entityType, int i, int j, int k) {
		return this.spawn(entityType, new BlockPos(i, j, k));
	}

	public <E extends Entity> E spawn(EntityType<E> entityType, float f, float g, float h) {
		return this.spawn(entityType, new Vec3((double)f, (double)g, (double)h));
	}

	public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> entityType, BlockPos blockPos) {
		E mob = (E)this.spawn(entityType, blockPos);
		mob.removeFreeWill();
		return mob;
	}

	public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> entityType, int i, int j, int k) {
		return this.spawnWithNoFreeWill(entityType, new BlockPos(i, j, k));
	}

	public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> entityType, Vec3 vec3) {
		E mob = (E)this.spawn(entityType, vec3);
		mob.removeFreeWill();
		return mob;
	}

	public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> entityType, float f, float g, float h) {
		return this.spawnWithNoFreeWill(entityType, new Vec3((double)f, (double)g, (double)h));
	}

	public GameTestSequence walkTo(Mob mob, BlockPos blockPos, float f) {
		return this.startSequence().thenExecuteAfter(2, () -> {
			Path path = mob.getNavigation().createPath(this.absolutePos(blockPos), 0);
			mob.getNavigation().moveTo(path, (double)f);
		});
	}

	public void pressButton(int i, int j, int k) {
		this.pressButton(new BlockPos(i, j, k));
	}

	public void pressButton(BlockPos blockPos) {
		this.assertBlockState(blockPos, blockStatex -> blockStatex.is(BlockTags.BUTTONS), () -> "Expected button");
		BlockPos blockPos2 = this.absolutePos(blockPos);
		BlockState blockState = this.getLevel().getBlockState(blockPos2);
		ButtonBlock buttonBlock = (ButtonBlock)blockState.getBlock();
		buttonBlock.press(blockState, this.getLevel(), blockPos2);
	}

	public void useBlock(BlockPos blockPos) {
		this.useBlock(blockPos, this.makeMockPlayer());
	}

	public void useBlock(BlockPos blockPos, Player player) {
		BlockPos blockPos2 = this.absolutePos(blockPos);
		this.useBlock(blockPos, player, new BlockHitResult(Vec3.atCenterOf(blockPos2), Direction.NORTH, blockPos2, true));
	}

	public void useBlock(BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		BlockPos blockPos2 = this.absolutePos(blockPos);
		BlockState blockState = this.getLevel().getBlockState(blockPos2);
		InteractionResult interactionResult = blockState.use(this.getLevel(), player, InteractionHand.MAIN_HAND, blockHitResult);
		if (!interactionResult.consumesAction()) {
			UseOnContext useOnContext = new UseOnContext(player, InteractionHand.MAIN_HAND, blockHitResult);
			player.getItemInHand(InteractionHand.MAIN_HAND).useOn(useOnContext);
		}
	}

	public LivingEntity makeAboutToDrown(LivingEntity livingEntity) {
		livingEntity.setAirSupply(0);
		livingEntity.setHealth(0.25F);
		return livingEntity;
	}

	public Player makeMockSurvivalPlayer() {
		return new Player(this.getLevel(), BlockPos.ZERO, 0.0F, new GameProfile(UUID.randomUUID(), "test-mock-player")) {
			@Override
			public boolean isSpectator() {
				return false;
			}

			@Override
			public boolean isCreative() {
				return false;
			}
		};
	}

	public LivingEntity withLowHealth(LivingEntity livingEntity) {
		livingEntity.setHealth(0.25F);
		return livingEntity;
	}

	public Player makeMockPlayer() {
		return new Player(this.getLevel(), BlockPos.ZERO, 0.0F, new GameProfile(UUID.randomUUID(), "test-mock-player")) {
			@Override
			public boolean isSpectator() {
				return false;
			}

			@Override
			public boolean isCreative() {
				return true;
			}

			@Override
			public boolean isLocalPlayer() {
				return true;
			}
		};
	}

	public ServerPlayer makeMockServerPlayerInLevel() {
		ServerPlayer serverPlayer = new ServerPlayer(this.getLevel().getServer(), this.getLevel(), new GameProfile(UUID.randomUUID(), "test-mock-player")) {
			@Override
			public boolean isSpectator() {
				return false;
			}

			@Override
			public boolean isCreative() {
				return true;
			}
		};
		this.getLevel().getServer().getPlayerList().placeNewPlayer(new Connection(PacketFlow.SERVERBOUND), serverPlayer);
		return serverPlayer;
	}

	public void pullLever(int i, int j, int k) {
		this.pullLever(new BlockPos(i, j, k));
	}

	public void pullLever(BlockPos blockPos) {
		this.assertBlockPresent(Blocks.LEVER, blockPos);
		BlockPos blockPos2 = this.absolutePos(blockPos);
		BlockState blockState = this.getLevel().getBlockState(blockPos2);
		LeverBlock leverBlock = (LeverBlock)blockState.getBlock();
		leverBlock.pull(blockState, this.getLevel(), blockPos2);
	}

	public void pulseRedstone(BlockPos blockPos, long l) {
		this.setBlock(blockPos, Blocks.REDSTONE_BLOCK);
		this.runAfterDelay(l, () -> this.setBlock(blockPos, Blocks.AIR));
	}

	public void destroyBlock(BlockPos blockPos) {
		this.getLevel().destroyBlock(this.absolutePos(blockPos), false, null);
	}

	public void setBlock(int i, int j, int k, Block block) {
		this.setBlock(new BlockPos(i, j, k), block);
	}

	public void setBlock(int i, int j, int k, BlockState blockState) {
		this.setBlock(new BlockPos(i, j, k), blockState);
	}

	public void setBlock(BlockPos blockPos, Block block) {
		this.setBlock(blockPos, block.defaultBlockState());
	}

	public void setBlock(BlockPos blockPos, BlockState blockState) {
		this.getLevel().setBlock(this.absolutePos(blockPos), blockState, 3);
	}

	public void setNight() {
		this.setDayTime(13000);
	}

	public void setDayTime(int i) {
		this.getLevel().setDayTime((long)i);
	}

	public void assertBlockPresent(Block block, int i, int j, int k) {
		this.assertBlockPresent(block, new BlockPos(i, j, k));
	}

	public void assertBlockPresent(Block block, BlockPos blockPos) {
		BlockState blockState = this.getBlockState(blockPos);
		this.assertBlock(blockPos, block2 -> blockState.is(block), "Expected " + block.getName().getString() + ", got " + blockState.getBlock().getName().getString());
	}

	public void assertBlockNotPresent(Block block, int i, int j, int k) {
		this.assertBlockNotPresent(block, new BlockPos(i, j, k));
	}

	public void assertBlockNotPresent(Block block, BlockPos blockPos) {
		this.assertBlock(blockPos, block2 -> !this.getBlockState(blockPos).is(block), "Did not expect " + block.getName().getString());
	}

	public void succeedWhenBlockPresent(Block block, int i, int j, int k) {
		this.succeedWhenBlockPresent(block, new BlockPos(i, j, k));
	}

	public void succeedWhenBlockPresent(Block block, BlockPos blockPos) {
		this.succeedWhen(() -> this.assertBlockPresent(block, blockPos));
	}

	public void assertBlock(BlockPos blockPos, Predicate<Block> predicate, String string) {
		this.assertBlock(blockPos, predicate, (Supplier<String>)(() -> string));
	}

	public void assertBlock(BlockPos blockPos, Predicate<Block> predicate, Supplier<String> supplier) {
		this.assertBlockState(blockPos, blockState -> predicate.test(blockState.getBlock()), supplier);
	}

	public <T extends Comparable<T>> void assertBlockProperty(BlockPos blockPos, Property<T> property, T comparable) {
		BlockState blockState = this.getBlockState(blockPos);
		boolean bl = blockState.hasProperty(property);
		if (!bl || !blockState.getValue(property).equals(comparable)) {
			String string = bl ? "was " + blockState.getValue(property) : "property " + property.getName() + " is missing";
			String string2 = String.format(Locale.ROOT, "Expected property %s to be %s, %s", property.getName(), comparable, string);
			throw new GameTestAssertPosException(string2, this.absolutePos(blockPos), blockPos, this.testInfo.getTick());
		}
	}

	public <T extends Comparable<T>> void assertBlockProperty(BlockPos blockPos, Property<T> property, Predicate<T> predicate, String string) {
		this.assertBlockState(blockPos, blockState -> {
			if (!blockState.hasProperty(property)) {
				return false;
			} else {
				T comparable = blockState.getValue(property);
				return predicate.test(comparable);
			}
		}, () -> string);
	}

	public void assertBlockState(BlockPos blockPos, Predicate<BlockState> predicate, Supplier<String> supplier) {
		BlockState blockState = this.getBlockState(blockPos);
		if (!predicate.test(blockState)) {
			throw new GameTestAssertPosException((String)supplier.get(), this.absolutePos(blockPos), blockPos, this.testInfo.getTick());
		}
	}

	public void assertRedstoneSignal(BlockPos blockPos, Direction direction, IntPredicate intPredicate, Supplier<String> supplier) {
		BlockPos blockPos2 = this.absolutePos(blockPos);
		ServerLevel serverLevel = this.getLevel();
		BlockState blockState = serverLevel.getBlockState(blockPos2);
		int i = blockState.getSignal(serverLevel, blockPos2, direction);
		if (!intPredicate.test(i)) {
			throw new GameTestAssertPosException((String)supplier.get(), blockPos2, blockPos, this.testInfo.getTick());
		}
	}

	public void assertEntityPresent(EntityType<?> entityType) {
		List<? extends Entity> list = this.getLevel().getEntities(entityType, this.getBounds(), Entity::isAlive);
		if (list.isEmpty()) {
			throw new GameTestAssertException("Expected " + entityType.toShortString() + " to exist");
		}
	}

	public void assertEntityPresent(EntityType<?> entityType, int i, int j, int k) {
		this.assertEntityPresent(entityType, new BlockPos(i, j, k));
	}

	public void assertEntityPresent(EntityType<?> entityType, BlockPos blockPos) {
		BlockPos blockPos2 = this.absolutePos(blockPos);
		List<? extends Entity> list = this.getLevel().getEntities(entityType, new AABB(blockPos2), Entity::isAlive);
		if (list.isEmpty()) {
			throw new GameTestAssertPosException("Expected " + entityType.toShortString(), blockPos2, blockPos, this.testInfo.getTick());
		}
	}

	public void assertEntityPresent(EntityType<?> entityType, Vec3 vec3, Vec3 vec32) {
		List<? extends Entity> list = this.getLevel().getEntities(entityType, new AABB(vec3, vec32), Entity::isAlive);
		if (list.isEmpty()) {
			throw new GameTestAssertPosException(
				"Expected " + entityType.toShortString() + " between ", BlockPos.containing(vec3), BlockPos.containing(vec32), this.testInfo.getTick()
			);
		}
	}

	public void assertEntitiesPresent(EntityType<?> entityType, BlockPos blockPos, int i, double d) {
		BlockPos blockPos2 = this.absolutePos(blockPos);
		List<? extends Entity> list = this.getEntities((EntityType<? extends Entity>)entityType, blockPos, d);
		if (list.size() != i) {
			throw new GameTestAssertPosException(
				"Expected " + i + " entities of type " + entityType.toShortString() + ", actual number of entities found=" + list.size(),
				blockPos2,
				blockPos,
				this.testInfo.getTick()
			);
		}
	}

	public void assertEntityPresent(EntityType<?> entityType, BlockPos blockPos, double d) {
		List<? extends Entity> list = this.getEntities((EntityType<? extends Entity>)entityType, blockPos, d);
		if (list.isEmpty()) {
			BlockPos blockPos2 = this.absolutePos(blockPos);
			throw new GameTestAssertPosException("Expected " + entityType.toShortString(), blockPos2, blockPos, this.testInfo.getTick());
		}
	}

	public <T extends Entity> List<T> getEntities(EntityType<T> entityType, BlockPos blockPos, double d) {
		BlockPos blockPos2 = this.absolutePos(blockPos);
		return this.getLevel().getEntities(entityType, new AABB(blockPos2).inflate(d), Entity::isAlive);
	}

	public void assertEntityInstancePresent(Entity entity, int i, int j, int k) {
		this.assertEntityInstancePresent(entity, new BlockPos(i, j, k));
	}

	public void assertEntityInstancePresent(Entity entity, BlockPos blockPos) {
		BlockPos blockPos2 = this.absolutePos(blockPos);
		List<? extends Entity> list = this.getLevel().getEntities(entity.getType(), new AABB(blockPos2), Entity::isAlive);
		list.stream()
			.filter(entity2 -> entity2 == entity)
			.findFirst()
			.orElseThrow(() -> new GameTestAssertPosException("Expected " + entity.getType().toShortString(), blockPos2, blockPos, this.testInfo.getTick()));
	}

	public void assertItemEntityCountIs(Item item, BlockPos blockPos, double d, int i) {
		BlockPos blockPos2 = this.absolutePos(blockPos);
		List<ItemEntity> list = this.getLevel().getEntities(EntityType.ITEM, new AABB(blockPos2).inflate(d), Entity::isAlive);
		int j = 0;

		for (ItemEntity itemEntity : list) {
			ItemStack itemStack = itemEntity.getItem();
			if (itemStack.is(item)) {
				j += itemStack.getCount();
			}
		}

		if (j != i) {
			throw new GameTestAssertPosException(
				"Expected " + i + " " + item.getDescription().getString() + " items to exist (found " + j + ")", blockPos2, blockPos, this.testInfo.getTick()
			);
		}
	}

	public void assertItemEntityPresent(Item item, BlockPos blockPos, double d) {
		BlockPos blockPos2 = this.absolutePos(blockPos);

		for (Entity entity : this.getLevel().getEntities(EntityType.ITEM, new AABB(blockPos2).inflate(d), Entity::isAlive)) {
			ItemEntity itemEntity = (ItemEntity)entity;
			if (itemEntity.getItem().getItem().equals(item)) {
				return;
			}
		}

		throw new GameTestAssertPosException("Expected " + item.getDescription().getString() + " item", blockPos2, blockPos, this.testInfo.getTick());
	}

	public void assertItemEntityNotPresent(Item item, BlockPos blockPos, double d) {
		BlockPos blockPos2 = this.absolutePos(blockPos);

		for (Entity entity : this.getLevel().getEntities(EntityType.ITEM, new AABB(blockPos2).inflate(d), Entity::isAlive)) {
			ItemEntity itemEntity = (ItemEntity)entity;
			if (itemEntity.getItem().getItem().equals(item)) {
				throw new GameTestAssertPosException("Did not expect " + item.getDescription().getString() + " item", blockPos2, blockPos, this.testInfo.getTick());
			}
		}
	}

	public void assertEntityNotPresent(EntityType<?> entityType) {
		List<? extends Entity> list = this.getLevel().getEntities(entityType, this.getBounds(), Entity::isAlive);
		if (!list.isEmpty()) {
			throw new GameTestAssertException("Did not expect " + entityType.toShortString() + " to exist");
		}
	}

	public void assertEntityNotPresent(EntityType<?> entityType, int i, int j, int k) {
		this.assertEntityNotPresent(entityType, new BlockPos(i, j, k));
	}

	public void assertEntityNotPresent(EntityType<?> entityType, BlockPos blockPos) {
		BlockPos blockPos2 = this.absolutePos(blockPos);
		List<? extends Entity> list = this.getLevel().getEntities(entityType, new AABB(blockPos2), Entity::isAlive);
		if (!list.isEmpty()) {
			throw new GameTestAssertPosException("Did not expect " + entityType.toShortString(), blockPos2, blockPos, this.testInfo.getTick());
		}
	}

	public void assertEntityTouching(EntityType<?> entityType, double d, double e, double f) {
		Vec3 vec3 = new Vec3(d, e, f);
		Vec3 vec32 = this.absoluteVec(vec3);
		Predicate<? super Entity> predicate = entity -> entity.getBoundingBox().intersects(vec32, vec32);
		List<? extends Entity> list = this.getLevel().getEntities(entityType, this.getBounds(), predicate);
		if (list.isEmpty()) {
			throw new GameTestAssertException("Expected " + entityType.toShortString() + " to touch " + vec32 + " (relative " + vec3 + ")");
		}
	}

	public void assertEntityNotTouching(EntityType<?> entityType, double d, double e, double f) {
		Vec3 vec3 = new Vec3(d, e, f);
		Vec3 vec32 = this.absoluteVec(vec3);
		Predicate<? super Entity> predicate = entity -> !entity.getBoundingBox().intersects(vec32, vec32);
		List<? extends Entity> list = this.getLevel().getEntities(entityType, this.getBounds(), predicate);
		if (list.isEmpty()) {
			throw new GameTestAssertException("Did not expect " + entityType.toShortString() + " to touch " + vec32 + " (relative " + vec3 + ")");
		}
	}

	public <E extends Entity, T> void assertEntityData(BlockPos blockPos, EntityType<E> entityType, Function<? super E, T> function, @Nullable T object) {
		BlockPos blockPos2 = this.absolutePos(blockPos);
		List<E> list = this.getLevel().getEntities(entityType, new AABB(blockPos2), Entity::isAlive);
		if (list.isEmpty()) {
			throw new GameTestAssertPosException("Expected " + entityType.toShortString(), blockPos2, blockPos, this.testInfo.getTick());
		} else {
			for (E entity : list) {
				T object2 = (T)function.apply(entity);
				if (object2 == null) {
					if (object != null) {
						throw new GameTestAssertException("Expected entity data to be: " + object + ", but was: " + object2);
					}
				} else if (!object2.equals(object)) {
					throw new GameTestAssertException("Expected entity data to be: " + object + ", but was: " + object2);
				}
			}
		}
	}

	public <E extends LivingEntity> void assertEntityIsHolding(BlockPos blockPos, EntityType<E> entityType, Item item) {
		BlockPos blockPos2 = this.absolutePos(blockPos);
		List<E> list = this.getLevel().getEntities(entityType, new AABB(blockPos2), Entity::isAlive);
		if (list.isEmpty()) {
			throw new GameTestAssertPosException("Expected entity of type: " + entityType, blockPos2, blockPos, this.getTick());
		} else {
			for (E livingEntity : list) {
				if (livingEntity.isHolding(item)) {
					return;
				}
			}

			throw new GameTestAssertPosException("Entity should be holding: " + item, blockPos2, blockPos, this.getTick());
		}
	}

	public <E extends Entity & InventoryCarrier> void assertEntityInventoryContains(BlockPos blockPos, EntityType<E> entityType, Item item) {
		BlockPos blockPos2 = this.absolutePos(blockPos);
		List<E> list = this.getLevel().getEntities(entityType, new AABB(blockPos2), object -> ((Entity)object).isAlive());
		if (list.isEmpty()) {
			throw new GameTestAssertPosException("Expected " + entityType.toShortString() + " to exist", blockPos2, blockPos, this.getTick());
		} else {
			for (E entity : list) {
				if (entity.getInventory().hasAnyMatching(itemStack -> itemStack.is(item))) {
					return;
				}
			}

			throw new GameTestAssertPosException("Entity inventory should contain: " + item, blockPos2, blockPos, this.getTick());
		}
	}

	public void assertContainerEmpty(BlockPos blockPos) {
		BlockPos blockPos2 = this.absolutePos(blockPos);
		BlockEntity blockEntity = this.getLevel().getBlockEntity(blockPos2);
		if (blockEntity instanceof BaseContainerBlockEntity && !((BaseContainerBlockEntity)blockEntity).isEmpty()) {
			throw new GameTestAssertException("Container should be empty");
		}
	}

	public void assertContainerContains(BlockPos blockPos, Item item) {
		BlockPos blockPos2 = this.absolutePos(blockPos);
		BlockEntity blockEntity = this.getLevel().getBlockEntity(blockPos2);
		if (!(blockEntity instanceof BaseContainerBlockEntity)) {
			throw new GameTestAssertException("Expected a container at " + blockPos + ", found " + BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType()));
		} else if (((BaseContainerBlockEntity)blockEntity).countItem(item) != 1) {
			throw new GameTestAssertException("Container should contain: " + item);
		}
	}

	public void assertSameBlockStates(BoundingBox boundingBox, BlockPos blockPos) {
		BlockPos.betweenClosedStream(boundingBox).forEach(blockPos2 -> {
			BlockPos blockPos3 = blockPos.offset(blockPos2.getX() - boundingBox.minX(), blockPos2.getY() - boundingBox.minY(), blockPos2.getZ() - boundingBox.minZ());
			this.assertSameBlockState(blockPos2, blockPos3);
		});
	}

	public void assertSameBlockState(BlockPos blockPos, BlockPos blockPos2) {
		BlockState blockState = this.getBlockState(blockPos);
		BlockState blockState2 = this.getBlockState(blockPos2);
		if (blockState != blockState2) {
			this.fail("Incorrect state. Expected " + blockState2 + ", got " + blockState, blockPos);
		}
	}

	public void assertAtTickTimeContainerContains(long l, BlockPos blockPos, Item item) {
		this.runAtTickTime(l, () -> this.assertContainerContains(blockPos, item));
	}

	public void assertAtTickTimeContainerEmpty(long l, BlockPos blockPos) {
		this.runAtTickTime(l, () -> this.assertContainerEmpty(blockPos));
	}

	public <E extends Entity, T> void succeedWhenEntityData(BlockPos blockPos, EntityType<E> entityType, Function<E, T> function, T object) {
		this.succeedWhen(() -> this.assertEntityData(blockPos, entityType, function, object));
	}

	public <E extends Entity> void assertEntityProperty(E entity, Predicate<E> predicate, String string) {
		if (!predicate.test(entity)) {
			throw new GameTestAssertException("Entity " + entity + " failed " + string + " test");
		}
	}

	public <E extends Entity, T> void assertEntityProperty(E entity, Function<E, T> function, String string, T object) {
		T object2 = (T)function.apply(entity);
		if (!object2.equals(object)) {
			throw new GameTestAssertException("Entity " + entity + " value " + string + "=" + object2 + " is not equal to expected " + object);
		}
	}

	public void succeedWhenEntityPresent(EntityType<?> entityType, int i, int j, int k) {
		this.succeedWhenEntityPresent(entityType, new BlockPos(i, j, k));
	}

	public void succeedWhenEntityPresent(EntityType<?> entityType, BlockPos blockPos) {
		this.succeedWhen(() -> this.assertEntityPresent(entityType, blockPos));
	}

	public void succeedWhenEntityNotPresent(EntityType<?> entityType, int i, int j, int k) {
		this.succeedWhenEntityNotPresent(entityType, new BlockPos(i, j, k));
	}

	public void succeedWhenEntityNotPresent(EntityType<?> entityType, BlockPos blockPos) {
		this.succeedWhen(() -> this.assertEntityNotPresent(entityType, blockPos));
	}

	public void succeed() {
		this.testInfo.succeed();
	}

	private void ensureSingleFinalCheck() {
		if (this.finalCheckAdded) {
			throw new IllegalStateException("This test already has final clause");
		} else {
			this.finalCheckAdded = true;
		}
	}

	public void succeedIf(Runnable runnable) {
		this.ensureSingleFinalCheck();
		this.testInfo.createSequence().thenWaitUntil(0L, runnable).thenSucceed();
	}

	public void succeedWhen(Runnable runnable) {
		this.ensureSingleFinalCheck();
		this.testInfo.createSequence().thenWaitUntil(runnable).thenSucceed();
	}

	public void succeedOnTickWhen(int i, Runnable runnable) {
		this.ensureSingleFinalCheck();
		this.testInfo.createSequence().thenWaitUntil((long)i, runnable).thenSucceed();
	}

	public void runAtTickTime(long l, Runnable runnable) {
		this.testInfo.setRunAtTickTime(l, runnable);
	}

	public void runAfterDelay(long l, Runnable runnable) {
		this.runAtTickTime(this.testInfo.getTick() + l, runnable);
	}

	public void randomTick(BlockPos blockPos) {
		BlockPos blockPos2 = this.absolutePos(blockPos);
		ServerLevel serverLevel = this.getLevel();
		serverLevel.getBlockState(blockPos2).randomTick(serverLevel, blockPos2, serverLevel.random);
	}

	public int getHeight(Heightmap.Types types, int i, int j) {
		BlockPos blockPos = this.absolutePos(new BlockPos(i, 0, j));
		return this.relativePos(this.getLevel().getHeightmapPos(types, blockPos)).getY();
	}

	public void fail(String string, BlockPos blockPos) {
		throw new GameTestAssertPosException(string, this.absolutePos(blockPos), blockPos, this.getTick());
	}

	public void fail(String string, Entity entity) {
		throw new GameTestAssertPosException(string, entity.blockPosition(), this.relativePos(entity.blockPosition()), this.getTick());
	}

	public void fail(String string) {
		throw new GameTestAssertException(string);
	}

	public void failIf(Runnable runnable) {
		this.testInfo.createSequence().thenWaitUntil(runnable).thenFail(() -> new GameTestAssertException("Fail conditions met"));
	}

	public void failIfEver(Runnable runnable) {
		LongStream.range(this.testInfo.getTick(), (long)this.testInfo.getTimeoutTicks()).forEach(l -> this.testInfo.setRunAtTickTime(l, runnable::run));
	}

	public GameTestSequence startSequence() {
		return this.testInfo.createSequence();
	}

	public BlockPos absolutePos(BlockPos blockPos) {
		BlockPos blockPos2 = this.testInfo.getStructureBlockPos();
		BlockPos blockPos3 = blockPos2.offset(blockPos);
		return StructureTemplate.transform(blockPos3, Mirror.NONE, this.testInfo.getRotation(), blockPos2);
	}

	public BlockPos relativePos(BlockPos blockPos) {
		BlockPos blockPos2 = this.testInfo.getStructureBlockPos();
		Rotation rotation = this.testInfo.getRotation().getRotated(Rotation.CLOCKWISE_180);
		BlockPos blockPos3 = StructureTemplate.transform(blockPos, Mirror.NONE, rotation, blockPos2);
		return blockPos3.subtract(blockPos2);
	}

	public Vec3 absoluteVec(Vec3 vec3) {
		Vec3 vec32 = Vec3.atLowerCornerOf(this.testInfo.getStructureBlockPos());
		return StructureTemplate.transform(vec32.add(vec3), Mirror.NONE, this.testInfo.getRotation(), this.testInfo.getStructureBlockPos());
	}

	public Vec3 relativeVec(Vec3 vec3) {
		Vec3 vec32 = Vec3.atLowerCornerOf(this.testInfo.getStructureBlockPos());
		return StructureTemplate.transform(vec3.subtract(vec32), Mirror.NONE, this.testInfo.getRotation(), this.testInfo.getStructureBlockPos());
	}

	public void assertTrue(boolean bl, String string) {
		if (!bl) {
			throw new GameTestAssertException(string);
		}
	}

	public void assertFalse(boolean bl, String string) {
		if (bl) {
			throw new GameTestAssertException(string);
		}
	}

	public long getTick() {
		return this.testInfo.getTick();
	}

	private AABB getBounds() {
		return this.testInfo.getStructureBounds();
	}

	private AABB getRelativeBounds() {
		AABB aABB = this.testInfo.getStructureBounds();
		return aABB.move(BlockPos.ZERO.subtract(this.absolutePos(BlockPos.ZERO)));
	}

	public void forEveryBlockInStructure(Consumer<BlockPos> consumer) {
		AABB aABB = this.getRelativeBounds();
		BlockPos.MutableBlockPos.betweenClosedStream(aABB.move(0.0, 1.0, 0.0)).forEach(consumer);
	}

	public void onEachTick(Runnable runnable) {
		LongStream.range(this.testInfo.getTick(), (long)this.testInfo.getTimeoutTicks()).forEach(l -> this.testInfo.setRunAtTickTime(l, runnable::run));
	}

	public void placeAt(Player player, ItemStack itemStack, BlockPos blockPos, Direction direction) {
		BlockPos blockPos2 = this.absolutePos(blockPos.relative(direction));
		BlockHitResult blockHitResult = new BlockHitResult(Vec3.atCenterOf(blockPos2), direction, blockPos2, false);
		UseOnContext useOnContext = new UseOnContext(player, InteractionHand.MAIN_HAND, blockHitResult);
		itemStack.useOn(useOnContext);
	}
}
