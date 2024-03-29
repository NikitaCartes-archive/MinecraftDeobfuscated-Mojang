package net.minecraft.client.main;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.properties.PropertyMap.Serializer;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.logging.LogUtils;
import com.mojang.util.UndashedUuid;
import java.io.File;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import javax.annotation.Nullable;
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
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.client.telemetry.events.GameLoadTimesEvent;
import net.minecraft.core.UUIDUtil;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.NativeModuleLister;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class Main {
	static final Logger LOGGER = LogUtils.getLogger();

	@DontObfuscate
	public static void main(String[] strings) {
		Stopwatch stopwatch = Stopwatch.createStarted(Ticker.systemTicker());
		Stopwatch stopwatch2 = Stopwatch.createStarted(Ticker.systemTicker());
		GameLoadTimesEvent.INSTANCE.beginStep(TelemetryProperty.LOAD_TIME_TOTAL_TIME_MS, stopwatch);
		GameLoadTimesEvent.INSTANCE.beginStep(TelemetryProperty.LOAD_TIME_PRE_WINDOW_MS, stopwatch2);
		SharedConstants.tryDetectVersion();
		SharedConstants.enableDataFixerOptimizations();
		OptionParser optionParser = new OptionParser();
		optionParser.allowsUnrecognizedOptions();
		optionParser.accepts("demo");
		optionParser.accepts("disableMultiplayer");
		optionParser.accepts("disableChat");
		optionParser.accepts("fullscreen");
		optionParser.accepts("checkGlErrors");
		OptionSpec<Void> optionSpec = optionParser.accepts("jfrProfile");
		OptionSpec<String> optionSpec2 = optionParser.accepts("quickPlayPath").withRequiredArg();
		OptionSpec<String> optionSpec3 = optionParser.accepts("quickPlaySingleplayer").withRequiredArg();
		OptionSpec<String> optionSpec4 = optionParser.accepts("quickPlayMultiplayer").withRequiredArg();
		OptionSpec<String> optionSpec5 = optionParser.accepts("quickPlayRealms").withRequiredArg();
		OptionSpec<File> optionSpec6 = optionParser.accepts("gameDir").withRequiredArg().ofType(File.class).defaultsTo(new File("."));
		OptionSpec<File> optionSpec7 = optionParser.accepts("assetsDir").withRequiredArg().ofType(File.class);
		OptionSpec<File> optionSpec8 = optionParser.accepts("resourcePackDir").withRequiredArg().ofType(File.class);
		OptionSpec<String> optionSpec9 = optionParser.accepts("proxyHost").withRequiredArg();
		OptionSpec<Integer> optionSpec10 = optionParser.accepts("proxyPort").withRequiredArg().defaultsTo("8080").ofType(Integer.class);
		OptionSpec<String> optionSpec11 = optionParser.accepts("proxyUser").withRequiredArg();
		OptionSpec<String> optionSpec12 = optionParser.accepts("proxyPass").withRequiredArg();
		OptionSpec<String> optionSpec13 = optionParser.accepts("username").withRequiredArg().defaultsTo("Player" + Util.getMillis() % 1000L);
		OptionSpec<String> optionSpec14 = optionParser.accepts("uuid").withRequiredArg();
		OptionSpec<String> optionSpec15 = optionParser.accepts("xuid").withOptionalArg().defaultsTo("");
		OptionSpec<String> optionSpec16 = optionParser.accepts("clientId").withOptionalArg().defaultsTo("");
		OptionSpec<String> optionSpec17 = optionParser.accepts("accessToken").withRequiredArg().required();
		OptionSpec<String> optionSpec18 = optionParser.accepts("version").withRequiredArg().required();
		OptionSpec<Integer> optionSpec19 = optionParser.accepts("width").withRequiredArg().ofType(Integer.class).defaultsTo(854);
		OptionSpec<Integer> optionSpec20 = optionParser.accepts("height").withRequiredArg().ofType(Integer.class).defaultsTo(480);
		OptionSpec<Integer> optionSpec21 = optionParser.accepts("fullscreenWidth").withRequiredArg().ofType(Integer.class);
		OptionSpec<Integer> optionSpec22 = optionParser.accepts("fullscreenHeight").withRequiredArg().ofType(Integer.class);
		OptionSpec<String> optionSpec23 = optionParser.accepts("userProperties").withRequiredArg().defaultsTo("{}");
		OptionSpec<String> optionSpec24 = optionParser.accepts("profileProperties").withRequiredArg().defaultsTo("{}");
		OptionSpec<String> optionSpec25 = optionParser.accepts("assetIndex").withRequiredArg();
		OptionSpec<String> optionSpec26 = optionParser.accepts("userType").withRequiredArg().defaultsTo(User.Type.LEGACY.getName());
		OptionSpec<String> optionSpec27 = optionParser.accepts("versionType").withRequiredArg().defaultsTo("release");
		OptionSpec<String> optionSpec28 = optionParser.nonOptions();
		OptionSet optionSet = optionParser.parse(strings);
		List<String> list = optionSet.valuesOf(optionSpec28);
		if (!list.isEmpty()) {
			LOGGER.info("Completely ignored arguments: {}", list);
		}

		String string = parseArgument(optionSet, optionSpec9);
		Proxy proxy = Proxy.NO_PROXY;
		if (string != null) {
			try {
				proxy = new Proxy(Type.SOCKS, new InetSocketAddress(string, parseArgument(optionSet, optionSpec10)));
			} catch (Exception var85) {
			}
		}

		final String string2 = parseArgument(optionSet, optionSpec11);
		final String string3 = parseArgument(optionSet, optionSpec12);
		if (!proxy.equals(Proxy.NO_PROXY) && stringHasValue(string2) && stringHasValue(string3)) {
			Authenticator.setDefault(new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(string2, string3.toCharArray());
				}
			});
		}

		int i = parseArgument(optionSet, optionSpec19);
		int j = parseArgument(optionSet, optionSpec20);
		OptionalInt optionalInt = ofNullable(parseArgument(optionSet, optionSpec21));
		OptionalInt optionalInt2 = ofNullable(parseArgument(optionSet, optionSpec22));
		boolean bl = optionSet.has("fullscreen");
		boolean bl2 = optionSet.has("demo");
		boolean bl3 = optionSet.has("disableMultiplayer");
		boolean bl4 = optionSet.has("disableChat");
		String string4 = parseArgument(optionSet, optionSpec18);
		Gson gson = new GsonBuilder().registerTypeAdapter(PropertyMap.class, new Serializer()).create();
		PropertyMap propertyMap = GsonHelper.fromJson(gson, parseArgument(optionSet, optionSpec23), PropertyMap.class);
		PropertyMap propertyMap2 = GsonHelper.fromJson(gson, parseArgument(optionSet, optionSpec24), PropertyMap.class);
		String string5 = parseArgument(optionSet, optionSpec27);
		File file = parseArgument(optionSet, optionSpec6);
		File file2 = optionSet.has(optionSpec7) ? parseArgument(optionSet, optionSpec7) : new File(file, "assets/");
		File file3 = optionSet.has(optionSpec8) ? parseArgument(optionSet, optionSpec8) : new File(file, "resourcepacks/");
		UUID uUID = optionSet.has(optionSpec14)
			? UndashedUuid.fromStringLenient((String)optionSpec14.value(optionSet))
			: UUIDUtil.createOfflinePlayerUUID((String)optionSpec13.value(optionSet));
		String string6 = optionSet.has(optionSpec25) ? (String)optionSpec25.value(optionSet) : null;
		String string7 = optionSet.valueOf(optionSpec15);
		String string8 = optionSet.valueOf(optionSpec16);
		String string9 = parseArgument(optionSet, optionSpec2);
		String string10 = unescapeJavaArgument(parseArgument(optionSet, optionSpec3));
		String string11 = unescapeJavaArgument(parseArgument(optionSet, optionSpec4));
		String string12 = unescapeJavaArgument(parseArgument(optionSet, optionSpec5));
		if (optionSet.has(optionSpec)) {
			JvmProfiler.INSTANCE.start(net.minecraft.util.profiling.jfr.Environment.CLIENT);
		}

		CrashReport.preload();

		try {
			Bootstrap.bootStrap();
			GameLoadTimesEvent.INSTANCE.setBootstrapTime(Bootstrap.bootstrapDuration.get());
			Bootstrap.validate();
		} catch (Throwable var84) {
			CrashReport crashReport = CrashReport.forThrowable(var84, "Bootstrap");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Initialization");
			NativeModuleLister.addCrashSection(crashReportCategory);
			Minecraft.fillReport(null, null, string4, null, crashReport);
			Minecraft.crash(null, file, crashReport);
			return;
		}

		String string13 = (String)optionSpec26.value(optionSet);
		User.Type type = User.Type.byName(string13);
		if (type == null) {
			LOGGER.warn("Unrecognized user type: {}", string13);
		}

		User user = new User(
			(String)optionSpec13.value(optionSet),
			uUID,
			(String)optionSpec17.value(optionSet),
			emptyStringToEmptyOptional(string7),
			emptyStringToEmptyOptional(string8),
			type
		);
		GameConfig gameConfig = new GameConfig(
			new GameConfig.UserData(user, propertyMap, propertyMap2, proxy),
			new DisplayData(i, j, optionalInt, optionalInt2, bl),
			new GameConfig.FolderData(file, file3, file2, string6),
			new GameConfig.GameData(bl2, string4, string5, bl3, bl4),
			new GameConfig.QuickPlayData(string9, string10, string11, string12)
		);
		Util.startTimerHackThread();
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
		final Minecraft minecraft = null;

		try {
			Thread.currentThread().setName("Render thread");
			RenderSystem.initRenderThread();
			RenderSystem.beginInitialization();
			minecraft = new Minecraft(gameConfig);
			RenderSystem.finishInitialization();
		} catch (SilentInitException var82) {
			Util.shutdownExecutors();
			LOGGER.warn("Failed to create window: ", var82);
			return;
		} catch (Throwable var83) {
			CrashReport crashReport2 = CrashReport.forThrowable(var83, "Initializing game");
			CrashReportCategory crashReportCategory2 = crashReport2.addCategory("Initialization");
			NativeModuleLister.addCrashSection(crashReportCategory2);
			Minecraft.fillReport(minecraft, null, gameConfig.game.launchVersion, null, crashReport2);
			Minecraft.crash(minecraft, gameConfig.location.gameDirectory, crashReport2);
			return;
		}

		Minecraft minecraft2 = minecraft;
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

			while(minecraft2.isRunning()) {
			}
		} else {
			thread2 = null;

			try {
				RenderSystem.initGameThread(false);
				minecraft2.run();
			} catch (Throwable var81) {
				LOGGER.error("Unhandled game exception", var81);
			}
		}

		BufferUploader.reset();

		try {
			minecraft2.stop();
			if (thread2 != null) {
				thread2.join();
			}
		} catch (InterruptedException var79) {
			LOGGER.error("Exception during client thread shutdown", var79);
		} finally {
			minecraft2.destroy();
		}
	}

	@Nullable
	private static String unescapeJavaArgument(@Nullable String string) {
		return string == null ? null : StringEscapeUtils.unescapeJava(string);
	}

	private static Optional<String> emptyStringToEmptyOptional(String string) {
		return string.isEmpty() ? Optional.empty() : Optional.of(string);
	}

	private static OptionalInt ofNullable(@Nullable Integer integer) {
		return integer != null ? OptionalInt.of(integer) : OptionalInt.empty();
	}

	@Nullable
	private static <T> T parseArgument(OptionSet optionSet, OptionSpec<T> optionSpec) {
		try {
			return optionSet.valueOf(optionSpec);
		} catch (Throwable var5) {
			if (optionSpec instanceof ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec) {
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
