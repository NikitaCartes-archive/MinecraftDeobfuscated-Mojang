package net.minecraft.client.resources;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
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
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class DirectAssetIndex extends AssetIndex {
	private static final Logger LOGGER = LogUtils.getLogger();
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
	public Collection<ResourceLocation> getFiles(String string, String string2, Predicate<ResourceLocation> predicate) {
		Path path = this.assetsDirectory.toPath().resolve(string2);

		try {
			Stream<Path> stream = Files.walk(path.resolve(string));

			Collection var6;
			try {
				var6 = (Collection)stream.filter(pathx -> Files.isRegularFile(pathx, new LinkOption[0]))
					.filter(pathx -> !pathx.endsWith(".mcmeta"))
					.map(path2 -> new ResourceLocation(string2, path.relativize(path2).toString().replaceAll("\\\\", "/")))
					.filter(predicate)
					.collect(Collectors.toList());
			} catch (Throwable var9) {
				if (stream != null) {
					try {
						stream.close();
					} catch (Throwable var8) {
						var9.addSuppressed(var8);
					}
				}

				throw var9;
			}

			if (stream != null) {
				stream.close();
			}

			return var6;
		} catch (NoSuchFileException var10) {
		} catch (IOException var11) {
			LOGGER.warn("Unable to getFiles on {}", string, var11);
		}

		return Collections.emptyList();
	}
}
