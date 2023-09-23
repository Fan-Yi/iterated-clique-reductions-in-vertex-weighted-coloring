package WeightBucketPack;

import java.io.*;
import java.util.*;
import java.lang.StrictMath;

import UsrPausePack.UsrPause;
import GraphPack.WeightedVertexPack.WeightedVertex;

public class WeightBasedPartition{
	private ArrayList<Integer> v_ascending_weight_array;
	private ArrayList<Integer> index_in_v_ascending_weight_array;
	private ArrayList<Integer> ptr_to_in_vertices; // in vertices on the left
	private ArrayList<Integer> ptr_to_out_vertices; // out vertices on the right
	private int dynamic_max_weight;	
	private int dynamic_min_weight;

	public WeightBasedPartition(int min_weight, int max_weight, int vertex_num, HashSet<Integer> remaining_vertex_set, WeightedVertex[] vertices)
	{
		dynamic_max_weight = max_weight;
		dynamic_min_weight = min_weight;
		v_ascending_weight_array = new ArrayList<Integer>(vertex_num + 2);
		index_in_v_ascending_weight_array = new ArrayList<Integer>(vertex_num + 2);
		ptr_to_in_vertices = new ArrayList<Integer>(dynamic_max_weight + 1 + 2);
		ptr_to_out_vertices = new ArrayList<Integer>(dynamic_max_weight + 1 + 2);
		ArrayList<Integer> v_weight_num = new ArrayList<Integer>(dynamic_max_weight + 1 + 2);
		
		// for placing elements
		ArrayList<Integer> qtr_to_v_weight = new ArrayList<Integer>(dynamic_max_weight + 1 + 2);

		for(int i = 0; i <= dynamic_max_weight + 1; i++) // initialize enough locations to avoid NullPointException
		{
			v_weight_num.add(0);
		}
		
		// make statistics
		for(int v: remaining_vertex_set)
		{
			int w = vertices[v].get_weight();
			v_weight_num.set(w, v_weight_num.get(w) + 1);
		}

		ptr_to_in_vertices.add(0);
		qtr_to_v_weight.add(0);
		
		// partition
		for(int w = 1; w <= dynamic_max_weight + 1; w++)// 
		{
			ptr_to_in_vertices.add(ptr_to_in_vertices.get(w-1) + v_weight_num.get(w-1));
			ptr_to_out_vertices.add(ptr_to_in_vertices.get(w));
			qtr_to_v_weight.add(ptr_to_in_vertices.get(w));
		}
		ptr_to_out_vertices.add(dynamic_max_weight + 1);

		// placement
		for(int i = 0; i <= vertex_num + 1; i++) // initialize enough locations to avoid NullPointException
		{
			v_ascending_weight_array.add(0);
			index_in_v_ascending_weight_array.add(0);
		}
		for(int v : remaining_vertex_set)
		{
			int w = vertices[v].get_weight();
			// place v in the partition with weight w
			v_ascending_weight_array.set(qtr_to_v_weight.get(w), v);
			index_in_v_ascending_weight_array.set(v, qtr_to_v_weight.get(w));
			qtr_to_v_weight.set(w, qtr_to_v_weight.get(w) + 1);	
		}
	} // 

	public int get_dynamic_max_weight()
	{
		return dynamic_max_weight;
	}
	
	public int get_dynamic_min_weight()
	{
		return dynamic_min_weight;
	}

	public void place_out_vertex_from_graph(int v, WeightedVertex[] vertices)
	{
		int v_ptr = index_in_v_ascending_weight_array.get(v);
		int boundary_loc = ptr_to_out_vertices.get(vertices[v].get_weight()) - 1;
		int boundary_v = v_ascending_weight_array.get(boundary_loc);
		//
		v_ascending_weight_array.set(v_ptr, boundary_v);// already had a copy, don't need a tmp vertex
		index_in_v_ascending_weight_array.set(boundary_v, v_ptr);
		//
		v_ascending_weight_array.set(boundary_loc, v);
		index_in_v_ascending_weight_array.set(v, boundary_loc);
		//
		ptr_to_out_vertices.set(vertices[v].get_weight(), ptr_to_out_vertices.get(vertices[v].get_weight()) - 1);

		while(ptr_to_in_vertices.get(dynamic_max_weight).intValue() == ptr_to_out_vertices.get(dynamic_max_weight).intValue())
			dynamic_max_weight--;
		while(ptr_to_in_vertices.get(dynamic_min_weight).intValue() == ptr_to_out_vertices.get(dynamic_min_weight).intValue())
			dynamic_min_weight++;
	} // public void place_out_vertex_from_graph(int v, Vertex[] vertices)

	public ArrayList<Integer> get_vertex_list_of_top_weight(int k)
	// top-k weights
	{
		ArrayList<Integer> ret_list = new ArrayList<Integer>();
		int min_weight_in_loop = StrictMath.max(dynamic_max_weight - k + 1, dynamic_min_weight);
		for(int w = dynamic_max_weight; w >= min_weight_in_loop; w--)
		{
			for(int i = ptr_to_in_vertices.get(w); i < ptr_to_out_vertices.get(w); i++)
			{
				ret_list.add(v_ascending_weight_array.get(i));
			}
		}
		return ret_list;
	}

