package net.minecraft.world.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class SpawnEggItem extends Item {
	private static final Map<EntityType<?>, SpawnEggItem> BY_ID = Maps.<EntityType<?>, SpawnEggItem>newIdentityHashMap();
	private final int color1;
	private final int color2;
	private final EntityType<?> defaultType;

	public SpawnEggItem(EntityType<?> entityType, int i, int j, Item.Properties properties) {
		super(properties);
		this.defaultType = entityType;
		this.color1 = i;
		this.color2 = j;
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
			Block block = blockState.getBlock();
			if (block == Blocks.SPAWNER) {
				BlockEntity blockEntity = level.getBlockEntity(blockPos);
				if (blockEntity instanceof SpawnerBlockEntity) {
					BaseSpawner baseSpawner = ((SpawnerBlockEntity)blockEntity).getSpawner();
					EntityType<?> entityType = this.getType(itemStack.getTag());
					baseSpawner.setEntityId(entityType);
					blockEntity.setChanged();
					level.sendBlockUpdated(blockPos, blockState, blockState, 3);
					itemStack.shrink(1);
					return InteractionResult.SUCCESS;
				}
			}

			BlockPos blockPos2;
			if (blockState.getCollisionShape(level, blockPos).isEmpty()) {
				blockPos2 = blockPos;
			} else {
				blockPos2 = blockPos.relative(direction);
			}

			EntityType<?> entityType2 = this.getType(itemStack.getTag());
			if (entityType2.spawn(
					level, itemStack, useOnContext.getPlayer(), blockPos2, MobSpawnType.SPAWN_EGG, true, !Objects.equals(blockPos, blockPos2) && direction == Direction.UP
				)
				!= null) {
				itemStack.shrink(1);
			}

			return InteractionResult.SUCCESS;
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		HitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
		if (hitResult.getType() != HitResult.Type.BLOCK) {
			return InteractionResultHolder.pass(itemStack);
		} else if (level.isClientSide) {
			return InteractionResultHolder.success(itemStack);
		} else {
			BlockHitResult blockHitResult = (BlockHitResult)hitResult;
			BlockPos blockPos = blockHitResult.getBlockPos();
			if (!(level.getBlockState(blockPos).getBlock() instanceof LiquidBlock)) {
				return InteractionResultHolder.pass(itemStack);
			} else if (level.mayInteract(player, blockPos) && player.mayUseItemAt(blockPos, blockHitResult.getDirection(), itemStack)) {
				EntityType<?> entityType = this.getType(itemStack.getTag());
				if (entityType.spawn(level, itemStack, player, blockPos, MobSpawnType.SPAWN_EGG, false, false) == null) {
					return InteractionResultHolder.pass(itemStack);
				} else {
					if (!player.abilities.instabuild) {
						itemStack.shrink(1);
					}

					player.awardStat(Stats.ITEM_USED.get(this));
					return InteractionResultHolder.success(itemStack);
				}
			} else {
				return InteractionResultHolder.fail(itemStack);
			}
		}
	}

	public boolean spawnsEntity(@Nullable CompoundTag compoundTag, EntityType<?> entityType) {
		return Objects.equals(this.getType(compoundTag), entityType);
	}

	@Environment(EnvType.CLIENT)
	public int getColor(int i) {
		return i == 0 ? this.color1 : this.color2;
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public static SpawnEggItem byId(@Nullable EntityType<?> entityType) {
		return (SpawnEggItem)BY_ID.get(entityType);
	}

	public static Iterable<SpawnEggItem> eggs() {
		return Iterables.unmodifiableIterable(BY_ID.values());
	}

	public EntityType<?> getType(@Nullable CompoundTag compoundTag) {
		if (compoundTag != null && compoundTag.contains("EntityTag", 10)) {
			CompoundTag compoundTag2 = compoundTag.getCompound("EntityTag");
			if (compoundTag2.contains("id", 8)) {
				return (EntityType<?>)EntityType.byString(compoundTag2.getString("id")).orElse(this.defaultType);
			}
		}

		return this.defaultType;
	}
}
