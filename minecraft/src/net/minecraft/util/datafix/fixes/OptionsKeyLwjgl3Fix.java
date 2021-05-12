package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class OptionsKeyLwjgl3Fix extends DataFix {
	public static final String KEY_UNKNOWN = "key.unknown";
	private static final Int2ObjectMap<String> MAP = DataFixUtils.make(new Int2ObjectOpenHashMap<>(), int2ObjectOpenHashMap -> {
		int2ObjectOpenHashMap.put(0, "key.unknown");
		int2ObjectOpenHashMap.put(11, "key.0");
		int2ObjectOpenHashMap.put(2, "key.1");
		int2ObjectOpenHashMap.put(3, "key.2");
		int2ObjectOpenHashMap.put(4, "key.3");
		int2ObjectOpenHashMap.put(5, "key.4");
		int2ObjectOpenHashMap.put(6, "key.5");
		int2ObjectOpenHashMap.put(7, "key.6");
		int2ObjectOpenHashMap.put(8, "key.7");
		int2ObjectOpenHashMap.put(9, "key.8");
		int2ObjectOpenHashMap.put(10, "key.9");
		int2ObjectOpenHashMap.put(30, "key.a");
		int2ObjectOpenHashMap.put(40, "key.apostrophe");
		int2ObjectOpenHashMap.put(48, "key.b");
		int2ObjectOpenHashMap.put(43, "key.backslash");
		int2ObjectOpenHashMap.put(14, "key.backspace");
		int2ObjectOpenHashMap.put(46, "key.c");
		int2ObjectOpenHashMap.put(58, "key.caps.lock");
		int2ObjectOpenHashMap.put(51, "key.comma");
		int2ObjectOpenHashMap.put(32, "key.d");
		int2ObjectOpenHashMap.put(211, "key.delete");
		int2ObjectOpenHashMap.put(208, "key.down");
		int2ObjectOpenHashMap.put(18, "key.e");
		int2ObjectOpenHashMap.put(207, "key.end");
		int2ObjectOpenHashMap.put(28, "key.enter");
		int2ObjectOpenHashMap.put(13, "key.equal");
		int2ObjectOpenHashMap.put(1, "key.escape");
		int2ObjectOpenHashMap.put(33, "key.f");
		int2ObjectOpenHashMap.put(59, "key.f1");
		int2ObjectOpenHashMap.put(68, "key.f10");
		int2ObjectOpenHashMap.put(87, "key.f11");
		int2ObjectOpenHashMap.put(88, "key.f12");
		int2ObjectOpenHashMap.put(100, "key.f13");
		int2ObjectOpenHashMap.put(101, "key.f14");
		int2ObjectOpenHashMap.put(102, "key.f15");
		int2ObjectOpenHashMap.put(103, "key.f16");
		int2ObjectOpenHashMap.put(104, "key.f17");
		int2ObjectOpenHashMap.put(105, "key.f18");
		int2ObjectOpenHashMap.put(113, "key.f19");
		int2ObjectOpenHashMap.put(60, "key.f2");
		int2ObjectOpenHashMap.put(61, "key.f3");
		int2ObjectOpenHashMap.put(62, "key.f4");
		int2ObjectOpenHashMap.put(63, "key.f5");
		int2ObjectOpenHashMap.put(64, "key.f6");
		int2ObjectOpenHashMap.put(65, "key.f7");
		int2ObjectOpenHashMap.put(66, "key.f8");
		int2ObjectOpenHashMap.put(67, "key.f9");
		int2ObjectOpenHashMap.put(34, "key.g");
		int2ObjectOpenHashMap.put(41, "key.grave.accent");
		int2ObjectOpenHashMap.put(35, "key.h");
		int2ObjectOpenHashMap.put(199, "key.home");
		int2ObjectOpenHashMap.put(23, "key.i");
		int2ObjectOpenHashMap.put(210, "key.insert");
		int2ObjectOpenHashMap.put(36, "key.j");
		int2ObjectOpenHashMap.put(37, "key.k");
		int2ObjectOpenHashMap.put(82, "key.keypad.0");
		int2ObjectOpenHashMap.put(79, "key.keypad.1");
		int2ObjectOpenHashMap.put(80, "key.keypad.2");
		int2ObjectOpenHashMap.put(81, "key.keypad.3");
		int2ObjectOpenHashMap.put(75, "key.keypad.4");
		int2ObjectOpenHashMap.put(76, "key.keypad.5");
		int2ObjectOpenHashMap.put(77, "key.keypad.6");
		int2ObjectOpenHashMap.put(71, "key.keypad.7");
		int2ObjectOpenHashMap.put(72, "key.keypad.8");
		int2ObjectOpenHashMap.put(73, "key.keypad.9");
		int2ObjectOpenHashMap.put(78, "key.keypad.add");
		int2ObjectOpenHashMap.put(83, "key.keypad.decimal");
		int2ObjectOpenHashMap.put(181, "key.keypad.divide");
		int2ObjectOpenHashMap.put(156, "key.keypad.enter");
		int2ObjectOpenHashMap.put(141, "key.keypad.equal");
		int2ObjectOpenHashMap.put(55, "key.keypad.multiply");
		int2ObjectOpenHashMap.put(74, "key.keypad.subtract");
		int2ObjectOpenHashMap.put(38, "key.l");
		int2ObjectOpenHashMap.put(203, "key.left");
		int2ObjectOpenHashMap.put(56, "key.left.alt");
		int2ObjectOpenHashMap.put(26, "key.left.bracket");
		int2ObjectOpenHashMap.put(29, "key.left.control");
		int2ObjectOpenHashMap.put(42, "key.left.shift");
		int2ObjectOpenHashMap.put(219, "key.left.win");
		int2ObjectOpenHashMap.put(50, "key.m");
		int2ObjectOpenHashMap.put(12, "key.minus");
		int2ObjectOpenHashMap.put(49, "key.n");
		int2ObjectOpenHashMap.put(69, "key.num.lock");
		int2ObjectOpenHashMap.put(24, "key.o");
		int2ObjectOpenHashMap.put(25, "key.p");
		int2ObjectOpenHashMap.put(209, "key.page.down");
		int2ObjectOpenHashMap.put(201, "key.page.up");
		int2ObjectOpenHashMap.put(197, "key.pause");
		int2ObjectOpenHashMap.put(52, "key.period");
		int2ObjectOpenHashMap.put(183, "key.print.screen");
		int2ObjectOpenHashMap.put(16, "key.q");
		int2ObjectOpenHashMap.put(19, "key.r");
		int2ObjectOpenHashMap.put(205, "key.right");
		int2ObjectOpenHashMap.put(184, "key.right.alt");
		int2ObjectOpenHashMap.put(27, "key.right.bracket");
		int2ObjectOpenHashMap.put(157, "key.right.control");
		int2ObjectOpenHashMap.put(54, "key.right.shift");
		int2ObjectOpenHashMap.put(220, "key.right.win");
		int2ObjectOpenHashMap.put(31, "key.s");
		int2ObjectOpenHashMap.put(70, "key.scroll.lock");
		int2ObjectOpenHashMap.put(39, "key.semicolon");
		int2ObjectOpenHashMap.put(53, "key.slash");
		int2ObjectOpenHashMap.put(57, "key.space");
		int2ObjectOpenHashMap.put(20, "key.t");
		int2ObjectOpenHashMap.put(15, "key.tab");
		int2ObjectOpenHashMap.put(22, "key.u");
		int2ObjectOpenHashMap.put(200, "key.up");
		int2ObjectOpenHashMap.put(47, "key.v");
		int2ObjectOpenHashMap.put(17, "key.w");
		int2ObjectOpenHashMap.put(45, "key.x");
		int2ObjectOpenHashMap.put(21, "key.y");
		int2ObjectOpenHashMap.put(44, "key.z");
	});

	public OptionsKeyLwjgl3Fix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Override
	public TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped(
			"OptionsKeyLwjgl3Fix",
			this.getInputSchema().getType(References.OPTIONS),
			typed -> typed.update(
					DSL.remainderFinder(),
					dynamic -> (Dynamic)dynamic.getMapValues()
							.map(map -> dynamic.createMap((Map<? extends Dynamic<?>, ? extends Dynamic<?>>)map.entrySet().stream().map(entry -> {
									if (((Dynamic)entry.getKey()).asString("").startsWith("key_")) {
										int i = Integer.parseInt(((Dynamic)entry.getValue()).asString(""));
										if (i < 0) {
											int j = i + 100;
											String string;
											if (j == 0) {
												string = "key.mouse.left";
											} else if (j == 1) {
												string = "key.mouse.right";
											} else if (j == 2) {
												string = "key.mouse.middle";
											} else {
												string = "key.mouse." + (j + 1);
											}

											return Pair.of((Dynamic)entry.getKey(), ((Dynamic)entry.getValue()).createString(string));
										} else {
											String string2 = MAP.getOrDefault(i, "key.unknown");
											return Pair.of((Dynamic)entry.getKey(), ((Dynamic)entry.getValue()).createString(string2));
										}
									} else {
										return Pair.of((Dynamic)entry.getKey(), (Dynamic)entry.getValue());
									}
								}).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))))
							.result()
							.orElse(dynamic)
				)
		);
	}
}
