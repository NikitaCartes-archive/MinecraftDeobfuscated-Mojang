package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import java.io.File;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class ServerList {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Minecraft minecraft;
	private final List<ServerData> serverList = Lists.<ServerData>newArrayList();

	public ServerList(Minecraft minecraft) {
		this.minecraft = minecraft;
		this.load();
	}

	public void load() {
		try {
			this.serverList.clear();
			CompoundTag compoundTag = NbtIo.read(new File(this.minecraft.gameDirectory, "servers.dat"));
			if (compoundTag == null) {
				return;
			}

			ListTag listTag = compoundTag.getList("servers", 10);

			for (int i = 0; i < listTag.size(); i++) {
				this.serverList.add(ServerData.read(listTag.getCompound(i)));
			}
		} catch (Exception var4) {
			LOGGER.error("Couldn't load server list", (Throwable)var4);
		}
	}

	public void save() {
		try {
			ListTag listTag = new ListTag();

			for (ServerData serverData : this.serverList) {
				listTag.add(serverData.write());
			}

			CompoundTag compoundTag = new CompoundTag();
			compoundTag.put("servers", listTag);
			File file = File.createTempFile("servers", ".dat", this.minecraft.gameDirectory);
			NbtIo.write(compoundTag, file);
			File file2 = new File(this.minecraft.gameDirectory, "servers.dat_old");
			File file3 = new File(this.minecraft.gameDirectory, "servers.dat");
			Util.safeReplaceFile(file3, file, file2);
		} catch (Exception var6) {
			LOGGER.error("Couldn't save server list", (Throwable)var6);
		}
	}

	public ServerData get(int i) {
		return (ServerData)this.serverList.get(i);
	}

	public void remove(ServerData serverData) {
		this.serverList.remove(serverData);
	}

	public void add(ServerData serverData) {
		this.serverList.add(serverData);
	}

	public int size() {
		return this.serverList.size();
	}

	public void swap(int i, int j) {
		ServerData serverData = this.get(i);
		this.serverList.set(i, this.get(j));
		this.serverList.set(j, serverData);
		this.save();
	}

	public void replace(int i, ServerData serverData) {
		this.serverList.set(i, serverData);
	}

	public static void saveSingleServer(ServerData serverData) {
		ServerList serverList = new ServerList(Minecraft.getInstance());
		serverList.load();

		for (int i = 0; i < serverList.size(); i++) {
			ServerData serverData2 = serverList.get(i);
			if (serverData2.name.equals(serverData.name) && serverData2.ip.equals(serverData.ip)) {
				serverList.replace(i, serverData);
				break;
			}
		}

		serverList.save();
	}
}
