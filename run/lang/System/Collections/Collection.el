namespace System.Collections;

abstract class Collection<T> {

    public abstract int32 size();

    public abstract bool contains(T el);
}