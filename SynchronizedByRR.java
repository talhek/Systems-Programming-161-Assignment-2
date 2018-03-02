package bgu.spl.mics.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SynchronizedByRR<V> {
	
	List<V> synlist = Collections.synchronizedList(new ArrayList<V>());
	int size = 0;
	MessageBusImpl msgBus1;

	public synchronized V get() {
		int _indexByroundRobModulu;

		_indexByroundRobModulu= this.getIndexByModulu();
		V obj = this.synlist.get(_indexByroundRobModulu);
		return obj;
	}

	public void remove(V c) {
		this.synlist.remove(c);
		this.size = this.size - 1;
	}

	public void add(V c) {
		if (c == null){
			msgBus1.writeToLog("tried to add a null object!");
		}
		else{
			this.synlist.add(c);
			size = size+1;
		}
	}

	public boolean isEmpty() {
		return this.size == 0;
	}
	private int getIndexByModulu(){
		return ((this.size++) % (this.synlist.size()));
	}
}
