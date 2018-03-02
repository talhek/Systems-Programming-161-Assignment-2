package bgu.spl.app;

import bgu.spl.mics.Request;

public class ManufacturingOrderRequest implements Request<Receipt> {
	private String shoeType;
	private int amount;
	private int originalAmount;
	
	public ManufacturingOrderRequest(String shoeType, int amount) {
		this.shoeType = shoeType;
		this.amount = amount;
		originalAmount = amount;
	}
	
	public String getShoeType(){
		return shoeType;
	}
	
	public int getAmount(){
		return amount;
	}
	
	public void decAmount(){
		amount = amount - 1;
	}
	
	public int getOriginalAmount(){
		return originalAmount;
	}
}