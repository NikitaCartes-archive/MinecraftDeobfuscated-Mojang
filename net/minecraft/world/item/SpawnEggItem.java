/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SpawnEggItem
extends Item {
    private static final Map<EntityType<? extends Mob>, SpawnEggItem> BY_ID = Maps.newIdentityHashMap();
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
        BlockEntity blockEntity;
        Level level = useOnContext.getLevel();
        if (!(level instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack itemStack = useOnContext.getItemInHand();
        BlockPos blockPos = useOnContext.getClickedPos();
        Direction direction = useOnContext.getClickedFace();
        BlockState blockState = level.getBlockState(blockPos);
        if (blockState.is(Blocks.SPAWNER) && (blockEntity = level.getBlockEntity(blockPos)) instanceof SpawnerBlockEntity) {
            BaseSpawner baseSpawner = ((SpawnerBlockEntity)blockEntity).getSpawner();
            EntityType<?> entityType = this.getType(itemStack.getTag());
            baseSpawner.setEntityId(entityType);
            blockEntity.setChanged();
            level.sendBlockUpdated(blockPos, blockState, blockState, 3);
            itemStack.shrink(1);
            return InteractionResult.CONSUME;
        }
        BlockPos blockPos2 = blockState.getCollisionShape(level, blockPos).isEmpty() ? blockPos : blockPos.relative(direction);
        EntityType<?> entityType2 = this.getType(itemStack.getTag());
        if (entityType2.spawn((ServerLevel)level, itemStack, useOnContext.getPlayer(), blockPos2, MobSpawnType.SPAWN_EGG, true, !Objects.equals(blockPos, blockPos2) && direction == Direction.UP) != null) {
            itemStack.shrink(1);
            level.gameEvent((Entity)useOnContext.getPlayer(), GameEvent.ENTITY_PLACE, blockPos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        BlockHitResult hitResult = SpawnEggItem.getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (((HitResult)hitResult).getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemStack);
        }
        if (!(level instanceof ServerLevel)) {
            return InteractionResultHolder.success(itemStack);
        }
        BlockHitResult blockHitResult = hitResult;
        BlockPos blockPos = blockHitResult.getBlockPos();
        if (!(level.getBlockState(blockPos).getBlock() instanceof LiquidBlock)) {
            return InteractionResultHolder.pass(itemStack);
        }
        if (!level.mayInteract(player, blockPos) || !player.mayUseItemAt(blockPos, blockHitResult.getDirection(), itemStack)) {
            return InteractionResultHolder.fail(itemStack);
        }
        EntityType<?> entityType = this.getType(itemStack.getTag());
        if (entityType.spawn((ServerLevel)level, itemStack, player, blockPos, MobSpawnType.SPAWN_EGG, false, false) == null) {
            return InteractionResultHolder.pass(itemStack);
        }
        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        level.gameEvent(GameEvent.ENTITY_PLACE, player);
        return InteractionResultHolder.consume(itemStack);
    }

    public boolean spawnsEntity(@Nullable CompoundTag compoundTag, EntityType<?> entityType) {
        return Objects.equals(this.getType(compoundTag), entityType);
    }

    public int getColor(int i) {
        return i == 0 ? this.backgroundColor : this.highlightColor;
    }

    @Nullable
    public static SpawnEggItem byId(@Nullable EntityType<?> entityType) {
        return BY_ID.get(entityType);
    }

    public static Iterable<SpawnEggItem> eggs() {
        return Iterables.unmodifiableIterable(BY_ID.values());
    }

    public EntityType<?> getType(@Nullable CompoundTag compoundTag) {
        CompoundTag compoundTag2;
        if (compoundTag != null && compoundTag.contains("EntityTag", 10) && (compoundTag2 = compoundTag.getCompound("EntityTag")).contains("id", 8)) {
            return EntityType.byString(compoundTag2.getString("id")).orElse(this.defaultType);
        }
        return this.defaultType;
    }

    public Optional<Mob> spawnOffspringFromSpawnEgg(Player player, Mob mob, EntityType<? extends Mob> entityType, ServerLevel serverLevel, Vec3 vec3, ItemStack itemStack) {
        if (!this.spawnsEntity(itemStack.getTag(), entityType)) {
            return Optional.empty();
        }
        Mob mob2 = mob instanceof AgeableMob ? ((AgeableMob)mob).getBreedOffspring(serverLevel, (AgeableMob)mob) : entityType.create(serverLevel);
        if (mob2 == null) {
            return Optional.empty();
        }
        mob2.setBaby(true);
        if (!mob2.isBaby()) {
            return Optional.empty();
        }
        mob2.moveTo(vec3.x(), vec3.y(), vec3.z(), 0.0f, 0.0f);
        serverLevel.addFreshEntityWithPassengers(mob2);
        if (itemStack.hasCustomHoverName()) {
            mob2.setCustomName(itemStack.getHoverName());
        }
        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }
        return Optional.of(mob2);
    }
}

