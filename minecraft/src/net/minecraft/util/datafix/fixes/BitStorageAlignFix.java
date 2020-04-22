package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.stream.LongStream;
import net.minecraft.util.Mth;

public class BitStorageAlignFix extends DataFix {
	public BitStorageAlignFix(Schema schema) {
		super(schema, false);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.CHUNK);
		Type<?> type2 = type.findFieldType("Level");
		OpticFinder<?> opticFinder = DSL.fieldFinder("Level", type2);
		OpticFinder<?> opticFinder2 = opticFinder.type().findField("Sections");
		Type<?> type3 = ((ListType)opticFinder2.type()).getElement();
		OpticFinder<?> opticFinder3 = DSL.typeFinder(type3);
		Type<Pair<String, Dynamic<?>>> type4 = DSL.named(References.BLOCK_STATE.typeName(), DSL.remainderType());
		OpticFinder<List<Pair<String, Dynamic<?>>>> opticFinder4 = DSL.fieldFinder("Palette", DSL.list(type4));
		return this.fixTypeEverywhereTyped(
			"BitStorageAlignFix",
			type,
			this.getOutputSchema().getType(References.CHUNK),
			typed -> typed.updateTyped(opticFinder, typedx -> typedx.updateTyped(opticFinder2, typedxx -> typedxx.updateTyped(opticFinder3, typedxxx -> {
							int i = (Integer)typedxxx.getOptional(opticFinder4).map(list -> Math.max(4, DataFixUtils.ceillog2(list.size()))).orElse(0);
							return i != 0 && !Mth.isPowerOfTwo(i) ? typedxxx.update(DSL.remainderFinder(), dynamic -> this.fixBlockStates(dynamic, i)) : typedxxx;
						})))
		);
	}

	private Dynamic<?> fixBlockStates(Dynamic<?> dynamic, int i) {
		return dynamic.update("BlockStates", dynamic2 -> {
			long[] ls = dynamic2.asLongStream().toArray();
			long[] ms = addPadding(4096, i, ls);
			return dynamic.createLongList(LongStream.of(ms));
		});
	}

	public static long[] addPadding(int i, int j, long[] ls) {
		int k = ls.length;
		if (k == 0) {
			return ls;
		} else {
			long l = (1L << j) - 1L;
			int m = 64 / j;
			int n = (i + m - 1) / m;
			long[] ms = new long[n];
			int o = 0;
			int p = 0;
			long q = 0L;
			int r = 0;
			long s = ls[0];
			long t = k > 1 ? ls[1] : 0L;

			for (int u = 0; u < i; u++) {
				int v = u * j;
				int w = v >> 6;
				int x = (u + 1) * j - 1 >> 6;
				int y = v ^ w << 6;
				if (w != r) {
					s = t;
					t = w + 1 < k ? ls[w + 1] : 0L;
					r = w;
				}

				long z;
				if (w == x) {
					z = s >>> y & l;
				} else {
					int aa = 64 - y;
					z = (s >>> y | t << aa) & l;
				}

				int aa = p + j;
				if (aa >= 64) {
					ms[o++] = q;
					q = z;
					p = j;
				} else {
					q |= z << p;
					p = aa;
				}
			}

			if (q != 0L) {
				ms[o] = q;
			}

			return ms;
		}
	}
}
