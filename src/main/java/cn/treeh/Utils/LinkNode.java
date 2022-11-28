package cn.treeh.Utils;

public class LinkNode<T> {
    public T getObject() {
        return object;
    }

    T object;
    public LinkNode<T> former, next;
    public LinkNode(T o){
        object = o;
    }
}
