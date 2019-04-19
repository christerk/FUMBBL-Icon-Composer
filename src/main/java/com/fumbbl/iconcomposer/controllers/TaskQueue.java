package com.fumbbl.iconcomposer.controllers;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TaskQueue {

	private ExecutorService threadPool;
	private Collection<Runnable> queue;

	private Controller controller;
	
	private long completeTasks = 0;
	private long numTasks = 0;
	
	public TaskQueue(Controller controller) {
		this.controller = controller;
		threadPool = Executors.newSingleThreadExecutor();
	}
	
	public void execute(Runnable task) {
		if (queue == null) {
			threadPool.execute(task);
		} else {
			queue.add(task);
		}
	}

	public void shutdownNow() {
		threadPool.shutdownNow();
	}

	public void startProgress() {
		execute(new Runnable() {

			@Override
			public void run() {
			}
			
		});
	}

	public double getProgress() {
		return 1;
	}

	public void stopProgress() {
	}

	public void startBatch() {
		queue =  new LinkedList<Runnable>();
	}

	public void runBatch() {
		if (queue != null) {
			numTasks = queue.size();
			if (numTasks == 0) {
				return;
			}

			completeTasks = 0;
			for (Runnable r : queue) {
				threadPool.execute(new Runnable() {
					@Override
					public void run() {
						r.run();
						completeTasks++;
						controller.onProgress(((double)completeTasks) / ((double)numTasks), completeTasks >= numTasks);
					}
					
				});
			}
			queue = null;
		}
	}
}
