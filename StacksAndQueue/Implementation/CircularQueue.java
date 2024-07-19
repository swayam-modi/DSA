package StacksAndQueues;

public class CircularQueue {
   protected int[] data;
   protected int front = 0;
   protected int end = 0;
   protected int size = 0;

    CircularQueue(){
    	this(10);
    }

    CircularQueue(int l){
    	data = new int[l];
    }

    public boolean insert(int value){
    	if(isFull()){
    		return false;
    	}
    	this.data[end++] = value;
    	end %= this.data.length;
    	size++;
        return true;
    }

    public int remove() throws Exception{
    	if(isEmpty()){
    		throw new Exception("queue is Empty!!");
    	}
   
         int removed = this.data[front++];
         front %= this.data.length;
         size--;

         return removed;
     }
     public int front() throws Exception{
         if(isEmpty()){
             throw new Exception("queue is Empty!!");
         }
         return this.data[front];
     }


     public void display(){
     	int i = front;
         do{
             System.out.print(data[i] +" <- ");
             i++;
             i %=data.length;
         }while (i != end);
         System.out.print("End");
     }

    protected boolean isFull(){
    	return size == this.data.length;
    }

    protected boolean isEmpty(){
    	return size == 0;
    }


    

}
