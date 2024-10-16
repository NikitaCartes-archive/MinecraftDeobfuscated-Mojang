package net.minecraft.client.main;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.properties.PropertyMap.Serializer;
import com.mojang.blaze3d.TracyBootstrap;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.jtracy.TracyClient;
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
import java.util.concurrent.CompletableFuture;
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
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class Main {
	@DontObfuscate
	public static void main(String[] strings) {
		OptionParser optionParser = new OptionParser();
		optionParser.allowsUnrecognizedOptions();
		optionParser.accepts("demo");
		optionParser.accepts("disableMultiplayer");
		optionParser.accepts("disableChat");
		optionParser.accepts("fullscreen");
		optionParser.accepts("checkGlErrors");
		OptionSpec<Void> optionSpec = optionParser.accepts("jfrProfile");
		OptionSpec<Void> optionSpec2 = optionParser.accepts("tracy");
		OptionSpec<Void> optionSpec3 = optionParser.accepts("tracyNoImages");
		OptionSpec<String> optionSpec4 = optionParser.accepts("quickPlayPath").withRequiredArg();
		OptionSpec<String> optionSpec5 = optionParser.accepts("quickPlaySingleplayer").withRequiredArg();
		OptionSpec<String> optionSpec6 = optionParser.accepts("quickPlayMultiplayer").withRequiredArg();
		OptionSpec<String> optionSpec7 = optionParser.accepts("quickPlayRealms").withRequiredArg();
		OptionSpec<File> optionSpec8 = optionParser.accepts("gameDir").withRequiredArg().<File>ofType(File.class).defaultsTo(new File("."));
		OptionSpec<File> optionSpec9 = optionParser.accepts("assetsDir").withRequiredArg().ofType(File.class);
		OptionSpec<File> optionSpec10 = optionParser.accepts("resourcePackDir").withRequiredArg().ofType(File.class);
		OptionSpec<String> optionSpec11 = optionParser.accepts("proxyHost").withRequiredArg();
		OptionSpec<Integer> optionSpec12 = optionParser.accepts("proxyPort").withRequiredArg().defaultsTo("8080").ofType(Integer.class);
		OptionSpec<String> optionSpec13 = optionParser.accepts("proxyUser").withRequiredArg();
		OptionSpec<String> optionSpec14 = optionParser.accepts("proxyPass").withRequiredArg();
		OptionSpec<String> optionSpec15 = optionParser.accepts("username").withRequiredArg().defaultsTo("Player" + System.currentTimeMillis() % 1000L);
		OptionSpec<String> optionSpec16 = optionParser.accepts("uuid").withRequiredArg();
		OptionSpec<String> optionSpec17 = optionParser.accepts("xuid").withOptionalArg().defaultsTo("");
		OptionSpec<String> optionSpec18 = optionParser.accepts("clientId").withOptionalArg().defaultsTo("");
		OptionSpec<String> optionSpec19 = optionParser.accepts("accessToken").withRequiredArg().required();
		OptionSpec<String> optionSpec20 = optionParser.accepts("version").withRequiredArg().required();
		OptionSpec<Integer> optionSpec21 = optionParser.accepts("width").withRequiredArg().<Integer>ofType(Integer.class).defaultsTo(854);
		OptionSpec<Integer> optionSpec22 = optionParser.accepts("height").withRequiredArg().<Integer>ofType(Integer.class).defaultsTo(480);
		OptionSpec<Integer> optionSpec23 = optionParser.accepts("fullscreenWidth").withRequiredArg().ofType(Integer.class);
		OptionSpec<Integer> optionSpec24 = optionParser.accepts("fullscreenHeight").withRequiredArg().ofType(Integer.class);
		OptionSpec<String> optionSpec25 = optionParser.accepts("userProperties").withRequiredArg().defaultsTo("{}");
		OptionSpec<String> optionSpec26 = optionParser.accepts("profileProperties").withRequiredArg().defaultsTo("{}");
		OptionSpec<String> optionSpec27 = optionParser.accepts("assetIndex").withRequiredArg();
		OptionSpec<String> optionSpec28 = optionParser.accepts("userType").withRequiredArg().defaultsTo("legacy");
		OptionSpec<String> optionSpec29 = optionParser.accepts("versionType").withRequiredArg().defaultsTo("release");
		OptionSpec<String> optionSpec30 = optionParser.nonOptions();
		OptionSet optionSet = optionParser.parse(strings);
		File file = parseArgument(optionSet, optionSpec8);
		String string = parseArgument(optionSet, optionSpec20);
		String string2 = "Pre-bootstrap";

		Logger logger;
		GameConfig gameConfig;
		try {
			if (optionSet.has(optionSpec)) {
				JvmProfiler.INSTANCE.start(net.minecraft.util.profiling.jfr.Environment.CLIENT);
			}

			if (optionSet.has(optionSpec2)) {
				TracyBootstrap.setup();
			}

			Stopwatch stopwatch = Stopwatch.createStarted(Ticker.systemTicker());
			Stopwatch stopwatch2 = Stopwatch.createStarted(Ticker.systemTicker());
			GameLoadTimesEvent.INSTANCE.beginStep(TelemetryProperty.LOAD_TIME_TOTAL_TIME_MS, stopwatch);
			GameLoadTimesEvent.INSTANCE.beginStep(TelemetryProperty.LOAD_TIME_PRE_WINDOW_MS, stopwatch2);
			SharedConstants.tryDetectVersion();
			TracyClient.reportAppInfo("Minecraft Java Edition " + SharedConstants.getCurrentVersion().getName());
			CompletableFuture<?> completableFuture = DataFixers.optimize(DataFixTypes.TYPES_FOR_LEVEL_LIST);
			CrashReport.preload();
			logger = LogUtils.getLogger();
			string2 = "Bootstrap";
			Bootstrap.bootStrap();
			GameLoadTimesEvent.INSTANCE.setBootstrapTime(Bootstrap.bootstrapDuration.get());
			Bootstrap.validate();
			string2 = "Argument parsing";
			List<String> list = optionSet.valuesOf(optionSpec30);
			if (!list.isEmpty()) {
				logger.info("Completely ignored arguments: {}", list);
			}

			String string3 = optionSpec28.value(optionSet);
			User.Type type = User.Type.byName(string3);
			if (type == null) {
				logger.warn("Unrecognized user type: {}", string3);
			}

			String string4 = parseArgument(optionSet, optionSpec11);
			Proxy proxy = Proxy.NO_PROXY;
			if (string4 != null) {
				try {
					proxy = new Proxy(Type.SOCKS, new InetSocketAddress(string4, parseArgument(optionSet, optionSpec12)));
				} catch (Exception var81) {
				}
			}

			final String string5 = parseArgument(optionSet, optionSpec13);
			final String string6 = parseArgument(optionSet, optionSpec14);
			if (!proxy.equals(Proxy.NO_PROXY) && stringHasValue(string5) && stringHasValue(string6)) {
				Authenticator.setDefault(new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(string5, string6.toCharArray());
					}
				});
			}

			int i = parseArgument(optionSet, optionSpec21);
			int j = parseArgument(optionSet, optionSpec22);
			OptionalInt optionalInt = ofNullable(parseArgument(optionSet, optionSpec23));
			OptionalInt optionalInt2 = ofNullable(parseArgument(optionSet, optionSpec24));
			boolean bl = optionSet.has("fullscreen");
			boolean bl2 = optionSet.has("demo");
			boolean bl3 = optionSet.has("disableMultiplayer");
			boolean bl4 = optionSet.has("disableChat");
			boolean bl5 = !optionSet.has(optionSpec3);
			Gson gson = new GsonBuilder().registerTypeAdapter(PropertyMap.class, new Serializer()).create();
			PropertyMap propertyMap = GsonHelper.fromJson(gson, parseArgument(optionSet, optionSpec25), PropertyMap.class);
			PropertyMap propertyMap2 = GsonHelper.fromJson(gson, parseArgument(optionSet, optionSpec26), PropertyMap.class);
			String string7 = parseArgument(optionSet, optionSpec29);
			File file2 = optionSet.has(optionSpec9) ? parseArgument(optionSet, optionSpec9) : new File(file, "assets/");
			File file3 = optionSet.has(optionSpec10) ? parseArgument(optionSet, optionSpec10) : new File(file, "resourcepacks/");
			UUID uUID = hasValidUuid(optionSpec16, optionSet, logger)
				? UndashedUuid.fromStringLenient(optionSpec16.value(optionSet))
				: UUIDUtil.createOfflinePlayerUUID(optionSpec15.value(optionSet));
			String string8 = optionSet.has(optionSpec27) ? optionSpec27.value(optionSet) : null;
			String string9 = optionSet.valueOf(optionSpec17);
			String string10 = optionSet.valueOf(optionSpec18);
			String string11 = parseArgument(optionSet, optionSpec4);
			String string12 = unescapeJavaArgument(parseArgument(optionSet, optionSpec5));
			String string13 = unescapeJavaArgument(parseArgument(optionSet, optionSpec6));
			String string14 = unescapeJavaArgument(parseArgument(optionSet, optionSpec7));
			User user = new User(
				optionSpec15.value(optionSet), uUID, optionSpec19.value(optionSet), emptyStringToEmptyOptional(string9), emptyStringToEmptyOptional(string10), type
			);
			gameConfig = new GameConfig(
				new GameConfig.UserData(user, propertyMap, propertyMap2, proxy),
				new DisplayData(i, j, optionalInt, optionalInt2, bl),
				new GameConfig.FolderData(file, file3, file2, string8),
				new GameConfig.GameData(bl2, string, string7, bl3, bl4, bl5),
				new GameConfig.QuickPlayData(string11, string12, string13, string14)
			);
			Util.startTimerHackThread();
			completableFuture.join();
		} catch (Throwable var82) {
			CrashReport crashReport = CrashReport.forThrowable(var82, string2);
			CrashReportCategory crashReportCategory = crashReport.addCategory("Initialization");
			NativeModuleLister.addCrashSection(crashReportCategory);
			Minecraft.fillReport(null, null, string, null, crashReport);
			Minecraft.crash(null, file, crashReport);
			return;
		}

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
		thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(logger));
		Runtime.getRuntime().addShutdownHook(thread);
		Minecraft minecraft = null;

		try {
			Thread.currentThread().setName("Render thread");
			RenderSystem.initRenderThread();
			RenderSystem.beginInitialization();
			minecraft = new Minecraft(gameConfig);
			RenderSystem.finishInitialization();
		} catch (SilentInitException var79) {
			Util.shutdownExecutors();
			logger.warn("Failed to create window: ", (Throwable)var79);
			return;
		} catch (Throwable var80) {
			CrashReport crashReport2 = CrashReport.forThrowable(var80, "Initializing game");
			CrashReportCategory crashReportCategory2 = crashReport2.addCategory("Initialization");
			NativeModuleLister.addCrashSection(crashReportCategory2);
			Minecraft.fillReport(minecraft, null, gameConfig.game.launchVersion, null, crashReport2);
			Minecraft.crash(minecraft, gameConfig.location.gameDirectory, crashReport2);
			return;
		}

		Minecraft minecraft2 = minecraft;
		minecraft.run();
		BufferUploader.reset();

		try {
			minecraft2.stop();
		} finally {
			minecraft.destroy();
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

	private static boolean hasValidUuid(OptionSpec<String> optionSpec, OptionSet optionSet, Logger logger) {
		return optionSet.has(optionSpec) && isUuidValid(optionSpec, optionSet, logger);
	}

	private static boolean isUuidValid(OptionSpec<String> optionSpec, OptionSet optionSet, Logger logger) {
		try {
			UndashedUuid.fromStringLenient(optionSpec.value(optionSet));
			return true;
		} catch (IllegalArgumentException var4) {
			logger.warn("Invalid UUID: '{}", optionSpec.value(optionSet));
			return false;
		}
	}

	static {
		System.setProperty("java.awt.headless", "true");
	}
}
