package bgu.spl.app.MicroServices;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import bgu.spl.app.ManufacturingOrderRequest;
import bgu.spl.app.Receipt;
import bgu.spl.app.Store;
import bgu.spl.app.TerminationBroadcast;
import bgu.spl.app.TickBroadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.impl.MessageBusImpl;
/**
 * The ShoeFactoryService is a micro-service that simulates a shoe factory that manufactures shoes for the store micro-service
 * in our program. The ShoeFactoryService handles the {@code ManufacturingOrderRequest}. When done manufacturing,
 * this micro-service completes the request with a receipt. 
 */
public class ShoeFactoryService extends MicroService{
	private int _current_tick;
	private CountDownLatch cdl;
	
		/**
	 * The constructor sets the {@code ShoeFactoryService} name, starting tick, and {@link CountDownLatch}
	 * @param name - the name of the {@code ShoeFactoryService}
	 * @param cdl - a shared {@link CountDownLatch} for all micro-services
	 */
	
	public ShoeFactoryService(String name, CountDownLatch cdl) {
		super(name);
		_current_tick = 0;
		this.cdl = cdl;
	}

	@Override
	protected void initialize() {
		LinkedBlockingQueue<ManufacturingOrderRequest> _requests_queue = new LinkedBlockingQueue<ManufacturingOrderRequest>();
		this.subscribeBroadcast(TerminationBroadcast.class, terB->{
			MessageBusImpl.writeToLog("ShoeFactoryService " + getName() + " terminating");
        	this.terminate();
        });
		
		MessageBusImpl.writeToLog("ShoeFactoryService " + getName() + " subscribing to tick broadcast");		
		this.subscribeBroadcast(TickBroadcast.class, _tick_broadcast->{
            _current_tick = _tick_broadcast.getTick();
            if(!_requests_queue.isEmpty()) {
            	if(_requests_queue.peek().getAmount()>=1){
            		Store.getInstance().add(_requests_queue.peek().getShoeType(), 1);
            		_requests_queue.peek().decAmount();
            	}
            	else {
            		Receipt _receipt = new Receipt("Factory", "Store", _requests_queue.peek().getShoeType(), false, _current_tick, _current_tick-(_requests_queue.peek().getOriginalAmount()-_requests_queue.peek().getAmount()), _requests_queue.peek().getAmount());
					Store.getInstance().file(_receipt);
					this.complete(_requests_queue.peek(), _receipt);
					try {
						_requests_queue.take();
					} catch (Exception e) {
						e.printStackTrace();
					}
            	}
            }
        });
		
		MessageBusImpl.writeToLog("ShoeFactoryService " + getName() + " subscribing to manufacturing");
		this.subscribeRequest(ManufacturingOrderRequest.class, _manufacturing_request -> {
			_requests_queue.add(_manufacturing_request);
		});
		cdl.countDown();
	}

}
