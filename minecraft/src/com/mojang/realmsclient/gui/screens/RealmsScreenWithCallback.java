package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.dto.WorldTemplate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.RealmsScreen;

@Environment(EnvType.CLIENT)
public abstract class RealmsScreenWithCallback extends RealmsScreen {
	protected abstract void callback(@Nullable WorldTemplate worldTemplate);
}
