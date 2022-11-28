package cn.treeh.Utils;

import java.util.Iterator;
import java.util.LinkedList;

public class IteratorQueue<E> implements Iterator<E> {
    public Iterator<E> iterator;
    LinkedList<E> queue;
    LinkedList<E> new_queue;
    Iterator<E> queueIterator;
    E now;
    @Override
    public boolean hasNext() {
        return iterator.hasNext() || queue.size() != 0;
    }

    @Override
    public E next() {
        queueIterator = null;
        if(new_queue.size() != 0)
            queue.addAll(new_queue);
        new_queue.clear();
        if(queue.size() != 0)
            return queue.pollFirst();
        else
            return iterator.next();
    }
    public E poll(){
        if(queue.size() != 0){
            if(queueIterator == null)
                queueIterator = queue.iterator();
            if(queueIterator.hasNext())
                return queueIterator.next();
        }
        if(iterator.hasNext()){
            now = iterator.next();
            new_queue.add(now);
            return now;
        }
        return null;
    }
    public IteratorQueue(Iterator<E> i){
        iterator = i;
        queue = new LinkedList<>();
        new_queue = new LinkedList<>();
    }
}
