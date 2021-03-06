import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class Instruction {
	private String instruct;
	private short opcode = 0;
	private short r_type_opcode = 0;
	private boolean r_type = false;
	private boolean i_type = false;
	private boolean is_exit;
	private boolean is_nop;
	private short rd = 0;
	private short rs = 0;
	private short rt = 0;
	private short imm = 0;

	public Instruction(String line) throws Exception {
		instruct = line;

		line = line.replaceAll(",", "");
		StringTokenizer tokens = new StringTokenizer(line, " ");
		String op = "", t1 = "", t2 = "", t3 = "";
		
		op = tokens.nextToken();
		try {
			t1 = tokens.nextToken();
			t2 = tokens.nextToken();
			t3 = tokens.nextToken();
		} catch(NoSuchElementException e) {}

		if(op.equalsIgnoreCase("add")) {
			r_type_opcode = 32;
			r_type = true;
		} else if(op.equalsIgnoreCase("mult")) {
			r_type_opcode = 24;
			r_type = true;
		} else if(op.equalsIgnoreCase("div")) {
			r_type_opcode = 26;
			r_type = true;
		}else if(op.equalsIgnoreCase("sub")) {
			r_type_opcode = 34;
			r_type = true;
		} else if(op.equalsIgnoreCase("and")) {
			r_type_opcode = 36;
			r_type = true;
		} else if(op.equalsIgnoreCase("or")) {
			r_type_opcode = 37;
			r_type = true;
		} else if(op.equalsIgnoreCase("nor")) {
			r_type_opcode = 39;
			r_type = true;
		} else if(op.equalsIgnoreCase("slt")) {
			r_type_opcode = 42;
			r_type = true;
		} 
		
		//i type
		else if(op.equalsIgnoreCase("addi")) {
			opcode = 32;
			i_type = true;
		} else if(op.equalsIgnoreCase("andi")) {
			opcode = 36;
			i_type = true;
		} else if(op.equalsIgnoreCase("subi")) {
			opcode = 34;
			i_type = true;
		} else if(op.equalsIgnoreCase("xori")) {
			opcode = 38;
			i_type = true;
		} 

		//load and store
		else if(op.equalsIgnoreCase("lw")) {
			opcode = 35;
		} else if(op.equalsIgnoreCase("sw")) {
			opcode = 43;
		}
		else if(op.equalsIgnoreCase("j")) {
			opcode = 2;
		}
		else if(op.equalsIgnoreCase("beq")) {
			opcode = 4;
		}

		else if(op.equalsIgnoreCase("nop")) {
			is_nop = true;
		}

		else if(op.equalsIgnoreCase("exit")) {
			is_exit = true;
		}


		// Parse additional parameters
		if(opcode == 35 || opcode == 43) {
			rt = parseReg2(t1);
			if(t2.indexOf('(') != -1) {
				rs = parseReg1(t2);
				imm = parseOffset(t2);
			} else {
				rs = parseReg2(t2);
				imm = 0;
			}
		} else if(r_type) {
			rd = parseReg2(t1);
			rs = parseReg2(t2);
			rt = parseReg2(t3);
		} else if(i_type) {
			rs = parseReg2(t1);
			rt = parseReg2(t2);
			imm = parseReg2(t3);
		} else if(opcode == 2) {
			imm = parseAddr(t1);
		} else if(opcode == 4) {
			rs = parseReg2(t1);
			rt = parseReg2(t2);
			imm = parseAddr(t3);
		}

	}


	private short parseOffset(String token) {
		return parseAddr(token.substring(0, token.indexOf('(')));
	}


	private short parseReg1(String token) {
		try {
			return parseReg2(token.substring(token.indexOf('(')+1, token.indexOf(')')));
		} catch (Exception e) {
			return 0;
		}
	}

	private short parseAddr(String address) {
		if(address.contains("x")) {
			return Short.parseShort(
					address.substring(address.indexOf('x')+1), 16);
		}
		return Short.parseShort(address);
	}

	private short parseReg2(String register) throws Exception {
		if(register.charAt(0) == '$') {
			if(register.equalsIgnoreCase("$zero")) {
				return 0;
			}if(register.equalsIgnoreCase("$one")) {
				return 1;
			} else if(register.equalsIgnoreCase("$gp")) {
				return 28;
			} else if(register.equalsIgnoreCase("$sp")) {
				return 29;
			} else if(register.equalsIgnoreCase("$fp")) {
				return 30;
			} else if(register.equalsIgnoreCase("$ra")) {
				return 31;
			}

			char prefix = register.charAt(1);
			short number = Short.parseShort(register.substring(2));
			switch(prefix) {
			case 'v':
				number += 2;
				break;
			case 'a':
				number += 4;
				break;
			case 'r':
				number += 8;
				if(number >= 16) {
					number += 8;
				}
				break;
			case 's':
				number += 16;
				break;

			default:
				throw new Exception("Invalid register " + register);
			}
			assert register.equals(Register.name(number));
			return number;
		}

		return parseAddr(register);
	}


	public String getInstruct() {
		return instruct;
	}


	public short getOpcode() {
		return opcode;
	}


	public short getrOp() {
		return r_type_opcode;
	}


	public short getRd() {
		return rd;
	}


	public short getRs() {
		return rs;
	}


	public short getRt() {
		return rt;
	}


	public short getImm() {
		return imm;
	}


	public boolean is_r_type() {
		return r_type;
	}

	public boolean is_i_type() {
		return i_type;
	}

	public boolean isExit() {
		return is_exit;
	}

	public boolean isNop() {
		return is_nop;
	}

	@Override
	public String toString() {
		return String.format("%s", instruct);
	}

	public String representation(boolean hex) {
		if(r_type) 
			return String.format("%-25s", instruct);
		else return String.format("%-25s (imm: %d)", instruct, imm);
	}
}
