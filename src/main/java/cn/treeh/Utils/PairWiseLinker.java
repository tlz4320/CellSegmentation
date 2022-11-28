package cn.treeh.Utils;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public class PairWiseLinker<T> implements Iterable<T>{
    public LinkNode<T> root;
    public LinkNode<T> tail;
    public LinkNode<T> add(Object o){
        if(root == null)
            tail = root = new LinkNode(o);
        else{
            tail.next = new LinkNode(o);
            tail.next.former = tail;
            tail = tail.next;
        }
        return tail;
    }
    public void remove(Object o){
        if(root.object.equals(o)) {
            root = root.next;
            root.former = null;
            return;
        }
        if(tail.object.equals(o)){
            tail = tail.former;
            tail.next = null;
            return;
        }
        LinkNode<T> n = root;
        while(n != tail){
            n = n.next;
            if(n.object.equals(o)) {
                n.former.next = n.next;
                n.next.former = n.former;
                return;
            }
        }
    }
    class PairWiseIterator<T> implements Iterator<T> {
        PairWiseIterator(LinkNode<T> root){
            node = root;
        }
        LinkNode<T> node;
        @Override
        public boolean hasNext() {
            return node != null;
        }

        @Override
        public T next() {
            LinkNode<T> res = node;
            node = node.next;
            return res.getObject();
        }

        @Override
        public void remove() {

        }

    }
    @Override
    public Iterator<T> iterator() {
        return new PairWiseIterator<>(root);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        LinkNode<T> node = root;
        while(node != null){
            action.accept(node.getObject());
            node = node.next;
        }
    }

    @Override
    public Spliterator<T> spliterator() {
        return Iterable.super.spliterator();
    }
}
