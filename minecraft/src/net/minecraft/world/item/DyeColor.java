package net.minecraft.world.item;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.material.MaterialColor;
import org.jetbrains.annotations.Contract;

public enum DyeColor implements StringRepresentable {
	WHITE(0, "white", 16383998, MaterialColor.SNOW, 15790320, 16777215),
	ORANGE(1, "orange", 16351261, MaterialColor.COLOR_ORANGE, 15435844, 16738335),
	MAGENTA(2, "magenta", 13061821, MaterialColor.COLOR_MAGENTA, 12801229, 16711935),
	LIGHT_BLUE(3, "light_blue", 3847130, MaterialColor.COLOR_LIGHT_BLUE, 6719955, 10141901),
	YELLOW(4, "yellow", 16701501, MaterialColor.COLOR_YELLOW, 14602026, 16776960),
	LIME(5, "lime", 8439583, MaterialColor.COLOR_LIGHT_GREEN, 4312372, 12582656),
	PINK(6, "pink", 15961002, MaterialColor.COLOR_PINK, 14188952, 16738740),
	GRAY(7, "gray", 4673362, MaterialColor.COLOR_GRAY, 4408131, 8421504),
	LIGHT_GRAY(8, "light_gray", 10329495, MaterialColor.COLOR_LIGHT_GRAY, 11250603, 13882323),
	CYAN(9, "cyan", 1481884, MaterialColor.COLOR_CYAN, 2651799, 65535),
	PURPLE(10, "purple", 8991416, MaterialColor.COLOR_PURPLE, 8073150, 10494192),
	BLUE(11, "blue", 3949738, MaterialColor.COLOR_BLUE, 2437522, 255),
	BROWN(12, "brown", 8606770, MaterialColor.COLOR_BROWN, 5320730, 9127187),
	GREEN(13, "green", 6192150, MaterialColor.COLOR_GREEN, 3887386, 65280),
	RED(14, "red", 11546150, MaterialColor.COLOR_RED, 11743532, 16711680),
	BLACK(15, "black", 1908001, MaterialColor.COLOR_BLACK, 1973019, 0);

	private static final IntFunction<DyeColor> BY_ID = ByIdMap.continuous(DyeColor::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
	public static final IntFunction<DyeColor> BY_ID_WRAP = ByIdMap.continuous(DyeColor::getId, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
	private static final Int2ObjectOpenHashMap<DyeColor> BY_FIREWORK_COLOR = new Int2ObjectOpenHashMap<>(
		(Map<? extends Integer, ? extends DyeColor>)Arrays.stream(values()).collect(Collectors.toMap(dyeColor -> dyeColor.fireworkColor, dyeColor -> dyeColor))
	);
	public static final StringRepresentable.EnumCodec<DyeColor> CODEC = StringRepresentable.fromEnum(DyeColor::values);
	private final int id;
	private final String name;
	private final MaterialColor color;
	private final float[] textureDiffuseColors;
	private final int fireworkColor;
	private final int textColor;

	private DyeColor(int j, String string2, int k, MaterialColor materialColor, int l, int m) {
		this.id = j;
		this.name = string2;
		this.color = materialColor;
		this.textColor = m;
		int n = (k & 0xFF0000) >> 16;
		int o = (k & 0xFF00) >> 8;
		int p = (k & 0xFF) >> 0;
		this.textureDiffuseColors = new float[]{(float)n / 255.0F, (float)o / 255.0F, (float)p / 255.0F};
		this.fireworkColor = l;
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public float[] getTextureDiffuseColors() {
		return this.textureDiffuseColors;
	}

	public MaterialColor getMaterialColor() {
		return this.color;
	}

	public int getFireworkColor() {
		return this.fireworkColor;
	}

	public int getTextColor() {
		return this.textColor;
	}

	public static DyeColor byId(int i) {
		return (DyeColor)BY_ID.apply(i);
	}

	@Nullable
	@Contract("_,!null->!null;_,null->_")
	public static DyeColor byName(String string, @Nullable DyeColor dyeColor) {
		DyeColor dyeColor2 = (DyeColor)CODEC.byName(string);
		return dyeColor2 != null ? dyeColor2 : dyeColor;
	}

	@Nullable
	public static DyeColor byFireworkColor(int i) {
		return BY_FIREWORK_COLOR.get(i);
	}

	public String toString() {
		return this.name;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}
}
