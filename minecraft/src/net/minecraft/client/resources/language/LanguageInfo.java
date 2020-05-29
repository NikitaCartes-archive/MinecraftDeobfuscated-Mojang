package net.minecraft.client.resources.language;

import com.mojang.bridge.game.Language;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class LanguageInfo implements Language, Comparable<LanguageInfo> {
	private final String code;
	private final String region;
	private final String name;
	private final boolean bidirectional;

	public LanguageInfo(String string, String string2, String string3, boolean bl) {
		this.code = string;
		this.region = string2;
		this.name = string3;
		this.bidirectional = bl;
	}

	@Override
	public String getCode() {
		return this.code;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getRegion() {
		return this.region;
	}

	public boolean isBidirectional() {
		return this.bidirectional;
	}

	public String toString() {
		return String.format("%s (%s)", this.name, this.region);
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			return !(object instanceof LanguageInfo) ? false : this.code.equals(((LanguageInfo)object).code);
		}
	}

	public int hashCode() {
		return this.code.hashCode();
	}

	public int compareTo(LanguageInfo languageInfo) {
		return this.code.compareTo(languageInfo.code);
	}
}
