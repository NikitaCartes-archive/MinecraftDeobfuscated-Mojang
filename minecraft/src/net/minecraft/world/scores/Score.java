package net.minecraft.world.scores;

import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;

public class Score implements ReadOnlyScoreInfo {
	private static final String TAG_SCORE = "Score";
	private static final String TAG_LOCKED = "Locked";
	private static final String TAG_DISPLAY = "display";
	private static final String TAG_FORMAT = "format";
	private int value;
	private boolean locked = true;
	@Nullable
	private Component display;
	@Nullable
	private NumberFormat numberFormat;

	@Override
	public int value() {
		return this.value;
	}

	public void value(int i) {
		this.value = i;
	}

	@Override
	public boolean isLocked() {
		return this.locked;
	}

	public void setLocked(boolean bl) {
		this.locked = bl;
	}

	@Nullable
	public Component display() {
		return this.display;
	}

	public void display(@Nullable Component component) {
		this.display = component;
	}

	@Nullable
	@Override
	public NumberFormat numberFormat() {
		return this.numberFormat;
	}

	public void numberFormat(@Nullable NumberFormat numberFormat) {
		this.numberFormat = numberFormat;
	}

	public CompoundTag write(HolderLookup.Provider provider) {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putInt("Score", this.value);
		compoundTag.putBoolean("Locked", this.locked);
		if (this.display != null) {
			compoundTag.putString("display", Component.Serializer.toJson(this.display, provider));
		}

		if (this.numberFormat != null) {
			NumberFormatTypes.CODEC
				.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this.numberFormat)
				.ifSuccess(tag -> compoundTag.put("format", tag));
		}

		return compoundTag;
	}

	public static Score read(CompoundTag compoundTag, HolderLookup.Provider provider) {
		Score score = new Score();
		score.value = compoundTag.getInt("Score");
		score.locked = compoundTag.getBoolean("Locked");
		if (compoundTag.contains("display", 8)) {
			score.display = Component.Serializer.fromJson(compoundTag.getString("display"), provider);
		}

		if (compoundTag.contains("format", 10)) {
			NumberFormatTypes.CODEC
				.parse(provider.createSerializationContext(NbtOps.INSTANCE), compoundTag.get("format"))
				.ifSuccess(numberFormat -> score.numberFormat = numberFormat);
		}

		return score;
	}
}
