/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.shaders.Program;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public interface Shader {
    public int getId();

    public void markDirty();

    public Program getVertexProgram();

    public Program getFragmentProgram();

    public void attachToProgram();
}

