package net.minecraft.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import java.util.List;
import java.util.Optional;
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
import net.minecraft.world.entity.player.ChatVisiblity;

@Environment(EnvType.CLIENT)
public abstract class Option {
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
		(options, double_) -> options.fov = double_,
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
		(options, double_) -> options.fovEffectScale = Mth.sqrt(double_),
		(options, progressOption) -> {
			progressOption.setTooltip(Minecraft.getInstance().font.split(ACCESSIBILITY_TOOLTIP_FOV_EFFECT, 200));
			double d = progressOption.toPct(progressOption.get(options));
			return d == 0.0 ? progressOption.genericValueLabel(new TranslatableComponent("options.fovEffectScale.off")) : progressOption.percentValueLabel(d);
		}
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
			progressOption.setTooltip(Minecraft.getInstance().font.split(ACCESSIBILITY_TOOLTIP_SCREEN_EFFECT, 200));
			double d = progressOption.toPct(progressOption.get(options));
			return d == 0.0 ? progressOption.genericValueLabel(new TranslatableComponent("options.screenEffectScale.off")) : progressOption.percentValueLabel(d);
		}
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
	public static final BooleanOption RAW_MOUSE_INPUT = new BooleanOption("options.rawMouseInput", options -> options.rawMouseInput, (options, boolean_) -> {
		options.rawMouseInput = boolean_;
		Window window = Minecraft.getInstance().getWindow();
		if (window != null) {
			window.updateRawMouseInput(boolean_);
		}
	});
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
	public static final CycleOption AMBIENT_OCCLUSION = new CycleOption("options.ao", (options, integer) -> {
		options.ambientOcclusion = AmbientOcclusionStatus.byId(options.ambientOcclusion.getId() + integer);
		Minecraft.getInstance().levelRenderer.allChanged();
	}, (options, cycleOption) -> cycleOption.genericValueLabel(new TranslatableComponent(options.ambientOcclusion.getKey())));
	public static final CycleOption ATTACK_INDICATOR = new CycleOption(
		"options.attackIndicator",
		(options, integer) -> options.attackIndicator = AttackIndicatorStatus.byId(options.attackIndicator.getId() + integer),
		(options, cycleOption) -> cycleOption.genericValueLabel(new TranslatableComponent(options.attackIndicator.getKey()))
	);
	public static final CycleOption CHAT_VISIBILITY = new CycleOption(
		"options.chat.visibility",
		(options, integer) -> options.chatVisibility = ChatVisiblity.byId((options.chatVisibility.getId() + integer) % 3),
		(options, cycleOption) -> cycleOption.genericValueLabel(new TranslatableComponent(options.chatVisibility.getKey()))
	);
	private static final Component GRAPHICS_TOOLTIP_FAST = new TranslatableComponent("options.graphics.fast.tooltip");
	private static final Component GRAPHICS_TOOLTIP_FABULOUS = new TranslatableComponent(
		"options.graphics.fabulous.tooltip", new TranslatableComponent("options.graphics.fabulous").withStyle(ChatFormatting.ITALIC)
	);
	private static final Component GRAPHICS_TOOLTIP_FANCY = new TranslatableComponent("options.graphics.fancy.tooltip");
	public static final CycleOption GRAPHICS = new CycleOption(
		"options.graphics",
		(options, integer) -> {
			Minecraft minecraft = Minecraft.getInstance();
			GpuWarnlistManager gpuWarnlistManager = minecraft.getGpuWarnlistManager();
			if (options.graphicsMode == GraphicsStatus.FANCY && gpuWarnlistManager.willShowWarning()) {
				gpuWarnlistManager.showWarning();
			} else {
				options.graphicsMode = options.graphicsMode.cycleNext();
				if (options.graphicsMode == GraphicsStatus.FABULOUS && (!GlStateManager.supportsFramebufferBlit() || gpuWarnlistManager.isSkippingFabulous())) {
					options.graphicsMode = GraphicsStatus.FAST;
				}

				minecraft.levelRenderer.allChanged();
			}
		},
		(options, cycleOption) -> {
			switch (options.graphicsMode) {
				case FAST:
					cycleOption.setTooltip(Minecraft.getInstance().font.split(GRAPHICS_TOOLTIP_FAST, 200));
					break;
				case FANCY:
					cycleOption.setTooltip(Minecraft.getInstance().font.split(GRAPHICS_TOOLTIP_FANCY, 200));
					break;
				case FABULOUS:
					cycleOption.setTooltip(Minecraft.getInstance().font.split(GRAPHICS_TOOLTIP_FABULOUS, 200));
			}

			MutableComponent mutableComponent = new TranslatableComponent(options.graphicsMode.getKey());
			return options.graphicsMode == GraphicsStatus.FABULOUS
				? cycleOption.genericValueLabel(mutableComponent.withStyle(ChatFormatting.ITALIC))
				: cycleOption.genericValueLabel(mutableComponent);
		}
	);
	public static final CycleOption GUI_SCALE = new CycleOption(
		"options.guiScale",
		(options, integer) -> options.guiScale = Integer.remainderUnsigned(
				options.guiScale + integer, Minecraft.getInstance().getWindow().calculateScale(0, Minecraft.getInstance().isEnforceUnicode()) + 1
			),
		(options, cycleOption) -> options.guiScale == 0
				? cycleOption.genericValueLabel(new TranslatableComponent("options.guiScale.auto"))
				: cycleOption.genericValueLabel(options.guiScale)
	);
	public static final CycleOption MAIN_HAND = new CycleOption(
		"options.mainHand",
		(options, integer) -> options.mainHand = options.mainHand.getOpposite(),
		(options, cycleOption) -> cycleOption.genericValueLabel(options.mainHand.getName())
	);
	public static final CycleOption NARRATOR = new CycleOption(
		"options.narrator",
		(options, integer) -> {
			if (NarratorChatListener.INSTANCE.isActive()) {
				options.narratorStatus = NarratorStatus.byId(options.narratorStatus.getId() + integer);
			} else {
				options.narratorStatus = NarratorStatus.OFF;
			}

			NarratorChatListener.INSTANCE.updateNarratorStatus(options.narratorStatus);
		},
		(options, cycleOption) -> NarratorChatListener.INSTANCE.isActive()
				? cycleOption.genericValueLabel(options.narratorStatus.getName())
				: cycleOption.genericValueLabel(new TranslatableComponent("options.narrator.notavailable"))
	);
	public static final CycleOption PARTICLES = new CycleOption(
		"options.particles",
		(options, integer) -> options.particles = ParticleStatus.byId(options.particles.getId() + integer),
		(options, cycleOption) -> cycleOption.genericValueLabel(new TranslatableComponent(options.particles.getKey()))
	);
	public static final CycleOption RENDER_CLOUDS = new CycleOption("options.renderClouds", (options, integer) -> {
		options.renderClouds = CloudStatus.byId(options.renderClouds.getId() + integer);
		if (Minecraft.useShaderTransparency()) {
			RenderTarget renderTarget = Minecraft.getInstance().levelRenderer.getCloudsTarget();
			if (renderTarget != null) {
				renderTarget.clear(Minecraft.ON_OSX);
			}
		}
	}, (options, cycleOption) -> cycleOption.genericValueLabel(new TranslatableComponent(options.renderClouds.getKey())));
	public static final CycleOption TEXT_BACKGROUND = new CycleOption(
		"options.accessibility.text_background",
		(options, integer) -> options.backgroundForChatOnly = !options.backgroundForChatOnly,
		(options, cycleOption) -> cycleOption.genericValueLabel(
				new TranslatableComponent(options.backgroundForChatOnly ? "options.accessibility.text_background.chat" : "options.accessibility.text_background.everywhere")
			)
	);
	private static final Component CHAT_TOOLTIP_HIDE_MATCHED_NAMES = new TranslatableComponent("options.hideMatchedNames.tooltip");
	public static final BooleanOption AUTO_JUMP = new BooleanOption(
		"options.autoJump", options -> options.autoJump, (options, boolean_) -> options.autoJump = boolean_
	);
	public static final BooleanOption AUTO_SUGGESTIONS = new BooleanOption(
		"options.autoSuggestCommands", options -> options.autoSuggestions, (options, boolean_) -> options.autoSuggestions = boolean_
	);
	public static final BooleanOption HIDE_MATCHED_NAMES = new BooleanOption(
		"options.hideMatchedNames", CHAT_TOOLTIP_HIDE_MATCHED_NAMES, options -> options.hideMatchedNames, (options, boolean_) -> options.hideMatchedNames = boolean_
	);
	public static final BooleanOption CHAT_COLOR = new BooleanOption(
		"options.chat.color", options -> options.chatColors, (options, boolean_) -> options.chatColors = boolean_
	);
	public static final BooleanOption CHAT_LINKS = new BooleanOption(
		"options.chat.links", options -> options.chatLinks, (options, boolean_) -> options.chatLinks = boolean_
	);
	public static final BooleanOption CHAT_LINKS_PROMPT = new BooleanOption(
		"options.chat.links.prompt", options -> options.chatLinksPrompt, (options, boolean_) -> options.chatLinksPrompt = boolean_
	);
	public static final BooleanOption DISCRETE_MOUSE_SCROLL = new BooleanOption(
		"options.discrete_mouse_scroll", options -> options.discreteMouseScroll, (options, boolean_) -> options.discreteMouseScroll = boolean_
	);
	public static final BooleanOption ENABLE_VSYNC = new BooleanOption("options.vsync", options -> options.enableVsync, (options, boolean_) -> {
		options.enableVsync = boolean_;
		if (Minecraft.getInstance().getWindow() != null) {
			Minecraft.getInstance().getWindow().updateVsync(options.enableVsync);
		}
	});
	public static final BooleanOption ENTITY_SHADOWS = new BooleanOption(
		"options.entityShadows", options -> options.entityShadows, (options, boolean_) -> options.entityShadows = boolean_
	);
	public static final BooleanOption FORCE_UNICODE_FONT = new BooleanOption(
		"options.forceUnicodeFont", options -> options.forceUnicodeFont, (options, boolean_) -> {
			options.forceUnicodeFont = boolean_;
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft.getWindow() != null) {
				minecraft.selectMainFont(boolean_);
			}
		}
	);
	public static final BooleanOption INVERT_MOUSE = new BooleanOption(
		"options.invertMouse", options -> options.invertYMouse, (options, boolean_) -> options.invertYMouse = boolean_
	);
	public static final BooleanOption REALMS_NOTIFICATIONS = new BooleanOption(
		"options.realmsNotifications", options -> options.realmsNotifications, (options, boolean_) -> options.realmsNotifications = boolean_
	);
	public static final BooleanOption REDUCED_DEBUG_INFO = new BooleanOption(
		"options.reducedDebugInfo", options -> options.reducedDebugInfo, (options, boolean_) -> options.reducedDebugInfo = boolean_
	);
	public static final BooleanOption SHOW_SUBTITLES = new BooleanOption(
		"options.showSubtitles", options -> options.showSubtitles, (options, boolean_) -> options.showSubtitles = boolean_
	);
	public static final BooleanOption SNOOPER_ENABLED = new BooleanOption("options.snooper", options -> {
		if (options.snooperEnabled) {
		}

		return false;
	}, (options, boolean_) -> options.snooperEnabled = boolean_);
	public static final CycleOption TOGGLE_CROUCH = new CycleOption(
		"key.sneak",
		(options, integer) -> options.toggleCrouch = !options.toggleCrouch,
		(options, cycleOption) -> cycleOption.genericValueLabel(new TranslatableComponent(options.toggleCrouch ? "options.key.toggle" : "options.key.hold"))
	);
	public static final CycleOption TOGGLE_SPRINT = new CycleOption(
		"key.sprint",
		(options, integer) -> options.toggleSprint = !options.toggleSprint,
		(options, cycleOption) -> cycleOption.genericValueLabel(new TranslatableComponent(options.toggleSprint ? "options.key.toggle" : "options.key.hold"))
	);
	public static final BooleanOption TOUCHSCREEN = new BooleanOption(
		"options.touchscreen", options -> options.touchscreen, (options, boolean_) -> options.touchscreen = boolean_
	);
	public static final BooleanOption USE_FULLSCREEN = new BooleanOption("options.fullscreen", options -> options.fullscreen, (options, boolean_) -> {
		options.fullscreen = boolean_;
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.getWindow() != null && minecraft.getWindow().isFullscreen() != options.fullscreen) {
			minecraft.getWindow().toggleFullScreen();
			options.fullscreen = minecraft.getWindow().isFullscreen();
		}
	});
	public static final BooleanOption VIEW_BOBBING = new BooleanOption(
		"options.viewBobbing", options -> options.bobView, (options, boolean_) -> options.bobView = boolean_
	);
	private final Component caption;
	private Optional<List<FormattedCharSequence>> toolTip = Optional.empty();

	public Option(String string) {
		this.caption = new TranslatableComponent(string);
	}

	public abstract AbstractWidget createButton(Options options, int i, int j, int k);

	protected Component getCaption() {
		return this.caption;
	}

	public void setTooltip(List<FormattedCharSequence> list) {
		this.toolTip = Optional.of(list);
	}

	public Optional<List<FormattedCharSequence>> getTooltip() {
		return this.toolTip;
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
