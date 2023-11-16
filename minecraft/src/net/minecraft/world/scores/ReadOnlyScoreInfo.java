package net.minecraft.world.scores;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.numbers.NumberFormat;

public interface ReadOnlyScoreInfo {
	int value();

	boolean isLocked();

	@Nullable
	NumberFormat numberFormat();

	default MutableComponent formatValue(NumberFormat numberFormat) {
		return ((NumberFormat)Objects.requireNonNullElse(this.numberFormat(), numberFormat)).format(this.value());
	}

	static MutableComponent safeFormatValue(@Nullable ReadOnlyScoreInfo readOnlyScoreInfo, NumberFormat numberFormat) {
		return readOnlyScoreInfo != null ? readOnlyScoreInfo.formatValue(numberFormat) : numberFormat.format(0);
	}
}
