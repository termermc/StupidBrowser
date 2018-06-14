package net.termer.stupidbrowser;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.jsoup.Jsoup;

public class OpenOrSaveDialog extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private OpenOrSaveDialog _THIS_ = this;
	
	private String url = null;
	private String ct = null;
	
	private ActionListener viewImage = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if(Browser.isValidURL(url)) {
				try {
					ImageViewer.open(new URL(url));
					_THIS_.setVisible(false);
				} catch (MalformedURLException ex) {
					ex.printStackTrace();
				}
			}
		}
	};
	private ActionListener viewMedia = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if(Browser.isValidURL(url)) {
				new Async(new Runnable() {
					public void run() {
						try {
							Runtime.getRuntime().exec("vlc "+url);
						} catch (IOException e) {
							JOptionPane.showMessageDialog(null, "Error occured when opening media with VLC:\n"+e.getClass().toString()+": "+e.getMessage());
							e.printStackTrace();
						}
					}
				}).start();
				setVisible(false);
			}
		}
	};
	
	// Components
	private JLabel text = new JLabel("");
	private JButton saveButton = new JButton("Save");
	private JButton openButton = new JButton("Open");
	private JButton cancelButton = new JButton("Cancel");
	
	public OpenOrSaveDialog(String address, String type) {
		setIconImage(Browser._ICON_);
		
		url = address;
		ct = type;
		text.setText("The file \""+url.split("/")[url.split("/").length-1]+"\" is a "+ct+" document, what would you like to do?");
		
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_THIS_.setVisible(false);
			}
		});
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser(Browser._DOWNLOADS_);
				jfc.showSaveDialog(_THIS_);
				boolean ok = true;
				File f = jfc.getSelectedFile();
				if(f.exists()) {
					if(JOptionPane.showConfirmDialog(_THIS_, "The file \""+f.getName()+"\" already exists, replace it?")!=0) {
						ok = false;
					}
				}
				if(ok) {
					saveButton.setEnabled(false);
					try {
						FileOutputStream fout = new FileOutputStream(f);
						@SuppressWarnings("deprecation")
						BufferedInputStream urlConn = Jsoup.connect(url).ignoreContentType(true).validateTLSCertificates(false).ignoreHttpErrors(true).execute().bodyStream();
						while(urlConn.available()>0) {
							fout.write(urlConn.read());
						}
						fout.close();
						urlConn.close();
						JOptionPane.showMessageDialog(_THIS_, "File saved as "+f.getName());
						_THIS_.setVisible(false);
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(_THIS_, "Failed to download file");
						ex.printStackTrace();
					}
					saveButton.setEnabled(true);
				}
			}
		});
		
		getContentPane().setLayout(new FlowLayout());
		getContentPane().add(text);
		getContentPane().add(saveButton);
		getContentPane().add(openButton);
		getContentPane().add(cancelButton);
		
		setResizable(false);
		setTitle("Save");
		setSize(500,100);
	}
	
	public void openImage() {
		setVisible(true);
		openButton.setEnabled(true);
		openButton.removeActionListener(viewImage);
		openButton.removeActionListener(viewMedia);
		openButton.addActionListener(viewImage);
	}
	public void openMedia() {
		setVisible(true);
		openButton.setEnabled(true);
		openButton.removeActionListener(viewImage);
		openButton.removeActionListener(viewMedia);
		openButton.addActionListener(viewMedia);
	}
	public void openFile() {
		setVisible(true);
		openButton.setEnabled(false);
	}
	
	public String getFileURL() {
		return url;
	}
}
