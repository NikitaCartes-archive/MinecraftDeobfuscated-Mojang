package com.mojang.blaze3d.platform;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWVidMode.Buffer;

@Environment(EnvType.CLIENT)
public final class VideoMode {
	private final int width;
	private final int height;
	private final int redBits;
	private final int greenBits;
	private final int blueBits;
	private final int refreshRate;
	private static final Pattern PATTERN = Pattern.compile("(\\d+)x(\\d+)(?:@(\\d+)(?::(\\d+))?)?");

	public VideoMode(int i, int j, int k, int l, int m, int n) {
		this.width = i;
		this.height = j;
		this.redBits = k;
		this.greenBits = l;
		this.blueBits = m;
		this.refreshRate = n;
	}

	public VideoMode(Buffer buffer) {
		this.width = buffer.width();
		this.height = buffer.height();
		this.redBits = buffer.redBits();
		this.greenBits = buffer.greenBits();
		this.blueBits = buffer.blueBits();
		this.refreshRate = buffer.refreshRate();
	}

	public VideoMode(GLFWVidMode gLFWVidMode) {
		this.width = gLFWVidMode.width();
		this.height = gLFWVidMode.height();
		this.redBits = gLFWVidMode.redBits();
		this.greenBits = gLFWVidMode.greenBits();
		this.blueBits = gLFWVidMode.blueBits();
		this.refreshRate = gLFWVidMode.refreshRate();
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public int getRedBits() {
		return this.redBits;
	}

	public int getGreenBits() {
		return this.greenBits;
	}

	public int getBlueBits() {
		return this.blueBits;
	}

	public int getRefreshRate() {
		return this.refreshRate;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			VideoMode videoMode = (VideoMode)object;
			return this.width == videoMode.width
				&& this.height == videoMode.height
				&& this.redBits == videoMode.redBits
				&& this.greenBits == videoMode.greenBits
				&& this.blueBits == videoMode.blueBits
				&& this.refreshRate == videoMode.refreshRate;
		} else {
			return false;
		}
	}

	public int hashCode() {
		return Objects.hash(new Object[]{this.width, this.height, this.redBits, this.greenBits, this.blueBits, this.refreshRate});
	}

	public String toString() {
		return String.format(Locale.ROOT, "%sx%s@%s (%sbit)", this.width, this.height, this.refreshRate, this.redBits + this.greenBits + this.blueBits);
	}

	public static Optional<VideoMode> read(@Nullable String string) {
		if (string == null) {
			return Optional.empty();
		} else {
			try {
				Matcher matcher = PATTERN.matcher(string);
				if (matcher.matches()) {
					int i = Integer.parseInt(matcher.group(1));
					int j = Integer.parseInt(matcher.group(2));
					String string2 = matcher.group(3);
					int k;
					if (string2 == null) {
						k = 60;
					} else {
						k = Integer.parseInt(string2);
					}

					String string3 = matcher.group(4);
					int l;
					if (string3 == null) {
						l = 24;
					} else {
						l = Integer.parseInt(string3);
					}

					int m = l / 3;
					return Optional.of(new VideoMode(i, j, m, m, m, k));
				}
			} catch (Exception var9) {
			}

			return Optional.empty();
		}
	}

	public String write() {
		return String.format(Locale.ROOT, "%sx%s@%s:%s", this.width, this.height, this.refreshRate, this.redBits + this.greenBits + this.blueBits);
	}
}
