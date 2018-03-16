package strategies;

import java.util.*;

import automail.Clock;
import automail.MailItem;
import automail.PriorityMailItem;
import automail.StorageTube;
import exceptions.TubeFullException;

public class MyRobotBehaviour implements IRobotBehaviour{
	
	private static int weightLimit = 2000;
	private boolean newPriority; // Used if we are notified that a priority item has arrived. 
	private boolean strongRobot;
		
	public MyRobotBehaviour(boolean strong) {
		newPriority = false;
		strongRobot = strong;
	}
	
	public void startDelivery() {
		newPriority = false;
	}
	
	@Override
    public void priorityArrival(int priority, int weight) {
		//if weight excess weightLimit, don't inform weak robot priority arrives.
    	if(weight > weightLimit && !strongRobot) {
    		newPriority = false;
    	}
    	else
    		newPriority = true;
    }
	
	@Override
	public boolean returnToMailRoom(StorageTube tube) {
		if (tube.isEmpty() || !newPriority) {
			return false; // Empty tube means we are returning anyway
		} 
		else {
			// Return if we don't have a priority item in tube and a new one came in
			Boolean priority = (tube.peek() instanceof PriorityMailItem);
			if(newPriority){
				newPriority = false;  //change newPriority to false to prevent 2 robots come back
				return !priority;
			}
			else
				return false;
		}
	}
}
