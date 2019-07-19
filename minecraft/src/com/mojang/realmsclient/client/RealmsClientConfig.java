package com.mojang.realmsclient.client;

import java.net.Proxy;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RealmsClientConfig {
	private static Proxy proxy;

	public static Proxy getProxy() {
		return proxy;
	}

	public static void setProxy(Proxy proxy) {
		if (RealmsClientConfig.proxy == null) {
			RealmsClientConfig.proxy = proxy;
		}
	}
}
