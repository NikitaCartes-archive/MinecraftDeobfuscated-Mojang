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
	public File getFile(String string) {
		return new File(this.assetsDirectory, string);
	}

	@Override
	public Collection<String> getFiles(String string, int i, Predicate<String> predicate) {
		Path path = this.assetsDirectory.toPath().resolve("minecraft/");

		try {
			Stream<Path> stream = Files.walk(path.resolve(string), i, new FileVisitOption[0]);
			Throwable var6 = null;

			Collection var7;
			try {
				var7 = (Collection)stream.filter(pathx -> Files.isRegularFile(pathx, new LinkOption[0]))
					.filter(pathx -> !pathx.endsWith(".mcmeta"))
					.map(path::relativize)
					.map(Object::toString)
					.map(stringx -> stringx.replaceAll("\\\\", "/"))
					.filter(predicate)
					.collect(Collectors.toList());
			} catch (Throwable var18) {
				var6 = var18;
				throw var18;
			} finally {
				if (stream != null) {
					if (var6 != null) {
						try {
							stream.close();
						} catch (Throwable var17) {
							var6.addSuppressed(var17);
						}
					} else {
						stream.close();
					}
				}
			}

			return var7;
		} catch (NoSuchFileException var20) {
		} catch (IOException var21) {
			LOGGER.warn("Unable to getFiles on {}", string, var21);
		}

		return Collections.emptyList();
	}
}
