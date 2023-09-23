package DegreeBucketPack;

import java.io.*;
import java.util.*;
import java.lang.StrictMath;

import UsrPausePack.UsrPause;
import GraphPack.WeightedVertexPack.VertexPack.Vertex;

public class DegreeBasedPartition{
	private ArrayList<Integer> v_ascending_degree_array;
	private ArrayList<Integer> index_in_v_ascending_degree_array;
	private ArrayList<Integer> ptr_to_in_vertices; // in vertices on the left
	private ArrayList<Integer> ptr_to_out_vertices; // out vertices on the right
	private int dynamic_max_degree;

	public DegreeBasedPartition(int max_degree, int vertex_num, HashSet<Integer> remaining_vertex_set, Vertex[] vertices)
	{
		dynamic_max_degree = max_degree;
		v_ascending_degree_array = new ArrayList<Integer>(vertex_num + 2);
		index_in_v_ascending_degree_array = new ArrayList<Integer>(vertex_num + 2);
		ptr_to_in_vertices = new ArrayList<Integer>(dynamic_max_degree + 1 + 2);
		ptr_to_out_vertices = new ArrayList<Integer>(dynamic_max_degree + 1 + 2);
		ArrayList<Integer> v_degree_num = new ArrayList<Integer>(dynamic_max_degree + 1 + 2);
		
		// for placing vertices
		ArrayList<Integer> qtr_to_v_degree = new ArrayList<Integer>(dynamic_max_degree + 1 + 2);

		for(int i = 0; i <= dynamic_max_degree + 1; i++) // initialize enough locations to avoid NullPointException
		{
			v_degree_num.add(0);
		}
		
		// make statistics
		for(int v: remaining_vertex_set)
		{
			int d = vertices[v].get_degree();
			v_degree_num.set(d, v_degree_num.get(d) + 1);
		}

		ptr_to_in_vertices.add(0);
		qtr_to_v_degree.add(0);
		
		// partition
		// same degree partitions, left: in partitions; right: out partitions
		for(int d = 1; d <= dynamic_max_degree + 1; d++)// initialize enough locations to avoid NullPointException
		{
			ptr_to_in_vertices.add(ptr_to_in_vertices.get(d-1) + v_degree_num.get(d-1)); // 
			ptr_to_out_vertices.add(ptr_to_in_vertices.get(d)); // when partitions are dynamically adjusted, this is the law, yet initially, we have other ways
			qtr_to_v_degree.add(ptr_to_in_vertices.get(d));
		}
		ptr_to_out_vertices.add(dynamic_max_degree + 1); // mark the end of the partition, this index will be fixed which differs from other indices

		// placement
		for(int i = 0; i <= vertex_num + 1; i++) // initialize enough locations to avoid NullPointException, this is a bijection
		{
			v_ascending_degree_array.add(0);
			index_in_v_ascending_degree_array.add(0);
		}
		for(int v : remaining_vertex_set)
		{
			int d = vertices[v].get_degree();
			// place v in the partition with degree d
			// maintain the bijection
			v_ascending_degree_array.set(qtr_to_v_degree.get(d), v);
			index_in_v_ascending_degree_array.set(v, qtr_to_v_degree.get(d));
			// move the pointer
			qtr_to_v_degree.set(d, qtr_to_v_degree.get(d) + 1);	
		}
	} // public DegreeBasedPartition(int max_degree, int vertex_num, ArrayList<Integer> remaining_vertex_set, Vertex[] vertices)


	public int get_dynamic_max_degree()
	{
		return dynamic_max_degree;
	}

	public void place_out_vertex_from_graph(int v, Vertex[] vertices)
	{

		int v_ptr = index_in_v_ascending_degree_array.get(v);
		int boundary_loc = ptr_to_out_vertices.get(vertices[v].get_degree()) - 1;
		int boundary_v = v_ascending_degree_array.get(boundary_loc);
		//
		v_ascending_degree_array.set(v_ptr, boundary_v);// already had a copy, don't need a tmp vertex
		index_in_v_ascending_degree_array.set(boundary_v, v_ptr);
		//
		v_ascending_degree_array.set(boundary_loc, v);
		index_in_v_ascending_degree_array.set(v, boundary_loc);
		//
		ptr_to_out_vertices.set(vertices[v].get_degree(), ptr_to_out_vertices.get(vertices[v].get_degree()) - 1);

		while(ptr_to_in_vertices.get(dynamic_max_degree).intValue() == ptr_to_out_vertices.get(dynamic_max_degree).intValue())
			dynamic_max_degree--;
	} // public void place_out_vertex_from_graph(int v, Vertex[] vertices)


	public void degree_dec_by_one(int v, Vertex[] vertices)
	{
		int v_ptr = index_in_v_ascending_degree_array.get(v);
		int boundary_loc_1 = ptr_to_in_vertices.get(vertices[v].get_degree());
		int boundary_v_1 = v_ascending_degree_array.get(boundary_loc_1);
		int boundary_loc_2 = ptr_to_out_vertices.get(vertices[v].get_degree() - 1);
		int boundary_v_2 = v_ascending_degree_array.get(boundary_loc_2);
		//
		v_ascending_degree_array.set(v_ptr, boundary_v_1);
		v_ascending_degree_array.set(boundary_loc_1, boundary_v_2);
		v_ascending_degree_array.set(boundary_loc_2, v);
		//
		index_in_v_ascending_degree_array.set(boundary_v_2, boundary_loc_1);
		index_in_v_ascending_degree_array.set(boundary_v_1, v_ptr);
		index_in_v_ascending_degree_array.set(v, boundary_loc_2);
		//
		ptr_to_in_vertices.set(vertices[v].get_degree(), ptr_to_in_vertices.get(vertices[v].get_degree()) + 1);
		ptr_to_out_vertices.set(vertices[v].get_degree() - 1, ptr_to_out_vertices.get(vertices[v].get_degree() - 1) + 1);
		//
		while(ptr_to_in_vertices.get(dynamic_max_degree).intValue() == ptr_to_out_vertices.get(dynamic_max_degree).intValue())
		{
			dynamic_max_degree--;
		}
	} // public void degree_dec_by_one(int v, Vertex[] vertices)

