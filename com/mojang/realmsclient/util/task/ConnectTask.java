/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerAddress;
import com.mojang.realmsclient.util.task.LongRunningTask;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsConnect;

@Environment(value=EnvType.CLIENT)
public class ConnectTask
extends LongRunningTask {
    private final RealmsConnect realmsConnect;
    private final RealmsServer server;
    private final RealmsServerAddress address;

    public ConnectTask(Screen screen, RealmsServer realmsServer, RealmsServerAddress realmsServerAddress) {
        this.server = realmsServer;
        this.address = realmsServerAddress;
        this.realmsConnect = new RealmsConnect(screen);
    }

    @Override
    public void run() {
        this.setTitle(Component.translatable("mco.connect.connecting"));
        this.realmsConnect.connect(this.server, ServerAddress.parseString(this.address.address));
    }

    @Override
    public void abortTask() {
        this.realmsConnect.abort();
        Minecraft.getInstance().getDownloadedPackSource().clearServerPack();
    }

    @Override
    public void tick() {
        this.realmsConnect.tick();
    }
}

