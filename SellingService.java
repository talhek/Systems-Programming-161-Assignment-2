package bgu.spl.app.MicroServices;

import java.util.concurrent.CountDownLatch;

import bgu.spl.app.BuyResult;
import bgu.spl.app.PurchaseOrderRequest;
import bgu.spl.app.Receipt;
import bgu.spl.app.RestockRequest;
import bgu.spl.app.Store;
import bgu.spl.app.TerminationBroadcast;
import bgu.spl.app.TickBroadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.impl.MessageBusImpl;

/**
 * The SellingService class is a micro-service which simulates a selling person in a Shoes store,
 * and therefore he is the middle-man, which receives and handles {@code PurchaseOrderRequest} from clients,
 * and it handles them it by trying to take the required shoe from the storage. If that operation was successful,
 * it will create a receipt and pass it to the client and file it in the store, and if the shoe was out of stock
 * it will send the store manager a restock request in order the make the shoes factory make more of that needed shoe.
 */

public class SellingService extends MicroService{
    private int _current_tick;
    private CountDownLatch cdl;
	
	/**
	 * The constructor defines the {@code SellingService} name, shared {@link CountDownLatch} 
	 * @param name - the name of the {@code SellingService}
	 * @param cdl -  a cross program shared object {@link CountDownLatch} to start all services at the same time
	 */
    public SellingService(String name, CountDownLatch cdl) {
        super(name);
        _current_tick = 1;
        this.cdl = cdl;
    }

    @Override
    protected void initialize() {
        this.subscribeBroadcast(TerminationBroadcast.class, terB->{
        	MessageBusImpl.writeToLog("SellingService " + getName() + " terminating");
        	this.terminate();
        });
        
        MessageBusImpl.writeToLog("Selling Service " + getName() + " started");
    	MessageBusImpl.writeToLog("SellingService " + getName() + " subscribing to tick broadcast");        
        this.subscribeBroadcast(TickBroadcast.class, _tick_broadcast->{
            _current_tick = _tick_broadcast.getTick();
        });
        
        subscribeRequest(PurchaseOrderRequest.class, _purchase_req -> {
            MessageBusImpl.writeToLog("SellingService " + getName() + " got a new purchase request from " + _purchase_req.getName());
            BuyResult resultOfSale;
            Receipt receipt;
            resultOfSale = Store.getInstance().take(_purchase_req.getShoe(), _purchase_req.getDiscount());
            if(resultOfSale.equals( BuyResult.NOT_ON_DISCOUNT))
                this.complete(_purchase_req, null);
            else {
                if (resultOfSale.equals(BuyResult.DISCOUNTED_PRICE)){
                    receipt = new Receipt(this.getName(), _purchase_req.getSenderName(), _purchase_req.getShoe(), true, _current_tick, _purchase_req.getTick(), 1);
                    Store.getInstance().file(receipt);
                    this.complete(_purchase_req, receipt);
                }
                else {
                    if(resultOfSale.equals(BuyResult.REGULAR_PRICE)){
                        receipt = new Receipt(this.getName(), _purchase_req.getSenderName(), _purchase_req.getShoe(), false, _current_tick, _purchase_req.getTick(), 1);
                        Store.getInstance().file(receipt);
                        this.complete(_purchase_req, receipt);
                    }
                    else {
                    	MessageBusImpl.writeToLog("SellingService " + getName() + " sending restock request");
                        this.sendRequest(new RestockRequest(_purchase_req.getShoe()), isSuccessful -> {
                            if (!isSuccessful){
                                this.complete(_purchase_req, null);
                            }
                            else{
                                Receipt someReceipt = new Receipt(this.getName(), _purchase_req.getSenderName(), _purchase_req.getShoe(), false, _current_tick, _purchase_req.getTick(), 1);
                                Store.getInstance().file(someReceipt);
                                this.complete(_purchase_req, someReceipt);
                            }
                        });
                    }
                }
            }
        });
        cdl.countDown();
    }
}