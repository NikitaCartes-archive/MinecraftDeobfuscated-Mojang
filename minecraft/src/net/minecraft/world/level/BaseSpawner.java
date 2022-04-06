package net.minecraft.world.level;

import com.mojang.logging.LogUtils;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.phys.AABB;
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
	private final RandomSource random = RandomSource.create();

	public void setEntityId(EntityType<?> entityType) {
		this.nextSpawnData.getEntityToSpawn().putString("id", Registry.ENTITY_TYPE.getKey(entityType).toString());
	}

	private boolean isNearPlayer(Level level, BlockPos blockPos) {
		return level.hasNearbyAlivePlayer(
			(double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, (double)this.requiredPlayerRange
		);
	}

	public void clientTick(Level level, BlockPos blockPos) {
		if (!this.isNearPlayer(level, blockPos)) {
			this.oSpin = this.spin;
		} else {
			double d = (double)blockPos.getX() + level.random.nextDouble();
			double e = (double)blockPos.getY() + level.random.nextDouble();
			double f = (double)blockPos.getZ() + level.random.nextDouble();
			level.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0);
			level.addParticle(ParticleTypes.FLAME, d, e, f, 0.0, 0.0, 0.0);
			if (this.spawnDelay > 0) {
				this.spawnDelay--;
			}

			this.oSpin = this.spin;
			this.spin = (this.spin + (double)(1000.0F / ((float)this.spawnDelay + 200.0F))) % 360.0;
		}
	}

	public void serverTick(ServerLevel serverLevel, BlockPos blockPos) {
		if (this.isNearPlayer(serverLevel, blockPos)) {
			if (this.spawnDelay == -1) {
				this.delay(serverLevel, blockPos);
			}

			if (this.spawnDelay > 0) {
				this.spawnDelay--;
			} else {
				boolean bl = false;

				for (int i = 0; i < this.spawnCount; i++) {
					CompoundTag compoundTag = this.nextSpawnData.getEntityToSpawn();
					Optional<EntityType<?>> optional = EntityType.by(compoundTag);
					if (optional.isEmpty()) {
						this.delay(serverLevel, blockPos);
						return;
					}

					ListTag listTag = compoundTag.getList("Pos", 6);
					int j = listTag.size();
					double d = j >= 1
						? listTag.getDouble(0)
						: (double)blockPos.getX() + (serverLevel.random.nextDouble() - serverLevel.random.nextDouble()) * (double)this.spawnRange + 0.5;
					double e = j >= 2 ? listTag.getDouble(1) : (double)(blockPos.getY() + serverLevel.random.nextInt(3) - 1);
					double f = j >= 3
						? listTag.getDouble(2)
						: (double)blockPos.getZ() + (serverLevel.random.nextDouble() - serverLevel.random.nextDouble()) * (double)this.spawnRange + 0.5;
					if (serverLevel.noCollision(((EntityType)optional.get()).getAABB(d, e, f))) {
						BlockPos blockPos2 = new BlockPos(d, e, f);
						if (this.nextSpawnData.getCustomSpawnRules().isPresent()) {
							if (!((EntityType)optional.get()).getCategory().isFriendly() && serverLevel.getDifficulty() == Difficulty.PEACEFUL) {
								continue;
							}

							SpawnData.CustomSpawnRules customSpawnRules = (SpawnData.CustomSpawnRules)this.nextSpawnData.getCustomSpawnRules().get();
							if (!customSpawnRules.blockLightLimit().isValueInRange(serverLevel.getBrightness(LightLayer.BLOCK, blockPos2))
								|| !customSpawnRules.skyLightLimit().isValueInRange(serverLevel.getBrightness(LightLayer.SKY, blockPos2))) {
								continue;
							}
						} else if (!SpawnPlacements.checkSpawnRules((EntityType)optional.get(), serverLevel, MobSpawnType.SPAWNER, blockPos2, serverLevel.getRandom())) {
							continue;
						}

						Entity entity = EntityType.loadEntityRecursive(compoundTag, serverLevel, entityx -> {
							entityx.moveTo(d, e, f, entityx.getYRot(), entityx.getXRot());
							return entityx;
						});
						if (entity == null) {
							this.delay(serverLevel, blockPos);
							return;
						}

						int k = serverLevel.getEntitiesOfClass(
								entity.getClass(),
								new AABB(
										(double)blockPos.getX(),
										(double)blockPos.getY(),
										(double)blockPos.getZ(),
										(double)(blockPos.getX() + 1),
										(double)(blockPos.getY() + 1),
										(double)(blockPos.getZ() + 1)
									)
									.inflate((double)this.spawnRange)
							)
							.size();
						if (k >= this.maxNearbyEntities) {
							this.delay(serverLevel, blockPos);
							return;
						}

						entity.moveTo(entity.getX(), entity.getY(), entity.getZ(), serverLevel.random.nextFloat() * 360.0F, 0.0F);
						if (entity instanceof Mob mob) {
							if (this.nextSpawnData.getCustomSpawnRules().isEmpty() && !mob.checkSpawnRules(serverLevel, MobSpawnType.SPAWNER)
								|| !mob.checkSpawnObstruction(serverLevel)) {
								continue;
							}

							if (this.nextSpawnData.getEntityToSpawn().size() == 1 && this.nextSpawnData.getEntityToSpawn().contains("id", 8)) {
								((Mob)entity).finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.SPAWNER, null, null);
							}
						}

						if (!serverLevel.tryAddFreshEntityWithPassengers(entity)) {
							this.delay(serverLevel, blockPos);
							return;
						}

						serverLevel.levelEvent(2004, blockPos, 0);
						if (entity instanceof Mob) {
							((Mob)entity).spawnAnim();
						}

						bl = true;
					}
				}

				if (bl) {
					this.delay(serverLevel, blockPos);
				}
			}
		}
	}

	private void delay(Level level, BlockPos blockPos) {
		if (this.maxSpawnDelay <= this.minSpawnDelay) {
			this.spawnDelay = this.minSpawnDelay;
		} else {
			this.spawnDelay = this.minSpawnDelay + this.random.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
		}

		this.spawnPotentials.getRandom(this.random).ifPresent(wrapper -> this.setNextSpawnData(level, blockPos, (SpawnData)wrapper.getData()));
		this.broadcastEvent(level, blockPos, 1);
	}

	public void load(@Nullable Level level, BlockPos blockPos, CompoundTag compoundTag) {
		this.spawnDelay = compoundTag.getShort("Delay");
		boolean bl = compoundTag.contains("SpawnPotentials", 9);
		boolean bl2 = compoundTag.contains("SpawnData", 10);
		if (!bl) {
			SpawnData spawnData;
			if (bl2) {
				spawnData = (SpawnData)SpawnData.CODEC
					.parse(NbtOps.INSTANCE, compoundTag.getCompound("SpawnData"))
					.resultOrPartial(string -> LOGGER.warn("Invalid SpawnData: {}", string))
					.orElseGet(SpawnData::new);
			} else {
				spawnData = new SpawnData();
			}

			this.spawnPotentials = SimpleWeightedRandomList.single(spawnData);
			this.setNextSpawnData(level, blockPos, spawnData);
		} else {
			ListTag listTag = compoundTag.getList("SpawnPotentials", 10);
			this.spawnPotentials = (SimpleWeightedRandomList<SpawnData>)SpawnData.LIST_CODEC
				.parse(NbtOps.INSTANCE, listTag)
				.resultOrPartial(string -> LOGGER.warn("Invalid SpawnPotentials list: {}", string))
				.orElseGet(SimpleWeightedRandomList::empty);
			if (bl2) {
				SpawnData spawnData2 = (SpawnData)SpawnData.CODEC
					.parse(NbtOps.INSTANCE, compoundTag.getCompound("SpawnData"))
					.resultOrPartial(string -> LOGGER.warn("Invalid SpawnData: {}", string))
					.orElseGet(SpawnData::new);
				this.setNextSpawnData(level, blockPos, spawnData2);
			} else {
				this.spawnPotentials.getRandom(this.random).ifPresent(wrapper -> this.setNextSpawnData(level, blockPos, (SpawnData)wrapper.getData()));
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
		compoundTag.put(
			"SpawnData",
			(Tag)SpawnData.CODEC.encodeStart(NbtOps.INSTANCE, this.nextSpawnData).result().orElseThrow(() -> new IllegalStateException("Invalid SpawnData"))
		);
		compoundTag.put("SpawnPotentials", (Tag)SpawnData.LIST_CODEC.encodeStart(NbtOps.INSTANCE, this.spawnPotentials).result().orElseThrow());
		return compoundTag;
	}

	@Nullable
	public Entity getOrCreateDisplayEntity(Level level) {
		if (this.displayEntity == null) {
			this.displayEntity = EntityType.loadEntityRecursive(this.nextSpawnData.getEntityToSpawn(), level, Function.identity());
			if (this.nextSpawnData.getEntityToSpawn().size() == 1 && this.nextSpawnData.getEntityToSpawn().contains("id", 8) && this.displayEntity instanceof Mob) {
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
		} else {
			return false;
		}
	}

	public void setNextSpawnData(@Nullable Level level, BlockPos blockPos, SpawnData spawnData) {
		this.nextSpawnData = spawnData;
	}

	public abstract void broadcastEvent(Level level, BlockPos blockPos, int i);

	public double getSpin() {
		return this.spin;
	}

	public double getoSpin() {
		return this.oSpin;
	}
}
