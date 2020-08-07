package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RealmsDescriptionDto extends ValueObject implements ReflectionBasedSerialization {
	@SerializedName("name")
	public String name;
	@SerializedName("description")
	public String description;

	public RealmsDescriptionDto(String string, String string2) {
		this.name = string;
		this.description = string2;
	}
}
