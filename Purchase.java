package bgu.spl.app;

public class Purchase {
	private String shoeType;
	private int tick;
	
	public Purchase(String shoeType, int tick){
		this.shoeType = shoeType;
		this.tick = tick;
	}
	
	public String getShoeType() {
		return shoeType;
	}
	public void setShoeType(String shoeType) {
		this.shoeType = shoeType;
	}
	public int getTick() {
		return tick;
	}
	public void setTick(int tick) {
		this.tick = tick;
	}
	
}