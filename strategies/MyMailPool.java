package strategies;

import java.util.*;
import java.math.*;
import static java.util.Comparator.comparing;

import automail.Building;
import automail.Clock;
import automail.MailItem;
import automail.PriorityMailItem;
import automail.StorageTube;
import exceptions.TubeFullException;

public class MyMailPool implements IMailPool{
	private ArrayList<MailItem> nonPriorityPool;
	private ArrayList<MailItem> priorityPool;
	private ArrayList<MailItem> nonPriorityPoolWeak;
	private ArrayList<MailItem> priorityPoolWeak;
	private static final int MAX_TAKE = 4;
	
	//have separate mail pools for weak robot
	public MyMailPool(){
		// Start empty
		nonPriorityPool = new ArrayList<MailItem>();
		priorityPool = new ArrayList<MailItem>();
		nonPriorityPoolWeak = new ArrayList<MailItem>();
		priorityPoolWeak = new ArrayList<MailItem>();
	}
	
	
	//add new mails into pools
	public void addToPool(MailItem mailItem) {
	
		// Check whether it has a priority or not
		if(mailItem instanceof PriorityMailItem){
			priorityPool.add(mailItem);
			if(mailItem.getWeight() <= 2000) { //check if it can be delivered by weak robot
				priorityPoolWeak.add(mailItem); //add to weak robot pool
			}
		}
		else {
			nonPriorityPool.add(mailItem);	
			if(mailItem.getWeight() <= 2000)
				nonPriorityPoolWeak.add(mailItem);
		}
	}
	
	//get nonPriority pool size. If asked by strong robot, return the nonPriorityPool size, if asked by weak robot, return the weak pool size.
	private int getNonPriorityPoolSize(boolean strong){
		int sizeStrong = nonPriorityPool.size();
		int sizeWeak = nonPriorityPoolWeak.size();
		if(strong) {
			return sizeStrong;
		}
		else {
			return sizeWeak;
		}
	}

	//get Priority pool size. If asked by strong robot, return the PriorityPool size, if asked by weak robot, return the weak pool size.
	private int getPriorityPoolSize(boolean strong) {
		int sizeStrong = priorityPool.size();
		int sizeWeak = priorityPoolWeak.size();
		if(strong) 
			return sizeStrong;
		else
			return sizeWeak;
	} 
	
	//return the mail in the pool with the highest "priority index" (not the direct priority, but a priority index calculated from both time and priority by the method below priorityIndex)
	private MailItem highestPriorityMail(ArrayList<MailItem> mailList) {
		double highestMail = 0;
		int index = 0;
		for(int i = 0; i < mailList.size(); i++) {
			if(priorityIndex(mailList.get(i)) > highestMail) {
				highestMail = priorityIndex(mailList.get(i));
				index = i;
			}
		}
		return mailList.get(index);
	}
	
	//priority Index takes both time and priority into consideration
	private double priorityIndex(MailItem mail) {
		double delay = Clock.Time() - mail.getArrivalTime();
		double priority = ((PriorityMailItem) mail).getPriorityLevel();
		return Math.pow(delay, 1.1) * (1 + Math.sqrt(priority));
	}
	
	//return the mail with highest priority
	private MailItem getHighestPriorityMail(boolean strong) {
		if(getPriorityPoolSize(strong) == 0) { //if the pool size is zero, no mail in pool
			return null;
		}
		if(strong) {
			MailItem temp = highestPriorityMail(priorityPool);
			//delete the mail in both priorityPool and the weak pool
			priorityPool.remove(temp);
			if(priorityPoolWeak.contains(temp)) {
				priorityPoolWeak.remove(temp);	
			}
			return temp;
		}
		else {
			MailItem temp = highestPriorityMail(priorityPoolWeak);
			priorityPool.remove(temp);
			priorityPoolWeak.remove(temp);
			return temp;
		}
	}
	
	//return the mailItem that arrive earliest
	private MailItem earliestArrival(ArrayList<MailItem> mailList){
		int earlistArrival = 1000000;
		int index = 0;
		for(int i = 0; i < mailList.size(); i++) {
			if(mailList.get(i).getArrivalTime() < earlistArrival) {
				index = i;
				earlistArrival = mailList.get(i).getArrivalTime();
			}
		}
		return mailList.get(index);
	}
	
