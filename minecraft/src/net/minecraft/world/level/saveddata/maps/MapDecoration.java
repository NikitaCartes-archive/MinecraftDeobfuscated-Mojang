package net.minecraft.world.level.saveddata.maps;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.material.MapColor;

public record MapDecoration(MapDecoration.Type type, byte x, byte y, byte rot, @Nullable Component name) {
	public byte getImage() {
		return this.type.getIcon();
	}

	public boolean renderOnFrame() {
		return this.type.isRenderedOnFrame();
	}

	public static enum Type implements StringRepresentable {
		PLAYER("player", false, true),
		FRAME("frame", true, true),
		RED_MARKER("red_marker", false, true),
		BLUE_MARKER("blue_marker", false, true),
		TARGET_X("target_x", true, false),
		TARGET_POINT("target_point", true, false),
		PLAYER_OFF_MAP("player_off_map", false, true),
		PLAYER_OFF_LIMITS("player_off_limits", false, true),
		MANSION("mansion", true, 5393476, false, true),
		MONUMENT("monument", true, 3830373, false, true),
		BANNER_WHITE("banner_white", true, true),
		BANNER_ORANGE("banner_orange", true, true),
		BANNER_MAGENTA("banner_magenta", true, true),
		BANNER_LIGHT_BLUE("banner_light_blue", true, true),
		BANNER_YELLOW("banner_yellow", true, true),
		BANNER_LIME("banner_lime", true, true),
		BANNER_PINK("banner_pink", true, true),
		BANNER_GRAY("banner_gray", true, true),
		BANNER_LIGHT_GRAY("banner_light_gray", true, true),
		BANNER_CYAN("banner_cyan", true, true),
		BANNER_PURPLE("banner_purple", true, true),
		BANNER_BLUE("banner_blue", true, true),
		BANNER_BROWN("banner_brown", true, true),
		BANNER_GREEN("banner_green", true, true),
		BANNER_RED("banner_red", true, true),
		BANNER_BLACK("banner_black", true, true),
		RED_X("red_x", true, false),
		DESERT_VILLAGE("village_desert", true, MapColor.COLOR_LIGHT_GRAY.col, false, true),
		PLAINS_VILLAGE("village_plains", true, MapColor.COLOR_LIGHT_GRAY.col, false, true),
		SAVANNA_VILLAGE("village_savanna", true, MapColor.COLOR_LIGHT_GRAY.col, false, true),
		SNOWY_VILLAGE("village_snowy", true, MapColor.COLOR_LIGHT_GRAY.col, false, true),
		TAIGA_VILLAGE("village_taiga", true, MapColor.COLOR_LIGHT_GRAY.col, false, true),
		JUNGLE_TEMPLE("jungle_temple", true, MapColor.COLOR_LIGHT_GRAY.col, false, true),
		SWAMP_HUT("swamp_hut", true, MapColor.COLOR_LIGHT_GRAY.col, false, true);

		public static final Codec<MapDecoration.Type> CODEC = StringRepresentable.fromEnum(MapDecoration.Type::values);
		private final String name;
		private final byte icon;
		private final boolean renderedOnFrame;
		private final int mapColor;
		private final boolean isExplorationMapElement;
		private final boolean trackCount;

		private Type(String string2, boolean bl, boolean bl2) {
			this(string2, bl, -1, bl2, false);
		}

		private Type(String string2, boolean bl, int j, boolean bl2, boolean bl3) {
			this.name = string2;
			this.trackCount = bl2;
			this.icon = (byte)this.ordinal();
			this.renderedOnFrame = bl;
			this.mapColor = j;
			this.isExplorationMapElement = bl3;
		}

		public byte getIcon() {
			return this.icon;
		}

		public boolean isExplorationMapElement() {
			return this.isExplorationMapElement;
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

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
