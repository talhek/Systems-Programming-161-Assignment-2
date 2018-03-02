package bgu.spl.mics.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Message;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.RequestCompleted;

public class MessageBusImpl implements bgu.spl.mics.MessageBus {
	// this map will hold all of the requests that are sent, and a list of
	// MicroServices that are subscribed to each request
	private static Map<Class<? extends Request>, SynchronizedByRR<MicroService>> _mapOfRequests;
	// this map will hold all of the broadcasts that are sent, and a list of
	// MicroServices that are subscribed to each broadcast
	private static Map<Class<? extends Broadcast>, List<MicroService>> _mapOfBroadcasts;
	// this map will hold all of the MicroServices that are registered to the
	// program, and a queue of personal messages that each one holds
	private static Map<MicroService, LinkedBlockingQueue<Message>> _mapOfMicroservices;
	// that map will hold the requests that were sent, and all of the
	// MicroServices that are sending requests on the program
	private static Map<Request, MicroService> _mapOfReqSenders;
	
	private static class MessageBusHolder {
        private static MessageBusImpl instance = new MessageBusImpl();
    }
	// Logger initialization..
	private final static Logger LOGGER = Logger.getLogger(MessageBusImpl.class.getName());

	public static void writeToLog(Object object) {
		synchronized (LOGGER) {
			LOGGER.info(object.toString() + "\n");
		}
	}

	public static void writeToLog(String message) {
		synchronized (LOGGER) {
			LOGGER.warning(message + "\n");
		}
	}

	private MessageBusImpl() {
		_mapOfBroadcasts = new ConcurrentHashMap<Class<? extends Broadcast>, List<MicroService>>();
		_mapOfRequests = new ConcurrentHashMap<Class<? extends Request>, SynchronizedByRR<MicroService>>();
		_mapOfMicroservices = new ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>>();
		_mapOfReqSenders = new ConcurrentHashMap<Request, MicroService>();
	}

	public static MessageBusImpl getInstance() {
		return MessageBusHolder.instance;
	}

	@Override
	public void subscribeRequest(Class<? extends Request> type, MicroService m) {
		synchronized (type) {
			if (!_mapOfRequests.containsKey(type))
				_mapOfRequests.put(type, new SynchronizedByRR<>());
			_mapOfRequests.get(type).add(m);
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		synchronized (type) {
			if (!_mapOfBroadcasts.containsKey(type)) {
				ArrayList<MicroService> list = new ArrayList<MicroService>();
				_mapOfBroadcasts.put(type, list);
				_mapOfBroadcasts.get(type).add(m);
			}
			else
			{
				_mapOfBroadcasts.get(type).add(m);
			}
		}
	}

	@Override
	public <T> void complete(Request<T> r, T result) {
		try{
		if (!_mapOfMicroservices.containsKey(_mapOfReqSenders.get(r))) {
			writeToLog(r.getClass().getName() + "there is no such request to complete");	
		}
		else{
			RequestCompleted reqC = new RequestCompleted<>(r, result);
			MicroService m = _mapOfReqSenders.get(r);
			_mapOfMicroservices.get(m).add(reqC);
			_mapOfReqSenders.remove(r);
		}
		}
		catch(Exception e)
		{
			
		}
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		if (_mapOfBroadcasts.containsKey(b.getClass())) {
			for (int i = 0; i < _mapOfBroadcasts.get(b.getClass()).size(); i++) {
				_mapOfMicroservices.get(_mapOfBroadcasts.get(b.getClass()).get(i)).add(b);
			}
		}
	}

	@Override
	public boolean sendRequest(Request<?> r, MicroService requester) {
		if (!_mapOfMicroservices.containsKey(requester)) {
			writeToLog(requester.getName() + "this MicroService isn't even registered yet! it cannot send requests!");
			return false;
		}
		if (!_mapOfRequests.containsKey(r.getClass())) {
			writeToLog("no service to handle the request from: " + requester.getName());
			return false;
		}
		MicroService m = _mapOfRequests.get(r.getClass()).get();
		synchronized(m){
			_mapOfMicroservices.get(m).add(r);
			_mapOfReqSenders.put(r, requester);
			return true;
		}

	}

	@Override
	public void register(MicroService m) {
		LinkedBlockingQueue<Message> queue = new LinkedBlockingQueue<Message>();
		_mapOfMicroservices.put(m, queue);
	}

	@Override
	public void unregister(MicroService m) {
		if (_mapOfMicroservices.containsKey(m)) {
			while (!_mapOfMicroservices.get(m).isEmpty()) {
				_mapOfMicroservices.get(m).remove();
			}
		}
	}
	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		if (!_mapOfMicroservices.containsKey(m))
			throw new IllegalStateException("this micro-service was never registered!");
		return _mapOfMicroservices.get(m).take();
	}
}