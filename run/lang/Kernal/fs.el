import SysD;

namespace Kernal {

    protected static ProcessFiles[32] _pool;
    protected static ProcessFiles* firstFree; 

    public static void setupFS() {
        firstFree = &_pool;
        for(int32 i = 0; i < _pool.length; i++) {
            _pool[i].numOpen = cast<int32>(&_pool[i+1]);
        }
        _pool[_pool.length - 1].numOpen = 0;
    }

    public static ProcessFiles* getNew() {
        if(firstFree == nullptr) {
            return nullptr;
        }
        ProcessFiles* next = firstFree;
        firstFree = force_cast<ProcessFiles*>(firstFree.numOpen);
        return next;
    }
    public static void release(ProcessFiles* files) {
        // TODO probably should close any active file handles first...
        files.numOpen = cast<int32>(firstFree);
        firstFree = files;
    }

    struct ProcessFiles {
        public int32 numOpen;
    }
}