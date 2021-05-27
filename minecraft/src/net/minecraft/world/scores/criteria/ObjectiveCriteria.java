package net.minecraft.world.scores.criteria;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatType;

public class ObjectiveCriteria {
	private static final Map<String, ObjectiveCriteria> CUSTOM_CRITERIA = Maps.<String, ObjectiveCriteria>newHashMap();
	private static final Map<String, ObjectiveCriteria> CRITERIA_CACHE = Maps.<String, ObjectiveCriteria>newHashMap();
	public static final ObjectiveCriteria DUMMY = registerCustom("dummy");
	public static final ObjectiveCriteria TRIGGER = registerCustom("trigger");
	public static final ObjectiveCriteria DEATH_COUNT = registerCustom("deathCount");
	public static final ObjectiveCriteria KILL_COUNT_PLAYERS = registerCustom("playerKillCount");
	public static final ObjectiveCriteria KILL_COUNT_ALL = registerCustom("totalKillCount");
	public static final ObjectiveCriteria HEALTH = registerCustom("health", true, ObjectiveCriteria.RenderType.HEARTS);
	public static final ObjectiveCriteria FOOD = registerCustom("food", true, ObjectiveCriteria.RenderType.INTEGER);
	public static final ObjectiveCriteria AIR = registerCustom("air", true, ObjectiveCriteria.RenderType.INTEGER);
	public static final ObjectiveCriteria ARMOR = registerCustom("armor", true, ObjectiveCriteria.RenderType.INTEGER);
	public static final ObjectiveCriteria EXPERIENCE = registerCustom("xp", true, ObjectiveCriteria.RenderType.INTEGER);
	public static final ObjectiveCriteria LEVEL = registerCustom("level", true, ObjectiveCriteria.RenderType.INTEGER);
	public static final ObjectiveCriteria[] TEAM_KILL = new ObjectiveCriteria[]{
		registerCustom("teamkill." + ChatFormatting.BLACK.getName()),
		registerCustom("teamkill." + ChatFormatting.DARK_BLUE.getName()),
		registerCustom("teamkill." + ChatFormatting.DARK_GREEN.getName()),
		registerCustom("teamkill." + ChatFormatting.DARK_AQUA.getName()),
		registerCustom("teamkill." + ChatFormatting.DARK_RED.getName()),
		registerCustom("teamkill." + ChatFormatting.DARK_PURPLE.getName()),
		registerCustom("teamkill." + ChatFormatting.GOLD.getName()),
		registerCustom("teamkill." + ChatFormatting.GRAY.getName()),
		registerCustom("teamkill." + ChatFormatting.DARK_GRAY.getName()),
		registerCustom("teamkill." + ChatFormatting.BLUE.getName()),
		registerCustom("teamkill." + ChatFormatting.GREEN.getName()),
		registerCustom("teamkill." + ChatFormatting.AQUA.getName()),
		registerCustom("teamkill." + ChatFormatting.RED.getName()),
		registerCustom("teamkill." + ChatFormatting.LIGHT_PURPLE.getName()),
		registerCustom("teamkill." + ChatFormatting.YELLOW.getName()),
		registerCustom("teamkill." + ChatFormatting.WHITE.getName())
	};
	public static final ObjectiveCriteria[] KILLED_BY_TEAM = new ObjectiveCriteria[]{
		registerCustom("killedByTeam." + ChatFormatting.BLACK.getName()),
		registerCustom("killedByTeam." + ChatFormatting.DARK_BLUE.getName()),
		registerCustom("killedByTeam." + ChatFormatting.DARK_GREEN.getName()),
		registerCustom("killedByTeam." + ChatFormatting.DARK_AQUA.getName()),
		registerCustom("killedByTeam." + ChatFormatting.DARK_RED.getName()),
		registerCustom("killedByTeam." + ChatFormatting.DARK_PURPLE.getName()),
		registerCustom("killedByTeam." + ChatFormatting.GOLD.getName()),
		registerCustom("killedByTeam." + ChatFormatting.GRAY.getName()),
		registerCustom("killedByTeam." + ChatFormatting.DARK_GRAY.getName()),
		registerCustom("killedByTeam." + ChatFormatting.BLUE.getName()),
		registerCustom("killedByTeam." + ChatFormatting.GREEN.getName()),
		registerCustom("killedByTeam." + ChatFormatting.AQUA.getName()),
		registerCustom("killedByTeam." + ChatFormatting.RED.getName()),
		registerCustom("killedByTeam." + ChatFormatting.LIGHT_PURPLE.getName()),
		registerCustom("killedByTeam." + ChatFormatting.YELLOW.getName()),
		registerCustom("killedByTeam." + ChatFormatting.WHITE.getName())
	};
	private final String name;
	private final boolean readOnly;
	private final ObjectiveCriteria.RenderType renderType;

	private static ObjectiveCriteria registerCustom(String string, boolean bl, ObjectiveCriteria.RenderType renderType) {
		ObjectiveCriteria objectiveCriteria = new ObjectiveCriteria(string, bl, renderType);
		CUSTOM_CRITERIA.put(string, objectiveCriteria);
		return objectiveCriteria;
	}

	private static ObjectiveCriteria registerCustom(String string) {
		return registerCustom(string, false, ObjectiveCriteria.RenderType.INTEGER);
	}

	protected ObjectiveCriteria(String string) {
		this(string, false, ObjectiveCriteria.RenderType.INTEGER);
	}

	protected ObjectiveCriteria(String string, boolean bl, ObjectiveCriteria.RenderType renderType) {
		this.name = string;
		this.readOnly = bl;
		this.renderType = renderType;
		CRITERIA_CACHE.put(string, this);
	}

	public static Set<String> getCustomCriteriaNames() {
		return ImmutableSet.copyOf(CUSTOM_CRITERIA.keySet());
	}

	public static Optional<ObjectiveCriteria> byName(String string) {
		ObjectiveCriteria objectiveCriteria = (ObjectiveCriteria)CRITERIA_CACHE.get(string);
		if (objectiveCriteria != null) {
			return Optional.of(objectiveCriteria);
		} else {
			int i = string.indexOf(58);
			return i < 0
				? Optional.empty()
				: Registry.STAT_TYPE
					.getOptional(ResourceLocation.of(string.substring(0, i), '.'))
					.flatMap(statType -> getStat(statType, ResourceLocation.of(string.substring(i + 1), '.')));
		}
	}

	private static <T> Optional<ObjectiveCriteria> getStat(StatType<T> statType, ResourceLocation resourceLocation) {
		return statType.getRegistry().getOptional(resourceLocation).map(statType::get);
	}

	public String getName() {
		return this.name;
	}

	public boolean isReadOnly() {
		return this.readOnly;
	}

	public ObjectiveCriteria.RenderType getDefaultRenderType() {
		return this.renderType;
	}

	public static enum RenderType {
		INTEGER("integer"),
		HEARTS("hearts");

		private final String id;
		private static final Map<String, ObjectiveCriteria.RenderType> BY_ID;

		private RenderType(String string2) {
			this.id = string2;
		}

		public String getId() {
			return this.id;
		}

		public static ObjectiveCriteria.RenderType byId(String string) {
			return (ObjectiveCriteria.RenderType)BY_ID.getOrDefault(string, INTEGER);
		}

		static {
			Builder<String, ObjectiveCriteria.RenderType> builder = ImmutableMap.builder();

			for (ObjectiveCriteria.RenderType renderType : values()) {
				builder.put(renderType.id, renderType);
			}

			BY_ID = builder.build();
		}
	}
}
