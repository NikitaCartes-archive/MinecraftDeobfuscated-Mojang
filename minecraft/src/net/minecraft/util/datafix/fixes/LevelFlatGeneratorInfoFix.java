package net.minecraft.util.datafix.fixes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.math.NumberUtils;

public class LevelFlatGeneratorInfoFix extends DataFix {
	private static final String GENERATOR_OPTIONS = "generatorOptions";
	@VisibleForTesting
	static final String DEFAULT = "minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block;1;village";
	private static final Splitter SPLITTER = Splitter.on(';').limit(5);
	private static final Splitter LAYER_SPLITTER = Splitter.on(',');
	private static final Splitter OLD_AMOUNT_SPLITTER = Splitter.on('x').limit(2);
	private static final Splitter AMOUNT_SPLITTER = Splitter.on('*').limit(2);
	private static final Splitter BLOCK_SPLITTER = Splitter.on(':').limit(3);

	public LevelFlatGeneratorInfoFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Override
	public TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped(
			"LevelFlatGeneratorInfoFix", this.getInputSchema().getType(References.LEVEL), typed -> typed.update(DSL.remainderFinder(), this::fix)
		);
	}

	private Dynamic<?> fix(Dynamic<?> dynamic) {
		return dynamic.get("generatorName").asString("").equalsIgnoreCase("flat")
			? dynamic.update(
				"generatorOptions", dynamicx -> DataFixUtils.orElse(dynamicx.asString().map(this::fixString).map(dynamicx::createString).result(), dynamicx)
			)
			: dynamic;
	}

	@VisibleForTesting
	String fixString(String string) {
		if (string.isEmpty()) {
			return "minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block;1;village";
		} else {
			Iterator<String> iterator = SPLITTER.split(string).iterator();
			String string2 = (String)iterator.next();
			int i;
			String string3;
			if (iterator.hasNext()) {
				i = NumberUtils.toInt(string2, 0);
				string3 = (String)iterator.next();
			} else {
				i = 0;
				string3 = string2;
			}

			if (i >= 0 && i <= 3) {
				StringBuilder stringBuilder = new StringBuilder();
				Splitter splitter = i < 3 ? OLD_AMOUNT_SPLITTER : AMOUNT_SPLITTER;
				stringBuilder.append((String)StreamSupport.stream(LAYER_SPLITTER.split(string3).spliterator(), false).map(stringx -> {
					List<String> list = splitter.splitToList(stringx);
					int j;
					String string2x;
					if (list.size() == 2) {
						j = NumberUtils.toInt((String)list.get(0));
						string2x = (String)list.get(1);
					} else {
						j = 1;
						string2x = (String)list.get(0);
					}

					List<String> list2 = BLOCK_SPLITTER.splitToList(string2x);
					int k = ((String)list2.get(0)).equals("minecraft") ? 1 : 0;
					String string3x = (String)list2.get(k);
					int l = i == 3 ? EntityBlockStateFix.getBlockId("minecraft:" + string3x) : NumberUtils.toInt(string3x, 0);
					int m = k + 1;
					int n = list2.size() > m ? NumberUtils.toInt((String)list2.get(m), 0) : 0;
					return (j == 1 ? "" : j + "*") + BlockStateData.getTag(l << 4 | n).get("Name").asString("");
				}).collect(Collectors.joining(",")));

				while (iterator.hasNext()) {
					stringBuilder.append(';').append((String)iterator.next());
				}

				return stringBuilder.toString();
			} else {
				return "minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block;1;village";
			}
		}
	}
}
