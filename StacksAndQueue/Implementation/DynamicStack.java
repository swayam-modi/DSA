package StacksAndQueues;

public class DynamicStack <T> extends CustomStack{
    DynamicStack(){
        super();
    }
    DynamicStack(int len){
        super(len);
    }
    public boolean push(Object val){
         if(this.isFull()){
             Object[] temp = new Object[this.ptr*2];
             for(int i=0;i<this.arr.length;i++){
                 temp[i] = arr[i];
             }
             this.arr = temp;
         }
         arr[++this.ptr] = val;
         return true;
    }

}
