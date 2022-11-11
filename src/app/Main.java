package app;

import java.util.Scanner;

import app.chip8.Chip8;
import app.chip8.display.Chip8Frame;

public class Main extends Thread{
	private Chip8 ch8;
	private Chip8Frame frame;
	
	public Main() {
		this.ch8 = new Chip8();
		this.ch8.init();
		this.frame = new Chip8Frame(this.ch8);
	}
	
	public void run() {
		while(true) {
			this.ch8.run();
			this.ch8.setKeyBuffer(this.frame.getKeys().getKeyBuffer());
			if(this.ch8.getRedraw()) {
				this.frame.repaint();
				this.ch8.removeRedrawFlag();
			}
			try {
				Thread.sleep(16);
			} catch(InterruptedException e) {
				System.out.println("Some error has occurred: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		
		//or main.ch8.loadRom(args[0]);
		
		System.out.println("Path of rom: ");
		String path = sc.nextLine();
		sc.close();
		if(path.length() > 0) {
			Main main = new Main();
			main.ch8.loadRom(path);
			main.start();
		}
	}
}
