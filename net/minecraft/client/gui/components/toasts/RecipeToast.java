/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components.toasts;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
    public Toast.Visibility render(PoseStack poseStack, ToastComponent toastComponent, long l) {
        if (this.changed) {
            this.lastChanged = l;
            this.changed = false;
        }
        if (this.recipes.isEmpty()) {
            return Toast.Visibility.HIDE;
        }
        toastComponent.getMinecraft().getTextureManager().bind(TEXTURE);
        RenderSystem.color3f(1.0f, 1.0f, 1.0f);
        toastComponent.blit(poseStack, 0, 0, 0, 32, this.width(), this.height());
        toastComponent.getMinecraft().font.draw(poseStack, I18n.get("recipe.toast.title", new Object[0]), 30.0f, 7.0f, -11534256);
        toastComponent.getMinecraft().font.draw(poseStack, I18n.get("recipe.toast.description", new Object[0]), 30.0f, 18.0f, -16777216);
        Recipe<?> recipe = this.recipes.get((int)(l / Math.max(1L, 5000L / (long)this.recipes.size()) % (long)this.recipes.size()));
        ItemStack itemStack = recipe.getToastSymbol();
        RenderSystem.pushMatrix();
        RenderSystem.scalef(0.6f, 0.6f, 1.0f);
        toastComponent.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(itemStack, 3, 3);
        RenderSystem.popMatrix();
        toastComponent.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(recipe.getResultItem(), 8, 8);
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

