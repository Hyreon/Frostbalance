package botmanager.frostbalance.grid.building;

import botmanager.frostbalance.resource.crafting.CraftingRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorkshopType {

    String name;

    List<CraftingRecipe> validRecipes;

    public WorkshopType(String name, CraftingRecipe... validRecipes) {
        this.name = name;
        this.validRecipes = new ArrayList<>(Arrays.asList(validRecipes));
    }

    public boolean addRecipe(CraftingRecipe recipe) {
        return validRecipes.add(recipe);
    }

    public String getName() {
        return name;
    }

    public List<CraftingRecipe> getValidRecipes() {
        return new ArrayList<>(validRecipes);
    }

    @Override
    public String toString() {
        return name;
    }

}
