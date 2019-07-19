package net.minecraft;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class FileUtil {
	private static final Pattern COPY_COUNTER_PATTERN = Pattern.compile("(<name>.*) \\((<count>\\d*)\\)", 66);
	private static final Pattern RESERVED_WINDOWS_FILENAMES = Pattern.compile(".*\\.|(?:COM|CLOCK\\$|CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\..*)?", 2);

	@Environment(EnvType.CLIENT)
	public static String findAvailableName(Path path, String string, String string2) throws IOException {
		for (char c : SharedConstants.ILLEGAL_FILE_CHARACTERS) {
			string = string.replace(c, '_');
		}

		string = string.replaceAll("[./\"]", "_");
		if (RESERVED_WINDOWS_FILENAMES.matcher(string).matches()) {
			string = "_" + string + "_";
		}

		Matcher matcher = COPY_COUNTER_PATTERN.matcher(string);
		int i = 0;
		if (matcher.matches()) {
			string = matcher.group("name");
			i = Integer.parseInt(matcher.group("count"));
		}

		if (string.length() > 255 - string2.length()) {
			string = string.substring(0, 255 - string2.length());
		}

		while (true) {
			String string3 = string;
			if (i != 0) {
				String string4 = " (" + i + ")";
				int j = 255 - string4.length();
				if (string.length() > j) {
					string3 = string.substring(0, j);
				}

				string3 = string3 + string4;
			}

			string3 = string3 + string2;
			Path path2 = path.resolve(string3);

			try {
				Path path3 = Files.createDirectory(path2);
				Files.deleteIfExists(path3);
				return path.relativize(path3).toString();
			} catch (FileAlreadyExistsException var8) {
				i++;
			}
		}
	}

	public static boolean isPathNormalized(Path path) {
		Path path2 = path.normalize();
		return path2.equals(path);
	}

	public static boolean isPathPortable(Path path) {
		for (Path path2 : path) {
			if (RESERVED_WINDOWS_FILENAMES.matcher(path2.toString()).matches()) {
				return false;
			}
		}

		return true;
	}

	public static Path createPathToResource(Path path, String string, String string2) {
		String string3 = string + string2;
		Path path2 = Paths.get(string3);
		if (path2.endsWith(string2)) {
			throw new InvalidPathException(string3, "empty resource name");
		} else {
			return path.resolve(path2);
		}
	}
}
