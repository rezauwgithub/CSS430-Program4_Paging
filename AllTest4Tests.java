



class AllTest4Tests extends Thread {
	
	
	public void run() {
		
		// Random Accesses Performance Test
		doTest(1, "disabled");
		doTest(1, "enabled");
		
		// Localized Accesses Performance Test
		doTest(2, "disabled");
		doTest(2, "enabled");
		
		// Mixed Accesses Performance Test
		doTest(3, "disabled");
		doTest(3, "enabled");
		
		// Adversary Accesses Performance Test
		doTest(4, "disabled");
		doTest(4, "enabled");
		
		
		SysLib.cout("\n");
		SysLib.exit();
	
	}
	
	
	
	private void doTest(int testType, String parameter) {
			
		SysLib.cout("\n");
		
		SysLib.exec(SysLib.stringToArgs("Test4 " + parameter + " " + testType));
		SysLib.join();
		
		SysLib.cout("Test " + testType + " - " + parameter + " finished.\n");		
	}	
}