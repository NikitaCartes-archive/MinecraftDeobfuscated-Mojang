/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.gui.screens.inventory.BlastFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import net.minecraft.client.gui.screens.inventory.CartographyTableScreen;
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
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class MenuScreens {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<MenuType<?>, ScreenConstructor<?, ?>> SCREENS = Maps.newHashMap();

    public static <T extends AbstractContainerMenu> void create(@Nullable MenuType<T> menuType, Minecraft minecraft, int i, Component component) {
        if (menuType == null) {
            LOGGER.warn("Trying to open invalid screen with name: {}", (Object)component.getString());
            return;
        }
        ScreenConstructor<T, ?> screenConstructor = MenuScreens.getConstructor(menuType);
        if (screenConstructor == null) {
            LOGGER.warn("Failed to create screen for menu type: {}", (Object)Registry.MENU.getKey(menuType));
            return;
        }
        screenConstructor.fromPacket(component, menuType, minecraft, i);
    }

    @Nullable
    private static <T extends AbstractContainerMenu> ScreenConstructor<T, ?> getConstructor(MenuType<T> menuType) {
        return SCREENS.get(menuType);
    }

    private static <M extends AbstractContainerMenu, U extends Screen> void register(MenuType<? extends M> menuType, ScreenConstructor<M, U> screenConstructor) {
        ScreenConstructor<M, U> screenConstructor2 = SCREENS.put(menuType, screenConstructor);
        if (screenConstructor2 != null) {
            throw new IllegalStateException("Duplicate registration for " + Registry.MENU.getKey(menuType));
        }
    }

    public static boolean selfTest() {
        boolean bl = false;
        for (MenuType menuType : Registry.MENU) {
            if (SCREENS.containsKey(menuType)) continue;
            LOGGER.debug("Menu {} has no matching screen", (Object)Registry.MENU.getKey(menuType));
            bl = true;
        }
        return bl;
    }

    static {
        MenuScreens.register(MenuType.GENERIC_9x1, ContainerScreen::new);
        MenuScreens.register(MenuType.GENERIC_9x2, ContainerScreen::new);
        MenuScreens.register(MenuType.GENERIC_9x3, ContainerScreen::new);
        MenuScreens.register(MenuType.GENERIC_9x4, ContainerScreen::new);
        MenuScreens.register(MenuType.GENERIC_9x5, ContainerScreen::new);
        MenuScreens.register(MenuType.GENERIC_9x6, ContainerScreen::new);
        MenuScreens.register(MenuType.GENERIC_3x3, DispenserScreen::new);
        MenuScreens.register(MenuType.ANVIL, AnvilScreen::new);
        MenuScreens.register(MenuType.BEACON, BeaconScreen::new);
        MenuScreens.register(MenuType.BLAST_FURNACE, BlastFurnaceScreen::new);
        MenuScreens.register(MenuType.BREWING_STAND, BrewingStandScreen::new);
        MenuScreens.register(MenuType.CRAFTING, CraftingScreen::new);
        MenuScreens.register(MenuType.ENCHANTMENT, EnchantmentScreen::new);
        MenuScreens.register(MenuType.FURNACE, FurnaceScreen::new);
        MenuScreens.register(MenuType.GRINDSTONE, GrindstoneScreen::new);
        MenuScreens.register(MenuType.HOPPER, HopperScreen::new);
        MenuScreens.register(MenuType.LECTERN, LecternScreen::new);
        MenuScreens.register(MenuType.LOOM, LoomScreen::new);
        MenuScreens.register(MenuType.MERCHANT, MerchantScreen::new);
        MenuScreens.register(MenuType.SHULKER_BOX, ShulkerBoxScreen::new);
        MenuScreens.register(MenuType.SMOKER, SmokerScreen::new);
        MenuScreens.register(MenuType.CARTOGRAPHY_TABLE, CartographyTableScreen::new);
        MenuScreens.register(MenuType.STONECUTTER, StonecutterScreen::new);
    }

    @Environment(value=EnvType.CLIENT)
    static interface ScreenConstructor<T extends AbstractContainerMenu, U extends Screen> {
        default public void fromPacket(Component component, MenuType<T> menuType, Minecraft minecraft, int i) {
            U screen = this.create(menuType.create(i, minecraft.player.inventory), minecraft.player.inventory, component);
            minecraft.player.containerMenu = ((MenuAccess)screen).getMenu();
            minecraft.setScreen((Screen)screen);
        }

        public U create(T var1, Inventory var2, Component var3);
    }
}

