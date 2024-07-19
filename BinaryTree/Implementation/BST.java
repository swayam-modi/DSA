public class BST{
	private class Node{
		int value;
		int height;
		Node left;
		Node right;

		Node(int value){
			this.value = value;
		}
	}

	public Node root;
    
    //height function
	public int height(Node node){
		if(node == null){
			return -1;
		}
		return node.height;
	}

	//insert array element function
	public void insertArray(int[] arr){
		for(int e: arr){
			insert(e);
		}
	}

	//insert sorted array function
	public void insertSorted(int[] arr){
		sortedHelper(arr,0,arr.length-1);
	}
	private void sortedHelper(int[] arr,int s,int e){
		if(s > e){
			return;
		}
		int mid = s+(e-s)/2;
		
		insert(arr[mid]);
		sortedHelper(arr,s,mid-1);
		sortedHelper(arr,mid+1,e);
	}


    //insertion function
	public void insert(int val){
		this.root = insert(root,val);
	}
	private Node insert(Node node,int val){
		if(node == null){
			node = new Node(val);
			return node;
		}

		if(val > node.value){
			node.right = insert(node.right,val); 
		}
		else{
			node.left = insert(node.left,val);
		}

		node.height = Math.max(height(node.left),height(node.right))+1;

		return node;
	}
    
    //Display function
	public void display(){
		display(root,0);
	}

	private void display(Node node,int level){
		if(node == null){
			return;
		}

		display(node.right,level+1);

		if(level > 0){
			for(int i = 0;i < level -1;i++){
				System.out.print("|\t");
			}
			System.out.println("|----> ("+node.value+")");
		}
		else{
			System.out.println("("+node.value+")");
		}

		display(node.left,level+1);
	}

	//isBalenced function
	public boolean isBalanced(){
		return isBalanced(root);
	}
	private boolean isBalanced(Node node){
		if(node == null){
           return true;
		}

		return Math.abs(height(node.left)-height(node.right)) <=1 && isBalanced(node.left) && isBalanced(node.right);
	}
}