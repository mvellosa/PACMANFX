/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphicEngine;

import java.util.HashMap;
import javafx.scene.image.Image;

/**
 * Classe auxiliar criada para salvar as imagens que serão utilizadas
 * @author matheus
 */
public class ImageStorage {
    HashMap<String, Image> images;
    
    public ImageStorage(){
        images = new HashMap<>();
        images.put("blinky", new Image(resourcePath("blinky.png")));
        images.put("cherry", new Image(resourcePath("cherry.png")));
        images.put("clyde", new Image(resourcePath("clyde.png")));
        images.put("consumableghost", new Image(resourcePath("consumableghost.png")));
        images.put("emptyheart", new Image(resourcePath("emptyheart.png")));
        images.put("heart", new Image(resourcePath("heart.png")));
        images.put("inky", new Image(resourcePath("inky.png")));
        images.put("menubg", new Image(resourcePath("menubg.png")));
        images.put("orange", new Image(resourcePath("orange.png")));
        images.put("pacdot", new Image(resourcePath("pacdot.png")));
        images.put("pacman", new Image(resourcePath("pacman.png")));
        images.put("pill", new Image(resourcePath("pill.png")));
        images.put("pinky", new Image(resourcePath("pinky.png")));
        images.put("strawberry", new Image(resourcePath("strawberry.png")));
        images.put("wall", new Image(resourcePath("wall.png")));
    }
    
    /**
     * acha o caminho de um arquivo baseado em seu nome
     * @param fileName nome do arquivo sendo procurado
     * @return caminho do arquivo
     */
    public static String resourcePath(String fileName){
        return PacmanGUI.class.getResource(fileName).toString();    
    }
    
    /**
     * devolve a imagem salva de mesmo nome da passada por parametro
     * @param imageName nome do arquivo da imagem (sem extensão)
     * @return imagem encontrada ou null caso não seja encontrada
     */
    public Image getImage(String imageName){
        return images.get(imageName);
    }
}
