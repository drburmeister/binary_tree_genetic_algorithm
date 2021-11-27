package EA_Final;

import java.io.PrintStream;
import java.util.Random;

/*
 * Author: Dennis Burmeister
 * School: Marquette University
 * Class: COEN 4870 - Evolutionary Algorithms
 * Description: EA with binary tree representation of equations to find the
 * largest output with a given set of n doubles with n+1 operators (+, -, *, /, %)
*/
public class main_EA_Final {

	//Note: "leaf" is substituted for "double/number" due to numbers exclusively being leaf nodes	

	//Variable for printing method
	static final int COUNT = 3;
	
	//initialize operators for easier inputting
	static final char mult = '*';
	static final char add = '+';
	static final char min = '-';
	static final char mod = '%';
	static final char div = '/';
	
	//Accessible ints for the sizes of the operators and the variables
	static int leafSize = 0;
	static int opSize = 0;
	
	//Used to exit out of the swapX methods to prevent extra instructions
	static boolean out = false;
	//Also in swapX methods to keep global track of the current node index
	static int index = 0;
	//In swapLeafs method to keep global track of values at node indexes
	static double val1 = 0;
	static double val2 = 0;
	//In swapOps method to keep global track of operators at node indexes
	static char op1 = '0';
	static char op2 = '0';
	
	//Node Structure For creating trees
	static class Node {
		private char op; //operator
		private double data; //double
		private double fitness; 
		private Node left, right; //left and right children
		public Node(char op, double data, double fitness, Node left, Node right) {
			super();
			this.op = op;
			this.data = data;
			this.fitness = fitness;
			this.left = left;
			this.right = right;
		}
	    Node copy() {
	        Node left = null;
	        Node right = null;
	        if (this.left != null) {
	            left = this.left.copy();
	        }
	        if (this.right != null) {
	            right = this.right.copy();
	        }
	        return new Node(op, data, fitness, left, right);
	    }
	}
	public static Node[] copyTree(Node[] og) {
		Node[] copy = new Node[og.length];
		for (int i = 0; i < og.length; i++) {
			copy[i] = og[i].copy();
		}
		return copy;
	}
	//Main Method
	public static void main(String[] args) {	
		int gen = 0; //generation count
		
		//EA Parameters
		int pMutation = 100; //"normal" mutation rate
		int pHyper = 20; //"drastic" mutation chance
		int GenLimit = 1000; //number of generations EA will go through
		int PopSize = 100; //Population size
		
		//initializing equation
		double[] leaf_inputs = {1,2,3,4,5,6,7,8,9};
		char[] op_inputs = {add, mult, min, min, add, mult, div, mod};
		leafSize = leaf_inputs.length;
		opSize = op_inputs.length;
		
		//initialize Tree arrays
		Node[] population = new Node[PopSize];
		Node[] lambda = new Node[PopSize];
		
		//Randomly initialize population
		population = pop_init(PopSize, leaf_inputs, op_inputs);
		//sort by fitness
		sort_pop(population);
		//print initial best fitness
		System.out.println(population[0].fitness);
		
		//Note: no parent selection/crossover because in this representation there is little to no pressure added from its inclusion
		
		//start evolution
		while (gen < GenLimit) {
			lambda = copyTree(population); //create copy of population
			mutate(lambda, pMutation, pHyper); //mutate the copy
			population = survivorSelection(population, lambda); //keep the best in the pop (mu+lambda)
			//printTreeArray(population);
			//System.out.println(population[0].fitness);
			gen++; //increment generation counter
		}
		print2D(population[0]); //print the best tree
		System.out.println("\n\n\n"); //format output
		System.out.println(population[0].fitness); //print the fitness of the best tree
	}
	static void sort_pop(Node[] sorted) {
		for (int i = 0; i < sorted.length; i++) {
			sorted[i].fitness = calcTree(sorted[i]);
		}
		int n = sorted.length;
		
		for (int i = 0; i < sorted.length; i++){  
			for (int j = i + 1; j < sorted.length; j++)   
			{  
				Node tmp = new Node('0', 0, 0, null,null);  
				if (sorted[i].fitness < sorted[j].fitness){  
					tmp = sorted[i].copy();  
					sorted[i] = sorted[j].copy();  
					sorted[j] = tmp;  
				}  
			}
		}
	}
	//initialize population method
	static Node[] pop_init(int size, double[] l, char[] o) {
		Node[] pop = new Node[size];
		double[] leaf = l.clone();
		char[] op = o.clone();
		
		for (int i = 0; i < size; i++) {
			shuffleDouble(leaf);
			shuffleChar(op);
			pop[i] = randomTree(leaf, op).copy();
			pop[i].fitness = calcTree(pop[i]);
		}		
		return pop;
	}
	//method to generate a random tree with given inputs
	
