package GraphPack.DynamicSimpleUndirectedVertexWeightedGraphPack;

import java.util.*;
import java.io.*;
import java.lang.StrictMath;

import GraphPack.SimpleUndirectedVertexWeightedGraphPack.SimpleUndirectedVertexWeightedGraph;
import DegreeBucketPack.DegreeBasedPartition;
import WeightBucketPack.WeightBasedPartition;


public class DynamicSimpleUndirectedVertexWeightedGraph extends SimpleUndirectedVertexWeightedGraph{

	public class PositiveIntegerMap{

		private TreeMap<Integer, Integer> big_to_small;
		private ArrayList<Integer> small_to_big;
	
		public PositiveIntegerMap()
		{
			small_to_big = new ArrayList<Integer>(remaining_vertex_set.size() + 1);
			big_to_small = new TreeMap<Integer, Integer>();

			small_to_big.add(0);
			for(int v = 1; v <= vertex_num; v++)
			{
				if(remaining_vertex_set.contains(v))
				{
					small_to_big.add(v);
					big_to_small.put(v, small_to_big.size() - 1);
				}
			}

		} // public PositiveIntegerMap()

		public int get_original_id_from_new_id(int new_id)
		{
			try{
				return small_to_big.get(new_id);
			}catch(IndexOutOfBoundsException e)
			{
				System.out.println("Exception occurs when computing the original vertex id of " + new_id +", may because of the absence of construction function: " + e);
				System.exit(1);
			}
			return 0;
		}

		public int get_new_id_from_original_id(int org_id)
		{
// System.out.println("about to get new id from the original vertex id " + org_id);
			try{
				return big_to_small.get(org_id);
			}catch(IndexOutOfBoundsException e)
			{
				System.out.println("Exception occurs when computing the new vertex id of " + org_id + ", may because of the absence of construction function: " + e);
				System.exit(1);
			}
			return 0;
		}
		
		public String toString()
		{
			// String output_str = new String();
			StringBuffer s_buffer = new StringBuffer();
			
			// output_str += "big to small\n";
			s_buffer.append("big to small\n");
			for(int i = 1; i < small_to_big.size(); i++)
			{
				// output_str += i + " " + small_to_big.get(i) + "\n";
				s_buffer.append(i + " " + small_to_big.get(i) + "\n");
			}
			// output_str += "small to big\n";
			s_buffer.append("small to big\n");;
			// Get a set of the entries.
			Set<Map.Entry<Integer, Integer>> e_set = big_to_small.entrySet();
			// Display the elements.
			for(Map.Entry<Integer, Integer> me : e_set) {
				// output_str += me.getKey() + " " + me.getValue() + "\n";
				s_buffer.append(me.getKey() + " " + me.getValue() + "\n");
			}
			// return output_str;
			return s_buffer.toString();
		}
	};

	protected ArrayList<Integer> removed_vertex_list = new ArrayList<Integer>();
	protected HashSet<Integer> remaining_vertex_set = new HashSet<Integer>();
	protected ArrayList<Integer> removed_edge_list = new ArrayList<Integer>();
	protected HashSet<Integer> remaining_edge_set = new HashSet<Integer>();

	private PositiveIntegerMap id_map_in_shrinking;

	protected DegreeBasedPartition dgr_based_partition;
	protected WeightBasedPartition weight_based_partition;

	public DynamicSimpleUndirectedVertexWeightedGraph(String instance_file_name_with_path)
	{
		super(instance_file_name_with_path);
		for(int v = 1; v <= vertex_num; v++)
		{
			remaining_vertex_set.add(v);
		}
		for(int e = 0; e < edge_num; e++)
		{
			remaining_edge_set.add(e);
		}

		dgr_based_partition = new DegreeBasedPartition(get_max_degree(), vertex_num, remaining_vertex_set, vertices);
		weight_based_partition = new WeightBasedPartition(get_min_vertex_weight(), get_max_vertex_weight(), vertex_num, remaining_vertex_set, vertices);
	}

