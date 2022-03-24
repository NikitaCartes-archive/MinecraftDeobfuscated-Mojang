/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
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
import net.minecraft.client.OptionInstance;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.ToggleKeyMapping;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class Options {
    static final Logger LOGGER = LogUtils.getLogger();
    static final Gson GSON = new Gson();
    private static final TypeToken<List<String>> RESOURCE_PACK_TYPE = new TypeToken<List<String>>(){};
    public static final int RENDER_DISTANCE_TINY = 2;
    public static final int RENDER_DISTANCE_SHORT = 4;
    public static final int RENDER_DISTANCE_NORMAL = 8;
    public static final int RENDER_DISTANCE_FAR = 12;
    public static final int RENDER_DISTANCE_REALLY_FAR = 16;
    public static final int RENDER_DISTANCE_EXTREME = 32;
    private static final int TOOLTIP_WIDTH = 200;
    private static final Splitter OPTION_SPLITTER = Splitter.on(':').limit(2);
    private static final float DEFAULT_VOLUME = 1.0f;
    public static final String DEFAULT_SOUND_DEVICE = "";
    private static final Component ACCESSIBILITY_TOOLTIP_DARK_MOJANG_BACKGROUND = new TranslatableComponent("options.darkMojangStudiosBackgroundColor.tooltip");
    private final OptionInstance<Boolean> darkMojangStudiosBackground = OptionInstance.createBoolean("options.darkMojangStudiosBackgroundColor", minecraft -> {
        List<FormattedCharSequence> list = minecraft.font.split(ACCESSIBILITY_TOOLTIP_DARK_MOJANG_BACKGROUND, 200);
        return boolean_ -> list;
    }, false);
    private static final Component ACCESSIBILITY_TOOLTIP_HIDE_LIGHTNING_FLASHES = new TranslatableComponent("options.hideLightningFlashes.tooltip");
    private final OptionInstance<Boolean> hideLightningFlash = OptionInstance.createBoolean("options.hideLightningFlashes", minecraft -> {
        List<FormattedCharSequence> list = minecraft.font.split(ACCESSIBILITY_TOOLTIP_HIDE_LIGHTNING_FLASHES, 200);
        return boolean_ -> list;
    }, false);
    private final OptionInstance<Double> sensitivity = new OptionInstance<Double>("options.sensitivity", OptionInstance.noTooltip(), double_ -> {
        Component component = this.sensitivity().getCaption();
        if (double_ == 0.0) {
            return Options.genericValueLabel(component, new TranslatableComponent("options.sensitivity.min"));
        }
        if (double_ == 1.0) {
            return Options.genericValueLabel(component, new TranslatableComponent("options.sensitivity.max"));
        }
        return Options.percentValueLabel(component, 2.0 * double_);
    }, OptionInstance.UnitDouble.INSTANCE, 0.5, double_ -> {});
    private final OptionInstance<Integer> renderDistance;
    private final OptionInstance<Integer> simulationDistance;
    private int serverRenderDistance = 0;
    private final OptionInstance<Double> entityDistanceScaling = new OptionInstance<Double>("options.entityDistanceScaling", OptionInstance.noTooltip(), double_ -> Options.percentValueLabel(this.entityDistanceScaling().getCaption(), double_), new OptionInstance.IntRange(2, 20).xmap(i -> (double)i / 4.0, double_ -> (int)(double_ * 4.0)), Codec.doubleRange(0.5, 5.0), 1.0, double_ -> {});
    public static final int UNLIMITED_FRAMERATE_CUTOFF = 260;
    private final OptionInstance<Integer> framerateLimit = new OptionInstance<Integer>("options.framerateLimit", OptionInstance.noTooltip(), integer -> {
        Component component = this.framerateLimit().getCaption();
        if (integer == 260) {
            return Options.genericValueLabel(component, new TranslatableComponent("options.framerateLimit.max"));
        }
        return Options.genericValueLabel(component, new TranslatableComponent("options.framerate", integer));
    }, new OptionInstance.IntRange(1, 26).xmap(i -> i * 10, integer -> integer / 10), Codec.intRange(10, 260), 120, integer -> Minecraft.getInstance().getWindow().setFramerateLimit((int)integer));
    private final OptionInstance<CloudStatus> cloudStatus = new OptionInstance<CloudStatus>("options.renderClouds", OptionInstance.noTooltip(), cloudStatus -> new TranslatableComponent(cloudStatus.getKey()), new OptionInstance.Enum<CloudStatus>(Arrays.asList(CloudStatus.values()), Codec.either(Codec.BOOL, Codec.STRING).xmap(either -> either.map(boolean_ -> boolean_ != false ? CloudStatus.FANCY : CloudStatus.OFF, string -> switch (string) {
        case "true" -> CloudStatus.FANCY;
        case "fast" -> CloudStatus.FAST;
        default -> CloudStatus.OFF;
    }), cloudStatus -> Either.right(switch (cloudStatus) {
        default -> throw new IncompatibleClassChangeError();
        case CloudStatus.FANCY -> "true";
        case CloudStatus.FAST -> "fast";
        case CloudStatus.OFF -> "false";
    }))), CloudStatus.FANCY, cloudStatus -> {
        RenderTarget renderTarget;
        if (Minecraft.useShaderTransparency() && (renderTarget = Minecraft.getInstance().levelRenderer.getCloudsTarget()) != null) {
            renderTarget.clear(Minecraft.ON_OSX);
        }
    });
    private static final Component GRAPHICS_TOOLTIP_FAST = new TranslatableComponent("options.graphics.fast.tooltip");
    private static final Component GRAPHICS_TOOLTIP_FABULOUS = new TranslatableComponent("options.graphics.fabulous.tooltip", new TranslatableComponent("options.graphics.fabulous").withStyle(ChatFormatting.ITALIC));
    private static final Component GRAPHICS_TOOLTIP_FANCY = new TranslatableComponent("options.graphics.fancy.tooltip");
    private final OptionInstance<GraphicsStatus> graphicsMode = new OptionInstance<GraphicsStatus>("options.graphics", minecraft -> {
        List<FormattedCharSequence> list = minecraft.font.split(GRAPHICS_TOOLTIP_FAST, 200);
        List<FormattedCharSequence> list2 = minecraft.font.split(GRAPHICS_TOOLTIP_FANCY, 200);
        List<FormattedCharSequence> list3 = minecraft.font.split(GRAPHICS_TOOLTIP_FABULOUS, 200);
        return graphicsStatus -> switch (graphicsStatus) {
            default -> throw new IncompatibleClassChangeError();
            case GraphicsStatus.FANCY -> list2;
            case GraphicsStatus.FAST -> list;
            case GraphicsStatus.FABULOUS -> list3;
        };
    }, graphicsStatus -> {
        TranslatableComponent mutableComponent = new TranslatableComponent(graphicsStatus.getKey());
        if (graphicsStatus == GraphicsStatus.FABULOUS) {
            return mutableComponent.withStyle(ChatFormatting.ITALIC);
        }
        return mutableComponent;
    }, new OptionInstance.AltEnum<GraphicsStatus>(Arrays.asList(GraphicsStatus.values()), Stream.of(GraphicsStatus.values()).filter(graphicsStatus -> graphicsStatus != GraphicsStatus.FABULOUS).collect(Collectors.toList()), () -> Minecraft.getInstance().isRunning() && Minecraft.getInstance().getGpuWarnlistManager().isSkippingFabulous(), (optionInstance, graphicsStatus) -> {
        Minecraft minecraft = Minecraft.getInstance();
        GpuWarnlistManager gpuWarnlistManager = minecraft.getGpuWarnlistManager();
        if (graphicsStatus == GraphicsStatus.FABULOUS && gpuWarnlistManager.willShowWarning()) {
            gpuWarnlistManager.showWarning();
            return;
        }
        optionInstance.set(graphicsStatus);
        minecraft.levelRenderer.allChanged();
    }, Codec.INT.xmap(GraphicsStatus::byId, GraphicsStatus::getId)), GraphicsStatus.FANCY, graphicsStatus -> {});
    private final OptionInstance<AmbientOcclusionStatus> ambientOcclusion = new OptionInstance<AmbientOcclusionStatus>("options.ao", OptionInstance.noTooltip(), ambientOcclusionStatus -> new TranslatableComponent(ambientOcclusionStatus.getKey()), new OptionInstance.Enum<AmbientOcclusionStatus>(Arrays.asList(AmbientOcclusionStatus.values()), Codec.either(Codec.BOOL.xmap(boolean_ -> boolean_ != false ? AmbientOcclusionStatus.MAX.getId() : AmbientOcclusionStatus.OFF.getId(), integer -> integer.intValue() == AmbientOcclusionStatus.MAX.getId()), Codec.INT).xmap(either -> either.map(integer -> integer, integer -> integer), Either::right).xmap(AmbientOcclusionStatus::byId, AmbientOcclusionStatus::getId)), AmbientOcclusionStatus.MAX, ambientOcclusionStatus -> Minecraft.getInstance().levelRenderer.allChanged());
    private static final Component PRIORITIZE_CHUNK_TOOLTIP_NONE = new TranslatableComponent("options.prioritizeChunkUpdates.none.tooltip");
    private static final Component PRIORITIZE_CHUNK_TOOLTIP_PLAYER_AFFECTED = new TranslatableComponent("options.prioritizeChunkUpdates.byPlayer.tooltip");
    private static final Component PRIORITIZE_CHUNK_TOOLTIP_NEARBY = new TranslatableComponent("options.prioritizeChunkUpdates.nearby.tooltip");
    private final OptionInstance<PrioritizeChunkUpdates> prioritizeChunkUpdates = new OptionInstance<PrioritizeChunkUpdates>("options.prioritizeChunkUpdates", minecraft -> prioritizeChunkUpdates -> switch (prioritizeChunkUpdates) {
        default -> throw new IncompatibleClassChangeError();
        case PrioritizeChunkUpdates.NONE -> minecraft.font.split(PRIORITIZE_CHUNK_TOOLTIP_NONE, 200);
        case PrioritizeChunkUpdates.PLAYER_AFFECTED -> minecraft.font.split(PRIORITIZE_CHUNK_TOOLTIP_PLAYER_AFFECTED, 200);
        case PrioritizeChunkUpdates.NEARBY -> minecraft.font.split(PRIORITIZE_CHUNK_TOOLTIP_NEARBY, 200);
    }, prioritizeChunkUpdates -> new TranslatableComponent(prioritizeChunkUpdates.getKey()), new OptionInstance.Enum<PrioritizeChunkUpdates>(Arrays.asList(PrioritizeChunkUpdates.values()), Codec.INT.xmap(PrioritizeChunkUpdates::byId, PrioritizeChunkUpdates::getId)), PrioritizeChunkUpdates.NONE, prioritizeChunkUpdates -> {});
    public List<String> resourcePacks = Lists.newArrayList();
    public List<String> incompatibleResourcePacks = Lists.newArrayList();
    private final OptionInstance<ChatVisiblity> chatVisibility = new OptionInstance<ChatVisiblity>("options.chat.visibility", OptionInstance.noTooltip(), chatVisiblity -> new TranslatableComponent(chatVisiblity.getKey()), new OptionInstance.Enum<ChatVisiblity>(Arrays.asList(ChatVisiblity.values()), Codec.INT.xmap(ChatVisiblity::byId, ChatVisiblity::getId)), ChatVisiblity.FULL, chatVisiblity -> {});
    private final OptionInstance<Double> chatOpacity = new OptionInstance<Double>("options.chat.opacity", OptionInstance.noTooltip(), double_ -> Options.percentValueLabel(this.chatOpacity().getCaption(), double_ * 0.9 + 0.1), OptionInstance.UnitDouble.INSTANCE, 1.0, double_ -> Minecraft.getInstance().gui.getChat().rescaleChat());
    private final OptionInstance<Double> chatLineSpacing = new OptionInstance<Double>("options.chat.line_spacing", OptionInstance.noTooltip(), double_ -> Options.percentValueLabel(this.chatLineSpacing().getCaption(), double_), OptionInstance.UnitDouble.INSTANCE, 0.0, double_ -> {});
    private final OptionInstance<Double> textBackgroundOpacity = new OptionInstance<Double>("options.accessibility.text_background_opacity", OptionInstance.noTooltip(), double_ -> Options.percentValueLabel(this.textBackgroundOpacity().getCaption(), double_), OptionInstance.UnitDouble.INSTANCE, 0.5, double_ -> Minecraft.getInstance().gui.getChat().rescaleChat());
    @Nullable
    public String fullscreenVideoModeString;
    public boolean hideServerAddress;
    public boolean advancedItemTooltips;
    public boolean pauseOnLostFocus = true;
    private final Set<PlayerModelPart> modelParts = EnumSet.allOf(PlayerModelPart.class);
    private final OptionInstance<HumanoidArm> mainHand = new OptionInstance<HumanoidArm>("options.mainHand", OptionInstance.noTooltip(), HumanoidArm::getName, new OptionInstance.Enum<HumanoidArm>(Arrays.asList(HumanoidArm.values()), Codec.STRING.xmap(string -> "left".equals(string) ? HumanoidArm.LEFT : HumanoidArm.RIGHT, humanoidArm -> humanoidArm == HumanoidArm.LEFT ? "left" : "right")), HumanoidArm.RIGHT, humanoidArm -> this.broadcastOptions());
    public int overrideWidth;
    public int overrideHeight;
    public boolean heldItemTooltips = true;
    private final OptionInstance<Double> chatScale = new OptionInstance<Double>("options.chat.scale", OptionInstance.noTooltip(), double_ -> {
        if (double_ == 0.0) {
            return CommonComponents.optionStatus(this.chatScale().getCaption(), false);
        }
        return Options.percentValueLabel(this.chatScale().getCaption(), double_);
    }, OptionInstance.UnitDouble.INSTANCE, 0.0, double_ -> Minecraft.getInstance().gui.getChat().rescaleChat());
    private final OptionInstance<Double> chatWidth = new OptionInstance<Double>("options.chat.width", OptionInstance.noTooltip(), double_ -> Options.pixelValueLabel(this.chatWidth().getCaption(), ChatComponent.getWidth(double_)), OptionInstance.UnitDouble.INSTANCE, 1.0, double_ -> Minecraft.getInstance().gui.getChat().rescaleChat());
    private final OptionInstance<Double> chatHeightUnfocused = new OptionInstance<Double>("options.chat.height.unfocused", OptionInstance.noTooltip(), double_ -> Options.pixelValueLabel(this.chatHeightUnfocused().getCaption(), ChatComponent.getHeight(double_)), OptionInstance.UnitDouble.INSTANCE, ChatComponent.defaultUnfocusedPct(), double_ -> Minecraft.getInstance().gui.getChat().rescaleChat());
    private final OptionInstance<Double> chatHeightFocused = new OptionInstance<Double>("options.chat.height.focused", OptionInstance.noTooltip(), double_ -> Options.pixelValueLabel(this.chatHeightFocused().getCaption(), ChatComponent.getHeight(double_)), OptionInstance.UnitDouble.INSTANCE, 1.0, double_ -> Minecraft.getInstance().gui.getChat().rescaleChat());
    private final OptionInstance<Double> chatDelay = new OptionInstance<Double>("options.chat.delay_instant", OptionInstance.noTooltip(), double_ -> {
        if (double_ <= 0.0) {
            return new TranslatableComponent("options.chat.delay_none");
        }
        return new TranslatableComponent("options.chat.delay", String.format("%.1f", double_));
    }, new OptionInstance.IntRange(0, 60).xmap(i -> (double)i / 10.0, double_ -> (int)(double_ * 10.0)), Codec.doubleRange(0.0, 6.0), 0.0, double_ -> {});
    private final OptionInstance<Integer> mipmapLevels = new OptionInstance<Integer>("options.mipmapLevels", OptionInstance.noTooltip(), integer -> {
        Component component = this.mipmapLevels().getCaption();
        if (integer == 0) {
            return CommonComponents.optionStatus(component, false);
        }
        return Options.genericValueLabel(component, integer);
    }, new OptionInstance.IntRange(0, 4), 4, integer -> {});
    private final Object2FloatMap<SoundSource> sourceVolumes = Util.make(new Object2FloatOpenHashMap(), object2FloatOpenHashMap -> object2FloatOpenHashMap.defaultReturnValue(1.0f));
    public boolean useNativeTransport = true;
    private final OptionInstance<AttackIndicatorStatus> attackIndicator = new OptionInstance<AttackIndicatorStatus>("options.attackIndicator", OptionInstance.noTooltip(), attackIndicatorStatus -> new TranslatableComponent(attackIndicatorStatus.getKey()), new OptionInstance.Enum<AttackIndicatorStatus>(Arrays.asList(AttackIndicatorStatus.values()), Codec.INT.xmap(AttackIndicatorStatus::byId, AttackIndicatorStatus::getId)), AttackIndicatorStatus.CROSSHAIR, attackIndicatorStatus -> {});
    public TutorialSteps tutorialStep = TutorialSteps.MOVEMENT;
    public boolean joinedFirstServer = false;
    public boolean hideBundleTutorial = false;
    private final OptionInstance<Integer> biomeBlendRadius = new OptionInstance<Integer>("options.biomeBlendRadius", OptionInstance.noTooltip(), integer -> {
        int i = integer * 2 + 1;
        return Options.genericValueLabel(this.biomeBlendRadius().getCaption(), new TranslatableComponent("options.biomeBlendRadius." + i));
    }, new OptionInstance.IntRange(0, 7), 2, integer -> Minecraft.getInstance().levelRenderer.allChanged());
    private final OptionInstance<Double> mouseWheelSensitivity = new OptionInstance<Double>("options.mouseWheelSensitivity", OptionInstance.noTooltip(), double_ -> Options.genericValueLabel(this.mouseWheelSensitivity().getCaption(), new TextComponent(String.format("%.2f", double_))), new OptionInstance.IntRange(-200, 100).xmap(Options::logMouse, Options::unlogMouse), Codec.doubleRange(Options.logMouse(-200), Options.logMouse(100)), Options.logMouse(0), double_ -> {});
    private final OptionInstance<Boolean> rawMouseInput = OptionInstance.createBoolean("options.rawMouseInput", true, boolean_ -> {
        Window window = Minecraft.getInstance().getWindow();
        if (window != null) {
            window.updateRawMouseInput((boolean)boolean_);
        }
    });
    public int glDebugVerbosity = 1;
    private final OptionInstance<Boolean> autoJump = OptionInstance.createBoolean("options.autoJump", true);
    private final OptionInstance<Boolean> autoSuggestions = OptionInstance.createBoolean("options.autoSuggestCommand", true);
    private final OptionInstance<Boolean> chatColors = OptionInstance.createBoolean("options.chat.color", true);
    private final OptionInstance<Boolean> chatLinks = OptionInstance.createBoolean("options.chat.links", true);
    private final OptionInstance<Boolean> chatLinksPrompt = OptionInstance.createBoolean("options.chat.links.prompt", true);
    private final OptionInstance<Boolean> enableVsync = OptionInstance.createBoolean("options.vsync", true, boolean_ -> {
        if (Minecraft.getInstance().getWindow() != null) {
            Minecraft.getInstance().getWindow().updateVsync((boolean)boolean_);
        }
    });
    private final OptionInstance<Boolean> entityShadows = OptionInstance.createBoolean("options.entityShadows", true);
    private final OptionInstance<Boolean> forceUnicodeFont = OptionInstance.createBoolean("options.forceUnicodeFont", false, boolean_ -> {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getWindow() != null) {
            minecraft.selectMainFont((boolean)boolean_);
            minecraft.resizeDisplay();
        }
    });
    private final OptionInstance<Boolean> invertYMouse = OptionInstance.createBoolean("options.invertMouse", false);
    private final OptionInstance<Boolean> discreteMouseScroll = OptionInstance.createBoolean("options.discrete_mouse_scroll", false);
    private final OptionInstance<Boolean> realmsNotifications = OptionInstance.createBoolean("optionss.realmsNotifications", true);
    private static final Component ALLOW_SERVER_LISTING_TOOLTIP = new TranslatableComponent("options.allowServerListing.tooltip");
    private final OptionInstance<Boolean> allowServerListing = OptionInstance.createBoolean("options.allowServerListing", minecraft -> {
        List<FormattedCharSequence> list = minecraft.font.split(ALLOW_SERVER_LISTING_TOOLTIP, 200);
        return boolean_ -> list;
    }, true, boolean_ -> this.broadcastOptions());
    private final OptionInstance<Boolean> reducedDebugInfo = OptionInstance.createBoolean("options.reducedDebugInfo", false);
    private final OptionInstance<Boolean> showSubtitles = OptionInstance.createBoolean("options.showSubtitles", false);
    private static final Component DIRECTIONAL_AUDIO_TOOLTIP_ON = new TranslatableComponent("options.directionalAudio.on.tooltip");
    private static final Component DIRECTIONAL_AUDIO_TOOLTIP_OFF = new TranslatableComponent("options.directionalAudio.off.tooltip");
    private final OptionInstance<Boolean> directionalAudio = OptionInstance.createBoolean("options.directionalAudio", minecraft -> {
        List<FormattedCharSequence> list = minecraft.font.split(DIRECTIONAL_AUDIO_TOOLTIP_ON, 200);
        List<FormattedCharSequence> list2 = minecraft.font.split(DIRECTIONAL_AUDIO_TOOLTIP_OFF, 200);
        return boolean_ -> boolean_ != false ? list : list2;
    }, false, boolean_ -> {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        soundManager.reload();
        soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    });
    private final OptionInstance<Boolean> backgroundForChatOnly = new OptionInstance<Boolean>("options.accessibility.text_background", OptionInstance.noTooltip(), boolean_ -> boolean_ != false ? new TranslatableComponent("options.accessibility.text_background.chat") : new TranslatableComponent("options.accessibility.text_background.everywhere"), OptionInstance.BOOLEAN_VALUES, true, boolean_ -> {});
    private final OptionInstance<Boolean> touchscreen = OptionInstance.createBoolean("options.touchscreen", false);
    private final OptionInstance<Boolean> fullscreen = OptionInstance.createBoolean("options.fullscreen", false, boolean_ -> {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getWindow() != null && minecraft.getWindow().isFullscreen() != boolean_.booleanValue()) {
            minecraft.getWindow().toggleFullScreen();
            this.fullscreen().set(minecraft.getWindow().isFullscreen());
        }
    });
    private final OptionInstance<Boolean> bobView = OptionInstance.createBoolean("options.viewBobbing", true);
    private static final Component MOVEMENT_TOGGLE = new TranslatableComponent("options.key.toggle");
    private static final Component MOVEMENT_HOLD = new TranslatableComponent("options.key.hold");
    private final OptionInstance<Boolean> toggleCrouch = new OptionInstance<Boolean>("key.sneak", OptionInstance.noTooltip(), boolean_ -> boolean_ != false ? MOVEMENT_TOGGLE : MOVEMENT_HOLD, OptionInstance.BOOLEAN_VALUES, false, boolean_ -> {});
    private final OptionInstance<Boolean> toggleSprint = new OptionInstance<Boolean>("key.sprint", OptionInstance.noTooltip(), boolean_ -> boolean_ != false ? MOVEMENT_TOGGLE : MOVEMENT_HOLD, OptionInstance.BOOLEAN_VALUES, false, boolean_ -> {});
    public boolean skipMultiplayerWarning;
    public boolean skipRealms32bitWarning;
    private static final Component CHAT_TOOLTIP_HIDE_MATCHED_NAMES = new TranslatableComponent("options.hideMatchedNames.tooltip");
    private final OptionInstance<Boolean> hideMatchedNames = OptionInstance.createBoolean("options.hideMatchedNames", minecraft -> {
        List<FormattedCharSequence> list = minecraft.font.split(CHAT_TOOLTIP_HIDE_MATCHED_NAMES, 200);
        return boolean_ -> list;
    }, true);
    private final OptionInstance<Boolean> showAutosaveIndicator = OptionInstance.createBoolean("options.autosaveIndicator", true);
    public final KeyMapping keyUp = new KeyMapping("key.forward", 87, "key.categories.movement");
    public final KeyMapping keyLeft = new KeyMapping("key.left", 65, "key.categories.movement");
    public final KeyMapping keyDown = new KeyMapping("key.back", 83, "key.categories.movement");
    public final KeyMapping keyRight = new KeyMapping("key.right", 68, "key.categories.movement");
    public final KeyMapping keyJump = new KeyMapping("key.jump", 32, "key.categories.movement");
    public final KeyMapping keyShift = new ToggleKeyMapping("key.sneak", 340, "key.categories.movement", this.toggleCrouch::get);
    public final KeyMapping keySprint = new ToggleKeyMapping("key.sprint", 341, "key.categories.movement", this.toggleSprint::get);
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
    public boolean hideGui;
    private CameraType cameraType = CameraType.FIRST_PERSON;
    public boolean renderDebug;
    public boolean renderDebugCharts;
    public boolean renderFpsChart;
    public String lastMpIp = "";
    public boolean smoothCamera;
    private final OptionInstance<Integer> fov = new OptionInstance<Integer>("options.fov", OptionInstance.noTooltip(), integer -> {
        Component component = this.fov().getCaption();
        return switch (integer) {
            case 70 -> Options.genericValueLabel(component, new TranslatableComponent("options.fov.min"));
            case 110 -> Options.genericValueLabel(component, new TranslatableComponent("options.fov.max"));
            default -> Options.genericValueLabel(component, integer);
        };
    }, new OptionInstance.IntRange(30, 110), Codec.DOUBLE.xmap(double_ -> (int)(double_ * 40.0 + 70.0), integer -> ((double)integer.intValue() - 70.0) / 40.0), 70, integer -> Minecraft.getInstance().levelRenderer.needsUpdate());
    private static final Component ACCESSIBILITY_TOOLTIP_SCREEN_EFFECT = new TranslatableComponent("options.screenEffectScale.tooltip");
    private final OptionInstance<Double> screenEffectScale = new OptionInstance<Double>("options.screenEffectScale", minecraft -> double_ -> minecraft.font.split(ACCESSIBILITY_TOOLTIP_SCREEN_EFFECT, 200), double_ -> {
        Component component = this.screenEffectScale().getCaption();
        if (double_ == 0.0) {
            return Options.genericValueLabel(component, CommonComponents.OPTION_OFF);
        }
        return Options.percentValueLabel(component, double_);
    }, OptionInstance.UnitDouble.INSTANCE, 1.0, double_ -> {});
    private static final Component ACCESSIBILITY_TOOLTIP_FOV_EFFECT = new TranslatableComponent("options.fovEffectScale.tooltip");
    private final OptionInstance<Double> fovEffectScale = new OptionInstance<Double>("options.fovEffectScale", minecraft -> double_ -> minecraft.font.split(ACCESSIBILITY_TOOLTIP_FOV_EFFECT, 200), double_ -> {
        Component component = this.fovEffectScale().getCaption();
        if (double_ == 0.0) {
            return Options.genericValueLabel(component, CommonComponents.OPTION_OFF);
        }
        return Options.percentValueLabel(component, double_);
    }, OptionInstance.UnitDouble.INSTANCE.xmap(Mth::square, Math::sqrt), Codec.doubleRange(0.0, 1.0), 1.0, double_ -> {});
    private static final Component ACCESSIBILITY_TOOLTIP_DARKNESS_EFFECT = new TranslatableComponent("options.darknessEffectScale.tooltip");
    private final OptionInstance<Double> darknessEffectScale = new OptionInstance<Double>("options.darknessEffectScale", minecraft -> double_ -> minecraft.font.split(ACCESSIBILITY_TOOLTIP_DARKNESS_EFFECT, 200), double_ -> {
        Component component = this.darknessEffectScale().getCaption();
        if (double_ == 0.0) {
            return Options.genericValueLabel(component, CommonComponents.OPTION_OFF);
        }
        return Options.percentValueLabel(component, double_);
    }, OptionInstance.UnitDouble.INSTANCE.xmap(Mth::square, Math::sqrt), 1.0, double_ -> {});
    private final OptionInstance<Double> gamma = new OptionInstance<Double>("options.gamma", OptionInstance.noTooltip(), double_ -> {
        Component component = this.gamma().getCaption();
        int i = (int)(double_ * 100.0);
        if (i == 0) {
            return Options.genericValueLabel(component, new TranslatableComponent("options.gamma.min"));
        }
        if (i == 50) {
            return Options.genericValueLabel(component, new TranslatableComponent("options.gamma.default"));
        }
        if (i == 100) {
            return Options.genericValueLabel(component, new TranslatableComponent("options.gamma.max"));
        }
        return Options.genericValueLabel(component, i);
    }, OptionInstance.UnitDouble.INSTANCE, 0.5, double_ -> {});
    private final OptionInstance<Integer> guiScale = new OptionInstance<Integer>("options.guiScale", OptionInstance.noTooltip(), integer -> integer == 0 ? new TranslatableComponent("options.guiScale.auto") : new TextComponent(Integer.toString(integer)), OptionInstance.clampingLazyMax(0, () -> {
        Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.isRunning()) {
            return 0x7FFFFFFE;
        }
        return minecraft.getWindow().calculateScale(0, minecraft.isEnforceUnicode());
    }), 0, integer -> {});
    private final OptionInstance<ParticleStatus> particles = new OptionInstance<ParticleStatus>("options.particles", OptionInstance.noTooltip(), particleStatus -> new TranslatableComponent(particleStatus.getKey()), new OptionInstance.Enum<ParticleStatus>(Arrays.asList(ParticleStatus.values()), Codec.INT.xmap(ParticleStatus::byId, ParticleStatus::getId)), ParticleStatus.ALL, particleStatus -> {});
    private final OptionInstance<NarratorStatus> narrator = new OptionInstance<NarratorStatus>("options.narrator", OptionInstance.noTooltip(), narratorStatus -> {
        if (NarratorChatListener.INSTANCE.isActive()) {
            return narratorStatus.getName();
        }
        return new TranslatableComponent("options.narrator.notavailable");
    }, new OptionInstance.Enum<NarratorStatus>(Arrays.asList(NarratorStatus.values()), Codec.INT.xmap(NarratorStatus::byId, NarratorStatus::getId)), NarratorStatus.OFF, narratorStatus -> NarratorChatListener.INSTANCE.updateNarratorStatus((NarratorStatus)((Object)narratorStatus)));
    public String languageCode = "en_us";
    private final OptionInstance<String> soundDevice = new OptionInstance<String>("options.audioDevice", OptionInstance.noTooltip(), string -> {
        if (DEFAULT_SOUND_DEVICE.equals(string)) {
            return new TranslatableComponent("options.audioDevice.default");
        }
        if (string.startsWith("OpenAL Soft on ")) {
            return new TextComponent(string.substring(SoundEngine.OPEN_AL_SOFT_PREFIX_LENGTH));
        }
        return new TextComponent((String)string);
    }, new OptionInstance.LazyEnum<String>(() -> Stream.concat(Stream.of(DEFAULT_SOUND_DEVICE), Minecraft.getInstance().getSoundManager().getAvailableSoundDevices().stream()).toList(), string -> {
        if (!Minecraft.getInstance().isRunning() || string == DEFAULT_SOUND_DEVICE || Minecraft.getInstance().getSoundManager().getAvailableSoundDevices().contains(string)) {
            return Optional.of(string);
        }
        return Optional.empty();
    }, Codec.STRING), "", string -> {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        soundManager.reload();
        soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    });
    public boolean syncWrites;

    public OptionInstance<Boolean> darkMojangStudiosBackground() {
        return this.darkMojangStudiosBackground;
    }

    public OptionInstance<Boolean> hideLightningFlash() {
        return this.hideLightningFlash;
    }

    public OptionInstance<Double> sensitivity() {
        return this.sensitivity;
    }

    public OptionInstance<Integer> renderDistance() {
        return this.renderDistance;
    }

    public OptionInstance<Integer> simulationDistance() {
        return this.simulationDistance;
    }

    public OptionInstance<Double> entityDistanceScaling() {
        return this.entityDistanceScaling;
    }

    public OptionInstance<Integer> framerateLimit() {
        return this.framerateLimit;
    }

    public OptionInstance<CloudStatus> cloudStatus() {
        return this.cloudStatus;
    }

    public OptionInstance<GraphicsStatus> graphicsMode() {
        return this.graphicsMode;
    }

    public OptionInstance<AmbientOcclusionStatus> ambientOcclusion() {
        return this.ambientOcclusion;
    }

    public OptionInstance<PrioritizeChunkUpdates> prioritizeChunkUpdates() {
        return this.prioritizeChunkUpdates;
    }

    public OptionInstance<ChatVisiblity> chatVisibility() {
        return this.chatVisibility;
    }

    public OptionInstance<Double> chatOpacity() {
        return this.chatOpacity;
    }

    public OptionInstance<Double> chatLineSpacing() {
        return this.chatLineSpacing;
    }

    public OptionInstance<Double> textBackgroundOpacity() {
        return this.textBackgroundOpacity;
    }

    public OptionInstance<HumanoidArm> mainHand() {
        return this.mainHand;
    }

    public OptionInstance<Double> chatScale() {
        return this.chatScale;
    }

    public OptionInstance<Double> chatWidth() {
        return this.chatWidth;
    }

    public OptionInstance<Double> chatHeightUnfocused() {
        return this.chatHeightUnfocused;
    }

    public OptionInstance<Double> chatHeightFocused() {
        return this.chatHeightFocused;
    }

    public OptionInstance<Double> chatDelay() {
        return this.chatDelay;
    }

    public OptionInstance<Integer> mipmapLevels() {
        return this.mipmapLevels;
    }

    public OptionInstance<AttackIndicatorStatus> attackIndicator() {
        return this.attackIndicator;
    }

    public OptionInstance<Integer> biomeBlendRadius() {
        return this.biomeBlendRadius;
    }

    private static double logMouse(int i) {
        return Math.pow(10.0, (double)i / 100.0);
    }

    private static int unlogMouse(double d) {
        return Mth.floor(Math.log10(d) * 100.0);
    }

    public OptionInstance<Double> mouseWheelSensitivity() {
        return this.mouseWheelSensitivity;
    }

    public OptionInstance<Boolean> rawMouseInput() {
        return this.rawMouseInput;
    }

    public OptionInstance<Boolean> autoJump() {
        return this.autoJump;
    }

    public OptionInstance<Boolean> autoSuggestions() {
        return this.autoSuggestions;
    }

    public OptionInstance<Boolean> chatColors() {
        return this.chatColors;
    }

    public OptionInstance<Boolean> chatLinks() {
        return this.chatLinks;
    }

    public OptionInstance<Boolean> chatLinksPrompt() {
        return this.chatLinksPrompt;
    }

    public OptionInstance<Boolean> enableVsync() {
        return this.enableVsync;
    }

    public OptionInstance<Boolean> entityShadows() {
        return this.entityShadows;
    }

    public OptionInstance<Boolean> forceUnicodeFont() {
        return this.forceUnicodeFont;
    }

    public OptionInstance<Boolean> invertYMouse() {
        return this.invertYMouse;
    }

    public OptionInstance<Boolean> discreteMouseScroll() {
        return this.discreteMouseScroll;
    }

    public OptionInstance<Boolean> realmsNotifications() {
        return this.realmsNotifications;
    }

    public OptionInstance<Boolean> allowServerListing() {
        return this.allowServerListing;
    }

    public OptionInstance<Boolean> reducedDebugInfo() {
        return this.reducedDebugInfo;
    }

    public OptionInstance<Boolean> showSubtitles() {
        return this.showSubtitles;
    }

    public OptionInstance<Boolean> directionalAudio() {
        return this.directionalAudio;
    }

    public OptionInstance<Boolean> backgroundForChatOnly() {
        return this.backgroundForChatOnly;
    }

    public OptionInstance<Boolean> touchscreen() {
        return this.touchscreen;
    }

    public OptionInstance<Boolean> fullscreen() {
        return this.fullscreen;
    }

    public OptionInstance<Boolean> bobView() {
        return this.bobView;
    }

    public OptionInstance<Boolean> toggleCrouch() {
        return this.toggleCrouch;
    }

    public OptionInstance<Boolean> toggleSprint() {
        return this.toggleSprint;
    }

    public OptionInstance<Boolean> hideMatchedNames() {
        return this.hideMatchedNames;
    }

    public OptionInstance<Boolean> showAutosaveIndicator() {
        return this.showAutosaveIndicator;
    }

    public OptionInstance<Integer> fov() {
        return this.fov;
    }

    public OptionInstance<Double> screenEffectScale() {
        return this.screenEffectScale;
    }

    public OptionInstance<Double> fovEffectScale() {
        return this.fovEffectScale;
    }

    public OptionInstance<Double> darknessEffectScale() {
        return this.darknessEffectScale;
    }

    public OptionInstance<Double> gamma() {
        return this.gamma;
    }

    public OptionInstance<Integer> guiScale() {
        return this.guiScale;
    }

    public OptionInstance<ParticleStatus> particles() {
        return this.particles;
    }

    public OptionInstance<NarratorStatus> narrator() {
        return this.narrator;
    }

    public OptionInstance<String> soundDevice() {
        return this.soundDevice;
    }

    public Options(Minecraft minecraft2, File file) {
        this.minecraft = minecraft2;
        this.optionsFile = new File(file, "options.txt");
        boolean bl = minecraft2.is64Bit();
        boolean bl2 = bl && Runtime.getRuntime().maxMemory() >= 1000000000L;
        this.renderDistance = new OptionInstance<Integer>("options.renderDistance", OptionInstance.noTooltip(), integer -> Options.genericValueLabel(this.renderDistance().getCaption(), new TranslatableComponent("options.chunks", integer)), new OptionInstance.IntRange(2, bl2 ? 32 : 16), bl ? 12 : 8, integer -> Minecraft.getInstance().levelRenderer.needsUpdate());
        this.simulationDistance = new OptionInstance<Integer>("options.simulationDistance", OptionInstance.noTooltip(), integer -> Options.genericValueLabel(this.simulationDistance().getCaption(), new TranslatableComponent("options.chunks", integer)), new OptionInstance.IntRange(5, bl2 ? 32 : 16), bl ? 12 : 8, integer -> {});
        this.syncWrites = Util.getPlatform() == Util.OS.WINDOWS;
        this.load();
    }

    public float getBackgroundOpacity(float f) {
        return this.backgroundForChatOnly.get() != false ? f : this.textBackgroundOpacity().get().floatValue();
    }

    public int getBackgroundColor(float f) {
        return (int)(this.getBackgroundOpacity(f) * 255.0f) << 24 & 0xFF000000;
    }

    public int getBackgroundColor(int i) {
        return this.backgroundForChatOnly.get() != false ? i : (int)(this.textBackgroundOpacity.get() * 255.0) << 24 & 0xFF000000;
    }

    public void setKey(KeyMapping keyMapping, InputConstants.Key key) {
        keyMapping.setKey(key);
        this.save();
    }

    private void processOptions(FieldAccess fieldAccess) {
        fieldAccess.process("autoJump", this.autoJump);
        fieldAccess.process("autoSuggestions", this.autoSuggestions);
        fieldAccess.process("chatColors", this.chatColors);
        fieldAccess.process("chatLinks", this.chatLinks);
        fieldAccess.process("chatLinksPrompt", this.chatLinksPrompt);
        fieldAccess.process("enableVsync", this.enableVsync);
        fieldAccess.process("entityShadows", this.entityShadows);
        fieldAccess.process("forceUnicodeFont", this.forceUnicodeFont);
        fieldAccess.process("discrete_mouse_scroll", this.discreteMouseScroll);
        fieldAccess.process("invertYMouse", this.invertYMouse);
        fieldAccess.process("realmsNotifications", this.realmsNotifications);
        fieldAccess.process("reducedDebugInfo", this.reducedDebugInfo);
        fieldAccess.process("showSubtitles", this.showSubtitles);
        fieldAccess.process("directionalAudio", this.directionalAudio);
        fieldAccess.process("touchscreen", this.touchscreen);
        fieldAccess.process("fullscreen", this.fullscreen);
        fieldAccess.process("bobView", this.bobView);
        fieldAccess.process("toggleCrouch", this.toggleCrouch);
        fieldAccess.process("toggleSprint", this.toggleSprint);
        fieldAccess.process("darkMojangStudiosBackground", this.darkMojangStudiosBackground);
        fieldAccess.process("hideLightningFlashes", this.hideLightningFlash);
        fieldAccess.process("mouseSensitivity", this.sensitivity);
        fieldAccess.process("fov", this.fov);
        fieldAccess.process("screenEffectScale", this.screenEffectScale);
        fieldAccess.process("fovEffectScale", this.fovEffectScale);
        fieldAccess.process("gamma", this.gamma);
        fieldAccess.process("renderDistance", this.renderDistance);
        fieldAccess.process("simulationDistance", this.simulationDistance);
        fieldAccess.process("entityDistanceScaling", this.entityDistanceScaling);
        fieldAccess.process("guiScale", this.guiScale);
        fieldAccess.process("particles", this.particles);
        fieldAccess.process("maxFps", this.framerateLimit);
        fieldAccess.process("graphicsMode", this.graphicsMode);
        fieldAccess.process("ao", this.ambientOcclusion);
        fieldAccess.process("prioritizeChunkUpdates", this.prioritizeChunkUpdates);
        fieldAccess.process("biomeBlendRadius", this.biomeBlendRadius);
        fieldAccess.process("renderClouds", this.cloudStatus);
        this.resourcePacks = fieldAccess.process("resourcePacks", this.resourcePacks, Options::readPackList, GSON::toJson);
        this.incompatibleResourcePacks = fieldAccess.process("incompatibleResourcePacks", this.incompatibleResourcePacks, Options::readPackList, GSON::toJson);
        this.lastMpIp = fieldAccess.process("lastServer", this.lastMpIp);
        this.languageCode = fieldAccess.process("lang", this.languageCode);
        fieldAccess.process("soundDevice", this.soundDevice);
        fieldAccess.process("chatVisibility", this.chatVisibility);
        fieldAccess.process("chatOpacity", this.chatOpacity);
        fieldAccess.process("chatLineSpacing", this.chatLineSpacing);
        fieldAccess.process("textBackgroundOpacity", this.textBackgroundOpacity);
        fieldAccess.process("backgroundForChatOnly", this.backgroundForChatOnly);
        this.hideServerAddress = fieldAccess.process("hideServerAddress", this.hideServerAddress);
        this.advancedItemTooltips = fieldAccess.process("advancedItemTooltips", this.advancedItemTooltips);
        this.pauseOnLostFocus = fieldAccess.process("pauseOnLostFocus", this.pauseOnLostFocus);
        this.overrideWidth = fieldAccess.process("overrideWidth", this.overrideWidth);
        this.overrideHeight = fieldAccess.process("overrideHeight", this.overrideHeight);
        this.heldItemTooltips = fieldAccess.process("heldItemTooltips", this.heldItemTooltips);
        fieldAccess.process("chatHeightFocused", this.chatHeightFocused);
        fieldAccess.process("chatDelay", this.chatDelay);
        fieldAccess.process("chatHeightUnfocused", this.chatHeightUnfocused);
        fieldAccess.process("chatScale", this.chatScale);
        fieldAccess.process("chatWidth", this.chatWidth);
        fieldAccess.process("mipmapLevels", this.mipmapLevels);
        this.useNativeTransport = fieldAccess.process("useNativeTransport", this.useNativeTransport);
        fieldAccess.process("mainHand", this.mainHand);
        fieldAccess.process("attackIndicator", this.attackIndicator);
        fieldAccess.process("narrator", this.narrator);
        this.tutorialStep = fieldAccess.process("tutorialStep", this.tutorialStep, TutorialSteps::getByName, TutorialSteps::getName);
        fieldAccess.process("mouseWheelSensitivity", this.mouseWheelSensitivity);
        fieldAccess.process("rawMouseInput", this.rawMouseInput);
        this.glDebugVerbosity = fieldAccess.process("glDebugVerbosity", this.glDebugVerbosity);
        this.skipMultiplayerWarning = fieldAccess.process("skipMultiplayerWarning", this.skipMultiplayerWarning);
        this.skipRealms32bitWarning = fieldAccess.process("skipRealms32bitWarning", this.skipRealms32bitWarning);
        fieldAccess.process("hideMatchedNames", this.hideMatchedNames);
        this.joinedFirstServer = fieldAccess.process("joinedFirstServer", this.joinedFirstServer);
        this.hideBundleTutorial = fieldAccess.process("hideBundleTutorial", this.hideBundleTutorial);
        this.syncWrites = fieldAccess.process("syncChunkWrites", this.syncWrites);
        fieldAccess.process("showAutosaveIndicator", this.showAutosaveIndicator);
        fieldAccess.process("allowServerListing", this.allowServerListing);
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
                if (Options.isTrue(compoundTag2.getString("fancyGraphics"))) {
                    this.graphicsMode.set(GraphicsStatus.FANCY);
                } else {
                    this.graphicsMode.set(GraphicsStatus.FAST);
                }
            }
            this.processOptions(new FieldAccess(){

                @Nullable
                private String getValueOrNull(String string) {
                    return compoundTag2.contains(string) ? compoundTag2.getString(string) : null;
                }

                @Override
                public <T> void process(String string, OptionInstance<T> optionInstance) {
                    String string2 = this.getValueOrNull(string);
                    if (string2 != null) {
                        JsonReader jsonReader = new JsonReader(new StringReader(string2.isEmpty() ? "\"\"" : string2));
                        JsonElement jsonElement = JsonParser.parseReader(jsonReader);
                        DataResult dataResult = optionInstance.codec().parse(JsonOps.INSTANCE, jsonElement);
                        dataResult.error().ifPresent(partialResult -> LOGGER.error("Error parsing option value " + string2 + " for option " + optionInstance + ": " + partialResult.message()));
                        dataResult.result().ifPresent(optionInstance::set);
                    }
                }

                @Override
                public int process(String string, int i) {
                    String string2 = this.getValueOrNull(string);
                    if (string2 != null) {
                        try {
                            return Integer.parseInt(string2);
                        } catch (NumberFormatException numberFormatException) {
                            LOGGER.warn("Invalid integer value for option {} = {}", string, string2, numberFormatException);
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
                            LOGGER.warn("Invalid floating point value for option {} = {}", string, string2, numberFormatException);
                        }
                    }
                    return f;
                }

                @Override
                public <T> T process(String string, T object, Function<String, T> function, Function<T, String> function2) {
                    String string2 = this.getValueOrNull(string);
                    return string2 == null ? object : function.apply(string2);
                }
            });
            if (compoundTag2.contains("fullscreenResolution")) {
                this.fullscreenVideoModeString = compoundTag2.getString("fullscreenResolution");
            }
            if (this.minecraft.getWindow() != null) {
                this.minecraft.getWindow().setFramerateLimit(this.framerateLimit.get());
            }
            KeyMapping.resetMapping();
        } catch (Exception exception) {
            LOGGER.error("Failed to load options", exception);
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
                public <T> void process(String string, OptionInstance<T> optionInstance) {
                    DataResult<JsonElement> dataResult = optionInstance.codec().encodeStart(JsonOps.INSTANCE, (JsonElement)optionInstance.get());
                    dataResult.error().ifPresent(partialResult -> LOGGER.error("Error saving option " + optionInstance + ": " + partialResult));
                    dataResult.result().ifPresent(jsonElement -> {
                        this.writePrefix(string);
                        printWriter.println(GSON.toJson((JsonElement)jsonElement));
                    });
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
            });
            if (this.minecraft.getWindow().getPreferredFullscreenVideoMode().isPresent()) {
                printWriter.println("fullscreenResolution:" + this.minecraft.getWindow().getPreferredFullscreenVideoMode().get().write());
            }
        } catch (Exception exception) {
            LOGGER.error("Failed to save options", exception);
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
            this.minecraft.player.connection.send(new ServerboundClientInformationPacket(this.languageCode, this.renderDistance.get(), this.chatVisibility.get(), this.chatColors.get(), i, this.mainHand.get(), this.minecraft.isTextFilteringEnabled(), this.allowServerListing.get()));
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
            return this.cloudStatus.get();
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

    public File getFile() {
        return this.optionsFile;
    }

    public String dumpOptionsForReport() {
        Stream<Pair<String, String>> stream = Stream.builder().add(Pair.of("ao", this.ambientOcclusion.get())).add(Pair.of("biomeBlendRadius", this.biomeBlendRadius.get())).add(Pair.of("enableVsync", this.enableVsync.get())).add(Pair.of("entityDistanceScaling", this.entityDistanceScaling.get())).add(Pair.of("entityShadows", this.entityShadows.get())).add(Pair.of("forceUnicodeFont", this.forceUnicodeFont.get())).add(Pair.of("fov", this.fov.get())).add(Pair.of("fovEffectScale", this.fovEffectScale.get())).add(Pair.of("prioritizeChunkUpdates", this.prioritizeChunkUpdates.get())).add(Pair.of("fullscreen", this.fullscreen.get())).add(Pair.of("fullscreenResolution", String.valueOf(this.fullscreenVideoModeString))).add(Pair.of("gamma", this.gamma.get())).add(Pair.of("glDebugVerbosity", this.glDebugVerbosity)).add(Pair.of("graphicsMode", this.graphicsMode.get())).add(Pair.of("guiScale", this.guiScale.get())).add(Pair.of("maxFps", this.framerateLimit.get())).add(Pair.of("mipmapLevels", this.mipmapLevels.get())).add(Pair.of("narrator", this.narrator.get())).add(Pair.of("overrideHeight", this.overrideHeight)).add(Pair.of("overrideWidth", this.overrideWidth)).add(Pair.of("particles", this.particles.get())).add(Pair.of("reducedDebugInfo", this.reducedDebugInfo.get())).add(Pair.of("renderClouds", this.cloudStatus.get())).add(Pair.of("renderDistance", this.renderDistance.get())).add(Pair.of("simulationDistance", this.simulationDistance.get())).add(Pair.of("resourcePacks", this.resourcePacks)).add(Pair.of("screenEffectScale", this.screenEffectScale.get())).add(Pair.of("syncChunkWrites", this.syncWrites)).add(Pair.of("useNativeTransport", this.useNativeTransport)).add(Pair.of("soundDevice", this.soundDevice.get())).build();
        return stream.map(pair -> (String)pair.getFirst() + ": " + pair.getSecond()).collect(Collectors.joining(System.lineSeparator()));
    }

    public void setServerRenderDistance(int i) {
        this.serverRenderDistance = i;
    }

    public int getEffectiveRenderDistance() {
        return this.serverRenderDistance > 0 ? Math.min(this.renderDistance.get(), this.serverRenderDistance) : this.renderDistance.get();
    }

    private static Component pixelValueLabel(Component component, int i) {
        return new TranslatableComponent("options.pixel_value", component, i);
    }

    private static Component percentValueLabel(Component component, double d) {
        return new TranslatableComponent("options.percent_value", component, (int)(d * 100.0));
    }

    public static Component genericValueLabel(Component component, Component component2) {
        return new TranslatableComponent("options.generic_value", component, component2);
    }

    public static Component genericValueLabel(Component component, int i) {
        return Options.genericValueLabel(component, new TextComponent(Integer.toString(i)));
    }

    @Environment(value=EnvType.CLIENT)
    static interface FieldAccess {
        public <T> void process(String var1, OptionInstance<T> var2);

        public int process(String var1, int var2);

        public boolean process(String var1, boolean var2);

        public String process(String var1, String var2);

        public float process(String var1, float var2);

        public <T> T process(String var1, T var2, Function<String, T> var3, Function<T, String> var4);
    }
}

