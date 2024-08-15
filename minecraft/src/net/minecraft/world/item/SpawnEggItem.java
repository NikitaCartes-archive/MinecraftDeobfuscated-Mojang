package net.minecraft.world.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SpawnEggItem extends Item {
	private static final Map<EntityType<? extends Mob>, SpawnEggItem> BY_ID = Maps.<EntityType<? extends Mob>, SpawnEggItem>newIdentityHashMap();
	private static final MapCodec<EntityType<?>> ENTITY_TYPE_FIELD_CODEC = BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("id");
	private final int backgroundColor;
	private final int highlightColor;
	private final EntityType<?> defaultType;

	public SpawnEggItem(EntityType<? extends Mob> entityType, int i, int j, Item.Properties properties) {
		super(properties);
		this.defaultType = entityType;
		this.backgroundColor = i;
		this.highlightColor = j;
		BY_ID.put(entityType, this);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			ItemStack itemStack = useOnContext.getItemInHand();
			BlockPos blockPos = useOnContext.getClickedPos();
			Direction direction = useOnContext.getClickedFace();
			BlockState blockState = level.getBlockState(blockPos);
			if (level.getBlockEntity(blockPos) instanceof Spawner spawner) {
				EntityType<?> entityType = this.getType(itemStack);
				spawner.setEntityId(entityType, level.getRandom());
				level.sendBlockUpdated(blockPos, blockState, blockState, 3);
				level.gameEvent(useOnContext.getPlayer(), GameEvent.BLOCK_CHANGE, blockPos);
				itemStack.shrink(1);
				return InteractionResult.SUCCESS;
			} else {
				BlockPos blockPos2;
				if (blockState.getCollisionShape(level, blockPos).isEmpty()) {
					blockPos2 = blockPos;
				} else {
					blockPos2 = blockPos.relative(direction);
				}

				EntityType<?> entityType = this.getType(itemStack);
				if (entityType.spawn(
						(ServerLevel)level,
						itemStack,
						useOnContext.getPlayer(),
						blockPos2,
						EntitySpawnReason.SPAWN_EGG,
						true,
						!Objects.equals(blockPos, blockPos2) && direction == Direction.UP
					)
					!= null) {
					itemStack.shrink(1);
					level.gameEvent(useOnContext.getPlayer(), GameEvent.ENTITY_PLACE, blockPos);
				}

				return InteractionResult.SUCCESS;
			}
		}
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		BlockHitResult blockHitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
		if (blockHitResult.getType() != HitResult.Type.BLOCK) {
			return InteractionResult.PASS;
		} else if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			BlockPos blockPos = blockHitResult.getBlockPos();
			if (!(level.getBlockState(blockPos).getBlock() instanceof LiquidBlock)) {
				return InteractionResult.PASS;
			} else if (level.mayInteract(player, blockPos) && player.mayUseItemAt(blockPos, blockHitResult.getDirection(), itemStack)) {
				EntityType<?> entityType = this.getType(itemStack);
				Entity entity = entityType.spawn((ServerLevel)level, itemStack, player, blockPos, EntitySpawnReason.SPAWN_EGG, false, false);
				if (entity == null) {
					return InteractionResult.PASS;
				} else {
					itemStack.consume(1, player);
					player.awardStat(Stats.ITEM_USED.get(this));
					level.gameEvent(player, GameEvent.ENTITY_PLACE, entity.position());
					return InteractionResult.SUCCESS;
				}
			} else {
				return InteractionResult.FAIL;
			}
		}
	}

	public boolean spawnsEntity(ItemStack itemStack, EntityType<?> entityType) {
		return Objects.equals(this.getType(itemStack), entityType);
	}

	public int getColor(int i) {
		return i == 0 ? this.backgroundColor : this.highlightColor;
	}

	@Nullable
	public static SpawnEggItem byId(@Nullable EntityType<?> entityType) {
		return (SpawnEggItem)BY_ID.get(entityType);
	}

	public static Iterable<SpawnEggItem> eggs() {
		return Iterables.unmodifiableIterable(BY_ID.values());
	}

	public EntityType<?> getType(ItemStack itemStack) {
		CustomData customData = itemStack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
		return !customData.isEmpty() ? (EntityType)customData.read(ENTITY_TYPE_FIELD_CODEC).result().orElse(this.defaultType) : this.defaultType;
	}

	@Override
	public FeatureFlagSet requiredFeatures() {
		return this.defaultType.requiredFeatures();
	}

	public Optional<Mob> spawnOffspringFromSpawnEgg(
		Player player, Mob mob, EntityType<? extends Mob> entityType, ServerLevel serverLevel, Vec3 vec3, ItemStack itemStack
	) {
		if (!this.spawnsEntity(itemStack, entityType)) {
			return Optional.empty();
		} else {
			Mob mob2;
			if (mob instanceof AgeableMob) {
				mob2 = ((AgeableMob)mob).getBreedOffspring(serverLevel, (AgeableMob)mob);
			} else {
				mob2 = entityType.create(serverLevel, EntitySpawnReason.SPAWN_EGG);
			}

			if (mob2 == null) {
				return Optional.empty();
			} else {
				mob2.setBaby(true);
				if (!mob2.isBaby()) {
					return Optional.empty();
				} else {
					mob2.moveTo(vec3.x(), vec3.y(), vec3.z(), 0.0F, 0.0F);
					serverLevel.addFreshEntityWithPassengers(mob2);
					mob2.setCustomName(itemStack.get(DataComponents.CUSTOM_NAME));
					itemStack.consume(1, player);
					return Optional.of(mob2);
				}
			}
		}
	}
}
