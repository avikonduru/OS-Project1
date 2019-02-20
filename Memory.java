import java.util.Scanner;
import java.util.Random;
import java.io.*;

public class Memory {
	
	static int[] memory = new int[2000];   //Initialize memory as a global integer array
	static boolean kernelSig = false;
	public static final int LIMIT = 999;

	public static void main(String[] args) throws FileNotFoundException{
		
		loadMemory(args[0]);  //Load file name into memory
		
		Scanner input = new Scanner(System.in);	//Read output from CPU
		
		while (input.hasNextLine()){	//Output each line one at a time
			String line = input.nextLine();
			String command = line.split("-")[0];	//The command is the first part of the original string split by -
			int address = Integer.parseInt(line.split("-")[1].split(":")[0]);	//The address is the first part of the original string split by :
			int data = Integer.parseInt(line.split("-")[1].split(":")[1]);	//The address is the second part of the original string split by :
			
			if(command.equals("r")){	//Process read command
				System.out.println(read(address));
			}else if(command.equals("w")){	//Process write command
				write(address,data);
			}else if(command.equals("e")){	//Process exit command
				System.exit(0);
			}else if(command.equals("k")){	//Process enter kernel command
				kernelSig = true;
			}else if(command.equals("x")){	//Process exit kernel command
				kernelSig = false;
			}
		}
		
	}
	
	public static int read(int address) throws SecurityException{	//Read function
		if((!kernelSig && address <= LIMIT) || (kernelSig)){	//Return memory address if not in kernel mode and below user stack limit or when it is in kernel mode
			return memory[address];
		}else{
			throw new SecurityException("Memory violation: accessing system address 1000 in user mode");	//Else if not, throw an Exception
		}
	}
	
	public static void write(int address, int data) throws SecurityException{
		if((!kernelSig && address <= LIMIT) || (kernelSig)){	//Assign data to memory address if not in kernel mode and below user stack limit or when it is in kernel mode
			memory[address] = data;
		}else{
			throw new SecurityException("Memory violation: accessing system address 1000 in user mode");	//Else if not, throw an Exception
		}
	}
	
	public static void loadMemory(String filename) throws FileNotFoundException{
		
		Scanner input = new Scanner(new File(filename));	//Read in file name one by one
		int counter = 0;
		
		while (input.hasNextLine()){
			
			String str = input.nextLine().split(" ")[0];	//Read only the int before the space
			
			try {
				
				if (str.indexOf(".") != -1) {	//if int has a . before it then take that int and have it as the address for the next int
					
					counter = Integer.parseInt(str.substring(1, str.length()));
					
				}else{
					
					memory[counter++] = Integer.parseInt(str);
					
				}	
			}catch (Exception e) {	//Catch exception
				counter--;
			}
			
		}
		
	}
	
}