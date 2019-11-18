package Lab4_5;

public class CodingBatTasks {
	public int[] fix34(int[] nums) {
		int last4 = 0;

		for (int i = 0; i < nums.length; i++) {
			if (nums[i] == 3) {
				for (int j = last4; j < nums.length; j++) {
					if (nums[j] == 4) {
						int temp = nums[i + 1];
						nums[i + 1] = 4;
						nums[j] = temp;
						last4 = j;
						break;
					}
				}
			}
		}
		return nums;
	}

	public boolean no14(int[] nums) {
		if (nums.length < 2) {
			return true;
		}
		boolean has4 = false;
		boolean has1 = false;

		for (int i = 0; i < nums.length; i++) {
			if (nums[i] == 1) {
				has1 = true;
			}
			if (nums[i] == 4) {
				has4 = true;
			}
		}

		return has4 ^ has1;
	}

	public boolean has77(int[] nums) {
		for (int i = 0; i < nums.length - 1; i++) {
			if ((nums[i] == 7 && nums[i + 1] == 7) || (nums[i] == 7 && i != nums.length - 2 && nums[i + 2] == 7)) {
				return true;
			}
		}
		return false;
	}

	public int blackjack(int a, int b) {
		if (a > 21 && b > 21) {
			return 0;
		}
		if (21 - a < 21 - b) {
			return a > 21 ? b : a;
		} else {
			return b > 21 ? a : b;
		}
	}

	public int luckySum(int a, int b, int c) {
		int[] nums = { a, b, c };
		int sum = 0;
		for (int i = 0; i < nums.length; i++) {
			if (nums[i] == 13) {
				break;
			}
			sum = sum + nums[i];
		}
		return sum;
	}

	public boolean nearTen(int num) {
		int near = num % 10;
		return (near <= 2) || (near >= 8);
	}

	public String fizzString(String str) {
		if ((str.charAt(0) == 'f') && (str.charAt(str.length() - 1) == 'b')) {
			return "FizzBuzz";
		}
		if (str.charAt(0) == 'f') {
			return "Fizz";
		}
		if (str.charAt(str.length() - 1) == 'b') {
			return "Buzz";
		}
		return str;
	}

	public boolean cigarParty(int cigars, boolean isWeekend) {
		if (isWeekend) {
			return cigars >= 40;
		}

		return cigars <= 60 && cigars >= 40;
	}
}
