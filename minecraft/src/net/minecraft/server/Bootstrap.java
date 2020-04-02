package net.minecraft.server;

import java.io.PrintStream;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import net.minecraft.SharedConstants;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.locale.Language;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.FireBlock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Bootstrap {
	public static final PrintStream STDOUT = System.out;
	private static boolean isBootstrapped;
	private static final Logger LOGGER = LogManager.getLogger();

	public static void bootStrap() {
		if (!isBootstrapped) {
			isBootstrapped = true;
			if (Registry.REGISTRY.isEmpty()) {
				throw new IllegalStateException("Unable to load registries");
			} else {
				FireBlock.bootStrap();
				ComposterBlock.bootStrap();
				if (EntityType.getKey(EntityType.PLAYER) == null) {
					throw new IllegalStateException("Failed loading EntityTypes");
				} else {
					PotionBrewing.bootStrap();
					EntitySelectorOptions.bootStrap();
					DispenseItemBehavior.bootStrap();
					ArgumentTypes.bootStrap();
					wrapStreams();
				}
			}
		}
	}

	private static <T> void checkTranslations(Registry<T> registry, Function<T, String> function, Set<String> set) {
		Language language = Language.getInstance();
		registry.iterator().forEachRemaining(object -> {
			String string = (String)function.apply(object);
			if (!language.exists(string)) {
				set.add(string);
			}
		});
	}

	public static Set<String> getMissingTranslations() {
		Set<String> set = new TreeSet();
		checkTranslations(Registry.ATTRIBUTES, Attribute::getDescriptionId, set);
		checkTranslations(Registry.ENTITY_TYPE, EntityType::getDescriptionId, set);
		checkTranslations(Registry.MOB_EFFECT, MobEffect::getDescriptionId, set);
		checkTranslations(Registry.ITEM, Item::getDescriptionId, set);
		checkTranslations(Registry.ENCHANTMENT, Enchantment::getDescriptionId, set);
		checkTranslations(Registry.BIOME, Biome::getDescriptionId, set);
		checkTranslations(Registry.BLOCK, Block::getDescriptionId, set);
		checkTranslations(Registry.CUSTOM_STAT, resourceLocation -> "stat." + resourceLocation.toString().replace(':', '.'), set);
		return set;
	}

	public static void validate() {
		if (!isBootstrapped) {
			throw new IllegalArgumentException("Not bootstrapped");
		} else {
			if (SharedConstants.IS_RUNNING_IN_IDE) {
				getMissingTranslations().forEach(string -> LOGGER.error("Missing translations: " + string));
			}

			DefaultAttributes.validate();
		}
	}

	private static void wrapStreams() {
		if (LOGGER.isDebugEnabled()) {
			System.setErr(new DebugLoggedPrintStream("STDERR", System.err));
			System.setOut(new DebugLoggedPrintStream("STDOUT", STDOUT));
		} else {
			System.setErr(new LoggedPrintStream("STDERR", System.err));
			System.setOut(new LoggedPrintStream("STDOUT", STDOUT));
		}
	}

	public static void realStdoutPrintln(String string) {
		STDOUT.println(string);
	}
}
