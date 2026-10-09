import SysD;

namespace Kernal {

    struct ProcessFiles {
        public int32 numOpen;
        public FileHandle*[16] handles;
        
        @/
            Open a new file handled in the set.

            @param path The path to the file to open
            @param read If the file should be opened for read access
            @param write If the file should be opened for write access
            @returns `-1` on a failure.
            @returns Else returns the file handle
        /@
        public int32 open(char* path, bool read, bool write) {
            if(numOpen == handles.length) {
                return -1;
            }
            int32 outHandle = 0;
            for(; outHandle < handles.length; outHandle++) {
                if(handles[outHandle] == nullptr) {
                    break;
                }
            }
            FileHandle* ptr = FileHandle.new();
            if(ptr == nullptr) {
                return -1;
            }
            handles[outHandle] = ptr;
            ptr.setup(path, read, write);
            numOpen++;
            return outHandle;
        }

        public void close() {
            if(numOpen == 0) {
                return;
            }
            for(int32 i = 0; i < handles.length; i++) {
                if(handles[i] != nullptr) {
                    handles[i].close();
                    handles[i] = nullptr;
                }
            }
            numOpen = 0;
        }

        protected static ProcessFiles[128] pool;
        protected static ProcessFiles* nextFree = 0xffff_ffff;

        private void reset() {
            for(int32 i = 0; i < handles.length; i++) {
                handles[i] = nullptr;
            }
            numOpen = 0;
        }

        public static FileHandle* new() {
            if(nextFree == 0xffff_ffff) {
                nextFree = &pool;
                for(int32 i = 0; i < pool.length - 1; i++) {
                    pool[i].numOpen = cast<int32>(&pool[i+1]);
                }
                pool[pool.length - 1].numOpen = 0;
            }
            if(nextFree == nullptr) {
                return nullptr;
            }
            ProcessFiles* next = nextFree;
            nextFree = force_cast<ProcessFiles*>(next.numOpen);
            next.reset();
            return next;
        }

        protected void release() {
            numOpen = cast<int32>(nextFree);
            nextFree = &this;
        }
    }

    @/
        Kernal level file handle.
    /@
    struct FileHandle {
        @/ Peripheral level file handle. Doubles as next free pointer when un-allocated /@
        public int32 rawHandle;
        @/ If the handle is open for read access /@
        public bool readOpen;
        @/ If the handle is open for write access /@
        public bool writeOpen;

        @/ If the file handle represents the console /@
        public bool isConsole;

        @/ The path to the file opened /@
        public char* path;

        public void setup(char* path, bool read, bool write) {
            this.path = path;
            readOpen = read;
            writeOpen = write;
        }

        @/
            Close the file handle, flushing any remaining input if open for write access;
        /@
        public void close() {
            if(writeOpen) {
                // flush?
            }
            release();
        }

        @/
            Write a byte buffer to the file

            @param buffer The buffer to write
            @param len The number of bytes to write from the buffer
        /@
        public void write(uint8* buffer, int32 len) {
            if(!writeOpen) {
                return;
            }
            if(isConsole) {
                Kernal.printStr(cast<char*>(buffer), len);
                return;
            }
            // TODO something here?
        }
        @/
            Write a buffer to the file.

            **IF handle represents a console stream the write will be ignored**

            @param buffer The buffer to write
            @param len The number of words to write from the buffer
        /@
        public void write(void* buffer, int32 len) {
            if(!writeOpen) {
                return;
            }
            if(isConsole) {
                return;
            }
            // TODO something here?
        }


        @/ Pool of file handles for allocation /@
        protected static FileHandle[128] pool;
        @/ Pointer to the next un-aoocated file handle /@ 
        protected static FileHandle* nextFree = 0xffff_ffff;

        @/
            Get a new file handle.

            @returns `nullptr` if there are no avalible file handles
            @returns Otherwise returns a pointer to the allocated handle
        /@
        public static FileHandle* new() {
            if(nextFree == 0xffff_ffff) {
                nextFree = &pool;
                for(int32 i = 0; i < pool.length - 1; i++) {
                    pool[i].rawHandle = cast<int32>(&pool[i+1]);
                }
                pool[pool.length - 1].rawHandle = 0;
            }
            if(nextFree == nullptr) {
                return nullptr;
            }
            FileHandle* next = nextFree;
            nextFree = force_cast<FileHandle*>(next.rawHandle);
            return next;
        }

        @/
            Releases the file handle for re-allocation.

            Use after calling this is equal to a use after free.
        /@
        protected void release() {
            rawHandle = cast<int32>(nextFree);
            nextFree = &this;
        }
    }
}