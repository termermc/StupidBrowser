package net.termer.stupidbrowser;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.termer.tconfig.tConfig;

public class BrowserWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	
	// Back and forward buttons
	public JButton backBtn = new JButton("<");
	public JButton forwardBtn = new JButton(">");
	
	// Tabs list
	public ArrayList<WebpageTab> webpageTabs = new ArrayList<WebpageTab>();
	
	// This
	private BrowserWindow _THIS_ = this;
	
	// Menu bar
	public JMenuBar menuBar = new JMenuBar();
	
	// Tabs
	public JTabbedPane tabs = new JTabbedPane();
	
	// Address bar
	public JTextField addressBar = new JTextField(35);
	
	// Go button
	public JButton goButton = new JButton("Go");
	
	// Status bar
	public JLabel statusBar = new JLabel("Ready");
	
	public BrowserWindow(String url) {
		// Set the icon
		setIconImage(Browser._ICON_);
		
		// Set layout
		getContentPane().setLayout(new FlowLayout());
		
		// Setup tabs
		WebpageTab tab = new WebpageTab(url,this);
		webpageTabs.add(tab);
		tabs.addTab("New Tab", tab);
		
		// Setup menu bar
		JMenu browserMenu = new JMenu("Browser");
		JMenuItem newBrowserWindow = new JMenuItem("New window");
		newBrowserWindow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Browser.spawnBrowserWindow(Browser._HOMEPAGE_);
			}
		});
		JMenuItem newTab = new JMenuItem("New tab");
		newTab.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				spawnTab("about:newtab");
			}
		});
		JMenuItem closeTab = new JMenuItem("Close tab");
		closeTab.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(tabs.getTabCount()>1) {
					despawnTab((WebpageTab) tabs.getComponentAt(tabs.getSelectedIndex()));
				} else {
					Browser.despawnBrowserWindow(_THIS_);
				}
			}
		});
		JMenuItem setHomepage = new JMenuItem("Set homepage");
		setHomepage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String input = JOptionPane.showInputDialog("Please enter the new URL for your homepage:", Browser._HOMEPAGE_);
				if(Browser.isValidURL(input)) {
					try {
						tConfig.changeValue(new File(Browser._NAME_+"/settings.ini"), "homepage", input, ":", "#");
						Browser.reloadSettings();
						statusBar.setText("New homepage set to "+input);
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(_THIS_, "Error occured while setting homepage:\n"+ex.getClass().getName()+": "+ex.getMessage());
						ex.printStackTrace();
					}
				} else {
					if(input!=null) {
						JOptionPane.showMessageDialog(_THIS_, "Invalid URL, be sure to include HTTP:// in it");
					}
				}
			}
		});
		JMenuItem setDownloadsDir = new JMenuItem("Set downloads location");
		setDownloadsDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String input = JOptionPane.showInputDialog("Please enter a directory: ", Browser._DOWNLOADS_);
				if(input!=null) {
					File dir = new File(input);
					if(dir.isDirectory()) {
						try {
							tConfig.changeValue(new File(Browser._NAME_+"/settings.ini"), "downloads", input, ":", "#");
							Browser.reloadSettings();
							statusBar.setText("New downloads location set to "+input);
						} catch (IOException ex) {
							JOptionPane.showMessageDialog(_THIS_, "Error occured while setting downloads location:\n"+ex.getClass().getName()+": "+ex.getMessage());
							ex.printStackTrace();
						}
					} else {
						if(input!=null) {
							JOptionPane.showMessageDialog(_THIS_, "Path is not a directory");
						}
					}
				}
			}
		});
		JMenuItem setSearchURL = new JMenuItem("Set search URL");
		setSearchURL.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String input = JOptionPane.showInputDialog("Please enter the new URL for your search engine, using \"%s\" as a placeholder for your search term:", Browser._SEARCH_);
				if(Browser.isValidURL(input)) {
					if(input.contains("%s")) {
						try {
							tConfig.changeValue(new File(Browser._NAME_+"/settings.ini"), "search", input, ":", "#");
							Browser.reloadSettings();
							statusBar.setText("New search URL set to "+input);
						} catch (IOException ex) {
							JOptionPane.showMessageDialog(_THIS_, "Error occured while setting homepage:\n"+ex.getClass().getName()+": "+ex.getMessage());
							ex.printStackTrace();
						}
					} else {
						JOptionPane.showMessageDialog(_THIS_, "The URL does not contain \"%s\", which is replaced with the search term");
					}
				} else {
					if(input!=null) {
						JOptionPane.showMessageDialog(_THIS_, "Invalid URL, be sure to include HTTP:// in it");
					}
				}
			}
		});
		JMenuItem browserHelp = new JMenuItem(Browser._NAME_+" help");
		browserHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				spawnTab("about:help");
			}
		});
		JMenuItem aboutBrowser = new JMenuItem("About "+Browser._NAME_);
		aboutBrowser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				spawnTab("about:");
			}
		});
		JMenuItem addBookmark = new JMenuItem("Add bookmark");
		addBookmark.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				WebpageTab tab = (WebpageTab) tabs.getComponentAt(tabs.getSelectedIndex());
				AddBookmarkWindow.open(tab.title, tab.url);
			}
		});
		JMenuItem viewBookmarks = new JMenuItem("View bookmarks");
		viewBookmarks.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				spawnTab("about:bookmarks");
			}
		});
		
		browserMenu.add(newBrowserWindow);
		browserMenu.add(newTab);
		browserMenu.add(closeTab);
		
		JMenu editMenu = new JMenu("Edit");
		editMenu.add(setHomepage);
		editMenu.add(setDownloadsDir);
		editMenu.add(setSearchURL);
		
		JMenu helpMenu = new JMenu("Help");
		helpMenu.add(browserHelp);
		helpMenu.add(aboutBrowser);
		
		JMenu bookmarksMenu = new JMenu("Bookmarks");
		bookmarksMenu.add(viewBookmarks);
		bookmarksMenu.add(addBookmark);
		
		menuBar.add(browserMenu);
		menuBar.add(editMenu);
		menuBar.add(helpMenu);
		menuBar.add(bookmarksMenu);
		
		setJMenuBar(menuBar);
		
		// Add components
		getContentPane().add(backBtn);
		getContentPane().add(forwardBtn);
		getContentPane().add(addressBar);
		getContentPane().add(goButton);
		getContentPane().add(tabs);
		getContentPane().add(statusBar);
		
		// Setup window
		addWindowListener(new WindowListener() {
			public void windowActivated(WindowEvent e) {}
			public void windowClosed(WindowEvent e) {}
			public void windowClosing(WindowEvent e) {
				Browser.despawnBrowserWindow(_THIS_);
			}
			public void windowDeactivated(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}
			public void windowOpened(WindowEvent e) {}
		});
		
		// Add resize listener
		addComponentListener(new ComponentAdapter() {
		    public void componentResized(ComponentEvent e) {
		    	fixDimensions();
		    }
		});
		
		backBtn.setToolTipText("Go back");
		forwardBtn.setToolTipText("Go forward");
		
		backBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Async(new Runnable() {
					public void run() {
						((WebpageTab)tabs.getComponentAt(tabs.getSelectedIndex())).back();
					}
				}).start();
			}
		});
		forwardBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Async(new Runnable() {
					public void run() {
						((WebpageTab)tabs.getComponentAt(tabs.getSelectedIndex())).forward();
					}
				}).start();
			}
		});
		goButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Async(new Runnable() {
					public void run() {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						((WebpageTab)tabs.getComponent(tabs.getSelectedIndex())).setURL(addressBar.getText());
					}
				}).start();
			}
		});
		
		addressBar.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_ENTER) {
					new Async(new Runnable() {
						public void run() {
							WebpageTab tab = ((WebpageTab)tabs.getComponent(tabs.getSelectedIndex()));
							String tmp = addressBar.getText();
							if((!tmp.contains(".") && !tmp.trim().toLowerCase().startsWith("localhost")) || (tmp.contains(" ") && !tmp.contains("."))) {
								tab.setURL(Browser._SEARCH_.replaceAll("%s", tmp.trim()));
							} else {
								if(!tmp.startsWith("http")) tmp="http://"+tmp;
								tab.setURL(tmp);
							}
						}
					}).start();
				}
			}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
		});
		
		tabs.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if(e.getButton()==MouseEvent.BUTTON2 && tabs.getTabCount()>1) {
					new Async(new Runnable() {
						public void run() {
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							despawnTab((WebpageTab) tabs.getComponentAt(tabs.getSelectedIndex()));
						}
					}).start();
				}
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});
		tabs.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				addressBar.setText(((WebpageTab)tabs.getComponentAt(tabs.getSelectedIndex())).url);
				setTitle(((WebpageTab)tabs.getComponentAt(tabs.getSelectedIndex())).title+" - "+Browser._NAME_);
			}
		});
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setSize(600,500);
		setTitle(Browser._NAME_);
		setVisible(true);
