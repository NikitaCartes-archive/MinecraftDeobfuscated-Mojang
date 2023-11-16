package net.minecraft.world.scores;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.numbers.NumberFormat;

public record PlayerScoreEntry(String owner, int value, @Nullable Component display, @Nullable NumberFormat numberFormatOverride) {
	public boolean isHidden() {
		return this.owner.startsWith("#");
	}

	public Component ownerName() {
		return (Component)(this.display != null ? this.display : Component.literal(this.owner()));
	}

	public MutableComponent formatValue(NumberFormat numberFormat) {
		return ((NumberFormat)Objects.requireNonNullElse(this.numberFormatOverride, numberFormat)).format(this.value);
	}
}
