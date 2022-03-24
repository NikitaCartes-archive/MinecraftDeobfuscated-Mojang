package net.minecraft.world.entity.monster.warden;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.SpawnUtil;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class WardenSpawnTracker {
	public static final Codec<WardenSpawnTracker> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.NON_NEGATIVE_INT.fieldOf("ticks_since_last_warning").orElse(0).forGetter(wardenSpawnTracker -> wardenSpawnTracker.ticksSinceLastWarning),
					ExtraCodecs.NON_NEGATIVE_INT.fieldOf("warning_level").orElse(0).forGetter(wardenSpawnTracker -> wardenSpawnTracker.warningLevel),
					ExtraCodecs.NON_NEGATIVE_INT.fieldOf("cooldown_ticks").orElse(0).forGetter(wardenSpawnTracker -> wardenSpawnTracker.cooldownTicks)
				)
				.apply(instance, WardenSpawnTracker::new)
	);
	public static final int WARNINGS_UNTIL_SPAWN = 3;
	private static final int WARNING_SOUND_RADIUS = 10;
	private static final int WARNING_CHECK_DIAMETER = 48;
	private static final int DECREASE_WARNING_LEVEL_EVERY_INTERVAL = 12000;
	private static final int WARNING_COOLDOWN_AFTER_DISTANT_SOUND = 200;
	private static final int WARDEN_SPAWN_ATTEMPTS = 20;
	private static final int WARDEN_SPAWN_RANGE_XZ = 5;
	private static final int WARDEN_SPAWN_RANGE_Y = 6;
	private static final int DARKNESS_RADIUS = 40;
	private static final Int2ObjectMap<SoundEvent> SOUND_BY_LEVEL = Util.make(new Int2ObjectOpenHashMap<>(), int2ObjectOpenHashMap -> {
		int2ObjectOpenHashMap.put(1, SoundEvents.WARDEN_NEARBY_CLOSE);
		int2ObjectOpenHashMap.put(2, SoundEvents.WARDEN_NEARBY_CLOSER);
		int2ObjectOpenHashMap.put(3, SoundEvents.WARDEN_NEARBY_CLOSEST);
	});
	private int ticksSinceLastWarning;
	private int warningLevel;
	private int cooldownTicks;

	public WardenSpawnTracker(int i, int j, int k) {
		this.ticksSinceLastWarning = i;
		this.warningLevel = j;
		this.cooldownTicks = k;
	}

	public void tick() {
		if (this.ticksSinceLastWarning >= 12000) {
			this.decreaseWarningLevel();
			this.ticksSinceLastWarning = 0;
		} else {
			this.ticksSinceLastWarning++;
		}

		if (this.cooldownTicks > 0) {
			this.cooldownTicks--;
		}
	}

	public void reset() {
		this.ticksSinceLastWarning = 0;
		this.warningLevel = 0;
		this.cooldownTicks = 0;
	}

	public boolean prepareWarningEvent(ServerLevel serverLevel, BlockPos blockPos) {
		if (!this.canPrepareWarningEvent(serverLevel, blockPos)) {
			return false;
		} else {
			List<ServerPlayer> list = getNearbyPlayers(serverLevel, blockPos);
			if (list.isEmpty()) {
				return false;
			} else {
				Optional<WardenSpawnTracker> optional = list.stream()
					.map(Player::getWardenSpawnTracker)
					.max(Comparator.comparingInt(wardenSpawnTracker -> wardenSpawnTracker.warningLevel));
				optional.ifPresent(wardenSpawnTracker -> {
					wardenSpawnTracker.increaseWarningLevel();
					list.forEach(serverPlayer -> serverPlayer.getWardenSpawnTracker().copyWarningLevelFrom(wardenSpawnTracker));
				});
				return true;
			}
		}
	}

	public boolean canPrepareWarningEvent(ServerLevel serverLevel, BlockPos blockPos) {
		if (this.cooldownTicks > 0) {
			return false;
		} else {
			AABB aABB = AABB.ofSize(Vec3.atCenterOf(blockPos), 48.0, 48.0, 48.0);
			return serverLevel.getEntitiesOfClass(Warden.class, aABB).isEmpty();
		}
	}

	private static List<ServerPlayer> getNearbyPlayers(ServerLevel serverLevel, BlockPos blockPos) {
		Vec3 vec3 = Vec3.atCenterOf(blockPos);
		double d = 16.0;
		Predicate<ServerPlayer> predicate = serverPlayer -> serverPlayer.position().closerThan(vec3, 16.0);
		return serverLevel.getPlayers(predicate.and(LivingEntity::isAlive));
	}

	public void triggerWarningEvent(ServerLevel serverLevel, BlockPos blockPos) {
		if (this.getWarningLevel() < 3) {
			Warden.applyDarknessAround(serverLevel, Vec3.atCenterOf(blockPos), null, 40);
			playWarningSound(serverLevel, blockPos, this.warningLevel);
		} else {
			summonWarden(serverLevel, blockPos);
		}
	}

	private static void playWarningSound(ServerLevel serverLevel, BlockPos blockPos, int i) {
		SoundEvent soundEvent = SOUND_BY_LEVEL.get(i);
		if (soundEvent != null) {
			int j = blockPos.getX() + Mth.randomBetweenInclusive(serverLevel.random, -10, 10);
			int k = blockPos.getY() + Mth.randomBetweenInclusive(serverLevel.random, -10, 10);
			int l = blockPos.getZ() + Mth.randomBetweenInclusive(serverLevel.random, -10, 10);
			serverLevel.playSound(null, (double)j, (double)k, (double)l, soundEvent, SoundSource.HOSTILE, 5.0F, 1.0F);
		}
	}

	private static void summonWarden(ServerLevel serverLevel, BlockPos blockPos) {
		Optional<Warden> optional = SpawnUtil.trySpawnMob(EntityType.WARDEN, serverLevel, blockPos, 20, 5, 6);
		optional.ifPresent(warden -> {
			warden.getBrain().setMemoryWithExpiry(MemoryModuleType.IS_EMERGING, Unit.INSTANCE, (long)WardenAi.EMERGE_DURATION);
			serverLevel.playSound(null, warden.getX(), warden.getY(), warden.getZ(), SoundEvents.WARDEN_AGITATED, SoundSource.BLOCKS, 5.0F, 1.0F);
		});
	}

	private void increaseWarningLevel() {
		if (this.cooldownTicks <= 0) {
			this.ticksSinceLastWarning = 0;
			this.cooldownTicks = 200;
			this.setWarningLevel(this.getWarningLevel() + 1);
		}
	}

	private void decreaseWarningLevel() {
		this.setWarningLevel(this.getWarningLevel() - 1);
	}

	public void setWarningLevel(int i) {
		this.warningLevel = Mth.clamp(i, 0, 3);
	}

	private int getWarningLevel() {
		return this.warningLevel;
	}

	private void copyWarningLevelFrom(WardenSpawnTracker wardenSpawnTracker) {
		this.ticksSinceLastWarning = wardenSpawnTracker.ticksSinceLastWarning;
		this.warningLevel = wardenSpawnTracker.warningLevel;
		this.cooldownTicks = wardenSpawnTracker.cooldownTicks;
	}
}
