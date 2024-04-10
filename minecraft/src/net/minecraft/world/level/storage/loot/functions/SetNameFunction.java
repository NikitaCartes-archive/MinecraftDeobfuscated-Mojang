package net.minecraft.world.level.storage.loot.functions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class SetNameFunction extends LootItemConditionalFunction {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final MapCodec<SetNameFunction> CODEC = RecordCodecBuilder.mapCodec(
		instance -> commonFields(instance)
				.<Optional<Component>, Optional<LootContext.EntityTarget>, SetNameFunction.Target>and(
					instance.group(
						ComponentSerialization.CODEC.optionalFieldOf("name").forGetter(setNameFunction -> setNameFunction.name),
						LootContext.EntityTarget.CODEC.optionalFieldOf("entity").forGetter(setNameFunction -> setNameFunction.resolutionContext),
						SetNameFunction.Target.CODEC.optionalFieldOf("target", SetNameFunction.Target.CUSTOM_NAME).forGetter(setNameFunction -> setNameFunction.target)
					)
				)
				.apply(instance, SetNameFunction::new)
	);
	private final Optional<Component> name;
	private final Optional<LootContext.EntityTarget> resolutionContext;
	private final SetNameFunction.Target target;

	private SetNameFunction(
		List<LootItemCondition> list, Optional<Component> optional, Optional<LootContext.EntityTarget> optional2, SetNameFunction.Target target
	) {
		super(list);
		this.name = optional;
		this.resolutionContext = optional2;
		this.target = target;
	}

	@Override
	public LootItemFunctionType<SetNameFunction> getType() {
		return LootItemFunctions.SET_NAME;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return (Set<LootContextParam<?>>)this.resolutionContext.map(entityTarget -> Set.of(entityTarget.getParam())).orElse(Set.of());
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
		this.name
			.ifPresent(
				component -> itemStack.set(
						this.target.component(), (Component)createResolver(lootContext, (LootContext.EntityTarget)this.resolutionContext.orElse(null)).apply(component)
					)
			);
		return itemStack;
	}

	public static LootItemConditionalFunction.Builder<?> setName(Component component, SetNameFunction.Target target) {
		return simpleBuilder(list -> new SetNameFunction(list, Optional.of(component), Optional.empty(), target));
	}

	public static LootItemConditionalFunction.Builder<?> setName(Component component, SetNameFunction.Target target, LootContext.EntityTarget entityTarget) {
		return simpleBuilder(list -> new SetNameFunction(list, Optional.of(component), Optional.of(entityTarget), target));
	}

	public static enum Target implements StringRepresentable {
		CUSTOM_NAME("custom_name"),
		ITEM_NAME("item_name");

		public static final Codec<SetNameFunction.Target> CODEC = StringRepresentable.fromEnum(SetNameFunction.Target::values);
		private final String name;

		private Target(final String string2) {
			this.name = string2;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public DataComponentType<Component> component() {
			return switch (this) {
				case CUSTOM_NAME -> DataComponents.CUSTOM_NAME;
				case ITEM_NAME -> DataComponents.ITEM_NAME;
			};
		}
	}
}
