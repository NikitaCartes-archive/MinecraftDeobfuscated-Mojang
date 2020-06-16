package net.minecraft.client;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.InputConstants;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.events.GuiEventListener;
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
	private double accumulatedScroll;
	private double lastMouseEventTime = Double.MIN_VALUE;
	private boolean mouseGrabbed;

	public MouseHandler(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	private void onPress(long l, int i, int j, int k) {
		if (l == this.minecraft.getWindow().getWindow()) {
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
				if (this.minecraft.options.touchscreen && this.clickDepth++ > 0) {
					return;
				}

				this.activeButton = m;
				this.mousePressedTime = Blaze3D.getTime();
			} else if (this.activeButton != -1) {
				if (this.minecraft.options.touchscreen && --this.clickDepth > 0) {
					return;
				}

				this.activeButton = -1;
			}

			boolean[] bls = new boolean[]{false};
			if (this.minecraft.overlay == null) {
				if (this.minecraft.screen == null) {
					if (!this.mouseGrabbed && bl) {
						this.grabMouse();
					}
				} else {
					double d = this.xpos * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
					double e = this.ypos * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();
					if (bl) {
						Screen.wrapScreenError(
							() -> bls[0] = this.minecraft.screen.mouseClicked(d, e, m), "mouseClicked event handler", this.minecraft.screen.getClass().getCanonicalName()
						);
					} else {
						Screen.wrapScreenError(
							() -> bls[0] = this.minecraft.screen.mouseReleased(d, e, m), "mouseReleased event handler", this.minecraft.screen.getClass().getCanonicalName()
						);
					}
				}
			}

			if (!bls[0] && (this.minecraft.screen == null || this.minecraft.screen.passEvents) && this.minecraft.overlay == null) {
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
			double f = (this.minecraft.options.discreteMouseScroll ? Math.signum(e) : e) * this.minecraft.options.mouseWheelSensitivity;
			if (this.minecraft.overlay == null) {
				if (this.minecraft.screen != null) {
					double g = this.xpos * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
					double h = this.ypos * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();
					this.minecraft.screen.mouseScrolled(g, h, f);
				} else if (this.minecraft.player != null) {
					if (this.accumulatedScroll != 0.0 && Math.signum(f) != Math.signum(this.accumulatedScroll)) {
						this.accumulatedScroll = 0.0;
					}

					this.accumulatedScroll += f;
					float i = (float)((int)this.accumulatedScroll);
					if (i == 0.0F) {
						return;
					}

					this.accumulatedScroll -= (double)i;
					if (this.minecraft.player.isSpectator()) {
						if (this.minecraft.gui.getSpectatorGui().isMenuActive()) {
							this.minecraft.gui.getSpectatorGui().onMouseScrolled((double)(-i));
						} else {
							float j = Mth.clamp(this.minecraft.player.abilities.getFlyingSpeed() + i * 0.005F, 0.0F, 0.2F);
							this.minecraft.player.abilities.setFlyingSpeed(j);
						}
					} else {
						this.minecraft.player.inventory.swapPaint((double)i);
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
			}

			GuiEventListener guiEventListener = this.minecraft.screen;
			if (guiEventListener != null && this.minecraft.overlay == null) {
				double f = d * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
				double g = e * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();
				Screen.wrapScreenError(() -> guiEventListener.mouseMoved(f, g), "mouseMoved event handler", guiEventListener.getClass().getCanonicalName());
				if (this.activeButton != -1 && this.mousePressedTime > 0.0) {
					double h = (d - this.xpos) * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
					double i = (e - this.ypos) * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();
					Screen.wrapScreenError(
						() -> guiEventListener.mouseDragged(f, g, this.activeButton, h, i), "mouseDragged event handler", guiEventListener.getClass().getCanonicalName()
					);
				}
			}

			this.minecraft.getProfiler().push("mouse");
			if (this.isMouseGrabbed() && this.minecraft.isWindowActive()) {
				this.accumulatedDX = this.accumulatedDX + (d - this.xpos);
				this.accumulatedDY = this.accumulatedDY + (e - this.ypos);
			}

			this.turnPlayer();
			this.xpos = d;
			this.ypos = e;
			this.minecraft.getProfiler().pop();
		}
	}

	public void turnPlayer() {
		double d = Blaze3D.getTime();
		double e = d - this.lastMouseEventTime;
		this.lastMouseEventTime = d;
		if (this.isMouseGrabbed() && this.minecraft.isWindowActive()) {
			double f = this.minecraft.options.sensitivity * 0.6F + 0.2F;
			double g = f * f * f * 8.0;
			double j;
			double k;
			if (this.minecraft.options.smoothCamera) {
				double h = this.smoothTurnX.getNewDeltaValue(this.accumulatedDX * g, e * g);
				double i = this.smoothTurnY.getNewDeltaValue(this.accumulatedDY * g, e * g);
				j = h;
				k = i;
			} else {
				this.smoothTurnX.reset();
				this.smoothTurnY.reset();
				j = this.accumulatedDX * g;
				k = this.accumulatedDY * g;
			}

			this.accumulatedDX = 0.0;
			this.accumulatedDY = 0.0;
			int l = 1;
			if (this.minecraft.options.invertYMouse) {
				l = -1;
			}

			this.minecraft.getTutorial().onMouse(j, k);
			if (this.minecraft.player != null) {
				this.minecraft.player.turn(j, k * (double)l);
			}
		} else {
			this.accumulatedDX = 0.0;
			this.accumulatedDY = 0.0;
		}
	}

	public boolean isLeftPressed() {
		return this.isLeftPressed;
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
