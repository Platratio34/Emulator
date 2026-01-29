import SysD;

namespace System.Collections;

abstract class Queue<T> extends Collection<T> {
    protected uint32 _size = 0;

    public abstract bool enqueue(T el);

    public abstract bool has();

    public abstract T dequeue();

    public abstract T peek();

    @Override
    public uint32 size() {
        return _size;
    }
}