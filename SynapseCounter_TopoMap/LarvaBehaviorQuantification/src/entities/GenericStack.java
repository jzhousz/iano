package entities;

import java.util.*;

/**
* The stack.
* 
*/
public class GenericStack <T> 
{
	private ArrayList<T> stack = new ArrayList<T> ();
	private int top = 0;

	/**
	* Get the size.
	* 
	* @return The size.
	*/
	public int size () { return top; }
	
	/**
	* Check whether the static is empty.
	* 
	* @return true if it's empty.
	*/
	public Boolean isEmpty() 
	{ 
		if( top == 0 )
			return true;
		else
			return false;
	}

	/**
	* Push an element.
	* 
	* @param item An item being pushed.
	* @return None.
	*/
	public void push (T item) {
		stack.add (top++, item);
	}

	/**
	* Pop an item.
	* 
	* @return An item.
	*/
	public T pop () {
		return stack.remove (--top);
	}

	public static void main (String[] args) {
		GenericStack<Integer> s = new GenericStack<Integer> ();
		s.push (17);
		int i = s.pop ();
		System.out.format ("%4d%n", i);
	}

}