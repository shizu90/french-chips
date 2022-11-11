package app.chip8;

public class Opcode {
	public int value;
	public int x;
	public int y;
	public int nnn;
	public int kk;
	
	public Opcode() {}
	
	public void setValue(int current_opcode) {
		this.value = current_opcode;
		this.x = (current_opcode & 0x0F00) >> 8;
		this.y = (current_opcode & 0x00F0) >> 4;
		this.kk = (current_opcode & 0x00FF);
		this.nnn = (current_opcode & 0x0FFF);
	}
}
