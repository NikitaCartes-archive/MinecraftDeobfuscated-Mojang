/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.player;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import java.util.BitSet;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

public class StackedContents {
    private static final int EMPTY = 0;
    public final Int2IntMap contents = new Int2IntOpenHashMap();

    public void accountSimpleStack(ItemStack itemStack) {
        if (!(itemStack.isDamaged() || itemStack.isEnchanted() || itemStack.hasCustomHoverName())) {
            this.accountStack(itemStack);
        }
    }

    public void accountStack(ItemStack itemStack) {
        this.accountStack(itemStack, 64);
    }

    public void accountStack(ItemStack itemStack, int i) {
        if (!itemStack.isEmpty()) {
            int j = StackedContents.getStackingIndex(itemStack);
            int k = Math.min(i, itemStack.getCount());
            this.put(j, k);
        }
    }

    public static int getStackingIndex(ItemStack itemStack) {
        return BuiltInRegistries.ITEM.getId(itemStack.getItem());
    }

    boolean has(int i) {
        return this.contents.get(i) > 0;
    }

    int take(int i, int j) {
        int k = this.contents.get(i);
        if (k >= j) {
            this.contents.put(i, k - j);
            return i;
        }
        return 0;
    }

    void put(int i, int j) {
        this.contents.put(i, this.contents.get(i) + j);
    }

    public boolean canCraft(Recipe<?> recipe, @Nullable IntList intList) {
        return this.canCraft(recipe, intList, 1);
    }

    public boolean canCraft(Recipe<?> recipe, @Nullable IntList intList, int i) {
        return new RecipePicker(recipe).tryPick(i, intList);
    }

    public int getBiggestCraftableStack(Recipe<?> recipe, @Nullable IntList intList) {
        return this.getBiggestCraftableStack(recipe, Integer.MAX_VALUE, intList);
    }

    public int getBiggestCraftableStack(Recipe<?> recipe, int i, @Nullable IntList intList) {
        return new RecipePicker(recipe).tryPickAll(i, intList);
    }

    public static ItemStack fromStackingIndex(int i) {
        if (i == 0) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(Item.byId(i));
    }

    public void clear() {
        this.contents.clear();
    }

    class RecipePicker {
        private final Recipe<?> recipe;
        private final List<Ingredient> ingredients = Lists.newArrayList();
        private final int ingredientCount;
        private final int[] items;
        private final int itemCount;
        private final BitSet data;
        private final IntList path = new IntArrayList();

        public RecipePicker(Recipe<?> recipe) {
            this.recipe = recipe;
            this.ingredients.addAll(recipe.getIngredients());
            this.ingredients.removeIf(Ingredient::isEmpty);
            this.ingredientCount = this.ingredients.size();
            this.items = this.getUniqueAvailableIngredientItems();
            this.itemCount = this.items.length;
            this.data = new BitSet(this.ingredientCount + this.itemCount + this.ingredientCount + this.ingredientCount * this.itemCount);
            for (int i = 0; i < this.ingredients.size(); ++i) {
                IntList intList = this.ingredients.get(i).getStackingIds();
                for (int j = 0; j < this.itemCount; ++j) {
                    if (!intList.contains(this.items[j])) continue;
                    this.data.set(this.getIndex(true, j, i));
                }
            }
        }

        public boolean tryPick(int i, @Nullable IntList intList) {
            boolean bl2;
            if (i <= 0) {
                return true;
            }
            int j = 0;
            while (this.dfs(i)) {
                StackedContents.this.take(this.items[this.path.getInt(0)], i);
                int k = this.path.size() - 1;
                this.setSatisfied(this.path.getInt(k));
                for (int l = 0; l < k; ++l) {
                    this.toggleResidual((l & 1) == 0, this.path.get(l), this.path.get(l + 1));
                }
                this.path.clear();
                this.data.clear(0, this.ingredientCount + this.itemCount);
                ++j;
            }
            boolean bl = j == this.ingredientCount;
            boolean bl3 = bl2 = bl && intList != null;
            if (bl2) {
                intList.clear();
            }
            this.data.clear(0, this.ingredientCount + this.itemCount + this.ingredientCount);
            int m = 0;
            NonNullList<Ingredient> list = this.recipe.getIngredients();
            for (int n = 0; n < list.size(); ++n) {
                if (bl2 && ((Ingredient)list.get(n)).isEmpty()) {
                    intList.add(0);
                    continue;
                }
                for (int o = 0; o < this.itemCount; ++o) {
                    if (!this.hasResidual(false, m, o)) continue;
                    this.toggleResidual(true, o, m);
                    StackedContents.this.put(this.items[o], i);
                    if (!bl2) continue;
                    intList.add(this.items[o]);
                }
                ++m;
            }
            return bl;
        }

