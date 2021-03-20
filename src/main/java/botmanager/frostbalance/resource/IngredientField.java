package botmanager.frostbalance.resource;

import botmanager.frostbalance.resource.crafting.CraftingModifier;
import botmanager.frostbalance.resource.crafting.ItemModifier;

import java.util.*;

/**
 * For many crafting recipes, different ingredients can substitute the main one.
 * This affects the final result.
 * Different grades represent different qualities; for example, steel makes for better
 * armor than leather, and coal is a better burner fuel than leaves.
 */
public class IngredientField {

    private static HashMap<ItemType, IngredientField> simpleIngredients = new HashMap<>();

    String displayName;

    /**
     * The accepted items for this ingredient field.
     */
    List<ItemType> validItems;

    /**
     * The modifiers to apply, based on the item used.
     * If not mapped, no modifier is applied.
     */
    Map<ItemType, ItemModifier> outputEffects = new HashMap<>();

    /**
     * The affect this item has on the crafting recipe.
     * This may affect the speed, relative cost, relative yield,
     * or even add a new item altogether.
     */
    Map<ItemType, CraftingModifier> craftingEffects = new HashMap<>();

    public IngredientField(String displayName, ItemType... validTypes) {
        this.displayName = displayName;

        validItems = Arrays.asList(validTypes.clone());
    }

    public IngredientField(String displayName, Map<ItemType, ItemModifier> outputEffects, Map<ItemType, CraftingModifier> craftingEffects) {
        this.displayName = displayName;

        validItems = new ArrayList<>(outputEffects.keySet());

        this.outputEffects = outputEffects;
        this.craftingEffects = craftingEffects;
    }

    public Collection<ItemModifier> getPossibleItemModifiers() {
        return outputEffects.values();
    }

    public static IngredientField simple(ItemType type) {
        if (simpleIngredients.containsKey(type)) {
            return simpleIngredients.get(type);
        } else {
            IngredientField simpleIngredient = new IngredientField(type.getName(), type);
            simpleIngredients.put(type, simpleIngredient);
            return simpleIngredient;
        }
    }

    public List<ItemType> acceptableItems() {
        return validItems;
    }
}
