package GraphPack.EdgePack;

public class Edge{

	protected int v1, v2;

	public Edge()
	{
		v1 = 0; 
		v2 = 0;
	}

	public Edge(int u1, int u2)
	{
		v1 = u1;
		v2 = u2;
	}

	public void set_vertices(int u1, int u2)
	{
		v1 = u1;
		v2 = u2;
	}

	public void get_vertices(int[] vertex_array)
	{
		vertex_array[0] = v1;
		vertex_array[1] = v2;
	}
}
