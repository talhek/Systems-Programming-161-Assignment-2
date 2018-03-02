package bgu.spl.app;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import bgu.spl.app.MicroServices.ManagementService;
import bgu.spl.app.MicroServices.SellingService;
import bgu.spl.app.MicroServices.ShoeFactoryService;
import bgu.spl.app.MicroServices.TimeService;
import bgu.spl.app.MicroServices.WebsiteClientService;
import bgu.spl.mics.impl.MessageBusImpl;


/**
 * The ShoeStoreRunner class is the class which holds the main in our program and which accepts a JSON input file as an argument, reads it and runs the program with the
 * given data from the input file given from the Assignment staff.
 */
 
public class ShoeStoreRunner {
	public static void main(String [] args) {
		Store store = Store.getInstance();
		MessageBusImpl msgBus = MessageBusImpl.getInstance();
		Gson gson = new GsonBuilder().create();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(args[0]));
		} catch (FileNotFoundException e) {
			System.out.println("");
		} finally {
			
		}
		json jsonFile = new json();
		jsonFile = gson.fromJson(br, json.class);
		
		
		// Reading the shoes from the Json file
		shoeTypes[] myShoes =jsonFile.getInitialStorage();
		ShoeStorageInfo[] shoesStorage = new ShoeStorageInfo[myShoes.length];
		for(int i=0; i<jsonFile.getInitialStorage().length; i++)
			shoesStorage[i] = new ShoeStorageInfo(myShoes[i].getType(), myShoes[i].getAmount(),0);
		
		store.load(shoesStorage);
		// Reading the customers from the Json file
		customers[] allCustomers = new customers[jsonFile.getServices().getCustomers().length];
		for(int i=0; i<allCustomers.length; i++)
			allCustomers[i] = new customers();
		for(int i=0; i<allCustomers.length; i++)
			allCustomers[i] = jsonFile.getServices().getCustomers()[i];
		
		
		// Reading the factories from the Json file
		int _factories;
		_factories = jsonFile.getServices().getFactories();
		
		
		// Reading the manager from the Json file
		Manager _manager = jsonFile.getServices().getManager();
		
		// Reading the sellers from the Json file
		int _sellers;
		_sellers = jsonFile.getServices().getSellers();
		
		// Reading the time from the Json file
		time _time = jsonFile.getServices().getTime();
		
		
		CountDownLatch cdl = new CountDownLatch(_sellers + _factories + allCustomers.length + 1);
		new Thread(new TimeService(_time.getSpeed(), _time.getDuration(), cdl)).start();
		// Creating the threads: [ShoeFactoryService]'s
		for(int i=0; i<_factories; i++)
			new Thread(new ShoeFactoryService("Factory"+i, cdl)).start();
		
		// Creating the runnable [WebsiteClientService]'s
		WebsiteClientService[] allClients = new WebsiteClientService[allCustomers.length];
		for(int i=0; i<allCustomers.length; i++){
			List<Purchase> listOfSchedules = new LinkedList<Purchase>();
			for(int j=0; j<allCustomers[i].getpurchaseSchedule().length; j++)
				listOfSchedules.add(allCustomers[i].getpurchaseSchedule()[j]);
			List<PurchaseSchedule> listOfActualSchedules = new LinkedList<PurchaseSchedule>();
			for(int j=0; j<listOfSchedules.size(); j++){
				listOfActualSchedules.add(new PurchaseSchedule(listOfSchedules.get(j).getShoeType(), listOfSchedules.get(j).getTick()));
			}
			Set<String> wishList = new HashSet<String>();
			for(int j=0; j<allCustomers[i].getWishList().length; j++)
				wishList.add(allCustomers[i].getWishList()[j]);
			new Thread(new WebsiteClientService("Client "+allCustomers[i].getname(), allCustomers[i].getname(), listOfActualSchedules, wishList, cdl)).start();
		}


		// Creating the runnable [SellingService]'s
		SellingService[] allSellers = new SellingService[_sellers];
		for(int i=0; i<_sellers; i++)
			new Thread(new SellingService("SellingService"+i, cdl)).start();
		
		// Creating the runnable ManagementService
		List<DiscountSchedule> listOfDiscounts = new LinkedList<DiscountSchedule>();
		for(int i=0; i<_manager.getDiscountSchedule().length; i++)
			listOfDiscounts.add(new DiscountSchedule(_manager.getDiscountSchedule()[i].getShoeType(), _manager.getDiscountSchedule()[i].getTick(), _manager.getDiscountSchedule()[i].getAmount()));
		new Thread(new ManagementService(listOfDiscounts, cdl)).start();
	}
}
