package Lab4_5;

public class Flag {
	
	private int min;
	private int max;
	
	public Flag(int min, int max) {
		this.min = 0;
		this.max = 40;
	}
	
	public static void main(String[] args) {
		Flag flag = new Flag(1,40);
		flag.print();
	}
	
	public char determineCharacter(int column, int row) {
		// 	???
		if(
			(column >=min && column <min+2) || 
			(row >=min && row <min+2) ||
			(row >=max-1 && row <= max)||
			(column >=max-1 && column <= max)
		) {
			return '?';
		}
		
		// --- or |||
		if((row == 17 && column <=17) ) {
			return '-';
		}else if((column == 17 && row <=17)){
			return '|';
		}
		
		// === or /// or ' '
		if(row<17 && column<17) {
			int num = (column+row) % 3;
			return num == 1 ? '/' : num == 2 ? '=' : ' ';
		}
		
		// 	+++
		if(row == max-column) {
			return '+';
		}
		
		// 	)))
		if((row % 3 == 0) && (column>max-row)) {
			return ')';
		}
		
		// 	(((
		if((column % 4 == 0) && (max-column>row)) {
			return '(';
		}
		
		return ' ';
		
	}
	
	
	public void print() {
		String outputLine;  
		for (int row = min; row <= max; row++){      
		    outputLine = "";      
		    for (int column = min; column <= max; column++){
		        outputLine = outputLine+determineCharacter (column, row);
		    }      
		    System.out.println (outputLine);
		} 
	}
	
	
}
