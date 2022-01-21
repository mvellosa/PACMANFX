package LogicEngine;

import MyUtils.Pathfind;
import MyUtils.Position;
import MyUtils.Utils;

import SystemElements.Ghost;
import SystemElements.Pacman;
import SystemElements.Board;
import SystemElements.Consumable;
import SystemElements.Entity;
import SystemElements.Fruit;
import SystemElements.Pill;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

/**
 * classe para tratar da lógica da movimentação dos elementos
 * @author matheus
 */
public class GameLogic {
    
    private Board _board;
    private int _score = 0;
    
    private int _totalPacdots = 0;
    private int _pacdotsConsumed = 0;
    
    private int _consumedGhostReward = 200;
    
    private int _fruitsSpawned = 0;
    
    private boolean _gotExtraLife = false;
    
    private boolean[][] _validityPosMap;
    
    private final Pacman _player;
    private List<Entity> _enemies;
    
    private List<Consumable> _consumables;
    private boolean _playerHit = false;
    
    private Fruit _levelFruit;
    
    private final Position PLAYERSTARTPOSITION = new Position(17, 9);
    private final Position GHOSTSTARTPOSITION = new Position(7, 7);
    
    private List<Pill> _pills;
    
    private final Collection<Character> _invalidSpaceSymbols;

    /**
     * Construtor
     * @param baseBoard tabuleiro base onde residam as entidades
     * @param player jogador
     */
    public GameLogic(Board baseBoard, Pacman player){
        _board = baseBoard;
        _enemies = new ArrayList();
        _consumables = new ArrayList();
        _pills = new ArrayList<Pill>();
        
        _invalidSpaceSymbols = _board.getInvalidSpaceSymbols();
        
        initValidPositionsStage();
        
        initPills();
        
        for (char[] line : _board.getBaseBoard()){
            for (char symbol : line){
                if (symbol == '.'){
                    _totalPacdots++;
                }
            }
        }
        
        _player = player;
        _player.setPos(PLAYERSTARTPOSITION);
    }
    
    /**
     * inicializa pilulas, suas posicoes e recompensas
     */
    private void initPills(){
        for(int i = 0; i < 4; i++){
            _pills.add(new Pill(50));
        }
        
        _pills.get(0).setPos(new Position(1, 1));
        _pills.get(1).setPos(new Position(1, _board.getWidth() - 2));
        _pills.get(2).setPos(new Position(_board.getHeight() - 2, 1));
        _pills.get(3).setPos(new Position(_board.getHeight() - 2, _board.getHeight() - 2));
    }
    
    /**
     * reinicia as posicoes dos inimigos e do pacman para as iniciais
     */
    public void restartPositions(){
        int ghostsPlaced = 0;
        for (Entity enemy : _enemies){
            //_validityPosMap[enemy.getPosX()][enemy.getPosY()] = true;
            enemy.setPos(GHOSTSTARTPOSITION.getX(), GHOSTSTARTPOSITION.getY() + ghostsPlaced++);
        }
        //_validityPosMap[_player.getPosX()][_player.getPosY()] = true;
        _player.setPos(PLAYERSTARTPOSITION);
        _playerHit = false;
    }
    
    /**
     * getter para o score atual
     * @return score atual
     */
    public int getCurrentScore(){
        return _score;
    }
    
    /**
     * seta a fruta que devera ser spawnada no level atual
     * @param levelFruit fruta referente ao level
     */
    public void setLevelFruit(Fruit levelFruit){
        this._levelFruit = levelFruit;
        this._levelFruit.die();
    }
    
    /**
     * getter para a fruta referente ao level atual
     * @return fruta do level atual
     */
    public Fruit getLevelFruit(){
        return _levelFruit;
    }
    
    /**
     * adiciona um consumivel a lista de consumiveis do level
     * @param consumable consumivel adicionado
     */
    public void addLevelConsumable(Consumable consumable){
        consumable.setPos(randomValidPosition());
        _consumables.add(consumable);
    }
    
