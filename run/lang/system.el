import SysD;
import Kernal.Console;

namespace System {
    
    private static Console* console;
    
    private static const uint32 initialHeapSize = 0x1000;
    private static const uint32 maxHeapSize = 0x1_0000;
    private static const uint32 MALLOC_BLOCK_SIZE = 0x0800;

    private static uint32 heapSize;
    private static void* heapStart;
    /*
    heap: {
        [0]: 0
        [1]: [free size, including this]
        ...
        [0]: 0x8000_0001
        [1]: obj1
        [2]: 0x8000_0002
        [3]: obj2.m1
        [4]: obj2.m2
        [5]: 0x0000_0004
        [6]: -
        [7]: -
        [8]: -
        [9]: -
        [10]: 0x8000_0001
        [11]: obj3
    }
    */

    internal static void main() {
        heapStart = Kernal.Memory.mallocBlock();
        heapSize = MALLOC_BLOCK_SIZE;
        while(heapSize < initialHeapSize) {
            Kernal.Memory.mallocBlock();
            heapSize += MALLOC_BLOCK_SIZE;
        }
    }

    public static void onInterrupt(uint32 code) {

    }

    public static void exit() {
        Kernal.exit();
    }

    public static void* malloc(uint32 words) {
        void* ptr = heapStart;
        uint32 heapEnd = heapStart + heapSize
        while(ptr < heapEnd - 1) {
            uint32 wordsAllocated = *ptr;
            if(wordsAllocated > 0x8000_0000) {
                ptr += wordsAllocated & 0x7fff_ffff;
            } else {
                uint32 wordsFree = wordsAllocated;
                if(wordsFree >= words && (ptr+words) < heapEnd) {
                    ptr[0] = words | 0x8000_0000;
                    wordsFree -= words + 1;
                    if(ptr+words+1 < heapEnd) {
                        ptr[words+1] = wordsFree;
                    }
                    return ptr+1;
                } else {
                    ptr += wordsFree;
                }
            }
        }
        if(heapSize < maxHeapSize) {
            Kernal.Memory.mallocBlock();
            heapSize += MALLOC_BLOCK_SIZE;
        }
        ptr[0] = words | 0x8000_0000;
        ptr[words+1] = heapSize - (ptr + words + 1);
        return ptr+1;
    }

    public static void* free(void* ptr) {
        uint32 heapEnd = heapStart + heapSize
        if(ptr < heapStart || ptr > heapEnd) {
            return;
        }
        uint32 length = *(ptr-1);
        uint32 nextWordsFree;
        if(length + ptr >= heapEnd) {
            nextWordsFree = heapEnd - ptr;
        } else if(ptr[length] > 0x8000_0000) {
            nextWordsFree = ptr[0x8000_0001] & 0x7fff_ffff;
        } else {
            nextWordsFree = length;
        }
        ptr--;
        ptr[0] = nextWordsFree;
    }
}