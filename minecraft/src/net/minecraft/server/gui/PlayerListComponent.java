package net.minecraft.server.gui;

import java.util.Vector;
import javax.swing.JList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class PlayerListComponent extends JList<String> {
	private final MinecraftServer server;
	private int tickCount;

	public PlayerListComponent(MinecraftServer minecraftServer) {
		this.server = minecraftServer;
		minecraftServer.addTickable(this::tick);
	}

	public void tick() {
		if (this.tickCount++ % 20 == 0) {
			Vector<String> vector = new Vector();

			for (int i = 0; i < this.server.getPlayerList().getPlayers().size(); i++) {
				vector.add(((ServerPlayer)this.server.getPlayerList().getPlayers().get(i)).getGameProfile().getName());
			}

			this.setListData(vector);
		}
	}
}
