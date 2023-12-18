package net.minecraft.world.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class MobEffects {
	private static final int DARKNESS_EFFECT_FACTOR_PADDING_DURATION_TICKS = 22;
	public static final Holder<MobEffect> MOVEMENT_SPEED = register(
		"speed",
		new MobEffect(MobEffectCategory.BENEFICIAL, 3402751)
			.addAttributeModifier(Attributes.MOVEMENT_SPEED, "91AEAA56-376B-4498-935B-2F7F68070635", 0.2F, AttributeModifier.Operation.MULTIPLY_TOTAL)
	);
	public static final Holder<MobEffect> MOVEMENT_SLOWDOWN = register(
		"slowness",
		new MobEffect(MobEffectCategory.HARMFUL, 9154528)
			.addAttributeModifier(Attributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-514C1F160890", -0.15F, AttributeModifier.Operation.MULTIPLY_TOTAL)
	);
	public static final Holder<MobEffect> DIG_SPEED = register(
		"haste",
		new MobEffect(MobEffectCategory.BENEFICIAL, 14270531)
			.addAttributeModifier(Attributes.ATTACK_SPEED, "AF8B6E3F-3328-4C0A-AA36-5BA2BB9DBEF3", 0.1F, AttributeModifier.Operation.MULTIPLY_TOTAL)
	);
	public static final Holder<MobEffect> DIG_SLOWDOWN = register(
		"mining_fatigue",
		new MobEffect(MobEffectCategory.HARMFUL, 4866583)
			.addAttributeModifier(Attributes.ATTACK_SPEED, "55FCED67-E92A-486E-9800-B47F202C4386", -0.1F, AttributeModifier.Operation.MULTIPLY_TOTAL)
	);
	public static final Holder<MobEffect> DAMAGE_BOOST = register(
		"strength",
		new MobEffect(MobEffectCategory.BENEFICIAL, 16762624)
			.addAttributeModifier(Attributes.ATTACK_DAMAGE, "648D7064-6A60-4F59-8ABE-C2C23A6DD7A9", 3.0, AttributeModifier.Operation.ADDITION)
	);
	public static final Holder<MobEffect> HEAL = register("instant_health", new HealOrHarmMobEffect(MobEffectCategory.BENEFICIAL, 16262179, false));
	public static final Holder<MobEffect> HARM = register("instant_damage", new HealOrHarmMobEffect(MobEffectCategory.HARMFUL, 11101546, true));
	public static final Holder<MobEffect> JUMP = register("jump_boost", new MobEffect(MobEffectCategory.BENEFICIAL, 16646020));
	public static final Holder<MobEffect> CONFUSION = register("nausea", new MobEffect(MobEffectCategory.HARMFUL, 5578058));
	public static final Holder<MobEffect> REGENERATION = register("regeneration", new RegenerationMobEffect(MobEffectCategory.BENEFICIAL, 13458603));
	public static final Holder<MobEffect> DAMAGE_RESISTANCE = register("resistance", new MobEffect(MobEffectCategory.BENEFICIAL, 9520880));
	public static final Holder<MobEffect> FIRE_RESISTANCE = register("fire_resistance", new MobEffect(MobEffectCategory.BENEFICIAL, 16750848));
	public static final Holder<MobEffect> WATER_BREATHING = register("water_breathing", new MobEffect(MobEffectCategory.BENEFICIAL, 10017472));
	public static final Holder<MobEffect> INVISIBILITY = register("invisibility", new MobEffect(MobEffectCategory.BENEFICIAL, 16185078));
	public static final Holder<MobEffect> BLINDNESS = register("blindness", new MobEffect(MobEffectCategory.HARMFUL, 2039587));
	public static final Holder<MobEffect> NIGHT_VISION = register("night_vision", new MobEffect(MobEffectCategory.BENEFICIAL, 12779366));
	public static final Holder<MobEffect> HUNGER = register("hunger", new HungerMobEffect(MobEffectCategory.HARMFUL, 5797459));
	public static final Holder<MobEffect> WEAKNESS = register(
		"weakness",
		new MobEffect(MobEffectCategory.HARMFUL, 4738376)
			.addAttributeModifier(Attributes.ATTACK_DAMAGE, "22653B89-116E-49DC-9B6B-9971489B5BE5", -4.0, AttributeModifier.Operation.ADDITION)
	);
	public static final Holder<MobEffect> POISON = register("poison", new PoisonMobEffect(MobEffectCategory.HARMFUL, 8889187));
	public static final Holder<MobEffect> WITHER = register("wither", new WitherMobEffect(MobEffectCategory.HARMFUL, 7561558));
	public static final Holder<MobEffect> HEALTH_BOOST = register(
		"health_boost",
		new MobEffect(MobEffectCategory.BENEFICIAL, 16284963)
			.addAttributeModifier(Attributes.MAX_HEALTH, "5D6F0BA2-1186-46AC-B896-C61C5CEE99CC", 4.0, AttributeModifier.Operation.ADDITION)
	);
	public static final Holder<MobEffect> ABSORPTION = register(
		"absorption",
		new AbsorptionMobEffect(MobEffectCategory.BENEFICIAL, 2445989)
			.addAttributeModifier(Attributes.MAX_ABSORPTION, "EAE29CF0-701E-4ED6-883A-96F798F3DAB5", 4.0, AttributeModifier.Operation.ADDITION)
	);
	public static final Holder<MobEffect> SATURATION = register("saturation", new SaturationMobEffect(MobEffectCategory.BENEFICIAL, 16262179));
	public static final Holder<MobEffect> GLOWING = register("glowing", new MobEffect(MobEffectCategory.NEUTRAL, 9740385));
	public static final Holder<MobEffect> LEVITATION = register("levitation", new MobEffect(MobEffectCategory.HARMFUL, 13565951));
	public static final Holder<MobEffect> LUCK = register(
		"luck",
		new MobEffect(MobEffectCategory.BENEFICIAL, 5882118)
			.addAttributeModifier(Attributes.LUCK, "03C3C89D-7037-4B42-869F-B146BCB64D2E", 1.0, AttributeModifier.Operation.ADDITION)
	);
	public static final Holder<MobEffect> UNLUCK = register(
		"unluck",
		new MobEffect(MobEffectCategory.HARMFUL, 12624973)
			.addAttributeModifier(Attributes.LUCK, "CC5AF142-2BD2-4215-B636-2605AED11727", -1.0, AttributeModifier.Operation.ADDITION)
	);
	public static final Holder<MobEffect> SLOW_FALLING = register("slow_falling", new MobEffect(MobEffectCategory.BENEFICIAL, 15978425));
	public static final Holder<MobEffect> CONDUIT_POWER = register("conduit_power", new MobEffect(MobEffectCategory.BENEFICIAL, 1950417));
	public static final Holder<MobEffect> DOLPHINS_GRACE = register("dolphins_grace", new MobEffect(MobEffectCategory.BENEFICIAL, 8954814));
	public static final Holder<MobEffect> BAD_OMEN = register("bad_omen", new BadOmenMobEffect(MobEffectCategory.NEUTRAL, 745784));
	public static final Holder<MobEffect> HERO_OF_THE_VILLAGE = register("hero_of_the_village", new MobEffect(MobEffectCategory.BENEFICIAL, 4521796));
	public static final Holder<MobEffect> DARKNESS = register("darkness", new MobEffect(MobEffectCategory.HARMFUL, 2696993).setBlendDuration(22));

	private static Holder<MobEffect> register(String string, MobEffect mobEffect) {
		return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, new ResourceLocation(string), mobEffect);
	}

	public static Holder<MobEffect> bootstrap(Registry<MobEffect> registry) {
		return MOVEMENT_SPEED;
	}
}
