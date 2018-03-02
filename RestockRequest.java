package bgu.spl.app;

import bgu.spl.mics.Request;

public class RestockRequest implements Request<Boolean> {
    private String shoeType;
    private int amount;
    
    public RestockRequest(String shoeType) {
        this.shoeType = shoeType;
        setAmount(0);
    }

    public String getShoeType() {
        return shoeType;
    }

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}
    
}