namespace Memory {

    protected static void* heapStart = 0x2_3000;
    protected static MemoryBlock* allocatedBlocks = 0;
    protected static MemoryBlock* blockFreeList = 0x2_2000;
    protected static const void* ALLOCATED_BLOCK_LIST = 0x2_2000;

    public static void setup() {
        MemoryBlock* list = blockFreeList;
        while(list < (heapStart - 3)) {
            list.next = list + 1;
            list++;
        }
        list.next = nullptr;
    }

    public static void* malloc(uint32 size) {
        if(size > 0x1000) {
            return nullptr;
        }
        if(blockFreeList == nullptr) {
            return nullptr;
        }
        MemoryBlock* block = allocatedBlocks;
        if(block == nullptr) {
            MemoryBlock* next = blockFreeList;
            blockFreeList = blockFreeList.next;
            next.start = block.end + 1;
            next.end = next.start + size - 1;
            next.next = nullptr;
            allocatedBlocks = next;
            return heapStart;
        }
        void* lastEnd = heapStart;
        while((block.next != nullptr) & ((block.start - lastEnd) >= size)) {
            lastEnd = block.end;
            block = block.next;
        }
        if(block.next == nullptr) {
            if(block.end + size - 1 > 0x3_0000) {
                return nullptr;
            }
            MemoryBlock* next = blockFreeList;
            blockFreeList = blockFreeList.next;
            next.start = block.end + 1;
            next.end = next.start + size - 1;
            next.next = nullptr;
            block.next = next;
            return next.start;
        }
        MemoryBlock* next = blockFreeList;
        blockFreeList = blockFreeList.next;
        next.start = block.end + 1;
        next.end = next.start + size - 1;
        next.next = block.next;
        block.next = next;
        return next.start;
    }

    public static void free(void* ptr) {
        if(allocatedBlocks == nullptr) {
            return;
        }
        MemoryBlock* block = allocatedBlocks;
        MemoryBlock* last = nullptr;
        while((block.next != nullptr) & (block.start != ptr)) {
            last = block;
            block = block.next;
        }
        if(block.start != ptr) {
            return;
        }
        if(last == nullptr) {
            allocatedBlocks = nullptr;
        } else {
            last.next = block.next;
        }
        block.next = blockFreeList;
        blockFreeList = block;
    }

    struct MemoryBlock {
        public MemoryBlock* next;
        public void* start;
        public void* end;
    }
}