package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class SimpleCookingSerializer<T extends AbstractCookingRecipe> implements RecipeSerializer<T> {
	private final AbstractCookingRecipe.Factory<T> factory;
	private final Codec<T> codec;

	public SimpleCookingSerializer(AbstractCookingRecipe.Factory<T> factory, int i) {
		this.factory = factory;
		this.codec = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter(abstractCookingRecipe -> abstractCookingRecipe.group),
						CookingBookCategory.CODEC.fieldOf("category").orElse(CookingBookCategory.MISC).forGetter(abstractCookingRecipe -> abstractCookingRecipe.category),
						Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(abstractCookingRecipe -> abstractCookingRecipe.ingredient),
						BuiltInRegistries.ITEM
							.byNameCodec()
							.xmap(ItemStack::new, ItemStack::getItem)
							.fieldOf("result")
							.forGetter(abstractCookingRecipe -> abstractCookingRecipe.result),
						Codec.FLOAT.fieldOf("experience").orElse(0.0F).forGetter(abstractCookingRecipe -> abstractCookingRecipe.experience),
						Codec.INT.fieldOf("cookingtime").orElse(i).forGetter(abstractCookingRecipe -> abstractCookingRecipe.cookingTime)
					)
					.apply(instance, factory::create)
		);
	}

	@Override
	public Codec<T> codec() {
		return this.codec;
	}

	public T fromNetwork(FriendlyByteBuf friendlyByteBuf) {
		String string = friendlyByteBuf.readUtf();
		CookingBookCategory cookingBookCategory = friendlyByteBuf.readEnum(CookingBookCategory.class);
		Ingredient ingredient = Ingredient.fromNetwork(friendlyByteBuf);
		ItemStack itemStack = friendlyByteBuf.readItem();
		float f = friendlyByteBuf.readFloat();
		int i = friendlyByteBuf.readVarInt();
		return this.factory.create(string, cookingBookCategory, ingredient, itemStack, f, i);
	}

	public void toNetwork(FriendlyByteBuf friendlyByteBuf, T abstractCookingRecipe) {
		friendlyByteBuf.writeUtf(abstractCookingRecipe.group);
		friendlyByteBuf.writeEnum(abstractCookingRecipe.category());
		abstractCookingRecipe.ingredient.toNetwork(friendlyByteBuf);
		friendlyByteBuf.writeItem(abstractCookingRecipe.result);
		friendlyByteBuf.writeFloat(abstractCookingRecipe.experience);
		friendlyByteBuf.writeVarInt(abstractCookingRecipe.cookingTime);
	}

	public AbstractCookingRecipe create(String string, CookingBookCategory cookingBookCategory, Ingredient ingredient, ItemStack itemStack, float f, int i) {
		return this.factory.create(string, cookingBookCategory, ingredient, itemStack, f, i);
	}
}
