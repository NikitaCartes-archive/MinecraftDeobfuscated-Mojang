package com.mojang.realmsclient.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record WorldGenerationInfo(String seed, LevelType levelType, boolean generateStructures) {
}
