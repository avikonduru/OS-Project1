import java.util.Scanner;
import java.util.Random;
import java.io.*;

public class CPU {
	
	public static int PC = 0;		//Registers
	public static int SP = 1000;
	public static int IR;
	public static int AC = 0;
	public static int X;
	public static int Y;
	
	public static boolean kernelSig = false;
	public static boolean running = true;
	public static int timer = 10;				//Default timer
    public static int counter = 0;
	
	public static void main(String[] args){
		
		try{
			int x;
			String textfile = args[0];	//First argument is the textfile name
			timer = Integer.parseInt(args[1]);	//Second arguement is the timer int
			
			Runtime rt = Runtime.getRuntime();		//Initialize the process between CPU and Memory using runtime exec
			Process proc = rt.exec("java Memory " + textfile);	//Run Memory class with textfile argument
			OutputStream os = proc.getOutputStream();	//Create input and output stream for proc
			InputStream is = proc.getInputStream();
			 
			PrintWriter pw = new PrintWriter(os);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			
			int interrupts = 0;	//Count time til interrupt
			
			while(running){	//Run will exit instruction is not called
				
				if(interrupts < timer){	//If the amount of interrupts is less than what the timer calls for
					
					fetchToIR(pw,proc,br);	//Get data from memory to IR
					if(IR == -1 && counter >= 100){	//Error handling if goes over limit
                        System.out.println("Memory violation: accessing system address 1000 in user mode");
                        System.exit(0);
                    }
                    counter++;
					InstructionSet(IR,pw,proc,br);	//Execute command that the IR specifies by calling fuction
					
					if(!kernelSig){	//If not in kernel mode increment time til interrupt
						interrupts++;
					}
					
				}else{
					
					interrupts = 0;	//Reset time till interrupt and enter kernel mode
					enterKernelMode(1000,pw);
					
				}
				
			}
			
			proc.waitFor();
			int exitVal = proc.exitValue();	//Exit out of process

			System.exit(0);
			 
		}catch(Throwable t){
			t.printStackTrace();
		}
		
	}
	
	public static void InstructionSet(int num, PrintWriter pw, Process proc, BufferedReader br) throws IOException{
		switch(num){	//Depending on the argument use a different case
		case 1:
			fetchToIR(pw,proc,br);
			AC = IR;
			break;
		case 2:
			fetchToIR(pw,proc,br);
			AC = readFunction((IR),pw,proc,br);
			break;
		case 3:
			fetchToIR(pw,proc,br);
			AC = readFunction((readFunction((IR),pw,proc,br)),pw,proc,br);
			break;
		case 4:
			fetchToIR(pw,proc,br);
			AC = readFunction((IR + X),pw,proc,br);
			break;
		case 5:
			fetchToIR(pw,proc,br);
			AC = readFunction((IR + Y),pw,proc,br);
			break;
		case 6:
			AC = readFunction((SP + X),pw,proc,br);
			break;
		case 7:
			fetchToIR(pw,proc,br);
			writeFunction(IR,AC,pw);
			break;
		case 8:
			AC = 1 + (int)(99*Math.random());
			break;
		case 9:
			fetchToIR(pw,proc,br);
			int port = IR;
			if(port == 1){
				System.out.print(AC);
			}else if(port == 2){
				System.out.print((char)AC);
			}
			break;
		case 10:
			AC += X;
			break;
		case 11:
			AC += Y;
			break;
		case 12:
			AC -= X;
			break;
		case 13:
			AC -= Y;
			break;
		case 14:
			X = AC;
			break;
		case 15:
			AC = X;
			break;
		case 16:
			Y = AC;
			break;
		case 17:
			AC = Y;
			break;
		case 18:
			SP = AC;
			break;
		case 19:
			AC = SP;
			break;
		case 20:
			fetchToIR(pw,proc,br);
			PC = IR;
			break;
		case 21:
			fetchToIR(pw,proc,br);
			if(AC == 0){
				PC = IR;
			}
			break;
		case 22:
			fetchToIR(pw,proc,br);
			if(AC != 0){
				PC = IR;
			}
			break;
		case 23:
			fetchToIR(pw,proc,br);
			push(PC,pw,proc,br);
			PC = IR;
			break;
		case 24:
			PC = pop(pw,proc,br);
			break;
		case 25:
			X++;
			break;
		case 26:
			X--;
			break;
		case 27:
			push(AC,pw,proc,br);
			break;
		case 28:
			AC = pop(pw,proc,br);
			break;
		case 29:
			enterKernelMode(1500,pw);
			break;
		case 30:
			exitKernelMode(pw,proc,br);
			break;
		case 50:
			String exitString = "e-0:0\n";
			pw.printf(exitString);
			pw.flush(); 
			running = false;
			break;
		default:
			break;
		}
	}
	
	public static void enterKernelMode(int execAddress, PrintWriter pw){	//Use function to enter into kernel mode
		
		kernelSig = true;
		String kernelString = "k-0:0\n";
		pw.printf(kernelString);
		pw.flush();
		
		writeFunction(1999, SP, pw);	//Write SP and PC to kernel stack
		writeFunction(1998, PC, pw);
		
		SP = 1998;
		PC = execAddress;
		
	}
	
	public static void exitKernelMode(PrintWriter pw, Process proc, BufferedReader br) throws IOException{	//Use function to exit kernel mode
		
		PC = pop(pw,proc,br);	//Pop data to PC and SP
		SP = pop(pw,proc,br);
		
		kernelSig = false;
		String exitKernelString = "x-0:0\n";
		pw.printf(exitKernelString);
		pw.flush();
		
	}
	
	public static int pop(PrintWriter pw, Process proc, BufferedReader br) throws IOException{
		return readFunction(SP++,pw,proc,br);
	}
	
	public static void push(int num, PrintWriter pw, Process proc, BufferedReader br){
		writeFunction(--SP, num, pw);
	}
	
	public static void fetchToIR(PrintWriter pw, Process proc, BufferedReader br) throws IOException{
		IR = readFunction(PC++,pw,proc,br);
	}
	
	public static int readFunction(int address, PrintWriter pw, Process proc, BufferedReader br) throws IOException{
		
		String readString = "r-" + address + ":0\n";	//provide command to Memory
		pw.printf(readString);
		pw.flush();  

		String readLine;	//Read output from memory and put int value into IR
	  
		while (((readLine = br.readLine()) != null)){
			return Integer.parseInt(readLine);
		}
		return -1;
		
	}
	
	public static void writeFunction(int address, int data, PrintWriter pw){
		
		String writeString = "w-" + address + ":" + data + "\n";	//Provide write command with data to Memory
		pw.printf(writeString);
		pw.flush();
		
	}
	
}