package net.minecraft.client;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.InputConstants;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraft.util.SmoothDouble;
import org.lwjgl.glfw.GLFWDropCallback;

@Environment(EnvType.CLIENT)
public class MouseHandler {
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
	private double accumulatedScrollX;
	private double accumulatedScrollY;
	private double lastHandleMovementTime = Double.MIN_VALUE;
	private boolean mouseGrabbed;

	public MouseHandler(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	private void onPress(long l, int i, int j, int k) {
		if (l == this.minecraft.getWindow().getWindow()) {
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

				this.activeButton = m;
				this.mousePressedTime = Blaze3D.getTime();
			} else if (this.activeButton != -1) {
				if (this.minecraft.options.touchscreen().get() && --this.clickDepth > 0) {
					return;
				}

				this.activeButton = -1;
			}

			boolean[] bls = new boolean[]{false};
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
						Screen.wrapScreenError(() -> bls[0] = screen.mouseClicked(d, e, m), "mouseClicked event handler", screen.getClass().getCanonicalName());
					} else {
						Screen.wrapScreenError(() -> bls[0] = screen.mouseReleased(d, e, m), "mouseReleased event handler", screen.getClass().getCanonicalName());
					}
				}
			}

			if (!bls[0] && this.minecraft.screen == null && this.minecraft.getOverlay() == null) {
				if (m == 0) {
					this.isLeftPressed = bl;
				} else if (m == 2) {
					this.isMiddlePressed = bl;
				} else if (m == 1) {
					this.isRightPressed = bl;
				}

				KeyMapping.set(InputConstants.Type.MOUSE.getOrCreate(m), bl);
				if (bl) {
					if (this.minecraft.player.isSpectator() && m == 2) {
						this.minecraft.gui.getSpectatorGui().onMouseMiddleClick();
					} else {
						KeyMapping.click(InputConstants.Type.MOUSE.getOrCreate(m));
					}
				}
			}
		}
	}

	private void onScroll(long l, double d, double e) {
		if (l == Minecraft.getInstance().getWindow().getWindow()) {
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
					if (this.accumulatedScrollX != 0.0 && Math.signum(g) != Math.signum(this.accumulatedScrollX)) {
						this.accumulatedScrollX = 0.0;
					}

					if (this.accumulatedScrollY != 0.0 && Math.signum(h) != Math.signum(this.accumulatedScrollY)) {
						this.accumulatedScrollY = 0.0;
					}

					this.accumulatedScrollX += g;
					this.accumulatedScrollY += h;
					int k = (int)this.accumulatedScrollX;
					int m = (int)this.accumulatedScrollY;
					if (k == 0 && m == 0) {
						return;
					}

					this.accumulatedScrollX -= (double)k;
					this.accumulatedScrollY -= (double)m;
					int n = m == 0 ? -k : m;
					if (this.minecraft.player.isSpectator()) {
						if (this.minecraft.gui.getSpectatorGui().isMenuActive()) {
							this.minecraft.gui.getSpectatorGui().onMouseScrolled(-n);
						} else {
							float o = Mth.clamp(this.minecraft.player.getAbilities().getFlyingSpeed() + (float)m * 0.005F, 0.0F, 0.2F);
							this.minecraft.player.getAbilities().setFlyingSpeed(o);
						}
					} else {
						this.minecraft.player.getInventory().swapPaint((double)n);
					}
				}
			}
		}
	}

	private void onDrop(long l, List<Path> list) {
		if (this.minecraft.screen != null) {
			this.minecraft.screen.onFilesDrop(list);
		}
	}

	public void setup(long l) {
		InputConstants.setupMouseCallbacks(
			l,
			(lx, d, e) -> this.minecraft.execute(() -> this.onMove(lx, d, e)),
			(lx, i, j, k) -> this.minecraft.execute(() -> this.onPress(lx, i, j, k)),
			(lx, d, e) -> this.minecraft.execute(() -> this.onScroll(lx, d, e)),
			(lx, i, m) -> {
				Path[] paths = new Path[i];

				for (int j = 0; j < i; j++) {
					paths[j] = Paths.get(GLFWDropCallback.getName(m, j));
				}

				this.minecraft.execute(() -> this.onDrop(lx, Arrays.asList(paths)));
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
			if (screen != null && this.minecraft.getOverlay() == null && (this.accumulatedDX != 0.0 || this.accumulatedDY != 0.0)) {
				double f = this.xpos * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
				double g = this.ypos * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();
				Screen.wrapScreenError(() -> screen.mouseMoved(f, g), "mouseMoved event handler", screen.getClass().getCanonicalName());
				if (this.activeButton != -1 && this.mousePressedTime > 0.0) {
					double h = this.accumulatedDX * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
					double i = this.accumulatedDY * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();
					Screen.wrapScreenError(() -> screen.mouseDragged(f, g, this.activeButton, h, i), "mouseDragged event handler", screen.getClass().getCanonicalName());
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
