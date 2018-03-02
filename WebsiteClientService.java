package bgu.spl.app.MicroServices;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import bgu.spl.app.NewDiscountBroadcast;
import bgu.spl.app.PurchaseOrderRequest;
import bgu.spl.app.PurchaseSchedule;
import bgu.spl.app.TerminationBroadcast;
import bgu.spl.app.TickBroadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.impl.MessageBusImpl;


/**
 * The WebsiteClientService class is a micro-service that simulates an online customer in the Shoe Store Website.
 * The WebsiteClientService can send purchase requests and afterwards when it received all of it purchases it will terminate,
 * and while sending purchase requests the WebsiteClientService can also subscribe and get notified about a new discount for 
 * an item it is interested in.
 */
 
public class WebsiteClientService extends MicroService {
    private String _client_name;
    private List<PurchaseSchedule> _list_purchases;
    private Set<String> wishList;
    private int _current_tick;
    private CountDownLatch cdl;

		/**
	 * The constructor sets the client's name, purchase schedule, wish list,
	 * and {@link CountDownLatch}
	 * @param _client_name - the name of the customer
	 * @param _list_purchases - the purchase requests schedule
	 * @param wishList - the shoe types that the client will buy only when 
	 * there is a discount on them.
	 * @param cdl - a shared {@link CountDownLatch} for all services
	 */
	 
    public WebsiteClientService(String name, String _client_name, List<PurchaseSchedule> _list_purchases, Set<String> wishList, CountDownLatch cdl) {
        super(name);
        this._client_name = _client_name;
        _current_tick = 0;
        this._list_purchases = new LinkedList<PurchaseSchedule>();
        this.wishList = new HashSet<String>();
        for(int i=0; i<_list_purchases.size(); i++)
            this._list_purchases.add(_list_purchases.get(i));
        for (String someWish : wishList) {
            this.wishList.add(someWish);
        }
        this.cdl = cdl;
    }

    @Override
    protected void initialize() {
                this.subscribeBroadcast(TerminationBroadcast.class, terB->{
                	MessageBusImpl.writeToLog("WebsiteClientService" + getName() + " terminating");
                	this.terminate();
                });
                
                MessageBusImpl.writeToLog("Website Client Service " + getName() + " started");
            	MessageBusImpl.writeToLog("WebsiteClientService " + getName() + " subscribing to TickBroadcast");                
                this.subscribeBroadcast(TickBroadcast.class, _tick_broadcast->{
                    _current_tick = _tick_broadcast.getTick();
                    if(!_list_purchases.isEmpty()){
	                    for(int i=0; i<_list_purchases.size(); i++) {
	                    	if(_list_purchases.get(i).getTick()==_current_tick) {
	                    		MessageBusImpl.writeToLog("Website Client Service " + getName() + " sending purchase request");
	                    		sendRequest(new PurchaseOrderRequest(_current_tick, _list_purchases.get(i).getShoeType(), _client_name, false, _client_name), _receipt -> {
	                                // Not sure what to do with the receipt - it's already filed @ store
	                            });
	                    	}
	                    }
                    }
                    else {
                    	if(wishList.isEmpty())
                    		terminate();
                    }
                });
                
                MessageBusImpl.writeToLog("WebsiteClientService " + getName() + " subscribing to NewDiscountBroadcast");
                this.subscribeBroadcast(NewDiscountBroadcast.class, _discount_broadcast->{
                	if(!wishList.isEmpty()){
	                    for(String someWish : wishList) {
	                    		if(someWish.equals(_discount_broadcast.getShoeType())){
	                    			MessageBusImpl.writeToLog("Website Client Service " + getName() + " sending purchase request");
	                    			sendRequest(new PurchaseOrderRequest(_current_tick, someWish, _client_name, true, _client_name), _receipt -> {
	                                    // Not sure what to do with the receipt - it's already filed @ store
	                                });
	                    		}
	                    }
                	} 
                	else {
                		if(_list_purchases.isEmpty())
                			terminate();
                	}
                });
                cdl.countDown();
    }
}