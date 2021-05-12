package net.minecraft.client.resources;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class DirectAssetIndex extends AssetIndex {
	private final File assetsDirectory;

	public DirectAssetIndex(File file) {
		this.assetsDirectory = file;
	}

	@Override
	public File getFile(ResourceLocation resourceLocation) {
		return new File(this.assetsDirectory, resourceLocation.toString().replace(':', '/'));
	}

	@Override
	public File getRootFile(String string) {
		return new File(this.assetsDirectory, string);
	}

	@Override
	public Collection<ResourceLocation> getFiles(String string, String string2, int i, Predicate<String> predicate) {
		Path path = this.assetsDirectory.toPath().resolve(string2);

		try {
			Stream<Path> stream = Files.walk(path.resolve(string), i, new FileVisitOption[0]);

			Collection var7;
			try {
				var7 = (Collection)stream.filter(pathx -> Files.isRegularFile(pathx, new LinkOption[0]))
					.filter(pathx -> !pathx.endsWith(".mcmeta"))
					.filter(pathx -> predicate.test(pathx.getFileName().toString()))
					.map(path2 -> new ResourceLocation(string2, path.relativize(path2).toString().replaceAll("\\\\", "/")))
					.collect(Collectors.toList());
			} catch (Throwable var10) {
				if (stream != null) {
					try {
						stream.close();
					} catch (Throwable var9) {
						var10.addSuppressed(var9);
					}
				}

				throw var10;
			}

			if (stream != null) {
				stream.close();
			}

			return var7;
		} catch (NoSuchFileException var11) {
		} catch (IOException var12) {
			LOGGER.warn("Unable to getFiles on {}", string, var12);
		}

		return Collections.emptyList();
	}
}
