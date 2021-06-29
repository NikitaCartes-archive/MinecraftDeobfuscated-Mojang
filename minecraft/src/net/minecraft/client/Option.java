package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;

@Environment(EnvType.CLIENT)
public abstract class Option {
	protected static final int OPTIONS_TOOLTIP_WIDTH = 200;
	public static final ProgressOption BIOME_BLEND_RADIUS = new ProgressOption(
		"options.biomeBlendRadius", 0.0, 7.0, 1.0F, options -> (double)options.biomeBlendRadius, (options, double_) -> {
			options.biomeBlendRadius = Mth.clamp((int)double_.doubleValue(), 0, 7);
			Minecraft.getInstance().levelRenderer.allChanged();
		}, (options, progressOption) -> {
			double d = progressOption.get(options);
			int i = (int)d * 2 + 1;
			return progressOption.genericValueLabel(new TranslatableComponent("options.biomeBlendRadius." + i));
		}
	);
	public static final ProgressOption CHAT_HEIGHT_FOCUSED = new ProgressOption(
		"options.chat.height.focused", 0.0, 1.0, 0.0F, options -> options.chatHeightFocused, (options, double_) -> {
			options.chatHeightFocused = double_;
			Minecraft.getInstance().gui.getChat().rescaleChat();
		}, (options, progressOption) -> {
			double d = progressOption.toPct(progressOption.get(options));
			return progressOption.pixelValueLabel(ChatComponent.getHeight(d));
		}
	);
	public static final ProgressOption CHAT_HEIGHT_UNFOCUSED = new ProgressOption(
		"options.chat.height.unfocused", 0.0, 1.0, 0.0F, options -> options.chatHeightUnfocused, (options, double_) -> {
			options.chatHeightUnfocused = double_;
			Minecraft.getInstance().gui.getChat().rescaleChat();
		}, (options, progressOption) -> {
			double d = progressOption.toPct(progressOption.get(options));
			return progressOption.pixelValueLabel(ChatComponent.getHeight(d));
		}
	);
	public static final ProgressOption CHAT_OPACITY = new ProgressOption(
		"options.chat.opacity", 0.0, 1.0, 0.0F, options -> options.chatOpacity, (options, double_) -> {
			options.chatOpacity = double_;
			Minecraft.getInstance().gui.getChat().rescaleChat();
		}, (options, progressOption) -> {
			double d = progressOption.toPct(progressOption.get(options));
			return progressOption.percentValueLabel(d * 0.9 + 0.1);
		}
	);
	public static final ProgressOption CHAT_SCALE = new ProgressOption("options.chat.scale", 0.0, 1.0, 0.0F, options -> options.chatScale, (options, double_) -> {
		options.chatScale = double_;
		Minecraft.getInstance().gui.getChat().rescaleChat();
	}, (options, progressOption) -> {
		double d = progressOption.toPct(progressOption.get(options));
		return (Component)(d == 0.0 ? CommonComponents.optionStatus(progressOption.getCaption(), false) : progressOption.percentValueLabel(d));
	});
	public static final ProgressOption CHAT_WIDTH = new ProgressOption("options.chat.width", 0.0, 1.0, 0.0F, options -> options.chatWidth, (options, double_) -> {
		options.chatWidth = double_;
		Minecraft.getInstance().gui.getChat().rescaleChat();
	}, (options, progressOption) -> {
		double d = progressOption.toPct(progressOption.get(options));
		return progressOption.pixelValueLabel(ChatComponent.getWidth(d));
	});
	public static final ProgressOption CHAT_LINE_SPACING = new ProgressOption(
		"options.chat.line_spacing",
		0.0,
		1.0,
		0.0F,
		options -> options.chatLineSpacing,
		(options, double_) -> options.chatLineSpacing = double_,
		(options, progressOption) -> progressOption.percentValueLabel(progressOption.toPct(progressOption.get(options)))
	);
	public static final ProgressOption CHAT_DELAY = new ProgressOption(
		"options.chat.delay_instant", 0.0, 6.0, 0.1F, options -> options.chatDelay, (options, double_) -> options.chatDelay = double_, (options, progressOption) -> {
			double d = progressOption.get(options);
			return d <= 0.0 ? new TranslatableComponent("options.chat.delay_none") : new TranslatableComponent("options.chat.delay", String.format("%.1f", d));
		}
	);
	public static final ProgressOption FOV = new ProgressOption(
		"options.fov",
		30.0,
		110.0,
		1.0F,
		options -> options.fov,
		(options, double_) -> {
			options.fov = double_;
			Minecraft.getInstance().levelRenderer.needsUpdate();
		},
		(options, progressOption) -> {
			double d = progressOption.get(options);
			if (d == 70.0) {
				return progressOption.genericValueLabel(new TranslatableComponent("options.fov.min"));
			} else {
				return d == progressOption.getMaxValue()
					? progressOption.genericValueLabel(new TranslatableComponent("options.fov.max"))
					: progressOption.genericValueLabel((int)d);
			}
		}
	);
	private static final Component ACCESSIBILITY_TOOLTIP_FOV_EFFECT = new TranslatableComponent("options.fovEffectScale.tooltip");
	public static final ProgressOption FOV_EFFECTS_SCALE = new ProgressOption(
		"options.fovEffectScale",
		0.0,
		1.0,
		0.0F,
		options -> Math.pow((double)options.fovEffectScale, 2.0),
		(options, double_) -> options.fovEffectScale = (float)Math.sqrt(double_),
		(options, progressOption) -> {
			double d = progressOption.toPct(progressOption.get(options));
			return d == 0.0 ? progressOption.genericValueLabel(CommonComponents.OPTION_OFF) : progressOption.percentValueLabel(d);
		},
		minecraft -> minecraft.font.split(ACCESSIBILITY_TOOLTIP_FOV_EFFECT, 200)
	);
	private static final Component ACCESSIBILITY_TOOLTIP_SCREEN_EFFECT = new TranslatableComponent("options.screenEffectScale.tooltip");
	public static final ProgressOption SCREEN_EFFECTS_SCALE = new ProgressOption(
		"options.screenEffectScale",
		0.0,
		1.0,
		0.0F,
		options -> (double)options.screenEffectScale,
		(options, double_) -> options.screenEffectScale = double_.floatValue(),
		(options, progressOption) -> {
			double d = progressOption.toPct(progressOption.get(options));
			return d == 0.0 ? progressOption.genericValueLabel(CommonComponents.OPTION_OFF) : progressOption.percentValueLabel(d);
		},
		minecraft -> minecraft.font.split(ACCESSIBILITY_TOOLTIP_SCREEN_EFFECT, 200)
	);
	public static final ProgressOption FRAMERATE_LIMIT = new ProgressOption(
		"options.framerateLimit",
		10.0,
		260.0,
		10.0F,
		options -> (double)options.framerateLimit,
		(options, double_) -> {
			options.framerateLimit = (int)double_.doubleValue();
			Minecraft.getInstance().getWindow().setFramerateLimit(options.framerateLimit);
		},
		(options, progressOption) -> {
			double d = progressOption.get(options);
			return d == progressOption.getMaxValue()
				? progressOption.genericValueLabel(new TranslatableComponent("options.framerateLimit.max"))
				: progressOption.genericValueLabel(new TranslatableComponent("options.framerate", (int)d));
		}
	);
	public static final ProgressOption GAMMA = new ProgressOption(
		"options.gamma", 0.0, 1.0, 0.0F, options -> options.gamma, (options, double_) -> options.gamma = double_, (options, progressOption) -> {
			double d = progressOption.toPct(progressOption.get(options));
			if (d == 0.0) {
				return progressOption.genericValueLabel(new TranslatableComponent("options.gamma.min"));
			} else {
				return d == 1.0 ? progressOption.genericValueLabel(new TranslatableComponent("options.gamma.max")) : progressOption.percentAddValueLabel((int)(d * 100.0));
			}
		}
	);
	public static final ProgressOption MIPMAP_LEVELS = new ProgressOption(
		"options.mipmapLevels",
		0.0,
		4.0,
		1.0F,
		options -> (double)options.mipmapLevels,
		(options, double_) -> options.mipmapLevels = (int)double_.doubleValue(),
		(options, progressOption) -> {
			double d = progressOption.get(options);
			return (Component)(d == 0.0 ? CommonComponents.optionStatus(progressOption.getCaption(), false) : progressOption.genericValueLabel((int)d));
		}
	);
	public static final ProgressOption MOUSE_WHEEL_SENSITIVITY = new LogaritmicProgressOption(
		"options.mouseWheelSensitivity",
		0.01,
		10.0,
		0.01F,
		options -> options.mouseWheelSensitivity,
		(options, double_) -> options.mouseWheelSensitivity = double_,
		(options, progressOption) -> {
			double d = progressOption.toPct(progressOption.get(options));
			return progressOption.genericValueLabel(new TextComponent(String.format("%.2f", progressOption.toValue(d))));
		}
	);
	public static final CycleOption<Boolean> RAW_MOUSE_INPUT = CycleOption.createOnOff(
		"options.rawMouseInput", options -> options.rawMouseInput, (options, option, boolean_) -> {
			options.rawMouseInput = boolean_;
			Window window = Minecraft.getInstance().getWindow();
			if (window != null) {
				window.updateRawMouseInput(boolean_);
			}
		}
	);
	public static final ProgressOption RENDER_DISTANCE = new ProgressOption(
		"options.renderDistance", 2.0, 16.0, 1.0F, options -> (double)options.renderDistance, (options, double_) -> {
			options.renderDistance = (int)double_.doubleValue();
			Minecraft.getInstance().levelRenderer.needsUpdate();
		}, (options, progressOption) -> {
			double d = progressOption.get(options);
			return progressOption.genericValueLabel(new TranslatableComponent("options.chunks", (int)d));
		}
	);
	public static final ProgressOption ENTITY_DISTANCE_SCALING = new ProgressOption(
		"options.entityDistanceScaling",
		0.5,
		5.0,
		0.25F,
		options -> (double)options.entityDistanceScaling,
		(options, double_) -> options.entityDistanceScaling = (float)double_.doubleValue(),
		(options, progressOption) -> {
			double d = progressOption.get(options);
			return progressOption.percentValueLabel(d);
		}
	);
	public static final ProgressOption SENSITIVITY = new ProgressOption(
		"options.sensitivity", 0.0, 1.0, 0.0F, options -> options.sensitivity, (options, double_) -> options.sensitivity = double_, (options, progressOption) -> {
			double d = progressOption.toPct(progressOption.get(options));
			if (d == 0.0) {
				return progressOption.genericValueLabel(new TranslatableComponent("options.sensitivity.min"));
			} else {
				return d == 1.0 ? progressOption.genericValueLabel(new TranslatableComponent("options.sensitivity.max")) : progressOption.percentValueLabel(2.0 * d);
			}
		}
	);
	public static final ProgressOption TEXT_BACKGROUND_OPACITY = new ProgressOption(
		"options.accessibility.text_background_opacity", 0.0, 1.0, 0.0F, options -> options.textBackgroundOpacity, (options, double_) -> {
			options.textBackgroundOpacity = double_;
			Minecraft.getInstance().gui.getChat().rescaleChat();
		}, (options, progressOption) -> progressOption.percentValueLabel(progressOption.toPct(progressOption.get(options)))
	);
	public static final CycleOption<AmbientOcclusionStatus> AMBIENT_OCCLUSION = CycleOption.create(
		"options.ao",
		AmbientOcclusionStatus.values(),
		ambientOcclusionStatus -> new TranslatableComponent(ambientOcclusionStatus.getKey()),
		options -> options.ambientOcclusion,
		(options, option, ambientOcclusionStatus) -> {
			options.ambientOcclusion = ambientOcclusionStatus;
			Minecraft.getInstance().levelRenderer.allChanged();
		}
	);
	public static final CycleOption<AttackIndicatorStatus> ATTACK_INDICATOR = CycleOption.create(
		"options.attackIndicator",
		AttackIndicatorStatus.values(),
		attackIndicatorStatus -> new TranslatableComponent(attackIndicatorStatus.getKey()),
		options -> options.attackIndicator,
		(options, option, attackIndicatorStatus) -> options.attackIndicator = attackIndicatorStatus
	);
	public static final CycleOption<ChatVisiblity> CHAT_VISIBILITY = CycleOption.create(
		"options.chat.visibility",
		ChatVisiblity.values(),
		chatVisiblity -> new TranslatableComponent(chatVisiblity.getKey()),
		options -> options.chatVisibility,
		(options, option, chatVisiblity) -> options.chatVisibility = chatVisiblity
	);
	private static final Component GRAPHICS_TOOLTIP_FAST = new TranslatableComponent("options.graphics.fast.tooltip");
	private static final Component GRAPHICS_TOOLTIP_FABULOUS = new TranslatableComponent(
		"options.graphics.fabulous.tooltip", new TranslatableComponent("options.graphics.fabulous").withStyle(ChatFormatting.ITALIC)
	);
	private static final Component GRAPHICS_TOOLTIP_FANCY = new TranslatableComponent("options.graphics.fancy.tooltip");
	public static final CycleOption<GraphicsStatus> GRAPHICS = CycleOption.<GraphicsStatus>create(
			"options.graphics",
			Arrays.asList(GraphicsStatus.values()),
			(List<GraphicsStatus>)Stream.of(GraphicsStatus.values()).filter(graphicsStatus -> graphicsStatus != GraphicsStatus.FABULOUS).collect(Collectors.toList()),
			() -> Minecraft.getInstance().getGpuWarnlistManager().isSkippingFabulous(),
			graphicsStatus -> {
				MutableComponent mutableComponent = new TranslatableComponent(graphicsStatus.getKey());
				return graphicsStatus == GraphicsStatus.FABULOUS ? mutableComponent.withStyle(ChatFormatting.ITALIC) : mutableComponent;
			},
			options -> options.graphicsMode,
			(options, option, graphicsStatus) -> {
				Minecraft minecraft = Minecraft.getInstance();
				GpuWarnlistManager gpuWarnlistManager = minecraft.getGpuWarnlistManager();
				if (graphicsStatus == GraphicsStatus.FABULOUS && gpuWarnlistManager.willShowWarning()) {
					gpuWarnlistManager.showWarning();
				} else {
					options.graphicsMode = graphicsStatus;
					minecraft.levelRenderer.allChanged();
				}
			}
		)
		.setTooltip(minecraft -> {
			List<FormattedCharSequence> list = minecraft.font.split(GRAPHICS_TOOLTIP_FAST, 200);
			List<FormattedCharSequence> list2 = minecraft.font.split(GRAPHICS_TOOLTIP_FANCY, 200);
			List<FormattedCharSequence> list3 = minecraft.font.split(GRAPHICS_TOOLTIP_FABULOUS, 200);
			return graphicsStatus -> {
				switch (graphicsStatus) {
					case FANCY:
						return list2;
					case FAST:
						return list;
					case FABULOUS:
						return list3;
					default:
						return ImmutableList.of();
				}
			};
		});
	public static final CycleOption GUI_SCALE = CycleOption.create(
		"options.guiScale",
		(Supplier<List<Integer>>)(() -> (List)IntStream.rangeClosed(
					0, Minecraft.getInstance().getWindow().calculateScale(0, Minecraft.getInstance().isEnforceUnicode())
				)
				.boxed()
				.collect(Collectors.toList())),
		integer -> (Component)(integer == 0 ? new TranslatableComponent("options.guiScale.auto") : new TextComponent(Integer.toString(integer))),
		options -> options.guiScale,
		(options, option, integer) -> options.guiScale = integer
	);
	public static final CycleOption<HumanoidArm> MAIN_HAND = CycleOption.create(
		"options.mainHand", HumanoidArm.values(), HumanoidArm::getName, options -> options.mainHand, (options, option, humanoidArm) -> {
			options.mainHand = humanoidArm;
			options.broadcastOptions();
		}
	);
	public static final CycleOption<NarratorStatus> NARRATOR = CycleOption.create(
		"options.narrator",
		NarratorStatus.values(),
		narratorStatus -> (Component)(NarratorChatListener.INSTANCE.isActive()
				? narratorStatus.getName()
				: new TranslatableComponent("options.narrator.notavailable")),
		options -> options.narratorStatus,
		(options, option, narratorStatus) -> {
			options.narratorStatus = narratorStatus;
			NarratorChatListener.INSTANCE.updateNarratorStatus(narratorStatus);
		}
	);
	public static final CycleOption<ParticleStatus> PARTICLES = CycleOption.create(
		"options.particles",
		ParticleStatus.values(),
		particleStatus -> new TranslatableComponent(particleStatus.getKey()),
		options -> options.particles,
		(options, option, particleStatus) -> options.particles = particleStatus
	);
	public static final CycleOption<CloudStatus> RENDER_CLOUDS = CycleOption.create(
		"options.renderClouds",
		CloudStatus.values(),
		cloudStatus -> new TranslatableComponent(cloudStatus.getKey()),
		options -> options.renderClouds,
		(options, option, cloudStatus) -> {
			options.renderClouds = cloudStatus;
			if (Minecraft.useShaderTransparency()) {
				RenderTarget renderTarget = Minecraft.getInstance().levelRenderer.getCloudsTarget();
				if (renderTarget != null) {
					renderTarget.clear(Minecraft.ON_OSX);
				}
			}
		}
	);
	public static final CycleOption<Boolean> TEXT_BACKGROUND = CycleOption.createBinaryOption(
		"options.accessibility.text_background",
		new TranslatableComponent("options.accessibility.text_background.chat"),
		new TranslatableComponent("options.accessibility.text_background.everywhere"),
		options -> options.backgroundForChatOnly,
		(options, option, boolean_) -> options.backgroundForChatOnly = boolean_
	);
	private static final Component CHAT_TOOLTIP_HIDE_MATCHED_NAMES = new TranslatableComponent("options.hideMatchedNames.tooltip");
	public static final CycleOption<Boolean> AUTO_JUMP = CycleOption.createOnOff(
		"options.autoJump", options -> options.autoJump, (options, option, boolean_) -> options.autoJump = boolean_
	);
	public static final CycleOption<Boolean> AUTO_SUGGESTIONS = CycleOption.createOnOff(
		"options.autoSuggestCommands", options -> options.autoSuggestions, (options, option, boolean_) -> options.autoSuggestions = boolean_
	);
	public static final CycleOption<Boolean> CHAT_COLOR = CycleOption.createOnOff(
		"options.chat.color", options -> options.chatColors, (options, option, boolean_) -> options.chatColors = boolean_
	);
	public static final CycleOption<Boolean> HIDE_MATCHED_NAMES = CycleOption.createOnOff(
		"options.hideMatchedNames",
		CHAT_TOOLTIP_HIDE_MATCHED_NAMES,
		options -> options.hideMatchedNames,
		(options, option, boolean_) -> options.hideMatchedNames = boolean_
	);
	public static final CycleOption<Boolean> CHAT_LINKS = CycleOption.createOnOff(
		"options.chat.links", options -> options.chatLinks, (options, option, boolean_) -> options.chatLinks = boolean_
	);
	public static final CycleOption<Boolean> CHAT_LINKS_PROMPT = CycleOption.createOnOff(
		"options.chat.links.prompt", options -> options.chatLinksPrompt, (options, option, boolean_) -> options.chatLinksPrompt = boolean_
	);
	public static final CycleOption<Boolean> DISCRETE_MOUSE_SCROLL = CycleOption.createOnOff(
		"options.discrete_mouse_scroll", options -> options.discreteMouseScroll, (options, option, boolean_) -> options.discreteMouseScroll = boolean_
	);
	public static final CycleOption<Boolean> ENABLE_VSYNC = CycleOption.createOnOff(
		"options.vsync", options -> options.enableVsync, (options, option, boolean_) -> {
			options.enableVsync = boolean_;
			if (Minecraft.getInstance().getWindow() != null) {
				Minecraft.getInstance().getWindow().updateVsync(options.enableVsync);
			}
		}
	);
	public static final CycleOption<Boolean> ENTITY_SHADOWS = CycleOption.createOnOff(
		"options.entityShadows", options -> options.entityShadows, (options, option, boolean_) -> options.entityShadows = boolean_
	);
	public static final CycleOption<Boolean> FORCE_UNICODE_FONT = CycleOption.createOnOff(
		"options.forceUnicodeFont", options -> options.forceUnicodeFont, (options, option, boolean_) -> {
			options.forceUnicodeFont = boolean_;
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft.getWindow() != null) {
				minecraft.selectMainFont(boolean_);
				minecraft.resizeDisplay();
			}
		}
	);
	public static final CycleOption<Boolean> INVERT_MOUSE = CycleOption.createOnOff(
		"options.invertMouse", options -> options.invertYMouse, (options, option, boolean_) -> options.invertYMouse = boolean_
	);
	public static final CycleOption<Boolean> REALMS_NOTIFICATIONS = CycleOption.createOnOff(
		"options.realmsNotifications", options -> options.realmsNotifications, (options, option, boolean_) -> options.realmsNotifications = boolean_
	);
	public static final CycleOption<Boolean> REDUCED_DEBUG_INFO = CycleOption.createOnOff(
		"options.reducedDebugInfo", options -> options.reducedDebugInfo, (options, option, boolean_) -> options.reducedDebugInfo = boolean_
	);
	public static final CycleOption<Boolean> SHOW_SUBTITLES = CycleOption.createOnOff(
		"options.showSubtitles", options -> options.showSubtitles, (options, option, boolean_) -> options.showSubtitles = boolean_
	);
	public static final CycleOption<Boolean> SNOOPER_ENABLED = CycleOption.createOnOff("options.snooper", options -> {
		if (options.snooperEnabled) {
		}

		return false;
	}, (options, option, boolean_) -> options.snooperEnabled = boolean_);
	private static final Component MOVEMENT_TOGGLE = new TranslatableComponent("options.key.toggle");
	private static final Component MOVEMENT_HOLD = new TranslatableComponent("options.key.hold");
	public static final CycleOption<Boolean> TOGGLE_CROUCH = CycleOption.createBinaryOption(
		"key.sneak", MOVEMENT_TOGGLE, MOVEMENT_HOLD, options -> options.toggleCrouch, (options, option, boolean_) -> options.toggleCrouch = boolean_
	);
	public static final CycleOption<Boolean> TOGGLE_SPRINT = CycleOption.createBinaryOption(
		"key.sprint", MOVEMENT_TOGGLE, MOVEMENT_HOLD, options -> options.toggleSprint, (options, option, boolean_) -> options.toggleSprint = boolean_
	);
	public static final CycleOption<Boolean> TOUCHSCREEN = CycleOption.createOnOff(
		"options.touchscreen", options -> options.touchscreen, (options, option, boolean_) -> options.touchscreen = boolean_
	);
	public static final CycleOption<Boolean> USE_FULLSCREEN = CycleOption.createOnOff(
		"options.fullscreen", options -> options.fullscreen, (options, option, boolean_) -> {
			options.fullscreen = boolean_;
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft.getWindow() != null && minecraft.getWindow().isFullscreen() != options.fullscreen) {
				minecraft.getWindow().toggleFullScreen();
				options.fullscreen = minecraft.getWindow().isFullscreen();
			}
		}
	);
	public static final CycleOption<Boolean> VIEW_BOBBING = CycleOption.createOnOff(
		"options.viewBobbing", options -> options.bobView, (options, option, boolean_) -> options.bobView = boolean_
	);
	private static final Component ACCESSIBILITY_TOOLTIP_DARK_MOJANG_BACKGROUND = new TranslatableComponent("options.darkMojangStudiosBackgroundColor.tooltip");
	public static final CycleOption<Boolean> DARK_MOJANG_STUDIOS_BACKGROUND_COLOR = CycleOption.createOnOff(
		"options.darkMojangStudiosBackgroundColor",
		ACCESSIBILITY_TOOLTIP_DARK_MOJANG_BACKGROUND,
		options -> options.darkMojangStudiosBackground,
		(options, option, boolean_) -> options.darkMojangStudiosBackground = boolean_
	);
	private final Component caption;

	public Option(String string) {
		this.caption = new TranslatableComponent(string);
	}

	public abstract AbstractWidget createButton(Options options, int i, int j, int k);

	protected Component getCaption() {
		return this.caption;
	}

	protected Component pixelValueLabel(int i) {
		return new TranslatableComponent("options.pixel_value", this.getCaption(), i);
	}

	protected Component percentValueLabel(double d) {
		return new TranslatableComponent("options.percent_value", this.getCaption(), (int)(d * 100.0));
	}

	protected Component percentAddValueLabel(int i) {
		return new TranslatableComponent("options.percent_add_value", this.getCaption(), i);
	}

	protected Component genericValueLabel(Component component) {
		return new TranslatableComponent("options.generic_value", this.getCaption(), component);
	}

	protected Component genericValueLabel(int i) {
		return this.genericValueLabel(new TextComponent(Integer.toString(i)));
	}
}
