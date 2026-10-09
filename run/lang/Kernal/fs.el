import SysD;

namespace Kernal {

    enum OpenMode {
        READ(0b0_0000),
        WRITE(0b1_0000),
        APPEND(0b1_0001),

        STREAM_READ(0b0_1000),
        STREAM_WRITE(0b1_1000);
    }

    @/
        Open a new file under the currently active process

        @param path The path to the file to open
        @param mode The mode to open the file with
        @returns `-1` on a failure.
        @returns Else returns the file handle
    /@
    @Syscall(0x10)
    public static int32 fopen(char* path, OpenMode mode) {
        ProcessState& proc = &processStates[SysD.rPID];
        if(proc.files == nullptr) {
            proc.files = ProcessFiles.new();
            if(proc.files == nullptr) {
                return -1;
            }
        }
        return proc.files.open(path, mode);
    }

    @/
        Write to a file under the currently active process

        @param handle The file handle from `fopen`
        @param buffer The data to write to the file
        @param len The number of words of data to write

        @returns If write was successfull
    /@
    @Syscall(0x11)
    public static bool fwrite(int32 handle, void* buffer, int32 len) {
        ProcessState& proc = &processStates[SysD.rPID];
        if(proc.files == nullptr) {
            proc.files = ProcessFiles.new();
            if(proc.files == nullptr) {
                return false;
            }
        }
        if(handle < 0 || handle > proc.files.handles.length) {
            return false;
        }
        if(proc.files.handles[handle] == nullptr) {
            return false;
        }
        return proc.files.handles[handle].write(buffer, len);
    }

    @/
        Write to a file under the currently active process.

        This will write directly from the source buffer without flushing anything currently in the file handle buffer.

        @param handle The file handle from `fopen`
        @param buffer The data to write to the file
        @param len The number of words of data to write

        @returns If write was successfull
    /@
    @Syscall(0x11)
    public static bool fwriteD(int32 handle, void* buffer, int32 len) {
        ProcessState& proc = &processStates[SysD.rPID];
        if(proc.files == nullptr) {
            proc.files = ProcessFiles.new();
            if(proc.files == nullptr) {
                return false;
            }
        }
        if(handle < 0 || handle > proc.files.handles.length) {
            return false;
        }
        if(proc.files.handles[handle] == nullptr) {
            return false;
        }
        return proc.files.handles[handle].write(buffer, len);
    }

    @/
        Flush a file handle under the currently active process

        @param handle The file handle from `fopen`

        @returns If the handle existed
    /@
    @Syscall(0x13)
    public static bool fflush(int32 handle) {
        ProcessState& proc = &processStates[SysD.rPID];
        if(proc.files == nullptr) {
            proc.files = ProcessFiles.new();
            if(proc.files == nullptr) {
                return false;
            }
        }
        if(handle < 0 || handle > proc.files.handles.length) {
            return false;
        }
        if(proc.files.handles[handle] == nullptr) {
            return false;
        }
        proc.files.handles[handle].flush();
        return true;
    }

    struct ProcessFiles {
        public int32 numOpen;
        public FileHandle*[16] handles;
        
        @/
            Open a new file handled in the set.

            @param path The path to the file to open
            @param mode The mode to open the file with
            @returns `-1` on a failure.
            @returns Else returns the file handle
        /@
        public int32 open(char* path, OpenMode mode) {
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
            ptr.setup(path, mode);
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
        @/ The mode the handle was opened in /@
        public OpenMode mode;

        @/ The path to the file opened /@
        public char* path;

        public void* intBuffer = 0;
        public uint16 bufferCapacity = 0;
        public uint16 bufferSize = 0;

        public void setup(char* path, OpenMode mode) {
            this.path = path;
            this.mode = mode;
        }

        @/
            Close the file handle, flushing any remaining input if open for write access;
        /@
        public void close() {
            if(mode & OpenMode.WRITE != 0) {
                // flush?
            }
            release();
        }
        @/
            Write a buffer to the file.

            If the write operation would exceed the avalible remaning space in the internal buffer, the buffer will be flushed prior to the write.

            @param buffer The buffer to write
            @param len The number of words to write from the buffer
        /@
        public bool write(void* buffer, int32 len) {
            if(mode & OpenMode.WRITE == 0 || intBuffer == nullptr) {
                return false;
            }
            if(bufferSize + len > bufferCapacity) {
                flush();
            }
            if(len > bufferCapacity) {
                writeDirect(buffer, len);
                return true;
            }
            ...
            bufferSize += len;
            // TODO something here?
            return true;
        }

        @/
            Write a buffer directly to the target.

            This bypasses the internal buffer and will be written **befre** anything still in it.
            It is recommend to make a call to `flush` before if any data has been written

            @param buffer The buffer to write
            @param len The number of words to write
        /@
        public void writeDirect(void* buffer, int32 len) {
            if(buffer == nullptr || len == 0) {
                return;
            }
            if(mode == OpenMode.STREAM_WRITE) {
                if(path == nullptr) {
                    Kernal.printStr(buffer, len);
                }
                return;
            }
            if(mode & OpenMode.WRITE == 0) {
                return;
            }

        }

        @/
            Flushes any content from the write buffer to the handle's destination.
        /@
        public void flush() {
            writeDirect(intBuffer, bufferSize);
            bufferSize = 0;
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

    namespace FS {
        // struct FileWriteCommand
    }
}