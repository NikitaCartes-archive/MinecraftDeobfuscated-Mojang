package net.minecraft.server.packs.repository;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public abstract class BuiltInPackSource implements RepositorySource {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final String VANILLA_ID = "vanilla";
	private final PackType packType;
	private final VanillaPackResources vanillaPack;
	private final ResourceLocation packDir;

	public BuiltInPackSource(PackType packType, VanillaPackResources vanillaPackResources, ResourceLocation resourceLocation) {
		this.packType = packType;
		this.vanillaPack = vanillaPackResources;
		this.packDir = resourceLocation;
	}

	@Override
	public void loadPacks(Consumer<Pack> consumer) {
		Pack pack = this.createVanillaPack(this.vanillaPack);
		if (pack != null) {
			consumer.accept(pack);
		}

		this.listBundledPacks(consumer);
	}

	@Nullable
	protected abstract Pack createVanillaPack(PackResources packResources);

	protected abstract Component getPackTitle(String string);

	public VanillaPackResources getVanillaPack() {
		return this.vanillaPack;
	}

	private void listBundledPacks(Consumer<Pack> consumer) {
		Map<String, Function<String, Pack>> map = new HashMap();
		this.populatePackList(map::put);
		map.forEach((string, function) -> {
			Pack pack = (Pack)function.apply(string);
			if (pack != null) {
				consumer.accept(pack);
			}
		});
	}

	protected void populatePackList(BiConsumer<String, Function<String, Pack>> biConsumer) {
		this.vanillaPack.listRawPaths(this.packType, this.packDir, path -> this.discoverPacksInPath(path, biConsumer));
	}

	protected void discoverPacksInPath(@Nullable Path path, BiConsumer<String, Function<String, Pack>> biConsumer) {
		if (path != null && Files.isDirectory(path, new LinkOption[0])) {
			try {
				FolderRepositorySource.discoverPacks(
					path,
					(pathx, resourcesSupplier) -> biConsumer.accept(
							pathToId(pathx), (Function)string -> this.createBuiltinPack(string, resourcesSupplier, this.getPackTitle(string))
						)
				);
			} catch (IOException var4) {
				LOGGER.warn("Failed to discover packs in {}", path, var4);
			}
		}
	}

	private static String pathToId(Path path) {
		return StringUtils.removeEnd(path.getFileName().toString(), ".zip");
	}

	@Nullable
	protected abstract Pack createBuiltinPack(String string, Pack.ResourcesSupplier resourcesSupplier, Component component);
}
