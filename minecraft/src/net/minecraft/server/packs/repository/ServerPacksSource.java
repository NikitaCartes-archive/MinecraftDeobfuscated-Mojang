package net.minecraft.server.packs.repository;

import java.util.function.Consumer;
import net.minecraft.server.packs.VanillaPackResources;

public class ServerPacksSource implements RepositorySource {
	private final VanillaPackResources vanillaPack = new VanillaPackResources("minecraft");

	@Override
	public void loadPacks(Consumer<Pack> consumer, Pack.PackConstructor packConstructor) {
		Pack pack = Pack.create("vanilla", false, () -> this.vanillaPack, packConstructor, Pack.Position.BOTTOM, PackSource.BUILT_IN);
		if (pack != null) {
			consumer.accept(pack);
		}
	}
}
