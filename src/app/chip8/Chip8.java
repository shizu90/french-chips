package app.chip8;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

public class Chip8 {
	private char[] memory;
	private char[] v;
	private char i;
	private char pc;
	private char[] stack;
	private int sp;
	
	private int delay_timer;
	private int sound_timer;
	
	private Opcode opcode;
	
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
		this.opcode = new Opcode();
		
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
				"Opcode: " + Integer.toHexString(this.opcode.value) + 
				" PC: " + Integer.toHexString(this.pc) + 
				" SP: " + Integer.toHexString(this.sp) + 
				" I: " + Integer.toHexString(delay_timer)
		);
	}
	
	public void emuCycle() {
		this.opcode.setValue((memory[this.pc] << 8) | memory[this.pc + 1]);
		logOperation();
		switch(opcode.value & 0xF000) {
			case 0x0000:
				switch(opcode.value & 0x00FF) {
					case 0x00E0: //Clear display
						for(int i = 0; i < display.length; i++) {
							display[i] = 0;
						}
						pc += 0x2;
						redraw = true;
						break;
					case 0x00EE: //Return from subroutine
						sp--;
						pc = (char)(stack[sp]);
						pc += 0x2;
						break;
					default:
						System.out.println("Invalid opcode: " + Integer.toHexString(opcode.value));
						pc += 0x2;
						break;
				}
				break;
			case 0x1000: //Jump to address 0x1NNN
				pc = (char)opcode.nnn; 
				break;
			case 0x2000: //Call subroutine at address 0x2NNN
				stack[sp] = pc;
				sp++;
				pc = (char)opcode.nnn;
				break;
			case 0x3000: //Skip next instruction if V[0x3X00] equals to 0x30KK
				if(v[opcode.x] == opcode.kk)
					pc += 0x4;
				else 
					pc += 0x2;
				break;
			case 0x4000: //Skip next instruction if V[0x3X00] differs 0x30KK
				if(v[opcode.x] != opcode.kk)
					pc += 0x4;
				else 
					pc += 0x2;
				break;
			case 0x5000: //Skip next instruction if V[0x3X00] equals to V[0x30Y0]
				if(v[opcode.x] == v[opcode.y])
					pc += 0x4;
				else 
					pc += 0x2;
				break;
			case 0x6000: //Set V[0x6X00] as 0x60KK
				v[opcode.x] = (char)opcode.kk;
				pc += 0x2;
				break;
			case 0x7000: //Increment 0x70KK on V[0x7X00]
				v[opcode.x] = (char)((v[opcode.x] + opcode.kk) & 0xFF);
				pc += 0x2;
				break;
			case 0x8000:
				switch(opcode.value & 0x000F) {
					case 0x0000: // Set V[0x0X00] as V[0x00Y0]
						v[opcode.x] = v[opcode.y];
						pc += 0x2;
						break;
					case 0x0001: // Set V[0x0X00] as V[0x0X00] or V[0x00Y0]
						v[opcode.x] = (char)(v[opcode.x] | v[opcode.y]);
						pc += 0x2;
						break;
					case 0x0002: // Set V[0x0X00] as V[0x0X00] and V[0x00Y0]
						v[opcode.x] = (char)(v[opcode.x] & v[opcode.y]);
						pc += 0x2;
						break;
					case 0x0003: // Set V[0x0X00] as V[0x0X00] xor V[0x00Y0]
						v[opcode.x] = (char)(v[opcode.x] ^ v[opcode.y]);
						break;
					case 0x0004: // Set V[0x0X00] as V[0x0X00] + v[0x00X0] and V[0xF] as carry
						this.v[opcode.x] = (char)(v[opcode.x] + v[opcode.y]);
						v[0xF] = (char)(v[opcode.x] > 0xFF ? 1 : 0);
						if(v[opcode.x] < 0xFF) {
							v[opcode.x] = (char)(v[opcode.x] - 0xFF);
						}
						pc += 0x2;
						break;
					case 0x0005: // Set V[0x0X00] as V[0x0X00] - v[0x00X0] and V[0xF] as not borrow
						v[0xF] = (char)(v[opcode.x] > v[opcode.y] ? 1 : 0);
						v[opcode.x] = (char)(v[opcode.x] - v[opcode.y]);
						if(v[opcode.x] < 0) {
							v[opcode.x] += 0x100;
						}
						pc += 0x2;
						break;
					case 0x0006: //Set V[0x0X00] shr 1
						v[0xF] = (char)(v[opcode.x] & 0x1);
						v[opcode.x] = (char)(v[opcode.x] >> 0x1);
						pc += 0x2;
						break;
					case 0x0007: //Set V[0x0X00] as V[0x00F0] - V[0x0F00]
						v[0xF] = (char)(v[opcode.y] > v[opcode.x] ? 1 : 0);
						v[opcode.x] = (char)(v[opcode.y] - v[opcode.x]);
						if(v[opcode.x] < 0) 
							v[opcode.x] += 0x100;
						pc += 0x2;
						break;
					case 0x000E:
						v[0xF] = (char)(v[opcode.x] & 0x80);
						v[opcode.x] <<= 0x1;
						v[opcode.x] = v[opcode.x];
						if(v[opcode.x] > 0x100) {
							v[opcode.x] -= 0x100;
						}
						pc += 0x2;
						break;
					default:
						System.out.println("Invalid opcode: " + Integer.toHexString(opcode.value));
						pc += 0x2;
						break;
				}
				break;
			case 0x9000: // Skip next instruction if V[0x0X00] differs V[0x00X0]
				v[opcode.x] = v[opcode.x];
				v[opcode.y] = v[opcode.y];
				if(v[opcode.x] != v[opcode.y])
					pc += 0x4;
				else 
					pc += 0x2;
				break;
			case 0xA000: //Set index 0x0NNN
				i = (char)opcode.nnn;
				pc += 0x2;
				break;
			case 0xB000: //Jump to location 0x0NNN + V[0x0]
				pc = (char)(opcode.nnn + v[0x0]);
				break;
			case 0xC000: //Set V[0x0X00] as random byte and 0x00KK
				v[opcode.x] = (char)(((int)Math.floor(new Random().nextInt(256))) & opcode.kk);
				pc += 0x2;
				break;
			case 0xD000: //Display n-byte sprite starting at memory location I at (Vx, Vy), set VF = collision				
				int h = opcode.value & 0x000F;
				
				v[0xF] = 0x0;
				
				for(int y = 0; y < h; y++) {
					int ln = memory[i + y];
					for(int x = 0; x < 0x8; x++) {
						int px = ln & (0x80 >> x);
						if(px != 0x0) {
							int totalX = v[opcode.x] + x;
							int totalY = v[opcode.y] + y;
							totalX = totalX % 64;
							totalY = totalY % 32;
							int index = (totalY * 64) + totalX;
							if(display[index] == 0x1) {
								v[0xF] = 0x1;
							}
							display[index] ^= 0x1;
						}
					}
				}
				redraw = true;
				pc += 0x2;
				break;
			case 0xE000:
				switch(opcode.value & 0x00FF) {
					case 0x009E: //Skip next instruction if the key VX is pressed
						if(keys[v[opcode.x]] == 1)
							pc += 0x4;
						else 
							pc += 0x2;
						break;
					case 0x00A1: //Skip next instruction if the key VX is NOT pressed
						if(keys[v[opcode.x]] == 0)
							pc += 0x4;
						else 
							pc += 0x2;
						break;
					default:
						System.out.println("Invalid opcode: " + Integer.toHexString(opcode.value));
						pc += 0x2;
						break;
				}
				break;
			case 0xF000:
				switch(opcode.value & 0x00FF) {
					case 0x0007: //Set Vx as delay timer
						v[opcode.x] = (char)(delay_timer & 0xFF);
						pc += 0x2;
						break;
					case 0x000A: //Wait for a key press, store the value of key in Vx
						for(int i = 0; i <= keys.length; i++) {
							if(keys[i] == 1) {
								v[opcode.x] = (char)i;
								pc += 0x2;
								break;
							}
						}
						break;
					case 0x0015: //Set delay timer as Vx
						delay_timer = v[opcode.x];
						pc += 0x2;
						break;
					case 0x0018: //Set sound timer as Vx
						sound_timer = v[opcode.x];
						pc += 0x2;
						break;
					case 0x001E: //Increment index with Vx
						if(i + v[opcode.x] > 0xFFF)
							v[0xF] = 1;
						else
							v[0xF] = 0;
						i = (char)((i + v[opcode.x]) & 0xFFF);
						pc += 0x2;
						break;
					case 0x0029: //Sets index register to the location of sprite of VX
						i = (char)(v[opcode.x] * 0x5);
						redraw = true;
						pc += 0x2;
						break;
					case 0x0033:
						memory[i] = (char)(v[opcode.x] / 0x64);
						memory[i + 0x1] = (char)((v[opcode.x] % 0x64) / 0xA);
						memory[i + 0x2] = (char)((v[opcode.x] % 0x64) % 0xA);
						pc += 0x2;
						break;
					case 0x0055:
						for(int i = 0; i <= ((opcode.value & 0x0F00) >> 8); i++) {
							memory[this.i + i] = v[i];	
						}
						pc += 0x2;
						break;
					case 0x0065: //FX65 Fills V0 to VX with values from index register
						for(int i = 0; i <= ((opcode.value & 0x0F00) >> 8); i++) {
							v[i] = (char)(memory[this.i + i] & 0xFF);
						}
						i = (char)(i + ((opcode.value & 0x0F00) >> 8) + 1);
						pc += 0x2;
						break;
					default:
						System.out.println("Invalid opcode: " + Integer.toHexString(opcode.value));
						pc += 0x2;
						break;
				}
				break;
		}
		if(sound_timer > 0) {
			sound_timer--;
		}
		if(delay_timer > 0) {
			delay_timer--;
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
