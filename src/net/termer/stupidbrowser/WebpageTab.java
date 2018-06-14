package net.termer.stupidbrowser;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class WebpageTab extends JPanel {
	private static final long serialVersionUID = 1L;
	
	// Webpage title
	public String title = Browser._NAME_;
	
	// This
	private WebpageTab _THIS_ = this;
	
	// Webpage action handler
	private WebpageActionHandler actionHandler = null;
	
	// Page URL
	public String url = null;
	
	// Webpage
	private JEditorPane webpage = new JEditorPane();
	// Browser window
	private BrowserWindow bw = null;
	
	// Temp Hyperlink Event
	private String tmpE = null;
	
	// Temp URL
	private String tmpURL = null;
	
	// History
	private ArrayList<String> history = new ArrayList<String>();
	private int historyIndex = 0;
	
	// Scroll Pane
	public JScrollPane jsp = new JScrollPane(webpage);
	
	// Hyperlink listener
	HyperlinkListener hll = new HyperlinkListener() {
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if(e.getEventType()==EventType.ACTIVATED) {
				boolean proceed = true;
				if(actionHandler!=null) {
					proceed = actionHandler.onLinkClick(e.getDescription()+e.getURL().toString());
				}
				tmpE = e.getDescription();
				if(proceed) {
					if(e.getInputEvent().isControlDown()) {
						new Async(new Runnable() {
							public void run() {
								bw.spawnTab(tmpE);
							}
						}).start();
					} else if(e.getInputEvent().isShiftDown()) {
						new Async(new Runnable() {
							public void run() {
								Browser.spawnBrowserWindow(tmpE);
							}
						}).start();
						Browser.spawnBrowserWindow(e.getDescription());
					} else {
						new Async(new Runnable() {
							public void run() {
								setURL(tmpE);
							}
						}).start();
					}
				}
			} else if(e.getEventType()==EventType.ENTERED) {
				bw.statusBar.setText(e.getDescription());
				webpage.setCursor(new Cursor(Cursor.HAND_CURSOR));
			} else if(e.getEventType()==EventType.EXITED) {
				bw.statusBar.setText("Ready");
				webpage.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
	};
	
	public WebpageTab(String address, BrowserWindow browserWindow) {
		history.add(address);
		bw = browserWindow;
		url = address;
		webpage.setEditable(false);
		webpage.setContentType("text/html");
		
		webpage.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if(e.isControlDown()) {
					if(e.getKeyCode()==82) {
						reload();
					} else if(e.getKeyCode()==87) {
						bw.despawnTab(_THIS_);
					} else if(e.getKeyCode()==84) {
						bw.spawnTab("about:newtab");
					} else if(e.getKeyCode()==68) {
						AddBookmarkWindow.open(title, url);
					}
				}
				if(e.getKeyCode()==116) {
					reload();
				}
			}
			public void keyReleased(KeyEvent e) {
				
			}
			public void keyTyped(KeyEvent e) {
				
			}
			
		});
		webpage.addHyperlinkListener(hll);
		jsp.setPreferredSize(new Dimension(bw.getWidth()+Browser._TAB_WIDTH_OFFSET_,bw.getHeight()+Browser._TAB_HEIGHT_OFFSET_));
		setURL(address);
		add(jsp);
	}
	
	@SuppressWarnings("deprecation")
	public void setURL(String address) {
		bw.statusBar.setText("Loading "+address+"...");
		boolean proceed = true;
		if(actionHandler!=null) {
			proceed = actionHandler.onURLChange(address);
		}
		if(proceed) {
			if(address.trim().startsWith("about:")) {
				webpage.setContentType("text/html");
				webpage.setText("<style>\n"
						+ "body {\n"
						+ "background: white;\n"
						+ "color: black;\n"
						+ "}\n"
						+ "h1 {\n"
						+ "color: black;\n"
						+ "}\n"
						+ "p {\n"
						+ "color: black;\n"
						+ "}\n"
						+ "i {\n"
						+ "color: black;\n"
						+ "}\n"
						+ "</style>");
				try {
					webpage.setText(ResourceLoader.getInternalWebpage(address.trim().substring(address.trim().indexOf(":")+1)));
				} catch (IOException e) {
					try {
						webpage.setText(ResourceLoader.getInternalWebpage("404"));
					} catch (IOException ex) {
						ex.printStackTrace();
						webpage.setText("<h1>Error</h1><p>"+Browser._NAME_+" failed to load the required internal webpage.<br>Check console for errors.</p>");
					}
				}
				boolean isNew = true;
				for(String histURL : history) {
					if(StringFilter.same(address.trim(), histURL)) {
						isNew = false;
						break;
					}
				}
				if(isNew) {
					historyIndex++;
					if(historyIndex<history.size()) {
						ArrayList<String> tmpHist = new ArrayList<String>();
						for(int i = 0; i < historyIndex; i++) {
							tmpHist.add(history.get(i));
						}
						history = tmpHist;
					}
					history.add(address.trim());
				}
				url = address.trim();
				if(bw.tabs.getSelectedIndex()==bw.tabs.indexOfComponent(this)) {
					bw.addressBar.setText(address.trim());
					bw.setTitle("New Tab - "+Browser._NAME_);
				}
				title = "New Tab";
				bw.statusBar.setText("Ready");
			} if(address.trim().startsWith("action:")) {
				String trim = address.trim().substring(address.trim().indexOf(':')+1);
				if(trim.contains(":")) {
					String name = trim.split(":")[0];
					String param = trim.substring(trim.indexOf(":")+1);
					if(Browser._ACTION_URL_HANDLERS_.containsKey(name)) {
						Browser._ACTION_URL_HANDLERS_.get(name).handle(param, this);
					}
				}
			} else {
				String full = address;
				if(full.startsWith("//")) full="http:"+full;
				if(full.startsWith("/")) {
					full = "http://"+url.replaceAll("https://", "").replaceAll("http://", "").split("/")[0]+address;
				} else {
					if(!full.startsWith("http://") && !full.startsWith("https://")) {
						boolean site = false;
						for(String tld : Browser._TLDS_) {
							if(full.toLowerCase().contains('.'+tld)) {
								site = true;
								break;
							}
						}
						if(site) {
							full = "http://"+full;
						} else {
							full = history.get(historyIndex)+"/"+full;
						}
					}
				}
				if(Browser.isValidURL(full)) {
					try {
						Response conn = null;
						try {
							conn = Jsoup.connect(full).validateTLSCertificates(false).followRedirects(true).ignoreContentType(true).ignoreHttpErrors(true).execute();
						} catch(IOException e) {
							bw.statusBar.setText(e.getMessage());
							String stackTrace = "";
							for(StackTraceElement ste : e.getStackTrace()) {
								stackTrace+="at "+ste.getClassName()+"."+ste.getMethodName()+"("+ste.getFileName()+":"+Integer.toString(ste.getLineNumber())+")\n";
							}
							setTitle(StringFilter.substring("Connection Error", 15));
							bw.setTitle("Connection Error - "+Browser._NAME_);
							webpage.setText("<html>"
										  + "<head>"
										  + "<title>Connection Error</title>"
										  + "</head>"
										  + "<body>"
										  + "<h1>Connection Error</h1>"
										  + "<p style=\"color:black\">"+Browser._NAME_+" failed to load the URL because of the following error:<br><br></p>"
										  + "<code>"+e.getClass().getName()+": "+e.getMessage()+"</code><br><div style=\"margin-left:25px;\"><code>"+stackTrace.replaceAll("\n", "<br>")+"</code></div>"
										  + "</body>"
										  + "</html>");
						}
						if(conn!=null) {
							String type = conn.contentType();
							if(type.contains(";")) {
								type = type.split(";")[0];
							}
							if(type.startsWith("text/")) {
								webpage.setText("<style>body {background:white;color:black;}</style>");
								bw.statusBar.setText("Parsing webpage...");
								Document doc = conn.parse();
								Elements titles = doc.getElementsByTag("title");
								if(titles.size()>0) {
									title = titles.get(0).html();
									if(bw.tabs.indexOfComponent(_THIS_)==bw.tabs.getSelectedIndex()) {
										bw.setTitle(titles.get(0).html()+" - "+Browser._NAME_);
									}
									new Async(new Runnable() {
										public void run() {
											try {
												Thread.sleep(500);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
											try {
												setTitle(StringFilter.substring(titles.get(0).html(),15));
											} catch(Exception e) {
												e.printStackTrace();
											}
										}
									}).start();
								} else {
									title = Browser._NAME_;
									new Async(new Runnable() {
										public void run() {
											try {
												Thread.sleep(500);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
											setTitle(Browser._NAME_);
										}
									}).start();
								}
								bw.statusBar.setText("Rendering webpage...");
								webpage.setContentType(type);
								webpage.setText(doc.html());
								if(bw.tabs.getSelectedIndex()==bw.tabs.indexOfComponent(this)) {
									bw.addressBar.setText(conn.url().toString());
								}
								url = conn.url().toString();
								boolean isNew = true;
								for(String histURL : history) {
									if(StringFilter.same(conn.url().toString(), histURL)) {
										isNew = false;
										break;
									}
								}
								if(isNew) {
									historyIndex++;
									if(historyIndex<history.size()) {
										ArrayList<String> tmpHist = new ArrayList<String>();
										for(int i = 0; i < historyIndex; i++) {
											tmpHist.add(history.get(i));
										}
										history = tmpHist;
									}
									history.add(conn.url().toString());
								}
								if(conn.url().toString().contains("youtube.com/watch?v=")) {
									if(JOptionPane.showConfirmDialog(this, "Would you like to play this video in VLC?")==0) {
										tmpURL = conn.url().toString();
										new Async(new Runnable() {
											public void run() {
												try {
													Runtime.getRuntime().exec("vlc "+tmpURL);
													tmpURL = null;
												} catch (IOException e) {
													e.printStackTrace();
													JOptionPane.showMessageDialog(_THIS_, Browser._NAME_+" failed to open the video in VLC, is it installed?");
												}
											}
										}).start();
									}
								}
								bw.statusBar.setText("Ready");
							} else if(type.startsWith("image/")) {
								new OpenOrSaveDialog(conn.url().toString(),type).openImage();
								bw.statusBar.setText("Ready");
							} else if(type.startsWith("audio/") || type.startsWith("video/")) {
								new OpenOrSaveDialog(conn.url().toString(),type).openMedia();
								bw.statusBar.setText("Ready");
							} else {
								new OpenOrSaveDialog(conn.url().toString(),type).openFile();
							}
						}
					} catch (IOException e) {
						System.err.println("Error connecting to URL "+address);
						e.printStackTrace();
					}
				}
			}
		} else {
			bw.statusBar.setText("Action cancelled by plugin");
		}
	}
	public String getURL() {
		return url;
	}
	
	public void setWebpageActionHandler(WebpageActionHandler handler) {
		actionHandler = handler;
		handler.setWebpageTab(this);
	}
	public void clearWebpageActionHandler() {
		actionHandler = null;
	}
	public void setTitle(String title) {
		bw.tabs.setTitleAt(bw.tabs.indexOfComponent(this), title);
	}
	public void back() {
		if(historyIndex>0&&historyIndex<history.size()) {
			historyIndex--;
			setURL(history.get(historyIndex));
		}
	}
	public void forward() {
		if(historyIndex<history.size()-1 && historyIndex>-1) {
			historyIndex++;
			setURL(history.get(historyIndex));
		}
	}
	public void reload() {
		setURL(history.get(historyIndex));
	}
}
