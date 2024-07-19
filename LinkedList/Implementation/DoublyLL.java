package LL;
public class DoublyLL{

    private Node head;

    DoublyLL(){
        this.head = null;
    }

    public void add(int index,int val){
        if(index==0){
            addFirst(val);
        }
    }
    public void addAt(int val,int v){
        Node temp = head;

        while(temp != null){
            if(temp.value == val){
                break;
            }
            temp = temp.next;
        }
        if(temp == null){
            System.out.println("Value not Found");
            return;
        }
        Node node = new Node(v);

        node.next = temp.next;
        temp.next = node;
        node.pre = temp;
        if(temp.next != null){
            temp.next.pre = node;
        }

    }

    public void display (){
        Node temp = head;

        System.out.print("Null <- ");
        while(temp != null){
            System.out.print(temp.value +" <=> ");
            temp = temp.next;
        }
        System.out.println("Null");
    }

    private void addFirst(int val){
        Node node =new Node(val);
        node.next = head;
        node.pre =null;

        if(head != null){
            head.pre = node;
        }
        head = node;
    }
    public void addLast(int val){
        Node node = new Node(val);

        if(head == null){
            addFirst(val);
            return;
        }

        Node temp = head;
        while(temp.next != null){
            temp = temp.next;
        }

        node.next = null;
        node.pre = temp;
        temp.next= node;

    }














    private class Node{

        private int value;
        private Node next;
        private Node pre;

        Node(int val){
            this.value=val;
        }
    }


}