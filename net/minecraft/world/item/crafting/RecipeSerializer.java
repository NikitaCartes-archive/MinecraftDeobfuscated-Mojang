/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.ArmorDyeRecipe;
import net.minecraft.world.item.crafting.BannerDuplicateRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.BookCloningRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.DecoratedPotRecipe;
import net.minecraft.world.item.crafting.FireworkRocketRecipe;
import net.minecraft.world.item.crafting.FireworkStarFadeRecipe;
import net.minecraft.world.item.crafting.FireworkStarRecipe;
import net.minecraft.world.item.crafting.LegacyUpgradeRecipe;
import net.minecraft.world.item.crafting.MapCloningRecipe;
import net.minecraft.world.item.crafting.MapExtendingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.ShieldDecorationRecipe;
import net.minecraft.world.item.crafting.ShulkerBoxColoring;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.SuspiciousStewRecipe;
import net.minecraft.world.item.crafting.TippedArrowRecipe;

public interface RecipeSerializer<T extends Recipe<?>> {
    public static final RecipeSerializer<ShapedRecipe> SHAPED_RECIPE = RecipeSerializer.register("crafting_shaped", new ShapedRecipe.Serializer());
    public static final RecipeSerializer<ShapelessRecipe> SHAPELESS_RECIPE = RecipeSerializer.register("crafting_shapeless", new ShapelessRecipe.Serializer());
    public static final RecipeSerializer<ArmorDyeRecipe> ARMOR_DYE = RecipeSerializer.register("crafting_special_armordye", new SimpleCraftingRecipeSerializer<ArmorDyeRecipe>(ArmorDyeRecipe::new));
    public static final RecipeSerializer<BookCloningRecipe> BOOK_CLONING = RecipeSerializer.register("crafting_special_bookcloning", new SimpleCraftingRecipeSerializer<BookCloningRecipe>(BookCloningRecipe::new));
    public static final RecipeSerializer<MapCloningRecipe> MAP_CLONING = RecipeSerializer.register("crafting_special_mapcloning", new SimpleCraftingRecipeSerializer<MapCloningRecipe>(MapCloningRecipe::new));
    public static final RecipeSerializer<MapExtendingRecipe> MAP_EXTENDING = RecipeSerializer.register("crafting_special_mapextending", new SimpleCraftingRecipeSerializer<MapExtendingRecipe>(MapExtendingRecipe::new));
    public static final RecipeSerializer<FireworkRocketRecipe> FIREWORK_ROCKET = RecipeSerializer.register("crafting_special_firework_rocket", new SimpleCraftingRecipeSerializer<FireworkRocketRecipe>(FireworkRocketRecipe::new));
    public static final RecipeSerializer<FireworkStarRecipe> FIREWORK_STAR = RecipeSerializer.register("crafting_special_firework_star", new SimpleCraftingRecipeSerializer<FireworkStarRecipe>(FireworkStarRecipe::new));
    public static final RecipeSerializer<FireworkStarFadeRecipe> FIREWORK_STAR_FADE = RecipeSerializer.register("crafting_special_firework_star_fade", new SimpleCraftingRecipeSerializer<FireworkStarFadeRecipe>(FireworkStarFadeRecipe::new));
    public static final RecipeSerializer<TippedArrowRecipe> TIPPED_ARROW = RecipeSerializer.register("crafting_special_tippedarrow", new SimpleCraftingRecipeSerializer<TippedArrowRecipe>(TippedArrowRecipe::new));
    public static final RecipeSerializer<BannerDuplicateRecipe> BANNER_DUPLICATE = RecipeSerializer.register("crafting_special_bannerduplicate", new SimpleCraftingRecipeSerializer<BannerDuplicateRecipe>(BannerDuplicateRecipe::new));
    public static final RecipeSerializer<ShieldDecorationRecipe> SHIELD_DECORATION = RecipeSerializer.register("crafting_special_shielddecoration", new SimpleCraftingRecipeSerializer<ShieldDecorationRecipe>(ShieldDecorationRecipe::new));
    public static final RecipeSerializer<ShulkerBoxColoring> SHULKER_BOX_COLORING = RecipeSerializer.register("crafting_special_shulkerboxcoloring", new SimpleCraftingRecipeSerializer<ShulkerBoxColoring>(ShulkerBoxColoring::new));
    public static final RecipeSerializer<SuspiciousStewRecipe> SUSPICIOUS_STEW = RecipeSerializer.register("crafting_special_suspiciousstew", new SimpleCraftingRecipeSerializer<SuspiciousStewRecipe>(SuspiciousStewRecipe::new));
    public static final RecipeSerializer<RepairItemRecipe> REPAIR_ITEM = RecipeSerializer.register("crafting_special_repairitem", new SimpleCraftingRecipeSerializer<RepairItemRecipe>(RepairItemRecipe::new));
    public static final RecipeSerializer<SmeltingRecipe> SMELTING_RECIPE = RecipeSerializer.register("smelting", new SimpleCookingSerializer<SmeltingRecipe>(SmeltingRecipe::new, 200));
    public static final RecipeSerializer<BlastingRecipe> BLASTING_RECIPE = RecipeSerializer.register("blasting", new SimpleCookingSerializer<BlastingRecipe>(BlastingRecipe::new, 100));
    public static final RecipeSerializer<SmokingRecipe> SMOKING_RECIPE = RecipeSerializer.register("smoking", new SimpleCookingSerializer<SmokingRecipe>(SmokingRecipe::new, 100));
    public static final RecipeSerializer<CampfireCookingRecipe> CAMPFIRE_COOKING_RECIPE = RecipeSerializer.register("campfire_cooking", new SimpleCookingSerializer<CampfireCookingRecipe>(CampfireCookingRecipe::new, 100));
    public static final RecipeSerializer<StonecutterRecipe> STONECUTTER = RecipeSerializer.register("stonecutting", new SingleItemRecipe.Serializer<StonecutterRecipe>(StonecutterRecipe::new));
    public static final RecipeSerializer<LegacyUpgradeRecipe> SMITHING = RecipeSerializer.register("smithing", new LegacyUpgradeRecipe.Serializer());
    public static final RecipeSerializer<SmithingTransformRecipe> SMITHING_TRANSFORM = RecipeSerializer.register("smithing_transform", new SmithingTransformRecipe.Serializer());
    public static final RecipeSerializer<SmithingTrimRecipe> SMITHING_TRIM = RecipeSerializer.register("smithing_trim", new SmithingTrimRecipe.Serializer());
    public static final RecipeSerializer<DecoratedPotRecipe> DECORATED_POT_RECIPE = RecipeSerializer.register("crafting_decorated_pot", new SimpleCraftingRecipeSerializer<DecoratedPotRecipe>(DecoratedPotRecipe::new));

    public T fromJson(ResourceLocation var1, JsonObject var2);

    public T fromNetwork(ResourceLocation var1, FriendlyByteBuf var2);

    public void toNetwork(FriendlyByteBuf var1, T var2);

    public static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(String string, S recipeSerializer) {
        return (S)Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, string, recipeSerializer);
    }
}

