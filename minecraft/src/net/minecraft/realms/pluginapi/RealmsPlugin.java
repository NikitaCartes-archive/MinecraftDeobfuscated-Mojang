package net.minecraft.realms.pluginapi;

import com.mojang.datafixers.util.Either;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface RealmsPlugin {
	Either<LoadedRealmsPlugin, String> tryLoad(String string);
}
