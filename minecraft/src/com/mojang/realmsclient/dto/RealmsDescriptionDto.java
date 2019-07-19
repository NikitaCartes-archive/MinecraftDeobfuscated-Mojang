package com.mojang.realmsclient.dto;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RealmsDescriptionDto extends ValueObject {
	public String name;
	public String description;

	public RealmsDescriptionDto(String string, String string2) {
		this.name = string;
		this.description = string2;
	}
}
