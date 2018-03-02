package bgu.spl.app;

import bgu.spl.mics.Broadcast;

public class NewDiscountBroadcast implements Broadcast {
	private int discountAmount;
	private String shoeType;
	
	public NewDiscountBroadcast(int discountAmount, String shoeType) {
		this.discountAmount = discountAmount;
		this.shoeType = shoeType;
	}
	
	public int getDiscountAmount() {
		return discountAmount;
	}
	
	public String getShoeType() {
		return shoeType;
	}

}
