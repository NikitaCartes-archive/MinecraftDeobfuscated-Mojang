package net.minecraft.server.packs.repository;

import java.util.Map;
import net.minecraft.server.packs.VanillaPack;

public class ServerPacksSource implements RepositorySource {
	private final VanillaPack vanillaPack = new VanillaPack("minecraft");

	@Override
	public <T extends UnopenedPack> void loadPacks(Map<String, T> map, UnopenedPack.UnopenedPackConstructor<T> unopenedPackConstructor) {
		T unopenedPack = UnopenedPack.create("vanilla", false, () -> this.vanillaPack, unopenedPackConstructor, UnopenedPack.Position.BOTTOM);
		if (unopenedPack != null) {
			map.put("vanilla", unopenedPack);
		}
	}
}
