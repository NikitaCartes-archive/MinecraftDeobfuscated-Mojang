package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class PlayerInfo extends ValueObject implements ReflectionBasedSerialization {
	@SerializedName("name")
	private String name;
	@SerializedName("uuid")
	private UUID uuid;
	@SerializedName("operator")
	private boolean operator;
	@SerializedName("accepted")
	private boolean accepted;
	@SerializedName("online")
	private boolean online;

	public String getName() {
		return this.name;
	}

	public void setName(String string) {
		this.name = string;
	}

	public UUID getUuid() {
		return this.uuid;
	}

	public void setUuid(UUID uUID) {
		this.uuid = uUID;
	}

	public boolean isOperator() {
		return this.operator;
	}

	public void setOperator(boolean bl) {
		this.operator = bl;
	}

	public boolean getAccepted() {
		return this.accepted;
	}

	public void setAccepted(boolean bl) {
		this.accepted = bl;
	}

	public boolean getOnline() {
		return this.online;
	}

	public void setOnline(boolean bl) {
		this.online = bl;
	}
}
