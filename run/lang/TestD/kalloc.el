import SysD;

namespace Kernal {

    @/ The maximum number of pages a process can have /@
    protected static const int32 MAX_BLOCKS = 32;

    @/ Table of free flags for memory pages by absolute index /@
    protected static const bool* pageFreeTable = 0x8000;

    @/ Allocates a new memory page to the active process /@
    @Syscall(0x01)
    public static void* kalloc() {
        int32 cPages = SysD.rMemTbl[0];
        if(cPages >= MAX_BLOCKS) { // max per-process page allocation
            return nullptr;
        }
        int32 i = 0;
        while(pageFreeTable[i] && (i < 0x1000)) {
            i++;
        }
        if(i == 0x1000) { // no free blocks
            return nullptr;
        }
        pageFreeTable[i] = true;
        void* addr = 0x2_0000 + (cPages << 12);
        cPages += 1;
        SysD.rMemTbl[cPages] = 0x2_0000 + (i << 12);
        SysD.rMemTbl[0] = cPages;
        return addr;
    }

    @/ Frees a number of pages from the active process /@
    @Syscall(0x02)
    public static void kfree(int32 num) {
        int32 cPages = SysD.rMemTbl[0];
        if(num > cPages) {
            num = cPages;
        }
        while(num > 0) {
            cPages--;
            pageFreeTable[(cast<int32>(SysD.rMemTbl[cPages]) - 0x2_0000) >> 12] = false;
            num--;
        }
        int32 t = cast<int32>(0x200 + cPages);
        SysD.rMemTbl[0] = cPages;
    }
}