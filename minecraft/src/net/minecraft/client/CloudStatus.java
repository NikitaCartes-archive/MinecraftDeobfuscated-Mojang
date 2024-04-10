package net.minecraft.client;

import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.OptionEnum;
import net.minecraft.util.StringRepresentable;

@Environment(EnvType.CLIENT)
public enum CloudStatus implements OptionEnum, StringRepresentable {
	OFF(0, "false", "options.off"),
	FAST(1, "fast", "options.clouds.fast"),
	FANCY(2, "true", "options.clouds.fancy");

	public static final Codec<CloudStatus> CODEC = StringRepresentable.fromEnum(CloudStatus::values);
	private final int id;
	private final String legacyName;
	private final String key;

	private CloudStatus(final int j, final String string2, final String string3) {
		this.id = j;
		this.legacyName = string2;
		this.key = string3;
	}

	@Override
	public String getSerializedName() {
		return this.legacyName;
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public String getKey() {
		return this.key;
	}
}
