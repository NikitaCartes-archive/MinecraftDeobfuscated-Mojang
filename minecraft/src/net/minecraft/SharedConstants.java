package net.minecraft;

import com.mojang.bridge.game.GameVersion;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.types.constant.NamespacedStringType;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.commands.BrigadierExceptions;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class SharedConstants {
	public static final Level NETTY_LEAK_DETECTION = Level.DISABLED;
	public static boolean IS_RUNNING_IN_IDE;
	public static final char[] ILLEGAL_FILE_CHARACTERS = new char[]{'/', '\n', '\r', '\t', '\u0000', '\f', '`', '?', '*', '\\', '<', '>', '|', '"', ':'};
	private static GameVersion CURRENT_VERSION;

	public static boolean isAllowedChatCharacter(char c) {
		return c != 167 && c >= ' ' && c != 127;
	}

	public static String filterText(String string) {
		StringBuilder stringBuilder = new StringBuilder();

		for (char c : string.toCharArray()) {
			if (isAllowedChatCharacter(c)) {
				stringBuilder.append(c);
			}
		}

		return stringBuilder.toString();
	}

	@Environment(EnvType.CLIENT)
	public static String filterUnicodeSupplementary(String string) {
		StringBuilder stringBuilder = new StringBuilder();
		int i = 0;

		while (i < string.length()) {
			int j = string.codePointAt(i);
			if (!Character.isSupplementaryCodePoint(j)) {
				stringBuilder.appendCodePoint(j);
			} else {
				stringBuilder.append('�');
			}

			i = string.offsetByCodePoints(i, 1);
		}

		return stringBuilder.toString();
	}

	public static GameVersion getCurrentVersion() {
		if (CURRENT_VERSION == null) {
			CURRENT_VERSION = DetectedVersion.tryDetectVersion();
		}

		return CURRENT_VERSION;
	}

	static {
		ResourceLeakDetector.setLevel(NETTY_LEAK_DETECTION);
		CommandSyntaxException.ENABLE_COMMAND_STACK_TRACES = false;
		CommandSyntaxException.BUILT_IN_EXCEPTIONS = new BrigadierExceptions();
		NamespacedStringType.ENSURE_NAMESPACE = NamespacedSchema::ensureNamespaced;
	}
}
