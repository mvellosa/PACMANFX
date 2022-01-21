package GraphicEngine;

import LogicEngine.GameLogic;
import java.util.HashSet;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import SystemElements.Board;
import SystemElements.Fruit;
import SystemElements.Ghost;
import SystemElements.Pacman;
import java.util.ArrayList;
import javafx.animation.AnimationTimer;

/**
 *
 * @author matheus
 */
public class PacmanGUI extends Application {
    static int screenWidth;
    static int screenHeight;
    
    static int BLOCKSIZE = 32;
    
    static Stage gameStage;
    
    static Scene menuScene;
    static Scene gameScene;

    static ImageStorage imageStorage;
    
    static Pacman player;
    
    static Board gameBoard;
    static GameLogic logicEngine;
    
    
    static ArrayList<Ghost> ghosts;
    
    static HashSet<String> currentlyActiveKeys;
    
    static long lastPlayerMove = 0;
    static long lastGhostMove = 0;
    static long poweredUpTimer = 0;
    
    static int currentLevel = 1;
    
    static Scene currentScene;
    
    @Override
    public void start(Stage stage) {
        stage.setTitle("Pacman");
        
        gameBoard = new Board();
        player = new Pacman();
        
        Ghost Blinky = new Ghost("Blinky");
        Ghost Pinky = new Ghost("Pinky");
        Ghost Inky = new Ghost("Inky", false);
        Ghost Clyde = new Ghost("Clyde", false);
        
        ghosts = new ArrayList<>();
        
        ghosts.add(Blinky);
        ghosts.add(Pinky);
        ghosts.add(Inky);
        ghosts.add(Clyde);
        
        imageStorage = new ImageStorage();
        
        screenWidth = gameBoard.getWidth() * BLOCKSIZE;
        screenHeight = (gameBoard.getHeight() + 1) * BLOCKSIZE;
        
        logicEngine = new GameLogic(gameBoard, player);
        
        for (Ghost ghost: ghosts){
            logicEngine.addEnemy(ghost);
        }
        
        logicEngine.setLevelFruit(new Fruit(100));
        logicEngine.restartPositions();
        
        gameStage = stage;
        
        menuScene = setMenuScene();
        loadMenuBinds();
        
        currentScene = menuScene;
        
        loadStage(gameStage);
    }
    
    /**
     * atualiza o tabuleiro movimentando as entidades necessárias
     * @param gc
     */
    private static void update(GraphicsContext gc){
        gc.clearRect(0, 0, screenWidth, screenHeight);
        
        if (player.getLives() == 0){
            gameover();
        } else if (logicEngine.levelEnded()){
            nextLevel();
            currentLevel++;
        }
        
        long currentTime = System.currentTimeMillis();
        
        if (player.isPoweredUp()){
            if (poweredUpTimer == 0){
                poweredUpTimer = currentTime;
            }
            else if (currentTime > poweredUpTimer + 5000 - (1000 * (currentLevel - 1))) {
                player.powerDown();
                logicEngine.resetConsumedGhostReward();
                poweredUpTimer = 0;
            }
        }
        
        if(currentTime >= (lastPlayerMove + 100)) {
            int nextX = player.getPosX();
            int nextY = player.getPosY();
            
            if (currentlyActiveKeys.contains("LEFT") || currentlyActiveKeys.contains("A"))
                nextY--;

            if (currentlyActiveKeys.contains("RIGHT") || currentlyActiveKeys.contains("D"))
                nextY++;

            if (currentlyActiveKeys.contains("UP") || currentlyActiveKeys.contains("W"))
                nextX--;

            if (currentlyActiveKeys.contains("DOWN") || currentlyActiveKeys.contains("S"))
                nextX++;
            
            if (nextX != player.getPosX() || nextY != player.getPosY()){
                logicEngine.tryMovePlayer(nextX, nextY);
            }
            
            lastPlayerMove = currentTime;
        }

        if (currentTime >= (lastGhostMove + 400)){
            logicEngine.updateEnemiesPositions();
            lastGhostMove = currentTime;
        }
        
        if (logicEngine.playerHit()){
            player.setLives(player.getLives() - 1);
            logicEngine.restartPositions();
        }
        
        
        drawBoard(gc);
    }
    
    /**
     * cria os event handlers para o menu inicial
     */
    public static void loadMenuBinds(){
        menuScene.setOnKeyPressed(new EventHandler<KeyEvent>(){
            @Override
            public void handle(final KeyEvent ke){
                String keyPressed = ke.getCode().toString();
                if (keyPressed.equals("SPACE")){
                    gameScene = setGameScene();
                    loadGameBinds();
                    
                    currentScene = gameScene;
                    loadStage(gameStage);
                } else if (keyPressed.equals("ESCAPE")){
                    quitGame();
                }
            }
        });
    }
    
    /**
     * cria os event handlers para o jogo
     */
    private static void loadGameBinds(){
        currentlyActiveKeys = new HashSet<>();
        
        gameScene.setOnKeyPressed(new EventHandler<KeyEvent>(){
            @Override
            public void handle(final KeyEvent ke){
                String keyPressed = ke.getCode().toString();
                if (keyPressed.equals("ESCAPE")){
                    quitGame();
                }
                if (currentlyActiveKeys.isEmpty())
                    currentlyActiveKeys.add(keyPressed);
            }
        });
        
        gameScene.setOnKeyReleased(new EventHandler<KeyEvent>(){
            @Override
            public void handle(final KeyEvent ke){
                String keyReleased = ke.getCode().toString();
                currentlyActiveKeys.remove(keyReleased);
            }
        });
    }
    
