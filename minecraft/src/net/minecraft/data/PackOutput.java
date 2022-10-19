package net.minecraft.data;

import java.nio.file.Path;
import net.minecraft.resources.ResourceLocation;

public class PackOutput {
	private final Path outputFolder;

	public PackOutput(Path path) {
		this.outputFolder = path;
	}

	public Path getOutputFolder() {
		return this.outputFolder;
	}

	public Path getOutputFolder(PackOutput.Target target) {
		return this.getOutputFolder().resolve(target.directory);
	}

	public PackOutput.PathProvider createPathProvider(PackOutput.Target target, String string) {
		return new PackOutput.PathProvider(this, target, string);
	}

	public static class PathProvider {
		private final Path root;
		private final String kind;

		PathProvider(PackOutput packOutput, PackOutput.Target target, String string) {
			this.root = packOutput.getOutputFolder(target);
			this.kind = string;
		}

		public Path file(ResourceLocation resourceLocation, String string) {
			return this.root.resolve(resourceLocation.getNamespace()).resolve(this.kind).resolve(resourceLocation.getPath() + "." + string);
		}

		public Path json(ResourceLocation resourceLocation) {
			return this.root.resolve(resourceLocation.getNamespace()).resolve(this.kind).resolve(resourceLocation.getPath() + ".json");
		}
	}

	public static enum Target {
		DATA_PACK("data"),
		RESOURCE_PACK("assets"),
		REPORTS("reports");

		final String directory;

		private Target(String string2) {
			this.directory = string2;
		}
	}
}
