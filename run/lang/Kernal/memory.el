import SysD;

namespace Kernal.Memory {

    protected static const int32 MMU_DEVICE_TYPE = 0x0100_0002;
    protected static const int32 MMU_START = 0x1_0000;
    protected static const int32 MMU_MAX_BLOCKS = 0x0800;
    protected static int32 mmuId;

    // protected static SysD.AddressSpace* blocks = MMU_START;
    // protected static SysD.AddressSpace** blocks2 = MMU_START;

    // @Privileged()
    // internal static void _setup() {
    //     mmuId = Kernal.getPeripheral(MMU_DEVICE_TYPE);
    //     Kernal.peripheralCmd(mmuId, 3, new int32[] {0x00, MMU_START, MMU_MAX_BLOCKS});
    // }

    // @Syscall
    // public static void* mallocBlock() {
    //     int32 blockId;
    //     int32 lastOffset = 0;
    //     int32 pid = SysD.rPID;
    //     for(int32 i = 1; i < MMU_MAX_BLOCKS; i++) {
    //         if(blocks[i].state == 0) {
    //             blockId = i;
    //             break;
    //         } elseif(blocks[i].pid == pid && blocks[i].offset > lastOffset) {
    //             lastOffset = blocks[i].offset;
    //         }
    //     }
    //     if(blockId == 0) {
    //         return 0x0;
    //     }
    //     blocks[blockId].state = 1;
    //     blocks[blockId].pid = pid;
    //     blocks[blockId].offset = lastOffset + MEMORY_BLOCK_SIZE;
    //     return (void*)(MEMORY_PROCESS_START + blocks[blockId].offset);
    // }

    // @Syscall
    // public static void freeBlock(void* block) {
    //     int32 pid = SysD.getPID();
    //     int32 offset = ((int32)block) - MEMORY_PROCESS_START;
    //     for(int32 i = 1; i < MMU_MAX_BLOCKS; i++) {
    //         if(blocks[i].pid != pid && blocks[i].state != 1) {
    //             continue;
    //         }
    //         if(blocks[i].offset == offset) {
    //             blocks[i].state = 0;
    //             blocks[i].pid = 0;
    //             return;
    //         }
    //     }
    // }

    // internal static int32 getBlocks() {
    //     int32 pid = SysD.getPID();
    //     int32 num = 0;
    //     for(int32 i = 1; i < MMU_MAX_BLOCKS; i++) {
    //         if(blocks[i].pid == pid && blocks[i].state == 1) {
    //             num++;
    //         }
    //     }
    //     return num;
    // }

    struct PageMapTable {

        public int32 numPages;
        public int32* pagePointers;

    }
}