	public String vertex_and_edge_num_change_info_to_string()
	{
		String output_string = new String();

		output_string += " & " + vertex_num + " & " + edge_num; 
		output_string += " & " + remaining_vertex_set.size() + " & " + remaining_edge_set.size();

		return output_string;
	}
	
	public PositiveIntegerMap get_id_map_in_shrinking()
	{
		return id_map_in_shrinking;
	}
	
	public void set_id_map_in_shrinking()
	{
		id_map_in_shrinking = new PositiveIntegerMap();
	}

	public String toString(boolean shrunk)
	{
		// String output_string = new String();
		StringBuffer s_buffer = new StringBuffer();
		// headline
		if(shrunk)
		{
			// output_string += ("p edge " + remaining_vertex_set.size() + " " + remaining_edge_set.size() + "\n");
			s_buffer.append("p edge " + remaining_vertex_set.size() + " " + remaining_edge_set.size() + "\n");
		}
		else
		{
			// output_string += ("p edge " + vertex_num + " " + remaining_edge_set.size() + "\n");
			s_buffer.append("p edge " + vertex_num + " " + remaining_edge_set.size() + "\n");
		}
		
		// v lines
		if(shrunk)
		{
			for(int v : remaining_vertex_set) // 
			{
				// output_string += ("v " + id_map_in_shrinking.get_new_id_from_original_id(v) + " " + vertices[v].get_weight() + "\n");
				s_buffer.append("v " + id_map_in_shrinking.get_new_id_from_original_id(v) + " " + vertices[v].get_weight() + "\n");
			}
		}
		else
		{
			for(int v = 1; v <= vertex_num; v++)
			{
				// output_string += ("v " + v + " " + vertices[v].get_weight() + "\n");
				s_buffer.append("v " + v + " " + vertices[v].get_weight() + "\n");
			}
		}
		
		// e lines
		for(int e : remaining_edge_set)
		{
			int v1, v2;
			int[] v_array = new int[2];
			edges[e].get_vertices(v_array);
			v1 = v_array[0]; v2 = v_array[1];
			if(shrunk)
			{
				// output_string += ("e " + id_map_in_shrinking.get_new_id_from_original_id(v1) + " " + id_map_in_shrinking.get_new_id_from_original_id(v2) + "\n");
				s_buffer.append("e " + id_map_in_shrinking.get_new_id_from_original_id(v1) + " " + id_map_in_shrinking.get_new_id_from_original_id(v2) + "\n");
			}
			else
			{
				// output_string += ("e " + v1 + " " + v2 + "\n");
				s_buffer.append("e " + v1 + " " + v2 + "\n");
			}

		}

		// return output_string;
		return s_buffer.toString();
	}

	public HashSet<Integer> get_remaining_vertex_set()
	{
		return remaining_vertex_set;
	}

	public ArrayList<Integer> get_removed_vertex_list()
	{
		return removed_vertex_list;
	}

	public HashSet<Integer> get_remaining_edge_set()
	{
		return remaining_edge_set;
	}

	public ArrayList<Integer> get_removed_edge_list()
	{
		return removed_edge_list;
	}


