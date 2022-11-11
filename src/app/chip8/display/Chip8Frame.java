package app.chip8.display;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;

import app.chip8.Chip8;
import app.chip8.data.Chip8Keys;

public class Chip8Frame extends JFrame{
	private static final long serialVersionUID = 1L;
	
	private Chip8Panel panel;
	private Chip8Keys keys;
	
	public Chip8Frame(Chip8 chip8) {
		setPreferredSize(new Dimension(340, 620));
		pack();
		setPreferredSize(new Dimension(630 + getInsets().left + getInsets().right, 290 + getInsets().top + getInsets().bottom));
		this.panel = new Chip8Panel(chip8);
		setLayout(new BorderLayout());
		add(this.panel, BorderLayout.CENTER);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("French Chips");
		setResizable(false);
		pack();
		setVisible(true);
		
		this.keys = new Chip8Keys();
	}
	
	public Chip8Keys getKeys() {
		return this.keys;
	}
	
}
