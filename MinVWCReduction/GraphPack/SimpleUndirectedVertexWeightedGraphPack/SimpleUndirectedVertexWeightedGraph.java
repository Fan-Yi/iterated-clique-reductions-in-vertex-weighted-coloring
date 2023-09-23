package GraphPack.SimpleUndirectedVertexWeightedGraphPack;

import java.util.*;
import java.io.*;
import java.lang.StrictMath;

import GraphPack.WeightedVertexPack.WeightedVertex;
import GraphPack.EdgePack.Edge;

import GraphPack.GraphFormatParserPack.SimpleUndirectedVertexWeightedGraphDIMACSParser;

import UsrPausePack.UsrPause;
import DebugConfigPack.DebugConfig;


public class SimpleUndirectedVertexWeightedGraph{
	protected WeightedVertex vertices[];
	protected Edge edges[];

	protected int vertex_num;
	protected int edge_num;

	private int max_degree;

	private int max_vertex_weight;
	private int min_vertex_weight;

	// for determining whether two vertices are connected
	protected HashSet<Long> edge_hash_id_set = new HashSet<Long>();
	// for determining edge id
	protected HashMap<Long, Long> edge_hash_id_to_index_id_map = new HashMap<Long, Long>();

	private long pair_id_encode(long n1, long n2) // must use long, int type is not big enough
	{
		return ((n1 + n2 + 1) * (n1 + n2) >> 1) + n2;
	}

	private void decode_pair_id(long pair_id, long[] array_int)
	{
		long w = (long)(StrictMath.sqrt((pair_id << 3) + 1) - 1) / 2;
		long t = (w * w + w) >> 1;
		long n2 = pair_id - t;
		long n1 = w - n2;
		array_int[0] = n1;
		array_int[1] = n2; 
	}

	protected long unordered_pair_id_encode(long v1, long v2)
	{
		long n1, n2; // n1 captures the smaller value, n2 captures the bigger one

		if(v1 < v2)
		{
			n1 = v1; n2 = v2;
		}
		else
		{
			n1 = v2; n2 = v1;
		}
		return pair_id_encode(n1, n2);
	}

	private void insert_edge_hash_id_to_set(long u, long v)
	{	
		long edge_hash_id = unordered_pair_id_encode(u, v);
		edge_hash_id_set.add(edge_hash_id);		
	}
	
	public void remove_edge_hash_id_from_set(long u, long v)
	{
		long edge_hash_id = unordered_pair_id_encode(u, v);
		edge_hash_id_set.remove(Long.valueOf(edge_hash_id));	
	}

	private void insert_edge_hash_id_to_index_id_into_map(long u, long v, long e)
	{
		long edge_hash_id = unordered_pair_id_encode(u, v);
		edge_hash_id_to_index_id_map.put(edge_hash_id, e);
	}
	
	public void remove_edge_hash_id_to_index_id_from_map(long u, long v)
	{
		long edge_hash_id = unordered_pair_id_encode(u, v);
		edge_hash_id_to_index_id_map.remove(Long.valueOf(edge_hash_id));
	}
/*
	private boolean edge_hash_id_in_set(long u, long v)
	{
		long edge_hash_id = unordered_pair_id_encode(u, v);
		return edge_hash_id_set.contains(edge_hash_id);
	}
*/

	public boolean is_connected(long u, long v)
	{
		long hash_value = unordered_pair_id_encode(u, v);
		return edge_hash_id_set.contains(hash_value);
	}

	protected long edge_hash_id_num()
	{
		return edge_hash_id_set.size();
	}
	
	protected boolean edge_hash_id_in_map(long u, long v)
	{
		long edge_hash_id = unordered_pair_id_encode(u, v);
		return edge_hash_id_to_index_id_map.containsKey(edge_hash_id);
	}

	protected long edge_index_of(long u, long v)
	{
		long edge_hash_id = unordered_pair_id_encode(u, v);
		return edge_hash_id_to_index_id_map.get(edge_hash_id);
	}

	public int get_max_degree()
	{
		return max_degree;
	}

	public void set_max_degree(int max_d)
	{
		max_degree = max_d;
	}

	public int get_max_vertex_weight()
	{
		return max_vertex_weight;
	}

	public void set_max_vertex_weight(int max_w)
	{
		max_vertex_weight = max_w;
	}