	public ArrayList<Integer> get_vertex_list_of_weight_at_most(int max_w)
	{
		ArrayList<Integer> ret_list = new ArrayList<Integer>();
		int max_w_in_loop = StrictMath.min(max_w, dynamic_max_weight);
		for(int w = dynamic_min_weight; w <= max_w_in_loop; w++)
		{
			for(int i = ptr_to_in_vertices.get(w); i < ptr_to_out_vertices.get(w); i++)
			{
				ret_list.add(v_ascending_weight_array.get(i));
			}
		}
		return ret_list;
	}

	public ArrayList<Integer> get_vertex_list_of_weight_at_least(int min_w)
	{
		ArrayList<Integer> ret_list = new ArrayList<Integer>();
		int min_w_in_loop = StrictMath.max(min_w, dynamic_min_weight);
		for(int w = min_w_in_loop; w <= dynamic_max_weight; w++)
		{
			for(int i = ptr_to_in_vertices.get(w); i < ptr_to_out_vertices.get(w); i++)
			{
				ret_list.add(v_ascending_weight_array.get(i));
			}
		}
		return ret_list;
	}
	
	public ArrayList<Integer> get_vertex_list_of_weight_between(int min_w, int max_w)
	{
		ArrayList<Integer> ret_list = new ArrayList<Integer>();
		int min_w_in_loop = StrictMath.max(min_w, dynamic_min_weight);
		int max_w_in_loop = StrictMath.min(max_w, dynamic_max_weight);
		for(int w = min_w_in_loop; w <= max_w_in_loop; w++)
		{
			for(int i = ptr_to_in_vertices.get(w); i < ptr_to_out_vertices.get(w); i++)
			{
				ret_list.add(v_ascending_weight_array.get(i));
			}
		}
		return ret_list;
	}

	public ArrayList<Integer> get_vertices_of_maximum_weight()
	{
		return get_vertices_of_weight(dynamic_max_weight);
	}
	
	public int maximum_weight_vertex_count()
	{
		return ptr_to_out_vertices.get(dynamic_max_weight) - ptr_to_in_vertices.get(dynamic_max_weight);
	}
	
	public ArrayList<Integer> get_vertices_of_weight(int w)
	{
		ArrayList<Integer> ret_list = new ArrayList<Integer>();
		for(int i = ptr_to_in_vertices.get(w); i < ptr_to_out_vertices.get(w); i++)
		{
			ret_list.add(v_ascending_weight_array.get(i));
		}
		return ret_list;
	}

	public boolean check_partitions(int vertex_num, WeightedVertex[] vertices, ArrayList<Integer> remaining_vertex_set, ArrayList<Integer> removed_vertex_list)
	{
		for(int i = 0; i < remaining_vertex_set.size(); i++) // considering vertices both in and out
		{
			if(index_in_v_ascending_weight_array.get(v_ascending_weight_array.get(i)) != i)
			{
				System.out.println("Location " + i + " in the array contains " + v_ascending_weight_array.get(i));
				System.out.println("this vertex is actually in " + index_in_v_ascending_weight_array.get(v_ascending_weight_array.get(i)));
				System.out.println("bijection error occurs");
				return false;
			}
		}
		for(int w = dynamic_min_weight; w <= dynamic_max_weight; w++)
		{
			for(int i = ptr_to_in_vertices.get(w); i < ptr_to_out_vertices.get(w); i++)
			{
				int v = v_ascending_weight_array.get(i);
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
				if(vertices[v].get_weight() != w)
				{
					System.out.println("according to get_weight(), " + v + "\'s weight is " + vertices[v].get_weight());
					System.out.println("according to partitions, " + v + "\'s weight is " + w);
					System.out.println("for vertex " + v + ", weight conflicts between partitions and dynamic vertices");
					return false;
				}
			}
			for(int i = ptr_to_out_vertices.get(w); i < ptr_to_in_vertices.get(w+1); i++)
			{
				int v = v_ascending_weight_array.get(i);
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
				if(vertices[v].get_weight() != w)
				{
					System.out.println("according to get_weight(), " + v + "\'s weight is " + vertices[v].get_weight());
					System.out.println("according to partitions, " + v + "\'s weight is " + w);
					System.out.println("for vertex " + v + ", weight conflicts between partitions and dynamic vertices");
					return false;
				}
			}
		}
		return true;
	}

	public void show_partitions()
	{
		for(int w = dynamic_min_weight; w <= dynamic_max_weight; w++)
		{
			System.out.println("Weight " + w);
			System.out.print("\tin vertices:\n\t");
			for(int i = ptr_to_in_vertices.get(w); i < ptr_to_out_vertices.get(w); i++)
			{
				System.out.print(v_ascending_weight_array.get(i) + "\t");
			}
			System.out.println();
			System.out.print("\tout vertices:\n\t");
			for(int i = ptr_to_out_vertices.get(w); i < ptr_to_in_vertices.get(w+1); i++)
			{
				System.out.print(v_ascending_weight_array.get(i) + "\t");
			}
			System.out.println();
		}
	}	
}
