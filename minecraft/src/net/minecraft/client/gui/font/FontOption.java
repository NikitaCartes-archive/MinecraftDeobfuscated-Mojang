package net.minecraft.client.gui.font;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.StringRepresentable;

@Environment(EnvType.CLIENT)
public enum FontOption implements StringRepresentable {
	UNIFORM("uniform"),
	JAPANESE_VARIANTS("jp");

	public static final Codec<FontOption> CODEC = StringRepresentable.fromEnum(FontOption::values);
	private final String name;

	private FontOption(final String string2) {
		this.name = string2;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	@Environment(EnvType.CLIENT)
	public static class Filter {
		private final Map<FontOption, Boolean> values;
		public static final Codec<FontOption.Filter> CODEC = Codec.unboundedMap(FontOption.CODEC, Codec.BOOL).xmap(FontOption.Filter::new, filter -> filter.values);
		public static final FontOption.Filter ALWAYS_PASS = new FontOption.Filter(Map.of());

		public Filter(Map<FontOption, Boolean> map) {
			this.values = map;
		}

		public boolean apply(Set<FontOption> set) {
			for (Entry<FontOption, Boolean> entry : this.values.entrySet()) {
				if (set.contains(entry.getKey()) != (Boolean)entry.getValue()) {
					return false;
				}
			}

			return true;
		}

		public FontOption.Filter merge(FontOption.Filter filter) {
			Map<FontOption, Boolean> map = new HashMap(filter.values);
			map.putAll(this.values);
			return new FontOption.Filter(Map.copyOf(map));
		}
	}
}
