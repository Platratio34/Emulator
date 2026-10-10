import Kernal;

namespace FS {

    class FileHandle {
        private int32 kernalID = -1;

        public FileHandle(int32 kernalID) {
            this.kernalID = kernalID;
        }

        public ~FileHandle() {
            if(kernalID == -1) {
                return;
            }
            close();
        }

        public void close() {
            Kernal.FS.close(kernalID);
            kernalID = -1;
        }
    }

    public static FileHandle* open(char* path, Kernal.FS.OpenMode mode) {
        int32 id = Kernal.FS.open(path, mode);
        if(id == -1) {
            return nullptr;
        }
        return new FileHandle(id);
    }
}