	public void remove_vertex_from_graph(int u) // modify the graph accordingly
	{
		// maintain partitions
		weight_based_partition.place_out_vertex_from_graph(u, vertices);

		dgr_based_partition.place_out_vertex_from_graph(u, vertices);
		for(int nb_of_u : vertices[u].get_neighbors())
		{
			dgr_based_partition.degree_dec_by_one(nb_of_u, vertices);
		}

		remaining_vertex_set.remove(Integer.valueOf(u));
		removed_vertex_list.add(u);

		// remove from the static graph
		boolean max_degree_can_be_decreased = false;
		for(int nb_of_u : vertices[u].get_neighbors())
		{
			vertices[nb_of_u].get_neighbors().remove(Integer.valueOf(u));
			
			int e_to_remove = (int)edge_index_of(u, nb_of_u);
			remaining_edge_set.remove(Integer.valueOf(e_to_remove));
			removed_edge_list.add(e_to_remove);
			vertices[nb_of_u].get_adj_edges().remove(Integer.valueOf(e_to_remove));
			remove_edge_hash_id_from_set(u, nb_of_u);
			remove_edge_hash_id_to_index_id_from_map(u, nb_of_u);
			if(vertices[nb_of_u].get_degree() == get_max_degree())
			{
				max_degree_can_be_decreased = true;
			}
			vertices[nb_of_u].set_degree(vertices[nb_of_u].get_degree() - 1);
			

			// max_vertex_weight
			if(vertices[u].get_weight() == get_max_vertex_weight())
			{
				set_max_vertex_weight(weight_based_partition.get_dynamic_max_weight());
			}
			// min_vertex_weight
			if(vertices[u].get_weight() == get_min_vertex_weight())
			{
				set_min_vertex_weight(weight_based_partition.get_dynamic_min_weight());
			}
			// max_degree
			if(vertices[u].get_degree() == get_max_degree() || max_degree_can_be_decreased)
			{
				set_max_degree(dgr_based_partition.get_dynamic_max_degree());
			}
		}
		
// System.out.println(u + " has been removed in the dynamic simple undirected vertex weighted graph file============================================================.");
	} // public void remove_vertex_from_graph(int u)
	
	public void show_graph()
	{
		System.out.println("p edge " + vertex_num + " " + edge_num);
		for(int v = 1; v <= vertex_num; v++)
		{
			System.out.println("v " + v + " " + vertices[v].get_weight());
		}
		System.out.println("neighbr lists:");
		for(int v : remaining_vertex_set)
		{
			System.out.println(v + ": " + vertices[v].get_neighbors());

		}
		System.out.println();
	}
	
	public void check_graph()
	{
		if(remaining_vertex_set.size() + removed_vertex_list.size() != vertex_num)
		{
			System.out.println("remaining or removed vertex set contains errors");
			System.exit(1);
		}
		
		if(remaining_edge_set.size() + removed_edge_list.size() != edge_num)
		{
			System.out.println("remaining or removed edge set contains errors");
			System.exit(1);
		}
		
		for(int v = 1; v <= vertex_num; v++)
		{
// System.out.println("checking vertex " + v);
			boolean is_remaining = remaining_vertex_set.contains(Integer.valueOf(v));
			boolean is_removed = removed_vertex_list.contains(Integer.valueOf(v));
			if(is_remaining == is_removed)
			{
				System.out.println("Vertex " + v + " exists in both the remaining and the removed set.");
				System.exit(1);
			}
		}
		
		for(int e = 0; e < edge_num; e++)
		{
// System.out.println("checking edge " + e);
			int v1, v2;
			int[] v_array = new int[2];
			edges[e].get_vertices(v_array);
			v1 = v_array[0]; v2 = v_array[1];
			boolean is_remaining = remaining_edge_set.contains(Integer.valueOf(e));
			boolean is_removed = removed_edge_list.contains(Integer.valueOf(e));
			if(is_remaining == is_removed)
			{
				System.out.println("Edge " + e + " exists in both the remaining and the removed set.");
				System.exit(1);
			}
			boolean is_adj = is_connected(v1, v2);
			boolean is_in_map = edge_hash_id_in_map(v1, v2);
			boolean is_remaining_conflict_with_hash = is_remaining && (!is_adj || !is_in_map);
			boolean is_removed_conflict_with_hash = is_removed && (is_adj || is_in_map);
			if(is_remaining_conflict_with_hash)
			{
				System.out.println("Edge " + e + " in remaining conflict with hash");
				System.exit(1);
			}
			if(is_removed_conflict_with_hash)
			{
				System.out.println("Edge " + e + " in removed conflict with hash");
				System.out.println("is_removed: " + is_removed);
				System.out.println("is_adj: " + is_adj);
				System.out.println("is_in_map: " + is_in_map);
				System.exit(1);
			}
		}
	}
}
