package net.minecraft.network.protocol.game;

import java.util.Random;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class DebugEntityNameGenerator {
	private static final String[] NAMES_FIRST_PART = new String[]{
		"Slim",
		"Far",
		"River",
		"Silly",
		"Fat",
		"Thin",
		"Fish",
		"Bat",
		"Dark",
		"Oak",
		"Sly",
		"Bush",
		"Zen",
		"Bark",
		"Cry",
		"Slack",
		"Soup",
		"Grim",
		"Hook",
		"Dirt",
		"Mud",
		"Sad",
		"Hard",
		"Crook",
		"Sneak",
		"Stink",
		"Weird",
		"Fire",
		"Soot",
		"Soft",
		"Rough",
		"Cling",
		"Scar"
	};
	private static final String[] NAMES_SECOND_PART = new String[]{
		"Fox",
		"Tail",
		"Jaw",
		"Whisper",
		"Twig",
		"Root",
		"Finder",
		"Nose",
		"Brow",
		"Blade",
		"Fry",
		"Seek",
		"Wart",
		"Tooth",
		"Foot",
		"Leaf",
		"Stone",
		"Fall",
		"Face",
		"Tongue",
		"Voice",
		"Lip",
		"Mouth",
		"Snail",
		"Toe",
		"Ear",
		"Hair",
		"Beard",
		"Shirt",
		"Fist"
	};

	public static String getEntityName(UUID uUID) {
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
