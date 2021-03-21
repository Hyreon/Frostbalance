package botmanager.frostbalance.resource.crafting;

import botmanager.frostbalance.resource.IngredientField;
import botmanager.frostbalance.resource.Inventory;
import botmanager.frostbalance.resource.ItemStack;
import botmanager.frostbalance.resource.ItemType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A crafting recipe for simple crafting, with costs and outputs.
 * This does not keep track of item classes.
 */
public class CraftingRecipe {

    String name;

    /**
     * Represents how long this recipe normally takes to complete.
     * Recipes do not yield part of their result; however,
     * progress can be saved and walked away from, so long as the ingredients remain.
     * A base turn of '0' means this can be done any time without spending a turn.
     */
    int baseTurns;

    Map<ItemType, Double> yields;

    /**
     * The items that must be consumed to make this.
     * Note that the same ingredient must be used for a given ingredient field.
     */
    Map<IngredientField, Double> costs;

    public CraftingRecipe(String displayName, Map<IngredientField, Double> costs, Map<ItemType, Double> yields, int turns) {
        this.name = displayName;
        this.costs = costs;
        this.yields = yields;
        this.baseTurns = turns;
    }

    public boolean craft(Inventory input, Inventory output) {
        return craft(input, output, new ArrayList<>(), 1, false);
    }

    /**
     * Attempts to craft the item.
     * @param preferredItems The items that will be used per ingredient.
     * @param minimumQuality The minimum quality that is accepted in an item stack.
     * @param acceptSubstitutions Whether higher quality crafting materials will be used.
     * @return Whether or not the item was crafted
     */
    public boolean craft(Inventory input, Inventory output, List<ItemType> preferredItems, int minimumQuality, boolean acceptSubstitutions) {
        //TODO use preferred items
        //TODO use minimum quality
        //TODO choose whether to accept substitutions of higher qualities
        Inventory inputFiction = input.makeFiction();
        for (IngredientField ingredient : costs.keySet()) {
            boolean itemFound = false;
            for (ItemType possibleItemType : ingredient.acceptableItems()) {
                ItemStack itemToTest = new ItemStack(possibleItemType, costs.get(ingredient), 1, true);
                if (input.hasItem(itemToTest)) {
                    itemFound = true;
                    inputFiction.removeItem(itemToTest);
                    break;
                }
            }

            if (!itemFound) {
                return false;
            }

        }

        for (ItemType key : yields.keySet()) {
            ItemStack newItem = new ItemStack(key, yields.get(key));
            output.addItem(newItem);
        }

        input.loadFiction(inputFiction);

        return true;
    }

    public int getBaseTurns() {
        return baseTurns;
    }

    @Override
    public String toString() {
        return name;
    }

}
