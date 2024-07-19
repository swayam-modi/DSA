package StacksAndQueues;

class CustomStack <T> {
	protected Object[] arr;
    protected int ptr = -1;

   public CustomStack(){
    	this(10);
      }

   public CustomStack(int len){
    	this.arr = new Object[len];
    }

    public boolean push(T value){
    	if(isFull()){
    		return false;
    	}
    	arr[++ptr] = value;
      return true;
    } 

    public T pop() throws Exception{
    	if(isEmpty()){
    		throw new Exception("No elements left in stack!!");
    	}
      return (T)arr[this.ptr--];
    }

    public T peek() throws Exception {
        if(isEmpty()){
            throw new Exception("No elements left in stack!!");
        }
        return (T)arr[ptr];
    }

  protected boolean isFull(){
    return ptr == arr.length-1;
    }

  protected boolean isEmpty(){
    return ptr == -1;
  }



}