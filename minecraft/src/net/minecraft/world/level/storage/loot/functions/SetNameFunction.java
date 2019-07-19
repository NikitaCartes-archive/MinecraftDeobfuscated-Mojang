package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SetNameFunction extends LootItemConditionalFunction {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Component name;
	@Nullable
	private final LootContext.EntityTarget resolutionContext;

	private SetNameFunction(LootItemCondition[] lootItemConditions, @Nullable Component component, @Nullable LootContext.EntityTarget entityTarget) {
		super(lootItemConditions);
		this.name = component;
		this.resolutionContext = entityTarget;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return this.resolutionContext != null ? ImmutableSet.of(this.resolutionContext.getParam()) : ImmutableSet.of();
	}

	public static UnaryOperator<Component> createResolver(LootContext lootContext, @Nullable LootContext.EntityTarget entityTarget) {
		if (entityTarget != null) {
			Entity entity = lootContext.getParamOrNull(entityTarget.getParam());
			if (entity != null) {
				CommandSourceStack commandSourceStack = entity.createCommandSourceStack().withPermission(2);
				return component -> {
					try {
						return ComponentUtils.updateForEntity(commandSourceStack, component, entity, 0);
					} catch (CommandSyntaxException var4) {
						LOGGER.warn("Failed to resolve text component", (Throwable)var4);
						return component;
					}
				};
			}
		}

		return component -> component;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		if (this.name != null) {
			itemStack.setHoverName((Component)createResolver(lootContext, this.resolutionContext).apply(this.name));
		}

		return itemStack;
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<SetNameFunction> {
		public Serializer() {
			super(new ResourceLocation("set_name"), SetNameFunction.class);
		}

		public void serialize(JsonObject jsonObject, SetNameFunction setNameFunction, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, setNameFunction, jsonSerializationContext);
			if (setNameFunction.name != null) {
				jsonObject.add("name", Component.Serializer.toJsonTree(setNameFunction.name));
			}

			if (setNameFunction.resolutionContext != null) {
				jsonObject.add("entity", jsonSerializationContext.serialize(setNameFunction.resolutionContext));
			}
		}

		public SetNameFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
			Component component = Component.Serializer.fromJson(jsonObject.get("name"));
			LootContext.EntityTarget entityTarget = GsonHelper.getAsObject(jsonObject, "entity", null, jsonDeserializationContext, LootContext.EntityTarget.class);
			return new SetNameFunction(lootItemConditions, component, entityTarget);
		}
	}
}
