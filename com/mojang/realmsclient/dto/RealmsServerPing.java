/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.dto;

import com.mojang.realmsclient.dto.ValueObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class RealmsServerPing
extends ValueObject {
    public volatile String nrOfPlayers = "0";
    public volatile String playerList = "";
}

