package bgu.spl.app.MicroServices;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import bgu.spl.app.DiscountSchedule;
import bgu.spl.app.ManufacturingOrderRequest;
import bgu.spl.app.NewDiscountBroadcast;
import bgu.spl.app.RestockRequest;
import bgu.spl.app.Store;
import bgu.spl.app.TerminationBroadcast;
import bgu.spl.app.TickBroadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.impl.MessageBusImpl;

	
/**
 * The ManagementService class is a micro-service which simulates a real store Manager. it handles {@code RestockRequests},
 * which will order new items from the factory if any are needed. The ManagementService can also add discounts to shoes in the store
 * and send {@code NewDiscountBroadcast} to notify customers about them.
 */
 

public class ManagementService extends MicroService{
	private LinkedList<DiscountSchedule> _discounts;
	private Map<String, RestockRequest> _requests;
	private Map<String, Integer> _requests_saved;
	private int _current_tick;
	private CountDownLatch cdl;
	
		/**
	 * The constructor defines the manager with name, discount schedule, starting tick, and 
	 * a shared {@link CountDownLatch}.
	 * @param discounts - a list of the discounts
	 * @param cdl - a shared {@link CountDownLatch} for all services. 
	 */
	

	public ManagementService(List<DiscountSchedule> discounts, CountDownLatch cdl) {
		super("manager");
		_discounts = new LinkedList<DiscountSchedule>();
		_requests = new ConcurrentHashMap<String, RestockRequest>();
		_requests_saved =  new ConcurrentHashMap<String, Integer>();
		for(int i=0; i<discounts.size(); i++)
		{
			_discounts.add(discounts.get(i));
			Store.getInstance().add(discounts.get(i).getShoeType(), discounts.get(i).getAmount());
			Store.getInstance().addDiscount(discounts.get(i).getShoeType(), discounts.get(i).getAmount());
		}
		_current_tick = 0;
		this.cdl = cdl;
	}

	@Override
	protected void initialize() {
		this.subscribeBroadcast(TerminationBroadcast.class, terB->{
			MessageBusImpl.writeToLog("ManagementService " + getName() + " terminating");
        	this.terminate();
        });
		
		MessageBusImpl.writeToLog("ManagementService " + getName() + " started");
		MessageBusImpl.writeToLog("ManagementService " + getName() + " subscribing to tick broadcast");
		this.subscribeBroadcast(TickBroadcast.class, _tick_broadcast->{
            _current_tick = _tick_broadcast.getTick();
            for(int i=0; i<_discounts.size(); i++){
            	if(_discounts.get(i).getTick() == _current_tick)
            		sendBroadcast(new NewDiscountBroadcast(_discounts.get(i).getAmount(), _discounts.get(i).getShoeType()));
            }
        });
		
		this.subscribeRequest(RestockRequest.class, _restock_request ->{
			MessageBusImpl.writeToLog("ManagementService " + getName() + " got a new restock request");
			_restock_request.setAmount((_current_tick%5)+1);
			if(!_requests.containsKey(_restock_request.getShoeType())) {
				_requests.put(_restock_request.getShoeType(), _restock_request);
				sendRequest(new ManufacturingOrderRequest(_restock_request.getShoeType(), _restock_request.getAmount()), _receipt -> {
					this.complete(_restock_request, true);
				});
				_requests_saved.put(_restock_request.getShoeType(), ((_current_tick%5)));
			}
			else {
				if(_requests_saved.get(_restock_request.getShoeType()).intValue() == 0) {
					_requests_saved.remove((_restock_request.getShoeType()));
					_requests_saved.put((_restock_request.getShoeType()), ((_current_tick%5)));
					sendRequest(new ManufacturingOrderRequest(_restock_request.getShoeType(), _restock_request.getAmount()), _receipt -> {
						this.complete(_restock_request, true);
					});
				}
				else {
					int number = _requests_saved.get(_restock_request.getShoeType()).intValue();
					number = number - 1;
					Integer number1 = (Integer) number;
					_requests_saved.remove(_restock_request.getShoeType());
					_requests_saved.put(_restock_request.getShoeType(), number1);
				}
				
			}
			
			MessageBusImpl.writeToLog("ManagementService " + getName() + " sending manufacturing request");
			
			
		});
		cdl.countDown();
	}

}