	static Node randomTree(double[] leafs, char[] ops) {

		//initialize tree and its children so there's no NullPointer's
		Node tree = new Node('0', 0, 0, null, null);
		tree.left = new Node('0', 0, 0, null, null);
		tree.right = new Node('0', 0, 0, null, null);
		
		//There must be 1 more double than there are operators
		if(ops.length + 1 != leafs.length) {
			System.out.println("invalid inputs");
			return tree;
		}
		//sanity check if statement
		else if(ops != null) {
			//always set current node looked at to the first operator
			tree.op = ops[0];
			//if there is only 1 operator in array its children must be leaf nodes/integers
			if(ops.length == 1) {
				tree.left.data = leafs[0];
				tree.right.data = leafs[1];
				return tree; 
			}
			else {
				//To generate a random tree we first generate a random length for the left child
				Random rand = new Random();
				int left_h = rand.nextInt(ops.length-1); //length - 1 because the first node is already set as operator
				
				//if the length randomizes to zero it will be a leaf/integer node
				if (left_h == 0) { 
					tree.left.data = leafs[0];
				}
				else {
					//create new arrays for left subtree to hold variables for recursion (no duplicates)
					char[] op_left = new char[left_h];
					for (int i = 1; i < left_h + 1; i++) {
						op_left[i - 1] = ops[i];
					}
					double[] leaf_left = new double[left_h+1];
					for(int i = 0; i < leaf_left.length; i++) {
						leaf_left[i] = leafs[i];
					}
					//recurse until all subtrees are built
					tree.left = randomTree(leaf_left, op_left);
				}
				
				//The total height of all branches should = the number of operators
				int right_h = (ops.length-1) - left_h;
				//if the length randomizes to zero it will be a leaf/integer node
				if (right_h == 0) {
					tree.right.data = leafs[0];
				}
				//create new arrays for right subtree to hold variables for recursion (no duplicates)
				else {
					char[] op_right = new char[right_h];
					for (int i = left_h + 1; i < ops.length; i++) {
						op_right[i - (left_h+1)] = ops[i];
					}
					double[] leaf_right = new double[right_h+1];
					for(int i = left_h+1; i < leafs.length; i++) {
						leaf_right[i - (left_h+1)] = leafs[i];
					}
					//recurse until all subtrees are built
					tree.right = randomTree(leaf_right, op_right).copy();
				}
			}
		}
		//once all leafs are placed in (sub)tree this return statement will be reached
		return tree;
	}

    //Method to get "the fitness" of a tree
	public static double calcTree(Node tree) {
		//init variables
		double fitness = 0;
		double left = 0; 
		double right = 0;
		
		//checks if left node contains an operator
		if (tree.left.op != '0') {
			left = calcTree(tree.left); //if it does recursively call the node to calculate the sub tree
		} else { left = tree.left.data; } //if it doesn't set the value of the left node 
		
		//checks if right node contains an operator
		if (tree.right.op != '0') {
			right = calcTree(tree.right); //if it does recursively call the node to calculate the sub tree
		} else { right = tree.right.data; } //if it doesn't set the value of the right node 
		
		fitness = SimpleFunction(left, right, tree.op);	//call to method to get overall fitness
		return fitness; //return fitness
	}	

