package net.minecraft.realms;

import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Proxy;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;

@Environment(EnvType.CLIENT)
public class Realms {
	private static final RepeatedNarrator REPEATED_NARRATOR = new RepeatedNarrator(Duration.ofSeconds(5L));

	public static boolean isTouchScreen() {
		return Minecraft.getInstance().options.touchscreen;
	}

	public static Proxy getProxy() {
		return Minecraft.getInstance().getProxy();
	}

	public static String sessionId() {
		User user = Minecraft.getInstance().getUser();
		return user == null ? null : user.getSessionId();
	}

	public static String userName() {
		User user = Minecraft.getInstance().getUser();
		return user == null ? null : user.getName();
	}

	public static long currentTimeMillis() {
		return Util.getMillis();
	}

	public static String getSessionId() {
		return Minecraft.getInstance().getUser().getSessionId();
	}

	public static String getUUID() {
		return Minecraft.getInstance().getUser().getUuid();
	}

	public static String getName() {
		return Minecraft.getInstance().getUser().getName();
	}

	public static String uuidToName(String string) {
		return Minecraft.getInstance().getMinecraftSessionService().fillProfileProperties(new GameProfile(UUIDTypeAdapter.fromString(string), null), false).getName();
	}

	public static <V> CompletableFuture<V> execute(Supplier<V> supplier) {
		return Minecraft.getInstance().submit(supplier);
	}

	public static void execute(Runnable runnable) {
		Minecraft.getInstance().execute(runnable);
	}

	public static void setScreen(RealmsScreen realmsScreen) {
		execute((Supplier)(() -> {
			setScreenDirect(realmsScreen);
			return null;
		}));
	}

	public static void setScreenDirect(RealmsScreen realmsScreen) {
		Minecraft.getInstance().setScreen(realmsScreen.getProxy());
	}

	public static String getGameDirectoryPath() {
		return Minecraft.getInstance().gameDirectory.getAbsolutePath();
	}

	public static int survivalId() {
		return GameType.SURVIVAL.getId();
	}

	public static int creativeId() {
		return GameType.CREATIVE.getId();
	}

	public static int adventureId() {
		return GameType.ADVENTURE.getId();
	}

	public static int spectatorId() {
		return GameType.SPECTATOR.getId();
	}

	public static void setConnectedToRealms(boolean bl) {
		Minecraft.getInstance().setConnectedToRealms(bl);
	}

	public static CompletableFuture<?> downloadResourcePack(String string, String string2) {
		return Minecraft.getInstance().getClientPackSource().downloadAndSelectResourcePack(string, string2);
	}

	public static void clearResourcePack() {
		Minecraft.getInstance().getClientPackSource().clearServerPack();
	}

	public static boolean getRealmsNotificationsEnabled() {
		return Minecraft.getInstance().options.realmsNotifications;
	}

	public static boolean inTitleScreen() {
		return Minecraft.getInstance().screen != null && Minecraft.getInstance().screen instanceof TitleScreen;
	}

	public static void deletePlayerTag(File file) {
		if (file.exists()) {
			try {
				CompoundTag compoundTag = NbtIo.readCompressed(new FileInputStream(file));
				CompoundTag compoundTag2 = compoundTag.getCompound("Data");
				compoundTag2.remove("Player");
				NbtIo.writeCompressed(compoundTag, new FileOutputStream(file));
			} catch (Exception var3) {
				var3.printStackTrace();
			}
		}
	}

	public static void openUri(String string) {
		Util.getPlatform().openUri(string);
	}

	public static void setClipboard(String string) {
		Minecraft.getInstance().keyboardHandler.setClipboard(string);
	}

	public static String getMinecraftVersionString() {
		return SharedConstants.getCurrentVersion().getName();
	}

	public static ResourceLocation resourceLocation(String string) {
		return new ResourceLocation(string);
	}

	public static String getLocalizedString(String string, Object... objects) {
		return I18n.get(string, objects);
	}

	public static void bind(String string) {
		ResourceLocation resourceLocation = new ResourceLocation(string);
		Minecraft.getInstance().getTextureManager().bind(resourceLocation);
	}

	public static void narrateNow(String string) {
		NarratorChatListener narratorChatListener = NarratorChatListener.INSTANCE;
		narratorChatListener.clear();
		narratorChatListener.handle(ChatType.SYSTEM, new TextComponent(fixNarrationNewlines(string)));
	}

	private static String fixNarrationNewlines(String string) {
		return string.replace("\\n", System.lineSeparator());
	}

	public static void narrateNow(String... strings) {
		narrateNow(Arrays.asList(strings));
	}

	public static void narrateNow(Iterable<String> iterable) {
		narrateNow(joinNarrations(iterable));
	}

	public static String joinNarrations(Iterable<String> iterable) {
		return String.join(System.lineSeparator(), iterable);
	}

	public static void narrateRepeatedly(String string) {
		REPEATED_NARRATOR.narrate(fixNarrationNewlines(string));
	}
}
