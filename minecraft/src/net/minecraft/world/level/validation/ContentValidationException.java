package net.minecraft.world.level.validation;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ContentValidationException extends Exception {
	private final Path directory;
	private final List<ForbiddenSymlinkInfo> entries;

	public ContentValidationException(Path path, List<ForbiddenSymlinkInfo> list) {
		this.directory = path;
		this.entries = list;
	}

	public String getMessage() {
		return getMessage(this.directory, this.entries);
	}

	public static String getMessage(Path path, List<ForbiddenSymlinkInfo> list) {
		return "Failed to validate '"
			+ path
			+ "'. Found forbidden symlinks: "
			+ (String)list.stream().map(forbiddenSymlinkInfo -> forbiddenSymlinkInfo.link() + "->" + forbiddenSymlinkInfo.target()).collect(Collectors.joining(", "));
	}
}
