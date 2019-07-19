/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.realms.pluginapi;

import com.mojang.datafixers.util.Either;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.pluginapi.LoadedRealmsPlugin;

@Environment(value=EnvType.CLIENT)
public interface RealmsPlugin {
    public Either<LoadedRealmsPlugin, String> tryLoad(String var1);
}

