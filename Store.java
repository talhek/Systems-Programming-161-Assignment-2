package bgu.spl.app;

import java.util.LinkedList;

public class Store {
	private LinkedList<Receipt> receipts;
	private LinkedList<ShoeStorageInfo> shoes;
	
	
	private static class StoreHolder {
		private static Store instance = new Store();
	}
	
	public Store() {
		receipts = new LinkedList<Receipt>();
		shoes = new LinkedList<ShoeStorageInfo>();
	}
	
	public void load(ShoeStorageInfo[] storage) {
		for(int i=0; i<storage.length; i++)
		{
			shoes.add(storage[i]);
		}
		
	}
	
	public BuyResult take(String shoeType, boolean onlyDiscount) {
		for(int i=0; i<shoes.size(); i++) {
			if(shoes.get(i).getShoeType().equals(shoeType)) {
				if(shoes.get(i).getAmountOnStorage()>0) {
					if(shoes.get(i).getDiscountedAmount() > 0) {
						shoes.get(i).setAmountOnStorage(shoes.get(i).getAmountOnStorage()-1);
						shoes.get(i).setDiscountedAmount(shoes.get(i).getDiscountedAmount()-1);
						return BuyResult.DISCOUNTED_PRICE;
					}
					else {
						if(!onlyDiscount){
							shoes.get(i).setAmountOnStorage(shoes.get(i).getAmountOnStorage()-1);
							return BuyResult.REGULAR_PRICE;
						}
						else return BuyResult.NOT_ON_DISCOUNT;
					}
				} else return BuyResult.NOT_IN_STOCK;
			}
		}
		
		return BuyResult.NOT_IN_STOCK;
	}
	
	public void add(String shoeType, int amount) {
		for(int i=0; i<shoes.size(); i++)
			if(shoes.get(i).getShoeType().equals(shoeType))
				shoes.get(i).setAmountOnStorage(shoes.get(i).getAmountOnStorage()+amount);
	}
	
	public void addDiscount(String shoeType, int amount) {
		for(int i=0; i<shoes.size(); i++)
			if(shoes.get(i).getShoeType().equals(shoeType))
				shoes.get(i).setDiscountedAmount(shoes.get(i).getDiscountedAmount()+amount);
	}
	
	public void file(Receipt receipt) {
		receipts.add(receipt);
	}
	
	public void print() {
		for(int i=0; i<shoes.size(); i++)
			System.out.println(shoes.get(i).toString());
		for(int i=0; i<receipts.size(); i++)
			System.out.println(receipts.get(i).toString());
	}
	
	public static Store getInstance() {
		return StoreHolder.instance;	
	}
}
