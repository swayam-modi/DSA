public class SegmentTree{
	private class TreeNode{
		int data;
		int startInterval;
		int endInterval;
		TreeNode left;
		TreeNode right;

		TreeNode(int start,int end){

			this.startInterval = start;
			this.endInterval = end;
		}
	}
	private TreeNode root;

	public SegmentTree(int[] arr){

        this.root = createSegment(arr,0,arr.length-1);
	}

	private TreeNode createSegment(int[] arr,int s,int e){

		if(s == e){
			TreeNode leaf = new TreeNode(s,e);
            leaf.data = arr[s];

            return leaf;
		}

		TreeNode node = new TreeNode(s,e);
		int m = s + (e-s)/2;

		node.left = createSegment(arr,s,m);
		node.right = createSegment(arr,m+1,e);

		node.data = node.left.data + node.right.data;

		return node;
	}
	
    //find the perticular range sum
	public int findQuery(int s,int e){
		return findQuery(root,s,e);
	}

	private int findQuery(TreeNode node,int s,int e){

		if(node.startInterval >= s && node.endInterval <= e){
            return node.data;
		}

		if(node.startInterval > e || node.endInterval < s){
			return 0;
		}

		return findQuery(node.left,s,e) + findQuery(node.right,s,e);
	}

    //update query function
	public void update(int index,int val){
		   update(root,index,val);
	}

	private void update (TreeNode node,int index,int val){
		if(node.startInterval == index && node.endInterval == index){
			node.data = val;
			return;
		}

		if(node.startInterval > index || node.endInterval < index){
			return;
		}

		update(node.left,index,val);
		update(node.right,index,val);

		node.data = node.left.data + node.right.data;
	} 

    //Display function
	public void display(){
		display(this.root,0);
	}
	private void display(TreeNode node,int index){
		if(node == null){
			return;
		}

		display(node.right,index+1);

		if(index > 0){
			for(int i = 0;i<index-1;i++){
				System.out.print("|\t");
			}
			System.out.println("|---->("+node.startInterval+" ,"+node.endInterval+")["+node.data+"]");
		}else{
			System.out.println("("+node.startInterval+" ,"+node.endInterval+")["+node.data+"]");
		}
		display(node.left,index+1);
	}
}