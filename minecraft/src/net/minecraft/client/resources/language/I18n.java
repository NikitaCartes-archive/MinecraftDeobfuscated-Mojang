package net.minecraft.client.resources.language;

import java.util.IllegalFormatException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.locale.Language;

@Environment(EnvType.CLIENT)
public class I18n {
	private static volatile Language language = Language.getInstance();

	private I18n() {
	}

	static void setLanguage(Language language) {
		I18n.language = language;
	}

	public static String get(String string, Object... objects) {
		String string2 = language.getOrDefault(string);

		try {
			return String.format(string2, objects);
		} catch (IllegalFormatException var4) {
			return "Format error: " + string2;
		}
	}

	public static boolean exists(String string) {
		return language.has(string);
	}
}
