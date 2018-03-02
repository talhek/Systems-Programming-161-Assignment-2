
package bgu.spl.app;

import bgu.spl.mics.Request;

public class PurchaseOrderRequest implements Request<Receipt> {
    private int tick;
    private String shoeType;
    private String nameOfRequest;
    private boolean onlyDiscount;
    private String senderName;
    
    public PurchaseOrderRequest(int tick, String shoeType, String nameOfRequest, boolean onlyDiscount, String senderName) {
        this.shoeType = shoeType;
        this.nameOfRequest = nameOfRequest;
        this.onlyDiscount = onlyDiscount;
        this.senderName = senderName;
        this.tick = tick;
    }
    
    public int getTick() {
        return tick; 
    }
    public String getShoe() {
        return shoeType;
    }
    public String getName() {
        return nameOfRequest;
    }
    public boolean getDiscount() {
        return onlyDiscount;
    }
    public String getSenderName() {
        return senderName;
    }
}