/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.realms;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ServerAddress;

@Environment(value=EnvType.CLIENT)
public class RealmsServerAddress {
    private final String host;
    private final int port;

    protected RealmsServerAddress(String string, int i) {
        this.host = string;
        this.port = i;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public static RealmsServerAddress parseString(String string) {
        ServerAddress serverAddress = ServerAddress.parseString(string);
        return new RealmsServerAddress(serverAddress.getHost(), serverAddress.getPort());
    }
}

