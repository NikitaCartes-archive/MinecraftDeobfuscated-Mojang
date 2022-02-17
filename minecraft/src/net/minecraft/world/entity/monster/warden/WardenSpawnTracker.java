package net.minecraft.world.entity.monster.warden;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class WardenSpawnTracker {
	private static final int WARNING_SOUND_RADIUS = 10;
	private static final int WARNING_CHECK_RADIUS = 48;
	private static final int WARNINGS_UNTIL_SPAWN = 3;
	private static final int DECREASE_WARNING_LEVEL_EVERY_INTERVAL = 12000;
	private static final int WARNING_COOLDOWN_AFTER_DISTANT_SOUND = 200;
	private static final int WARDEN_SPAWN_ATTEMPTS = 20;
	private static final int WARDEN_SPAWN_RANGE_XZ = 5;
	private static final int WARDEN_SPAWN_RANGE_Y = 6;
	private static final int DARKNESS_RADIUS = 40;
	private static final int DARKNESS_DURATION = 260;
	private static final Int2ObjectMap<SoundEvent> SOUND_BY_LEVEL = Util.make(new Int2ObjectOpenHashMap<>(), int2ObjectOpenHashMap -> {
		int2ObjectOpenHashMap.put(1, SoundEvents.WARDEN_NEARBY_CLOSE);
		int2ObjectOpenHashMap.put(2, SoundEvents.WARDEN_NEARBY_CLOSER);
		int2ObjectOpenHashMap.put(3, SoundEvents.WARDEN_NEARBY_CLOSEST);
	});
	private int ticksSinceLastWarning;
	private int warningLevel;
	private int shriekerCooldownTicks;

	public void tick() {
		if (this.ticksSinceLastWarning >= 12000) {
			this.decreaseWarningLevel();
			this.ticksSinceLastWarning = 0;
		} else {
			this.ticksSinceLastWarning++;
		}

		if (this.shriekerCooldownTicks > 0) {
			this.shriekerCooldownTicks--;
		}
	}

	public void reset() {
		this.ticksSinceLastWarning = 0;
		this.warningLevel = 0;
		this.shriekerCooldownTicks = 0;
	}

	public boolean prepareWarningEvent(ServerLevel serverLevel, BlockPos blockPos, List<ServerPlayer> list) {
		if (!this.canPrepareWarningEvent(serverLevel, blockPos)) {
			return false;
		} else {
			Optional<WardenSpawnTracker> optional = list.stream()
				.max(Comparator.comparing(serverPlayer -> serverPlayer.getWardenSpawnTracker().warningLevel))
				.map(Player::getWardenSpawnTracker);
			if (optional.isPresent()) {
				WardenSpawnTracker wardenSpawnTracker = (WardenSpawnTracker)optional.get();
				wardenSpawnTracker.increaseWarningLevel();
				list.stream().map(Player::getWardenSpawnTracker).forEach(wardenSpawnTracker2 -> wardenSpawnTracker2.copyWarningLevelFrom(wardenSpawnTracker));
				return true;
			} else {
				return false;
			}
		}
	}

	public boolean canPrepareWarningEvent(ServerLevel serverLevel, BlockPos blockPos) {
		return this.shriekerCooldownTicks <= 0 && !this.areWardensNearby(serverLevel, Vec3.atCenterOf(blockPos));
	}

	private boolean areWardensNearby(ServerLevel serverLevel, Vec3 vec3) {
		return !serverLevel.getEntitiesOfClass(Warden.class, AABB.ofSize(vec3, 48.0, 48.0, 48.0)).isEmpty();
	}

	public boolean triggerWarningEvent(ServerLevel serverLevel, BlockPos blockPos) {
		if (!this.isAtMaxWarningLevel()) {
			int i = Mth.randomBetweenInclusive(serverLevel.random, -10, 10);
			int j = Mth.randomBetweenInclusive(serverLevel.random, -10, 10);
			int k = Mth.randomBetweenInclusive(serverLevel.random, -10, 10);
			return distantWardenSound(serverLevel, blockPos, blockPos.offset(i, j, k), SOUND_BY_LEVEL.get(this.warningLevel), 5.0F);
		} else {
			return summonWarden(serverLevel, blockPos);
		}
	}

	private void increaseWarningLevel() {
		if (this.shriekerCooldownTicks <= 0) {
			this.ticksSinceLastWarning = 0;
			this.shriekerCooldownTicks = 200;
			this.setWarningLevel(this.getWarningLevel() + 1);
		}
	}

	private void decreaseWarningLevel() {
		this.setWarningLevel(this.getWarningLevel() - 1);
	}

	private boolean isAtMaxWarningLevel() {
		return this.getWarningLevel() >= 3;
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
		this.shriekerCooldownTicks = wardenSpawnTracker.shriekerCooldownTicks;
	}

	public void readAdditionalSaveData(CompoundTag compoundTag) {
		if (compoundTag.contains("ticksSinceLastWarning", 99)) {
			this.ticksSinceLastWarning = compoundTag.getInt("ticksSinceLastWarning");
			this.warningLevel = compoundTag.getInt("warningCount");
			this.shriekerCooldownTicks = compoundTag.getInt("shriekerCooldownTicks");
		}
	}

	public void addAdditionalSaveData(CompoundTag compoundTag) {
		compoundTag.putInt("ticksSinceLastWarning", this.ticksSinceLastWarning);
		compoundTag.putInt("warningCount", this.warningLevel);
		compoundTag.putInt("shriekerCooldownTicks", this.shriekerCooldownTicks);
	}

	public static boolean distantWardenSound(ServerLevel serverLevel, BlockPos blockPos, BlockPos blockPos2, SoundEvent soundEvent, float f) {
		MobEffectInstance mobEffectInstance = new MobEffectInstance(MobEffects.DARKNESS, 260, 0, false, false);
		MobEffectUtil.addEffectToPlayersAround(serverLevel, null, Vec3.atCenterOf(blockPos), 40.0, mobEffectInstance, 200);
		serverLevel.playSound(null, (double)blockPos2.getX(), (double)blockPos2.getY(), (double)blockPos2.getZ(), soundEvent, SoundSource.HOSTILE, f, 1.0F);
		return true;
	}

	public static boolean summonWarden(ServerLevel serverLevel, BlockPos blockPos) {
		Optional<Warden> optional = SpawnUtil.trySpawnMob(EntityType.WARDEN, serverLevel, blockPos, 20, 5, 6);
		if (optional.isPresent()) {
			Warden warden = (Warden)optional.get();
			WardenAi.noticeSuspiciousLocation(warden, blockPos);
			warden.getBrain().setMemoryWithExpiry(MemoryModuleType.IS_EMERGING, true, (long)WardenAi.EMERGE_DURATION);
			serverLevel.playSound(null, warden.getX(), warden.getY(), warden.getZ(), SoundEvents.WARDEN_AGITATED, SoundSource.BLOCKS, 5.0F, 1.0F);
			return true;
		} else {
			return false;
		}
	}
}
