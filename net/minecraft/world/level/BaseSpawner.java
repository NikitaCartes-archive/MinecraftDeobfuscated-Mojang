/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import com.mojang.logging.LogUtils;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class BaseSpawner {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int EVENT_SPAWN = 1;
    private int spawnDelay = 20;
    private SimpleWeightedRandomList<SpawnData> spawnPotentials = SimpleWeightedRandomList.empty();
    private SpawnData nextSpawnData = new SpawnData();
    private double spin;
    private double oSpin;
    private int minSpawnDelay = 200;
    private int maxSpawnDelay = 800;
    private int spawnCount = 4;
    @Nullable
    private Entity displayEntity;
    private int maxNearbyEntities = 6;
    private int requiredPlayerRange = 16;
    private int spawnRange = 4;

    public void setEntityId(EntityType<?> entityType) {
        this.nextSpawnData.getEntityToSpawn().putString("id", Registry.ENTITY_TYPE.getKey(entityType).toString());
    }

    private boolean isNearPlayer(Level level, BlockPos blockPos) {
        return level.hasNearbyAlivePlayer((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, this.requiredPlayerRange);
    }

    public void clientTick(Level level, BlockPos blockPos) {
        if (!this.isNearPlayer(level, blockPos)) {
            this.oSpin = this.spin;
        } else {
            RandomSource randomSource = level.getRandom();
            double d = (double)blockPos.getX() + randomSource.nextDouble();
            double e = (double)blockPos.getY() + randomSource.nextDouble();
            double f = (double)blockPos.getZ() + randomSource.nextDouble();
            level.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0);
            level.addParticle(ParticleTypes.FLAME, d, e, f, 0.0, 0.0, 0.0);
            if (this.spawnDelay > 0) {
                --this.spawnDelay;
            }
            this.oSpin = this.spin;
            this.spin = (this.spin + (double)(1000.0f / ((float)this.spawnDelay + 200.0f))) % 360.0;
        }
    }

    public void serverTick(ServerLevel serverLevel, BlockPos blockPos) {
        if (!this.isNearPlayer(serverLevel, blockPos)) {
            return;
        }
        if (this.spawnDelay == -1) {
            this.delay(serverLevel, blockPos);
        }
        if (this.spawnDelay > 0) {
            --this.spawnDelay;
            return;
        }
        boolean bl = false;
        for (int i = 0; i < this.spawnCount; ++i) {
            SpawnData.CustomSpawnRules customSpawnRules;
            double f;
            CompoundTag compoundTag = this.nextSpawnData.getEntityToSpawn();
            Optional<EntityType<?>> optional = EntityType.by(compoundTag);
            if (optional.isEmpty()) {
                this.delay(serverLevel, blockPos);
                return;
            }
            ListTag listTag = compoundTag.getList("Pos", 6);
            int j = listTag.size();
            RandomSource randomSource = serverLevel.getRandom();
            double d = j >= 1 ? listTag.getDouble(0) : (double)blockPos.getX() + (randomSource.nextDouble() - randomSource.nextDouble()) * (double)this.spawnRange + 0.5;
            double e = j >= 2 ? listTag.getDouble(1) : (double)(blockPos.getY() + randomSource.nextInt(3) - 1);
            double d2 = f = j >= 3 ? listTag.getDouble(2) : (double)blockPos.getZ() + (randomSource.nextDouble() - randomSource.nextDouble()) * (double)this.spawnRange + 0.5;
            if (!serverLevel.noCollision(optional.get().getAABB(d, e, f))) continue;
            BlockPos blockPos2 = new BlockPos(d, e, f);
            if (!this.nextSpawnData.getCustomSpawnRules().isPresent() ? !SpawnPlacements.checkSpawnRules(optional.get(), serverLevel, MobSpawnType.SPAWNER, blockPos2, serverLevel.getRandom()) : !optional.get().getCategory().isFriendly() && serverLevel.getDifficulty() == Difficulty.PEACEFUL || !(customSpawnRules = this.nextSpawnData.getCustomSpawnRules().get()).blockLightLimit().isValueInRange(serverLevel.getBrightness(LightLayer.BLOCK, blockPos2)) || !customSpawnRules.skyLightLimit().isValueInRange(serverLevel.getBrightness(LightLayer.SKY, blockPos2))) continue;
            Entity entity2 = EntityType.loadEntityRecursive(compoundTag, serverLevel, entity -> {
                entity.moveTo(d, e, f, entity.getYRot(), entity.getXRot());
                return entity;
            });
            if (entity2 == null) {
                this.delay(serverLevel, blockPos);
                return;
            }
            int k = serverLevel.getEntitiesOfClass(entity2.getClass(), new AABB(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1).inflate(this.spawnRange)).size();
            if (k >= this.maxNearbyEntities) {
                this.delay(serverLevel, blockPos);
                return;
            }
            entity2.moveTo(entity2.getX(), entity2.getY(), entity2.getZ(), randomSource.nextFloat() * 360.0f, 0.0f);
            if (entity2 instanceof Mob) {
                Mob mob = (Mob)entity2;
                if (this.nextSpawnData.getCustomSpawnRules().isEmpty() && !mob.checkSpawnRules(serverLevel, MobSpawnType.SPAWNER) || !mob.checkSpawnObstruction(serverLevel)) continue;
                if (this.nextSpawnData.getEntityToSpawn().size() == 1 && this.nextSpawnData.getEntityToSpawn().contains("id", 8)) {
                    ((Mob)entity2).finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(entity2.blockPosition()), MobSpawnType.SPAWNER, null, null);
                }
            }
            if (!serverLevel.tryAddFreshEntityWithPassengers(entity2)) {
                this.delay(serverLevel, blockPos);
                return;
            }
            serverLevel.levelEvent(2004, blockPos, 0);
            if (entity2 instanceof Mob) {
                ((Mob)entity2).spawnAnim();
            }
            bl = true;
        }
        if (bl) {
            this.delay(serverLevel, blockPos);
        }
    }

    private void delay(Level level, BlockPos blockPos) {
        RandomSource randomSource = level.random;
        this.spawnDelay = this.maxSpawnDelay <= this.minSpawnDelay ? this.minSpawnDelay : this.minSpawnDelay + randomSource.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
        this.spawnPotentials.getRandom(randomSource).ifPresent(wrapper -> this.setNextSpawnData(level, blockPos, (SpawnData)wrapper.getData()));
        this.broadcastEvent(level, blockPos, 1);
    }

    public void load(@Nullable Level level, BlockPos blockPos, CompoundTag compoundTag) {
        this.spawnDelay = compoundTag.getShort("Delay");
        boolean bl = compoundTag.contains("SpawnPotentials", 9);
        boolean bl2 = compoundTag.contains("SpawnData", 10);
        if (!bl) {
            SpawnData spawnData = bl2 ? SpawnData.CODEC.parse(NbtOps.INSTANCE, compoundTag.getCompound("SpawnData")).resultOrPartial(string -> LOGGER.warn("Invalid SpawnData: {}", string)).orElseGet(SpawnData::new) : new SpawnData();
            this.spawnPotentials = SimpleWeightedRandomList.single(spawnData);
            this.setNextSpawnData(level, blockPos, spawnData);
        } else {
            ListTag listTag = compoundTag.getList("SpawnPotentials", 10);
            this.spawnPotentials = SpawnData.LIST_CODEC.parse(NbtOps.INSTANCE, listTag).resultOrPartial(string -> LOGGER.warn("Invalid SpawnPotentials list: {}", string)).orElseGet(SimpleWeightedRandomList::empty);
            if (bl2) {
                SpawnData spawnData2 = SpawnData.CODEC.parse(NbtOps.INSTANCE, compoundTag.getCompound("SpawnData")).resultOrPartial(string -> LOGGER.warn("Invalid SpawnData: {}", string)).orElseGet(SpawnData::new);
                this.setNextSpawnData(level, blockPos, spawnData2);
            } else {
                this.spawnPotentials.getRandom(level.getRandom()).ifPresent(wrapper -> this.setNextSpawnData(level, blockPos, (SpawnData)wrapper.getData()));
            }
        }
        if (compoundTag.contains("MinSpawnDelay", 99)) {
            this.minSpawnDelay = compoundTag.getShort("MinSpawnDelay");
            this.maxSpawnDelay = compoundTag.getShort("MaxSpawnDelay");
            this.spawnCount = compoundTag.getShort("SpawnCount");
        }
        if (compoundTag.contains("MaxNearbyEntities", 99)) {
            this.maxNearbyEntities = compoundTag.getShort("MaxNearbyEntities");
            this.requiredPlayerRange = compoundTag.getShort("RequiredPlayerRange");
        }
        if (compoundTag.contains("SpawnRange", 99)) {
            this.spawnRange = compoundTag.getShort("SpawnRange");
        }
        this.displayEntity = null;
    }

    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putShort("Delay", (short)this.spawnDelay);
        compoundTag.putShort("MinSpawnDelay", (short)this.minSpawnDelay);
        compoundTag.putShort("MaxSpawnDelay", (short)this.maxSpawnDelay);
        compoundTag.putShort("SpawnCount", (short)this.spawnCount);
        compoundTag.putShort("MaxNearbyEntities", (short)this.maxNearbyEntities);
        compoundTag.putShort("RequiredPlayerRange", (short)this.requiredPlayerRange);
        compoundTag.putShort("SpawnRange", (short)this.spawnRange);
        compoundTag.put("SpawnData", SpawnData.CODEC.encodeStart(NbtOps.INSTANCE, this.nextSpawnData).result().orElseThrow(() -> new IllegalStateException("Invalid SpawnData")));
        compoundTag.put("SpawnPotentials", SpawnData.LIST_CODEC.encodeStart(NbtOps.INSTANCE, this.spawnPotentials).result().orElseThrow());
        return compoundTag;
    }

    @Nullable
    public Entity getOrCreateDisplayEntity(Level level) {
        if (this.displayEntity == null) {
            this.displayEntity = EntityType.loadEntityRecursive(this.nextSpawnData.getEntityToSpawn(), level, Function.identity());
            if (this.nextSpawnData.getEntityToSpawn().size() != 1 || !this.nextSpawnData.getEntityToSpawn().contains("id", 8) || this.displayEntity instanceof Mob) {
                // empty if block
            }
        }
        return this.displayEntity;
    }

    public boolean onEventTriggered(Level level, int i) {
        if (i == 1) {
            if (level.isClientSide) {
                this.spawnDelay = this.minSpawnDelay;
            }
            return true;
        }
        return false;
    }

    public void setNextSpawnData(@Nullable Level level, BlockPos blockPos, SpawnData spawnData) {
        this.nextSpawnData = spawnData;
    }

    public abstract void broadcastEvent(Level var1, BlockPos var2, int var3);

    public double getSpin() {
        return this.spin;
    }

    public double getoSpin() {
        return this.oSpin;
    }
}

