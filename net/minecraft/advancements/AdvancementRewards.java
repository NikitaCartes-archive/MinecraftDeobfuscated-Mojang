/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.commands.CommandFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

public class AdvancementRewards {
    public static final AdvancementRewards EMPTY = new AdvancementRewards(0, new ResourceLocation[0], new ResourceLocation[0], CommandFunction.CacheableFunction.NONE);
    private final int experience;
    private final ResourceLocation[] loot;
    private final ResourceLocation[] recipes;
    private final CommandFunction.CacheableFunction function;

    public AdvancementRewards(int i, ResourceLocation[] resourceLocations, ResourceLocation[] resourceLocations2, CommandFunction.CacheableFunction cacheableFunction) {
        this.experience = i;
        this.loot = resourceLocations;
        this.recipes = resourceLocations2;
        this.function = cacheableFunction;
    }

    public ResourceLocation[] getRecipes() {
        return this.recipes;
    }

    public void grant(ServerPlayer serverPlayer) {
        serverPlayer.giveExperiencePoints(this.experience);
        LootContext lootContext = new LootContext.Builder(serverPlayer.getLevel()).withParameter(LootContextParams.THIS_ENTITY, serverPlayer).withParameter(LootContextParams.ORIGIN, serverPlayer.position()).withRandom(serverPlayer.getRandom()).create(LootContextParamSets.ADVANCEMENT_REWARD);
        boolean bl = false;
        for (ResourceLocation resourceLocation : this.loot) {
            for (ItemStack itemStack : serverPlayer.server.getLootTables().get(resourceLocation).getRandomItems(lootContext)) {
                if (serverPlayer.addItem(itemStack)) {
                    serverPlayer.level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f, ((serverPlayer.getRandom().nextFloat() - serverPlayer.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f);
                    bl = true;
                    continue;
                }
                ItemEntity itemEntity = serverPlayer.drop(itemStack, false);
                if (itemEntity == null) continue;
                itemEntity.setNoPickUpDelay();
                itemEntity.setOwner(serverPlayer.getUUID());
            }
        }
        if (bl) {
            serverPlayer.containerMenu.broadcastChanges();
        }
        if (this.recipes.length > 0) {
            serverPlayer.awardRecipesByKey(this.recipes);
        }
        MinecraftServer minecraftServer = serverPlayer.server;
        this.function.get(minecraftServer.getFunctions()).ifPresent(commandFunction -> minecraftServer.getFunctions().execute((CommandFunction)commandFunction, serverPlayer.createCommandSourceStack().withSuppressedOutput().withPermission(2)));
    }

    public String toString() {
        return "AdvancementRewards{experience=" + this.experience + ", loot=" + Arrays.toString(this.loot) + ", recipes=" + Arrays.toString(this.recipes) + ", function=" + this.function + "}";
    }

    public JsonElement serializeToJson() {
        JsonArray jsonArray;
        if (this == EMPTY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        if (this.experience != 0) {
            jsonObject.addProperty("experience", this.experience);
        }
        if (this.loot.length > 0) {
            jsonArray = new JsonArray();
            for (ResourceLocation resourceLocation : this.loot) {
                jsonArray.add(resourceLocation.toString());
            }
            jsonObject.add("loot", jsonArray);
        }
        if (this.recipes.length > 0) {
            jsonArray = new JsonArray();
            for (ResourceLocation resourceLocation : this.recipes) {
                jsonArray.add(resourceLocation.toString());
            }
            jsonObject.add("recipes", jsonArray);
        }
        if (this.function.getId() != null) {
            jsonObject.addProperty("function", this.function.getId().toString());
        }
        return jsonObject;
    }

    public static AdvancementRewards deserialize(JsonObject jsonObject) throws JsonParseException {
        int i = GsonHelper.getAsInt(jsonObject, "experience", 0);
        JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "loot", new JsonArray());
        ResourceLocation[] resourceLocations = new ResourceLocation[jsonArray.size()];
        for (int j = 0; j < resourceLocations.length; ++j) {
            resourceLocations[j] = new ResourceLocation(GsonHelper.convertToString(jsonArray.get(j), "loot[" + j + "]"));
        }
        JsonArray jsonArray2 = GsonHelper.getAsJsonArray(jsonObject, "recipes", new JsonArray());
        ResourceLocation[] resourceLocations2 = new ResourceLocation[jsonArray2.size()];
        for (int k = 0; k < resourceLocations2.length; ++k) {
            resourceLocations2[k] = new ResourceLocation(GsonHelper.convertToString(jsonArray2.get(k), "recipes[" + k + "]"));
        }
        CommandFunction.CacheableFunction cacheableFunction = jsonObject.has("function") ? new CommandFunction.CacheableFunction(new ResourceLocation(GsonHelper.getAsString(jsonObject, "function"))) : CommandFunction.CacheableFunction.NONE;
        return new AdvancementRewards(i, resourceLocations, resourceLocations2, cacheableFunction);
    }

    public static class Builder {
        private int experience;
        private final List<ResourceLocation> loot = Lists.newArrayList();
        private final List<ResourceLocation> recipes = Lists.newArrayList();
        @Nullable
        private ResourceLocation function;

        public static Builder experience(int i) {
            return new Builder().addExperience(i);
        }

        public Builder addExperience(int i) {
            this.experience += i;
            return this;
        }

        public static Builder loot(ResourceLocation resourceLocation) {
            return new Builder().addLootTable(resourceLocation);
        }

        public Builder addLootTable(ResourceLocation resourceLocation) {
            this.loot.add(resourceLocation);
            return this;
        }

        public static Builder recipe(ResourceLocation resourceLocation) {
            return new Builder().addRecipe(resourceLocation);
        }

        public Builder addRecipe(ResourceLocation resourceLocation) {
            this.recipes.add(resourceLocation);
            return this;
        }

        public static Builder function(ResourceLocation resourceLocation) {
            return new Builder().runs(resourceLocation);
        }

        public Builder runs(ResourceLocation resourceLocation) {
            this.function = resourceLocation;
            return this;
        }

        public AdvancementRewards build() {
            return new AdvancementRewards(this.experience, this.loot.toArray(new ResourceLocation[0]), this.recipes.toArray(new ResourceLocation[0]), this.function == null ? CommandFunction.CacheableFunction.NONE : new CommandFunction.CacheableFunction(this.function));
        }
    }
}

