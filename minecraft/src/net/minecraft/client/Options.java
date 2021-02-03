package net.minecraft.client;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.VideoMode;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
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

@Environment(EnvType.CLIENT)
public class Options {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = new Gson();
	private static final TypeToken<List<String>> RESOURCE_PACK_TYPE = new TypeToken<List<String>>() {
	};
	private static final Splitter OPTION_SPLITTER = Splitter.on(':').limit(2);
	public double sensitivity = 0.5;
	public int renderDistance;
	public float entityDistanceScaling = 1.0F;
	public int framerateLimit = 120;
	public CloudStatus renderClouds = CloudStatus.FANCY;
	public GraphicsStatus graphicsMode = GraphicsStatus.FANCY;
	public AmbientOcclusionStatus ambientOcclusion = AmbientOcclusionStatus.MAX;
	public List<String> resourcePacks = Lists.<String>newArrayList();
	public List<String> incompatibleResourcePacks = Lists.<String>newArrayList();
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
	public double chatHeightUnfocused = 0.44366196F;
	public double chatHeightFocused = 1.0;
	public double chatDelay;
	public int mipmapLevels = 4;
	private final Object2FloatMap<SoundSource> sourceVolumes = Util.make(
		new Object2FloatOpenHashMap<>(), object2FloatOpenHashMap -> object2FloatOpenHashMap.defaultReturnValue(1.0F)
	);
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
	public boolean reducedDebugInfo;
	public boolean snooperEnabled = true;
	public boolean showSubtitles;
	public boolean backgroundForChatOnly = true;
	public boolean touchscreen;
	public boolean fullscreen;
	public boolean bobView = true;
	public boolean toggleCrouch;
	public boolean toggleSprint;
	public boolean skipMultiplayerWarning;
	public boolean hideMatchedNames = true;
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
	public final KeyMapping[] keyHotbarSlots = new KeyMapping[]{
		new KeyMapping("key.hotbar.1", 49, "key.categories.inventory"),
		new KeyMapping("key.hotbar.2", 50, "key.categories.inventory"),
		new KeyMapping("key.hotbar.3", 51, "key.categories.inventory"),
		new KeyMapping("key.hotbar.4", 52, "key.categories.inventory"),
		new KeyMapping("key.hotbar.5", 53, "key.categories.inventory"),
		new KeyMapping("key.hotbar.6", 54, "key.categories.inventory"),
		new KeyMapping("key.hotbar.7", 55, "key.categories.inventory"),
		new KeyMapping("key.hotbar.8", 56, "key.categories.inventory"),
		new KeyMapping("key.hotbar.9", 57, "key.categories.inventory")
	};
	public final KeyMapping keySaveHotbarActivator = new KeyMapping("key.saveToolbarActivator", 67, "key.categories.creative");
	public final KeyMapping keyLoadHotbarActivator = new KeyMapping("key.loadToolbarActivator", 88, "key.categories.creative");
	public final KeyMapping[] keyMappings = ArrayUtils.addAll(
		new KeyMapping[]{
			this.keyAttack,
			this.keyUse,
			this.keyUp,
			this.keyLeft,
			this.keyDown,
			this.keyRight,
			this.keyJump,
			this.keyShift,
			this.keySprint,
			this.keyDrop,
			this.keyInventory,
			this.keyChat,
			this.keyPlayerList,
			this.keyPickItem,
			this.keyCommand,
			this.keySocialInteractions,
			this.keyScreenshot,
			this.keyTogglePerspective,
			this.keySmoothCamera,
			this.keyFullscreen,
			this.keySpectatorOutlines,
			this.keySwapOffhand,
			this.keySaveHotbarActivator,
			this.keyLoadHotbarActivator,
			this.keyAdvancements
		},
		this.keyHotbarSlots
	);
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
	public float screenEffectScale = 1.0F;
	public float fovEffectScale = 1.0F;
	public double gamma;
	public int guiScale;
	public ParticleStatus particles = ParticleStatus.ALL;
	public NarratorStatus narratorStatus = NarratorStatus.OFF;
	public String languageCode = "en_us";
	public boolean syncWrites;

