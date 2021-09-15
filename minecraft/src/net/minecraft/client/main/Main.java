package net.minecraft.client.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.properties.PropertyMap.Serializer;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import java.io.File;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.List;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import jdk.jfr.FlightRecorder;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.NativeModuleLister;
import net.minecraft.util.profiling.jfr.JfrRecording;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class Main {
	static final Logger LOGGER = LogManager.getLogger();

	@DontObfuscate
	public static void main(String[] strings) {
		SharedConstants.tryDetectVersion();
		JfrRecording.CUSTOM_EVENTS.forEach(FlightRecorder::register);
		OptionParser optionParser = new OptionParser();
		optionParser.allowsUnrecognizedOptions();
		optionParser.accepts("demo");
		optionParser.accepts("disableMultiplayer");
		optionParser.accepts("disableChat");
		optionParser.accepts("fullscreen");
		optionParser.accepts("checkGlErrors");
		OptionSpec<Void> optionSpec = optionParser.accepts("jfrProfile");
		OptionSpec<String> optionSpec2 = optionParser.accepts("server").withRequiredArg();
		OptionSpec<Integer> optionSpec3 = optionParser.accepts("port").withRequiredArg().<Integer>ofType(Integer.class).defaultsTo(25565);
		OptionSpec<File> optionSpec4 = optionParser.accepts("gameDir").withRequiredArg().<File>ofType(File.class).defaultsTo(new File("."));
		OptionSpec<File> optionSpec5 = optionParser.accepts("assetsDir").withRequiredArg().ofType(File.class);
		OptionSpec<File> optionSpec6 = optionParser.accepts("resourcePackDir").withRequiredArg().ofType(File.class);
		OptionSpec<String> optionSpec7 = optionParser.accepts("proxyHost").withRequiredArg();
		OptionSpec<Integer> optionSpec8 = optionParser.accepts("proxyPort").withRequiredArg().defaultsTo("8080").ofType(Integer.class);
		OptionSpec<String> optionSpec9 = optionParser.accepts("proxyUser").withRequiredArg();
		OptionSpec<String> optionSpec10 = optionParser.accepts("proxyPass").withRequiredArg();
		OptionSpec<String> optionSpec11 = optionParser.accepts("username").withRequiredArg().defaultsTo("Player" + Util.getMillis() % 1000L);
		OptionSpec<String> optionSpec12 = optionParser.accepts("uuid").withRequiredArg();
		OptionSpec<String> optionSpec13 = optionParser.accepts("accessToken").withRequiredArg().required();
		OptionSpec<String> optionSpec14 = optionParser.accepts("version").withRequiredArg().required();
		OptionSpec<Integer> optionSpec15 = optionParser.accepts("width").withRequiredArg().<Integer>ofType(Integer.class).defaultsTo(854);
		OptionSpec<Integer> optionSpec16 = optionParser.accepts("height").withRequiredArg().<Integer>ofType(Integer.class).defaultsTo(480);
		OptionSpec<Integer> optionSpec17 = optionParser.accepts("fullscreenWidth").withRequiredArg().ofType(Integer.class);
		OptionSpec<Integer> optionSpec18 = optionParser.accepts("fullscreenHeight").withRequiredArg().ofType(Integer.class);
		OptionSpec<String> optionSpec19 = optionParser.accepts("userProperties").withRequiredArg().defaultsTo("{}");
		OptionSpec<String> optionSpec20 = optionParser.accepts("profileProperties").withRequiredArg().defaultsTo("{}");
		OptionSpec<String> optionSpec21 = optionParser.accepts("assetIndex").withRequiredArg();
		OptionSpec<String> optionSpec22 = optionParser.accepts("userType").withRequiredArg().defaultsTo("legacy");
		OptionSpec<String> optionSpec23 = optionParser.accepts("versionType").withRequiredArg().defaultsTo("release");
		OptionSpec<String> optionSpec24 = optionParser.nonOptions();
		OptionSet optionSet = optionParser.parse(strings);
		List<String> list = optionSet.valuesOf(optionSpec24);
		if (!list.isEmpty()) {
			System.out.println("Completely ignored arguments: " + list);
		}

		String string = parseArgument(optionSet, optionSpec7);
		Proxy proxy = Proxy.NO_PROXY;
		if (string != null) {
			try {
				proxy = new Proxy(Type.SOCKS, new InetSocketAddress(string, parseArgument(optionSet, optionSpec8)));
			} catch (Exception var71) {
			}
		}

		final String string2 = parseArgument(optionSet, optionSpec9);
		final String string3 = parseArgument(optionSet, optionSpec10);
		if (!proxy.equals(Proxy.NO_PROXY) && stringHasValue(string2) && stringHasValue(string3)) {
			Authenticator.setDefault(new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(string2, string3.toCharArray());
				}
			});
		}

		int i = parseArgument(optionSet, optionSpec15);
		int j = parseArgument(optionSet, optionSpec16);
		OptionalInt optionalInt = ofNullable(parseArgument(optionSet, optionSpec17));
		OptionalInt optionalInt2 = ofNullable(parseArgument(optionSet, optionSpec18));
		boolean bl = optionSet.has("fullscreen");
		boolean bl2 = optionSet.has("demo");
		boolean bl3 = optionSet.has("disableMultiplayer");
		boolean bl4 = optionSet.has("disableChat");
		String string4 = parseArgument(optionSet, optionSpec14);
		Gson gson = new GsonBuilder().registerTypeAdapter(PropertyMap.class, new Serializer()).create();
		PropertyMap propertyMap = GsonHelper.fromJson(gson, parseArgument(optionSet, optionSpec19), PropertyMap.class);
		PropertyMap propertyMap2 = GsonHelper.fromJson(gson, parseArgument(optionSet, optionSpec20), PropertyMap.class);
		String string5 = parseArgument(optionSet, optionSpec23);
		File file = parseArgument(optionSet, optionSpec4);
		File file2 = optionSet.has(optionSpec5) ? parseArgument(optionSet, optionSpec5) : new File(file, "assets/");
		File file3 = optionSet.has(optionSpec6) ? parseArgument(optionSet, optionSpec6) : new File(file, "resourcepacks/");
		String string6 = optionSet.has(optionSpec12) ? optionSpec12.value(optionSet) : Player.createPlayerUUID(optionSpec11.value(optionSet)).toString();
		String string7 = optionSet.has(optionSpec21) ? optionSpec21.value(optionSet) : null;
		String string8 = parseArgument(optionSet, optionSpec2);
		Integer integer = parseArgument(optionSet, optionSpec3);
		if (optionSet.has(optionSpec)) {
			JfrRecording.start(JfrRecording.Environment.CLIENT);
		}

		CrashReport.preload();
		Bootstrap.bootStrap();
		Bootstrap.validate();
		Util.startTimerHackThread();
		User user = new User(optionSpec11.value(optionSet), string6, optionSpec13.value(optionSet), optionSpec22.value(optionSet));
		GameConfig gameConfig = new GameConfig(
			new GameConfig.UserData(user, propertyMap, propertyMap2, proxy),
			new DisplayData(i, j, optionalInt, optionalInt2, bl),
			new GameConfig.FolderData(file, file3, file2, string7),
			new GameConfig.GameData(bl2, string4, string5, bl3, bl4),
			new GameConfig.ServerData(string8, integer)
		);
		Thread thread = new Thread("Client Shutdown Thread") {
			public void run() {
				Minecraft minecraft = Minecraft.getInstance();
				if (minecraft != null) {
					IntegratedServer integratedServer = minecraft.getSingleplayerServer();
					if (integratedServer != null) {
						integratedServer.halt(true);
					}
				}
			}
		};
		thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
		Runtime.getRuntime().addShutdownHook(thread);

		final Minecraft minecraft;
		try {
			Thread.currentThread().setName("Render thread");
			RenderSystem.initRenderThread();
			RenderSystem.beginInitialization();
			minecraft = new Minecraft(gameConfig);
			RenderSystem.finishInitialization();
		} catch (SilentInitException var69) {
			LOGGER.warn("Failed to create window: ", (Throwable)var69);
			return;
		} catch (Throwable var70) {
			CrashReport crashReport = CrashReport.forThrowable(var70, "Initializing game");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Initialization");
			NativeModuleLister.addCrashSection(crashReportCategory);
			Minecraft.fillReport(null, null, gameConfig.game.launchVersion, null, crashReport);
			Minecraft.crash(crashReport);
			return;
		}

		Thread thread2;
		if (minecraft.renderOnThread()) {
			thread2 = new Thread("Game thread") {
				public void run() {
					try {
						RenderSystem.initGameThread(true);
						minecraft.run();
					} catch (Throwable var2) {
						Main.LOGGER.error("Exception in client thread", var2);
					}
				}
			};
			thread2.start();

			while (minecraft.isRunning()) {
			}
		} else {
			thread2 = null;

			try {
				RenderSystem.initGameThread(false);
				minecraft.run();
			} catch (Throwable var68) {
				LOGGER.error("Unhandled game exception", var68);
			}
		}

		BufferUploader.reset();

		try {
			minecraft.stop();
			if (thread2 != null) {
				thread2.join();
			}
		} catch (InterruptedException var66) {
			LOGGER.error("Exception during client thread shutdown", (Throwable)var66);
		} finally {
			minecraft.destroy();
		}
	}

	private static OptionalInt ofNullable(@Nullable Integer integer) {
		return integer != null ? OptionalInt.of(integer) : OptionalInt.empty();
	}

	@Nullable
	private static <T> T parseArgument(OptionSet optionSet, OptionSpec<T> optionSpec) {
		try {
			return optionSet.valueOf(optionSpec);
		} catch (Throwable var5) {
			if (optionSpec instanceof ArgumentAcceptingOptionSpec<T> argumentAcceptingOptionSpec) {
				List<T> list = argumentAcceptingOptionSpec.defaultValues();
				if (!list.isEmpty()) {
					return (T)list.get(0);
				}
			}

			throw var5;
		}
	}

	private static boolean stringHasValue(@Nullable String string) {
		return string != null && !string.isEmpty();
	}

	static {
		System.setProperty("java.awt.headless", "true");
	}
}
