package bgu.spl.app;

public class ShoeStorageInfo {
	private String shoeType;
	private int amountOnStorage;
	private int discountedAmount;

	public ShoeStorageInfo() {
		shoeType = "none";
		amountOnStorage = 0;
		discountedAmount = 0;
	}
	
	public ShoeStorageInfo(String shoeType, int amountOnStorage, int discountedAmount) {
		this.shoeType = shoeType;
		this.amountOnStorage = amountOnStorage;
		this.discountedAmount = discountedAmount;
	}
	
	public String getShoeType() {
		return shoeType;
	}

	public void setShoeType(String shoeType) {
		this.shoeType = shoeType;
	}

	public int getAmountOnStorage() {
		return amountOnStorage;
	}

	public void setAmountOnStorage(int amountOnStorage) {
		this.amountOnStorage = amountOnStorage;
	}

	public int getDiscountedAmount() {
		return discountedAmount;
	}

	public void setDiscountedAmount(int discountedAmount) {
		this.discountedAmount = discountedAmount;
	}

	@Override
	public String toString() {
		return "ShoeStorageInfo [shoeType=" + shoeType + ", amountOnStorage=" + amountOnStorage + ", discountedAmount="
				+ discountedAmount + "]";
	}
}
