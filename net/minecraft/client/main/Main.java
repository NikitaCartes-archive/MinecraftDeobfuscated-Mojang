/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import java.io.File;
import java.lang.reflect.Type;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import net.fabricmc.api.EnvType;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.main.SilentInitException;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.NativeModuleLister;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@net.fabricmc.api.Environment(value=EnvType.CLIENT)
public class Main {
    static final Logger LOGGER = LogManager.getLogger();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @DontObfuscate
    public static void main(String[] strings) {
        Thread thread2;
        Minecraft minecraft;
        SharedConstants.tryDetectVersion();
        OptionParser optionParser = new OptionParser();
        optionParser.allowsUnrecognizedOptions();
        optionParser.accepts("demo");
        optionParser.accepts("disableMultiplayer");
        optionParser.accepts("disableChat");
        optionParser.accepts("fullscreen");
        optionParser.accepts("checkGlErrors");
        OptionSpecBuilder optionSpec = optionParser.accepts("jfrProfile");
        ArgumentAcceptingOptionSpec<String> optionSpec2 = optionParser.accepts("server").withRequiredArg();
        ArgumentAcceptingOptionSpec<Integer> optionSpec3 = optionParser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(25565, (Integer[])new Integer[0]);
        ArgumentAcceptingOptionSpec<File> optionSpec4 = optionParser.accepts("gameDir").withRequiredArg().ofType(File.class).defaultsTo(new File("."), (File[])new File[0]);
        ArgumentAcceptingOptionSpec<File> optionSpec5 = optionParser.accepts("assetsDir").withRequiredArg().ofType(File.class);
        ArgumentAcceptingOptionSpec<File> optionSpec6 = optionParser.accepts("resourcePackDir").withRequiredArg().ofType(File.class);
        ArgumentAcceptingOptionSpec<String> optionSpec7 = optionParser.accepts("proxyHost").withRequiredArg();
        ArgumentAcceptingOptionSpec<Integer> optionSpec8 = optionParser.accepts("proxyPort").withRequiredArg().defaultsTo("8080", (String[])new String[0]).ofType(Integer.class);
        ArgumentAcceptingOptionSpec<String> optionSpec9 = optionParser.accepts("proxyUser").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> optionSpec10 = optionParser.accepts("proxyPass").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> optionSpec11 = optionParser.accepts("username").withRequiredArg().defaultsTo("Player" + Util.getMillis() % 1000L, (String[])new String[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec12 = optionParser.accepts("uuid").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> optionSpec13 = optionParser.accepts("xuid").withOptionalArg().defaultsTo("", (String[])new String[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec14 = optionParser.accepts("clientId").withOptionalArg().defaultsTo("", (String[])new String[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec15 = optionParser.accepts("accessToken").withRequiredArg().required();
        ArgumentAcceptingOptionSpec<String> optionSpec16 = optionParser.accepts("version").withRequiredArg().required();
        ArgumentAcceptingOptionSpec<Integer> optionSpec17 = optionParser.accepts("width").withRequiredArg().ofType(Integer.class).defaultsTo(854, (Integer[])new Integer[0]);
        ArgumentAcceptingOptionSpec<Integer> optionSpec18 = optionParser.accepts("height").withRequiredArg().ofType(Integer.class).defaultsTo(480, (Integer[])new Integer[0]);
        ArgumentAcceptingOptionSpec<Integer> optionSpec19 = optionParser.accepts("fullscreenWidth").withRequiredArg().ofType(Integer.class);
        ArgumentAcceptingOptionSpec<Integer> optionSpec20 = optionParser.accepts("fullscreenHeight").withRequiredArg().ofType(Integer.class);
        ArgumentAcceptingOptionSpec<String> optionSpec21 = optionParser.accepts("userProperties").withRequiredArg().defaultsTo("{}", (String[])new String[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec22 = optionParser.accepts("profileProperties").withRequiredArg().defaultsTo("{}", (String[])new String[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec23 = optionParser.accepts("assetIndex").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> optionSpec24 = optionParser.accepts("userType").withRequiredArg().defaultsTo(User.Type.LEGACY.getName(), (String[])new String[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec25 = optionParser.accepts("versionType").withRequiredArg().defaultsTo("release", (String[])new String[0]);
        NonOptionArgumentSpec<String> optionSpec26 = optionParser.nonOptions();
        OptionSet optionSet = optionParser.parse(strings);
        List<String> list = optionSet.valuesOf(optionSpec26);
        if (!list.isEmpty()) {
            System.out.println("Completely ignored arguments: " + list);
        }
        String string = Main.parseArgument(optionSet, optionSpec7);
        Proxy proxy = Proxy.NO_PROXY;
        if (string != null) {
            try {
                proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(string, (int)Main.parseArgument(optionSet, optionSpec8)));
            } catch (Exception exception) {
                // empty catch block
            }
        }
        final String string2 = Main.parseArgument(optionSet, optionSpec9);
        final String string3 = Main.parseArgument(optionSet, optionSpec10);
        if (!proxy.equals(Proxy.NO_PROXY) && Main.stringHasValue(string2) && Main.stringHasValue(string3)) {
            Authenticator.setDefault(new Authenticator(){

                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(string2, string3.toCharArray());
                }
            });
        }
        int i = Main.parseArgument(optionSet, optionSpec17);
        int j = Main.parseArgument(optionSet, optionSpec18);
        OptionalInt optionalInt = Main.ofNullable(Main.parseArgument(optionSet, optionSpec19));
        OptionalInt optionalInt2 = Main.ofNullable(Main.parseArgument(optionSet, optionSpec20));
        boolean bl = optionSet.has("fullscreen");
        boolean bl2 = optionSet.has("demo");
        boolean bl3 = optionSet.has("disableMultiplayer");
        boolean bl4 = optionSet.has("disableChat");
        String string4 = Main.parseArgument(optionSet, optionSpec16);
        Gson gson = new GsonBuilder().registerTypeAdapter((Type)((Object)PropertyMap.class), new PropertyMap.Serializer()).create();
        PropertyMap propertyMap = GsonHelper.fromJson(gson, Main.parseArgument(optionSet, optionSpec21), PropertyMap.class);
        PropertyMap propertyMap2 = GsonHelper.fromJson(gson, Main.parseArgument(optionSet, optionSpec22), PropertyMap.class);
        String string5 = Main.parseArgument(optionSet, optionSpec25);
        File file = Main.parseArgument(optionSet, optionSpec4);
        File file2 = optionSet.has(optionSpec5) ? Main.parseArgument(optionSet, optionSpec5) : new File(file, "assets/");
        File file3 = optionSet.has(optionSpec6) ? Main.parseArgument(optionSet, optionSpec6) : new File(file, "resourcepacks/");
        String string6 = optionSet.has(optionSpec12) ? (String)optionSpec12.value(optionSet) : Player.createPlayerUUID((String)optionSpec11.value(optionSet)).toString();
        String string7 = optionSet.has(optionSpec23) ? (String)optionSpec23.value(optionSet) : null;
        String string8 = optionSet.valueOf(optionSpec13);
        String string9 = optionSet.valueOf(optionSpec14);
        String string10 = Main.parseArgument(optionSet, optionSpec2);
        Integer integer = Main.parseArgument(optionSet, optionSpec3);
        if (optionSet.has(optionSpec)) {
            JvmProfiler.INSTANCE.start(Environment.CLIENT);
        }
        JvmProfiler.INSTANCE.initialize();
        CrashReport.preload();
        Bootstrap.bootStrap();
        Bootstrap.validate();
        Util.startTimerHackThread();
        String string11 = (String)optionSpec24.value(optionSet);
        User.Type type = User.Type.byName(string11);
        if (type == null) {
            LOGGER.warn("Unrecognized user type: {}", (Object)string11);
        }
        User user = new User((String)optionSpec11.value(optionSet), string6, (String)optionSpec15.value(optionSet), Main.emptyStringToEmptyOptional(string8), Main.emptyStringToEmptyOptional(string9), type);
        GameConfig gameConfig = new GameConfig(new GameConfig.UserData(user, propertyMap, propertyMap2, proxy), new DisplayData(i, j, optionalInt, optionalInt2, bl), new GameConfig.FolderData(file, file3, file2, string7), new GameConfig.GameData(bl2, string4, string5, bl3, bl4), new GameConfig.ServerData(string10, integer));
        Thread thread = new Thread("Client Shutdown Thread"){

            @Override
            public void run() {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft == null) {
                    return;
                }
                IntegratedServer integratedServer = minecraft.getSingleplayerServer();
                if (integratedServer != null) {
                    integratedServer.halt(true);
                }
            }
        };
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        Runtime.getRuntime().addShutdownHook(thread);
        try {
            Thread.currentThread().setName("Render thread");
            RenderSystem.initRenderThread();
            RenderSystem.beginInitialization();
            minecraft = new Minecraft(gameConfig);
            RenderSystem.finishInitialization();
        } catch (SilentInitException silentInitException) {
            LOGGER.warn("Failed to create window: ", (Throwable)silentInitException);
            return;
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Initializing game");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Initialization");
            NativeModuleLister.addCrashSection(crashReportCategory);
            Minecraft.fillReport(null, null, gameConfig.game.launchVersion, null, crashReport);
            Minecraft.crash(crashReport);
            return;
        }
        if (minecraft.renderOnThread()) {
            thread2 = new Thread("Game thread"){

                @Override
                public void run() {
                    try {
                        RenderSystem.initGameThread(true);
                        minecraft.run();
                    } catch (Throwable throwable) {
                        LOGGER.error("Exception in client thread", throwable);
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
            } catch (Throwable throwable2) {
                LOGGER.error("Unhandled game exception", throwable2);
            }
        }
        BufferUploader.reset();
        try {
            minecraft.stop();
            if (thread2 != null) {
                thread2.join();
            }
        } catch (InterruptedException interruptedException) {
            LOGGER.error("Exception during client thread shutdown", (Throwable)interruptedException);
        } finally {
            minecraft.destroy();
        }
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
        } catch (Throwable throwable) {
            ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec;
            List list;
            if (optionSpec instanceof ArgumentAcceptingOptionSpec && !(list = (argumentAcceptingOptionSpec = (ArgumentAcceptingOptionSpec)optionSpec).defaultValues()).isEmpty()) {
                return (T)list.get(0);
            }
            throw throwable;
        }
    }

    private static boolean stringHasValue(@Nullable String string) {
        return string != null && !string.isEmpty();
    }

    static {
        System.setProperty("java.awt.headless", "true");
    }
}

