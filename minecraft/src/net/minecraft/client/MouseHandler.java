package net.minecraft.client;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraft.util.SmoothDouble;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFWDropCallback;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class MouseHandler {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Minecraft minecraft;
	private boolean isLeftPressed;
	private boolean isMiddlePressed;
	private boolean isRightPressed;
	private double xpos;
	private double ypos;
	private int fakeRightMouse;
	private int activeButton = -1;
	private boolean ignoreFirstMove = true;
	private int clickDepth;
	private double mousePressedTime;
	private final SmoothDouble smoothTurnX = new SmoothDouble();
	private final SmoothDouble smoothTurnY = new SmoothDouble();
	private double accumulatedDX;
	private double accumulatedDY;
	private final ScrollWheelHandler scrollWheelHandler;
	private double lastHandleMovementTime = Double.MIN_VALUE;
	private boolean mouseGrabbed;

	public MouseHandler(Minecraft minecraft) {
		this.minecraft = minecraft;
		this.scrollWheelHandler = new ScrollWheelHandler();
	}

	private void onPress(long l, int i, int j, int k) {
		if (l == this.minecraft.getWindow().getWindow()) {
			this.minecraft.getFramerateLimitTracker().onInputReceived();
			if (this.minecraft.screen != null) {
				this.minecraft.setLastInputType(InputType.MOUSE);
			}

			boolean bl = j == 1;
			if (Minecraft.ON_OSX && i == 0) {
				if (bl) {
					if ((k & 2) == 2) {
						i = 1;
						this.fakeRightMouse++;
					}
				} else if (this.fakeRightMouse > 0) {
					i = 1;
					this.fakeRightMouse--;
				}
			}

			int m = i;
			if (bl) {
				if (this.minecraft.options.touchscreen().get() && this.clickDepth++ > 0) {
					return;
				}

				this.activeButton = i;
				this.mousePressedTime = Blaze3D.getTime();
			} else if (this.activeButton != -1) {
				if (this.minecraft.options.touchscreen().get() && --this.clickDepth > 0) {
					return;
				}

				this.activeButton = -1;
			}

			if (this.minecraft.getOverlay() == null) {
				if (this.minecraft.screen == null) {
					if (!this.mouseGrabbed && bl) {
						this.grabMouse();
					}
				} else {
					double d = this.xpos * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
					double e = this.ypos * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();
					Screen screen = this.minecraft.screen;
					if (bl) {
						screen.afterMouseAction();

						try {
							if (screen.mouseClicked(d, e, m)) {
								return;
							}
						} catch (Throwable var17) {
							CrashReport crashReport = CrashReport.forThrowable(var17, "mouseClicked event handler");
							screen.fillCrashDetails(crashReport);
							CrashReportCategory crashReportCategory = crashReport.addCategory("Mouse");
							crashReportCategory.setDetail("Scaled X", d);
							crashReportCategory.setDetail("Scaled Y", e);
							crashReportCategory.setDetail("Button", i);
							throw new ReportedException(crashReport);
						}
					} else {
						try {
							if (screen.mouseReleased(d, e, m)) {
								return;
							}
						} catch (Throwable var16) {
							CrashReport crashReport = CrashReport.forThrowable(var16, "mouseReleased event handler");
							screen.fillCrashDetails(crashReport);
							CrashReportCategory crashReportCategory = crashReport.addCategory("Mouse");
							crashReportCategory.setDetail("Scaled X", d);
							crashReportCategory.setDetail("Scaled Y", e);
							crashReportCategory.setDetail("Button", i);
							throw new ReportedException(crashReport);
						}
					}
				}
			}

			if (this.minecraft.screen == null && this.minecraft.getOverlay() == null) {
				if (i == 0) {
					this.isLeftPressed = bl;
				} else if (i == 2) {
					this.isMiddlePressed = bl;
				} else if (i == 1) {
					this.isRightPressed = bl;
				}

				KeyMapping.set(InputConstants.Type.MOUSE.getOrCreate(i), bl);
				if (bl) {
					if (this.minecraft.player.isSpectator() && i == 2) {
						this.minecraft.gui.getSpectatorGui().onMouseMiddleClick();
					} else {
						KeyMapping.click(InputConstants.Type.MOUSE.getOrCreate(i));
					}
				}
			}
		}
	}

	private void onScroll(long l, double d, double e) {
		if (l == Minecraft.getInstance().getWindow().getWindow()) {
			this.minecraft.getFramerateLimitTracker().onInputReceived();
			boolean bl = this.minecraft.options.discreteMouseScroll().get();
			double f = this.minecraft.options.mouseWheelSensitivity().get();
			double g = (bl ? Math.signum(d) : d) * f;
			double h = (bl ? Math.signum(e) : e) * f;
			if (this.minecraft.getOverlay() == null) {
				if (this.minecraft.screen != null) {
					double i = this.xpos * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
					double j = this.ypos * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();
					this.minecraft.screen.mouseScrolled(i, j, g, h);
					this.minecraft.screen.afterMouseAction();
				} else if (this.minecraft.player != null) {
					Vector2i vector2i = this.scrollWheelHandler.onMouseScroll(g, h);
					if (vector2i.x == 0 && vector2i.y == 0) {
						return;
					}

					int k = vector2i.y == 0 ? -vector2i.x : vector2i.y;
					if (this.minecraft.player.isSpectator()) {
						if (this.minecraft.gui.getSpectatorGui().isMenuActive()) {
							this.minecraft.gui.getSpectatorGui().onMouseScrolled(-k);
						} else {
							float m = Mth.clamp(this.minecraft.player.getAbilities().getFlyingSpeed() + (float)vector2i.y * 0.005F, 0.0F, 0.2F);
							this.minecraft.player.getAbilities().setFlyingSpeed(m);
						}
					} else {
						Inventory inventory = this.minecraft.player.getInventory();
						inventory.setSelectedHotbarSlot(ScrollWheelHandler.getNextScrollWheelSelection((double)k, inventory.selected, Inventory.getSelectionSize()));
					}
				}
			}
		}
	}

	private void onDrop(long l, List<Path> list, int i) {
		this.minecraft.getFramerateLimitTracker().onInputReceived();
		if (this.minecraft.screen != null) {
			this.minecraft.screen.onFilesDrop(list);
		}

		if (i > 0) {
			SystemToast.onFileDropFailure(this.minecraft, i);
		}
	}

	public void setup(long l) {
		InputConstants.setupMouseCallbacks(
			l,
			(lx, d, e) -> this.minecraft.execute(() -> this.onMove(lx, d, e)),
			(lx, i, j, k) -> this.minecraft.execute(() -> this.onPress(lx, i, j, k)),
			(lx, d, e) -> this.minecraft.execute(() -> this.onScroll(lx, d, e)),
			(lx, i, m) -> {
				List<Path> list = new ArrayList(i);
				int j = 0;

				for (int k = 0; k < i; k++) {
					String string = GLFWDropCallback.getName(m, k);

					try {
						list.add(Paths.get(string));
					} catch (InvalidPathException var11) {
						j++;
						LOGGER.error("Failed to parse path '{}'", string, var11);
					}
				}

				if (!list.isEmpty()) {
					int k = j;
					this.minecraft.execute(() -> this.onDrop(lx, list, k));
				}
			}
		);
	}

	private void onMove(long l, double d, double e) {
		if (l == Minecraft.getInstance().getWindow().getWindow()) {
			if (this.ignoreFirstMove) {
				this.xpos = d;
				this.ypos = e;
				this.ignoreFirstMove = false;
			} else {
				if (this.minecraft.isWindowActive()) {
					this.accumulatedDX = this.accumulatedDX + (d - this.xpos);
					this.accumulatedDY = this.accumulatedDY + (e - this.ypos);
				}

				this.xpos = d;
				this.ypos = e;
			}
		}
	}

	public void handleAccumulatedMovement() {
		double d = Blaze3D.getTime();
		double e = d - this.lastHandleMovementTime;
		this.lastHandleMovementTime = d;
		if (this.minecraft.isWindowActive()) {
			Screen screen = this.minecraft.screen;
			boolean bl = this.accumulatedDX != 0.0 || this.accumulatedDY != 0.0;
			if (bl) {
				this.minecraft.getFramerateLimitTracker().onInputReceived();
			}

			if (screen != null && this.minecraft.getOverlay() == null && bl) {
				double f = this.xpos * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
				double g = this.ypos * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();

				try {
					screen.mouseMoved(f, g);
				} catch (Throwable var19) {
					CrashReport crashReport = CrashReport.forThrowable(var19, "mouseMoved event handler");
					screen.fillCrashDetails(crashReport);
					CrashReportCategory crashReportCategory = crashReport.addCategory("Mouse");
					crashReportCategory.setDetail("Scaled X", f);
					crashReportCategory.setDetail("Scaled Y", g);
					throw new ReportedException(crashReport);
				}

				if (this.activeButton != -1 && this.mousePressedTime > 0.0) {
					double h = this.accumulatedDX * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
					double i = this.accumulatedDY * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();

					try {
						screen.mouseDragged(f, g, this.activeButton, h, i);
					} catch (Throwable var18) {
						CrashReport crashReport2 = CrashReport.forThrowable(var18, "mouseDragged event handler");
						screen.fillCrashDetails(crashReport2);
						CrashReportCategory crashReportCategory2 = crashReport2.addCategory("Mouse");
						crashReportCategory2.setDetail("Scaled X", f);
						crashReportCategory2.setDetail("Scaled Y", g);
						throw new ReportedException(crashReport2);
					}
				}

				screen.afterMouseMove();
			}

			if (this.isMouseGrabbed() && this.minecraft.player != null) {
				this.turnPlayer(e);
			}
		}

		this.accumulatedDX = 0.0;
		this.accumulatedDY = 0.0;
	}

	private void turnPlayer(double d) {
		double e = this.minecraft.options.sensitivity().get() * 0.6F + 0.2F;
		double f = e * e * e;
		double g = f * 8.0;
		double j;
		double k;
		if (this.minecraft.options.smoothCamera) {
			double h = this.smoothTurnX.getNewDeltaValue(this.accumulatedDX * g, d * g);
			double i = this.smoothTurnY.getNewDeltaValue(this.accumulatedDY * g, d * g);
			j = h;
			k = i;
		} else if (this.minecraft.options.getCameraType().isFirstPerson() && this.minecraft.player.isScoping()) {
			this.smoothTurnX.reset();
			this.smoothTurnY.reset();
			j = this.accumulatedDX * f;
			k = this.accumulatedDY * f;
		} else {
			this.smoothTurnX.reset();
			this.smoothTurnY.reset();
			j = this.accumulatedDX * g;
			k = this.accumulatedDY * g;
		}

		int l = 1;
		if (this.minecraft.options.invertYMouse().get()) {
			l = -1;
		}

		this.minecraft.getTutorial().onMouse(j, k);
		if (this.minecraft.player != null) {
			this.minecraft.player.turn(j, k * (double)l);
		}
	}

	public boolean isLeftPressed() {
		return this.isLeftPressed;
	}

	public boolean isMiddlePressed() {
		return this.isMiddlePressed;
	}

	public boolean isRightPressed() {
		return this.isRightPressed;
	}

	public double xpos() {
		return this.xpos;
	}

	public double ypos() {
		return this.ypos;
	}

	public void setIgnoreFirstMove() {
		this.ignoreFirstMove = true;
	}

	public boolean isMouseGrabbed() {
		return this.mouseGrabbed;
	}

	public void grabMouse() {
		if (this.minecraft.isWindowActive()) {
			if (!this.mouseGrabbed) {
				if (!Minecraft.ON_OSX) {
					KeyMapping.setAll();
				}

				this.mouseGrabbed = true;
				this.xpos = (double)(this.minecraft.getWindow().getScreenWidth() / 2);
				this.ypos = (double)(this.minecraft.getWindow().getScreenHeight() / 2);
				InputConstants.grabOrReleaseMouse(this.minecraft.getWindow().getWindow(), 212995, this.xpos, this.ypos);
				this.minecraft.setScreen(null);
				this.minecraft.missTime = 10000;
				this.ignoreFirstMove = true;
			}
		}
	}

	public void releaseMouse() {
		if (this.mouseGrabbed) {
			this.mouseGrabbed = false;
			this.xpos = (double)(this.minecraft.getWindow().getScreenWidth() / 2);
			this.ypos = (double)(this.minecraft.getWindow().getScreenHeight() / 2);
			InputConstants.grabOrReleaseMouse(this.minecraft.getWindow().getWindow(), 212993, this.xpos, this.ypos);
		}
	}

	public void cursorEntered() {
		this.ignoreFirstMove = true;
	}
}
