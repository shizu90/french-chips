package app.chip8;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

public class Chip8 {
	private char[] memory;
	private char[] v;
	private char vx;
	private char vy;
	private char i;
	private char pc;
	private char[] stack;
	private int sp;
	
	private int delay_timer;
	private int sound_timer;
	
	private char opcode;
	
	private byte[] keys;
	private byte[] display;
	
	private boolean redraw;
	
	private File rom;
	
	public void init() {
		this.memory = new char[0x1000];
		this.v = new char[0x10];
		this.stack = new char[0x10];
		this.sp = 0x0;
		this.i = 0x0;
		this.pc = 0x200;
		
		this.delay_timer = 0x0;
		this.sound_timer = 0x0;
		
		this.keys = new byte[0x10];
		this.display = new byte[0x40 * 0x20];
		this.loadFontset();
	}
	
	public void loadRom(String rom) {
		DataInputStream stream = null;
		try {
			this.rom = new File(rom);
			stream = new DataInputStream(new FileInputStream(this.rom));
			int read = 0;
			while(stream.available() > 0) {
				memory[0x200 + read] = (char)stream.readByte();
				read++;
			}
		} catch(IOException e) {
			System.out.println("Some error has occurred: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if(stream != null) 
				try {
					stream.close();
				} catch(IOException e) {
					System.out.println("Some error has occurred: " + e.getMessage());
					e.printStackTrace();
				}
		}
	}
	
	public void logOperation() {
		System.out.println(
				"Opcode: " + Integer.toHexString(this.opcode) + 
				" PC: " + Integer.toHexString(this.pc) + 
				" SP: " + Integer.toHexString(this.sp) + 
				" I: " + Integer.toHexString(delay_timer)
		);
	}
	
	public void emuCycle() {
		this.opcode = (char)((memory[this.pc] << 8) | memory[this.pc + 1]);
		logOperation();
		switch(this.opcode & 0xF000) {
			case 0x0000:
				switch(this.opcode & 0x00FF) {
					case 0x00E0: //Clear display
						for(int i = 0; i < this.display.length; i++) {
							this.display[i] = 0;
						}
						this.pc += 0x2;
						this.redraw = true;
						break;
					case 0x00EE: //Return from subroutine
						this.sp--;
						this.pc = (char)(this.stack[this.sp] + 0x2);
						this.pc += 0x2;
						break;
					default:
						System.out.println("Invalid opcode: " + Integer.toHexString(this.opcode));
						break;
				}
				break;
			case 0x1000: //Jump to address 0x1NNN
				this.pc = (char)(this.opcode & 0x0FFF); 
				break;
			case 0x2000: //Call subroutine at address 0x2NNN
				this.sp++;
				this.stack[this.sp] = this.pc;
				this.pc = (char)(this.opcode & 0x0FFF);
				break;
			case 0x3000: //Skip next instruction if V[0x3X00] equals to 0x30KK
				this.vx = this.v[((this.opcode & 0x0F00) >> 0x8)]; 
				if(this.vx == (this.opcode & 0x00FF)) {
					this.pc += 0x4;
				}else this.pc += 0x2;
				break;
			case 0x4000: //Skip next instruction if V[0x3X00] differs 0x30KK
				this.vx = this.v[((this.opcode & 0x0F00) >> 0x8)];
				if(vx != (this.opcode & 0x00FF)) {
					this.pc += 0x4;
				}else this.pc += 0x2;
				break;
			case 0x5000: //Skip next instruction if V[0x3X00] equals to V[0x30Y0]
				this.vx = this.v[((this.opcode & 0x0F00) >> 0x8)];
				this.vy = this.v[((this.opcode & 0x00F0) >> 0x4)]; 
				if(vx == vy) {
					this.pc += 0x4;
				}else this.pc += 0x2;
				break;
			case 0x6000: //Set V[0x6X00] as 0x60KK
				this.v[((this.opcode & 0x0F00) >> 0x8)] = (char)(this.opcode & 0x00FF);
				this.pc += 0x2;
				break;
			case 0x7000: //Increment 0x70KK on V[0x7X00]
				this.vx = this.v[((this.opcode & 0x0F00) >> 0x8)];
				this.v[((this.opcode & 0x0F00) >> 0x8)] = (char)((vx + (this.opcode & 0x00FF)) & 0xFF);
				this.pc += 0x2;
				break;
			case 0x8000:
				switch(this.opcode & 0x000F) {
					case 0x0000: // Set V[0x0X00] as V[0x00Y0]
						this.vy = this.v[((this.opcode & 0x00F0) >> 0x4)];
						this.v[((this.opcode & 0x0F00) >> 0x8)] = vy;
						this.pc += 0x2;
						break;
					case 0x0001: // Set V[0x0X00] as V[0x0X00] or V[0x00Y0]
						this.vx = this.v[((this.opcode & 0x0F00) >> 0x8)];
						this.vy = this.v[((this.opcode & 0x00F0) >> 0x4)]; 
						this.v[((this.opcode & 0x0F00) >> 0x8)] = (char)(vx | vy);
						this.pc += 0x2;
						break;
					case 0x0002: // Set V[0x0X00] as V[0x0X00] and V[0x00Y0]
						this.vx = this.v[((this.opcode & 0x0F00) >> 0x8)];
						this.vy = this.v[((this.opcode & 0x00F0) >> 0x4)];
						this.v[((this.opcode & 0x0F00) >> 0x8)] = (char)(vx & vy);
						this.pc += 0x2;
						break;
					case 0x0003: // Set V[0x0X00] as V[0x0X00] xor V[0x00Y0]
						this.vx = this.v[((this.opcode & 0x0F00) >> 0x8)];
						this.vy = this.v[((this.opcode & 0x00F0) >> 0x4)];
						this.v[((this.opcode & 0x0F00) >> 0x8)] = (char)(vx ^ vy);
						break;
					case 0x0004: // Set V[0x0X00] as V[0x0X00] + v[0x00X0] and V[0xF] as carry
						this.vx = this.v[((this.opcode & 0x0F00) >> 0x8)];
						this.vy = this.v[((this.opcode & 0x00F0) >> 0x4)];
						if((vx + vy) < 0xFF) {
							this.v[0xF] = 0x0;
						}else this.v[0xF] = 0x1;
						this.v[((this.opcode & 0x0F00) >> 0x8)] = (char)(vx + vy);
						this.pc += 0x2;
						break;
					case 0x0005: // Set V[0x0X00] as V[0x0X00] - v[0x00X0] and V[0xF] as not borrow
						this.vx = this.v[((this.opcode & 0x0F00) >> 0x8)];
						this.vy = this.v[((this.opcode & 0x00F0) >> 0x4)];
						if(vx >= vy) {
							this.v[0xF] = 0x1;
						}else this.v[0xF] &= 0x0;
						this.v[((this.opcode & 0x0F00) >> 8)] = (char)(vx - vy);
						this.pc += 0x2;
						break;
					case 0x0006: //Set V[0x0X00] shr 1
						this.vx = this.v[((this.opcode & 0x0F00) >> 0x8)];
						this.v[0xF] = (char)(vx & 0x1);
						this.v[((this.opcode & 0x0F00) >> 0x8)] = (char)(vx >> 0x1);
						this.pc += 0x2;
						break;
					case 0x0007: //Set V[0x0X00] as V[0x00F0] - V[0x0F00]
						this.vx = this.v[((this.opcode & 0x0F00) >> 0x8)];
						this.vy = this.v[((this.opcode & 0x00F0) >> 0x4)];
						if(vx > vy) {
							this.v[0xF] = 0;
						}else this.v[0xF] = 1;
						this.v[((this.opcode & 0x0F00) >> 0x8)] = (char)(vy - vx);
						this.pc += 0x2;
						break;
					case 0x000E:
						this.vx = this.v[((this.opcode & 0x0F00) >> 0x8)];
						this.v[0xF] = (char)(vx >> 0x7);
						this.v[((this.opcode & 0x0F00) >> 0x8)] = (char)(this.v[((this.opcode & 0x0F00) >> 0x8)] << 0x1);
						this.pc += 0x2;
						break;
					default:
						System.out.println("Invalid opcode: " + Integer.toHexString(this.opcode));
				}
				break;
			case 0x9000: // Skip next instruction if V[0x0X00] differs V[0x00X0]
				this.vx = this.v[((this.opcode & 0x0F00) >> 0x8)];
				this.vy = this.v[((this.opcode & 0x00F0) >> 0x4)];
				if(vx != vy) {
					this.pc += 0x4;
				}else this.pc += 0x2;
				break;
			case 0xA000: //Set index 0x0NNN
				this.i = (char)(this.opcode & 0x0FFF);
				this.pc += 0x2;
				break;
			case 0xB000: //Jump to location 0x0NNN + V[0x0]
				this.pc = (char)((this.opcode & 0x0FFF) + (this.v[0x0] & 0xFF));
				break;
			case 0xC000: //Set V[0x0X00] as random byte and 0x00KK
				this.v[((this.opcode & 0x0F00) >> 0x8)] = (char)(new Random().nextInt(256) & (this.opcode & 0x00FF));
				this.pc += 0x2;
				break;
			case 0xD000: //Display n-byte sprite starting at memory location I at (Vx, Vy), set VF = collision
				this.vx = this.v[((this.opcode & 0x0F00) >> 0x8)];
				this.vy = this.v[((this.opcode & 0x00F0) >> 0x4)];
				
				int h = this.opcode & 0x000F;
				
				this.v[0xF] = 0x0;
				
				for(int y = 0x0; y < h; y++) {
					int ln = this.memory[this.i + y];
					for(int x = 0; x < 0x8; x++) {
						int px = ln & (0x80 >> x);
						if(px != 0x0) {
							int totalX = this.vx + x;
							int totalY = this.vy + y;
							totalX = totalX % 64;
							totalY = totalY % 32;
							int index = (totalY * 64) + totalX;
							if(this.display[index] == 0x1) {
								this.v[0xF] = 0x1;
							}
							this.display[index] ^= 0x1;
						}
					}
				}
				this.redraw = true;
				this.pc += 0x2;
				break;
			case 0xE000:
				switch(opcode & 0x00FF) {
					case 0x009E: //Skip next instruction if the key VX is pressed
						if(this.keys[this.v[((opcode & 0x0F00) >> 8)]] == 1) {
							this.pc += 0x4;
						}else this.pc += 0x2;
						break;
					case 0x00A1: //Skip next instruction if the key VX is NOT pressed
						if(this.keys[this.v[((opcode & 0x0F00) >> 8)]] == 0) {
							this.pc += 0x4;
						}else this.pc += 0x2;
						break;
					default:
						System.out.println("Invalid opcode: " + Integer.toHexString(opcode));
						break;
				}
				break;
			case 0xF000:
				switch(this.opcode & 0x00FF) {
					case 0x0007: //Set Vx as delay timer
						this.v[((this.opcode & 0x0F00) >> 0x8)] = (char)(this.delay_timer & 0xFF);
						this.pc += 0x2;
						break;
					case 0x000A: //Wait for a key press, store the value of key in Vx
						for(int i = 0; i <= this.keys.length; i++) {
							if(this.keys[i] == 1) {
								this.v[((opcode & 0x0F00) >> 8)] = (char)i;
								this.pc += 0x2;
								break;
							}
						}
						break;
					case 0x0015: //Set delay timer as Vx
						this.delay_timer = this.v[((this.opcode & 0x0F00) >> 0x8)];
						this.pc += 0x2;
						break;
					case 0x0018: //Set sound timer as Vx
						this.sound_timer = this.v[((this.opcode & 0x0F00) >> 0x8)];
						this.pc += 0x2;
						break;
					case 0x001E: //Increment index with Vx
						this.vx = this.v[((this.opcode & 0x0F00) >> 0x8)];
						if(this.i + vx > 0xFFF) {
							this.v[0xF] = 1;
						}else {
							this.v[0xF] = 0;
						}
						this.i = (char)((this.i + vx) & 0xFFF);
						this.pc += 0x2;
						break;
					case 0x0029: //Sets index register to the location of sprite of VX
						this.vx = this.v[((this.opcode & 0x0F00) >> 0x8)];
						this.i = (char)(vx * 0x5);
						this.redraw = true;
						this.pc += 0x2;
						break;
					case 0x0033:
						vx = this.v[((this.opcode & 0x0F00) >> 0x8)];
						this.memory[this.i] = (char)(vx / 0x64);
						this.memory[this.i + 0x1] = (char)((vx % 0x64) / 0xA);
						this.memory[this.i + 0x2] = (char)((vx % 0x64) % 0xA);
						this.pc += 0x2;
						break;
					case 0x0055:
						for(int i = 0; i <= ((opcode & 0x0F00) >> 8); i++) {
							this.memory[this.i + i] = this.v[i];	
						}
						this.pc += 0x2;
						break;
					case 0x0065: //FX65 Fills V0 to VX with values from index register
						for(int i = 0; i <= ((this.opcode & 0x0F00) >> 8); i++) {
							this.v[i] = (char)(this.memory[this.i + i] & 0xFF);
						}
						this.i = (char)(this.i + ((opcode & 0x0F00) >> 8) + 1);
						this.pc += 0x2;
						break;
					default:
						System.out.println("Invalid opcode: " + Integer.toHexString(this.opcode));
				}
				this.pc += 0x2;
				break;
		}
		if(this.sound_timer > 0) {
			this.sound_timer--;
		}
		if(this.delay_timer > 0) {
			this.delay_timer--;
		}
	}
	
	public void loadFontset() {
		for(int i = 0; i < Chip8Fontset.fontset.length; i++) {
			this.memory[0x50 + i] = (char)(Chip8Fontset.fontset[i] & 0xFF);
		}
	}
	
	public void setKeyBuffer(int[] keyBuffer) {
		for(int i = 0; i < this.keys.length; i++) {
			this.keys[i] = (byte)keyBuffer[i];
		}
	}
	
	public void run() {
		this.emuCycle();
	}
	
	public void removeRedrawFlag() {
		this.redraw = false;
	}
	
	public byte[] getDisplay() {
		return this.display;
	}
	
	public boolean getRedraw() {
		return this.redraw;
	}
}
