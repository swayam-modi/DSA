package LL;

class CyclicLL{
   
   ListNode head;

   CyclicLL(){
   	this.head = null;
   }

   void addNormal(int val){
      ListNode node = new ListNode(val);
      if(head == null) {
          head = node;
          return;
      }
      set(node);

   }

   void addCy(int val,int index){
   	ListNode node = new ListNode(val);
   	ListNode temp = head;

   	while(temp.next != null){
   		temp = temp.next;
   	}
   	temp.next = node;
       node.next = get(index);
   }

   ListNode get(int index){
   	ListNode temp = head;

   	while(index != 0){
   		temp =temp.next;
           index--;
   	}
   	return temp;
   }
   boolean isCyclic(){

   	ListNode first = head;
   	ListNode second = head;

       while(second.next != null && second.next.next != null){
   		first = first.next;
   		second = second.next.next;
   		if(first == second){
   			return true;
   		}
   	}

   	return false;

   }

   int giveNode(){
    ListNode first = head;
    ListNode second = head;
    int length =len();

    while(length != 0){
        first = first.next;
        length--;
    }
       while(second != first){
           second = second.next;
           first = first.next;
       }

       return first.value;
   }



   int len(){
    //fast and slow pointer method 
   	ListNode first = head;
   	ListNode second = head;

       while(second != null && second.next != null && second.next.next != null){
   		first = first.next;
   		second = second.next.next;
   		if(first == second){
   			ListNode temp = first.next;
   			int length=1;
   			while(temp != first){
   				temp = temp.next;
   				length++;
   			}
   			return length;
   		}
   	}

   	return 0;
   }
   void set(ListNode node){
       ListNode temp = head;
       while(temp.next != null){
           temp =temp.next;
       }
       temp.next=node;

   }

   void display(ListNode node) {

       ListNode temp = head;
       int count = 0;

       while(count < 2 && temp != null){
           int val= temp.value;
           System.out.print(val + " --> ");
           temp = temp.next;

           if(temp == node) {
               count++;
           }

       }
       System.out.println(" REP");
   }


	class ListNode {
      int value;
      ListNode next;
      ListNode(int x) {
          value = x;
          next = null;
       }
    }
}
