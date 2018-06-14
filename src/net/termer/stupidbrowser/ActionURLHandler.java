package net.termer.stupidbrowser;

public interface ActionURLHandler {
	/**
	 * The method that is called when the assigned action URL is clicked
	 * @param param The URL's parameter
	 */
	public void handle(String param, WebpageTab tab);
}
