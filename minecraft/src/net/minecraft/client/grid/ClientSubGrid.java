package net.minecraft.client.grid;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.grid.GridCarrier;
import net.minecraft.world.grid.SubGrid;

@Environment(EnvType.CLIENT)
public class ClientSubGrid extends SubGrid implements AutoCloseable {
	private final SubGridRenderer renderer = new SubGridRenderer(this);

	public ClientSubGrid(ClientLevel clientLevel, GridCarrier gridCarrier) {
		super(clientLevel, gridCarrier);
	}

	public SubGridRenderer getRenderer() {
		return this.renderer;
	}

	public void close() {
		this.renderer.close();
	}
}