    /**
     * estágio com as entidades representadas no tabuleiro
     * @return tabuleiro com os elementos
     */
    public char[][] getCurrentStage(){
        char[][] currentStage = Utils.copyGrid(_board.getBaseBoard());
        
        if (fruitShouldSpawn()){
            _levelFruit.setPos(randomValidPosition());
            _levelFruit.live();
            _fruitsSpawned++;
        }
        
        if (_levelFruit.isAlive()){
            currentStage[_levelFruit.getPosX()][_levelFruit.getPosY()] = 'F';
        }
        
        for (Pill pill : _pills){
            if (pill.isAlive()){
                currentStage[pill.getPosX()][pill.getPosY()] = 'G';
            }
        }
        
        for (Entity ene : _enemies){
            char enemySymbol = 'E';
            if (ene instanceof Ghost){
                Ghost ghost = (Ghost) ene;
                enemySymbol = ghost.getName().charAt(0);
            }
            
            currentStage[ene.getPosX()][ene.getPosY()] = enemySymbol;
        }
        
        currentStage[_player.getPosX()][_player.getPosY()] = 'X';
        return currentStage;
    }
    
    /**
     * reinicia os parametros necessarios para o proximo estagio
     */
    public void nextLevel(){
        restartPositions();
        _board = new Board();
        initPills();
    }
    
    /**
     * verifica se os criterios para que uma fruta apareca foram cumpridos
     * @return 
     */
    private boolean fruitShouldSpawn(){
        return (_pacdotsConsumed == 70 || _pacdotsConsumed >= 100) &&
                (!_levelFruit.isAlive()) &&
                _fruitsSpawned < 2;
    }
    
    /**
     * checa se o jogador esta na mesma casa que um fantasma
     * @return true se o jogador estiver na mesma casa que um fantasma
     */
    public boolean playerHit(){
        return _playerHit;
    }
    
    /**
     * checa se todos pacdots ja foram comidos, e se o level deve terminar
     * @return true se o level tiver terminado false se nao tiver
     */
    public boolean levelEnded(){
        if (_totalPacdots == 0){
            return true;
        }
        return false;
    }
    
    /**
     * indica se o jogo deve terminar
     * @return true se o jogo deve terminar false caso contrario
     */
    public boolean gameOver(){
        if (_player.getLives() == 0){
            return true;
        }
        return false;
    }
    
    /**
     * checa se existe um inimigo em determinada posicao e se houver indica qual
     * @param pos posicao que sera checada
     * @return inimigo caso exista um na posicao ou nulo caso nao exista
     */
    private Entity enemyAtPosition(Position pos){
        for (Entity enemy : _enemies){
            if (Position.equals(pos, enemy.getPos())){
                return enemy;
            }
        }
        return null;
    }
    
    /**
     * checa se existe uma pilula em determinada posicao e se houver indica qual
     * @param pos posicao que sera checada
     * @return pilula caso exista uma na posicao ou nulo caso nao exista
     */
    private Pill pillAtPosition(Position pos){
        for (Pill pill : _pills){
            if (pill.isAlive() && Position.equals(pos, pill.getPos())){
                return pill;
            }
        }
        return null;
    }
    
    /**
     * reseta o valor da recompenas por comer um fantasma para o valor inicial
     */
    public void resetConsumedGhostReward(){
        _consumedGhostReward = 200;
    }
    
    /**
     * tenta mover o jogador para uma nova posicao
     * @param x nova posicao x
     * @param y nova posicao y
     * @return se a mudanca de posicao pode ser feita ou nao
     */
    public boolean tryMovePlayer(int x, int y){
        if (_validityPosMap[x][y]){
            Entity enemy = enemyAtPosition(new Position(x, y));
            if (enemy != null){
                if (_player.isPoweredUp()){
                    _score += _consumedGhostReward;
                    _consumedGhostReward *= 2;
                    enemy.setPos(GHOSTSTARTPOSITION);
                }else{
                    _playerHit = true;
                    return false;
                }
            }
            
            Pill pillFound = pillAtPosition(new Position(x, y));
            if (pillFound != null){
                _score += _player.consume(pillFound);
            }
            
            setNewPosition(_player, new Position(x, y));
            if (_board.getBaseBoard()[_player.getPosX()][_player.getPosY()] == '.'){
               _board.getBaseBoard()[_player.getPosX()][_player.getPosY()]  = ' ';
               _score += 10;
               _pacdotsConsumed++;
               _totalPacdots--;
            }
            
            if (_levelFruit.isAlive() && Position.equals(_player.getPos(), _levelFruit.getPos())){
                _score += _player.consume(_levelFruit);
                _levelFruit.die();
            }
            
            if (_score == 10000 && !_gotExtraLife){
                _gotExtraLife = true;
                _player.setLives(_player.getLives() + 1);
            }
            return true;
        }
        return false;
    }
    
