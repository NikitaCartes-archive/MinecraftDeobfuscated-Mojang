package net.minecraft.client;

import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.OptionEnum;
import net.minecraft.util.StringRepresentable;

@Environment(EnvType.CLIENT)
public enum InactivityFpsLimit implements OptionEnum, StringRepresentable {
	MINIMIZED(0, "minimized", "options.inactivityFpsLimit.minimized"),
	AFK(1, "afk", "options.inactivityFpsLimit.afk");

	public static final Codec<InactivityFpsLimit> CODEC = StringRepresentable.fromEnum(InactivityFpsLimit::values);
	private final int id;
	private final String serializedName;
	private final String key;

	private InactivityFpsLimit(final int j, final String string2, final String string3) {
		this.id = j;
		this.serializedName = string2;
		this.key = string3;
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public String getKey() {
		return this.key;
	}

	@Override
	public String getSerializedName() {
		return this.serializedName;
	}
}
