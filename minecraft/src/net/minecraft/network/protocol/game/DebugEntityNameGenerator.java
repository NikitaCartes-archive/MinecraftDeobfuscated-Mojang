package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

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

	public static String getEntityName(Entity entity) {
		if (entity instanceof Player) {
			return entity.getName().getString();
		} else {
			Component component = entity.getCustomName();
			return component != null ? component.getString() : getEntityName(entity.getUUID());
		}
	}

	public static String getEntityName(UUID uUID) {
		RandomSource randomSource = getRandom(uUID);
		return getRandomString(randomSource, NAMES_FIRST_PART) + getRandomString(randomSource, NAMES_SECOND_PART);
	}

	private static String getRandomString(RandomSource randomSource, String[] strings) {
		return Util.getRandom(strings, randomSource);
	}

	private static RandomSource getRandom(UUID uUID) {
		return RandomSource.create((long)(uUID.hashCode() >> 2));
	}
}