    /**
     * adiciona uma entidade como inimigo
     * @param newEntity entidade adicionada
     */
    public void addEnemy(Entity newEntity){
        //setFirstPosition(newEntity, GHOSTSTARTPOSITION);
        newEntity.setPos(GHOSTSTARTPOSITION);
        _enemies.add(newEntity);
    }
    
    /** 
     * remove entidade do estágio
     * @param ent entidade que será removida
     */
    public void removeEnemy(Entity ent){
        //_validityPosMap[ent.getPosX()][ent.getPosY()] = true;
        _enemies.remove(ent);
    }
    
    /**
     * seta o estágio inicial com quais posições são válidas ou não
     */
    private void initValidPositionsStage() {
        boolean[][] confirmedValidPositions = new boolean[_board.getHeight()][_board.getWidth()];
        
        for (int i = 0; i < _board.getHeight(); i++){
            for (int j = 0; j < _board.getWidth(); j++){
                char symbol = _board.getElementAt(i, j);
                
                confirmedValidPositions[i][j] = ! _invalidSpaceSymbols.contains(symbol);
            }
        }
        
        _validityPosMap = confirmedValidPositions;
    }
    
    /**
     * atualiza o tabuleiro movendo as entidades que precisam ser movidas
     */
    public void updateEnemiesPositions(){
        for (Entity enemy : _enemies){
            if (enemy instanceof Ghost){
                Ghost currentGhost = (Ghost) enemy;
                ghostMove(currentGhost);
            }
        }
    }
    
    /**
     * movimenta o fantasma
     * @param movingGhost 
     */
    private void ghostMove(Ghost movingGhost){
        Position currentPosition = movingGhost.getPos();
        Position nextMove;
        
        if (movingGhost.followsPlayer()){
            Stack<Position> moves = Pathfind.findPath(_validityPosMap, currentPosition, _player.getPos());
            nextMove = moves.pop();
            if (!_validityPosMap[nextMove.getX()][nextMove.getY()]){
                nextMove = randomValidNeighbour(currentPosition);
            }
        } else{
            nextMove = randomValidNeighbour(currentPosition);
        }
        
        if (Position.equals(nextMove, _player.getPos())){
            _playerHit = true;
        }
        
        setNewPosition(movingGhost, nextMove);
    }

    /**
     * gera uma posicao vizinha aleatoria valida
     * @param currentPosition posicao atual
     * @return posicao do vizinho valido caso exista, ou a posicao passada caso 
     * nao exista um vizinho valido
     */
    private Position randomValidNeighbour(Position currentPosition){
        Position validNeighbour;
        Collection<Position> validNeighbours = currentPosition.neighbours(_board.getHeight(), _board.getWidth());
        filterValidPositions(validNeighbours);
        if (validNeighbours.isEmpty()){
            validNeighbour = currentPosition;
        }else{
            validNeighbour = Utils.chooseRandom(validNeighbours);
        }
        return validNeighbour;
    }
    
    /**
     * muda a posição da entidade para a solicitada
     * @param ent entidade sendo movida
     * @param newPosition nova posição da entidade
     */
    private void setNewPosition(Entity ent, Position newPosition){
        //_validityPosMap[ent.getPosX()][ent.getPosY()] = true;
        //_validityPosMap[newPosition.getX()][newPosition.getY()] = false;
        ent.setPos(newPosition);
    }

    /**
     * filtra a coleção mantendo apenas os elementos válidos
     * @param group 
     */
    private void filterValidPositions(Collection<Position> group){
        group.removeIf(pos -> (! _validityPosMap[pos.getX()][pos.getY()]));
    }
    
    /**
     * seleciona uma posição válida aleatória do tabuleiro
     * @return posição aleatória do tabuleiro
     */
    private Position randomValidPosition() {
        List<Position> validPositions = new LinkedList();
        
        for (int i = 0; i < _board.getHeight(); i++){
            for (int j = 0; j < _board.getWidth(); j++){
                if (_validityPosMap[i][j]){
                    validPositions.add(new Position(i, j));
                }
            }
        }
        
        Random randGen = new Random();
        int selectedPositionIndex = randGen.nextInt(validPositions.size());
        
        return validPositions.get(selectedPositionIndex);
    }
}
