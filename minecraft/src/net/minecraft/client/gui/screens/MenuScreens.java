package net.minecraft.client.gui.screens;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.gui.screens.inventory.BlastFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import net.minecraft.client.gui.screens.inventory.CartographyScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.DispenserScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.gui.screens.inventory.GrindstoneScreen;
import net.minecraft.client.gui.screens.inventory.HopperScreen;
import net.minecraft.client.gui.screens.inventory.LecternScreen;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.client.gui.screens.inventory.SmokerScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class MenuScreens {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Map<MenuType<?>, MenuScreens.ScreenConstructor<?, ?>> SCREENS = Maps.<MenuType<?>, MenuScreens.ScreenConstructor<?, ?>>newHashMap();

	public static <T extends AbstractContainerMenu> void create(@Nullable MenuType<T> menuType, Minecraft minecraft, int i, Component component) {
		if (menuType == null) {
			LOGGER.warn("Trying to open invalid screen with name: {}", component.getString());
		} else {
			MenuScreens.ScreenConstructor<T, ?> screenConstructor = getConstructor(menuType);
			if (screenConstructor == null) {
				LOGGER.warn("Failed to create screen for menu type: {}", Registry.MENU.getKey(menuType));
			} else {
				screenConstructor.fromPacket(component, menuType, minecraft, i);
			}
		}
	}

	@Nullable
	private static <T extends AbstractContainerMenu> MenuScreens.ScreenConstructor<T, ?> getConstructor(MenuType<T> menuType) {
		return (MenuScreens.ScreenConstructor<T, ?>)SCREENS.get(menuType);
	}

	private static <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(
		MenuType<? extends M> menuType, MenuScreens.ScreenConstructor<M, U> screenConstructor
	) {
		MenuScreens.ScreenConstructor<?, ?> screenConstructor2 = (MenuScreens.ScreenConstructor<?, ?>)SCREENS.put(menuType, screenConstructor);
		if (screenConstructor2 != null) {
			throw new IllegalStateException("Duplicate registration for " + Registry.MENU.getKey(menuType));
		}
	}

	public static boolean selfTest() {
		boolean bl = false;

		for (MenuType<?> menuType : Registry.MENU) {
			if (!SCREENS.containsKey(menuType)) {
				LOGGER.debug("Menu {} has no matching screen", Registry.MENU.getKey(menuType));
				bl = true;
			}
		}

		return bl;
	}

	static {
		register(MenuType.GENERIC_9x1, ContainerScreen::new);
		register(MenuType.GENERIC_9x2, ContainerScreen::new);
		register(MenuType.GENERIC_9x3, ContainerScreen::new);
		register(MenuType.GENERIC_9x4, ContainerScreen::new);
		register(MenuType.GENERIC_9x5, ContainerScreen::new);
		register(MenuType.GENERIC_9x6, ContainerScreen::new);
		register(MenuType.GENERIC_3x3, DispenserScreen::new);
		register(MenuType.ANVIL, AnvilScreen::new);
		register(MenuType.BEACON, BeaconScreen::new);
		register(MenuType.BLAST_FURNACE, BlastFurnaceScreen::new);
		register(MenuType.BREWING_STAND, BrewingStandScreen::new);
		register(MenuType.CRAFTING, CraftingScreen::new);
		register(MenuType.ENCHANTMENT, EnchantmentScreen::new);
		register(MenuType.FURNACE, FurnaceScreen::new);
		register(MenuType.GRINDSTONE, GrindstoneScreen::new);
		register(MenuType.HOPPER, HopperScreen::new);
		register(MenuType.LECTERN, LecternScreen::new);
		register(MenuType.LOOM, LoomScreen::new);
		register(MenuType.MERCHANT, MerchantScreen::new);
		register(MenuType.SHULKER_BOX, ShulkerBoxScreen::new);
		register(MenuType.SMOKER, SmokerScreen::new);
		register(MenuType.CARTOGRAPHY, CartographyScreen::new);
		register(MenuType.STONECUTTER, StonecutterScreen::new);
	}

	@Environment(EnvType.CLIENT)
	interface ScreenConstructor<T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> {
		default void fromPacket(Component component, MenuType<T> menuType, Minecraft minecraft, int i) {
			U screen = this.create(menuType.create(i, minecraft.player.inventory), minecraft.player.inventory, component);
			minecraft.player.containerMenu = screen.getMenu();
			minecraft.setScreen(screen);
		}

		U create(T abstractContainerMenu, Inventory inventory, Component component);
	}
}
