import SysD;

namespace System.Collections;

class LinkedListQueue<T> extends Collection<T> {
    protected QueueEntry<T>* head;
    protected QueueEntry<T>* tail;

    public LinkedListQueue<T>() {

    }

    public bool enqueue(T el) {
        QueueEntry<E>* entry = new QueueEntry<E>(el)
        if(tail != nullptr) {
            tail.next = entry;
        } else if(head == nullptr) {
            head = nullptr;
        }
        tail = entry;
        return true;
    }

    public bool has() {
        return head != nullptr;
    }

    public T dequeue() {
        if(head == nullptr) {
            return nullptr;
        }
        T el = head.el;
        head = head.next;
        return el;
    }

    public T peek() {
        if(head == nullptr) {
            return nullptr;
        }
        return head.el;
    }

    @Override
    public uint32 size() {
        return _size;
    }

    struct QueueEntry<E> {
        public QueueEntry<E>* next;
        public E value;

        internal QueueEntry<E>(E el) {
            this.el = el;
        }
    }
}