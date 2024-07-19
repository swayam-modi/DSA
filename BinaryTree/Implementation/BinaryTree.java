
import java.util.Scanner;

public class BinaryTree{

	private class Node{
		int value;
		Node left;
		Node right;

        Node(){}
		
		Node(int value){
			this.value = value;
		}
	}

	private Node root;

	BinaryTree(){
		root  = new Node();
	}

	public void populate(Scanner sc){
        System.out.print("Enter root node: ");
        root.value = sc.nextInt();
        populate(root,sc);
	}

	public void populate(Node node,Scanner sc){

		System.out.print("Do you want to add left node of "+node.value+": ");
		boolean l = sc.nextBoolean();

		if(l){
			System.out.print("Enter value: ");
			Node left = new Node(sc.nextInt());
			node.left = left;
			populate(left,sc);
		}

		System.out.print("Do you want to add right node of "+node.value+": ");
		boolean r = sc.nextBoolean();

		if(r){
			System.out.print("Enter value: ");
			Node right = new Node(sc.nextInt());
			node.right = right;
			populate(right,sc);
		}
	}

	public void display(){
		prettyDisplay(root,0);
	}

	private void display (Node node,String indent){
		if(node == null){
			return;
		}
		System.out.println(indent+node.value);
		display(node.left,indent+"\t");
        display(node.right,indent+"\t");
	}

	private void prettyDisplay(Node node,int level){
		if(node == null){
			return;
		}
		prettyDisplay(node.right,level+1);
		if(level != 0){
			for(int i = 0 ; i < level -1;i++){
                System.out.print("|\t");
			}
			System.out.println("|-----> "+node.value);
		}else{
			System.out.println(node.value);
		}
		prettyDisplay(node.left,level+1);
	}

	public void preOrder(){
		preOrder(root);
	}
	
	//pre order traversals
	public void preOrder(Node node){
		if(node == null){
			return;
		}
		System.out.print(node.value+" ");
		preOrder(node.left);
		preOrder(node.right);
	}
    
    //In order traversal
	public void InOrder(){
		preOrder(root);
	}
	public void InOrder(Node node){
		if(node == null){
			return;
		}
		InOrder(node.left);
		System.out.print(node.value+" ");
		InOrder(node.right);
	}
    
    //Post order traversal
	public void postOrder(){
		preOrder(root);
	}
	public void postOrder(Node node){
		if(node == null){
			return;
		}
		postOrder(node.left);
		postOrder(node.right);

		System.out.print(node.value+" ");
	}
}