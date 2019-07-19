/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.dto;

import com.mojang.realmsclient.dto.ValueObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class PlayerInfo
extends ValueObject {
    private String name;
    private String uuid;
    private boolean operator = false;
    private boolean accepted = false;
    private boolean online = false;

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

