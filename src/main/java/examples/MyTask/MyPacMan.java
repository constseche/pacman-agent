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

    // This enum FSMStatesPacman contains the states of our FSM, but not used!!
    public enum FSMStatesPacman {
        ChaseGhost,
        EvadeGhost,
        RandomMove,
        GoToPill,
        ChasePill
    }
    private static final int MIN_DISTANCE = 20;
    //public  FSMStatesPacman state  = FSMStatesPacman.ChaseGhost;
    private Random random = new Random();
    int timePowerPill = Integer.MAX_VALUE;

    // alarm: ghosts and the time we saw them.
    // The second row contains not only the last time of alarm ghosts but also of blue ghosts.
    private Integer [][] alarm = new Integer [2][4]; //alarm
    //Here we store last edible ghost known position
    private Integer [] blues = new Integer [4]; //blue


    // This MOVE will be updated in every state and contain the returned move.
    public MOVE next_move = null;


    
    //public ArrayList<Integer> power_pills = new ArrayList<>();
    public   ArrayList<Integer> target_pills = new ArrayList<>();
     


    @Override
    public MOVE getMove(Game game, long timeDue) {
        MOVE ret_move = null;
        int current = game.getPacmanCurrentNodeIndex();

        ArrayList<Integer> target = null;
        target = updateTargetPills(game, current);

        if (evadeGhost(game, current)) {
            ret_move = next_move;
            //ret_move = evadeGhost(game, current);
            System.out.println("State1: evade ghost");
        } else if (chaseGhost(game, current)) {
            ret_move = next_move;
            System.out.println("State2: chase ghost");
        } else if (chasePill(game, current)) {
            ret_move = next_move;
            System.out.println("State3: chase pills");
        } else if ((chasePowerPill(game, current)) && (!game.wasPowerPillEaten())) {
            //timePowerPill = game.getCurrentLevelTime();
            ret_move = next_move;
            System.out.println("State4: chase power pill");
        } else {
            ret_move = randomMove(game, current);
            System.out.println("State5: Random Move");
        }

        return ret_move;
    }


    //============================evadeGhost===========================================
    // 1. update containers/vectors of ghosts
    // 2. then vector ghosts will contain the alarm ghosts
    // 3. Find the alarm-ghost which has the lower distance from ms pac-man
    // 4. Get away from it.

    public boolean evadeGhost(Game game, int current) {
        MOVE ret_move    = null;
        boolean ret = false;
        int  min_dist    = MIN_DISTANCE;
        int  alarm_ghost = -1;
        int  found       = 0;
        int  ghost_loc   = 0;

        updates(game, current);

        for (int i = 0 ; i < 4 ; i++) {

            if (alarm[0][i] != null) {

                int distance = game.getShortestPathDistance(current,alarm[0][i]);
                if (distance < min_dist) {
//
                    if (found == 0) {
                        found = 1;
                    }
//
                    min_dist = distance;
                    alarm_ghost = i;
                }
            }
        }

        if (found == 1){
            ret_move =  game.getNextMoveAwayFromTarget(current, alarm[0][alarm_ghost] ,Constants.DM.PATH );
            next_move = ret_move;
            ret = true;

        }

        return ret;
    }


    //============================chaseGhost===========================================
    // 1. update containers/vectors of ghosts
    // 2. then vector blues will contain the blue ghosts
    // 3. Find the blue-ghost which has the lower distance from ms pac-man
    // 4. Go towards it.
    public boolean chaseGhost(Game game, int current){
        boolean ret = false;

        MOVE ret_move = null;
        int  min_dist = Integer.MAX_VALUE;
        int  blue_ghost = -1;

        updates(game, current);

        for (int i = 0 ; i < 4 ; i++) {

            if (blues[i] != null) {
                int distance = game.getShortestPathDistance(current,blues[i]);

            if (distance < min_dist) {
                    blue_ghost = i;
                    min_dist = distance;
                }
            }
        }

        if (blue_ghost != -1){
            ret_move =  game.getNextMoveTowardsTarget(current, blues[blue_ghost] ,Constants.DM.PATH );
            next_move = ret_move;
            ret = true;
        }

        return ret;
    }