//		setResizable(false);
	}
	
	public void spawnTab(String url) {
		WebpageTab tab = new WebpageTab(url,this);
		webpageTabs.add(tab);
		tabs.add("New Tab", tab);
	}
	public void despawnTab(WebpageTab tab) {
		webpageTabs.remove(tab);
		tabs.remove(tab);
		if(tabs.getTabCount()<1) {
			Browser.despawnBrowserWindow(this);
		}
	}
	
	public WebpageTab getTab(String url) {
		WebpageTab tab = null;
		for(WebpageTab t : webpageTabs) {
			if(StringFilter.same(t.getURL().toLowerCase(), url.toLowerCase())) {
				tab = t;
				break;
			}
		}
		return tab;
	}
	
	public void fixDimensions() {
		new Async(new Runnable() {
    		public void run() {
    			try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    			for(int i = 0; i < tabs.getTabCount(); i++) {
		        	((WebpageTab)tabs.getComponentAt(i)).jsp.setPreferredSize(new Dimension(
		        			_THIS_.getWidth()+Browser._TAB_WIDTH_OFFSET_,
		        			_THIS_.getHeight()+Browser._TAB_HEIGHT_OFFSET_));
		        	tabs.validate();
		        	tabs.repaint();
		        	int index = tabs.getSelectedIndex();
		        	tabs.setSelectedIndex(0);
		        	tabs.setSelectedIndex(index);
		        }
    		}
    	}).start();
	}
}
