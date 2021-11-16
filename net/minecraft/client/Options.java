/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.AmbientOcclusionStatus;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.CameraType;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.NarratorStatus;
import net.minecraft.client.Option;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.ToggleKeyMapping;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class Options {
    static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();
    private static final TypeToken<List<String>> RESOURCE_PACK_TYPE = new TypeToken<List<String>>(){};
    public static final int RENDER_DISTANCE_TINY = 2;
    public static final int RENDER_DISTANCE_SHORT = 4;
    public static final int RENDER_DISTANCE_NORMAL = 8;
    public static final int RENDER_DISTANCE_FAR = 12;
    public static final int RENDER_DISTANCE_REALLY_FAR = 16;
    public static final int RENDER_DISTANCE_EXTREME = 32;
    private static final Splitter OPTION_SPLITTER = Splitter.on(':').limit(2);
    private static final float DEFAULT_VOLUME = 1.0f;
    public static final String DEFAULT_SOUND_DEVICE = "";
    public boolean darkMojangStudiosBackground;
    public boolean hideLightningFlashes;
    public double sensitivity = 0.5;
    public int renderDistance;
    public int simulationDistance;
    private int serverRenderDistance = 0;
    public float entityDistanceScaling = 1.0f;
    public int framerateLimit = 120;
    public CloudStatus renderClouds = CloudStatus.FANCY;
    public GraphicsStatus graphicsMode = GraphicsStatus.FANCY;
    public AmbientOcclusionStatus ambientOcclusion = AmbientOcclusionStatus.MAX;
    public PrioritizeChunkUpdates prioritizeChunkUpdates = PrioritizeChunkUpdates.NONE;
    public List<String> resourcePacks = Lists.newArrayList();
    public List<String> incompatibleResourcePacks = Lists.newArrayList();
    public ChatVisiblity chatVisibility = ChatVisiblity.FULL;
    public double chatOpacity = 1.0;
    public double chatLineSpacing;
    public double textBackgroundOpacity = 0.5;
    @Nullable
    public String fullscreenVideoModeString;
    public boolean hideServerAddress;
    public boolean advancedItemTooltips;
    public boolean pauseOnLostFocus = true;
    private final Set<PlayerModelPart> modelParts = EnumSet.allOf(PlayerModelPart.class);
    public HumanoidArm mainHand = HumanoidArm.RIGHT;
    public int overrideWidth;
    public int overrideHeight;
    public boolean heldItemTooltips = true;
    public double chatScale = 1.0;
    public double chatWidth = 1.0;
    public double chatHeightUnfocused = 0.44366195797920227;
    public double chatHeightFocused = 1.0;
    public double chatDelay;
    public int mipmapLevels = 4;
    private final Object2FloatMap<SoundSource> sourceVolumes = Util.make(new Object2FloatOpenHashMap(), object2FloatOpenHashMap -> object2FloatOpenHashMap.defaultReturnValue(1.0f));
    public boolean useNativeTransport = true;
    public AttackIndicatorStatus attackIndicator = AttackIndicatorStatus.CROSSHAIR;
    public TutorialSteps tutorialStep = TutorialSteps.MOVEMENT;
    public boolean joinedFirstServer = false;
    public boolean hideBundleTutorial = false;
    public int biomeBlendRadius = 2;
    public double mouseWheelSensitivity = 1.0;
    public boolean rawMouseInput = true;
    public int glDebugVerbosity = 1;
    public boolean autoJump = true;
    public boolean autoSuggestions = true;
    public boolean chatColors = true;
    public boolean chatLinks = true;
    public boolean chatLinksPrompt = true;
    public boolean enableVsync = true;
    public boolean entityShadows = true;
    public boolean forceUnicodeFont;
    public boolean invertYMouse;
    public boolean discreteMouseScroll;
    public boolean realmsNotifications = true;
    public boolean allowServerListing = true;
    public boolean reducedDebugInfo;
    public boolean showSubtitles;
    public boolean backgroundForChatOnly = true;
    public boolean touchscreen;
    public boolean fullscreen;
    public boolean bobView = true;
    public boolean toggleCrouch;
    public boolean toggleSprint;
    public boolean skipMultiplayerWarning;
    public boolean hideMatchedNames = true;
    public boolean showAutosaveIndicator = true;
    public final KeyMapping keyUp = new KeyMapping("key.forward", 87, "key.categories.movement");
    public final KeyMapping keyLeft = new KeyMapping("key.left", 65, "key.categories.movement");
    public final KeyMapping keyDown = new KeyMapping("key.back", 83, "key.categories.movement");
    public final KeyMapping keyRight = new KeyMapping("key.right", 68, "key.categories.movement");
    public final KeyMapping keyJump = new KeyMapping("key.jump", 32, "key.categories.movement");
    public final KeyMapping keyShift = new ToggleKeyMapping("key.sneak", 340, "key.categories.movement", () -> this.toggleCrouch);
    public final KeyMapping keySprint = new ToggleKeyMapping("key.sprint", 341, "key.categories.movement", () -> this.toggleSprint);
    public final KeyMapping keyInventory = new KeyMapping("key.inventory", 69, "key.categories.inventory");
    public final KeyMapping keySwapOffhand = new KeyMapping("key.swapOffhand", 70, "key.categories.inventory");
    public final KeyMapping keyDrop = new KeyMapping("key.drop", 81, "key.categories.inventory");
    public final KeyMapping keyUse = new KeyMapping("key.use", InputConstants.Type.MOUSE, 1, "key.categories.gameplay");
    public final KeyMapping keyAttack = new KeyMapping("key.attack", InputConstants.Type.MOUSE, 0, "key.categories.gameplay");
    public final KeyMapping keyPickItem = new KeyMapping("key.pickItem", InputConstants.Type.MOUSE, 2, "key.categories.gameplay");
    public final KeyMapping keyChat = new KeyMapping("key.chat", 84, "key.categories.multiplayer");
    public final KeyMapping keyPlayerList = new KeyMapping("key.playerlist", 258, "key.categories.multiplayer");
    public final KeyMapping keyCommand = new KeyMapping("key.command", 47, "key.categories.multiplayer");
    public final KeyMapping keySocialInteractions = new KeyMapping("key.socialInteractions", 80, "key.categories.multiplayer");
    public final KeyMapping keyScreenshot = new KeyMapping("key.screenshot", 291, "key.categories.misc");
    public final KeyMapping keyTogglePerspective = new KeyMapping("key.togglePerspective", 294, "key.categories.misc");
    public final KeyMapping keySmoothCamera = new KeyMapping("key.smoothCamera", InputConstants.UNKNOWN.getValue(), "key.categories.misc");
    public final KeyMapping keyFullscreen = new KeyMapping("key.fullscreen", 300, "key.categories.misc");
    public final KeyMapping keySpectatorOutlines = new KeyMapping("key.spectatorOutlines", InputConstants.UNKNOWN.getValue(), "key.categories.misc");
    public final KeyMapping keyAdvancements = new KeyMapping("key.advancements", 76, "key.categories.misc");
    public final KeyMapping[] keyHotbarSlots = new KeyMapping[]{new KeyMapping("key.hotbar.1", 49, "key.categories.inventory"), new KeyMapping("key.hotbar.2", 50, "key.categories.inventory"), new KeyMapping("key.hotbar.3", 51, "key.categories.inventory"), new KeyMapping("key.hotbar.4", 52, "key.categories.inventory"), new KeyMapping("key.hotbar.5", 53, "key.categories.inventory"), new KeyMapping("key.hotbar.6", 54, "key.categories.inventory"), new KeyMapping("key.hotbar.7", 55, "key.categories.inventory"), new KeyMapping("key.hotbar.8", 56, "key.categories.inventory"), new KeyMapping("key.hotbar.9", 57, "key.categories.inventory")};
    public final KeyMapping keySaveHotbarActivator = new KeyMapping("key.saveToolbarActivator", 67, "key.categories.creative");
    public final KeyMapping keyLoadHotbarActivator = new KeyMapping("key.loadToolbarActivator", 88, "key.categories.creative");
    public final KeyMapping[] keyMappings = ArrayUtils.addAll(new KeyMapping[]{this.keyAttack, this.keyUse, this.keyUp, this.keyLeft, this.keyDown, this.keyRight, this.keyJump, this.keyShift, this.keySprint, this.keyDrop, this.keyInventory, this.keyChat, this.keyPlayerList, this.keyPickItem, this.keyCommand, this.keySocialInteractions, this.keyScreenshot, this.keyTogglePerspective, this.keySmoothCamera, this.keyFullscreen, this.keySpectatorOutlines, this.keySwapOffhand, this.keySaveHotbarActivator, this.keyLoadHotbarActivator, this.keyAdvancements}, this.keyHotbarSlots);
    protected Minecraft minecraft;
    private final File optionsFile;
    public Difficulty difficulty = Difficulty.NORMAL;
    public boolean hideGui;
    private CameraType cameraType = CameraType.FIRST_PERSON;
    public boolean renderDebug;
    public boolean renderDebugCharts;
    public boolean renderFpsChart;
    public String lastMpIp = "";
    public boolean smoothCamera;
    public double fov = 70.0;
    public float screenEffectScale = 1.0f;
    public float fovEffectScale = 1.0f;
    public double gamma;
    public int guiScale;
    public ParticleStatus particles = ParticleStatus.ALL;
    public NarratorStatus narratorStatus = NarratorStatus.OFF;
    public String languageCode = "en_us";
    public String soundDevice = "";
    public boolean syncWrites;

    public Options(Minecraft minecraft, File file) {
        this.minecraft = minecraft;
        this.optionsFile = new File(file, "options.txt");
        if (minecraft.is64Bit() && Runtime.getRuntime().maxMemory() >= 1000000000L) {
            Option.RENDER_DISTANCE.setMaxValue(32.0f);
            Option.SIMULATION_DISTANCE.setMaxValue(32.0f);
        } else {
            Option.RENDER_DISTANCE.setMaxValue(16.0f);
            Option.SIMULATION_DISTANCE.setMaxValue(16.0f);
        }
        this.renderDistance = minecraft.is64Bit() ? 12 : 8;
        this.simulationDistance = minecraft.is64Bit() ? 12 : 8;
        this.gamma = 0.5;
        this.syncWrites = Util.getPlatform() == Util.OS.WINDOWS;
        this.load();
    }

    public float getBackgroundOpacity(float f) {
        return this.backgroundForChatOnly ? f : (float)this.textBackgroundOpacity;
    }

    public int getBackgroundColor(float f) {
        return (int)(this.getBackgroundOpacity(f) * 255.0f) << 24 & 0xFF000000;
    }

    public int getBackgroundColor(int i) {
        return this.backgroundForChatOnly ? i : (int)(this.textBackgroundOpacity * 255.0) << 24 & 0xFF000000;
    }

    public void setKey(KeyMapping keyMapping, InputConstants.Key key) {
        keyMapping.setKey(key);
        this.save();
    }

    private void processOptions(FieldAccess fieldAccess) {
        this.autoJump = fieldAccess.process("autoJump", this.autoJump);
        this.autoSuggestions = fieldAccess.process("autoSuggestions", this.autoSuggestions);
        this.chatColors = fieldAccess.process("chatColors", this.chatColors);
        this.chatLinks = fieldAccess.process("chatLinks", this.chatLinks);
        this.chatLinksPrompt = fieldAccess.process("chatLinksPrompt", this.chatLinksPrompt);
        this.enableVsync = fieldAccess.process("enableVsync", this.enableVsync);
        this.entityShadows = fieldAccess.process("entityShadows", this.entityShadows);
        this.forceUnicodeFont = fieldAccess.process("forceUnicodeFont", this.forceUnicodeFont);
        this.discreteMouseScroll = fieldAccess.process("discrete_mouse_scroll", this.discreteMouseScroll);
        this.invertYMouse = fieldAccess.process("invertYMouse", this.invertYMouse);
        this.realmsNotifications = fieldAccess.process("realmsNotifications", this.realmsNotifications);
        this.reducedDebugInfo = fieldAccess.process("reducedDebugInfo", this.reducedDebugInfo);
        this.showSubtitles = fieldAccess.process("showSubtitles", this.showSubtitles);
        this.touchscreen = fieldAccess.process("touchscreen", this.touchscreen);
        this.fullscreen = fieldAccess.process("fullscreen", this.fullscreen);
        this.bobView = fieldAccess.process("bobView", this.bobView);
        this.toggleCrouch = fieldAccess.process("toggleCrouch", this.toggleCrouch);
        this.toggleSprint = fieldAccess.process("toggleSprint", this.toggleSprint);
        this.darkMojangStudiosBackground = fieldAccess.process("darkMojangStudiosBackground", this.darkMojangStudiosBackground);
        this.hideLightningFlashes = fieldAccess.process("hideLightningFlashes", this.hideLightningFlashes);
        this.sensitivity = fieldAccess.process("mouseSensitivity", this.sensitivity);
        this.fov = fieldAccess.process("fov", (this.fov - 70.0) / 40.0) * 40.0 + 70.0;
        this.screenEffectScale = fieldAccess.process("screenEffectScale", this.screenEffectScale);
        this.fovEffectScale = fieldAccess.process("fovEffectScale", this.fovEffectScale);
        this.gamma = fieldAccess.process("gamma", this.gamma);
        this.renderDistance = fieldAccess.process("renderDistance", this.renderDistance);
        this.simulationDistance = fieldAccess.process("simulationDistance", this.simulationDistance);
        this.entityDistanceScaling = fieldAccess.process("entityDistanceScaling", this.entityDistanceScaling);
        this.guiScale = fieldAccess.process("guiScale", this.guiScale);
        this.particles = fieldAccess.process("particles", this.particles, ParticleStatus::byId, ParticleStatus::getId);
        this.framerateLimit = fieldAccess.process("maxFps", this.framerateLimit);
        this.difficulty = fieldAccess.process("difficulty", this.difficulty, Difficulty::byId, Difficulty::getId);
        this.graphicsMode = fieldAccess.process("graphicsMode", this.graphicsMode, GraphicsStatus::byId, GraphicsStatus::getId);
        this.ambientOcclusion = fieldAccess.process("ao", this.ambientOcclusion, Options::readAmbientOcclusion, ambientOcclusionStatus -> Integer.toString(ambientOcclusionStatus.getId()));
        this.prioritizeChunkUpdates = fieldAccess.process("prioritizeChunkUpdates", this.prioritizeChunkUpdates, PrioritizeChunkUpdates::byId, PrioritizeChunkUpdates::getId);
        this.biomeBlendRadius = fieldAccess.process("biomeBlendRadius", this.biomeBlendRadius);
        this.renderClouds = fieldAccess.process("renderClouds", this.renderClouds, Options::readCloudStatus, Options::writeCloudStatus);
        this.resourcePacks = fieldAccess.process("resourcePacks", this.resourcePacks, Options::readPackList, GSON::toJson);
        this.incompatibleResourcePacks = fieldAccess.process("incompatibleResourcePacks", this.incompatibleResourcePacks, Options::readPackList, GSON::toJson);
        this.lastMpIp = fieldAccess.process("lastServer", this.lastMpIp);
        this.languageCode = fieldAccess.process("lang", this.languageCode);
        this.soundDevice = fieldAccess.process("soundDevice", this.soundDevice);
        this.chatVisibility = fieldAccess.process("chatVisibility", this.chatVisibility, ChatVisiblity::byId, ChatVisiblity::getId);
        this.chatOpacity = fieldAccess.process("chatOpacity", this.chatOpacity);
        this.chatLineSpacing = fieldAccess.process("chatLineSpacing", this.chatLineSpacing);
        this.textBackgroundOpacity = fieldAccess.process("textBackgroundOpacity", this.textBackgroundOpacity);
        this.backgroundForChatOnly = fieldAccess.process("backgroundForChatOnly", this.backgroundForChatOnly);
        this.hideServerAddress = fieldAccess.process("hideServerAddress", this.hideServerAddress);
        this.advancedItemTooltips = fieldAccess.process("advancedItemTooltips", this.advancedItemTooltips);
        this.pauseOnLostFocus = fieldAccess.process("pauseOnLostFocus", this.pauseOnLostFocus);
        this.overrideWidth = fieldAccess.process("overrideWidth", this.overrideWidth);
        this.overrideHeight = fieldAccess.process("overrideHeight", this.overrideHeight);
        this.heldItemTooltips = fieldAccess.process("heldItemTooltips", this.heldItemTooltips);
        this.chatHeightFocused = fieldAccess.process("chatHeightFocused", this.chatHeightFocused);
        this.chatDelay = fieldAccess.process("chatDelay", this.chatDelay);
        this.chatHeightUnfocused = fieldAccess.process("chatHeightUnfocused", this.chatHeightUnfocused);
        this.chatScale = fieldAccess.process("chatScale", this.chatScale);
        this.chatWidth = fieldAccess.process("chatWidth", this.chatWidth);
        this.mipmapLevels = fieldAccess.process("mipmapLevels", this.mipmapLevels);
        this.useNativeTransport = fieldAccess.process("useNativeTransport", this.useNativeTransport);
        this.mainHand = fieldAccess.process("mainHand", this.mainHand, Options::readMainHand, Options::writeMainHand);
        this.attackIndicator = fieldAccess.process("attackIndicator", this.attackIndicator, AttackIndicatorStatus::byId, AttackIndicatorStatus::getId);
        this.narratorStatus = fieldAccess.process("narrator", this.narratorStatus, NarratorStatus::byId, NarratorStatus::getId);
        this.tutorialStep = fieldAccess.process("tutorialStep", this.tutorialStep, TutorialSteps::getByName, TutorialSteps::getName);
        this.mouseWheelSensitivity = fieldAccess.process("mouseWheelSensitivity", this.mouseWheelSensitivity);
        this.rawMouseInput = fieldAccess.process("rawMouseInput", this.rawMouseInput);
        this.glDebugVerbosity = fieldAccess.process("glDebugVerbosity", this.glDebugVerbosity);
        this.skipMultiplayerWarning = fieldAccess.process("skipMultiplayerWarning", this.skipMultiplayerWarning);
        this.hideMatchedNames = fieldAccess.process("hideMatchedNames", this.hideMatchedNames);
        this.joinedFirstServer = fieldAccess.process("joinedFirstServer", this.joinedFirstServer);
        this.hideBundleTutorial = fieldAccess.process("hideBundleTutorial", this.hideBundleTutorial);
        this.syncWrites = fieldAccess.process("syncChunkWrites", this.syncWrites);
        this.showAutosaveIndicator = fieldAccess.process("showAutosaveIndicator", this.showAutosaveIndicator);
        this.allowServerListing = fieldAccess.process("allowServerListing", this.allowServerListing);
        for (KeyMapping keyMapping : this.keyMappings) {
            String string2;
            String string = keyMapping.saveString();
            if (string.equals(string2 = fieldAccess.process("key_" + keyMapping.getName(), string))) continue;
            keyMapping.setKey(InputConstants.getKey(string2));
        }
        for (SoundSource soundSource2 : SoundSource.values()) {
            this.sourceVolumes.computeFloat(soundSource2, (soundSource, float_) -> Float.valueOf(fieldAccess.process("soundCategory_" + soundSource.getName(), float_ != null ? float_.floatValue() : 1.0f)));
        }
        for (PlayerModelPart playerModelPart : PlayerModelPart.values()) {
            boolean bl = this.modelParts.contains((Object)playerModelPart);
            boolean bl2 = fieldAccess.process("modelPart_" + playerModelPart.getId(), bl);
            if (bl2 == bl) continue;
            this.setModelPart(playerModelPart, bl2);
        }
    }

    public void load() {
        try {
            if (!this.optionsFile.exists()) {
                return;
            }
            this.sourceVolumes.clear();
            CompoundTag compoundTag = new CompoundTag();
            try (BufferedReader bufferedReader = Files.newReader(this.optionsFile, Charsets.UTF_8);){
                bufferedReader.lines().forEach(string -> {
                    try {
                        Iterator<String> iterator = OPTION_SPLITTER.split((CharSequence)string).iterator();
                        compoundTag.putString(iterator.next(), iterator.next());
                    } catch (Exception exception) {
                        LOGGER.warn("Skipping bad option: {}", string);
                    }
                });
            }
            final CompoundTag compoundTag2 = this.dataFix(compoundTag);
            if (!compoundTag2.contains("graphicsMode") && compoundTag2.contains("fancyGraphics")) {
                this.graphicsMode = Options.isTrue(compoundTag2.getString("fancyGraphics")) ? GraphicsStatus.FANCY : GraphicsStatus.FAST;
            }
            this.processOptions(new FieldAccess(){

                @Nullable
                private String getValueOrNull(String string) {
                    return compoundTag2.contains(string) ? compoundTag2.getString(string) : null;
                }

                @Override
                public int process(String string, int i) {
                    String string2 = this.getValueOrNull(string);
                    if (string2 != null) {
                        try {
                            return Integer.parseInt(string2);
                        } catch (NumberFormatException numberFormatException) {
                            LOGGER.warn("Invalid integer value for option {} = {}", (Object)string, (Object)string2, (Object)numberFormatException);
                        }
                    }
                    return i;
                }

                @Override
                public boolean process(String string, boolean bl) {
                    String string2 = this.getValueOrNull(string);
                    return string2 != null ? Options.isTrue(string2) : bl;
                }

                @Override
                public String process(String string, String string2) {
                    return MoreObjects.firstNonNull(this.getValueOrNull(string), string2);
                }

                @Override
                public double process(String string, double d) {
                    String string2 = this.getValueOrNull(string);
                    if (string2 != null) {
                        if (Options.isTrue(string2)) {
                            return 1.0;
                        }
                        if (Options.isFalse(string2)) {
                            return 0.0;
                        }
                        try {
                            return Double.parseDouble(string2);
                        } catch (NumberFormatException numberFormatException) {
                            LOGGER.warn("Invalid floating point value for option {} = {}", (Object)string, (Object)string2, (Object)numberFormatException);
                        }
                    }
                    return d;
                }

                @Override
                public float process(String string, float f) {
                    String string2 = this.getValueOrNull(string);
                    if (string2 != null) {
                        if (Options.isTrue(string2)) {
                            return 1.0f;
                        }
                        if (Options.isFalse(string2)) {
                            return 0.0f;
                        }
                        try {
                            return Float.parseFloat(string2);
                        } catch (NumberFormatException numberFormatException) {
                            LOGGER.warn("Invalid floating point value for option {} = {}", (Object)string, (Object)string2, (Object)numberFormatException);
                        }
                    }
                    return f;
                }

                @Override
                public <T> T process(String string, T object, Function<String, T> function, Function<T, String> function2) {
                    String string2 = this.getValueOrNull(string);
                    return string2 == null ? object : function.apply(string2);
                }

                @Override
                public <T> T process(String string, T object, IntFunction<T> intFunction, ToIntFunction<T> toIntFunction) {
                    String string2 = this.getValueOrNull(string);
                    if (string2 != null) {
                        try {
                            return intFunction.apply(Integer.parseInt(string2));
                        } catch (Exception exception) {
                            LOGGER.warn("Invalid integer value for option {} = {}", (Object)string, (Object)string2, (Object)exception);
                        }
                    }
                    return object;
                }
            });
            if (compoundTag2.contains("fullscreenResolution")) {
                this.fullscreenVideoModeString = compoundTag2.getString("fullscreenResolution");
            }
            if (this.minecraft.getWindow() != null) {
                this.minecraft.getWindow().setFramerateLimit(this.framerateLimit);
            }
            KeyMapping.resetMapping();
        } catch (Exception exception) {
            LOGGER.error("Failed to load options", (Throwable)exception);
        }
    }

    static boolean isTrue(String string) {
        return "true".equals(string);
    }

    static boolean isFalse(String string) {
        return "false".equals(string);
    }

    private CompoundTag dataFix(CompoundTag compoundTag) {
        int i = 0;
        try {
            i = Integer.parseInt(compoundTag.getString("version"));
        } catch (RuntimeException runtimeException) {
            // empty catch block
        }
        return NbtUtils.update(this.minecraft.getFixerUpper(), DataFixTypes.OPTIONS, compoundTag, i);
    }

    public void save() {
        try (final PrintWriter printWriter = new PrintWriter(new OutputStreamWriter((OutputStream)new FileOutputStream(this.optionsFile), StandardCharsets.UTF_8));){
            printWriter.println("version:" + SharedConstants.getCurrentVersion().getWorldVersion());
            this.processOptions(new FieldAccess(){

                public void writePrefix(String string) {
                    printWriter.print(string);
                    printWriter.print(':');
                }

                @Override
                public int process(String string, int i) {
                    this.writePrefix(string);
                    printWriter.println(i);
                    return i;
                }

                @Override
                public boolean process(String string, boolean bl) {
                    this.writePrefix(string);
                    printWriter.println(bl);
                    return bl;
                }

                @Override
                public String process(String string, String string2) {
                    this.writePrefix(string);
                    printWriter.println(string2);
                    return string2;
                }

                @Override
                public double process(String string, double d) {
                    this.writePrefix(string);
                    printWriter.println(d);
                    return d;
                }

                @Override
                public float process(String string, float f) {
                    this.writePrefix(string);
                    printWriter.println(f);
                    return f;
                }

                @Override
                public <T> T process(String string, T object, Function<String, T> function, Function<T, String> function2) {
                    this.writePrefix(string);
                    printWriter.println(function2.apply(object));
                    return object;
                }

                @Override
                public <T> T process(String string, T object, IntFunction<T> intFunction, ToIntFunction<T> toIntFunction) {
                    this.writePrefix(string);
                    printWriter.println(toIntFunction.applyAsInt(object));
                    return object;
                }
            });
            if (this.minecraft.getWindow().getPreferredFullscreenVideoMode().isPresent()) {
                printWriter.println("fullscreenResolution:" + this.minecraft.getWindow().getPreferredFullscreenVideoMode().get().write());
            }
        } catch (Exception exception) {
            LOGGER.error("Failed to save options", (Throwable)exception);
        }
        this.broadcastOptions();
    }

    public float getSoundSourceVolume(SoundSource soundSource) {
        return this.sourceVolumes.getFloat((Object)soundSource);
    }

    public void setSoundCategoryVolume(SoundSource soundSource, float f) {
        this.sourceVolumes.put(soundSource, f);
        this.minecraft.getSoundManager().updateSourceVolume(soundSource, f);
    }

    public void broadcastOptions() {
        if (this.minecraft.player != null) {
            int i = 0;
            for (PlayerModelPart playerModelPart : this.modelParts) {
                i |= playerModelPart.getMask();
            }
            this.minecraft.player.connection.send(new ServerboundClientInformationPacket(this.languageCode, this.renderDistance, this.chatVisibility, this.chatColors, i, this.mainHand, this.minecraft.isTextFilteringEnabled(), this.allowServerListing));
        }
    }

    private void setModelPart(PlayerModelPart playerModelPart, boolean bl) {
        if (bl) {
            this.modelParts.add(playerModelPart);
        } else {
            this.modelParts.remove((Object)playerModelPart);
        }
    }

    public boolean isModelPartEnabled(PlayerModelPart playerModelPart) {
        return this.modelParts.contains((Object)playerModelPart);
    }

    public void toggleModelPart(PlayerModelPart playerModelPart, boolean bl) {
        this.setModelPart(playerModelPart, bl);
        this.broadcastOptions();
    }

    public CloudStatus getCloudsType() {
        if (this.getEffectiveRenderDistance() >= 4) {
            return this.renderClouds;
        }
        return CloudStatus.OFF;
    }

    public boolean useNativeTransport() {
        return this.useNativeTransport;
    }

    public void loadSelectedResourcePacks(PackRepository packRepository) {
        LinkedHashSet<String> set = Sets.newLinkedHashSet();
        Iterator<String> iterator = this.resourcePacks.iterator();
        while (iterator.hasNext()) {
            String string = iterator.next();
            Pack pack = packRepository.getPack(string);
            if (pack == null && !string.startsWith("file/")) {
                pack = packRepository.getPack("file/" + string);
            }
            if (pack == null) {
                LOGGER.warn("Removed resource pack {} from options because it doesn't seem to exist anymore", (Object)string);
                iterator.remove();
                continue;
            }
            if (!pack.getCompatibility().isCompatible() && !this.incompatibleResourcePacks.contains(string)) {
                LOGGER.warn("Removed resource pack {} from options because it is no longer compatible", (Object)string);
                iterator.remove();
                continue;
            }
            if (pack.getCompatibility().isCompatible() && this.incompatibleResourcePacks.contains(string)) {
                LOGGER.info("Removed resource pack {} from incompatibility list because it's now compatible", (Object)string);
                this.incompatibleResourcePacks.remove(string);
                continue;
            }
            set.add(pack.getId());
        }
        packRepository.setSelected(set);
    }

    public CameraType getCameraType() {
        return this.cameraType;
    }

    public void setCameraType(CameraType cameraType) {
        this.cameraType = cameraType;
    }

    private static List<String> readPackList(String string) {
        List<String> list = GsonHelper.fromJson(GSON, string, RESOURCE_PACK_TYPE);
        return list != null ? list : Lists.newArrayList();
    }

    private static CloudStatus readCloudStatus(String string) {
        switch (string) {
            case "true": {
                return CloudStatus.FANCY;
            }
            case "fast": {
                return CloudStatus.FAST;
            }
        }
        return CloudStatus.OFF;
    }

    private static String writeCloudStatus(CloudStatus cloudStatus) {
        switch (cloudStatus) {
            case FANCY: {
                return "true";
            }
            case FAST: {
                return "fast";
            }
        }
        return "false";
    }

    private static AmbientOcclusionStatus readAmbientOcclusion(String string) {
        if (Options.isTrue(string)) {
            return AmbientOcclusionStatus.MAX;
        }
        if (Options.isFalse(string)) {
            return AmbientOcclusionStatus.OFF;
        }
        return AmbientOcclusionStatus.byId(Integer.parseInt(string));
    }

    private static HumanoidArm readMainHand(String string) {
        return "left".equals(string) ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }

    private static String writeMainHand(HumanoidArm humanoidArm) {
        return humanoidArm == HumanoidArm.LEFT ? "left" : "right";
    }

    public File getFile() {
        return this.optionsFile;
    }

    public String dumpOptionsForReport() {
        ImmutableCollection immutableList = ((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)ImmutableList.builder().add(Pair.of("ao", String.valueOf((Object)this.ambientOcclusion)))).add(Pair.of("biomeBlendRadius", String.valueOf(this.biomeBlendRadius)))).add(Pair.of("enableVsync", String.valueOf(this.enableVsync)))).add(Pair.of("entityDistanceScaling", String.valueOf(this.entityDistanceScaling)))).add(Pair.of("entityShadows", String.valueOf(this.entityShadows)))).add(Pair.of("forceUnicodeFont", String.valueOf(this.forceUnicodeFont)))).add(Pair.of("fov", String.valueOf(this.fov)))).add(Pair.of("fovEffectScale", String.valueOf(this.fovEffectScale)))).add(Pair.of("prioritizeChunkUpdates", String.valueOf((Object)this.prioritizeChunkUpdates)))).add(Pair.of("fullscreen", String.valueOf(this.fullscreen)))).add(Pair.of("fullscreenResolution", String.valueOf(this.fullscreenVideoModeString)))).add(Pair.of("gamma", String.valueOf(this.gamma)))).add(Pair.of("glDebugVerbosity", String.valueOf(this.glDebugVerbosity)))).add(Pair.of("graphicsMode", String.valueOf((Object)this.graphicsMode)))).add(Pair.of("guiScale", String.valueOf(this.guiScale)))).add(Pair.of("maxFps", String.valueOf(this.framerateLimit)))).add(Pair.of("mipmapLevels", String.valueOf(this.mipmapLevels)))).add(Pair.of("narrator", String.valueOf((Object)this.narratorStatus)))).add(Pair.of("overrideHeight", String.valueOf(this.overrideHeight)))).add(Pair.of("overrideWidth", String.valueOf(this.overrideWidth)))).add(Pair.of("particles", String.valueOf((Object)this.particles)))).add(Pair.of("reducedDebugInfo", String.valueOf(this.reducedDebugInfo)))).add(Pair.of("renderClouds", String.valueOf((Object)this.renderClouds)))).add(Pair.of("renderDistance", String.valueOf(this.renderDistance)))).add(Pair.of("simulationDistance", String.valueOf(this.simulationDistance)))).add(Pair.of("resourcePacks", String.valueOf(this.resourcePacks)))).add(Pair.of("screenEffectScale", String.valueOf(this.screenEffectScale)))).add(Pair.of("syncChunkWrites", String.valueOf(this.syncWrites)))).add(Pair.of("useNativeTransport", String.valueOf(this.useNativeTransport)))).add(Pair.of("soundDevice", String.valueOf(this.soundDevice)))).build();
        return immutableList.stream().map(pair -> (String)pair.getFirst() + ": " + (String)pair.getSecond()).collect(Collectors.joining(System.lineSeparator()));
    }

    public void setServerRenderDistance(int i) {
        this.serverRenderDistance = i;
    }

    public int getEffectiveRenderDistance() {
        return this.serverRenderDistance > 0 ? Math.min(this.renderDistance, this.serverRenderDistance) : this.renderDistance;
    }

    @Environment(value=EnvType.CLIENT)
    static interface FieldAccess {
        public int process(String var1, int var2);

        public boolean process(String var1, boolean var2);

        public String process(String var1, String var2);

        public double process(String var1, double var2);

        public float process(String var1, float var2);

        public <T> T process(String var1, T var2, Function<String, T> var3, Function<T, String> var4);

        public <T> T process(String var1, T var2, IntFunction<T> var3, ToIntFunction<T> var4);
    }
}

