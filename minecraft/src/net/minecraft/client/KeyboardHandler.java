package net.minecraft.client;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.ClipboardManager;
import com.mojang.blaze3d.platform.InputConstants;
import java.text.MessageFormat;
import java.util.Locale;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SimpleOptionsSubScreen;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class KeyboardHandler {
	public static final int DEBUG_CRASH_TIME = 10000;
	private final Minecraft minecraft;
	private boolean sendRepeatsToGui;
	private final ClipboardManager clipboardManager = new ClipboardManager();
	private long debugCrashKeyTime = -1L;
	private long debugCrashKeyReportedTime = -1L;
	private long debugCrashKeyReportedCount = -1L;
	private boolean handledDebugKey;

	public KeyboardHandler(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	private boolean handleChunkDebugKeys(int i) {
		switch (i) {
			case 69:
				this.minecraft.chunkPath = !this.minecraft.chunkPath;
				this.debugFeedback("ChunkPath: {0}", this.minecraft.chunkPath ? "shown" : "hidden");
				return true;
			case 76:
				this.minecraft.smartCull = !this.minecraft.smartCull;
				this.debugFeedback("SmartCull: {0}", this.minecraft.smartCull ? "enabled" : "disabled");
				return true;
			case 85:
				if (Screen.hasShiftDown()) {
					this.minecraft.levelRenderer.killFrustum();
					this.debugFeedback("Killed frustum");
				} else {
					this.minecraft.levelRenderer.captureFrustum();
					this.debugFeedback("Captured frustum");
				}

				return true;
			case 86:
				this.minecraft.chunkVisibility = !this.minecraft.chunkVisibility;
				this.debugFeedback("ChunkVisibility: {0}", this.minecraft.chunkVisibility ? "enabled" : "disabled");
				return true;
			case 87:
				this.minecraft.wireframe = !this.minecraft.wireframe;
				this.debugFeedback("WireFrame: {0}", this.minecraft.wireframe ? "enabled" : "disabled");
				return true;
			default:
				return false;
		}
	}

	private void debugComponent(ChatFormatting chatFormatting, Component component) {
		this.minecraft
			.gui
			.getChat()
			.addMessage(
				new TextComponent("")
					.append(new TranslatableComponent("debug.prefix").withStyle(new ChatFormatting[]{chatFormatting, ChatFormatting.BOLD}))
					.append(" ")
					.append(component)
			);
	}

	private void debugFeedbackComponent(Component component) {
		this.debugComponent(ChatFormatting.YELLOW, component);
	}

	private void debugFeedbackTranslated(String string, Object... objects) {
		this.debugFeedbackComponent(new TranslatableComponent(string, objects));
	}

	private void debugWarningTranslated(String string, Object... objects) {
		this.debugComponent(ChatFormatting.RED, new TranslatableComponent(string, objects));
	}

	private void debugFeedback(String string, Object... objects) {
		this.debugFeedbackComponent(new TextComponent(MessageFormat.format(string, objects)));
	}

	private boolean handleDebugKeys(int i) {
		if (this.debugCrashKeyTime > 0L && this.debugCrashKeyTime < Util.getMillis() - 100L) {
			return true;
		} else {
			switch (i) {
				case 65:
					this.minecraft.levelRenderer.allChanged();
					this.debugFeedbackTranslated("debug.reload_chunks.message");
					return true;
				case 66:
					boolean bl = !this.minecraft.getEntityRenderDispatcher().shouldRenderHitBoxes();
					this.minecraft.getEntityRenderDispatcher().setRenderHitBoxes(bl);
					this.debugFeedbackTranslated(bl ? "debug.show_hitboxes.on" : "debug.show_hitboxes.off");
					return true;
				case 67:
					if (this.minecraft.player.isReducedDebugInfo()) {
						return false;
					} else {
						ClientPacketListener clientPacketListener = this.minecraft.player.connection;
						if (clientPacketListener == null) {
							return false;
						}

						this.debugFeedbackTranslated("debug.copy_location.message");
						this.setClipboard(
							String.format(
								Locale.ROOT,
								"/execute in %s run tp @s %.2f %.2f %.2f %.2f %.2f",
								this.minecraft.player.level.dimension().location(),
								this.minecraft.player.getX(),
								this.minecraft.player.getY(),
								this.minecraft.player.getZ(),
								this.minecraft.player.getYRot(),
								this.minecraft.player.getXRot()
							)
						);
						return true;
					}
				case 68:
					if (this.minecraft.gui != null) {
						this.minecraft.gui.getChat().clearMessages(false);
					}

					return true;
				case 70:
					Option.RENDER_DISTANCE
						.set(
							this.minecraft.options,
							Mth.clamp(
								(double)(this.minecraft.options.renderDistance + (Screen.hasShiftDown() ? -1 : 1)),
								Option.RENDER_DISTANCE.getMinValue(),
								Option.RENDER_DISTANCE.getMaxValue()
							)
						);
					this.debugFeedbackTranslated("debug.cycle_renderdistance.message", this.minecraft.options.renderDistance);
					return true;
				case 71:
					boolean bl2 = this.minecraft.debugRenderer.switchRenderChunkborder();
					this.debugFeedbackTranslated(bl2 ? "debug.chunk_boundaries.on" : "debug.chunk_boundaries.off");
					return true;
				case 72:
					this.minecraft.options.advancedItemTooltips = !this.minecraft.options.advancedItemTooltips;
					this.debugFeedbackTranslated(this.minecraft.options.advancedItemTooltips ? "debug.advanced_tooltips.on" : "debug.advanced_tooltips.off");
					this.minecraft.options.save();
					return true;
				case 73:
					if (!this.minecraft.player.isReducedDebugInfo()) {
						this.copyRecreateCommand(this.minecraft.player.hasPermissions(2), !Screen.hasShiftDown());
					}

					return true;
				case 76:
					if (this.minecraft.debugClientMetricsStart(this::debugFeedbackComponent)) {
						this.debugFeedbackTranslated("debug.profiling.start", 10);
					}

					return true;
				case 78:
					if (!this.minecraft.player.hasPermissions(2)) {
						this.debugFeedbackTranslated("debug.creative_spectator.error");
					} else if (!this.minecraft.player.isSpectator()) {
						this.minecraft.player.chat("/gamemode spectator");
					} else {
						this.minecraft.player.chat("/gamemode " + MoreObjects.firstNonNull(this.minecraft.gameMode.getPreviousPlayerMode(), GameType.CREATIVE).getName());
					}

					return true;
				case 80:
					this.minecraft.options.pauseOnLostFocus = !this.minecraft.options.pauseOnLostFocus;
					this.minecraft.options.save();
					this.debugFeedbackTranslated(this.minecraft.options.pauseOnLostFocus ? "debug.pause_focus.on" : "debug.pause_focus.off");
					return true;
				case 81:
					this.debugFeedbackTranslated("debug.help.message");
					ChatComponent chatComponent = this.minecraft.gui.getChat();
					chatComponent.addMessage(new TranslatableComponent("debug.reload_chunks.help"));
					chatComponent.addMessage(new TranslatableComponent("debug.show_hitboxes.help"));
					chatComponent.addMessage(new TranslatableComponent("debug.copy_location.help"));
					chatComponent.addMessage(new TranslatableComponent("debug.clear_chat.help"));
					chatComponent.addMessage(new TranslatableComponent("debug.cycle_renderdistance.help"));
					chatComponent.addMessage(new TranslatableComponent("debug.chunk_boundaries.help"));
					chatComponent.addMessage(new TranslatableComponent("debug.advanced_tooltips.help"));
					chatComponent.addMessage(new TranslatableComponent("debug.inspect.help"));
					chatComponent.addMessage(new TranslatableComponent("debug.profiling.help"));
					chatComponent.addMessage(new TranslatableComponent("debug.creative_spectator.help"));
					chatComponent.addMessage(new TranslatableComponent("debug.pause_focus.help"));
					chatComponent.addMessage(new TranslatableComponent("debug.help.help"));
					chatComponent.addMessage(new TranslatableComponent("debug.reload_resourcepacks.help"));
					chatComponent.addMessage(new TranslatableComponent("debug.pause.help"));
					chatComponent.addMessage(new TranslatableComponent("debug.gamemodes.help"));
					return true;
				case 84:
					this.debugFeedbackTranslated("debug.reload_resourcepacks.message");
					this.minecraft.reloadResourcePacks();
					return true;
				case 293:
					if (!this.minecraft.player.hasPermissions(2)) {
						this.debugFeedbackTranslated("debug.gamemodes.error");
					} else {
						this.minecraft.setScreen(new GameModeSwitcherScreen());
					}

					return true;
				default:
					return false;
			}
		}
	}

	private void copyRecreateCommand(boolean bl, boolean bl2) {
		HitResult hitResult = this.minecraft.hitResult;
		if (hitResult != null) {
			switch (hitResult.getType()) {
				case BLOCK:
					BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
					BlockState blockState = this.minecraft.player.level.getBlockState(blockPos);
					if (bl) {
						if (bl2) {
							this.minecraft.player.connection.getDebugQueryHandler().queryBlockEntityTag(blockPos, compoundTagx -> {
								this.copyCreateBlockCommand(blockState, blockPos, compoundTagx);
								this.debugFeedbackTranslated("debug.inspect.server.block");
							});
						} else {
							BlockEntity blockEntity = this.minecraft.player.level.getBlockEntity(blockPos);
							CompoundTag compoundTag = blockEntity != null ? blockEntity.save(new CompoundTag()) : null;
							this.copyCreateBlockCommand(blockState, blockPos, compoundTag);
							this.debugFeedbackTranslated("debug.inspect.client.block");
						}
					} else {
						this.copyCreateBlockCommand(blockState, blockPos, null);
						this.debugFeedbackTranslated("debug.inspect.client.block");
					}
					break;
				case ENTITY:
					Entity entity = ((EntityHitResult)hitResult).getEntity();
					ResourceLocation resourceLocation = Registry.ENTITY_TYPE.getKey(entity.getType());
					if (bl) {
						if (bl2) {
							this.minecraft.player.connection.getDebugQueryHandler().queryEntityTag(entity.getId(), compoundTagx -> {
								this.copyCreateEntityCommand(resourceLocation, entity.position(), compoundTagx);
								this.debugFeedbackTranslated("debug.inspect.server.entity");
							});
						} else {
							CompoundTag compoundTag2 = entity.saveWithoutId(new CompoundTag());
							this.copyCreateEntityCommand(resourceLocation, entity.position(), compoundTag2);
							this.debugFeedbackTranslated("debug.inspect.client.entity");
						}
					} else {
						this.copyCreateEntityCommand(resourceLocation, entity.position(), null);
						this.debugFeedbackTranslated("debug.inspect.client.entity");
					}
			}
		}
	}

	private void copyCreateBlockCommand(BlockState blockState, BlockPos blockPos, @Nullable CompoundTag compoundTag) {
		if (compoundTag != null) {
			compoundTag.remove("x");
			compoundTag.remove("y");
			compoundTag.remove("z");
			compoundTag.remove("id");
		}

		StringBuilder stringBuilder = new StringBuilder(BlockStateParser.serialize(blockState));
		if (compoundTag != null) {
			stringBuilder.append(compoundTag);
		}

		String string = String.format(Locale.ROOT, "/setblock %d %d %d %s", blockPos.getX(), blockPos.getY(), blockPos.getZ(), stringBuilder);
		this.setClipboard(string);
	}

	private void copyCreateEntityCommand(ResourceLocation resourceLocation, Vec3 vec3, @Nullable CompoundTag compoundTag) {
		String string2;
		if (compoundTag != null) {
			compoundTag.remove("UUID");
			compoundTag.remove("Pos");
			compoundTag.remove("Dimension");
			String string = NbtUtils.toPrettyComponent(compoundTag).getString();
			string2 = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f %s", resourceLocation.toString(), vec3.x, vec3.y, vec3.z, string);
		} else {
			string2 = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f", resourceLocation.toString(), vec3.x, vec3.y, vec3.z);
		}

		this.setClipboard(string2);
	}

	public void keyPress(long l, int i, int j, int k, int m) {
		if (l == this.minecraft.getWindow().getWindow()) {
			if (this.debugCrashKeyTime > 0L) {
				if (!InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 67)
					|| !InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292)) {
					this.debugCrashKeyTime = -1L;
				}
			} else if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 67)
				&& InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292)) {
				this.handledDebugKey = true;
				this.debugCrashKeyTime = Util.getMillis();
				this.debugCrashKeyReportedTime = Util.getMillis();
				this.debugCrashKeyReportedCount = 0L;
			}

			Screen screen = this.minecraft.screen;
			if (k == 1 && (!(this.minecraft.screen instanceof ControlsScreen) || ((ControlsScreen)screen).lastKeySelection <= Util.getMillis() - 20L)) {
				if (this.minecraft.options.keyFullscreen.matches(i, j)) {
					this.minecraft.getWindow().toggleFullScreen();
					this.minecraft.options.fullscreen = this.minecraft.getWindow().isFullscreen();
					this.minecraft.options.save();
					return;
				}

				if (this.minecraft.options.keyScreenshot.matches(i, j)) {
					if (Screen.hasControlDown()) {
					}

					Screenshot.grab(
						this.minecraft.gameDirectory,
						this.minecraft.getMainRenderTarget(),
						component -> this.minecraft.execute(() -> this.minecraft.gui.getChat().addMessage(component))
					);
					return;
				}
			}

			if (NarratorChatListener.INSTANCE.isActive()) {
				boolean bl = screen == null || !(screen.getFocused() instanceof EditBox) || !((EditBox)screen.getFocused()).canConsumeInput();
				if (k != 0 && i == 66 && Screen.hasControlDown() && bl) {
					boolean bl2 = this.minecraft.options.narratorStatus == NarratorStatus.OFF;
					this.minecraft.options.narratorStatus = NarratorStatus.byId(this.minecraft.options.narratorStatus.getId() + 1);
					NarratorChatListener.INSTANCE.updateNarratorStatus(this.minecraft.options.narratorStatus);
					if (screen instanceof SimpleOptionsSubScreen) {
						((SimpleOptionsSubScreen)screen).updateNarratorButton();
					}

					if (bl2 && screen != null) {
						screen.narrationEnabled();
					}
				}
			}

			if (screen != null) {
				boolean[] bls = new boolean[]{false};
				Screen.wrapScreenError(() -> {
					if (k != 1 && (k != 2 || !this.sendRepeatsToGui)) {
						if (k == 0) {
							bls[0] = screen.keyReleased(i, j, m);
						}
					} else {
						screen.afterKeyboardAction();
						bls[0] = screen.keyPressed(i, j, m);
					}
				}, "keyPressed event handler", screen.getClass().getCanonicalName());
				if (bls[0]) {
					return;
				}
			}

			if (this.minecraft.screen == null || this.minecraft.screen.passEvents) {
				InputConstants.Key key = InputConstants.getKey(i, j);
				if (k == 0) {
					KeyMapping.set(key, false);
					if (i == 292) {
						if (this.handledDebugKey) {
							this.handledDebugKey = false;
						} else {
							this.minecraft.options.renderDebug = !this.minecraft.options.renderDebug;
							this.minecraft.options.renderDebugCharts = this.minecraft.options.renderDebug && Screen.hasShiftDown();
							this.minecraft.options.renderFpsChart = this.minecraft.options.renderDebug && Screen.hasAltDown();
						}
					}
				} else {
					if (i == 293 && this.minecraft.gameRenderer != null) {
						this.minecraft.gameRenderer.togglePostEffect();
					}

					boolean bl2x = false;
					if (this.minecraft.screen == null) {
						if (i == 256) {
							boolean bl3 = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292);
							this.minecraft.pauseGame(bl3);
						}

						bl2x = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292) && this.handleDebugKeys(i);
						this.handledDebugKey |= bl2x;
						if (i == 290) {
							this.minecraft.options.hideGui = !this.minecraft.options.hideGui;
						}
					}

					if (bl2x) {
						KeyMapping.set(key, false);
					} else {
						KeyMapping.set(key, true);
						KeyMapping.click(key);
					}

					if (this.minecraft.options.renderDebugCharts && i >= 48 && i <= 57) {
						this.minecraft.debugFpsMeterKeyPress(i - 48);
					}
				}
			}
		}
	}

	private void charTyped(long l, int i, int j) {
		if (l == this.minecraft.getWindow().getWindow()) {
			GuiEventListener guiEventListener = this.minecraft.screen;
			if (guiEventListener != null && this.minecraft.getOverlay() == null) {
				if (Character.charCount(i) == 1) {
					Screen.wrapScreenError(() -> guiEventListener.charTyped((char)i, j), "charTyped event handler", guiEventListener.getClass().getCanonicalName());
				} else {
					for (char c : Character.toChars(i)) {
						Screen.wrapScreenError(() -> guiEventListener.charTyped(c, j), "charTyped event handler", guiEventListener.getClass().getCanonicalName());
					}
				}
			}
		}
	}

	public void setSendRepeatsToGui(boolean bl) {
		this.sendRepeatsToGui = bl;
	}

	public void setup(long l) {
		InputConstants.setupKeyboardCallbacks(
			l, (lx, i, j, k, m) -> this.minecraft.execute(() -> this.keyPress(lx, i, j, k, m)), (lx, i, j) -> this.minecraft.execute(() -> this.charTyped(lx, i, j))
		);
	}

	public String getClipboard() {
		return this.clipboardManager.getClipboard(this.minecraft.getWindow().getWindow(), (i, l) -> {
			if (i != 65545) {
				this.minecraft.getWindow().defaultErrorCallback(i, l);
			}
		});
	}

	public void setClipboard(String string) {
		if (!string.isEmpty()) {
			this.clipboardManager.setClipboard(this.minecraft.getWindow().getWindow(), string);
		}
	}

	public void tick() {
		if (this.debugCrashKeyTime > 0L) {
			long l = Util.getMillis();
			long m = 10000L - (l - this.debugCrashKeyTime);
			long n = l - this.debugCrashKeyReportedTime;
			if (m < 0L) {
				if (Screen.hasControlDown()) {
					Blaze3D.youJustLostTheGame();
				}

				throw new ReportedException(new CrashReport("Manually triggered debug crash", new Throwable()));
			}

			if (n >= 1000L) {
				if (this.debugCrashKeyReportedCount == 0L) {
					this.debugFeedbackTranslated("debug.crash.message");
				} else {
					this.debugWarningTranslated("debug.crash.warning", Mth.ceil((float)m / 1000.0F));
				}

				this.debugCrashKeyReportedTime = l;
				this.debugCrashKeyReportedCount++;
			}
		}
	}
}
