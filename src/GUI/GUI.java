package GUI;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class GUI extends JFrame{
	public JTextArea textArea;
	public JButton btnRun;
	public JLabel lblInfo;
	
	public  GUI(){
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth();
		double height = screenSize.getHeight();
		setLayout(null);
		setTitle("Tomasulo");
		setSize((int) (0.5*width),(int) (0.5*height));
		//setExtendedState(MAXIMIZED_BOTH);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
		 btnRun =new JButton("Next");
		btnRun.setBounds((int)(0.4*width), (int)(0.25*height), (int)(0.05*width), (int)(0.05*height));
		add(btnRun);
			
		 textArea = new JTextArea();
		//JScrollPane scrollPane = new JScrollPane(textArea); 
		//textArea.setEditable(false);
		textArea.setBounds(0, (int)(0.11*height), (int)(0.3*width), (int)(0.4*height));
		add(textArea);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		
		lblInfo = new JLabel ();
		lblInfo.setBounds(0, (int)(0.01*height),(int)(0.3*width), (int)(0.1*height));
		add (lblInfo);
		repaint();
	}
	
	public static void main(String[] args) {
		new GUI();
		
	}
	

}
