package com.fabio.services;

import com.fabio.commands.IngredientCommand;
import com.fabio.converters.IngredientCommandToIngredient;
import com.fabio.converters.IngredientToIngredientCommand;
import com.fabio.domain.Ingredient;
import com.fabio.domain.Recipe;
import com.fabio.exceptions.NotFoundException;
import com.fabio.repositories.RecipeRepository;
import com.fabio.repositories.UnitOfMeasureRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Created by jt on 6/28/17.
 */
@Slf4j
@Service
public class IngredientServiceImpl implements IngredientService {

    private final IngredientToIngredientCommand ingredientToIngredientCommand;
    private final IngredientCommandToIngredient ingredientCommandToIngredient;
    private final RecipeRepository recipeRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;

    public IngredientServiceImpl(IngredientToIngredientCommand ingredientToIngredientCommand,
                                 IngredientCommandToIngredient ingredientCommandToIngredient,
                                 RecipeRepository recipeRepository, UnitOfMeasureRepository unitOfMeasureRepository) {
        this.ingredientToIngredientCommand = ingredientToIngredientCommand;
        this.ingredientCommandToIngredient = ingredientCommandToIngredient;
        this.recipeRepository = recipeRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
    }

    @Override
    public IngredientCommand findByRecipeIdAndIngredientId(Long recipeId, Long ingredientId) {

        Optional<Recipe> recipeOptional = recipeRepository.findById(recipeId);

        if (!recipeOptional.isPresent()){
            throw new NotFoundException("Recipe Not Found. For ID value: "+recipeId);
        }

        Recipe recipe = recipeOptional.get();

        Optional<IngredientCommand> ingredientCommandOptional = recipe.getIngredients().stream()
                .filter(ingredient -> ingredient.getId().equals(ingredientId))
                .map( ingredient -> ingredientToIngredientCommand.convert(ingredient)).findFirst();

        if(!ingredientCommandOptional.isPresent()){
            throw new NotFoundException("Ingredient Not Found. For ID value: "+ingredientId);
        }

        return ingredientCommandOptional.get();
    }

    @Override
    @Transactional
    public IngredientCommand saveIngredientCommand(IngredientCommand command) {

        System.out.println("command.getRecipeId = "+ command.getRecipeId());
        Ingredient ingr = ingredientCommandToIngredient.convert(command);
        System.out.println("ingredient. exists = "+ (ingr!=null));
        System.out.println("ingredient.getRecipe. exists = "+ (ingr.getRecipe()!=null));
        System.out.println("ingredient.getId = "+ ingr.getId());

        Optional<Recipe> recipeOptional = recipeRepository.findById(command.getRecipeId());

        if(!recipeOptional.isPresent()){

            //todo toss error if not found!
            System.out.println("Recipe not found for id: " + command.getRecipeId());
            return new IngredientCommand();
        } else {
            Recipe recipe = recipeOptional.get();

            Optional<Ingredient> ingredientOptional = recipe
                    .getIngredients()
                    .stream()
                    .filter(ingredient -> ingredient.getId().equals(command.getId()))
                    .findFirst();

            if(ingredientOptional.isPresent()){
                Ingredient ingredientFound = ingredientOptional.get();
                ingredientFound.setDescription(command.getDescription());
                ingredientFound.setAmount(command.getAmount());
                ingredientFound.setUom(unitOfMeasureRepository
                        .findById(command.getUom().getId())
                        .orElseThrow(() -> new RuntimeException("UOM NOT FOUND"))); //todo address this
            } else {
                //add new Ingredient
                Ingredient ingredient = ingredientCommandToIngredient.convert(command);
                ingredient.setRecipe(recipe);
                recipe.addIngredient(ingredient);
            }

            Recipe savedRecipe = recipeRepository.save(recipe);

            Optional<Ingredient> savedIngredientOptional = savedRecipe.getIngredients().stream()
                    .filter(recingr -> recingr.getId().equals(command.getId())).findFirst();
            if(!savedIngredientOptional.isPresent()){
                savedIngredientOptional = savedRecipe.getIngredients().stream()
                        .filter(recingr -> recingr.getDescription().equals(command.getDescription()))
                        .filter(recingr -> recingr.getAmount().equals(command.getAmount()))
                        .filter(recingr -> recingr.getUom().getId().equals(command.getUom().getId()))
                        .findFirst();
            }

            //to do check for fail
            return  ingredientToIngredientCommand.convert(savedIngredientOptional.get());
        }

    }


    @Override
    public void deleteById(Long recipeId, Long ingredientId) throws NoSuchElementException {
        Optional<Recipe> optRecipe = recipeRepository.findById(recipeId);
        Recipe recipe = optRecipe.get();
        Optional<Ingredient> optIngredient = recipe.getIngredients().stream()
                .filter(ingr -> ingr.getId().equals(ingredientId))
                .findFirst();
        Ingredient ingredient = optIngredient.get();
        ingredient.setRecipe(null);
        recipe.getIngredients().remove(ingredient);
        recipeRepository.save(recipe);
    }
}
