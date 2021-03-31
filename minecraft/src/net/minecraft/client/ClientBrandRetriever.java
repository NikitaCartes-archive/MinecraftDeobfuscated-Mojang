package net.minecraft.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.obfuscate.DontObfuscate;

@Environment(EnvType.CLIENT)
public class ClientBrandRetriever {
	public static final String VANILLA_NAME = "vanilla";

	@DontObfuscate
	public static String getClientModName() {
		return "vanilla";
	}
}
