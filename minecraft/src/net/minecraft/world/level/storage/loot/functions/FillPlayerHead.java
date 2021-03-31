package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.authlib.GameProfile;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class FillPlayerHead extends LootItemConditionalFunction {
	private final LootContext.EntityTarget entityTarget;

	public FillPlayerHead(LootItemCondition[] lootItemConditions, LootContext.EntityTarget entityTarget) {
		super(lootItemConditions);
		this.entityTarget = entityTarget;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.FILL_PLAYER_HEAD;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return ImmutableSet.of(this.entityTarget.getParam());
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		if (itemStack.is(Items.PLAYER_HEAD)) {
			Entity entity = lootContext.getParamOrNull(this.entityTarget.getParam());
			if (entity instanceof Player) {
				GameProfile gameProfile = ((Player)entity).getGameProfile();
				itemStack.getOrCreateTag().put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), gameProfile));
			}
		}

		return itemStack;
	}

	public static LootItemConditionalFunction.Builder<?> fillPlayerHead(LootContext.EntityTarget entityTarget) {
		return simpleBuilder(lootItemConditions -> new FillPlayerHead(lootItemConditions, entityTarget));
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<FillPlayerHead> {
		public void serialize(JsonObject jsonObject, FillPlayerHead fillPlayerHead, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, fillPlayerHead, jsonSerializationContext);
			jsonObject.add("entity", jsonSerializationContext.serialize(fillPlayerHead.entityTarget));
		}

		public FillPlayerHead deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
			LootContext.EntityTarget entityTarget = GsonHelper.getAsObject(jsonObject, "entity", jsonDeserializationContext, LootContext.EntityTarget.class);
			return new FillPlayerHead(lootItemConditions, entityTarget);
		}
	}
}
