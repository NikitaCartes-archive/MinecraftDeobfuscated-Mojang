package net.minecraft.world.level.saveddata.maps;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class MapDecoration {
	private final MapDecoration.Type type;
	private final byte x;
	private final byte y;
	private final byte rot;
	@Nullable
	private final Component name;

	public MapDecoration(MapDecoration.Type type, byte b, byte c, byte d, @Nullable Component component) {
		this.type = type;
		this.x = b;
		this.y = c;
		this.rot = d;
		this.name = component;
	}

	public byte getImage() {
		return this.type.getIcon();
	}

	public MapDecoration.Type getType() {
		return this.type;
	}

	public byte getX() {
		return this.x;
	}

	public byte getY() {
		return this.y;
	}

	public byte getRot() {
		return this.rot;
	}

	public boolean renderOnFrame() {
		return this.type.isRenderedOnFrame();
	}

	@Nullable
	public Component getName() {
		return this.name;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			return !(object instanceof MapDecoration mapDecoration)
				? false
				: this.type == mapDecoration.type
					&& this.rot == mapDecoration.rot
					&& this.x == mapDecoration.x
					&& this.y == mapDecoration.y
					&& Objects.equals(this.name, mapDecoration.name);
		}
	}

	public int hashCode() {
		int i = this.type.getIcon();
		i = 31 * i + this.x;
		i = 31 * i + this.y;
		i = 31 * i + this.rot;
		return 31 * i + Objects.hashCode(this.name);
	}

	public static enum Type {
		PLAYER(false, true),
		FRAME(true, true),
		RED_MARKER(false, true),
		BLUE_MARKER(false, true),
		TARGET_X(true, false),
		TARGET_POINT(true, false),
		PLAYER_OFF_MAP(false, true),
		PLAYER_OFF_LIMITS(false, true),
		MANSION(true, 5393476, false),
		MONUMENT(true, 3830373, false),
		BANNER_WHITE(true, true),
		BANNER_ORANGE(true, true),
		BANNER_MAGENTA(true, true),
		BANNER_LIGHT_BLUE(true, true),
		BANNER_YELLOW(true, true),
		BANNER_LIME(true, true),
		BANNER_PINK(true, true),
		BANNER_GRAY(true, true),
		BANNER_LIGHT_GRAY(true, true),
		BANNER_CYAN(true, true),
		BANNER_PURPLE(true, true),
		BANNER_BLUE(true, true),
		BANNER_BROWN(true, true),
		BANNER_GREEN(true, true),
		BANNER_RED(true, true),
		BANNER_BLACK(true, true),
		RED_X(true, false);

		private final byte icon;
		private final boolean renderedOnFrame;
		private final int mapColor;
		private final boolean trackCount;

		private Type(boolean bl, boolean bl2) {
			this(bl, -1, bl2);
		}

		private Type(boolean bl, int j, boolean bl2) {
			this.trackCount = bl2;
			this.icon = (byte)this.ordinal();
			this.renderedOnFrame = bl;
			this.mapColor = j;
		}

		public byte getIcon() {
			return this.icon;
		}

		public boolean isRenderedOnFrame() {
			return this.renderedOnFrame;
		}

		public boolean hasMapColor() {
			return this.mapColor >= 0;
		}

		public int getMapColor() {
			return this.mapColor;
		}

		public static MapDecoration.Type byIcon(byte b) {
			return values()[Mth.clamp(b, 0, values().length - 1)];
		}

		public boolean shouldTrackCount() {
			return this.trackCount;
		}
	}
}
