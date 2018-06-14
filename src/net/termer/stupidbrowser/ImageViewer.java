package net.termer.stupidbrowser;

import java.awt.Image;
import java.io.File;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

public class ImageViewer extends JFrame {
	private static final long serialVersionUID = 1L;

	private ImageViewer(File imgFile) {
		super(imgFile.getName()+" - "+Browser._NAME_);
		setIconImage(Browser._ICON_);
		try {
			Image img = (ImageIO.read(imgFile));
			setIconImage(img);
			ImageIcon icon = new ImageIcon(img);
			JLabel imgComp = new JLabel(icon);
			getContentPane().add(new JScrollPane(imgComp));
		} catch(Exception e) {
			JOptionPane.showMessageDialog(this, "Could not display image");
			setVisible(false);
		}
		
		setSize(400,400);
		setVisible(true);
	}
	private ImageViewer(URL imgURL) {
		super(imgURL.toString()+" - "+Browser._NAME_);
		try {
			Image img = (ImageIO.read(imgURL));
			setIconImage(img);
			ImageIcon icon = new ImageIcon(img);
			JLabel imgComp = new JLabel(icon);
			getContentPane().add(new JScrollPane(imgComp));
		} catch(Exception e) {
			JOptionPane.showMessageDialog(this, "Could not display image");
			setVisible(false);
		}
		
		setSize(400,400);
		setVisible(true);
	}
	
	public static void open(File imgFile) {
		new ImageViewer(imgFile);
	}
	public static void open(URL imgURL) {
		new ImageViewer(imgURL);
	}
}
