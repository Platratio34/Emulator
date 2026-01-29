namespace System.Collections;

abstract class Collection<T> {

    public abstract uint32 size();

    public abstract bool contains(T el);
}