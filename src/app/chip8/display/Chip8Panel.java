package app.chip8.display;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import app.chip8.Chip8;

public class Chip8Panel extends JPanel{
	private static final long serialVersionUID = 1L;
	
	private Chip8 chip8;
	
	public Chip8Panel(Chip8 chip8) {
		this.chip8 = chip8;
	}
	
	public void paint(Graphics gfx) {
		byte[] display = this.chip8.getDisplay();
		for(int i = 0; i < display.length; i++) {
			if(display[i] == 0) {
				gfx.setColor(Color.BLACK);
			}else {
				gfx.setColor(Color.WHITE);
			}
			int x = i % 64;
			int y = (int)Math.floor(i/64);
			
			gfx.fillRect(x * 10, y * 10, 10, 10);
		}
	}
}
