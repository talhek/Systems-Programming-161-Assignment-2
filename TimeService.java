package bgu.spl.app.MicroServices;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import bgu.spl.app.Store;
import bgu.spl.app.TerminationBroadcast;
import bgu.spl.app.TickBroadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.impl.MessageBusImpl;

/**
 * The TimeService class is a micro-service which handles the clock ticks in the program.
 * It is responsible for notifying those ticks to every other micro-service that is subscribed to {code@ TickBroadcast}.
 */
 
public class TimeService extends MicroService {
	int speed;
	int duration;
	private int tick;
	Timer time;
	private CountDownLatch cdl;
	
		/**
	 * 
	 * @param speed - number of milliseconds each clock tick takes
	 * @param duration - number of ticks before termination
	 * @param cdl - a {@link CountDownLatch} used to awaite all micro-services
	 */
	 
	public TimeService(int speed, int duration, CountDownLatch cdl) {
		super("timer");
		this.speed = speed;
		this.duration= duration;
		tick = 1;
		this.cdl = cdl;
	}

	/**
	 * The ShoeTick class runs the time clock as long as current tick <= duration.
	 * The ShoeTick sends {@code TickBroadcast} to notify all other micro-services about the program current tick.
	 * When the TimerService tick reaches it's duration, the ShoeTick sends a termination.
	 */
	 
	class ShoeTick extends TimerTask {

		@Override
		public void run() {
			
			if(tick<=duration) {
					TimeService.this.sendBroadcast(new TickBroadcast(tick));
					tick++;
			}
			else{
				sendBroadcast(new TerminationBroadcast());
				Store.getInstance().print();
				MessageBusImpl.writeToLog("TimeService " + getName() + " sending termination broadcast!");
				time.cancel();
			}
		}
		
	}
		/**
	 *  The method schedules the program global tick system at fixed rate.
	 * 	The method uses {@link CountDownLatch} to await for all working threads before starting the timer,
	 * causing them to run at once.
	 *
	 */
	 
	@Override
	protected void initialize() {
		try {
			cdl.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		MessageBusImpl.writeToLog("TimeService" + getName() + " started");
		this.subscribeBroadcast(TerminationBroadcast.class, terB->{
			MessageBusImpl.writeToLog("TimeService " + getName() + " terminating");
			time.cancel();
        	this.terminate();
        });
		
		time=new Timer();
		ShoeTick ticker1 = new ShoeTick();
		time.scheduleAtFixedRate(ticker1, 0, speed);
	}

}