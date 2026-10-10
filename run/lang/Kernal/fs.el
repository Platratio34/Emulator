import SysD;
import Peripheral;

namespace Kernal.FS {

    enum OpenMode {
        @/ Read Only /@
        READ(0b0_0000),
        @/ Write (overwrite) /@
        WRITE(0b1_0000),
        @/ Write append /@
        APPEND(0b1_0001),

        @/ Stream read only /@
        STREAM_READ(0b0_1000),
        @/ Stream write only /@
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
    public static int32 open(char* path, OpenMode mode) {
        if(fsDeviceId == 0) {
            if(!setupFS()) {
                return -1;
            }
        }
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

        @param handle The file handle from `open`
        @param buffer The data to write to the file
        @param len The number of words of data to write

        @returns If write was successful
    /@
    @Syscall(0x11)
    public static bool write(int32 handle, void* buffer, int32 len) {
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

        @param handle The file handle from `open`
        @param buffer The data to write to the file
        @param len The number of words of data to write

        @returns If write was successful
    /@
    @Syscall(0x12)
    public static bool writeD(int32 handle, void* buffer, int32 len) {
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

        @param handle The file handle from `open`

        @returns If the handle existed
    /@
    @Syscall(0x13)
    public static bool flush(int32 handle) {
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

    @/
        Flush a file handle under the currently active process

        @param handle The file handle from `open`

        @returns If the handle existed
    /@
    @Syscall(0x13)
    public static bool read(int32 handle, void* buffer, int32 capacity) {
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
    

    @/
        Close a handle under the currently active process

        @param handle The file handle from `open` to close
    /@
    @Syscall(0x1f)
    public static void close(int32 handle) {
        ProcessState& proc = &processStates[SysD.rPID];
        if(proc.files == nullptr) {
            proc.files = ProcessFiles.new();
            if(proc.files == nullptr) {
                return;
            }
        }
        if(handle < 0 || handle > proc.files.handles.length) {
            return;
        }
        if(proc.files.handles[handle] == nullptr) {
            return;
        }
        proc.files.handles[handle].close();
        proc.files.handles[handle] = nullptr;
        return;
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
            if(!ptr.open(path, mode)) {
                ptr.release();
                return -1;
            }
            handles[outHandle] = ptr;
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
            if(numOpen > 0) {
                for(int32 i = 0; i < handles.length; i++) {
                    if(handles[i] != nullptr) {
                        handles[i].close();
                        handles[i] = nullptr;
                    }
                }
            }
            numOpen = cast<int32>(nextFree);
            nextFree = this;
        }
    }

    private static int32 fsDeviceId = 0;
    public static bool setupFS() {
        for(int32 i = 1; i < 64; i++) {
            if(Peripheral.TABLE[i] == Peripheral.TYPE_STORAGE_VIRTUAL) {
                fsDeviceId = i;
                break;
            }
        }
        return fsDeviceId != 0;
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

        @/ Internal write buffer /@
        public void* intBuffer = 0;
        @/ Internal buffer capacity /@
        public uint16 bufferCapacity = 0;
        @/ Internal buffer current fill size /@
        public uint16 bufferSize = 0;
        @/ Read/Write Offset /@
        public int32 offset = 0;

        public bool open(char* path, OpenMode mode) {
            this.path = path;
            this.mode = mode;

            if(mode & OpenMode.STREAM_READ != 0) {
                // stream handle
                return true;
            }
            FileOpenCommand cmd = {,path};
            Peripheral.command(fsDeviceId, sizeof(cmd) / 4, &cmd);
            rawHandle = Peripheral.RSP_DATA[1];
            if(mode & OpenMode.WRITE != 0) {
                intBuffer = new int32[128];
                bufferCapacity = 128;
            }
            return rawHandle == 0;
        }

        @/
            Close the file handle, flushing any remaining input if open for write access;
        /@
        public void close() {
            if(mode & OpenMode.WRITE != 0) {
                // flush?
                flush();
            }
            release();
        }
        @/
            Write a buffer to the file.

            If the write operation would exceed the available remaining space in the internal buffer, the buffer will be flushed prior to the write.

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
            // ...
            bufferSize += len;
            // TODO something here?
            return true;
        }

        @/
            Write a buffer directly to the target.

            This bypasses the internal buffer and will be written **before** anything still in it.
            It is recommend to make a call to `flush` before if any data has been written

            @param buffer The buffer to write
            @param len The number of words to write
        /@
        public void writeDirect(void* buffer, int32 len) {
            if(mode == OpenMode.STREAM_WRITE) {
                if(path == nullptr) { // terminal out stream
                    Kernal.printStr(buffer, len);
                }
                return;
            }
            if((mode & OpenMode.WRITE == 0) || buffer == nullptr || len == 0) {
                return;
            }
            offset += len;

        }

        @/
            Flushes any content from the write buffer to the handle's destination.
        /@
        public void flush() {
            writeDirect(intBuffer, bufferSize);
            bufferSize = 0;
        }

        public void read(void* buffer, int32 len, int32& count) {
            if(mode == OpenMode.STREAM_READ) {
                if(path == nullptr) { // terminal in stream

                }
                return;
            }
            if(mode != OpenMode.READ) {
                return;
            }
            count = 0;
            FileReadCommand cmd = {,rawHandle,buffer,len,offset,count};
            Peripheral.command(fsDeviceId, sizeof(cmd) / 4, &cmd);
            offset += len;
        }

        public void seek(int32 newOffset) {
            offset = newOffset;
        }


        @/ Pool of file handles for allocation /@
        protected static FileHandle[128] pool;
        @/ Pointer to the next un-allocated file handle /@ 
        protected static FileHandle* nextFree = 0xffff_ffff;

        @/
            Get a new file handle.

            @returns `nullptr` if there are no available file handles
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
            nextFree = this;
        }
    }

    struct FileOpenCommand {
        public final int32 cmd = 0x10;
        public char* path;
    }
    
    struct FileReadCommand {
        public final int32 cmd = 0x11;
        public int32 handle;
        public void* buffer;
        public int32 bufferCapacity;
        public int32 offset;
        public int32& readPtr;
    }
}