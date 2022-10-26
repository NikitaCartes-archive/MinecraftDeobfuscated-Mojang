package net.minecraft;

import com.mojang.serialization.DataResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;

public class FileUtil {
	private static final Pattern COPY_COUNTER_PATTERN = Pattern.compile("(<name>.*) \\((<count>\\d*)\\)", 66);
	private static final int MAX_FILE_NAME = 255;
	private static final Pattern RESERVED_WINDOWS_FILENAMES = Pattern.compile(".*\\.|(?:COM|CLOCK\\$|CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\..*)?", 2);
	private static final Pattern STRICT_PATH_SEGMENT_CHECK = Pattern.compile("[-._a-z0-9]+");

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

	public static String getFullResourcePath(String string) {
		return FilenameUtils.getFullPath(string).replace(File.separator, "/");
	}

	public static String normalizeResourcePath(String string) {
		return FilenameUtils.normalize(string).replace(File.separator, "/");
	}

	public static DataResult<List<String>> decomposePath(String string) {
		int i = string.indexOf(47);
		if (i == -1) {
			return switch (string) {
				case "", ".", ".." -> DataResult.error("Invalid path '" + string + "'");
				default -> !isValidStrictPathSegment(string) ? DataResult.error("Invalid path '" + string + "'") : DataResult.success(List.of(string));
			};
		} else {
			List<String> list = new ArrayList();
			int j = 0;
			boolean bl = false;

			while (true) {
				String string2 = string.substring(j, i);
				switch (string2) {
					case "":
					case ".":
					case "..":
						return DataResult.error("Invalid segment '" + string2 + "' in path '" + string + "'");
				}

				if (!isValidStrictPathSegment(string2)) {
					return DataResult.error("Invalid segment '" + string2 + "' in path '" + string + "'");
				}

				list.add(string2);
				if (bl) {
					return DataResult.success(list);
				}

				j = i + 1;
				i = string.indexOf(47, j);
				if (i == -1) {
					i = string.length();
					bl = true;
				}
			}
		}
	}

	public static Path resolvePath(Path path, List<String> list) {
		int i = list.size();

		return switch (i) {
			case 0 -> path;
			case 1 -> path.resolve((String)list.get(0));
			default -> {
				String[] strings = new String[i - 1];

				for (int j = 1; j < i; j++) {
					strings[j - 1] = (String)list.get(j);
				}

				yield path.resolve(path.getFileSystem().getPath((String)list.get(0), strings));
			}
		};
	}

	public static boolean isValidStrictPathSegment(String string) {
		return STRICT_PATH_SEGMENT_CHECK.matcher(string).matches();
	}

	public static void validatePath(String... strings) {
		if (strings.length == 0) {
			throw new IllegalArgumentException("Path must have at least one element");
		} else {
			for (String string : strings) {
				if (string.equals("..") || string.equals(".") || !isValidStrictPathSegment(string)) {
					throw new IllegalArgumentException("Illegal segment " + string + " in path " + Arrays.toString(strings));
				}
			}
		}
	}
}
