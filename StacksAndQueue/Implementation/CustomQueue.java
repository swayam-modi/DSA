package StacksAndQueues;

public class CustomQueue <T> {
   private Object[] arr;
   private int end = -1;


   CustomQueue(){
      this(10);
   }
   CustomQueue(int len){
      arr = new Object[len];
   }
   public boolean insert(T value){
      if(isFull()){
         return false;
      }
      arr[++this.end] = value;
      return true;
   }
   public T remove() throws Exception{
      if(isEmpty()){
         throw new Exception("Already empty");
      }

      T removed =(T)arr[0];

      for(int i = 1; i<arr.length; i++){
         arr[i-1] = arr[i];
      }
      end--;

      return removed;
   }

   public T front() throws Exception{
      if(isEmpty()){
         throw new Exception("Already empty");
      }
      return (T)arr[0];
   }

   public void display(){
      for(int i = 0; i<=end; i++){
         System.out.print((T)arr[i]+" <- ");
      }


       System.out.print("End");

   }

   public boolean isFull(){
      return end == arr.length-1;
   }

   public boolean isEmpty(){
      return end == -1;
   }
}
