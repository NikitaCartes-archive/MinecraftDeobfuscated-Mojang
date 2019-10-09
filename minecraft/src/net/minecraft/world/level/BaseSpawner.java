package net.minecraft.world.level;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.util.WeighedRandom;
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
	private int spawnDelay = 20;
	private final List<SpawnData> spawnPotentials = Lists.<SpawnData>newArrayList();
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

	@Nullable
	private ResourceLocation getEntityId() {
		String string = this.nextSpawnData.getTag().getString("id");

		try {
			return StringUtil.isNullOrEmpty(string) ? null : new ResourceLocation(string);
		} catch (ResourceLocationException var4) {
			BlockPos blockPos = this.getPos();
			LOGGER.warn(
				"Invalid entity id '{}' at spawner {}:[{},{},{}]", string, this.getLevel().dimension.getType(), blockPos.getX(), blockPos.getY(), blockPos.getZ()
			);
			return null;
		}
	}

	public void setEntityId(EntityType<?> entityType) {
		this.nextSpawnData.getTag().putString("id", Registry.ENTITY_TYPE.getKey(entityType).toString());
	}

	private boolean isNearPlayer() {
		BlockPos blockPos = this.getPos();
		return this.getLevel()
			.hasNearbyAlivePlayer((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, (double)this.requiredPlayerRange);
	}

	public void tick() {
		if (!this.isNearPlayer()) {
			this.oSpin = this.spin;
		} else {
			Level level = this.getLevel();
			BlockPos blockPos = this.getPos();
			if (level.isClientSide) {
				double d = (double)((float)blockPos.getX() + level.random.nextFloat());
				double e = (double)((float)blockPos.getY() + level.random.nextFloat());
				double f = (double)((float)blockPos.getZ() + level.random.nextFloat());
				level.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0);
				level.addParticle(ParticleTypes.FLAME, d, e, f, 0.0, 0.0, 0.0);
				if (this.spawnDelay > 0) {
					this.spawnDelay--;
				}

				this.oSpin = this.spin;
				this.spin = (this.spin + (double)(1000.0F / ((float)this.spawnDelay + 200.0F))) % 360.0;
			} else {
				if (this.spawnDelay == -1) {
					this.delay();
				}

				if (this.spawnDelay > 0) {
					this.spawnDelay--;
					return;
				}

				boolean bl = false;

				for (int i = 0; i < this.spawnCount; i++) {
					CompoundTag compoundTag = this.nextSpawnData.getTag();
					Optional<EntityType<?>> optional = EntityType.by(compoundTag);
					if (!optional.isPresent()) {
						this.delay();
						return;
					}

					ListTag listTag = compoundTag.getList("Pos", 6);
					int j = listTag.size();
					double g = j >= 1
						? listTag.getDouble(0)
						: (double)blockPos.getX() + (level.random.nextDouble() - level.random.nextDouble()) * (double)this.spawnRange + 0.5;
					double h = j >= 2 ? listTag.getDouble(1) : (double)(blockPos.getY() + level.random.nextInt(3) - 1);
					double k = j >= 3
						? listTag.getDouble(2)
						: (double)blockPos.getZ() + (level.random.nextDouble() - level.random.nextDouble()) * (double)this.spawnRange + 0.5;
					if (level.noCollision(((EntityType)optional.get()).getAABB(g, h, k))
						&& SpawnPlacements.checkSpawnRules((EntityType)optional.get(), level.getLevel(), MobSpawnType.SPAWNER, new BlockPos(g, h, k), level.getRandom())) {
						Entity entity = EntityType.loadEntityRecursive(compoundTag, level, entityx -> {
							entityx.moveTo(g, h, k, entityx.yRot, entityx.xRot);
							return entityx;
						});
						if (entity == null) {
							this.delay();
							return;
						}

						int l = level.getEntitiesOfClass(
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
						if (l >= this.maxNearbyEntities) {
							this.delay();
							return;
						}

						entity.moveTo(entity.getX(), entity.getY(), entity.getZ(), level.random.nextFloat() * 360.0F, 0.0F);
						if (entity instanceof Mob) {
							Mob mob = (Mob)entity;
							if (!mob.checkSpawnRules(level, MobSpawnType.SPAWNER) || !mob.checkSpawnObstruction(level)) {
								continue;
							}

							if (this.nextSpawnData.getTag().size() == 1 && this.nextSpawnData.getTag().contains("id", 8)) {
								((Mob)entity).finalizeSpawn(level, level.getCurrentDifficultyAt(new BlockPos(entity)), MobSpawnType.SPAWNER, null, null);
							}
						}

						this.addWithPassengers(entity);
						level.levelEvent(2004, blockPos, 0);
						if (entity instanceof Mob) {
							((Mob)entity).spawnAnim();
						}

						bl = true;
					}
				}

				if (bl) {
					this.delay();
				}
			}
		}
	}

	private void addWithPassengers(Entity entity) {
		if (this.getLevel().addFreshEntity(entity)) {
			for (Entity entity2 : entity.getPassengers()) {
				this.addWithPassengers(entity2);
			}
		}
	}

	private void delay() {
		if (this.maxSpawnDelay <= this.minSpawnDelay) {
			this.spawnDelay = this.minSpawnDelay;
		} else {
			this.spawnDelay = this.minSpawnDelay + this.getLevel().random.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
		}

		if (!this.spawnPotentials.isEmpty()) {
			this.setNextSpawnData(WeighedRandom.getRandomItem(this.getLevel().random, this.spawnPotentials));
		}

		this.broadcastEvent(1);
	}

	public void load(CompoundTag compoundTag) {
		this.spawnDelay = compoundTag.getShort("Delay");
		this.spawnPotentials.clear();
		if (compoundTag.contains("SpawnPotentials", 9)) {
			ListTag listTag = compoundTag.getList("SpawnPotentials", 10);

			for (int i = 0; i < listTag.size(); i++) {
				this.spawnPotentials.add(new SpawnData(listTag.getCompound(i)));
			}
		}

		if (compoundTag.contains("SpawnData", 10)) {
			this.setNextSpawnData(new SpawnData(1, compoundTag.getCompound("SpawnData")));
		} else if (!this.spawnPotentials.isEmpty()) {
			this.setNextSpawnData(WeighedRandom.getRandomItem(this.getLevel().random, this.spawnPotentials));
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

		if (this.getLevel() != null) {
			this.displayEntity = null;
		}
	}

	public CompoundTag save(CompoundTag compoundTag) {
		ResourceLocation resourceLocation = this.getEntityId();
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
				for (SpawnData spawnData : this.spawnPotentials) {
					listTag.add(spawnData.save());
				}
			}

			compoundTag.put("SpawnPotentials", listTag);
			return compoundTag;
		}
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public Entity getOrCreateDisplayEntity() {
		if (this.displayEntity == null) {
			this.displayEntity = EntityType.loadEntityRecursive(this.nextSpawnData.getTag(), this.getLevel(), Function.identity());
			if (this.nextSpawnData.getTag().size() == 1 && this.nextSpawnData.getTag().contains("id", 8) && this.displayEntity instanceof Mob) {
				((Mob)this.displayEntity)
					.finalizeSpawn(this.getLevel(), this.getLevel().getCurrentDifficultyAt(new BlockPos(this.displayEntity)), MobSpawnType.SPAWNER, null, null);
			}
		}

		return this.displayEntity;
	}

	public boolean onEventTriggered(int i) {
		if (i == 1 && this.getLevel().isClientSide) {
			this.spawnDelay = this.minSpawnDelay;
			return true;
		} else {
			return false;
		}
	}

	public void setNextSpawnData(SpawnData spawnData) {
		this.nextSpawnData = spawnData;
	}

	public abstract void broadcastEvent(int i);

	public abstract Level getLevel();

	public abstract BlockPos getPos();

	@Environment(EnvType.CLIENT)
	public double getSpin() {
		return this.spin;
	}

	@Environment(EnvType.CLIENT)
	public double getoSpin() {
		return this.oSpin;
	}
}
