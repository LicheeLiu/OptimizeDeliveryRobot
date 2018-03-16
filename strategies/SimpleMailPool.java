package strategies;

import java.util.Stack;

import automail.MailItem;
import automail.PriorityMailItem;
import automail.StorageTube;
import exceptions.TubeFullException;

public class SimpleMailPool implements IMailPool{
	// My first job with Robotic Mailing Solutions Inc.!
	// 2 kinds of items so two structures
	// Remember stacks from 1st year - easy to use, not sure if good choice
	private Stack<MailItem> nonPriorityPool;
	private Stack<MailItem> priorityPool;
	private ArrayList<MailItem> nonPriorityPoolWeak;
	private ArrayList<MailItem> priorityPoolWeak;
	private static final int MAX_TAKE = 4;

	public SimpleMailPool(){
		// Start empty
		nonPriorityPool = new Stack<MailItem>();
		priorityPool = new Stack<MailItem>();		
		nonPriorityPoolWeak = new ArrayList<MailItem>();
		priorityPoolWeak = new ArrayList<MailItem>();
	}

	public void addToPool(MailItem mailItem) {
		// Check whether it has a priority or not
		if(mailItem instanceof PriorityMailItem){
			// Add to priority items
			// Kinda feel like I should be sorting or something
			priorityPool.push(mailItem);
		}
		else{
			// Add to nonpriority items
			// Maybe I need to sort here as well? Bit confused now
			nonPriorityPool.add(mailItem);
		}
	}
	
	private int getNonPriorityPoolSize(int weightLimit) {
		// This was easy until we got the weak robot
		// Oh well, there's not that many heavy mail items -- this should be close enough
		return nonPriorityPool.size();
	}
	
	private int getPriorityPoolSize(int weightLimit){
		// Same as above, but even less heavy priority items -- hope this works too
		return priorityPool.size();
	}

	private MailItem getNonPriorityMail(int weightLimit){
		if(getNonPriorityPoolSize(weightLimit) > 0){
			// Should I be getting the earliest one? 
			// Surely the risk of the weak robot getting a heavy item is small!
			return nonPriorityPool.pop();
		}
		else{
			return null;
		}
	}
	
	private MailItem getHighestPriorityMail(int weightLimit){
		if(getPriorityPoolSize(weightLimit) > 0){
			// How am I supposed to know if this is the highest/earliest?
			return priorityPool.pop();
		}
		else{
			return null;
		}
		
	}
	
	// Never really wanted to be a programmer any way ...

	@Override
	public void fillStorageTube(StorageTube tube, boolean strong) {
		int max = strong ? Integer.MAX_VALUE : 2000; // max weight
		// Priority items are important;
		// if there are some, grab one and go, otherwise take as many items as we can and go
		try{
			// Start afresh by emptying undelivered items back in the pool
			while(!tube.isEmpty()) {
				addToPool(tube.pop());
			}
			// Check for a top priority item
			if (getPriorityPoolSize(max) > 0) {
				// Add priority mail item
				tube.addItem(getHighestPriorityMail(max));
				// Won't add any more - want it delivered ASAP
			}
			else{
				// Get as many nonpriority items as available or as fit
				while(tube.getSize() < MAX_TAKE && getNonPriorityPoolSize(max) > 0) {
					tube.addItem(getNonPriorityMail(max));
				}
			}
		}
		catch(TubeFullException e){
			e.printStackTrace();
		}
	}

}
