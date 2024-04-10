package net.minecraft.util.datafix.fixes;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import org.slf4j.Logger;

public class ParticleUnflatteningFix extends DataFix {
	private static final Logger LOGGER = LogUtils.getLogger();

	public ParticleUnflatteningFix(Schema schema) {
		super(schema, true);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.PARTICLE);
		Type<?> type2 = this.getOutputSchema().getType(References.PARTICLE);
		return this.writeFixAndRead("ParticleUnflatteningFix", type, type2, this::fix);
	}

	private <T> Dynamic<T> fix(Dynamic<T> dynamic) {
		Optional<String> optional = dynamic.asString().result();
		if (optional.isEmpty()) {
			return dynamic;
		} else {
			String string = (String)optional.get();
			String[] strings = string.split(" ", 2);
			String string2 = NamespacedSchema.ensureNamespaced(strings[0]);
			Dynamic<T> dynamic2 = dynamic.createMap(Map.of(dynamic.createString("type"), dynamic.createString(string2)));

			return switch (string2) {
				case "minecraft:item" -> strings.length > 1 ? this.updateItem(dynamic2, strings[1]) : dynamic2;
				case "minecraft:block", "minecraft:block_marker", "minecraft:falling_dust", "minecraft:dust_pillar" -> strings.length > 1
				? this.updateBlock(dynamic2, strings[1])
				: dynamic2;
				case "minecraft:dust" -> strings.length > 1 ? this.updateDust(dynamic2, strings[1]) : dynamic2;
				case "minecraft:dust_color_transition" -> strings.length > 1 ? this.updateDustTransition(dynamic2, strings[1]) : dynamic2;
				case "minecraft:sculk_charge" -> strings.length > 1 ? this.updateSculkCharge(dynamic2, strings[1]) : dynamic2;
				case "minecraft:vibration" -> strings.length > 1 ? this.updateVibration(dynamic2, strings[1]) : dynamic2;
				case "minecraft:shriek" -> strings.length > 1 ? this.updateShriek(dynamic2, strings[1]) : dynamic2;
				default -> dynamic2;
			};
		}
	}

	private <T> Dynamic<T> updateItem(Dynamic<T> dynamic, String string) {
		int i = string.indexOf("{");
		Dynamic<T> dynamic2 = dynamic.createMap(Map.of(dynamic.createString("Count"), dynamic.createInt(1)));
		if (i == -1) {
			dynamic2 = dynamic2.set("id", dynamic.createString(string));
		} else {
			dynamic2 = dynamic2.set("id", dynamic.createString(string.substring(0, i)));
			CompoundTag compoundTag = parseTag(string.substring(i));
			if (compoundTag != null) {
				dynamic2 = dynamic2.set("tag", new Dynamic<>(NbtOps.INSTANCE, compoundTag).convert(dynamic.getOps()));
			}
		}

		return dynamic.set("item", dynamic2);
	}

	@Nullable
	private static CompoundTag parseTag(String string) {
		try {
			return TagParser.parseTag(string);
		} catch (Exception var2) {
			LOGGER.warn("Failed to parse tag: {}", string, var2);
			return null;
		}
	}

	private <T> Dynamic<T> updateBlock(Dynamic<T> dynamic, String string) {
		int i = string.indexOf("[");
		Dynamic<T> dynamic2 = dynamic.emptyMap();
		if (i == -1) {
			dynamic2 = dynamic2.set("Name", dynamic.createString(NamespacedSchema.ensureNamespaced(string)));
		} else {
			dynamic2 = dynamic2.set("Name", dynamic.createString(NamespacedSchema.ensureNamespaced(string.substring(0, i))));
			Map<Dynamic<T>, Dynamic<T>> map = parseBlockProperties(dynamic, string.substring(i));
			if (!map.isEmpty()) {
				dynamic2 = dynamic2.set("Properties", dynamic.createMap(map));
			}
		}

		return dynamic.set("block_state", dynamic2);
	}

	private static <T> Map<Dynamic<T>, Dynamic<T>> parseBlockProperties(Dynamic<T> dynamic, String string) {
		try {
			Map<Dynamic<T>, Dynamic<T>> map = new HashMap();
			StringReader stringReader = new StringReader(string);
			stringReader.expect('[');
			stringReader.skipWhitespace();

			while (stringReader.canRead() && stringReader.peek() != ']') {
				stringReader.skipWhitespace();
				String string2 = stringReader.readString();
				stringReader.skipWhitespace();
				stringReader.expect('=');
				stringReader.skipWhitespace();
				String string3 = stringReader.readString();
				stringReader.skipWhitespace();
				map.put(dynamic.createString(string2), dynamic.createString(string3));
				if (stringReader.canRead()) {
					if (stringReader.peek() != ',') {
						break;
					}

					stringReader.skip();
				}
			}

			stringReader.expect(']');
			return map;
		} catch (Exception var6) {
			LOGGER.warn("Failed to parse block properties: {}", string, var6);
			return Map.of();
		}
	}

	private static <T> Dynamic<T> readVector(Dynamic<T> dynamic, StringReader stringReader) throws CommandSyntaxException {
		float f = stringReader.readFloat();
		stringReader.expect(' ');
		float g = stringReader.readFloat();
		stringReader.expect(' ');
		float h = stringReader.readFloat();
		return dynamic.createList(Stream.of(f, g, h).map(dynamic::createFloat));
	}

	private <T> Dynamic<T> updateDust(Dynamic<T> dynamic, String string) {
		try {
			StringReader stringReader = new StringReader(string);
			Dynamic<T> dynamic2 = readVector(dynamic, stringReader);
			stringReader.expect(' ');
			float f = stringReader.readFloat();
			return dynamic.set("color", dynamic2).set("scale", dynamic.createFloat(f));
		} catch (Exception var6) {
			LOGGER.warn("Failed to parse particle options: {}", string, var6);
			return dynamic;
		}
	}

	private <T> Dynamic<T> updateDustTransition(Dynamic<T> dynamic, String string) {
		try {
			StringReader stringReader = new StringReader(string);
			Dynamic<T> dynamic2 = readVector(dynamic, stringReader);
			stringReader.expect(' ');
			float f = stringReader.readFloat();
			stringReader.expect(' ');
			Dynamic<T> dynamic3 = readVector(dynamic, stringReader);
			return dynamic.set("from_color", dynamic2).set("to_color", dynamic3).set("scale", dynamic.createFloat(f));
		} catch (Exception var7) {
			LOGGER.warn("Failed to parse particle options: {}", string, var7);
			return dynamic;
		}
	}

	private <T> Dynamic<T> updateSculkCharge(Dynamic<T> dynamic, String string) {
		try {
			StringReader stringReader = new StringReader(string);
			float f = stringReader.readFloat();
			return dynamic.set("roll", dynamic.createFloat(f));
		} catch (Exception var5) {
			LOGGER.warn("Failed to parse particle options: {}", string, var5);
			return dynamic;
		}
	}

	private <T> Dynamic<T> updateVibration(Dynamic<T> dynamic, String string) {
		try {
			StringReader stringReader = new StringReader(string);
			float f = (float)stringReader.readDouble();
			stringReader.expect(' ');
			float g = (float)stringReader.readDouble();
			stringReader.expect(' ');
			float h = (float)stringReader.readDouble();
			stringReader.expect(' ');
			int i = stringReader.readInt();
			Dynamic<T> dynamic2 = (Dynamic<T>)dynamic.createIntList(IntStream.of(new int[]{Mth.floor(f), Mth.floor(g), Mth.floor(h)}));
			Dynamic<T> dynamic3 = dynamic.createMap(Map.of(dynamic.createString("type"), dynamic.createString("minecraft:block"), dynamic.createString("pos"), dynamic2));
			return dynamic.set("destination", dynamic3).set("arrival_in_ticks", dynamic.createInt(i));
		} catch (Exception var10) {
			LOGGER.warn("Failed to parse particle options: {}", string, var10);
			return dynamic;
		}
	}

	private <T> Dynamic<T> updateShriek(Dynamic<T> dynamic, String string) {
		try {
			StringReader stringReader = new StringReader(string);
			int i = stringReader.readInt();
			return dynamic.set("delay", dynamic.createInt(i));
		} catch (Exception var5) {
			LOGGER.warn("Failed to parse particle options: {}", string, var5);
			return dynamic;
		}
	}
}
