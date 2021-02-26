package examples.StarterPacMan;

import pacman.controllers.PacmanController;

import pacman.game.Constants;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

import java.util.ArrayList;
import java.util.Random;


/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getMove() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., entrants.pacman.username).
 */
public class MyPacMan extends PacmanController {
	
	// This enum FSMStatesPacman contains the states of our FSM
	public enum FSMStatesPacman {
		ChaseGhost,
	    EvadeGhost,
	    RandomMove,
	    GoToPill
	};
	
	//-------------members attributes----------------
	
    private static final int MIN_DISTANCE = 20;
    
    public  FSMStatesPacman state  = FSMStatesPacman.State1;
    private Random          random = new Random();
    
    //The time MsPacMan ate last power pill
    int timePowerPill = Integer.MAX_VALUE;
    
    //Here we store last ghost known position and the time we saw it
    private Integer [][] ghosts = new Integer [2][4];
    //Here we store last edible ghost known position
    private Integer [] ghostsEdible = new Integer [4];
    //General memory for ghosts positions
    private Integer [] ghostGeneral = new Integer [4];
    //Misc variable
    private Constants.GHOST[] ghostEntity = new Constants.GHOST[4];
    
    
    //Power pills known positions
    //public ArrayList<Integer> power_pills = new ArrayList<>();
    //In target_pills we store available pills position inorder to eat them
    public   ArrayList<Integer> target_pills = new ArrayList<>();
   
