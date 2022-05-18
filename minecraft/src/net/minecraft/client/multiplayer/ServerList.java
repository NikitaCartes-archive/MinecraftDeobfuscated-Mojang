package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.thread.ProcessorMailbox;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ServerList {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final ProcessorMailbox<Runnable> IO_MAILBOX = ProcessorMailbox.create(Util.backgroundExecutor(), "server-list-io");
	private static final int MAX_HIDDEN_SERVERS = 16;
	private final Minecraft minecraft;
	private final List<ServerData> serverList = Lists.<ServerData>newArrayList();
	private final List<ServerData> hiddenServerList = Lists.<ServerData>newArrayList();

	public ServerList(Minecraft minecraft) {
		this.minecraft = minecraft;
		this.load();
	}

	public void load() {
		try {
			this.serverList.clear();
			this.hiddenServerList.clear();
			CompoundTag compoundTag = NbtIo.read(new File(this.minecraft.gameDirectory, "servers.dat"));
			if (compoundTag == null) {
				return;
			}

			ListTag listTag = compoundTag.getList("servers", 10);

			for (int i = 0; i < listTag.size(); i++) {
				CompoundTag compoundTag2 = listTag.getCompound(i);
				ServerData serverData = ServerData.read(compoundTag2);
				if (compoundTag2.getBoolean("hidden")) {
					this.hiddenServerList.add(serverData);
				} else {
					this.serverList.add(serverData);
				}
			}
		} catch (Exception var6) {
			LOGGER.error("Couldn't load server list", (Throwable)var6);
		}
	}

	public void save() {
		try {
			ListTag listTag = new ListTag();

			for (ServerData serverData : this.serverList) {
				CompoundTag compoundTag = serverData.write();
				compoundTag.putBoolean("hidden", false);
				listTag.add(compoundTag);
			}

			for (ServerData serverData : this.hiddenServerList) {
				CompoundTag compoundTag = serverData.write();
				compoundTag.putBoolean("hidden", true);
				listTag.add(compoundTag);
			}

			CompoundTag compoundTag2 = new CompoundTag();
			compoundTag2.put("servers", listTag);
			File file = File.createTempFile("servers", ".dat", this.minecraft.gameDirectory);
			NbtIo.write(compoundTag2, file);
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

	@Nullable
	public ServerData get(String string) {
		for (ServerData serverData : this.serverList) {
			if (serverData.ip.equals(string)) {
				return serverData;
			}
		}

		for (ServerData serverDatax : this.hiddenServerList) {
			if (serverDatax.ip.equals(string)) {
				return serverDatax;
			}
		}

		return null;
	}

	@Nullable
	public ServerData unhide(String string) {
		for (int i = 0; i < this.hiddenServerList.size(); i++) {
			ServerData serverData = (ServerData)this.hiddenServerList.get(i);
			if (serverData.ip.equals(string)) {
				this.hiddenServerList.remove(i);
				this.serverList.add(serverData);
				return serverData;
			}
		}

		return null;
	}

	public void remove(ServerData serverData) {
		if (!this.serverList.remove(serverData)) {
			this.hiddenServerList.remove(serverData);
		}
	}

	public void add(ServerData serverData, boolean bl) {
		if (bl) {
			this.hiddenServerList.add(0, serverData);

			while (this.hiddenServerList.size() > 16) {
				this.hiddenServerList.remove(this.hiddenServerList.size() - 1);
			}
		} else {
			this.serverList.add(serverData);
		}
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

	private static boolean set(ServerData serverData, List<ServerData> list) {
		for (int i = 0; i < list.size(); i++) {
			ServerData serverData2 = (ServerData)list.get(i);
			if (serverData2.name.equals(serverData.name) && serverData2.ip.equals(serverData.ip)) {
				list.set(i, serverData);
				return true;
			}
		}

		return false;
	}

	public static void saveSingleServer(ServerData serverData) {
		IO_MAILBOX.tell(() -> {
			ServerList serverList = new ServerList(Minecraft.getInstance());
			serverList.load();
			if (!set(serverData, serverList.serverList)) {
				set(serverData, serverList.hiddenServerList);
			}

			serverList.save();
		});
	}
}