	public Options(Minecraft minecraft, File file) {
		this.minecraft = minecraft;
		this.optionsFile = new File(file, "options.txt");
		if (minecraft.is64Bit() && Runtime.getRuntime().maxMemory() >= 1000000000L) {
			Option.RENDER_DISTANCE.setMaxValue(32.0F);
		} else {
			Option.RENDER_DISTANCE.setMaxValue(16.0F);
		}

		this.renderDistance = minecraft.is64Bit() ? 12 : 8;
		this.syncWrites = Util.getPlatform() == Util.OS.WINDOWS;
		this.load();
	}

	public float getBackgroundOpacity(float f) {
		return this.backgroundForChatOnly ? f : (float)this.textBackgroundOpacity;
	}

	public int getBackgroundColor(float f) {
		return (int)(this.getBackgroundOpacity(f) * 255.0F) << 24 & 0xFF000000;
	}

	public int getBackgroundColor(int i) {
		return this.backgroundForChatOnly ? i : (int)(this.textBackgroundOpacity * 255.0) << 24 & 0xFF000000;
	}

	public void setKey(KeyMapping keyMapping, InputConstants.Key key) {
		keyMapping.setKey(key);
		this.save();
	}

	private void processOptions(Options.FieldAccess fieldAccess) {
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
		this.snooperEnabled = fieldAccess.process("snooperEnabled", this.snooperEnabled);
		this.showSubtitles = fieldAccess.process("showSubtitles", this.showSubtitles);
		this.touchscreen = fieldAccess.process("touchscreen", this.touchscreen);
		this.fullscreen = fieldAccess.process("fullscreen", this.fullscreen);
		this.bobView = fieldAccess.process("bobView", this.bobView);
		this.toggleCrouch = fieldAccess.process("toggleCrouch", this.toggleCrouch);
		this.toggleSprint = fieldAccess.process("toggleSprint", this.toggleSprint);
		this.sensitivity = fieldAccess.process("mouseSensitivity", this.sensitivity);
		this.fov = fieldAccess.process("fov", (this.fov - 70.0) / 40.0) * 40.0 + 70.0;
		this.screenEffectScale = fieldAccess.process("screenEffectScale", this.screenEffectScale);
		this.fovEffectScale = fieldAccess.process("fovEffectScale", this.fovEffectScale);
		this.gamma = fieldAccess.process("gamma", this.gamma);
		this.renderDistance = fieldAccess.process("renderDistance", this.renderDistance);
		this.entityDistanceScaling = fieldAccess.process("entityDistanceScaling", this.entityDistanceScaling);
		this.guiScale = fieldAccess.process("guiScale", this.guiScale);
		this.particles = fieldAccess.process("particles", this.particles, ParticleStatus::byId, ParticleStatus::getId);
		this.framerateLimit = fieldAccess.process("maxFps", this.framerateLimit);
		this.difficulty = fieldAccess.process("difficulty", this.difficulty, Difficulty::byId, Difficulty::getId);
		this.graphicsMode = fieldAccess.process("graphicsMode", this.graphicsMode, GraphicsStatus::byId, GraphicsStatus::getId);
		this.ambientOcclusion = fieldAccess.process(
			"ao", this.ambientOcclusion, Options::readAmbientOcclusion, ambientOcclusionStatus -> Integer.toString(ambientOcclusionStatus.getId())
		);
		this.biomeBlendRadius = fieldAccess.process("biomeBlendRadius", this.biomeBlendRadius);
		this.renderClouds = fieldAccess.process("renderClouds", this.renderClouds, Options::readCloudStatus, Options::writeCloudStatus);
		this.resourcePacks = fieldAccess.process("resourcePacks", this.resourcePacks, Options::readPackList, GSON::toJson);
		this.incompatibleResourcePacks = fieldAccess.process("incompatibleResourcePacks", this.incompatibleResourcePacks, Options::readPackList, GSON::toJson);
		this.lastMpIp = fieldAccess.process("lastServer", this.lastMpIp);
		this.languageCode = fieldAccess.process("lang", this.languageCode);
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

		for (KeyMapping keyMapping : this.keyMappings) {
			String string = keyMapping.saveString();
			String string2 = fieldAccess.process("key_" + keyMapping.getName(), string);
			if (!string.equals(string2)) {
				keyMapping.setKey(InputConstants.getKey(string2));
			}
		}

		for (SoundSource soundSource : SoundSource.values()) {
			this.sourceVolumes
				.computeFloat(soundSource, (soundSourcex, float_) -> fieldAccess.process("soundCategory_" + soundSourcex.getName(), float_ != null ? float_ : 1.0F));
		}

		for (PlayerModelPart playerModelPart : PlayerModelPart.values()) {
			boolean bl = this.modelParts.contains(playerModelPart);
			boolean bl2 = fieldAccess.process("modelPart_" + playerModelPart.getId(), bl);
			if (bl2 != bl) {
				this.setModelPart(playerModelPart, bl2);
			}
		}
	}