/*
    public Constants.GHOST getClosestGhost(Game game) {
        int distance = Integer.MAX_VALUE;
        Constants.GHOST min_ghost = null;

        for (Constants.GHOST ghost : Constants.GHOST.values()) {
            if (game.getGhostCurrentNodeIndex(ghost) != -1) {
                int dist = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(ghost));
                if (dist < distance) {
                    distance = dist;
                    min_ghost = ghost;
                }
            }
        }
        return min_ghost;
    }
*/



    //============================updates===========================================
    // 1. update containers/vectors of ghosts

    public void updates(Game game, int current) {

        int ghost_ind = 0;
        int ghost_loc = 0;


        for (Constants.GHOST ghost : Constants.GHOST.values()) {
            ghost_loc = game.getGhostCurrentNodeIndex(ghost);
            //game.getGhostLairTime()

            if (alarm[1][ghost_ind] != null) { // if i have seen it before
                // and If a lot of time past since ghost seen, remove it
                if (game.getCurrentLevelTime() - alarm[1][ghost_ind] > 10) {
                    alarm[0][ghost_ind] = null;
                    alarm[1][ghost_ind] = null;
                    blues[ghost_ind] = null;
                }
            }

            if (game.getGhostEdibleTime(ghost) > 0) {
                blues[ghost_ind] = ghost_loc;
                alarm[0][ghost_ind] = null;
                alarm[1][ghost_ind] = game.getCurrentLevelTime();

            } else if (ghost_loc != -1) {
                // if not, save/update its position and the time that we saw it.
                // update ghosts vectors.
                int distance = game.getShortestPathDistance(current, ghost_loc);
                //int time = game.getCurrentLevelTime() - ghosts[1][ghost_ind];

                if ((distance > 70)) { // || (time > 10) ){
                    alarm[0][ghost_ind] = null;
                    alarm[1][ghost_ind] = null;

                } else {
                    alarm[0][ghost_ind] = ghost_loc;
                    alarm[1][ghost_ind] = game.getCurrentLevelTime();
                }
            }
            ghost_ind++;
        }

        // if power pill eaten all ghosts are blue.
        if (game.wasPowerPillEaten()) {
            timePowerPill = game.getCurrentLevelTime();

            for (int j = 0; j < 4; ++j) {
                //blues[j] = alarm[0][j];
                alarm[0][j] = null;
                alarm[1][j] = null;
            }
        }
    }

    //============================randomMove===========================================

    public MOVE randomMove (Game game, int current){
        MOVE   ret_move = null;
        MOVE[] moves    = game.getPossibleMoves(current, game.getPacmanLastMoveMade());

        if (moves.length > 0) {
            ret_move = moves[random.nextInt(moves.length)];
        }

        return ret_move;
    }


    //============================chasePill===========================================
    // not used here:
    // 1. get targetArray from ArrayList target
    // 2. move towards closest target using getClosestNodeIndexFromNodeIndex
    // used here:
    // find available pills that are shown.

    public boolean chasePill (Game game, int current){
        boolean ret = false;

        MOVE ret_move = null;
        int[] pills = game.getPillIndices();


        ArrayList<Integer> targets = updateTargetPills (game, current); //new ArrayList<Integer>();

        /*for (int i = 0; i < pills.length; i++) {
            Boolean pillStillAvailable = game.isPillStillAvailable(i);
            if (pillStillAvailable != null) {
                if (pillStillAvailable) {
                    targets.add(pills[i]);
                }
            }
        }*/

        if (!targets.isEmpty()) {
            int[] targetsArray = new int[targets.size()];

            for (int i = 0; i < targetsArray.length; i++) {
                targetsArray[i] = targets.get(i);
            }

            ret_move =  game.getNextMoveTowardsTarget(current, game.getClosestNodeIndexFromNodeIndex(current, targetsArray, Constants.DM.PATH), Constants.DM.PATH);
            ret = true;
            next_move = ret_move;
        }

        return ret;
    }


    public ArrayList<Integer> updateTargetPills (Game game, int current){
        int[] pills = game.getPillIndices();

        for (int i = 0; i < pills.length; ++i) {
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
    public boolean chasePowerPill(Game game, int current) {
        boolean ret = false;

        MOVE ret_move = null;
        int[] pills = game.getPowerPillIndices();

        ArrayList<Integer> power_pills = new ArrayList<Integer>();

        for (int i = 0; i < pills.length; ++i) {
            Boolean pillStillAvailable = game.isPillStillAvailable(i);

            if (pillStillAvailable != null) {
                if (pillStillAvailable) {
                    power_pills.add(pills[i]);
                }
            }
        }

        if (!power_pills.isEmpty()) {
            int[] power_pill_arr = new int[power_pills.size()];

            for (int i = 0; i < power_pill_arr.length; i++) {
                power_pill_arr[i] = power_pills.get(i);
            }

            ret_move = game.getNextMoveTowardsTarget(current, game.getClosestNodeIndexFromNodeIndex(current, power_pill_arr, Constants.DM.PATH), Constants.DM.PATH);
            ret = true;
            next_move = ret_move;
        }


        return ret;
    }





}