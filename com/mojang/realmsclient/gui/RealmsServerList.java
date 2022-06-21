/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.realmsclient.dto.RealmsServer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@Environment(value=EnvType.CLIENT)
public class RealmsServerList {
    private final Minecraft minecraft;
    private final Set<RealmsServer> removedServers = Sets.newHashSet();
    private List<RealmsServer> servers = Lists.newArrayList();

    public RealmsServerList(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public List<RealmsServer> updateServersList(List<RealmsServer> list) {
        ArrayList<RealmsServer> list2 = new ArrayList<RealmsServer>(list);
        list2.sort(new RealmsServer.McoServerComparator(this.minecraft.getUser().getName()));
        boolean bl = list2.removeAll(this.removedServers);
        if (!bl) {
            this.removedServers.clear();
        }
        this.servers = list2;
        return List.copyOf(this.servers);
    }

    public synchronized List<RealmsServer> removeItem(RealmsServer realmsServer) {
        this.servers.remove(realmsServer);
        this.removedServers.add(realmsServer);
        return List.copyOf(this.servers);
    }
}

