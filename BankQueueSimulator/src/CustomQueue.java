/**
 * CustomQueue.java
 *
 * A simple generic FIFO queue implemented from scratch using a singly
 * linked list of nodes. Built manually (instead of using java.util.Queue)
 * to demonstrate the underlying Data Structures concepts for the
 * Bank Queue Simulator assignment.
 *
 * Supported operations (all O(1)):
 *   enqueue(item)  - add a customer to the back of the line
 *   dequeue()      - remove and return the customer at the front
 *   peek()         - look at the front customer without removing it
 *   isEmpty()      - check whether anyone is waiting
 *   size()         - number of people currently waiting
 */
public class CustomQueue<T> {

    /** Internal linked-list node. */
    private static class Node<T> {
        T data;
        Node<T> next;
        Node(T data) { this.data = data; }
    }

    private Node<T> front;
    private Node<T> rear;
    private int size;

    public CustomQueue() {
        front = null;
        rear = null;
        size = 0;
    }

    /** Adds an item to the back of the queue. */
    public void enqueue(T item) {
        Node<T> newNode = new Node<>(item);
        if (rear == null) {
            front = newNode;
            rear = newNode;
        } else {
            rear.next = newNode;
            rear = newNode;
        }
        size++;
    }

    /** Removes and returns the item at the front of the queue. */
    public T dequeue() {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot dequeue from an empty queue.");
        }
        T data = front.data;
        front = front.next;
        if (front == null) {
            rear = null;
        }
        size--;
        return data;
    }

    /** Returns (without removing) the item at the front of the queue. */
    public T peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty.");
        }
        return front.data;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void clear() {
        front = null;
        rear = null;
        size = 0;
    }
}
