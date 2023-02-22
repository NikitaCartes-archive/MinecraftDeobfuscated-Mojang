package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ChunkToProtochunkFix extends DataFix {
	private static final int NUM_SECTIONS = 16;

	public ChunkToProtochunkFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Override
	public TypeRewriteRule makeRule() {
		return this.writeFixAndRead(
			"ChunkToProtoChunkFix",
			this.getInputSchema().getType(References.CHUNK),
			this.getOutputSchema().getType(References.CHUNK),
			dynamic -> dynamic.update("Level", ChunkToProtochunkFix::fixChunkData)
		);
	}

	private static <T> Dynamic<T> fixChunkData(Dynamic<T> dynamic) {
		boolean bl = dynamic.get("TerrainPopulated").asBoolean(false);
		boolean bl2 = dynamic.get("LightPopulated").asNumber().result().isEmpty() || dynamic.get("LightPopulated").asBoolean(false);
		String string;
		if (bl) {
			if (bl2) {
				string = "mobs_spawned";
			} else {
				string = "decorated";
			}
		} else {
			string = "carved";
		}

		return repackTicks(repackBiomes(dynamic)).set("Status", dynamic.createString(string)).set("hasLegacyStructureData", dynamic.createBoolean(true));
	}

	private static <T> Dynamic<T> repackBiomes(Dynamic<T> dynamic) {
		return dynamic.update("Biomes", dynamic2 -> DataFixUtils.orElse(dynamic2.asByteBufferOpt().result().map(byteBuffer -> {
				int[] is = new int[256];

				for (int i = 0; i < is.length; i++) {
					if (i < byteBuffer.capacity()) {
						is[i] = byteBuffer.get(i) & 255;
					}
				}

				return dynamic.createIntList(Arrays.stream(is));
			}), dynamic2));
	}

	private static <T> Dynamic<T> repackTicks(Dynamic<T> dynamic) {
		return DataFixUtils.orElse(
			dynamic.get("TileTicks")
				.asStreamOpt()
				.result()
				.map(
					stream -> {
						List<ShortList> list = (List<ShortList>)IntStream.range(0, 16).mapToObj(i -> new ShortArrayList()).collect(Collectors.toList());
						stream.forEach(dynamicxx -> {
							int i = dynamicxx.get("x").asInt(0);
							int j = dynamicxx.get("y").asInt(0);
							int k = dynamicxx.get("z").asInt(0);
							short s = packOffsetCoordinates(i, j, k);
							((ShortList)list.get(j >> 4)).add(s);
						});
						return dynamic.remove("TileTicks")
							.set(
								"ToBeTicked",
								dynamic.createList(list.stream().map(shortList -> dynamic.createList(shortList.intStream().mapToObj(i -> dynamic.createShort((short)i)))))
							);
					}
				),
			dynamic
		);
	}

	private static short packOffsetCoordinates(int i, int j, int k) {
		return (short)(i & 15 | (j & 15) << 4 | (k & 15) << 8);
	}
}
