package pca;

import java.util.ArrayList;

public class Combination
{
	/**
	 * Get the combination list. i.e., select k elements from n element. What are the combination.
	 * 
	 * @param n The total elements.
	 * @param k Select how many element form the total elements.
	 * @return All the combination.
	 */
	public static ArrayList<ArrayList<Integer>> getCombination(int n, int k)
	{
		ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();

		if (n <= 0 || n < k)
			return result;

		ArrayList<Integer> item = new ArrayList<Integer>();
		dfs(n, k, 1, item, result); // because it need to begin from 1

		return result;
	}

	private static void dfs(int n, int k, int start, ArrayList<Integer> item, ArrayList<ArrayList<Integer>> res)
	{
		if (item.size() == k)
		{
			res.add(new ArrayList<Integer>(item));
			return;
		}

		for (int i = start; i <= n; i++)
		{
			item.add(i);
			dfs(n, k, i + 1, item, res);
			item.remove(item.size() - 1);
		}
	}

	public static void main(String[] args)
	{
		ArrayList<ArrayList<Integer>> list = Combination.getCombination(4,3);
		
		System.out.println("list: " + list);
		
	}

}
