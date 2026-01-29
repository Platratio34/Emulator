import SysD;

namespace System.Collections;

class LinkedList<T> extends List<T> {
    protected ListEntry<T>* head = nullptr;
    protected ListEntry<T>* tail = nullptr;

    public LinkedList<T>() {

    }

    @Override
    public uint32 add(T el) {
        uint32 i = _size;
        _size++;
        ListEntry<T>* entry = new ListEntry<T>(el, nullptr);
        if(tail == nullptr) {
            head = entry;
            tail = entry;
        } else {
            tail.next = entry;
            tail = entry;
        }
        return i;
    }

    @Override
    public T get(uint32 index) {
        if(index < 0 || index >= _size) {
            throw;
        }
        if(index == 0) {
            return head.el;
        } else if(index == _size - 1) {
            return tail.el;
        }
        ListEntry<T>* entry = head;
        for(int i = 1; i <= index; i++) {
            entry = entry.next;
        }
        return entry.el;
    }

    @Override
    public T remove(uint32 index) {
        if(index < 0 || index >= _size) {
            throw;
        }
        _size--;
        if(index == 0) {
            T el = head.el;
            head = head.next;
            return el;
        }
        ListEntry<T>* entry = head;
        ListEntry<T>* entryToRemove = head;
        for(int i = 1; i < index; i++) {
            entry = entry.next;
            entryToRemove = entry.next;
        }
        T el = entry.next.el;
        if(tail == entryToRemove) {
            tail = entry;
        } else {
            entry.next = entryToRemove.next;
        }
        return el;
    }
    
    @Override
    public bool contains(T el) {
        if(_size == 0)
            return false;
        ListEntry<T> entry = head;
        if(entry.el == el)
            return true;
        while(entry.next != nullptr) {
            entry = entry.next;
            if(entry.el == el)
                return true;
        }
        return false;
    }

    struct ListEntry<E> {
        public ListEntry<E>* next;
        public E value;

        internal ListEntry<E>(E el, ListEntry<E> next) {
            this.el = el;
            this.next = next;
        }
    }
}