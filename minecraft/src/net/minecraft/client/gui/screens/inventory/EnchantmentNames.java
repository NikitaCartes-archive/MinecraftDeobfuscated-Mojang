package net.minecraft.client.gui.screens.inventory;

import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import org.apache.commons.lang3.StringUtils;

@Environment(EnvType.CLIENT)
public class EnchantmentNames {
	private static final EnchantmentNames INSTANCE = new EnchantmentNames();
	private final Random random = new Random();
	private final String[] words = "the elder scrolls klaatu berata niktu xyzzy bless curse light darkness fire air earth water hot dry cold wet ignite snuff embiggen twist shorten stretch fiddle destroy imbue galvanize enchant free limited range of towards inside sphere cube self other ball mental physical grow shrink demon elemental spirit animal creature beast humanoid undead fresh stale phnglui mglwnafh cthulhu rlyeh wgahnagl fhtagnbaguette"
		.split(" ");

	private EnchantmentNames() {
	}

	public static EnchantmentNames getInstance() {
		return INSTANCE;
	}

	public String getRandomName(Font font, int i) {
		int j = this.random.nextInt(2) + 3;
		String string = "";

		for (int k = 0; k < j; k++) {
			if (k > 0) {
				string = string + " ";
			}

			string = string + Util.getRandom(this.words, this.random);
		}

		List<String> list = font.split(string, i);
		return StringUtils.join(list.size() >= 2 ? list.subList(0, 2) : list, " ");
	}

	public void initSeed(long l) {
		this.random.setSeed(l);
	}
}
