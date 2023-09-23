package GraphPack.WeightedVertexPack.VertexPack;

import java.util.*;

public class Vertex{
	
	protected TreeSet<Integer> neighbors;
	protected TreeSet<Integer> adj_edges;
	protected int degree;


	public Vertex()
	{
		neighbors = new TreeSet<Integer>();
		adj_edges = new TreeSet<Integer>();
		degree = 0;
	}

	public Vertex(TreeSet<Integer> nbs, TreeSet<Integer> adj_edgs, int d)
	{
		neighbors = nbs;
		adj_edges = adj_edgs;
		degree = d;
	}

	public void set_neighbors(TreeSet<Integer> nbs)
	{
		neighbors = nbs;
	}

	public void set_adj_edges(TreeSet<Integer> adj_edgs)
	{
		adj_edges = adj_edgs;
	}
/*
	public void allocate_neighborhood_space(int dgr)
	{
		neighbors = new int[dgr];
		adj_edges = new int[dgr];
	}
*/
	public void add_neighbor(int name)
	{
		neighbors.add(name);
	}

	public void add_adj_edge(int name)
	{
		adj_edges.add(name);
	}

	public TreeSet<Integer> get_neighbors()
	{
		return neighbors;
	}

	public TreeSet<Integer> get_adj_edges()
	{
		return adj_edges;
	}

	public void set_degree(int d)
	{
		degree = d;
	}

	public int get_degree()
	{
		return degree;
	}

}
