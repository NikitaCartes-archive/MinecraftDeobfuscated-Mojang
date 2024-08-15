package net.minecraft.world.entity.monster.warden;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
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
	public static final int MAX_WARNING_LEVEL = 4;
	private static final double PLAYER_SEARCH_RADIUS = 16.0;
	private static final int WARNING_CHECK_DIAMETER = 48;
	private static final int DECREASE_WARNING_LEVEL_EVERY_INTERVAL = 12000;
	private static final int WARNING_LEVEL_INCREASE_COOLDOWN = 200;
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

	public static OptionalInt tryWarn(ServerLevel serverLevel, BlockPos blockPos, ServerPlayer serverPlayer) {
		if (hasNearbyWarden(serverLevel, blockPos)) {
			return OptionalInt.empty();
		} else {
			List<ServerPlayer> list = getNearbyPlayers(serverLevel, blockPos);
			if (!list.contains(serverPlayer)) {
				list.add(serverPlayer);
			}

			if (list.stream().anyMatch(serverPlayerx -> (Boolean)serverPlayerx.getWardenSpawnTracker().map(WardenSpawnTracker::onCooldown).orElse(false))) {
				return OptionalInt.empty();
			} else {
				Optional<WardenSpawnTracker> optional = list.stream()
					.flatMap(serverPlayerx -> serverPlayerx.getWardenSpawnTracker().stream())
					.max(Comparator.comparingInt(WardenSpawnTracker::getWarningLevel));
				if (optional.isPresent()) {
					WardenSpawnTracker wardenSpawnTracker = (WardenSpawnTracker)optional.get();
					wardenSpawnTracker.increaseWarningLevel();
					list.forEach(serverPlayerx -> serverPlayerx.getWardenSpawnTracker().ifPresent(wardenSpawnTracker2 -> wardenSpawnTracker2.copyData(wardenSpawnTracker)));
					return OptionalInt.of(wardenSpawnTracker.warningLevel);
				} else {
					return OptionalInt.empty();
				}
			}
		}
	}

	private boolean onCooldown() {
		return this.cooldownTicks > 0;
	}

	private static boolean hasNearbyWarden(ServerLevel serverLevel, BlockPos blockPos) {
		AABB aABB = AABB.ofSize(Vec3.atCenterOf(blockPos), 48.0, 48.0, 48.0);
		return !serverLevel.getEntitiesOfClass(Warden.class, aABB).isEmpty();
	}

	private static List<ServerPlayer> getNearbyPlayers(ServerLevel serverLevel, BlockPos blockPos) {
		Vec3 vec3 = Vec3.atCenterOf(blockPos);
		return serverLevel.getPlayers(serverPlayer -> !serverPlayer.isSpectator() && serverPlayer.position().closerThan(vec3, 16.0) && serverPlayer.isAlive());
	}

	private void increaseWarningLevel() {
		if (!this.onCooldown()) {
			this.ticksSinceLastWarning = 0;
			this.cooldownTicks = 200;
			this.setWarningLevel(this.getWarningLevel() + 1);
		}
	}

	private void decreaseWarningLevel() {
		this.setWarningLevel(this.getWarningLevel() - 1);
	}

	public void setWarningLevel(int i) {
		this.warningLevel = Mth.clamp(i, 0, 4);
	}

	public int getWarningLevel() {
		return this.warningLevel;
	}

	private void copyData(WardenSpawnTracker wardenSpawnTracker) {
		this.warningLevel = wardenSpawnTracker.warningLevel;
		this.cooldownTicks = wardenSpawnTracker.cooldownTicks;
		this.ticksSinceLastWarning = wardenSpawnTracker.ticksSinceLastWarning;
	}
}
