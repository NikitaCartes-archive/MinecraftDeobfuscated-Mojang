package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.dto.RealmsServerAddress;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsConnect;

@Environment(EnvType.CLIENT)
public class ConnectTask extends LongRunningTask {
	private final RealmsConnect realmsConnect;
	private final RealmsServerAddress address;

	public ConnectTask(Screen screen, RealmsServerAddress realmsServerAddress) {
		this.address = realmsServerAddress;
		this.realmsConnect = new RealmsConnect(screen);
	}

	public void run() {
		this.setTitle(new TranslatableComponent("mco.connect.connecting"));
		net.minecraft.realms.RealmsServerAddress realmsServerAddress = net.minecraft.realms.RealmsServerAddress.parseString(this.address.address);
		this.realmsConnect.connect(realmsServerAddress.getHost(), realmsServerAddress.getPort());
	}

	@Override
	public void abortTask() {
		this.realmsConnect.abort();
		Minecraft.getInstance().getClientPackSource().clearServerPack();
	}

	@Override
	public void tick() {
		this.realmsConnect.tick();
	}
}
