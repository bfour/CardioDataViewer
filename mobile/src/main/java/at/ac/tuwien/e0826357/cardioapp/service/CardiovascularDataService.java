package at.ac.tuwien.e0826357.cardioapp.service;

import java.util.Observable;

public abstract class CardiovascularDataService extends Observable {
	
	public abstract void start();
	
	public abstract void stop();
	
}
