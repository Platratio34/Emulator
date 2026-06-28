namespace Memory {

    protected static void* heapStart = 0x2_3000;
    // protected static MemroyBlock* firstFree = 0x2_2000;
    protected static MemoryBlock* allocatedBlocks = 0x2_2000;

    public static void* malloc(uint32 size) {
        MemoryBlock* block = allocatedBlocks;
        void* lastEnd = block.end;
        void* found = nullptr;
        while(block.start != 0) {
            uint32 emptySize = block.start - lastEnd;
            if(emptySize >= size) {
                if(lastEnd == 0) {
                    lastEnd = heapStart;
                }
                found = lastEnd;
                block.end += size;
            }
            block++;
        }
        return found;
    }

    protected struct MemoryBlock {
        public void* start;
        public void* end;
    }
}