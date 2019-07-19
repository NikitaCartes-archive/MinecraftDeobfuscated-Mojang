/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components.toasts;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

@Environment(value=EnvType.CLIENT)
public class RecipeToast
implements Toast {
    private final List<Recipe<?>> recipes = Lists.newArrayList();
    private long lastChanged;
    private boolean changed;

    public RecipeToast(Recipe<?> recipe) {
        this.recipes.add(recipe);
    }

    @Override
    public Toast.Visibility render(ToastComponent toastComponent, long l) {
        if (this.changed) {
            this.lastChanged = l;
            this.changed = false;
        }
        if (this.recipes.isEmpty()) {
            return Toast.Visibility.HIDE;
        }
        toastComponent.getMinecraft().getTextureManager().bind(TEXTURE);
        GlStateManager.color3f(1.0f, 1.0f, 1.0f);
        toastComponent.blit(0, 0, 0, 32, 160, 32);
        toastComponent.getMinecraft().font.draw(I18n.get("recipe.toast.title", new Object[0]), 30.0f, 7.0f, -11534256);
        toastComponent.getMinecraft().font.draw(I18n.get("recipe.toast.description", new Object[0]), 30.0f, 18.0f, -16777216);
        Lighting.turnOnGui();
        Recipe<?> recipe = this.recipes.get((int)(l / (5000L / (long)this.recipes.size()) % (long)this.recipes.size()));
        ItemStack itemStack = recipe.getToastSymbol();
        GlStateManager.pushMatrix();
        GlStateManager.scalef(0.6f, 0.6f, 1.0f);
        toastComponent.getMinecraft().getItemRenderer().renderAndDecorateItem(null, itemStack, 3, 3);
        GlStateManager.popMatrix();
        toastComponent.getMinecraft().getItemRenderer().renderAndDecorateItem(null, recipe.getResultItem(), 8, 8);
        return l - this.lastChanged >= 5000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    public void addItem(Recipe<?> recipe) {
        if (this.recipes.add(recipe)) {
            this.changed = true;
        }
    }

    public static void addOrUpdate(ToastComponent toastComponent, Recipe<?> recipe) {
        RecipeToast recipeToast = toastComponent.getToast(RecipeToast.class, NO_TOKEN);
        if (recipeToast == null) {
            toastComponent.addToast(new RecipeToast(recipe));
        } else {
            recipeToast.addItem(recipe);
        }
    }
}

