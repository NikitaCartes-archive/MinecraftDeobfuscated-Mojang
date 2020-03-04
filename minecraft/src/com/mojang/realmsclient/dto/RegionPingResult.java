package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RegionPingResult extends ValueObject implements ReflectionBasedSerialization {
	@SerializedName("regionName")
	private final String regionName;
	@SerializedName("ping")
	private final int ping;

	public RegionPingResult(String string, int i) {
		this.regionName = string;
		this.ping = i;
	}

	public int ping() {
		return this.ping;
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "%s --> %.2f ms", this.regionName, (float)this.ping);
	}
}
