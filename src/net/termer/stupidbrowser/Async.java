package net.termer.stupidbrowser;

public class Async extends Thread {
	private Runnable task = null;
	
	public Async(Runnable runnable) {
		task = runnable;
	}
	
	public void run() {
		task.run();
	}
}