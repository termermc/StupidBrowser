package net.termer.stupidbrowser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import net.termer.tconfig.tConfig;

public class Browser {
	// TLD List
	public static String[] _TLDS_ = {"com","net","org","edu","info","co","uk","us","me","onion"};
	
	// Action URL handlers
	public static HashMap<String,ActionURLHandler> _ACTION_URL_HANDLERS_ = new HashMap<String,ActionURLHandler>();
	
	// Tab dimension tweaks
	public static int _TAB_WIDTH_OFFSET_ = -20;
	public static int _TAB_HEIGHT_OFFSET_ = -160;
	
	// Settings map
	public static HashMap<String,String> _SETTINGS_ = new HashMap<String,String>();
	
	// Bookmarks
	public static HashMap<String,String> _BOOKMARKS_ = new HashMap<String,String>();
	
	// Browser name
	public static String _NAME_ = "Stupid Browser";
	
	// Homepage
	public static String _HOMEPAGE_ = "https://status.termer.net/homepage/";
	
	// Downloads dir
	public static String _DOWNLOADS_ = _NAME_+"/downloads/";
	
	// Search URL
	public static String _SEARCH_ = "https://searx.me/search?q=%s";
	
	// Browser windows
	private static ArrayList<BrowserWindow> _WINDOWS_ = new ArrayList<BrowserWindow>();
	
	// Browser Icon
	public static BufferedImage _ICON_ = null;
	
	public static void main(String[] args) {
		// Set look and feel
		try {
			UIManager.setLookAndFeel(UIManager.getInstalledLookAndFeels()[3].getClassName());
		} catch(Exception e) {
			System.out.println("Failed to set look and feel, using default look.");
		}
		
		// Load icon
		InputStream iconIs = Browser.class.getResourceAsStream("/resources/icon.png");
		try {
			_ICON_ = ImageIO.read(iconIs);
		} catch (IOException ex) {
			System.err.println("Failed to load browser icon");
			ex.printStackTrace();
		}
		
		// Load settings
		try {
			reloadSettings();
		} catch (IOException e) {
			System.err.println("Failed to create file system, do you have the correct permissions?");
			e.printStackTrace();
			System.exit(1);
		}
		
		// Specify URL to begin with
		String startURL = _HOMEPAGE_;
		
		// Check if URL is specified
		if(args.length > 0) {
			// Check if URL is valid
			String url = args[0];
			if(!url.startsWith("http://") && !url.startsWith("https://")) {
				url = "http://"+url;
			}
			if(isValidURL(url)) {
				System.out.println("URL specified in arguments, visiting it");
				startURL = url;
			} else {
				System.out.println("URL is invalid, ignoring");
			}
		} else {
			System.out.println("No URL specified in args");
		}
		
		System.out.println("Starting browser...");
		
		// Registering action URL handlers
		addActionURLHandler("edit_bookmark", new ActionURLHandler() {
			public void handle(String param, WebpageTab tab) {
				AddBookmarkWindow.open(param.split("///")[0], param.split("///")[1]);
			}
		});
		addActionURLHandler("delete_bookmark", new ActionURLHandler() {
			public void handle(String param, WebpageTab tab) {
				try {
					removeBookmark(param);
					tab.reload();
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(tab, "Failed to delete bookmark \""+param+"\"");
				}
			}
		});
		
		System.out.println("Opening URL \""+startURL+"\"");
		spawnBrowserWindow(startURL);
		System.out.println("Started.");
	}
	
	public static BrowserWindow spawnBrowserWindow(String url) {
		BrowserWindow window = new BrowserWindow(url);
		_WINDOWS_.add(window);
		return window;
	}
	public static void despawnBrowserWindow(BrowserWindow window) {
		_WINDOWS_.remove(window);
		window.setVisible(false);
		window.dispose();
		if(_WINDOWS_.size()<1) {
			System.exit(0);
		}
	}
	public static BrowserWindow[] getBrowserWindows() {
		return _WINDOWS_.toArray(new BrowserWindow[0]);
	}
	
	public static void reloadSettings() throws IOException {
		// Setup file system
		File browserDir = new File(_NAME_+"/downloads/");
		if(!browserDir.exists()) {
			browserDir.mkdirs();
		}
		File settingsFile = new File(_NAME_+"/settings.ini");
		if(!settingsFile.exists()) {
			settingsFile.createNewFile();
			
			// Define settings fields
			HashMap<String,String> tmpMap = new HashMap<String,String>();
			tmpMap.put("homepage", _HOMEPAGE_);
			tmpMap.put("downloads", _NAME_+"/downloads/");
			tmpMap.put("search", "https://searx.me/search?q=%s");
			
			tConfig.createConfig(settingsFile, tmpMap, ":");
		}
		File bookmarksFile = new File(_NAME_+"/bookmarks.ini");
		if(!bookmarksFile.exists()) {
			bookmarksFile.createNewFile();
		}
		
		// Load settings
		_SETTINGS_ = tConfig.parseConfig(settingsFile, ":", "#");
		
		// Load bookmarks
		_BOOKMARKS_ = tConfig.parseConfig(bookmarksFile, ":", "#");
		
		// Load values
		if(_SETTINGS_.containsKey("homepage")) {
			_HOMEPAGE_ = _SETTINGS_.get("homepage");
		}
		if(_SETTINGS_.containsKey("downloads")) {
			_DOWNLOADS_ = _SETTINGS_.get("downloads");
		}
		if(_SETTINGS_.containsKey("search")) {
			_SEARCH_ = _SETTINGS_.get("search");
		}
	}
	
	@SuppressWarnings("unused")
	public static boolean isValidURL(String url) {
		boolean valid = true;
		try {
			URL check = new URL(url);
		} catch (MalformedURLException e) {
			valid = false;
		}
		return valid;
	}
	
	public static void addActionURLHandler(String actionName, ActionURLHandler handler) {
		_ACTION_URL_HANDLERS_.put(actionName, handler);
	}
	
	public static void addBookmark(String title, String url) throws IOException {
		_BOOKMARKS_.put(title, url);
		tConfig.createConfig(new File(_NAME_+"/bookmarks.ini"), _BOOKMARKS_, ":");
		reloadSettings();
	}
	public static void removeBookmark(String title) throws IOException {
		_BOOKMARKS_.remove(title);
		tConfig.createConfig(new File(_NAME_+"/bookmarks.ini"), _BOOKMARKS_, ":");
		reloadSettings();
	}
}
