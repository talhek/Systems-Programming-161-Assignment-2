package bgu.spl.app;

public class DiscountSchedule {
	private String shoeType;
	private int tick;
	private int amount;
	
	public DiscountSchedule(String shoeType, int tick, int amount){
		this.shoeType = shoeType;
		this.tick = tick;
		this.amount = amount;
	}
	
	public String getShoeType() {
		return shoeType;
	}
	public void setshoeType(String shoeType) {
		this.shoeType = shoeType;
	}
	public int getTick() {
		return tick;
	}
	public void setTick(int tick) {
		this.tick = tick;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	
}
