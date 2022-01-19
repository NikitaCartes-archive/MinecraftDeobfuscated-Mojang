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
		super(schema, References.SAVED_DATA);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped(
			"SavedDataUUIDFix",
			this.getInputSchema().getType(this.typeReference),
			typed -> typed.updateTyped(
					typed.getType().findField("data"),
					typedx -> typedx.update(
							DSL.remainderFinder(),
							dynamic -> dynamic.update(
									"Raids",
									dynamicx -> dynamicx.createList(
											dynamicx.asStream()
												.map(
													dynamicxx -> dynamicxx.update(
															"HeroesOfTheVillage",
															dynamicxxx -> dynamicxxx.createList(
																	dynamicxxx.asStream().map(dynamicxxxx -> (Dynamic)createUUIDFromLongs(dynamicxxxx, "UUIDMost", "UUIDLeast").orElseGet(() -> {
																			LOGGER.warn("HeroesOfTheVillage contained invalid UUIDs.");
																			return dynamicxxxx;
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
