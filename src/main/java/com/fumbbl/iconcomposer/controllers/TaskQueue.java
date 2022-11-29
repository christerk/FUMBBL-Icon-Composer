package com.fumbbl.iconcomposer.controllers;

import com.fumbbl.iconcomposer.TaskManager;

import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskQueue {

	private final ExecutorService threadPool;
	private final Stack<LinkedList<Runnable>> queue;

	private final TaskManager manager;
	
	public TaskQueue(TaskManager manager) {
		this.manager = manager;
		threadPool = Executors.newSingleThreadExecutor();
		queue = new Stack<>();
	}
	
	public void execute(Runnable task) {
		if (queue.empty()) {
			threadPool.execute(task);
		} else {
			queue.elementAt(0).add(task);
		}
	}

	public void shutdownNow() {
		threadPool.shutdownNow();
	}

	public void startProgress() {
		execute(() -> {
		});
	}

	public double getProgress() {
		return 1;
	}

	public void stopProgress() {
	}

	public void startBatch() {
		queue.push(new LinkedList<>());
	}

	public void runBatch(Runnable completedCallback) {
		if (!queue.empty()) {
			LinkedList<Runnable> currentBatch = queue.pop();

			int numTasks = currentBatch.size();
			if (numTasks == 0) {
				manager.onProgress(1, true);
				return;
			}

			final int[] completeTasks = {0};
			for (Runnable r : currentBatch) {
				threadPool.execute(() -> {
					try {
						r.run();
					} catch (Exception e)
					{
						System.out.println(e.getMessage());
					}
					completeTasks[0]++;
					manager.onProgress(((double) completeTasks[0]) / ((double)numTasks), completeTasks[0] >= numTasks);
				});
			}
		}
	}
}
