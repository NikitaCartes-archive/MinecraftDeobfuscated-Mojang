package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class MobEffectIdFix extends DataFix {
	private static final Int2ObjectMap<String> ID_MAP = Util.make(new Int2ObjectOpenHashMap<>(), int2ObjectOpenHashMap -> {
		int2ObjectOpenHashMap.put(1, "minecraft:speed");
		int2ObjectOpenHashMap.put(2, "minecraft:slowness");
		int2ObjectOpenHashMap.put(3, "minecraft:haste");
		int2ObjectOpenHashMap.put(4, "minecraft:mining_fatigue");
		int2ObjectOpenHashMap.put(5, "minecraft:strength");
		int2ObjectOpenHashMap.put(6, "minecraft:instant_health");
		int2ObjectOpenHashMap.put(7, "minecraft:instant_damage");
		int2ObjectOpenHashMap.put(8, "minecraft:jump_boost");
		int2ObjectOpenHashMap.put(9, "minecraft:nausea");
		int2ObjectOpenHashMap.put(10, "minecraft:regeneration");
		int2ObjectOpenHashMap.put(11, "minecraft:resistance");
		int2ObjectOpenHashMap.put(12, "minecraft:fire_resistance");
		int2ObjectOpenHashMap.put(13, "minecraft:water_breathing");
		int2ObjectOpenHashMap.put(14, "minecraft:invisibility");
		int2ObjectOpenHashMap.put(15, "minecraft:blindness");
		int2ObjectOpenHashMap.put(16, "minecraft:night_vision");
		int2ObjectOpenHashMap.put(17, "minecraft:hunger");
		int2ObjectOpenHashMap.put(18, "minecraft:weakness");
		int2ObjectOpenHashMap.put(19, "minecraft:poison");
		int2ObjectOpenHashMap.put(20, "minecraft:wither");
		int2ObjectOpenHashMap.put(21, "minecraft:health_boost");
		int2ObjectOpenHashMap.put(22, "minecraft:absorption");
		int2ObjectOpenHashMap.put(23, "minecraft:saturation");
		int2ObjectOpenHashMap.put(24, "minecraft:glowing");
		int2ObjectOpenHashMap.put(25, "minecraft:levitation");
		int2ObjectOpenHashMap.put(26, "minecraft:luck");
		int2ObjectOpenHashMap.put(27, "minecraft:unluck");
		int2ObjectOpenHashMap.put(28, "minecraft:slow_falling");
		int2ObjectOpenHashMap.put(29, "minecraft:conduit_power");
		int2ObjectOpenHashMap.put(30, "minecraft:dolphins_grace");
		int2ObjectOpenHashMap.put(31, "minecraft:bad_omen");
		int2ObjectOpenHashMap.put(32, "minecraft:hero_of_the_village");
		int2ObjectOpenHashMap.put(33, "minecraft:darkness");
	});
	private static final Set<String> MOB_EFFECT_INSTANCE_CARRIER_ITEMS = Set.of(
		"minecraft:potion", "minecraft:splash_potion", "minecraft:lingering_potion", "minecraft:tipped_arrow"
	);

	public MobEffectIdFix(Schema schema) {
		super(schema, false);
	}

	private static <T> Optional<Dynamic<T>> getAndConvertMobEffectId(Dynamic<T> dynamic, String string) {
		return dynamic.get(string).asNumber().result().map(number -> ID_MAP.get(number.intValue())).map(dynamic::createString);
	}

	private static <T> Dynamic<T> setFieldIfPresent(Dynamic<T> dynamic, String string, Optional<Dynamic<T>> optional) {
		return optional.isEmpty() ? dynamic : dynamic.set(string, (Dynamic<?>)optional.get());
	}

	private static <T> Dynamic<T> replaceField(Dynamic<T> dynamic, String string, String string2, Optional<Dynamic<T>> optional) {
		return setFieldIfPresent(dynamic.remove(string), string2, optional);
	}

	private static <T> Dynamic<T> renameField(Dynamic<T> dynamic, String string, String string2) {
		return setFieldIfPresent(dynamic.remove(string), string2, dynamic.get(string).result());
	}

	private static <T> Dynamic<T> updateMobEffectIdField(Dynamic<T> dynamic, String string, Dynamic<T> dynamic2, String string2) {
		Optional<Dynamic<T>> optional = getAndConvertMobEffectId(dynamic, string);
		return replaceField(dynamic2, string, string2, optional);
	}

	private static <T> Dynamic<T> updateMobEffectIdField(Dynamic<T> dynamic, String string, String string2) {
		return updateMobEffectIdField(dynamic, string, dynamic, string2);
	}

	private static <T> Dynamic<T> updateMobEffectInstance(Dynamic<T> dynamic) {
		dynamic = updateMobEffectIdField(dynamic, "Id", "id");
		dynamic = renameField(dynamic, "Ambient", "ambient");
		dynamic = renameField(dynamic, "Amplifier", "amplifier");
		dynamic = renameField(dynamic, "Duration", "duration");
		dynamic = renameField(dynamic, "ShowParticles", "show_particles");
		dynamic = renameField(dynamic, "ShowIcon", "show_icon");
		dynamic = renameField(dynamic, "FactorCalculationData", "factor_calculation_data");
		Optional<Dynamic<T>> optional = dynamic.get("HiddenEffect").result().map(MobEffectIdFix::updateMobEffectInstance);
		return replaceField(dynamic, "HiddenEffect", "hidden_effect", optional);
	}

	private static <T> Dynamic<T> updateMobEffectInstanceList(Dynamic<T> dynamic, String string, String string2) {
		Optional<Dynamic<T>> optional = dynamic.get(string)
			.asStreamOpt()
			.result()
			.map(stream -> dynamic.createList(stream.map(MobEffectIdFix::updateMobEffectInstance)));
		return replaceField(dynamic, string, string2, optional);
	}

	private static <T> Dynamic<T> updateSuspiciousStewEntry(Dynamic<T> dynamic, Dynamic<T> dynamic2) {
		dynamic2 = updateMobEffectIdField(dynamic, "EffectId", dynamic2, "id");
		Optional<Dynamic<T>> optional = dynamic.get("EffectDuration").result();
		return replaceField(dynamic2, "EffectDuration", "duration", optional);
	}

	private static <T> Dynamic<T> updateSuspiciousStewEntry(Dynamic<T> dynamic) {
		return updateSuspiciousStewEntry(dynamic, dynamic);
	}

	private Typed<?> updateNamedChoice(Typed<?> typed, TypeReference typeReference, String string, Function<Dynamic<?>, Dynamic<?>> function) {
		Type<?> type = this.getInputSchema().getChoiceType(typeReference, string);
		Type<?> type2 = this.getOutputSchema().getChoiceType(typeReference, string);
		return typed.updateTyped(DSL.namedChoice(string, type), type2, typedx -> typedx.update(DSL.remainderFinder(), function));
	}

	private TypeRewriteRule blockEntityFixer() {
		Type<?> type = this.getInputSchema().getType(References.BLOCK_ENTITY);
		return this.fixTypeEverywhereTyped(
			"BlockEntityMobEffectIdFix", type, typed -> this.updateNamedChoice(typed, References.BLOCK_ENTITY, "minecraft:beacon", dynamic -> {
					dynamic = updateMobEffectIdField(dynamic, "Primary", "primary_effect");
					return updateMobEffectIdField(dynamic, "Secondary", "secondary_effect");
				})
		);
	}

	private static <T> Dynamic<T> fixMooshroomTag(Dynamic<T> dynamic) {
		Dynamic<T> dynamic2 = dynamic.emptyMap();
		Dynamic<T> dynamic3 = updateSuspiciousStewEntry(dynamic, dynamic2);
		if (!dynamic3.equals(dynamic2)) {
			dynamic = dynamic.set("stew_effects", dynamic.createList(Stream.of(dynamic3)));
		}

		return dynamic.remove("EffectId").remove("EffectDuration");
	}

	private static <T> Dynamic<T> fixArrowTag(Dynamic<T> dynamic) {
		return updateMobEffectInstanceList(dynamic, "CustomPotionEffects", "custom_potion_effects");
	}

	private static <T> Dynamic<T> fixAreaEffectCloudTag(Dynamic<T> dynamic) {
		return updateMobEffectInstanceList(dynamic, "Effects", "effects");
	}

	private static Dynamic<?> updateLivingEntityTag(Dynamic<?> dynamic) {
		return updateMobEffectInstanceList(dynamic, "ActiveEffects", "active_effects");
	}

	private TypeRewriteRule entityFixer() {
		Type<?> type = this.getInputSchema().getType(References.ENTITY);
		return this.fixTypeEverywhereTyped("EntityMobEffectIdFix", type, typed -> {
			typed = this.updateNamedChoice(typed, References.ENTITY, "minecraft:mooshroom", MobEffectIdFix::fixMooshroomTag);
			typed = this.updateNamedChoice(typed, References.ENTITY, "minecraft:arrow", MobEffectIdFix::fixArrowTag);
			typed = this.updateNamedChoice(typed, References.ENTITY, "minecraft:area_effect_cloud", MobEffectIdFix::fixAreaEffectCloudTag);
			return typed.update(DSL.remainderFinder(), MobEffectIdFix::updateLivingEntityTag);
		});
	}

	private TypeRewriteRule playerFixer() {
		Type<?> type = this.getInputSchema().getType(References.PLAYER);
		return this.fixTypeEverywhereTyped("PlayerMobEffectIdFix", type, typed -> typed.update(DSL.remainderFinder(), MobEffectIdFix::updateLivingEntityTag));
	}

	private static <T> Dynamic<T> fixSuspiciousStewTag(Dynamic<T> dynamic) {
		Optional<Dynamic<T>> optional = dynamic.get("Effects")
			.asStreamOpt()
			.result()
			.map(stream -> dynamic.createList(stream.map(MobEffectIdFix::updateSuspiciousStewEntry)));
		return replaceField(dynamic, "Effects", "effects", optional);
	}

	private TypeRewriteRule itemStackFixer() {
		OpticFinder<Pair<String, String>> opticFinder = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
		Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
		OpticFinder<?> opticFinder2 = type.findField("tag");
		return this.fixTypeEverywhereTyped(
			"ItemStackMobEffectIdFix",
			type,
			typed -> {
				Optional<Pair<String, String>> optional = typed.getOptional(opticFinder);
				if (optional.isPresent()) {
					String string = (String)((Pair)optional.get()).getSecond();
					if (string.equals("minecraft:suspicious_stew")) {
						return typed.updateTyped(opticFinder2, typedx -> typedx.update(DSL.remainderFinder(), MobEffectIdFix::fixSuspiciousStewTag));
					}

					if (MOB_EFFECT_INSTANCE_CARRIER_ITEMS.contains(string)) {
						return typed.updateTyped(
							opticFinder2,
							typedx -> typedx.update(DSL.remainderFinder(), dynamic -> updateMobEffectInstanceList(dynamic, "CustomPotionEffects", "custom_potion_effects"))
						);
					}
				}

				return typed;
			}
		);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return TypeRewriteRule.seq(this.blockEntityFixer(), this.entityFixer(), this.playerFixer(), this.itemStackFixer());
	}
}
