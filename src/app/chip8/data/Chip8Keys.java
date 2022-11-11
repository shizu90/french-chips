package app.chip8.data;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Chip8Keys implements KeyListener{
	
	private int[] keyBuffer;
	private int[] keyId;
	
	public Chip8Keys() {
		this.keyId = new int[256];
		this.keyBuffer = new int[16];
		
		this.fillKeyIds();
	}
	
	private void fillKeyIds() {
		for(int i = 0; i < keyId.length; i++) {
			this.keyId[i] = -1;
		}
		
		this.keyId['1'] = 1;
		this.keyId['2'] = 2;
		this.keyId['3'] = 3;		
		this.keyId['Q'] = 4;
		this.keyId['W'] = 5;
		this.keyId['E'] = 6;
		this.keyId['A'] = 7;
		this.keyId['S'] = 8;
		this.keyId['D'] = 9;
		this.keyId['Z'] = 0xA;
		this.keyId['X'] = 0;
		this.keyId['C'] = 0xB;
		this.keyId['4'] = 0xC;
		this.keyId['R'] = 0xD;
		this.keyId['F'] = 0xE;
		this.keyId['V'] = 0xF;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if(this.keyId[e.getKeyCode()] != -1) {
			this.keyBuffer[this.keyId[e.getKeyCode()]] = 1;
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		if(this.keyId[e.getKeyCode()] != -1) {
			this.keyBuffer[this.keyId[e.getKeyCode()]] = 0;
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e) {}
	
	public int[] getKeyBuffer() {
		return this.keyBuffer;
	}
	
	public int[] getKeyId() {
		return this.keyId;
	}
}
