package net.minecraft.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.OptionEnum;

@Environment(EnvType.CLIENT)
public enum CloudStatus implements OptionEnum {
	OFF(0, "options.off"),
	FAST(1, "options.clouds.fast"),
	FANCY(2, "options.clouds.fancy");

	private final int id;
	private final String key;

	private CloudStatus(int j, String string2) {
		this.id = j;
		this.key = string2;
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
