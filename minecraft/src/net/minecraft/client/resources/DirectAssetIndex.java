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
			Throwable var7 = null;

			Collection var8;
			try {
				var8 = (Collection)stream.filter(pathx -> Files.isRegularFile(pathx, new LinkOption[0]))
					.filter(pathx -> !pathx.endsWith(".mcmeta"))
					.filter(pathx -> predicate.test(pathx.getFileName().toString()))
					.map(path2 -> new ResourceLocation(string2, path.relativize(path2).toString().replaceAll("\\\\", "/")))
					.collect(Collectors.toList());
			} catch (Throwable var19) {
				var7 = var19;
				throw var19;
			} finally {
				if (stream != null) {
					if (var7 != null) {
						try {
							stream.close();
						} catch (Throwable var18) {
							var7.addSuppressed(var18);
						}
					} else {
						stream.close();
					}
				}
			}

			return var8;
		} catch (NoSuchFileException var21) {
		} catch (IOException var22) {
			LOGGER.warn("Unable to getFiles on {}", string, var22);
		}

		return Collections.emptyList();
	}
}
