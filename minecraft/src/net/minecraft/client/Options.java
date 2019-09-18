package net.minecraft.client;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.VideoMode;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.resources.UnopenedResourcePack;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class Options {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = new Gson();
	private static final Type RESOURCE_PACK_TYPE = new ParameterizedType() {
		public Type[] getActualTypeArguments() {
			return new Type[]{String.class};
		}

		public Type getRawType() {
			return List.class;
		}

		public Type getOwnerType() {
			return null;
		}
	};
	public static final Splitter COLON_SPLITTER = Splitter.on(':');
	public double sensitivity = 0.5;
	public int renderDistance = -1;
	public int framerateLimit = 120;
	public CloudStatus renderClouds = CloudStatus.FANCY;
	public boolean fancyGraphics = true;
	public AmbientOcclusionStatus ambientOcclusion = AmbientOcclusionStatus.MAX;
	public List<String> resourcePacks = Lists.<String>newArrayList();
	public List<String> incompatibleResourcePacks = Lists.<String>newArrayList();
	public ChatVisiblity chatVisibility = ChatVisiblity.FULL;
	public double chatOpacity = 1.0;
	public double textBackgroundOpacity = 0.5;
	@Nullable
	public String fullscreenVideoModeString;
	public boolean hideServerAddress;
	public boolean advancedItemTooltips;
	public boolean pauseOnLostFocus = true;
	private final Set<PlayerModelPart> modelParts = Sets.<PlayerModelPart>newHashSet(PlayerModelPart.values());
	public HumanoidArm mainHand = HumanoidArm.RIGHT;
	public int overrideWidth;
	public int overrideHeight;
	public boolean heldItemTooltips = true;
	public double chatScale = 1.0;
	public double chatWidth = 1.0;
	public double chatHeightUnfocused = 0.44366196F;
	public double chatHeightFocused = 1.0;
	public int mipmapLevels = 4;
	private final Map<SoundSource, Float> sourceVolumes = Maps.newEnumMap(SoundSource.class);
	public boolean useNativeTransport = true;
	public AttackIndicatorStatus attackIndicator = AttackIndicatorStatus.CROSSHAIR;
	public TutorialSteps tutorialStep = TutorialSteps.MOVEMENT;
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
	public final KeyMapping keyUp = new KeyMapping("key.forward", 87, "key.categories.movement");
	public final KeyMapping keyLeft = new KeyMapping("key.left", 65, "key.categories.movement");
	public final KeyMapping keyDown = new KeyMapping("key.back", 83, "key.categories.movement");
	public final KeyMapping keyRight = new KeyMapping("key.right", 68, "key.categories.movement");
	public final KeyMapping keyJump = new KeyMapping("key.jump", 32, "key.categories.movement");
	public final KeyMapping keyShift = new KeyMapping("key.sneak", 340, "key.categories.movement");
	public final KeyMapping keySprint = new KeyMapping("key.sprint", 341, "key.categories.movement");
	public final KeyMapping keyInventory = new KeyMapping("key.inventory", 69, "key.categories.inventory");
	public final KeyMapping keySwapHands = new KeyMapping("key.swapHands", 70, "key.categories.inventory");
	public final KeyMapping keyDrop = new KeyMapping("key.drop", 81, "key.categories.inventory");
	public final KeyMapping keyUse = new KeyMapping("key.use", InputConstants.Type.MOUSE, 1, "key.categories.gameplay");
	public final KeyMapping keyAttack = new KeyMapping("key.attack", InputConstants.Type.MOUSE, 0, "key.categories.gameplay");
	public final KeyMapping keyPickItem = new KeyMapping("key.pickItem", InputConstants.Type.MOUSE, 2, "key.categories.gameplay");
	public final KeyMapping keyChat = new KeyMapping("key.chat", 84, "key.categories.multiplayer");
	public final KeyMapping keyPlayerList = new KeyMapping("key.playerlist", 258, "key.categories.multiplayer");
	public final KeyMapping keyCommand = new KeyMapping("key.command", 47, "key.categories.multiplayer");
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
			this.keyScreenshot,
			this.keyTogglePerspective,
			this.keySmoothCamera,
			this.keyFullscreen,
			this.keySpectatorOutlines,
			this.keySwapHands,
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
	public int thirdPersonView;
	public boolean renderDebug;
	public boolean renderDebugCharts;
	public boolean renderFpsChart;
	public String lastMpIp = "";
	public boolean smoothCamera;
	public double fov = 70.0;
	public double gamma;
	public int guiScale;
	public ParticleStatus particles = ParticleStatus.ALL;
	public NarratorStatus narratorStatus = NarratorStatus.OFF;
	public String languageCode = "en_us";

	public Options(Minecraft minecraft, File file) {
		this.minecraft = minecraft;
		this.optionsFile = new File(file, "options.txt");
		if (minecraft.is64Bit() && Runtime.getRuntime().maxMemory() >= 1000000000L) {
			Option.RENDER_DISTANCE.setMaxValue(32.0F);
		} else {
			Option.RENDER_DISTANCE.setMaxValue(16.0F);
		}

		this.renderDistance = minecraft.is64Bit() ? 12 : 8;
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

	public void load() {
		try {
			if (!this.optionsFile.exists()) {
				return;
			}

			this.sourceVolumes.clear();
			List<String> list = IOUtils.readLines(new FileInputStream(this.optionsFile));
			CompoundTag compoundTag = new CompoundTag();

			for (String string : list) {
				try {
					Iterator<String> iterator = COLON_SPLITTER.omitEmptyStrings().limit(2).split(string).iterator();
					compoundTag.putString((String)iterator.next(), (String)iterator.next());
				} catch (Exception var10) {
					LOGGER.warn("Skipping bad option: {}", string);
				}
			}

			compoundTag = this.dataFix(compoundTag);

			for (String string : compoundTag.getAllKeys()) {
				String string2 = compoundTag.getString(string);

				try {
					if ("autoJump".equals(string)) {
						Option.AUTO_JUMP.set(this, string2);
					}

					if ("autoSuggestions".equals(string)) {
						Option.AUTO_SUGGESTIONS.set(this, string2);
					}

					if ("chatColors".equals(string)) {
						Option.CHAT_COLOR.set(this, string2);
					}

					if ("chatLinks".equals(string)) {
						Option.CHAT_LINKS.set(this, string2);
					}

					if ("chatLinksPrompt".equals(string)) {
						Option.CHAT_LINKS_PROMPT.set(this, string2);
					}

					if ("enableVsync".equals(string)) {
						Option.ENABLE_VSYNC.set(this, string2);
					}

					if ("entityShadows".equals(string)) {
						Option.ENTITY_SHADOWS.set(this, string2);
					}

					if ("forceUnicodeFont".equals(string)) {
						Option.FORCE_UNICODE_FONT.set(this, string2);
					}

					if ("discrete_mouse_scroll".equals(string)) {
						Option.DISCRETE_MOUSE_SCROLL.set(this, string2);
					}

					if ("invertYMouse".equals(string)) {
						Option.INVERT_MOUSE.set(this, string2);
					}

					if ("realmsNotifications".equals(string)) {
						Option.REALMS_NOTIFICATIONS.set(this, string2);
					}

					if ("reducedDebugInfo".equals(string)) {
						Option.REDUCED_DEBUG_INFO.set(this, string2);
					}

					if ("showSubtitles".equals(string)) {
						Option.SHOW_SUBTITLES.set(this, string2);
					}

					if ("snooperEnabled".equals(string)) {
						Option.SNOOPER_ENABLED.set(this, string2);
					}

					if ("touchscreen".equals(string)) {
						Option.TOUCHSCREEN.set(this, string2);
					}

					if ("fullscreen".equals(string)) {
						Option.USE_FULLSCREEN.set(this, string2);
					}

					if ("bobView".equals(string)) {
						Option.VIEW_BOBBING.set(this, string2);
					}

					if ("mouseSensitivity".equals(string)) {
						this.sensitivity = (double)readFloat(string2);
					}

					if ("fov".equals(string)) {
						this.fov = (double)(readFloat(string2) * 40.0F + 70.0F);
					}

					if ("gamma".equals(string)) {
						this.gamma = (double)readFloat(string2);
					}

					if ("renderDistance".equals(string)) {
						this.renderDistance = Integer.parseInt(string2);
					}

					if ("guiScale".equals(string)) {
						this.guiScale = Integer.parseInt(string2);
					}

					if ("particles".equals(string)) {
						this.particles = ParticleStatus.byId(Integer.parseInt(string2));
					}

					if ("maxFps".equals(string)) {
						this.framerateLimit = Integer.parseInt(string2);
						if (this.minecraft.getWindow() != null) {
							this.minecraft.getWindow().setFramerateLimit(this.framerateLimit);
						}
					}

					if ("difficulty".equals(string)) {
						this.difficulty = Difficulty.byId(Integer.parseInt(string2));
					}

					if ("fancyGraphics".equals(string)) {
						this.fancyGraphics = "true".equals(string2);
					}

					if ("tutorialStep".equals(string)) {
						this.tutorialStep = TutorialSteps.getByName(string2);
					}

					if ("ao".equals(string)) {
						if ("true".equals(string2)) {
							this.ambientOcclusion = AmbientOcclusionStatus.MAX;
						} else if ("false".equals(string2)) {
							this.ambientOcclusion = AmbientOcclusionStatus.OFF;
						} else {
							this.ambientOcclusion = AmbientOcclusionStatus.byId(Integer.parseInt(string2));
						}
					}

					if ("renderClouds".equals(string)) {
						if ("true".equals(string2)) {
							this.renderClouds = CloudStatus.FANCY;
						} else if ("false".equals(string2)) {
							this.renderClouds = CloudStatus.OFF;
						} else if ("fast".equals(string2)) {
							this.renderClouds = CloudStatus.FAST;
						}
					}

					if ("attackIndicator".equals(string)) {
						this.attackIndicator = AttackIndicatorStatus.byId(Integer.parseInt(string2));
					}

					if ("resourcePacks".equals(string)) {
						this.resourcePacks = GsonHelper.fromJson(GSON, string2, RESOURCE_PACK_TYPE);
						if (this.resourcePacks == null) {
							this.resourcePacks = Lists.<String>newArrayList();
						}
					}

					if ("incompatibleResourcePacks".equals(string)) {
						this.incompatibleResourcePacks = GsonHelper.fromJson(GSON, string2, RESOURCE_PACK_TYPE);
						if (this.incompatibleResourcePacks == null) {
							this.incompatibleResourcePacks = Lists.<String>newArrayList();
						}
					}

					if ("lastServer".equals(string)) {
						this.lastMpIp = string2;
					}

					if ("lang".equals(string)) {
						this.languageCode = string2;
					}

					if ("chatVisibility".equals(string)) {
						this.chatVisibility = ChatVisiblity.byId(Integer.parseInt(string2));
					}

					if ("chatOpacity".equals(string)) {
						this.chatOpacity = (double)readFloat(string2);
					}

					if ("textBackgroundOpacity".equals(string)) {
						this.textBackgroundOpacity = (double)readFloat(string2);
					}

					if ("backgroundForChatOnly".equals(string)) {
						this.backgroundForChatOnly = "true".equals(string2);
					}

					if ("fullscreenResolution".equals(string)) {
						this.fullscreenVideoModeString = string2;
					}

					if ("hideServerAddress".equals(string)) {
						this.hideServerAddress = "true".equals(string2);
					}

					if ("advancedItemTooltips".equals(string)) {
						this.advancedItemTooltips = "true".equals(string2);
					}

					if ("pauseOnLostFocus".equals(string)) {
						this.pauseOnLostFocus = "true".equals(string2);
					}

					if ("overrideHeight".equals(string)) {
						this.overrideHeight = Integer.parseInt(string2);
					}

					if ("overrideWidth".equals(string)) {
						this.overrideWidth = Integer.parseInt(string2);
					}

					if ("heldItemTooltips".equals(string)) {
						this.heldItemTooltips = "true".equals(string2);
					}

					if ("chatHeightFocused".equals(string)) {
						this.chatHeightFocused = (double)readFloat(string2);
					}

					if ("chatHeightUnfocused".equals(string)) {
						this.chatHeightUnfocused = (double)readFloat(string2);
					}

					if ("chatScale".equals(string)) {
						this.chatScale = (double)readFloat(string2);
					}

					if ("chatWidth".equals(string)) {
						this.chatWidth = (double)readFloat(string2);
					}

					if ("mipmapLevels".equals(string)) {
						this.mipmapLevels = Integer.parseInt(string2);
					}

					if ("useNativeTransport".equals(string)) {
						this.useNativeTransport = "true".equals(string2);
					}

					if ("mainHand".equals(string)) {
						this.mainHand = "left".equals(string2) ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
					}

					if ("narrator".equals(string)) {
						this.narratorStatus = NarratorStatus.byId(Integer.parseInt(string2));
					}

					if ("biomeBlendRadius".equals(string)) {
						this.biomeBlendRadius = Integer.parseInt(string2);
					}

					if ("mouseWheelSensitivity".equals(string)) {
						this.mouseWheelSensitivity = (double)readFloat(string2);
					}

					if ("rawMouseInput".equals(string)) {
						this.rawMouseInput = "true".equals(string2);
					}

					if ("glDebugVerbosity".equals(string)) {
						this.glDebugVerbosity = Integer.parseInt(string2);
					}

					for (KeyMapping keyMapping : this.keyMappings) {
						if (string.equals("key_" + keyMapping.getName())) {
							keyMapping.setKey(InputConstants.getKey(string2));
						}
					}

					for (SoundSource soundSource : SoundSource.values()) {
						if (string.equals("soundCategory_" + soundSource.getName())) {
							this.sourceVolumes.put(soundSource, readFloat(string2));
						}
					}

					for (PlayerModelPart playerModelPart : PlayerModelPart.values()) {
						if (string.equals("modelPart_" + playerModelPart.getId())) {
							this.setModelPart(playerModelPart, "true".equals(string2));
						}
					}
				} catch (Exception var11) {
					LOGGER.warn("Skipping bad option: {}:{}", string, string2);
				}
			}

			KeyMapping.resetMapping();
		} catch (Exception var12) {
			LOGGER.error("Failed to load options", (Throwable)var12);
		}
	}

	private CompoundTag dataFix(CompoundTag compoundTag) {
		int i = 0;

		try {
			i = Integer.parseInt(compoundTag.getString("version"));
		} catch (RuntimeException var4) {
		}

		return NbtUtils.update(this.minecraft.getFixerUpper(), DataFixTypes.OPTIONS, compoundTag, i);
	}

	private static float readFloat(String string) {
		if ("true".equals(string)) {
			return 1.0F;
		} else {
			return "false".equals(string) ? 0.0F : Float.parseFloat(string);
		}
	}

	public void save() {
		try {
			PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.optionsFile), StandardCharsets.UTF_8));
			Throwable var2 = null;

			try {
				printWriter.println("version:" + SharedConstants.getCurrentVersion().getWorldVersion());
				printWriter.println("autoJump:" + Option.AUTO_JUMP.get(this));
				printWriter.println("autoSuggestions:" + Option.AUTO_SUGGESTIONS.get(this));
				printWriter.println("chatColors:" + Option.CHAT_COLOR.get(this));
				printWriter.println("chatLinks:" + Option.CHAT_LINKS.get(this));
				printWriter.println("chatLinksPrompt:" + Option.CHAT_LINKS_PROMPT.get(this));
				printWriter.println("enableVsync:" + Option.ENABLE_VSYNC.get(this));
				printWriter.println("entityShadows:" + Option.ENTITY_SHADOWS.get(this));
				printWriter.println("forceUnicodeFont:" + Option.FORCE_UNICODE_FONT.get(this));
				printWriter.println("discrete_mouse_scroll:" + Option.DISCRETE_MOUSE_SCROLL.get(this));
				printWriter.println("invertYMouse:" + Option.INVERT_MOUSE.get(this));
				printWriter.println("realmsNotifications:" + Option.REALMS_NOTIFICATIONS.get(this));
				printWriter.println("reducedDebugInfo:" + Option.REDUCED_DEBUG_INFO.get(this));
				printWriter.println("snooperEnabled:" + Option.SNOOPER_ENABLED.get(this));
				printWriter.println("showSubtitles:" + Option.SHOW_SUBTITLES.get(this));
				printWriter.println("touchscreen:" + Option.TOUCHSCREEN.get(this));
				printWriter.println("fullscreen:" + Option.USE_FULLSCREEN.get(this));
				printWriter.println("bobView:" + Option.VIEW_BOBBING.get(this));
				printWriter.println("mouseSensitivity:" + this.sensitivity);
				printWriter.println("fov:" + (this.fov - 70.0) / 40.0);
				printWriter.println("gamma:" + this.gamma);
				printWriter.println("renderDistance:" + this.renderDistance);
				printWriter.println("guiScale:" + this.guiScale);
				printWriter.println("particles:" + this.particles.getId());
				printWriter.println("maxFps:" + this.framerateLimit);
				printWriter.println("difficulty:" + this.difficulty.getId());
				printWriter.println("fancyGraphics:" + this.fancyGraphics);
				printWriter.println("ao:" + this.ambientOcclusion.getId());
				printWriter.println("biomeBlendRadius:" + this.biomeBlendRadius);
				switch (this.renderClouds) {
					case FANCY:
						printWriter.println("renderClouds:true");
						break;
					case FAST:
						printWriter.println("renderClouds:fast");
						break;
					case OFF:
						printWriter.println("renderClouds:false");
				}

				printWriter.println("resourcePacks:" + GSON.toJson(this.resourcePacks));
				printWriter.println("incompatibleResourcePacks:" + GSON.toJson(this.incompatibleResourcePacks));
				printWriter.println("lastServer:" + this.lastMpIp);
				printWriter.println("lang:" + this.languageCode);
				printWriter.println("chatVisibility:" + this.chatVisibility.getId());
				printWriter.println("chatOpacity:" + this.chatOpacity);
				printWriter.println("textBackgroundOpacity:" + this.textBackgroundOpacity);
				printWriter.println("backgroundForChatOnly:" + this.backgroundForChatOnly);
				if (this.minecraft.getWindow().getPreferredFullscreenVideoMode().isPresent()) {
					printWriter.println("fullscreenResolution:" + ((VideoMode)this.minecraft.getWindow().getPreferredFullscreenVideoMode().get()).write());
				}

				printWriter.println("hideServerAddress:" + this.hideServerAddress);
				printWriter.println("advancedItemTooltips:" + this.advancedItemTooltips);
				printWriter.println("pauseOnLostFocus:" + this.pauseOnLostFocus);
				printWriter.println("overrideWidth:" + this.overrideWidth);
				printWriter.println("overrideHeight:" + this.overrideHeight);
				printWriter.println("heldItemTooltips:" + this.heldItemTooltips);
				printWriter.println("chatHeightFocused:" + this.chatHeightFocused);
				printWriter.println("chatHeightUnfocused:" + this.chatHeightUnfocused);
				printWriter.println("chatScale:" + this.chatScale);
				printWriter.println("chatWidth:" + this.chatWidth);
				printWriter.println("mipmapLevels:" + this.mipmapLevels);
				printWriter.println("useNativeTransport:" + this.useNativeTransport);
				printWriter.println("mainHand:" + (this.mainHand == HumanoidArm.LEFT ? "left" : "right"));
				printWriter.println("attackIndicator:" + this.attackIndicator.getId());
				printWriter.println("narrator:" + this.narratorStatus.getId());
				printWriter.println("tutorialStep:" + this.tutorialStep.getName());
				printWriter.println("mouseWheelSensitivity:" + this.mouseWheelSensitivity);
				printWriter.println("rawMouseInput:" + Option.RAW_MOUSE_INPUT.get(this));
				printWriter.println("glDebugVerbosity:" + this.glDebugVerbosity);

				for (KeyMapping keyMapping : this.keyMappings) {
					printWriter.println("key_" + keyMapping.getName() + ":" + keyMapping.saveString());
				}

				for (SoundSource soundSource : SoundSource.values()) {
					printWriter.println("soundCategory_" + soundSource.getName() + ":" + this.getSoundSourceVolume(soundSource));
				}

				for (PlayerModelPart playerModelPart : PlayerModelPart.values()) {
					printWriter.println("modelPart_" + playerModelPart.getId() + ":" + this.modelParts.contains(playerModelPart));
				}
			} catch (Throwable var15) {
				var2 = var15;
				throw var15;
			} finally {
				if (printWriter != null) {
					if (var2 != null) {
						try {
							printWriter.close();
						} catch (Throwable var14) {
							var2.addSuppressed(var14);
						}
					} else {
						printWriter.close();
					}
				}
			}
		} catch (Exception var17) {
			LOGGER.error("Failed to save options", (Throwable)var17);
		}

		this.broadcastOptions();
	}

	public float getSoundSourceVolume(SoundSource soundSource) {
		return this.sourceVolumes.containsKey(soundSource) ? (Float)this.sourceVolumes.get(soundSource) : 1.0F;
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

	public Set<PlayerModelPart> getModelParts() {
		return ImmutableSet.copyOf(this.modelParts);
	}

	public void setModelPart(PlayerModelPart playerModelPart, boolean bl) {
		if (bl) {
			this.modelParts.add(playerModelPart);
		} else {
			this.modelParts.remove(playerModelPart);
		}

		this.broadcastOptions();
	}

	public void toggleModelPart(PlayerModelPart playerModelPart) {
		if (this.getModelParts().contains(playerModelPart)) {
			this.modelParts.remove(playerModelPart);
		} else {
			this.modelParts.add(playerModelPart);
		}

		this.broadcastOptions();
	}

	public CloudStatus getCloudsType() {
		return this.renderDistance >= 4 ? this.renderClouds : CloudStatus.OFF;
	}

	public boolean useNativeTransport() {
		return this.useNativeTransport;
	}

	public void loadResourcePacks(PackRepository<UnopenedResourcePack> packRepository) {
		packRepository.reload();
		Set<UnopenedResourcePack> set = Sets.<UnopenedResourcePack>newLinkedHashSet();
		Iterator<String> iterator = this.resourcePacks.iterator();

		while (iterator.hasNext()) {
			String string = (String)iterator.next();
			UnopenedResourcePack unopenedResourcePack = packRepository.getPack(string);
			if (unopenedResourcePack == null && !string.startsWith("file/")) {
				unopenedResourcePack = packRepository.getPack("file/" + string);
			}

			if (unopenedResourcePack == null) {
				LOGGER.warn("Removed resource pack {} from options because it doesn't seem to exist anymore", string);
				iterator.remove();
			} else if (!unopenedResourcePack.getCompatibility().isCompatible() && !this.incompatibleResourcePacks.contains(string)) {
				LOGGER.warn("Removed resource pack {} from options because it is no longer compatible", string);
				iterator.remove();
			} else if (unopenedResourcePack.getCompatibility().isCompatible() && this.incompatibleResourcePacks.contains(string)) {
				LOGGER.info("Removed resource pack {} from incompatibility list because it's now compatible", string);
				this.incompatibleResourcePacks.remove(string);
			} else {
				set.add(unopenedResourcePack);
			}
		}

		packRepository.setSelected(set);
	}
}
