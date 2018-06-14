package net.termer.stupidbrowser;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class AddBookmarkWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private static AddBookmarkWindow current = null;
	
	// Text fields
	public JTextField titleField = new JTextField(18);
	public JTextField urlField = new JTextField(18);
	
	private AddBookmarkWindow() {
		super("Add Bookmark");
		
		setIconImage(Browser._ICON_);
		
		// Set layout
		getContentPane().setLayout(new FlowLayout());
		
		// Add button
		JButton addButton = new JButton("Add Bookmark");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(Browser.isValidURL(urlField.getText())) {
					try {
						Browser.addBookmark(titleField.getText(), urlField.getText());
						current.setVisible(false);
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(current, "Failed to add bookmark, check logs for details.");
						ex.printStackTrace();
					}
				} else {
					JOptionPane.showMessageDialog(current, "The URL you entered is not valid, be sure to include HTTP:// in it.");
				}
			}
		});
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				current.setVisible(false);
			}
		});
		
		// Add components
		getContentPane().add(new JLabel("Title"));
		getContentPane().add(titleField);
		getContentPane().add(new JLabel("URL"));
		getContentPane().add(urlField);
		getContentPane().add(addButton);
		getContentPane().add(cancelButton);
		
		// Setup window
		setSize(280,175);
		setResizable(false);
		setVisible(true);
	}
	
	public static void open(String title, String url) {
		if(current==null) current = new AddBookmarkWindow();
		
		current.titleField.setText(title);
		current.urlField.setText(url);
		
		current.setVisible(true);
	}
}