    //-------------member functions-------------
    public bool isGhostNear(Game game, Constants.GHOST ghost, int current) {
    	return game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(ghost)) <= MIN_DISTANCE;
    }
    
    public MOVE evadeGhost(Game game);
    public MOVE catchTheGhost(Game game);
    
    
    @Override
    public MOVE getMove(Game game, long timeDue) {
    	
    	MOVE ret_move = null;
    	int  current  = game.getPacmanCurrentNodeIndex();
    	
        ArrayList<Integer> target ;
        target = updateTargetPills(game, current);
    	
    	if (ret_move = evadeGhost(game, current)) {
    		System.out.println("State1: evade ghost");
    	} else if(ret_move = chaseGhost(game, current)){
    		System.out.println("State2: chase ghost");
    	} else if(!(chasePowerPill(game, current))) {
    		ret_move = chasePill(game, target, current);
    		System.out.println("State3: chase ghost");
    	} else if( (ret_move = chasePowerPill(game, current)) && (!game.wasPowerPillEaten()) ) {
    		timePowerPill = game.getCurrentLevelTime();
    		System.out.println("State4: chase power pill");
    	} else {
    		randomMove(game);
    	}
    	
    	return ret_move;
    	
    }
    
  //============================evadeGhost===========================================
    // 1. update containers/vectors of ghosts
    // 2. then vector ghosts will contain the non-eaten ghosts
    // 3. Find the alarm-ghost which has the lower distance from ms pac-man
    // 4. Get away from it. 
    
    public MOVE evadeGhost(Game game, int current) {
        
    	MOVE ret_move    = null;
        int  min_dist    = MIN_DISTANCE;
        int  alarm_ghost = -1;
        int  found       = 0;

        
        updates(game, current);
        
        for (int i = 0 ; i < 4 ; i++) {

                if (ghosts[0][i] != null) {
                    int distance = game.getShortestPathDistance(current,ghosts[0][i]);
      
                    
                    if (distance < min_dist) {
                    	
                    	if (!found) {
                    		found = 1;
                    	}
                    	
                        min_dist = distance;
                        alarm_ghost = i;
                    }
                }
        }
            
        if (found){
        	ret_move =  game.getNextMoveAwayFromTarget(current, ghosts[0][alarm_ghost] ,Constants.DM.PATH );
        }
    
        return ret_move ;    
   }
    
    
    public MOVE chaseGhost(Game game){
    	
    	MOVE ret_move = null;
        int  current  = game.getPacmanCurrentNodeIndex();
        int  min_dist = Integer.MAX_VALUE;
        int  num      = -1;
        boolean randMove = false;
        
        update_ghost_vecs(game, current);

        for (int i =0 ;i < ghostsEdible.length ; ++i) {
        	
            if (ghostsEdible[i] != null) {
                //randMove = true;
           
                int distance = game.getShortestPathDistance(current, ghostsEdible[i]);
                
                //if ((timePowerpill + 20 - game.getCurrentLevelTime()) > (distance *5)) {
                //if ((game.getCurrentLevelTime() - timePowerpill) - distance > -50){

                    if (distance < min_dist) {
                    	min_dist = distance;
                        num = i;
                    }
                //}

            }
        }
        
        if (num != -1) {
            ret_move =  game.getNextMoveTowardsTarget(current, ghostsEdible[num], Constants.DM.PATH);
        //}else if (randMove){
            //return randomMove(game);
        }
          //If there is no edible ghost return null
         
        return ret_move;
    }
    
    
    
    
    public void update_ghost_vecs(Game game, int current) {
    	
    	int ghost_ind = 0;
        int ghost_loc = 0;
        
        for (Constants.GHOST ghost : Constants.GHOST.values()) {
        	ghost_loc = game.getGhostCurrentNodeIndex(ghost);
        	
            //If ghost was eaten remove it from memory
            if(game.wasGhostEaten(ghost)){
                ghostGeneral[ghost_ind] = null;
                ghostsEdible[ghost_ind] = null;
                ghosts[0][ghost_ind] = null;
                ghosts[1][ghost_ind] = null;
                
            // if ghost_loc != -1 means that the ghost is shown. 
            // partial observability
            } else if (ghost_loc != -1) {
            	
            	 // if edible
            	 if (game.getGhostEdibleTime(ghost) > 2) {
                     ghostsEdible[ghost_ind] = ghost_loc;
                     
                 // if not, save/update its position and the time that we saw it. 
                 // update ghosts vectors.    
            	 } else {
            		 ghostsEdible[ghost_ind] = null;
            		 
            		 int distance = game.getShortestPathDistance(current,ghosts[0][ghost_ind]);
            		 int time = game.getCurrentLevelTime() - ghosts[1][i];
                     
            		 if ( (distance > 70) || (time > 15) ){
                         ghosts[0][ghost_ind] = null;
                         ghosts[1][ghost_ind] = null;
                    
                     } else {
                    	 ghosts[0][ghost_ind] = ghost_loc;
                    	 ghosts[1][ghost_ind] = game.getCurrentLevelTime();
                     
                    	 ghostGeneral[ghost_ind] = ghost_loc;
                     }                           
            	 }   
            }
            
            ghost_ind++;
        }
    
    }
            
    
    public void update_ghost_vecs_after_power_pill(Game game, int current) {
     //If you eat power pill fill the ghostsEdible array and empty the ghosts
    	if (game.wasPowerPillEaten()) {
    		

    		if (game.getCurrentLevelTime() - timePowerpill < 200) {
    			for (int j = 0; j < ghostGeneral.length; j++) {
    				ghostsEdible[j] = ghostGeneral[j];
    				ghosts[0][j]=null;
    				ghosts[1][j]=null;

    			}
    		}
    	}
//    	
    }
    public void updates(Game game, int current) {
    	
    	update_ghost_vecs(game, current);
    	update_ghost_vecs_after_power_pill(game, current);
//;
//        
//    //If you eat power pill fill the ghostsEdible array and empty the ghosts
//    if (game.wasPowerPillEaten()) {
//        timePowerpill = game.getCurrentLevelTime();
//
//        if (game.getCurrentLevelTime() - timePowerpill < 200) {
//            for (int j = 0; j < ghostGeneral.length; j++) {
//                ghostsEdible[j] = ghostGeneral[j];
//                ghosts[0][j]=null;
//                ghosts[1][j]=null;
//
//            }
//        }
//    }
//    //If the power pill time end restore your memory
//    if(game.getCurrentLevelTime()-timePowerpill >200){
//
//        for (int j=0;j<ghostGeneral.length;j++){
//            ghostsEdible[j]=null;
//        }
//    }

    }
    
    //============================randomMove===========================================

    public MOVE randomMove (Game game, int current){

        MOVE ret_move = null;

        //int current = game.getPacmanCurrentNodeIndex();
        MOVE[] moves = game.getPossibleMoves(current, game.getPacmanLastMoveMade());
        
        if (moves.length > 0) {
            return moves[random.nextInt(moves.length)];
        }
            //If you can't do random move return null
        return ret_move;
    }
    
    
    
    //============================chasePill===========================================
    // 1. get targetArray from ArrayList target
    // 2. move towards closest target using getClosestNodeIndexFromNodeIndex  
    public MOVE chasePill (Game game, ArrayList<Integer> target, int current){

        MOVE ret_move = null;
        //int current = game.getPacmanCurrentNodeIndex();
        if (!target.isEmpty()) {
            int[] targetsArray = new int[target.size()];        //convert from ArrayList to array

            for (int i = 0; i < targetsArray.length; i++) {
                targetsArray[i] = target.get(i);
            }
            
            ret_move = game.getNextMoveTowardsTarget(current, game.getClosestNodeIndexFromNodeIndex(current, targetsArray, Constants.DM.PATH), Constants.DM.PATH);
        }
        
        return ret_move;
    }

    
  //Here we update pills position, if eaten remove it from available list
    public ArrayList<Integer> updateTargetPills (Game game){

        int[] pills = game.getPillIndices();
        for (int i = 0; i < pills.length; i++) {
            //check which pills are available
            Boolean pillStillAvailable = game.isPillStillAvailable(i);
            if (pillStillAvailable != null) {
                if (pillStillAvailable && !target_pills.contains(pills[i])) {
                    target_pills.add(pills[i]);
                }
                else if (target_pills.contains(pills[i]) && !pillStillAvailable) {
                    target_pills.remove(pills[i]);
                }
            }

        }
        return target_pills;
    }
    
    
    //============================chasePowerPill===========================================
    // 1. update containers/vectors of ghosts
    // 2. then vector ghosts will contain the non-eaten ghosts
    // 3. Find the alarm-ghost which has the lower distance from ms pac-man
    // 4. Get away from it. 

    public MOVE chasePowerPill(Game game, int current) {
  
        int[] power_pills = game.getPowerPillIndices();

        for (int i = 0; i < power_pills.length; ++i) {          
            Boolean pillStillAvailable = game.isPillStillAvailable(i);
            
            if (pillStillAvailable != null) {
            	
                if (pillStillAvailable) {
                    power_pills.add(power_pills[i]);
                } else {
                    power_pills.remove((power_pills[i]);
                }
            }
        }
        
  
        if (!power_pills.isEmpty()) {
        	int[] power_pill_arr = new int[power_pills.size()];        //convert from ArrayList to array

        	for (int i = 0; i < power_pill_arr.length; i++) {
        		power_pill_arr[i] = power_pills.get(i);
        	}
        	
        	MOVE move = game.getNextMoveTowardsTarget(current, game.getClosestNodeIndexFromNodeIndex(current, powerPillsArray, Constants.DM.PATH), Constants.DM.PATH);
        }
        
        
        return move;
    }
    
    
    
}