package net.minecraft.server.packs.repository;

import java.util.function.Consumer;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;

public class ServerPacksSource implements RepositorySource {
	public static final PackMetadataSection BUILT_IN_METADATA = new PackMetadataSection(
		new TranslatableComponent("dataPack.vanilla.description"), PackType.SERVER_DATA.getVersion(SharedConstants.getCurrentVersion())
	);
	public static final String VANILLA_ID = "vanilla";
	private final VanillaPackResources vanillaPack = new VanillaPackResources(BUILT_IN_METADATA, "minecraft");

	@Override
	public void loadPacks(Consumer<Pack> consumer, Pack.PackConstructor packConstructor) {
		Pack pack = Pack.create("vanilla", false, () -> this.vanillaPack, packConstructor, Pack.Position.BOTTOM, PackSource.BUILT_IN);
		if (pack != null) {
			consumer.accept(pack);
		}
	}
}
