package com.fabio.services;

import com.fabio.domain.Recipe;
import com.fabio.repositories.RecipeRepository;
//import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ImageServiceImpl implements ImageService {

    private final RecipeRepository recipeRepository;

    @Autowired
    public ImageServiceImpl(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    @Override
    @Transactional
    public void saveImageFile(Long recipeId, MultipartFile file) {
        try{
            Recipe recipe = recipeRepository.findById(recipeId).get();
            Byte[] obj = new Byte[file.getBytes().length];
            int i = 0;
            for(byte b : file.getBytes()){
                obj[i++] = b;
            }
            recipe.setImage(obj);
            recipeRepository.save(recipe);
        }catch (IOException e){
            System.out.println(e.getMessage());
            e.printStackTrace();;
        }
    }

}