	public void load() {
		try {
			if (!this.optionsFile.exists()) {
				return;
			}

			this.sourceVolumes.clear();
			CompoundTag compoundTag = new CompoundTag();
			BufferedReader bufferedReader = Files.newReader(this.optionsFile, Charsets.UTF_8);
			Throwable var3 = null;

			try {
				bufferedReader.lines().forEach(string -> {
					try {
						Iterator<String> iterator = OPTION_SPLITTER.split(string).iterator();
						compoundTag.putString((String)iterator.next(), (String)iterator.next());
					} catch (Exception var3x) {
						LOGGER.warn("Skipping bad option: {}", string);
					}
				});
			} catch (Throwable var13) {
				var3 = var13;
				throw var13;
			} finally {
				if (bufferedReader != null) {
					if (var3 != null) {
						try {
							bufferedReader.close();
						} catch (Throwable var12) {
							var3.addSuppressed(var12);
						}
					} else {
						bufferedReader.close();
					}
				}
			}

			final CompoundTag compoundTag2 = this.dataFix(compoundTag);
			if (!compoundTag2.contains("graphicsMode") && compoundTag2.contains("fancyGraphics")) {
				if (isTrue(compoundTag2.getString("fancyGraphics"))) {
					this.graphicsMode = GraphicsStatus.FANCY;
				} else {
					this.graphicsMode = GraphicsStatus.FAST;
				}
			}

			this.processOptions(new Options.FieldAccess() {
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
						} catch (NumberFormatException var5) {
							Options.LOGGER.warn("Invalid integer value for option {} = {}", string, string2, var5);
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
					if (string2 == null) {
						return d;
					} else if (Options.isTrue(string2)) {
						return 1.0;
					} else if (Options.isFalse(string2)) {
						return 0.0;
					} else {
						try {
							return Double.parseDouble(string2);
						} catch (NumberFormatException var6) {
							Options.LOGGER.warn("Invalid floating point value for option {} = {}", string, string2, var6);
							return d;
						}
					}
				}

				@Override
				public float process(String string, float f) {
					String string2 = this.getValueOrNull(string);
					if (string2 == null) {
						return f;
					} else if (Options.isTrue(string2)) {
						return 1.0F;
					} else if (Options.isFalse(string2)) {
						return 0.0F;
					} else {
						try {
							return Float.parseFloat(string2);
						} catch (NumberFormatException var5) {
							Options.LOGGER.warn("Invalid floating point value for option {} = {}", string, string2, var5);
							return f;
						}
					}
				}

				@Override
				public <T> T process(String string, T object, Function<String, T> function, Function<T, String> function2) {
					String string2 = this.getValueOrNull(string);
					return (T)(string2 == null ? object : function.apply(string2));
				}

				@Override
				public <T> T process(String string, T object, IntFunction<T> intFunction, ToIntFunction<T> toIntFunction) {
					String string2 = this.getValueOrNull(string);
					if (string2 != null) {
						try {
							return (T)intFunction.apply(Integer.parseInt(string2));
						} catch (Exception var7) {
							Options.LOGGER.warn("Invalid integer value for option {} = {}", string, string2, var7);
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
		} catch (Exception var15) {
			LOGGER.error("Failed to load options", (Throwable)var15);
		}
	}

	private static boolean isTrue(String string) {
		return "true".equals(string);
	}

	private static boolean isFalse(String string) {
		return "false".equals(string);
	}

	private CompoundTag dataFix(CompoundTag compoundTag) {
		int i = 0;

		try {
			i = Integer.parseInt(compoundTag.getString("version"));
		} catch (RuntimeException var4) {
		}

		return NbtUtils.update(this.minecraft.getFixerUpper(), DataFixTypes.OPTIONS, compoundTag, i);
	}

	public void save() {
		try {
			final PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.optionsFile), StandardCharsets.UTF_8));
			Throwable var2 = null;

			try {
				printWriter.println("version:" + SharedConstants.getCurrentVersion().getWorldVersion());
				this.processOptions(new Options.FieldAccess() {
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
						printWriter.println((String)function2.apply(object));
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
					printWriter.println("fullscreenResolution:" + ((VideoMode)this.minecraft.getWindow().getPreferredFullscreenVideoMode().get()).write());
				}
			} catch (Throwable var12) {
				var2 = var12;
				throw var12;
			} finally {
				if (printWriter != null) {
					if (var2 != null) {
						try {
							printWriter.close();
						} catch (Throwable var11) {
							var2.addSuppressed(var11);
						}
					} else {
						printWriter.close();
					}
				}
			}
		} catch (Exception var14) {
			LOGGER.error("Failed to save options", (Throwable)var14);
		}

		this.broadcastOptions();
	}

	public float getSoundSourceVolume(SoundSource soundSource) {
		return this.sourceVolumes.getFloat(soundSource);
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

			this.minecraft
				.player
				.connection
				.send(new ServerboundClientInformationPacket(this.languageCode, this.renderDistance, this.chatVisibility, this.chatColors, i, this.mainHand));
		}
	}

	private void setModelPart(PlayerModelPart playerModelPart, boolean bl) {
		if (bl) {
			this.modelParts.add(playerModelPart);
		} else {
			this.modelParts.remove(playerModelPart);
		}
	}

	public boolean isModelPartEnabled(PlayerModelPart playerModelPart) {
		return this.modelParts.contains(playerModelPart);
	}

	public void toggleModelPart(PlayerModelPart playerModelPart, boolean bl) {
		this.setModelPart(playerModelPart, bl);
		this.broadcastOptions();
	}

	public CloudStatus getCloudsType() {
		return this.renderDistance >= 4 ? this.renderClouds : CloudStatus.OFF;
	}

	public boolean useNativeTransport() {
		return this.useNativeTransport;
	}

	public void loadSelectedResourcePacks(PackRepository packRepository) {
		Set<String> set = Sets.<String>newLinkedHashSet();
		Iterator<String> iterator = this.resourcePacks.iterator();

		while (iterator.hasNext()) {
			String string = (String)iterator.next();
			Pack pack = packRepository.getPack(string);
			if (pack == null && !string.startsWith("file/")) {
				pack = packRepository.getPack("file/" + string);
			}

			if (pack == null) {
				LOGGER.warn("Removed resource pack {} from options because it doesn't seem to exist anymore", string);
				iterator.remove();
			} else if (!pack.getCompatibility().isCompatible() && !this.incompatibleResourcePacks.contains(string)) {
				LOGGER.warn("Removed resource pack {} from options because it is no longer compatible", string);
				iterator.remove();
			} else if (pack.getCompatibility().isCompatible() && this.incompatibleResourcePacks.contains(string)) {
				LOGGER.info("Removed resource pack {} from incompatibility list because it's now compatible", string);
				this.incompatibleResourcePacks.remove(string);
			} else {
				set.add(pack.getId());
			}
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
		return (List<String>)(list != null ? list : Lists.<String>newArrayList());
	}

	private static CloudStatus readCloudStatus(String string) {
		switch (string) {
			case "true":
				return CloudStatus.FANCY;
			case "fast":
				return CloudStatus.FAST;
			case "false":
			default:
				return CloudStatus.OFF;
		}
	}

	private static String writeCloudStatus(CloudStatus cloudStatus) {
		switch (cloudStatus) {
			case FANCY:
				return "true";
			case FAST:
				return "fast";
			case OFF:
			default:
				return "false";
		}
	}

	private static AmbientOcclusionStatus readAmbientOcclusion(String string) {
		if (isTrue(string)) {
			return AmbientOcclusionStatus.MAX;
		} else {
			return isFalse(string) ? AmbientOcclusionStatus.OFF : AmbientOcclusionStatus.byId(Integer.parseInt(string));
		}
	}

	private static HumanoidArm readMainHand(String string) {
		return "left".equals(string) ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
	}

	private static String writeMainHand(HumanoidArm humanoidArm) {
		return humanoidArm == HumanoidArm.LEFT ? "left" : "right";
	}

	@Environment(EnvType.CLIENT)
	interface FieldAccess {
		int process(String string, int i);

		boolean process(String string, boolean bl);

		String process(String string, String string2);

		double process(String string, double d);

		float process(String string, float f);

		<T> T process(String string, T object, Function<String, T> function, Function<T, String> function2);

		<T> T process(String string, T object, IntFunction<T> intFunction, ToIntFunction<T> toIntFunction);
	}
}
