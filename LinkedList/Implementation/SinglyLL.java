package LL;

public class SinglyLL {
    private Node head;
    private Node tail;
    private int size;
    
    SinglyLL(){
        this.size=0;
    }

    public void add(int val){
        addLast(val);
    }

    public void add(int index,int val){
        if(index==0){
            addFirst(val);
            return;
        }if(index==size){
            addLast(val);
            return;
        }
        //important -----------
        Node temp = head;
        for(int i=1;i<index;i++){
            temp =temp.next;
        }
        Node node = new Node(val);
        node.next= temp.next;
        temp.next =node;
        size+=1;
    }
    public void addhelper(int val,int index){
        addRec(val,index,head);
    }

    private void addRec(int val,int index,Node temp){
        if(index == 1){
            Node node = new Node(val);
            node.next = temp.next;
            temp.next = node;
            size++;
            return;
        }
        addRec(val,index-1,temp.next);
    }

    public void remove (int index){
        if(index==0){
           removeFirst();
           return;
        }if(index==size-1){
            removeLast();
            return;
        }
        Node temp =head;
        for(int i=0;i<index-1;i++){
            temp= temp.next;
        }
        //Important ------------
        temp.next= temp.next.next;
        size--;

    }

    public Node find(int val){
    Node temp = head;
    while(temp != null){
        
        if(temp.value== val){
            return temp;
        }
        temp = temp.next;
    }
    return null;
    }

    public int get(int index) throws NullPointerException{
        if(index>=size){
            throw new NullPointerException();
        }

        if(index==0){
            return head.value;
        }
        if(index==size-1){
            return tail.value;
        }
        Node temp = head.next;
        for(int i=1;i<index;i++){
            temp=temp.next;
        }
        return temp.value;
    }
    public void display (){
        Node temp =head;
        while(temp != null){
            System.out.print(temp.value+" -> ");
            temp=temp.next;
        }
        System.out.println("Null");
    }
    public int length(){
        return this.size;
    }

    private void addFirst(int val){
        Node node = new Node(val);
        node.next=head;
        head=node;

        if(tail==null){
            tail=node;
        }
        size+=1;
    }
    private void addLast(int val){
        Node node = new Node(val);
        if(tail==null){
            addFirst(val);
            return;
        }
        tail.next = node;
        tail =node;
        size+=1;
    }

    private void removeFirst(){
        head = head.next;
        size--;
    }
    private void removeLast(){
        Node temp = head;
        for(int i=0;i<this.size-2;i++){
            temp= temp.next;
        }
        temp.next=null;
        tail=temp;
        size--;

    }

    private class Node{
        private int value;
        private Node next;

        Node(int val){
            this.value=val;
        }
    }

}
