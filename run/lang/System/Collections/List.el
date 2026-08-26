import SysD;

namespace System.Collections;

abstract class List<T> extends Collection<T> {
    protected int32 _size = 0;

    public abstract int32 add(T el);

    public abstract T get(int32 index);

    public abstract T remove(int32 index);

    @Override
    public int32 size() {
        return _size;
    }
}