public class AVL {
    private class Node{
        int value;
        int height;
        Node left;
        Node right;

        Node(int value){
            this.value = value;
        }
    }

    private Node root;

    public int height(){
        return height(this.root);
    }
    private int height(Node node){
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

        return reOrder(node);
    }

    private Node reOrder(Node node){

        if(height(node.left) - height(node.right) > 1){
            //left weighted tree

            if(height(node.left.left) - height(node.left.right) > 0){
                //left-left case
                return rightRotate(node);
            }
            if(height(node.left.left) - height(node.left.right) < 0){
                //left-right case
                node.left = leftRotate(node);
                return rightRotate(node);
            }
        }

        if(height(node.left) - height(node.right) < -1){
            //right weighted tree
            if(height(node.right.left) - height(node.right.right) < 0){
                //right-right case
                return leftRotate(node);
            }
            if(height(node.right.left) - height(node.right.right) > 0){
                //right-left case
                node.right = rightRotate(node);
                return leftRotate(node);
            }
        }

        return node;
    }

    public Node leftRotate(Node p) {
        //p = parent
        //c = child
        //gc = grand child

        Node c = p.right;
        Node gc = c.left;

        c.left = p;
        p.right = gc;

        c.height = Math.max(height(c.left)+1, height(c.right));
        p.height = Math.max(height(p.left), height(p.right));


        return c;
    }

    private Node rightRotate(Node p){
        //p = parent
        //c = child
        //gc = grand child

        Node c = p.left;
        Node gc = c.right;

        c.right = p;
        p.left = gc;

        c.height = Math.max(height(c.left),height(c.right)+1);
        p.height = Math.max(height(p.left),height(p.right));

        return c;
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