	public int get_min_vertex_weight()
	{
		return min_vertex_weight;
	}

	public void set_min_vertex_weight(int min_w)
	{
		min_vertex_weight = min_w;
	}

	private String scan_for_head_line(BufferedReader buff_r)
	{
				String s;
				do{					
					try{
						s = buff_r.readLine();
					}catch(IOException e)
					{
						System.out.println("Input error occurs when reading a line from file: " + e);
						return null;
					}	

					if(s == null)
					{
						System.out.println("no head lines found.");
						return null;
					}

					if(s.startsWith("c")) continue;
					if(s.startsWith("p")) break;
					else
					{
						System.out.println("Format error occurs in locating the head line.");
						return null;
					}
				}while(true);
				return s;		
	}

	public SimpleUndirectedVertexWeightedGraph(String instance_file_name_with_path)
	{
			System.out.println("File: " + instance_file_name_with_path);
			vertex_num = 0; 
			edge_num = 0;
			
			// open the input file
			// File instance_file = new File(instance_folder_name, instance_file_name);

			try{// catch(FileNotFoundException e)			
				// FileReader fr = new FileReader(instance_file);
				FileReader fr = new FileReader(instance_file_name_with_path);
				BufferedReader br = new BufferedReader(fr);
				String s = scan_for_head_line(br);
				if(s == null)
				{
					System.out.println("No head lines found.");
					System.exit(1);
				}

				// deal with the head line
				Scanner convert_str_in = new Scanner(s);
				int array_int[] = new int[2];

				SimpleUndirectedVertexWeightedGraphDIMACSParser dimacs_parser = new SimpleUndirectedVertexWeightedGraphDIMACSParser();
				if(!dimacs_parser.deal_with_head_line(convert_str_in, array_int))
				{
					System.out.println("Failure in dealing with the head line.");
					System.exit(1);
				}
				vertex_num = array_int[0];
				edge_num = array_int[1];

				vertices = new WeightedVertex[vertex_num + 2];
				edges = new Edge[edge_num + 2];

				for(int i = 1; i <= vertex_num; i++)
				{
					vertices[i] = new WeightedVertex(new TreeSet<Integer>(), new TreeSet<Integer>(), 0, 0);
				}

				for(int i = 0; i < edge_num; i++)
				{
					edges[i] = new Edge();
				}

if(DebugConfig.READ_WEIGHT_MODE)
{
				// deal with v lines
				for(int i = 1; i <= vertex_num; i++)
				{
					try{
						s = br.readLine();
					}catch(IOException e)
					{
						System.out.println("Input error occurs when reading a line from file: " + e);
						System.exit(1);
					}	

					if(s == null)
					{
						System.out.println("Not enough v lines."); 
						System.exit(1);
					}
					convert_str_in = new Scanner(s);
					if(!dimacs_parser.deal_with_vertex_line(convert_str_in, array_int))
					{
						System.out.println("Failure in dealing with v lines.");
						System.exit(1);
					}
					int v_id = array_int[0], v_weight = array_int[1];
					if(v_id != i)
					{
						System.out.println("v_id: " + v_id + ", v_line_id: " + i + ", not equal.");
						System.exit(1);
					}
// System.out.println("v_id: " + v_id);
// System.out.println("v_weight: " + v_weight);
					vertices[v_id].set_weight(v_weight);

// if(i % 10000 == 0) System.out.println(i + " v lines have been read");
				}
}

if(DebugConfig.CALC_PULLAN_WEIGHT_MODE)
{
				for(int v = 1; v <= vertex_num; v++)
				{
					vertices[v].set_weight(v % 200 + 1);
				}
}

				// deal with e lines
/*
				int[] v_degree_tmp = new int[vertex_num + 2];

				for(int v = 1; v <= vertex_num; v++)
				{
					v_degree_tmp[v] = 0;
				}
*/

				for(int i = 0; i < edge_num; i++)
				{
					try{
						s = br.readLine();
					}catch(IOException e)
					{
						System.out.println("Input error occurs when reading a line from file: " + e);
						System.exit(1);
					}

					if(s == null)
					{
System.out.println("Edge " + i + " is not available.");
System.out.println("not enough lines to be read");
						System.exit(1);
					}
					convert_str_in = new Scanner(s);
					if(!dimacs_parser.deal_with_edge_line(convert_str_in, array_int))
					{
System.out.println("format errors occur when reading e lines");
						System.exit(1);
					}
// if(i % 500000 == 0) System.out.println(i + " lines have been read.");
					int v1 = array_int[0], v2 = array_int[1];
					edges[i].set_vertices(v1, v2); 
					// v_degree_tmp[v1]++;
					// v_degree_tmp[v2]++;

					insert_edge_hash_id_to_set(v1, v2);
					insert_edge_hash_id_to_index_id_into_map(v1, v2, i);

// if(i % 10000 == 0) System.out.println(i + " e lines have been read");
				}// for(int i = 0; i < edge_num; i++)

				// initialize vertex info

				
				
if(DebugConfig.CALC_W2_WEIGHT_MODE)
{
				for(int v = 1; v <= vertex_num; v++)
				{
					int v_degree = vertices[v].get_degree();
					if(v_degree <= StrictMath.round(0.35 * max_degree))
						vertices[v].set_weight(8);
					else if(v_degree <= StrictMath.round(0.6 * max_degree))
						vertices[v].set_weight(4);
					else if(v_degree <= StrictMath.round(0.85 * max_degree))
						vertices[v].set_weight(2);
					else
						vertices[v].set_weight(1);
				}
}

				// compute max_vertex_weight and min_vertex_weight
				max_vertex_weight = vertices[1].get_weight();
				for(int v = 2; v <= vertex_num; v++)
				{
					if(vertices[v].get_weight() > max_vertex_weight)
						max_vertex_weight = vertices[v].get_weight();
				}
				min_vertex_weight = vertices[1].get_weight();
				for(int v = 2; v <= vertex_num; v++)
				{
					if(vertices[v].get_weight() < min_vertex_weight)
						min_vertex_weight = vertices[v].get_weight();
				}
			
				for(int e = 0; e < edge_num; e++)
				{
					edges[e].get_vertices(array_int);
					int v1 = array_int[0], v2 = array_int[1];

					vertices[v1].add_neighbor(v2);
					vertices[v2].add_neighbor(v1);
					vertices[v1].add_adj_edge(e);
					vertices[v2].add_adj_edge(e);
					// v_degree_tmp[v1]++;
					// v_degree_tmp[v2]++;
				}
				
				for(int v = 1; v <= vertex_num; v++)
				{
					// vertices[v].allocate_neighborhood_space(v_degree_tmp[v]);
					vertices[v].set_degree(vertices[v].get_neighbors().size());
				}
				// compute max_degree				
				max_degree = vertices[1].get_degree();
				for(int v = 2; v <= vertex_num; v++)
				{
					max_degree = vertices[v].get_degree() > max_degree ? vertices[v].get_degree() : max_degree;
				}
// System.out.println(1);
				// close file
				try{
					fr.close();
				}catch(IOException e)
				{
					System.out.println("Error occurs when closing the file reader");
					System.exit(1);
				}

			}catch(FileNotFoundException e)
			{
				System.out.println("Exception occurs when constructing a file reader: " + e);
				System.exit(1);
			}
// System.out.println("having contructed a simple undirected vertex weighted graph.");
// show_simple_undirected_vertex_weighted_graph();
	}// public SimpleUndirectedVertexWeightedGraph(String instance_folder_name, String instance_file_name)

	public void show_simple_undirected_vertex_weighted_graph()
	{
		System.out.println("p edge " + vertex_num + " " + edge_num);
		for(int v = 1; v <= vertex_num; v++)
		{
			System.out.println("v " + v + " " + vertices[v].get_weight());
		}
		System.out.println("neighbr lists:");
		for(int v = 1; v <= vertex_num; v++)
		{
			System.out.println(v + ": " + vertices[v].get_neighbors());
/*
			for(int i = 0; i < vertices[v].get_degree(); i++)
				System.out.print(vertices[v].get_neighbors()[i] + "\t");
			System.out.println();
*/
		}
		System.out.println();
	}

	public WeightedVertex[] get_vertices()
	{
		return vertices;
	}
	
	public int get_vertex_num()
	{
		return vertex_num;
	}
	
	public Edge[] get_edges()
	{
		return edges;
	}
	

	public int get_edge_num()
	{
		return edge_num;
	}	
}
