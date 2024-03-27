package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;

public class VibrationSelector {
	public static final Codec<VibrationSelector> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					VibrationInfo.CODEC.lenientOptionalFieldOf("event").forGetter(vibrationSelector -> vibrationSelector.currentVibrationData.map(Pair::getLeft)),
					Codec.LONG.fieldOf("tick").forGetter(vibrationSelector -> (Long)vibrationSelector.currentVibrationData.map(Pair::getRight).orElse(-1L))
				)
				.apply(instance, VibrationSelector::new)
	);
	private Optional<Pair<VibrationInfo, Long>> currentVibrationData;

	public VibrationSelector(Optional<VibrationInfo> optional, long l) {
		this.currentVibrationData = optional.map(vibrationInfo -> Pair.of(vibrationInfo, l));
	}

	public VibrationSelector() {
		this.currentVibrationData = Optional.empty();
	}

	public void addCandidate(VibrationInfo vibrationInfo, long l) {
		if (this.shouldReplaceVibration(vibrationInfo, l)) {
			this.currentVibrationData = Optional.of(Pair.of(vibrationInfo, l));
		}
	}

	private boolean shouldReplaceVibration(VibrationInfo vibrationInfo, long l) {
		if (this.currentVibrationData.isEmpty()) {
			return true;
		} else {
			Pair<VibrationInfo, Long> pair = (Pair<VibrationInfo, Long>)this.currentVibrationData.get();
			long m = pair.getRight();
			if (l != m) {
				return false;
			} else {
				VibrationInfo vibrationInfo2 = pair.getLeft();
				if (vibrationInfo.distance() < vibrationInfo2.distance()) {
					return true;
				} else {
					return vibrationInfo.distance() > vibrationInfo2.distance()
						? false
						: VibrationSystem.getGameEventFrequency(vibrationInfo.gameEvent()) > VibrationSystem.getGameEventFrequency(vibrationInfo2.gameEvent());
				}
			}
		}
	}

	public Optional<VibrationInfo> chosenCandidate(long l) {
		if (this.currentVibrationData.isEmpty()) {
			return Optional.empty();
		} else {
			return ((Pair)this.currentVibrationData.get()).getRight() < l
				? Optional.of((VibrationInfo)((Pair)this.currentVibrationData.get()).getLeft())
				: Optional.empty();
		}
	}

	public void startOver() {
		this.currentVibrationData = Optional.empty();
	}
}
