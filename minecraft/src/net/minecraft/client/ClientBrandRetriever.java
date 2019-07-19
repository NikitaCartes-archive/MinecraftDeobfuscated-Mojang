package net.minecraft.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ClientBrandRetriever {
	public static String getClientModName() {
		return "vanilla";
	}
}
