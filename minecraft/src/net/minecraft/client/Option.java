package net.minecraft.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
			MutableComponent mutableComponent = progressOption.createCaption();
			int i = (int)d * 2 + 1;
			return mutableComponent.append(new TranslatableComponent("options.biomeBlendRadius." + i));
		}
	);
	public static final ProgressOption CHAT_HEIGHT_FOCUSED = new ProgressOption(
		"options.chat.height.focused", 0.0, 1.0, 0.0F, options -> options.chatHeightFocused, (options, double_) -> {
			options.chatHeightFocused = double_;
			Minecraft.getInstance().gui.getChat().rescaleChat();
		}, (options, progressOption) -> {
			double d = progressOption.toPct(progressOption.get(options));
			return progressOption.createCaption().append(ChatComponent.getHeight(d) + "px");
		}
	);
	public static final ProgressOption CHAT_HEIGHT_UNFOCUSED = new ProgressOption(
		"options.chat.height.unfocused", 0.0, 1.0, 0.0F, options -> options.chatHeightUnfocused, (options, double_) -> {
			options.chatHeightUnfocused = double_;
			Minecraft.getInstance().gui.getChat().rescaleChat();
		}, (options, progressOption) -> {
			double d = progressOption.toPct(progressOption.get(options));
			return progressOption.createCaption().append(ChatComponent.getHeight(d) + "px");
		}
	);
	public static final ProgressOption CHAT_OPACITY = new ProgressOption(
		"options.chat.opacity", 0.0, 1.0, 0.0F, options -> options.chatOpacity, (options, double_) -> {
			options.chatOpacity = double_;
			Minecraft.getInstance().gui.getChat().rescaleChat();
		}, (options, progressOption) -> {
			double d = progressOption.toPct(progressOption.get(options));
			return progressOption.createCaption().append((int)(d * 90.0 + 10.0) + "%");
		}
	);
	public static final ProgressOption CHAT_SCALE = new ProgressOption("options.chat.scale", 0.0, 1.0, 0.0F, options -> options.chatScale, (options, double_) -> {
		options.chatScale = double_;
		Minecraft.getInstance().gui.getChat().rescaleChat();
	}, (options, progressOption) -> {
		double d = progressOption.toPct(progressOption.get(options));
		MutableComponent mutableComponent = progressOption.createCaption();
		return d == 0.0 ? mutableComponent.append(CommonComponents.OPTION_OFF) : mutableComponent.append((int)(d * 100.0) + "%");
	});
	public static final ProgressOption CHAT_WIDTH = new ProgressOption("options.chat.width", 0.0, 1.0, 0.0F, options -> options.chatWidth, (options, double_) -> {
		options.chatWidth = double_;
		Minecraft.getInstance().gui.getChat().rescaleChat();
	}, (options, progressOption) -> {
		double d = progressOption.toPct(progressOption.get(options));
		return progressOption.createCaption().append(ChatComponent.getWidth(d) + "px");
	});
	public static final ProgressOption CHAT_LINE_SPACING = new ProgressOption(
		"options.chat.line_spacing",
		0.0,
		1.0,
		0.0F,
		options -> options.chatLineSpacing,
		(options, double_) -> options.chatLineSpacing = double_,
		(options, progressOption) -> progressOption.createCaption().append((int)(progressOption.toPct(progressOption.get(options)) * 100.0) + "%")
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
			MutableComponent mutableComponent = progressOption.createCaption();
			if (d == 70.0) {
				return mutableComponent.append(new TranslatableComponent("options.fov.min"));
			} else {
				return d == progressOption.getMaxValue()
					? mutableComponent.append(new TranslatableComponent("options.fov.max"))
					: mutableComponent.append(Integer.toString((int)d));
			}
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
			MutableComponent mutableComponent = progressOption.createCaption();
			return d == progressOption.getMaxValue()
				? mutableComponent.append(new TranslatableComponent("options.framerateLimit.max"))
				: mutableComponent.append(new TranslatableComponent("options.framerate", (int)d));
		}
	);
	public static final ProgressOption GAMMA = new ProgressOption(
		"options.gamma", 0.0, 1.0, 0.0F, options -> options.gamma, (options, double_) -> options.gamma = double_, (options, progressOption) -> {
			double d = progressOption.toPct(progressOption.get(options));
			MutableComponent mutableComponent = progressOption.createCaption();
			if (d == 0.0) {
				return mutableComponent.append(new TranslatableComponent("options.gamma.min"));
			} else {
				return d == 1.0 ? mutableComponent.append(new TranslatableComponent("options.gamma.max")) : mutableComponent.append("+" + (int)(d * 100.0) + "%");
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
			MutableComponent mutableComponent = progressOption.createCaption();
			return d == 0.0 ? mutableComponent.append(CommonComponents.OPTION_OFF) : mutableComponent.append(Integer.toString((int)d));
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
			return progressOption.createCaption().append(String.format("%.2f", progressOption.toValue(d)));
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
			return progressOption.createCaption().append(new TranslatableComponent("options.chunks", (int)d));
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
			return progressOption.createCaption().append(new TranslatableComponent("options.entityDistancePercent", (int)(d * 100.0)));
		}
	);
	public static final ProgressOption SENSITIVITY = new ProgressOption(
		"options.sensitivity", 0.0, 1.0, 0.0F, options -> options.sensitivity, (options, double_) -> options.sensitivity = double_, (options, progressOption) -> {
			double d = progressOption.toPct(progressOption.get(options));
			MutableComponent mutableComponent = progressOption.createCaption();
			if (d == 0.0) {
				return mutableComponent.append(new TranslatableComponent("options.sensitivity.min"));
			} else {
				return d == 1.0 ? mutableComponent.append(new TranslatableComponent("options.sensitivity.max")) : mutableComponent.append((int)(d * 200.0) + "%");
			}
		}
	);
	public static final ProgressOption TEXT_BACKGROUND_OPACITY = new ProgressOption(
		"options.accessibility.text_background_opacity", 0.0, 1.0, 0.0F, options -> options.textBackgroundOpacity, (options, double_) -> {
			options.textBackgroundOpacity = double_;
			Minecraft.getInstance().gui.getChat().rescaleChat();
		}, (options, progressOption) -> progressOption.createCaption().append((int)(progressOption.toPct(progressOption.get(options)) * 100.0) + "%")
	);
	public static final CycleOption AMBIENT_OCCLUSION = new CycleOption("options.ao", (options, integer) -> {
		options.ambientOcclusion = AmbientOcclusionStatus.byId(options.ambientOcclusion.getId() + integer);
		Minecraft.getInstance().levelRenderer.allChanged();
	}, (options, cycleOption) -> cycleOption.createCaption().append(new TranslatableComponent(options.ambientOcclusion.getKey())));
	public static final CycleOption ATTACK_INDICATOR = new CycleOption(
		"options.attackIndicator",
		(options, integer) -> options.attackIndicator = AttackIndicatorStatus.byId(options.attackIndicator.getId() + integer),
		(options, cycleOption) -> cycleOption.createCaption().append(new TranslatableComponent(options.attackIndicator.getKey()))
	);
	public static final CycleOption CHAT_VISIBILITY = new CycleOption(
		"options.chat.visibility",
		(options, integer) -> options.chatVisibility = ChatVisiblity.byId((options.chatVisibility.getId() + integer) % 3),
		(options, cycleOption) -> cycleOption.createCaption().append(new TranslatableComponent(options.chatVisibility.getKey()))
	);
	public static final CycleOption GRAPHICS = new CycleOption(
		"options.graphics",
		(options, integer) -> {
			options.graphicsMode = options.graphicsMode.cycleNext();
			if (options.graphicsMode == GraphicsStatus.FABULOUS && !GlStateManager.supportsFramebufferBlit()) {
				options.graphicsMode = GraphicsStatus.FAST;
			}

			Minecraft.getInstance().levelRenderer.allChanged();
		},
		(options, cycleOption) -> {
			switch (options.graphicsMode) {
				case FAST:
					cycleOption.setTooltip("options.graphics.fast.tooltip");
					break;
				case FANCY:
					cycleOption.setTooltip("options.graphics.fancy.tooltip");
					break;
				case FABULOUS:
					cycleOption.setTooltip("options.graphics.fabulous.tooltip");
			}

			TranslatableComponent translatableComponent = new TranslatableComponent(options.graphicsMode.getKey());
			return options.graphicsMode == GraphicsStatus.FABULOUS
				? cycleOption.createCaption().append(new TextComponent("").append(translatableComponent).withStyle(ChatFormatting.ITALIC))
				: cycleOption.createCaption().append(translatableComponent);
		}
	);
	public static final CycleOption GUI_SCALE = new CycleOption(
		"options.guiScale",
		(options, integer) -> options.guiScale = Integer.remainderUnsigned(
				options.guiScale + integer, Minecraft.getInstance().getWindow().calculateScale(0, Minecraft.getInstance().isEnforceUnicode()) + 1
			),
		(options, cycleOption) -> {
			MutableComponent mutableComponent = cycleOption.createCaption();
			return options.guiScale == 0
				? mutableComponent.append(new TranslatableComponent("options.guiScale.auto"))
				: mutableComponent.append(Integer.toString(options.guiScale));
		}
	);
	public static final CycleOption MAIN_HAND = new CycleOption(
		"options.mainHand",
		(options, integer) -> options.mainHand = options.mainHand.getOpposite(),
		(options, cycleOption) -> cycleOption.createCaption().append(options.mainHand.getName())
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
				? cycleOption.createCaption().append(options.narratorStatus.getName())
				: cycleOption.createCaption().append(new TranslatableComponent("options.narrator.notavailable"))
	);
	public static final CycleOption PARTICLES = new CycleOption(
		"options.particles",
		(options, integer) -> options.particles = ParticleStatus.byId(options.particles.getId() + integer),
		(options, cycleOption) -> cycleOption.createCaption().append(new TranslatableComponent(options.particles.getKey()))
	);
	public static final CycleOption RENDER_CLOUDS = new CycleOption("options.renderClouds", (options, integer) -> {
		options.renderClouds = CloudStatus.byId(options.renderClouds.getId() + integer);
		if (Minecraft.useShaderTransparency()) {
			Minecraft.getInstance().levelRenderer.getCloudsTarget().clear(Minecraft.ON_OSX);
		}
	}, (options, cycleOption) -> cycleOption.createCaption().append(new TranslatableComponent(options.renderClouds.getKey())));
	public static final CycleOption TEXT_BACKGROUND = new CycleOption(
		"options.accessibility.text_background",
		(options, integer) -> options.backgroundForChatOnly = !options.backgroundForChatOnly,
		(options, cycleOption) -> cycleOption.createCaption()
				.append(
					new TranslatableComponent(
						options.backgroundForChatOnly ? "options.accessibility.text_background.chat" : "options.accessibility.text_background.everywhere"
					)
				)
	);
	public static final BooleanOption AUTO_JUMP = new BooleanOption(
		"options.autoJump", options -> options.autoJump, (options, boolean_) -> options.autoJump = boolean_
	);
	public static final BooleanOption AUTO_SUGGESTIONS = new BooleanOption(
		"options.autoSuggestCommands", options -> options.autoSuggestions, (options, boolean_) -> options.autoSuggestions = boolean_
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
		(options, cycleOption) -> cycleOption.createCaption().append(new TranslatableComponent(options.toggleCrouch ? "options.key.toggle" : "options.key.hold"))
	);
	public static final CycleOption TOGGLE_SPRINT = new CycleOption(
		"key.sprint",
		(options, integer) -> options.toggleSprint = !options.toggleSprint,
		(options, cycleOption) -> cycleOption.createCaption().append(new TranslatableComponent(options.toggleSprint ? "options.key.toggle" : "options.key.hold"))
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
	private final String captionId;
	private Optional<TranslatableComponent> toolTip;

	public Option(String string) {
		this.captionId = string;
		this.toolTip = Optional.empty();
	}

	public abstract AbstractWidget createButton(Options options, int i, int j, int k);

	public MutableComponent createCaption() {
		return new TranslatableComponent(this.captionId).append(": ");
	}

	public void setTooltip(String string) {
		this.toolTip = Optional.of(new TranslatableComponent(string));
	}

	public Optional<TranslatableComponent> getTooltip() {
		return this.toolTip;
	}
}
