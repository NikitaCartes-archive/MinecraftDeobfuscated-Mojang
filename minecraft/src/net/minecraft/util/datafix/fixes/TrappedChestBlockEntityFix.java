package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import org.slf4j.Logger;

public class TrappedChestBlockEntityFix extends DataFix {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int SIZE = 4096;
	private static final short SIZE_BITS = 12;

	public TrappedChestBlockEntityFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Override
	public TypeRewriteRule makeRule() {
		Type<?> type = this.getOutputSchema().getType(References.CHUNK);
		Type<?> type2 = type.findFieldType("Level");
		if (!(type2.findFieldType("TileEntities") instanceof ListType<?> listType)) {
			throw new IllegalStateException("Tile entity type is not a list type.");
		} else {
			OpticFinder<? extends List<?>> opticFinder = DSL.fieldFinder("TileEntities", (Type<? extends List<?>>)listType);
			Type<?> type4 = this.getInputSchema().getType(References.CHUNK);
			OpticFinder<?> opticFinder2 = type4.findField("Level");
			OpticFinder<?> opticFinder3 = opticFinder2.type().findField("Sections");
			Type<?> type5 = opticFinder3.type();
			if (!(type5 instanceof ListType)) {
				throw new IllegalStateException("Expecting sections to be a list.");
			} else {
				Type<?> type6 = ((ListType)type5).getElement();
				OpticFinder<?> opticFinder4 = DSL.typeFinder(type6);
				return TypeRewriteRule.seq(
					new AddNewChoices(this.getOutputSchema(), "AddTrappedChestFix", References.BLOCK_ENTITY).makeRule(),
					this.fixTypeEverywhereTyped("Trapped Chest fix", type4, typed -> typed.updateTyped(opticFinder2, typedx -> {
							Optional<? extends Typed<?>> optional = typedx.getOptionalTyped(opticFinder3);
							if (optional.isEmpty()) {
								return typedx;
							} else {
								List<? extends Typed<?>> list = ((Typed)optional.get()).getAllTyped(opticFinder4);
								IntSet intSet = new IntOpenHashSet();

								for (Typed<?> typed2 : list) {
									TrappedChestBlockEntityFix.TrappedChestSection trappedChestSection = new TrappedChestBlockEntityFix.TrappedChestSection(typed2, this.getInputSchema());
									if (!trappedChestSection.isSkippable()) {
										for (int i = 0; i < 4096; i++) {
											int j = trappedChestSection.getBlock(i);
											if (trappedChestSection.isTrappedChest(j)) {
												intSet.add(trappedChestSection.getIndex() << 12 | i);
											}
										}
									}
								}

								Dynamic<?> dynamic = typedx.get(DSL.remainderFinder());
								int k = dynamic.get("xPos").asInt(0);
								int l = dynamic.get("zPos").asInt(0);
								TaggedChoiceType<String> taggedChoiceType = (TaggedChoiceType<String>)this.getInputSchema().findChoiceType(References.BLOCK_ENTITY);
								return typedx.updateTyped(opticFinder, typedxx -> typedxx.updateTyped(taggedChoiceType.finder(), typedxxx -> {
										Dynamic<?> dynamicx = typedxxx.getOrCreate(DSL.remainderFinder());
										int kx = dynamicx.get("x").asInt(0) - (k << 4);
										int lx = dynamicx.get("y").asInt(0);
										int m = dynamicx.get("z").asInt(0) - (l << 4);
										return intSet.contains(LeavesFix.getIndex(kx, lx, m)) ? typedxxx.update(taggedChoiceType.finder(), pair -> pair.mapFirst(string -> {
												if (!Objects.equals(string, "minecraft:chest")) {
													LOGGER.warn("Block Entity was expected to be a chest");
												}

												return "minecraft:trapped_chest";
											})) : typedxxx;
									}));
							}
						}))
				);
			}
		}
	}

	public static final class TrappedChestSection extends LeavesFix.Section {
		@Nullable
		private IntSet chestIds;

		public TrappedChestSection(Typed<?> typed, Schema schema) {
			super(typed, schema);
		}

		@Override
		protected boolean skippable() {
			this.chestIds = new IntOpenHashSet();

			for (int i = 0; i < this.palette.size(); i++) {
				Dynamic<?> dynamic = (Dynamic<?>)this.palette.get(i);
				String string = dynamic.get("Name").asString("");
				if (Objects.equals(string, "minecraft:trapped_chest")) {
					this.chestIds.add(i);
				}
			}

			return this.chestIds.isEmpty();
		}

		public boolean isTrappedChest(int i) {
			return this.chestIds.contains(i);
		}
	}
}
