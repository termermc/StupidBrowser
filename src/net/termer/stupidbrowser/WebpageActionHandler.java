package net.termer.stupidbrowser;

public class WebpageActionHandler {
	/**
	 * The WebpageTab that owns this handler
	 */
	@SuppressWarnings("unused")
	private WebpageTab _TAB_ = null;
	
	/**
	 * Sets the WebpageTab that owns this handler
	 * @param tab The WebpageTab that owns this handler
	 */
	public void setWebpageTab(WebpageTab tab) {
		_TAB_ = tab;
	}
	
	/**
	 * Called when a webpage's URL changes
	 * @param url The URL the webpage changed to
	 * @return Whether the normal action should be executed
	 */
	public boolean onURLChange(String url) {
		return true;
	}
	/**
	 * Called when a user clicks a link on the webpage
	 * @param url The URL of the link clicked
	 * @return Whether the normal action should be executed
	 */
	public boolean onLinkClick(String url) {
		return true;
	}
}
