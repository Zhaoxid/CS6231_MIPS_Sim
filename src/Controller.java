import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;

@SuppressWarnings("rawtypes")
public class Controller {
	private GUI gui;
	private Processor processor;
	private List<Instruction> instructions;

	private DefaultListModel instructionModel;
	private DefaultListModel registerModel;
	private DefaultListModel memoryModel;

	private volatile boolean running = false;

	public Controller() {
		gui = new GUI();
		gui.setGUIListener(listener);

		processor = new Processor();

		instructionModel = new DefaultListModel();
		gui.setInstructionListModel(instructionModel);

		registerModel = new DefaultListModel();
		gui.setRegisterListModel(registerModel);

		memoryModel = new DefaultListModel();
		gui.setMemoryListModel(memoryModel);
	}

@SuppressWarnings("unchecked")
	private void refresh() {
		int pc = processor.getPC();
		gui.setPc(pc);

		int instructionIndex = pc / 4;
		if(instructionIndex >= instructionModel.getSize()) {
			gui.clearInstructionSelection();
		} else {
			gui.selectInstruction(instructionIndex);
		}

		registerModel.clear();
		memoryModel.clear();

		int[] registerData = processor.getReg();
		List<Integer> changedRegisters = processor.getChangedReg();
		
		for(int index : changedRegisters) {
			String repr = String.format(
					"%s: %s", Register.name(index), string_value(registerData[index]));
			registerModel.addElement(repr);
		}

		int[] memoryData = processor.getMem();
		List<Integer> changedMemory = processor.getChangedMemory();
		for(int index : changedMemory) {
			String repr = String.format(
					"%s: %s", string_value((short)index), string_value(memoryData[index]));
			memoryModel.addElement(repr);
		}
	}

	private String string_value(int b) {
			return String.format("%d", b & 0xffffffffL);
	}

	private void refreshLater() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				refresh();
			}
		});
	}


	private void run() {
		if(running) {
			return;
		}
		new Thread(){
			@Override
			public void run() {
				if(running) {
					return;
				}
				running = true;
				while(running && !processor.isDone()) 
					processor.step();
				
				refreshLater();
			};
		}.start();
	}

	private void stop() {
		running = false;
	}

	private synchronized void step() {
		processor.step();
	}

	private void reset() {
		stop();
		step(); //Block until running stops
		processor.reset();
	}

	@SuppressWarnings("unchecked")
	private void renderInstructions() {
		instructionModel.clear();
		for(Instruction i : instructions) {
			instructionModel.addElement(i.representation(false));
		}
	}

	private void load(String filename) {
		String line;
		BufferedReader reader = null;
		instructions = new ArrayList<Instruction>();

		instructionModel.clear();

		try {
			reader = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}

		try {
			int i = 0;
			while((line = reader.readLine()) != null){
				i++;
				if(line.length() == 0) {
					continue;
				}
				try {
					Instruction instruction = new Instruction(line);
					instructions.add(instruction);
				} catch (Exception e) {
					System.out.printf("Invalid instruction '%s' on line %d\n", line, i);
				}
			}
		} catch (IOException e) {
			System.out.printf("File reading error: %s \n", e.getMessage());
		}
		processor.setInstructionSet(instructions);
		renderInstructions();
		refresh();
	}

	private GUI.GUIListener listener = new GUI.GUIListener() {

		@Override
		public void onStop() {
			stop();
			refresh();
		}

		@Override
		public void onStep() {
			step();
			refresh();
		}

		@Override
		public void onRun() {
			run();
		}

		@Override
		public void onReset() {
			reset();
			refresh();
		}

		@Override
		public void onLoad(String filename) {
			load(filename);
		}

	};

	public static void main(String[] args) {
		new Controller();
	}
}
