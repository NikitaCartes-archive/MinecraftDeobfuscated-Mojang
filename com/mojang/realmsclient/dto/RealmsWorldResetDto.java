/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.dto;

import com.mojang.realmsclient.dto.ValueObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class RealmsWorldResetDto
extends ValueObject {
    private final String seed;
    private final long worldTemplateId;
    private final int levelType;
    private final boolean generateStructures;

    public RealmsWorldResetDto(String string, long l, int i, boolean bl) {
        this.seed = string;
        this.worldTemplateId = l;
        this.levelType = i;
        this.generateStructures = bl;
    }
}

