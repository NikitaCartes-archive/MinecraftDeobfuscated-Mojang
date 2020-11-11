package net.minecraft.world.level.saveddata.maps;

import java.util.Objects;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

	@Environment(EnvType.CLIENT)
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

	@Environment(EnvType.CLIENT)
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
		} else if (!(object instanceof MapDecoration)) {
			return false;
		} else {
			MapDecoration mapDecoration = (MapDecoration)object;
			return this.type == mapDecoration.type
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
		PLAYER(false),
		FRAME(true),
		RED_MARKER(false),
		BLUE_MARKER(false),
		TARGET_X(true),
		TARGET_POINT(true),
		PLAYER_OFF_MAP(false),
		PLAYER_OFF_LIMITS(false),
		MANSION(true, 5393476),
		MONUMENT(true, 3830373),
		BANNER_WHITE(true),
		BANNER_ORANGE(true),
		BANNER_MAGENTA(true),
		BANNER_LIGHT_BLUE(true),
		BANNER_YELLOW(true),
		BANNER_LIME(true),
		BANNER_PINK(true),
		BANNER_GRAY(true),
		BANNER_LIGHT_GRAY(true),
		BANNER_CYAN(true),
		BANNER_PURPLE(true),
		BANNER_BLUE(true),
		BANNER_BROWN(true),
		BANNER_GREEN(true),
		BANNER_RED(true),
		BANNER_BLACK(true),
		RED_X(true);

		private final byte icon = (byte)this.ordinal();
		private final boolean renderedOnFrame;
		private final int mapColor;

		private Type(boolean bl) {
			this(bl, -1);
		}

		private Type(boolean bl, int j) {
			this.renderedOnFrame = bl;
			this.mapColor = j;
		}

		public byte getIcon() {
			return this.icon;
		}

		@Environment(EnvType.CLIENT)
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
	}
}