	//Method to calculate the functions that happen throughout the trees
	public static double SimpleFunction(double x, double y, char op) {
		if (x == 0) x = 1;
		if (y == 0) y = 1;
		double result = 0;
		if (op == '*') {
			result = x*y;
		}
		else if (op == '+') {
			result = x+y;
		}
		else if (op == '-') {
			result = x-y;
		}
		else if (op == '/') {
			result = x/y;
		}
		else if (op == '%') {
			result = x%y;
		} else { System.out.println("Invalid Operator"); }
		return result;
	}
	public static boolean isOperator(char op) {
		boolean check = false;
		if (op == '*') check = true;
		else if (op == '+') check = true;
		else if (op == '-') check = true;
		else if (op == '/') check = true;
		else if (op == '%') check = true;
		return check;
	}
	
	public static void mutate(Node[] children, int pm, int hyper) {
		int n = children.length;
		for (int i = 0; i < n; i++) {
			operatorMutate(children[i], pm, hyper);
			variableMutate(children[i], pm);
		}
		sort_pop(children);
	}
	//method for swapping two operators in a tree
	public static void swapLeafs(Node tree, int i, int index1, int index2) {
		//i = index;
		if(index1 > index2) {
			int hold = index1;
			index1 = index2;
			index2 = hold;
		}
		
		if(isOperator(tree.left.op) && !getOut()) {
			swapLeafs(tree.left, index, index1, index2);
		}
		else if (!getOut()) {
			index++;
			if (index == index1) {
				val1 = tree.left.data;
				if(val2 != 0) {
					tree.left.data = val2;					index = 0;
					resetVals();					out = true;
					return;
				}
			} else if (index == index2) {
				val2 = tree.left.data;
				tree.left.data = val1;
				index = 0;
				out = true;
				return;
			} 
			
		}
		
		if(isOperator(tree.right.op) && !getOut()) {
			swapLeafs(tree.right, index, index1, index2);
		}
		else if (!getOut()) {
			index++;	
			if (index == index1) {
				val1 = tree.right.data;
				if(val2 != 0) {
					tree.right.data = val2;
					index = 0;
					resetVals();
					out = true;
					return;
				}
			} else if (index == index2) {
				val2 = tree.right.data;
				tree.right.data = val1;
				index = 0;
				out = true;
				return;
			} 
		}
	}
	
	public static void swapOps(Node tree, int i, int index1, int index2) {
		i = index;
		if(index1 > index2) {
			int hold = index1;
			index1 = index2;
			index2 = hold;
		}
		
		if (i == index1) {
			setOp1(tree.op);
			if (op2 != '0') {
				tree.op = op2;
				index = 0;
				resetOps();
				out = true;
				return;
			}
		}
		else if (i == index2) {
			setOp2(tree.op);
			tree.op = op1;
			index = 0;
			out = true;
			return;
		}
		
		if(isOperator(tree.left.op) && !getOut()) {
			index++;
			swapOps(tree.left, index, index1, index2);
		}
		
		if(isOperator(tree.right.op) && !getOut()) {
			index++;
			swapOps(tree.right, index, index1, index2);
		}
	}
		
	public static void operatorMutate(Node tree, int rate, int big_rate) {
		Random r = new Random();
		int index1 = -100;
		int index2 = -100;
		int p = r.nextInt(100);
		
		if(rate < big_rate) {
			System.out.println("invalid mutation probabilities");
		}
		if  (p < big_rate) {
			index1 = 0;
			index2 = r.nextInt(opSize - 1) + 1; //random index between 1 and length - 1
		}
		else if (p >= big_rate && p < rate) {
			index1 = r.nextInt(opSize); //random index between 1 and length - 1
			index2 = r.nextInt(opSize); //random index between 1 and length - 1
			//so index1 and index2 can't be duplicates
			while (index2 == index1) {
				index2 = r.nextInt(opSize); 
			}
		}
		//if mutation occurs swap designated operators
		if (index1 >= 0 && index2 >= 0) {
			swapOps(tree, 0, index1, index2);
			out = false;
			swapOps(tree, 0, index1, index2);
			out = false;
		}
	}
	