    /**
     * creates a windows to quit game
     */
    private static void quitGame(){
        Alert closeAlert = new Alert(
                            AlertType.CONFIRMATION,
                            "Do you really wish to quit ?",
                            ButtonType.YES,
                            ButtonType.NO);
                    
                    closeAlert.showAndWait();
        if (closeAlert.getResult() == ButtonType.YES){
            gameStage.close();
        }
    }
    
    /**
     * seta a scene atual em um stage passado por parametro
     * @param stage stage em que a scene atual seria colocada
     */
    private static void loadStage(Stage stage){
        stage.setScene(currentScene);
        stage.show();
    }
    
    /**
     * cria a scene do menu principal
     * @return scene do menu
     */
    private static Scene setMenuScene(){
        Canvas gameCanvas = new Canvas(screenWidth, screenHeight);
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        
        Image menuImage = new Image(resourcePath("menubg.png"));
        
        gc.drawImage(menuImage, 0, 0, screenWidth, screenHeight);
        
        Group root = new Group();
        root.getChildren().add(gameCanvas);
        return new Scene(root, screenWidth, screenHeight);
    }
    
    /**
     * cria a scene do jogo
     * @return scene do jogo
     */
    private static Scene setGameScene(){
        Canvas gameCanvas = new Canvas(screenWidth, screenHeight);
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        
        new AnimationTimer()
        {
            @Override
            public void handle(long currentNanoTime)
            {
                update(gc);
            }
        }.start();
        
        Group root = new Group();
        root.getChildren().add(gameCanvas);
        return new Scene(root, screenWidth, screenHeight);
    }
    
    /**
     * desenha o tabuleiro com os dados atuais
     * @param gc graphic context do canvas em que o desenho será feito
     */
    private static void drawBoard(GraphicsContext gc){
        
        for (int i = 0; i < gameBoard.getHeight(); i++){
            for (int j = 0; j < gameBoard.getWidth(); j++){
                Image spriteImage;
                char elementAt = logicEngine.getCurrentStage()[i][j];
                switch (elementAt) {
                    case '■':
                        spriteImage = imageStorage.getImage("wall");
                        break;
                    case '.':
                        spriteImage = imageStorage.getImage("pacdot");
                        break;
                    case 'X':
                        spriteImage = imageStorage.getImage("pacman");
                        break;
                    case 'B':
                        if (!player.isPoweredUp()){
                            spriteImage = imageStorage.getImage("blinky");
                        }else{
                            spriteImage = imageStorage.getImage("consumableghost");
                        }
                        break;
                    case 'P':
                        if (!player.isPoweredUp()){
                            spriteImage = imageStorage.getImage("pinky");
                        }else{
                            spriteImage = imageStorage.getImage("consumableghost");
                        }
                        break;
                    case 'I':
                        if (!player.isPoweredUp()){
                            spriteImage = imageStorage.getImage("inky");
                        }else{
                            spriteImage = imageStorage.getImage("consumableghost");
                        }
                        break;
                    case 'C':
                        if (!player.isPoweredUp()){
                            spriteImage = imageStorage.getImage("clyde");
                        }else{
                            spriteImage = imageStorage.getImage("consumableghost");
                        }
                        break;
                    case 'F':
                        String fruit = getFruitNameByReward(logicEngine.getLevelFruit().getReward());
                        
                        spriteImage = imageStorage.getImage(fruit);
                        break;
                    case 'G':
                        spriteImage = imageStorage.getImage("pill");
                        break;
                    default:
                        continue;
                }
                gc.drawImage(spriteImage, j * BLOCKSIZE , i * BLOCKSIZE, BLOCKSIZE, BLOCKSIZE);
            }
        }
        gc.fillText("Score: " + logicEngine.getCurrentScore(), screenWidth - 80, screenHeight - BLOCKSIZE / 2);
        for (int i = 0; i < player.getLives(); i++){
            gc.drawImage(imageStorage.getImage("heart"), 10 + (i * BLOCKSIZE), screenHeight - BLOCKSIZE, BLOCKSIZE, BLOCKSIZE);
        }
        
    }
    
    /**
     * checa e indica qual o nome da fruta que da a recompensa passada
     * @param reward recompensa que a fruta deve dar
     * @return fruta encontrada
     */
    private static String getFruitNameByReward(int reward){
        String equivalentFruit;
        switch (reward) {
            case 100:
                equivalentFruit = "cherry";
                break;
            case 300:
                equivalentFruit = "strawberry";
                break;
            default:
                equivalentFruit = "orange";
                break;
        }
        return equivalentFruit;
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
     * reinicia o jogo perdido
     */
    private static void gameover(){
        player = new Pacman();
        
        logicEngine = new GameLogic(gameBoard, player);
        
        for (Ghost ghost: ghosts){
            logicEngine.addEnemy(ghost);
        }
        
        logicEngine.setLevelFruit(new Fruit(100));
        
        currentScene = menuScene;
        
        loadStage(gameStage);
    }
    
    /**
     * avanca para o proximo level
     */
    private static void nextLevel(){
        logicEngine.nextLevel();
        if (logicEngine.getLevelFruit().getReward() == 100){
            logicEngine.setLevelFruit(new Fruit(300));
        }else{
            logicEngine.setLevelFruit(new Fruit(500));
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
