package net.minecraft.network.protocol.game;

import java.util.Random;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class DebugVillagerNameGenerator {
	private static final String[] NAMES_FIRST_PART = new String[]{
		"Slim", "Far", "River", "Silly", "Fat", "Thin", "Fish", "Bat", "Dark", "Oak", "Sly", "Bush", "Zen", "Bark", "Cry", "Slack", "Soup", "Grim", "Hook"
	};
	private static final String[] NAMES_SECOND_PART = new String[]{
		"Fox", "Tail", "Jaw", "Whisper", "Twig", "Root", "Finder", "Nose", "Brow", "Blade", "Fry", "Seek", "Tooth", "Foot", "Leaf", "Stone", "Fall", "Face", "Tongue"
	};

	public static String getVillagerName(UUID uUID) {
		Random random = getRandom(uUID);
		return getRandomString(random, NAMES_FIRST_PART) + getRandomString(random, NAMES_SECOND_PART);
	}

	private static String getRandomString(Random random, String[] strings) {
		return strings[random.nextInt(strings.length)];
	}

	private static Random getRandom(UUID uUID) {
		return new Random((long)(uUID.hashCode() >> 2));
	}
}
