import bwapi.*;
import bwta.*;

public class zerg_ai{

    private Mirror mirror = new Mirror();

    private Game game;

    private Player self;

    public void run() {
        mirror.getModule().setEventListener(new DefaultBWListener() {
            @Override
            public void onUnitCreate(Unit unit) {
                System.out.println("New unit " + unit.getType());
            }

            @Override
            public void onStart() {
                game = mirror.getGame();
                self = game.self();

                //Use BWTA to analyze map
                //This may take a few minutes if the map is processed first time!
                System.out.println("Analyzing map...");
                BWTA.readMap();
                BWTA.analyze();
                System.out.println("Map data ready");

            }

            boolean needOverlord = false;
            int supplyDiff;
            bool builtSpawningPool = false;

            @Override
            public void onFrame() {
                game.setTextSize(5);
                game.drawTextScreen(10, 10, "Playing as " + self.getName() + " - " + self.getRace());
                game.drawTextScreen(50, 10, Boolean.toString(needOverlord));

                StringBuilder units = new StringBuilder("My units:\n");

                supplyDiff = self.supplyTotal() - self.supplyUsed();

                if (supplyDiff <= 1) {
                    needOverlord = true;
                }
                
                //step 1: set drones to mine (done below)
                //step 2: build spawning pool
                if ((self.minerals() >= 200) && !builtSpawningPool)
                {
                    builtSpawningPool = true;
                    //build spawning pool here
                    //TilePosition targetLocation = get_pool_position();
                    //build_spawning_pool(targetLocation)
                }

                //iterate through my units
                for (Unit myUnit : self.getUnits()) {
                    units.append(myUnit.getType()).append(" ").append(myUnit.getTilePosition()).append("\n");

                    /*//if there's enough minerals, train a drone
                    if (myUnit.getType() == UnitType.Zerg_Larva && self.minerals() >= 50 && !needOverlord) {
                        myUnit.train(UnitType.Zerg_Drone);
                    }*/

                    //if necessary, train an overlord
                    if (myUnit.getType() == UnitType.Zerg_Larva && self.minerals() >= 100 && needOverlord) {
                        needOverlord = false;
                        myUnit.train(UnitType.Zerg_Overlord);                       
                    }

                    //if it's a drone and it's idle, send it to the closest mineral patch
                    if (myUnit.getType().isWorker() && myUnit.isIdle()) {
                        Unit closestMineral = null;

                        //find the closest mineral
                        closestMineral = getClosestMineral(myUnit);

                        //if a mineral patch was found, send the drone to gather it
                        if (closestMineral != null) {
                            myUnit.gather(closestMineral, false);
                        }
                    }
                }

                //draw my units on screen
                game.drawTextScreen(10, 25, units.toString());
            }

            Unit getClosestMineral(Unit target)
            {
                for (Unit neutralUnit : game.neutral().getUnits()) {
                    if (neutralUnit.getType().isMineralField()) {
                        if (closestMineral == null || target.getDistance(neutralUnit) < target.getDistance(closestMineral)) {
                            closestMineral = neutralUnit;
                        }
                    }
                }
                
                return closestMineral;
            }
            
            TileLocation getPoolPosition()
            {
                
            }
        });

        mirror.startGame();
    }

    public static void main(String... args) {
        new zerg_ai().run();
    }
}