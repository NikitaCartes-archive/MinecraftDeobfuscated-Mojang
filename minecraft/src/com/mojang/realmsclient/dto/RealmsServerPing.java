package com.mojang.realmsclient.dto;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RealmsServerPing extends ValueObject {
	public volatile String nrOfPlayers = "0";
	public volatile String playerList = "";
}
