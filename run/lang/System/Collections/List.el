import SysD;

namespace System.Collections;

abstract class List<T> extends Collection<T> {
    protected uint32 _size = 0;

    public abstract uint32 add(T el);

    public abstract T get(uint32 index);

    public abstract T remove(uint32 index);

    @Override
    public uint32 size() {
        return _size;
    }
}