/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import com.mojang.realmsclient.dto.ValueObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class PlayerInfo
extends ValueObject
implements ReflectionBasedSerialization {
    @SerializedName(value="name")
    private String name;
    @SerializedName(value="uuid")
    private String uuid;
    @SerializedName(value="operator")
    private boolean operator;
    @SerializedName(value="accepted")
    private boolean accepted;
    @SerializedName(value="online")
    private boolean online;

    public String getName() {
        return this.name;
    }

    public void setName(String string) {
        this.name = string;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String string) {
        this.uuid = string;
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