	public static void variableMutate(Node tree, int rate) {
		Random r = new Random();
		int index1 = -100;
		int index2 = -100;
		int p = r.nextInt(99);
		
		if (p >= 0 && p < rate) {
			index1 = r.nextInt(leafSize - 1) + 1; //random index between 1 and length - 1
			index2 = r.nextInt(leafSize - 1) + 1; //random index between 1 and length - 1
			//so index1 and index2 can't be duplicates
			while (index2 == index1) {
				index2 = r.nextInt(leafSize - 2) + 1; 
			}
			
		}
		//if mutation occurs swap designated operators
		if (index1 >= 0 && index2 >= 0) {
			swapLeafs(tree, 0, index1, index2);
			out = false;
			swapLeafs(tree, 0, index1, index2);
			out = false;
		}
	}
	
	public static Node[] survivorSelection(Node[] Mu, Node[] Lambda) {
		Node[] Survivors = new Node[Mu.length];
		
		Node[] MuPlusLambda = new Node[Mu.length + Lambda.length];
		for (int i = 0; i < Mu.length; i++) {
			MuPlusLambda[i] = Mu[i].copy();
			MuPlusLambda[i+Mu.length] = Lambda[i].copy();
		}
		sort_pop(MuPlusLambda);

		for (int k = 0; k < Mu.length; k++) {
			Survivors[k] = MuPlusLambda[k].copy();			
		}

		return Survivors;
	}
	
	//Finds max depth of tree (Not Used)
	//Source: https://www.educative.io/edpresso/finding-the-maximum-depth-of-a-binary-tree
	public static int maxDepth(Node root) { 
		   // Root being null means tree doesn't exist.
		   if (root == null) 
		     return 0; 
		    // Get the depth of the left and right subtree 
		// using recursion.
		   int leftDepth = maxDepth(root.left); 
		   int rightDepth = maxDepth(root.right); 
		
		   // Choose the larger one and add the root to it.
		   if (leftDepth > rightDepth) 
			   return (leftDepth + 1); 
		   else 
			   return (rightDepth + 1); 
		  } 
	/* 
	 *  The two methods below are slightly modified versions of methods gathered from   *
	 *  https://www.baeldung.com/java-print-binary-tree-diagram which are used for      *
	 *  printing out trees.																*
	 */
	static void print2DUtil(Node root, int space)
	{
	    // Base case
	    if (root == null)
	        return;
	 
	    // Increase distance between levels
	    space += COUNT;
	 
	    // Process right child first
	    print2DUtil(root.right, space);
	 
	    // Print current node after space
	    // count
	    System.out.print("\n");
	    for (int i = COUNT; i < space; i++)
	        System.out.print(" ");
	    if(root.op != '0') System.out.print(root.op + " ⟞ ");
	    else System.out.print((int)root.data);
	 
	    // Process left child
	    print2DUtil(root.left, space);
	} 
	// Wrapper over print2DUtil()
	static void print2D(Node root)
	{
	    // Pass initial space count as 0
	    print2DUtil(root, 0);
	}
	
	//Method to round down decimals for nicer printing: Source = https://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}
	//randomly shuffling a double (leaf) array
	static void shuffleDouble(double[] arr)
	{
		Random rnd = new Random();
    	for (int i = arr.length - 1; i > 0; i--)
		{
	      int index = rnd.nextInt(i + 1);
	      double a = arr[index];
	      arr[index] = arr[i];
	      arr[i] = a;
		}
	}
	
	//randomly shuffling an char (operator) array
	static void shuffleChar(char[] array) {
		Random rnd = new Random();
    	for (int i = array.length - 1; i > 0; i--)
		{
	      int index = rnd.nextInt(i + 1);
	      char a = array[index];
	      array[index] = array[i];
	      array[i] = a;
		}
	}
	//print and array of trees (used for testing)
	
	static void printTreeArray(Node[] tree) {
		for (int i = 0; i < tree.length; i++) {
			System.out.println(tree[i].fitness);
		} System.out.println();
	}
	
	//getters, setters, and resetters
	public static void resetVals() {
		val1 = 0; 
		val2 = 0;
	}
	public static boolean getOut() { return out; }
	public static void setOp1(char set) { op1 = set; }
	public static void setOp2(char set) { op2 = set; }
	public static void resetOps() {
		op1 = '0'; 
		op2 = '0';
	}
	public static void ReInit() {
		resetOps();
		resetVals();
		out = false;
		index = 0;
	}
}
