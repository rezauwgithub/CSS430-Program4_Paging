import java.io.*;

public class Cache {
 
	private static final int EMPTY_BLOCK = -1;					
	private static final int INVALID_BLOCK = -2;	
	
	private int victimBlock;								
	private int targetBlock;								 
	private int blockSize;							
	private int pageSize;							

	
	CacheBlock[] theCache;
	

	private class CacheBlock {
		
		// contains the disk block number of cached data. 
		// It should be set to -1 if this entry does not 
		// have valid block information.
		int blockFrame;
		
		// is set to 1 or true whenever this block is accessed. 
		// Reset it to 0 or false by the second-chance algorithm 
		// when searching a next victim.
		boolean referenceBit;
		
		// is set to 1 or true whenever this block is written. 
		// Reset it to 0 or false when this block is written 
		// back to the disk.
		boolean dirtyBit;
		
		byte[] byteDataBlock;
		
		private CacheBlock(int blockSize) {
			
			byteDataBlock = new byte[blockSize];
			
			blockFrame = EMPTY_BLOCK;
			
			referenceBit = false;					
			dirtyBit = false;						
		}
	}


	// The constructor: allocates a cacheBlocks number of cache blocks, 
	// each containing blockSize-byte data, on memory
	public Cache(int blockSize, int cacheBlocks) {
		
		theCache = new CacheBlock[cacheBlocks];
		
		pageSize = theCache.length;	
		
		this.blockSize = blockSize;						
		victimBlock = cacheBlocks - 1;
		
		for (int i = 0; i < pageSize; i++) {
			
			theCache[i] = new CacheBlock(blockSize);
		} 
	}
	

	private void updateCache(int targetBlock, int frame, boolean boolValue) {
		
		theCache[targetBlock].blockFrame = frame;				
		theCache[targetBlock].referenceBit = boolValue;			
	}	
	

	private void readCache(int targetBlock, int blockId, byte[] buffer) {
		
		System.arraycopy(theCache[targetBlock].byteDataBlock, 0, buffer, 0, blockSize);
		updateCache(targetBlock, blockId, true);	
	}	
	

	private void writeDisk(int targetBlock) {
		
		if (theCache[targetBlock].dirtyBit && (theCache[targetBlock].blockFrame != EMPTY_BLOCK)) {
			
			SysLib.rawwrite(theCache[targetBlock].blockFrame, theCache[targetBlock].byteDataBlock);
			
			theCache[targetBlock].dirtyBit = false;			
		}
	}


	private void addToCache(int targetBlock, int blockId, byte[] buffer) {
		
		System.arraycopy(buffer, 0, theCache[targetBlock].byteDataBlock, 0, blockSize);
		
		theCache[targetBlock].dirtyBit = true;			
		updateCache(targetBlock, blockId, true);
	}	
	
	
	private int theVictim() {
		
		while(true) {
			
			victimBlock++;
			victimBlock = (victimBlock % pageSize);
			
			if (!theCache[victimBlock].referenceBit) {
				
				return victimBlock;
			}
			
			
			theCache[victimBlock].referenceBit = false; 	
		}
	}	


	
	private int search(int blockToFind) {
		
		for (int i = 0; i < pageSize; i++) {	
		
			if(theCache[i].blockFrame == blockToFind) {
				
				return i;							
			}
		}
		
		return INVALID_BLOCK;								
	}
	

	

	// reads into the buffer[ ] array the cache block specified by blockId 
	// from the disk cache if it is in cache, otherwise reads the corresponding 
	// disk block from the disk device. Upon an error, it should return false, 
	// otherwise return true.
	public synchronized boolean read(int blockId, byte[] buffer) {
		
		if (blockId < 0) { 
		
			return false; 
		}
		
		
		targetBlock = search(blockId);					
		if (targetBlock != INVALID_BLOCK) {
			
			readCache(targetBlock, blockId, buffer);
			
			return true; 
		}
		
		
		targetBlock = search(EMPTY_BLOCK);					
		if (targetBlock != INVALID_BLOCK) {
			
			SysLib.rawread(blockId, theCache[targetBlock].byteDataBlock); 
			readCache(targetBlock, blockId, buffer);
			
			return true;
		}

		
		writeDisk(theVictim());					
		SysLib.rawread(blockId, theCache[victimBlock].byteDataBlock);	 
       	readCache(victimBlock, blockId, buffer);

		
		return true;
	}


	// writes the buffer[ ]array contents to the cache block specified 
	// by blockId from the disk cache if it is in cache, otherwise finds 
	// a free cache block and writes the buffer [ ] contents on it. 
	// No write through. Upon an error, it should return false, 
	// otherwise return true.
	public synchronized boolean write(int blockId, byte[] buffer) {
		
		if (blockId < 0) {
			
			return false; 
		}
		

		targetBlock = search(blockId);					
		if( targetBlock != INVALID_BLOCK) { 	
		
			addToCache(targetBlock, blockId, buffer);	
			
			return true; 
		}

		
		targetBlock = search(EMPTY_BLOCK);					
		if (targetBlock != INVALID_BLOCK) {
			
			addToCache(targetBlock, blockId, buffer);	
			
			return true; 
		}

		
		writeDisk(theVictim());					
		addToCache(victimBlock, blockId, buffer);

		
		return true;								
	}


	// writes back all dirty blocks to Disk.java and therefater forces 
	// Diskjava to write back all contents to the DISK file. 
	// The sync( ) method still maintains clean block copies in Cache.java, 
	// while the flush( ) method invalidates all cached blocks. 
	// The former method must be called when shutting down ThreadOS. 
	// On the other hand, the later method should be called when you keep 
	// running a different test case without receiving any caching effects 
	// incurred by the previous test.
	public synchronized void sync() {
		
		for (int i = 0; i < pageSize; i++) { 
			writeDisk(i); 
		}
		
		SysLib.sync();								
	}
	
	public synchronized void flush() {
		
		for (int i = 0; i < pageSize; i++) {
			
			writeDisk(i);							
			updateCache(i, EMPTY_BLOCK, false);			
		}
		
		SysLib.sync();								
	}
	
	/////////////////////////////////////////////////////////////////////////




}
