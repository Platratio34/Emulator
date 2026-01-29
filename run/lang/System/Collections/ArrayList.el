import SysD;

namespace System.Collections;

class ArrayList<T> extends List<T> {
    protected uint32 allocSize = 0;
    protected uint32 minSize = 0;
    protected T* ptr;

    public ArrayList<T>() {

    }
    /**
     * Creates a new ArrayList, setting the minimum allocated size
    */
    public ArrayList<T>(uint32 minSize) {
        allocSize = minSize;
        this.minSize = minSize;
        ptr = malloc(allocSize * sizeof(T));
    }

    /**
     * May cause re-allocation
    */
    public void setMinSize(uint32 minSize) {
        if(this.minSize == minSize) {
            return;
        }
        this.minSize = minSize;
        // only re-allocate if 1) the allocated size is smaller OR 2) the allocated size is larger & the current size smaller than the new minium
        if(allocSize < minSize || (allocSize > minSize && _size <= minSize)) {
            T* p2 = malloc(newSize * sizeof(T));
            SysD.memCopy(ptr, 0, _size, p2, 0);
            free(ptr);
            ptr = p2;
            allocSize = minSize;
        }
    }
    
    public uint32 getMinSize() {
        return minSize;
    }

    @Override
    public uint32 add(T el) {
        if(allocSize == _size) {
            allocSize += 2;
            T* p2 = malloc(allocSize * sizeof(T));
            SysD.memCopy(ptr, 0, _size, p2, 0);
            free(ptr);
            ptr = p2;
        }
        uint32 i = _size;
        ptr[i] = el;
        _size++;
        return i;
    }

    @Override
    public T get(uint32 index) {
        if(index < 0 || index >= _size) {
            throw;
        }
        return ptr[index];
    }

    @Override
    public T remove(uint32 index) {
        if(index < 0 || index >= _size) {
            throw;
        }
        T el = ptr[index];
        _size--;
        if(_size < allocSize - 4 && allocSize > minSize) { // we can shrink
            uint32 nSize = _size;
            if(nSize < minSize)
                nSize = minSize;
            T* p2 = malloc(nSize * sizeof(T));
            SysD.memCopy(ptr, 0, index, p2, 0);
            if(index < _size) { // wasn't last element
                SysD.memCopy(ptr, index+1, _size, p2, index); // shift everything down
            }
            allocSize = nSize;
            free(ptr);
            ptr = p2;
        } else if(index < _size) { // wasn't last element
            SysD.memCopy(ptr, index+1, _size, ptr, index); // shift everything down
        }
        return el;
    }
    
    @Override
    public bool contains(T el) {
        for(uint32 i = 0; i < _size; i++) {
            if(ptr[i] == el) {
                return true;
            }
        }
        return false;
    }
}