package com.mojang.realmsclient.gui;

import com.mojang.realmsclient.dto.RealmsServer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class RealmsServerList implements Iterable<RealmsServer> {
	private final Minecraft minecraft;
	private final Set<RealmsServer> removedServers = new HashSet();
	private List<RealmsServer> servers = List.of();

	public RealmsServerList(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	public void updateServersList(List<RealmsServer> list) {
		List<RealmsServer> list2 = new ArrayList(list);
		list2.sort(new RealmsServer.McoServerComparator(this.minecraft.getUser().getName()));
		boolean bl = list2.removeAll(this.removedServers);
		if (!bl) {
			this.removedServers.clear();
		}

		this.servers = list2;
	}

	public void removeItem(RealmsServer realmsServer) {
		this.servers.remove(realmsServer);
		this.removedServers.add(realmsServer);
	}

	public Iterator<RealmsServer> iterator() {
		return this.servers.iterator();
	}

	public boolean isEmpty() {
		return this.servers.isEmpty();
	}
}
