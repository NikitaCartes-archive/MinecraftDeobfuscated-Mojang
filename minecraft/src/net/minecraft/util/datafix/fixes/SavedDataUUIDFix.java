package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import org.slf4j.Logger;

public class SavedDataUUIDFix extends AbstractUUIDFix {
	private static final Logger LOGGER = LogUtils.getLogger();

	public SavedDataUUIDFix(Schema schema) {
		super(schema, References.SAVED_DATA_RAIDS);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped(
			"SavedDataUUIDFix",
			this.getInputSchema().getType(this.typeReference),
			typed -> typed.update(
					DSL.remainderFinder(),
					dynamic -> dynamic.update(
							"data",
							dynamicx -> dynamicx.update(
									"Raids",
									dynamicxx -> dynamicxx.createList(
											dynamicxx.asStream()
												.map(
													dynamicxxx -> dynamicxxx.update(
															"HeroesOfTheVillage",
															dynamicxxxx -> dynamicxxxx.createList(
																	dynamicxxxx.asStream().map(dynamicxxxxx -> (Dynamic)createUUIDFromLongs(dynamicxxxxx, "UUIDMost", "UUIDLeast").orElseGet(() -> {
																			LOGGER.warn("HeroesOfTheVillage contained invalid UUIDs.");
																			return dynamicxxxxx;
																		}))
																)
														)
												)
										)
								)
						)
				)
		);
	}
}