	public ArrayList<Integer> get_vertex_list_of_top_degree(int k) // top k
	{
		ArrayList<Integer> ret_list = new ArrayList<Integer>();
		int min_dgr_in_loop = StrictMath.max(dynamic_max_degree - k + 1, 0);
		for(int d = dynamic_max_degree; d >= min_dgr_in_loop; d--)
		{
			for(int i = ptr_to_in_vertices.get(d); i < ptr_to_out_vertices.get(d); i++)
			{
				ret_list.add(v_ascending_degree_array.get(i));
			}
		}
		return ret_list;
	}

	public ArrayList<Integer> get_vertex_list_of_degree_at_most(int max_d)
	{
		ArrayList<Integer> ret_list = new ArrayList<Integer>();
		int max_dgr_in_loop = StrictMath.min(max_d, dynamic_max_degree);
		for(int d = 0; d <= max_dgr_in_loop; d++)
		{
			for(int i = ptr_to_in_vertices.get(d); i < ptr_to_out_vertices.get(d); i++)
			{
				ret_list.add(v_ascending_degree_array.get(i));
			}
		}
		return ret_list;
	}

	public ArrayList<Integer> get_vertices_of_maximum_degree()
	{
		return get_vertices_of_degree(dynamic_max_degree);
	}
	
	public ArrayList<Integer> get_vertices_of_degree(int dgr)
	{
		ArrayList<Integer> ret_list = new ArrayList<Integer>();
		for(int i = ptr_to_in_vertices.get(dgr); i < ptr_to_out_vertices.get(dgr); i++)
		{
			ret_list.add(v_ascending_degree_array.get(i));
		}
		return ret_list;
	}

	public boolean check_partitions(int vertex_num, Vertex[] vertices, HashSet<Integer> remaining_vertex_set, ArrayList<Integer> removed_vertex_list)
	{
		for(int i = 0; i < remaining_vertex_set.size(); i++)
		{
			if(index_in_v_ascending_degree_array.get(v_ascending_degree_array.get(i)) != i)
			{
				System.out.println("Location " + i + " in the array contains " + v_ascending_degree_array.get(i));
				System.out.println("this vertex is actually in " + index_in_v_ascending_degree_array.get(v_ascending_degree_array.get(i)));
				System.out.println("bijection error occurs");
				return false;
			}
		}
		for(int d = 0; d <= dynamic_max_degree; d++)
		{
			for(int i = ptr_to_in_vertices.get(d); i < ptr_to_out_vertices.get(d); i++)
			{
				int v = v_ascending_degree_array.get(i);
				if(!remaining_vertex_set.contains(v))
				{
					System.out.println(v + " should be in the remaining_vertex_set according to partitions, conflicts between partitions and dynamic vertex sets");
					return false;
				}
				if(removed_vertex_list.contains(v))
				{
					System.out.println(v + " should not be in the removed_vertex_list according to partitions, conflicts between partitions and dynamic vertex sets");
					return false;
				}
				if(vertices[v].get_degree() != d)
				{
					System.out.println("according to get_degree(), " + v + "\'s degree is " + vertices[v].get_degree());
					System.out.println("according to partitions, " + v + "\'s degree is " + d);
					System.out.println("for vertex " + v + ", degree conflicts between partitions and dynamic vertices");
					return false;
				}
			}
			for(int i = ptr_to_out_vertices.get(d); i < ptr_to_in_vertices.get(d+1); i++)
			{
				int v = v_ascending_degree_array.get(i);
				if(remaining_vertex_set.contains(v))
				{
					System.out.println(v + " should not be in the remaining_vertex_set according to partitions, conflicts between partitions and dynamic vertex sets");
					return false;
				}
				if(!removed_vertex_list.contains(v))
				{
					System.out.println(v + " should be in the removed_vertex_list according to partitions, conflicts between partitions and dynamic vertex sets");
					return false;
				}
				if(vertices[v].get_degree() != d)
				{
					System.out.println("according to get_degree(), " + v + "\'s degree is " + vertices[v].get_degree());
					System.out.println("according to partitions, " + v + "\'s degree is " + d);
					System.out.println("for vertex " + v + ", degree conflicts between partitions and dynamic vertices");
					return false;
				}
			}
		}
		return true;
	}

	public void show_partitions()
	{
		for(int d = 0; d <= dynamic_max_degree; d++)
		{
			System.out.println("Degree " + d);
			System.out.print("\tin vertices:\n\t");
			for(int i = ptr_to_in_vertices.get(d); i < ptr_to_out_vertices.get(d); i++)
			{
				System.out.print(v_ascending_degree_array.get(i) + "\t");
			}
			System.out.println();
			System.out.print("\tout vertices:\n\t");
			for(int i = ptr_to_out_vertices.get(d); i < ptr_to_in_vertices.get(d+1); i++)
			{
				System.out.print(v_ascending_degree_array.get(i) + "\t");
			}
			System.out.println();
		}
	}
} // class DegreeBasedPartition
