/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.realms;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.RealmsVertexFormat;

@Environment(value=EnvType.CLIENT)
public class RealmsDefaultVertexFormat {
    public static final RealmsVertexFormat POSITION_COLOR = new RealmsVertexFormat(DefaultVertexFormat.POSITION_COLOR);
    public static final RealmsVertexFormat POSITION_TEX_COLOR = new RealmsVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR);
}