        private int[] getUniqueAvailableIngredientItems() {
            IntAVLTreeSet intCollection = new IntAVLTreeSet();
            for (Ingredient ingredient : this.ingredients) {
                intCollection.addAll(ingredient.getStackingIds());
            }
            IntIterator intIterator = intCollection.iterator();
            while (intIterator.hasNext()) {
                if (StackedContents.this.has(intIterator.nextInt())) continue;
                intIterator.remove();
            }
            return intCollection.toIntArray();
        }

        private boolean dfs(int i) {
            int j = this.itemCount;
            for (int k = 0; k < j; ++k) {
                if (StackedContents.this.contents.get(this.items[k]) < i) continue;
                this.visit(false, k);
                while (!this.path.isEmpty()) {
                    int o;
                    int l = this.path.size();
                    boolean bl = (l & 1) == 1;
                    int m = this.path.getInt(l - 1);
                    if (!bl && !this.isSatisfied(m)) break;
                    int n = bl ? this.ingredientCount : j;
                    for (o = 0; o < n; ++o) {
                        if (this.hasVisited(bl, o) || !this.hasConnection(bl, m, o) || !this.hasResidual(bl, m, o)) continue;
                        this.visit(bl, o);
                        break;
                    }
                    if ((o = this.path.size()) != l) continue;
                    this.path.removeInt(o - 1);
                }
                if (this.path.isEmpty()) continue;
                return true;
            }
            return false;
        }

        private boolean isSatisfied(int i) {
            return this.data.get(this.getSatisfiedIndex(i));
        }

        private void setSatisfied(int i) {
            this.data.set(this.getSatisfiedIndex(i));
        }

        private int getSatisfiedIndex(int i) {
            return this.ingredientCount + this.itemCount + i;
        }

        private boolean hasConnection(boolean bl, int i, int j) {
            return this.data.get(this.getIndex(bl, i, j));
        }

        private boolean hasResidual(boolean bl, int i, int j) {
            return bl != this.data.get(1 + this.getIndex(bl, i, j));
        }

        private void toggleResidual(boolean bl, int i, int j) {
            this.data.flip(1 + this.getIndex(bl, i, j));
        }

        private int getIndex(boolean bl, int i, int j) {
            int k = bl ? i * this.ingredientCount + j : j * this.ingredientCount + i;
            return this.ingredientCount + this.itemCount + this.ingredientCount + 2 * k;
        }

        private void visit(boolean bl, int i) {
            this.data.set(this.getVisitedIndex(bl, i));
            this.path.add(i);
        }

        private boolean hasVisited(boolean bl, int i) {
            return this.data.get(this.getVisitedIndex(bl, i));
        }

        private int getVisitedIndex(boolean bl, int i) {
            return (bl ? 0 : this.ingredientCount) + i;
        }

        public int tryPickAll(int i, @Nullable IntList intList) {
            int l;
            int j = 0;
            int k = Math.min(i, this.getMinIngredientCount()) + 1;
            while (true) {
                if (this.tryPick(l = (j + k) / 2, null)) {
                    if (k - j <= 1) break;
                    j = l;
                    continue;
                }
                k = l;
            }
            if (l > 0) {
                this.tryPick(l, intList);
            }
            return l;
        }

        private int getMinIngredientCount() {
            int i = Integer.MAX_VALUE;
            for (Ingredient ingredient : this.ingredients) {
                int j = 0;
                IntListIterator intListIterator = ingredient.getStackingIds().iterator();
                while (intListIterator.hasNext()) {
                    int k = (Integer)intListIterator.next();
                    j = Math.max(j, StackedContents.this.contents.get(k));
                }
                if (i <= 0) continue;
                i = Math.min(i, j);
            }
            return i;
        }
    }
}

