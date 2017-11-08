

import java.util.*;

public class Test4 extends Thread {
	
    private static final int BLOCK_SIZE = 512;
    private static final int ARRAY_SIZE = 350;
	
    private boolean isCaching;
	
	private int testCase = 0;
	
    private long startReadTime;
	private long stopReadTime;
	
    private long startWriteTime;
    private long stopWriteTime;
	
    private Random random;
	
    private String label = "Not Initialized";
	
	
	private byte[] readBlock;
	private byte[] writeBlock;


    public Test4 (String args[]) {
		
        testCase = Integer.parseInt(args[1]);

        readBlock = new byte[BLOCK_SIZE];		
        writeBlock = new byte[BLOCK_SIZE];
		
        random = new Random();
        random.nextBytes(writeBlock);
		
		isCaching = ((args[0].equals("enabled") || args[0].equals("enable")) || args[0].equals("-enabled"));
		
		
		if (isCaching == false) {
			if (!(args[0].equals("disabled") || args[0].equals("disable") || args[0].equals("-disabled"))) {
			
				SysLib.cout("Warning: Invalid Argument\n");
				SysLib.exit();
			}
		}
    }
	
	
	
    public void run() {
		
        SysLib.flush();

        switch (testCase) {
			
            case 1: randomAccessPerformanceTest();
                break;
				
            case 2: localizedAccessPerformanceTest();
                break;
				
            case 3: mixedAccessPerformanceTest();
                break;
				
            case 4: adversaryAccessPerformanceTest();
                break;
				
            default: SysLib.cout("Warning: Invalid Argument\n");
                break;
        }
		
        sync();
        SysLib.exit();
    }
	

    public void randomAccessPerformanceTest() {
		
        label = "Random Access Performance Test";
		
        int[] randomAccess = new int[ARRAY_SIZE];
		
        for (int i = 0; i < ARRAY_SIZE; i++) {
			
            randomAccess[i] = randomInt(512);
        }
		
        startWriteTime = getCurrentTime();
		
		
        for (int i = 0; i < ARRAY_SIZE; i++) {
            write(randomAccess[i], writeBlock);
        }
		
		
        stopWriteTime = getCurrentTime();
        startReadTime = getCurrentTime();
		
		
        for (int i = 0; i < ARRAY_SIZE; i++) {
            read(randomAccess[i], readBlock);
        }
		
        stopReadTime = getCurrentTime();
		
        validate();
        results();
    }


    public void localizedAccessPerformanceTest() {
		
        label = "Localized Access Performance Test";
		
        startWriteTime = getCurrentTime();
		
        for (int i = 0; i < ARRAY_SIZE; i++) {
            for (int j = 0; j < 10; j++) {
				
                write(j, writeBlock);
            }
        }
		
		
        stopWriteTime = getCurrentTime();
        startReadTime = getCurrentTime();
		
		
        for (int i = 0; i < ARRAY_SIZE; i++) {
            for (int j = 0; j < 10; j++) {
				
                read(j, readBlock);
            }
        }
		
		
        stopReadTime = getCurrentTime();

        validate();
        results();
    }


	
    public void mixedAccessPerformanceTest() {
		
        label = "Mixed Access Performance Test";
		
        int[] mixedAccess = new int[ARRAY_SIZE];
		
        for (int i = 0; i < ARRAY_SIZE; i++) {
			
            if (randomInt(10) < 9) {
                mixedAccess[i] = randomInt(10);
            }
            else {
                mixedAccess[i] = randomInt(512);
            }
        }

        startWriteTime = getCurrentTime();
		
        for (int i = 0; i < ARRAY_SIZE; i++) {
            write(mixedAccess[i], writeBlock);
        }
		
        stopWriteTime = getCurrentTime();

        startReadTime = getCurrentTime();
		
        for (int i = 0; i < ARRAY_SIZE; i++) {
            read(mixedAccess[i], readBlock);
        }
		
        stopReadTime = getCurrentTime();
		
		
        validate();
        results();
    }


    public void adversaryAccessPerformanceTest() {
		
        label = "Adversary Access Performance Test";

        startWriteTime = getCurrentTime();
		
        for (int i = 0; i < BLOCK_SIZE; i++) {
            write(i, writeBlock);
        }
		
        stopWriteTime = getCurrentTime();

        startReadTime = getCurrentTime();
		
        for (int i = 0; i < BLOCK_SIZE; i++) {
            read(i, readBlock);
        }
		
        stopReadTime = getCurrentTime();

        validate();
        results();
    }
	
	
    public void sync() {
		
        if (isCaching) {
			
            SysLib.csync();
        }
        else {
			
            SysLib.sync();
        }
    }
	
	
    public void read(int blockId, byte buffer[]) {
		
        if (isCaching) {
			
            SysLib.cread(blockId, buffer);
			
        } 
		else {
			
            SysLib.rawread(blockId, buffer);
        }
    }
	
	
    public void write(int blockId, byte buffer[]) {
        if (isCaching) {
			
            SysLib.cwrite(blockId, buffer);
			
        } 
		else {

            SysLib.rawwrite(blockId, buffer);
        }
    }
	
	
    public void validate() {
		
        if (!Arrays.equals(readBlock, writeBlock))
        {
            SysLib.cout("Warning: read and write blocks differ from one another.\n");
        }
    }
	
	
    public void results() {
		
        String status;
		if (isCaching) {
			status = "enabled";
		}
		else {
			status = "disabled";
		}
		
        SysLib.cout("Test: " + label + " with cache " + status + "\n");
        SysLib.cout("Average Write: " + getAverageWrite() + " msec \nAverage Read: " + getAverageRead() +" msec \n" );
    }
	
	
    public int randomInt(int max) {
		
        return (Math.abs(random.nextInt() % max));
    }

	
    public long getAverageWrite() {
		
        return ((stopWriteTime - startWriteTime) / ARRAY_SIZE);
    }
	
	
    public long getAverageRead() {
		
        return ((stopReadTime - startReadTime) / ARRAY_SIZE);
    }
	
	
    public long getCurrentTime() {
		
        return (new Date().getTime());
    }
}
