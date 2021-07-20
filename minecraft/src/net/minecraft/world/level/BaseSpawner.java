package net.minecraft.world.level;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BaseSpawner {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int EVENT_SPAWN = 1;
	private static WeightedRandomList<SpawnData> EMPTY_POTENTIALS = WeightedRandomList.create();
	private int spawnDelay = 20;
	private WeightedRandomList<SpawnData> spawnPotentials = EMPTY_POTENTIALS;
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
	private final Random random = new Random();

	@Nullable
	private ResourceLocation getEntityId(@Nullable Level level, BlockPos blockPos) {
		String string = this.nextSpawnData.getTag().getString("id");

		try {
			return StringUtil.isNullOrEmpty(string) ? null : new ResourceLocation(string);
		} catch (ResourceLocationException var5) {
			LOGGER.warn(
				"Invalid entity id '{}' at spawner {}:[{},{},{}]",
				string,
				level != null ? level.dimension().location() : "<null>",
				blockPos.getX(),
				blockPos.getY(),
				blockPos.getZ()
			);
			return null;
		}
	}

	public void setEntityId(EntityType<?> entityType) {
		this.nextSpawnData.getTag().putString("id", Registry.ENTITY_TYPE.getKey(entityType).toString());
	}

	public void setEntityId(EntityType<?> entityType, int i) {
		this.setEntityId(entityType);
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putInt("BlockLightLimit", i);
		this.nextSpawnData.getTag().put("CustomeSpawnRules", compoundTag);
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
					CompoundTag compoundTag = this.nextSpawnData.getTag();
					Optional<EntityType<?>> optional = EntityType.by(compoundTag);
					if (!optional.isPresent()) {
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
						if (compoundTag.contains("CustomeSpawnRules", 10)) {
							if (!((EntityType)optional.get()).getCategory().isFriendly() && serverLevel.getDifficulty() == Difficulty.PEACEFUL) {
								continue;
							}

							CompoundTag compoundTag2 = compoundTag.getCompound("CustomeSpawnRules");
							if (compoundTag2.contains("BlockLightLimit", 99) && serverLevel.getBrightness(LightLayer.BLOCK, blockPos2) > compoundTag2.getInt("BlockLightLimit")) {
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
							if (!mob.checkSpawnRules(serverLevel, MobSpawnType.SPAWNER) || !mob.checkSpawnObstruction(serverLevel)) {
								continue;
							}

							if (this.nextSpawnData.getTag().size() == 1 && this.nextSpawnData.getTag().contains("id", 8)) {
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

		this.spawnPotentials.getRandom(this.random).ifPresent(spawnData -> this.setNextSpawnData(level, blockPos, spawnData));
		this.broadcastEvent(level, blockPos, 1);
	}

	public void load(@Nullable Level level, BlockPos blockPos, CompoundTag compoundTag) {
		this.spawnDelay = compoundTag.getShort("Delay");
		List<SpawnData> list = Lists.<SpawnData>newArrayList();
		if (compoundTag.contains("SpawnPotentials", 9)) {
			ListTag listTag = compoundTag.getList("SpawnPotentials", 10);

			for (int i = 0; i < listTag.size(); i++) {
				list.add(new SpawnData(listTag.getCompound(i)));
			}
		}

		this.spawnPotentials = WeightedRandomList.create(list);
		if (compoundTag.contains("SpawnData", 10)) {
			this.setNextSpawnData(level, blockPos, new SpawnData(1, compoundTag.getCompound("SpawnData")));
		} else if (!list.isEmpty()) {
			this.spawnPotentials.getRandom(this.random).ifPresent(spawnData -> this.setNextSpawnData(level, blockPos, spawnData));
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

	public CompoundTag save(@Nullable Level level, BlockPos blockPos, CompoundTag compoundTag) {
		ResourceLocation resourceLocation = this.getEntityId(level, blockPos);
		if (resourceLocation == null) {
			return compoundTag;
		} else {
			compoundTag.putShort("Delay", (short)this.spawnDelay);
			compoundTag.putShort("MinSpawnDelay", (short)this.minSpawnDelay);
			compoundTag.putShort("MaxSpawnDelay", (short)this.maxSpawnDelay);
			compoundTag.putShort("SpawnCount", (short)this.spawnCount);
			compoundTag.putShort("MaxNearbyEntities", (short)this.maxNearbyEntities);
			compoundTag.putShort("RequiredPlayerRange", (short)this.requiredPlayerRange);
			compoundTag.putShort("SpawnRange", (short)this.spawnRange);
			compoundTag.put("SpawnData", this.nextSpawnData.getTag().copy());
			ListTag listTag = new ListTag();
			if (this.spawnPotentials.isEmpty()) {
				listTag.add(this.nextSpawnData.save());
			} else {
				for (SpawnData spawnData : this.spawnPotentials.unwrap()) {
					listTag.add(spawnData.save());
				}
			}

			compoundTag.put("SpawnPotentials", listTag);
			return compoundTag;
		}
	}

	@Nullable
	public Entity getOrCreateDisplayEntity(Level level) {
		if (this.displayEntity == null) {
			this.displayEntity = EntityType.loadEntityRecursive(this.nextSpawnData.getTag(), level, Function.identity());
			if (this.nextSpawnData.getTag().size() == 1 && this.nextSpawnData.getTag().contains("id", 8) && this.displayEntity instanceof Mob) {
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