	//get nonpriority mail that deliver to the same floor as those already in the tube. Among those to the same destination floor, get the mail that arrived earliest 
	//get the one arrived earliest if no mail in the pool share the same destination floor
	private MailItem getNonPriorityMail(boolean strong, int floor) {
		int size = getNonPriorityPoolSize(strong);
		if(size == 0)
			return null;
		if(strong) {
			ArrayList<MailItem> sameFloor = new ArrayList<MailItem>();
			if(floor != Building.LOWEST_FLOOR - 1) { //it's a strong robot and we have a destination floor to match mails
				for(int i = 0; i < nonPriorityPool.size(); i++) {
					if(nonPriorityPool.get(i).getDestFloor() == floor) {
						sameFloor.add(nonPriorityPool.get(i));
					}
				}
				MailItem temp ;
				if(sameFloor.isEmpty()) { //if there is no mail on the same floor, return the earliest arrival mail in the pool 
					temp = earliestArrival(nonPriorityPool);
				}
				else { 
				temp = earliestArrival(sameFloor);
				}
				nonPriorityPool.remove(temp);
				if(nonPriorityPoolWeak.contains(temp))
					nonPriorityPoolWeak.remove(temp);
				return temp;
			}
			else { //it's a strong robot and we don't have a destination floor, so just find out the earliest arrival mail in nonPriorityPool.
				MailItem temp = earliestArrival(nonPriorityPool);
				nonPriorityPool.remove(temp);
				if(nonPriorityPoolWeak.contains(temp))
					nonPriorityPoolWeak.remove(temp);
				return temp;
			}
		}
	
		else if(!strong) { 
			ArrayList<MailItem> sameFloorWeak = new ArrayList<MailItem>();
			
			if(floor != Building.LOWEST_FLOOR - 1) { //it's a weak robot and we have a destination floor to match mails
				for(int i = 0; i < nonPriorityPoolWeak.size(); i++) {
					if(nonPriorityPoolWeak.get(i).getDestFloor() == floor) {
						sameFloorWeak.add(nonPriorityPoolWeak.get(i));
					}
				}
				MailItem tempWeak;
				if(sameFloorWeak.isEmpty()) {  //if there is no mail on the same floor, return the earliest arrival mail in the pool 
					tempWeak = earliestArrival(nonPriorityPoolWeak);
				}
				else {
					tempWeak = earliestArrival(sameFloorWeak);
				}
				nonPriorityPool.remove(tempWeak);
				nonPriorityPoolWeak.remove(tempWeak);
				return tempWeak;
			}
			else { //it's a weak robot and we don't have a destination floor, so just find out the earliest arrival mail in nonPriorityPoolWeak.
				MailItem tempWeak = earliestArrival(nonPriorityPoolWeak);
				nonPriorityPool.remove(tempWeak);
				nonPriorityPoolWeak.remove(tempWeak);
				return tempWeak;
			}
		}
		return null;
	}
	
	@Override
	public void fillStorageTube(StorageTube tube, boolean strong){
		try{
			// Start afresh by emptying undelivered items back in the pool
			while(!tube.isEmpty()) {
				addToPool(tube.pop());
			}
			// Add in priority mail until tube is full or priority pool is empty
			while (getPriorityPoolSize(strong) > 0 && tube.getSize() < MAX_TAKE) {
				tube.addItem(getHighestPriorityMail(strong));
			}
			
			//get non-priority mail that deliver to the same floor as priority mail, if there are priority mails in the tube
			int floor = 0;
			if(tube.getSize() > 0) {
				floor = tube.peek().getDestFloor();
			}
			else {
				floor = Building.LOWEST_FLOOR - 1;
			}
			
			//add in nonpriority mail until tube is full or nonpriority pool is empty
			while(tube.getSize() < MAX_TAKE && getNonPriorityPoolSize(strong) > 0) {
				MailItem newMail = getNonPriorityMail(strong, floor);
				tube.addItem(newMail);
				if(newMail != null)
					floor = newMail.getDestFloor();
			}
		}
		catch(TubeFullException e){
			e.printStackTrace();
		}
	}
}