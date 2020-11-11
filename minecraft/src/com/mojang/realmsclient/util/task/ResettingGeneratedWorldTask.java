package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.WorldGenerationInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ResettingGeneratedWorldTask extends ResettingWorldTask {
	private final WorldGenerationInfo generationInfo;

	public ResettingGeneratedWorldTask(WorldGenerationInfo worldGenerationInfo, long l, Component component, Runnable runnable) {
		super(l, component, runnable);
		this.generationInfo = worldGenerationInfo;
	}

	@Override
	protected void sendResetRequest(RealmsClient realmsClient, long l) throws RealmsServiceException {
		realmsClient.resetWorldWithSeed(l, this.generationInfo);
	}
}
