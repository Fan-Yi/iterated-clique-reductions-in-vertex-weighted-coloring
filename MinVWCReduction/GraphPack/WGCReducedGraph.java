package GraphPack;

import java.util.*;

import GraphPack.DynamicSimpleUndirectedVertexWeightedGraphPack.DynamicSimpleUndirectedVertexWeightedGraph;
import GraphPack.WeightedVertexPack.WeightedVertex;
import GraphPack.WeightedVertexPack.VertexPack.Vertex;
import CliqueSearchPack.CliqueSearch;
import NeighborhoodSubsetSearchPack.NeighborhoodSubsetSearch;
import InterfacePack.IntRelate;
import TwoParametersInterfacePack.TwoParametersBehavior;
import ZeroParameterInterfacePack.ZeroParameterBehavior;
import ConfigPack.*;


import UsrPausePack.UsrPause;

public class WGCReducedGraph extends DynamicSimpleUndirectedVertexWeightedGraph
{

	private ArrayList<ArrayList<Integer>> critical_clique_list;
	private NeighborhoodSubsetSearch nb_subset_search;
	private CliqueSearch cliq_search;

	// for reductions
	private TreeSet<Integer> cand_remove_vertices_for_clique_reductions_wrt_degree_decrease;	

	private TreeSet<Integer> cand_remove_vertices_for_single_single;
	private TreeSet<Integer> cand_remove_edges_for_double_double;
	private	TreeSet<Integer> cand_remove_vertices_for_triple_double;
	private TreeSet<Integer> cand_remove_edges_for_quadruple_double;
	private TreeSet<Integer> cand_remove_vertices_for_link_link;
	private TreeSet<Integer> cand_remove_edges_for_triple_triple;

	private class Adjacency implements IntRelate{
			public boolean related(long v1, long v2){
				return is_connected(v1, v2);
		}
	};

	private class CliqueReductionWrtDegreeDecreaseDisregardingSubsetReductions implements ZeroParameterBehavior{
			public boolean behavior(){
				return apply_clique_reductions_wrt_degree_decrease_disregarding_subset_reductions();
		}
	};

	private class CliqueReductionWrtDegreeDecreaseConsideringSubsetReductions implements ZeroParameterBehavior{
			public boolean behavior(){
				return apply_clique_reductions_wrt_degree_decrease_considering_subset_reductions();
		}
	};

	private class CliqueReductionWrtTopLevelWeightUpdatesDisregardingSubsetReductions implements TwoParametersBehavior{
			public void behavior(ArrayList<Integer> last_top_level_weight_updated_colors, ArrayList<Integer> reserved_vertex_list){
				apply_clique_reductions_wrt_top_level_weight_updates_disregarding_subset_reductions(last_top_level_weight_updated_colors, reserved_vertex_list);
		}
	};

	private class CliqueReductionWrtTopLevelWeightUpdatesConsideringSubsetReductions implements TwoParametersBehavior{
			public void behavior(ArrayList<Integer> last_top_level_weight_updated_colors, ArrayList<Integer> reserved_vertex_list){
				apply_clique_reductions_wrt_top_level_weight_updates_considering_subset_reductions(last_top_level_weight_updated_colors, reserved_vertex_list);
		}
	};

	public WGCReducedGraph(String instance_file_name_with_path, int seed, long start_elap_milliseconds_from_past, long run_time_cut_off)
	{
		super(instance_file_name_with_path);

		critical_clique_list = new ArrayList<ArrayList<Integer>>();
		
		nb_subset_search = new NeighborhoodSubsetSearch(vertex_num);

// System.out.println("get_max_degree(): " + get_max_degree());
		cliq_search = new CliqueSearch(vertex_num, get_max_degree(), seed, start_elap_milliseconds_from_past, run_time_cut_off);

		cand_remove_vertices_for_clique_reductions_wrt_degree_decrease = new TreeSet<Integer>();	

		cand_remove_vertices_for_single_single = new TreeSet<Integer>();
		cand_remove_edges_for_double_double = new TreeSet<Integer>();
		cand_remove_vertices_for_triple_double = new TreeSet<Integer>();
		cand_remove_edges_for_quadruple_double = new TreeSet<Integer>();
		cand_remove_vertices_for_link_link = new TreeSet<Integer>();
		cand_remove_edges_for_triple_triple = new TreeSet<Integer>();

System.out.println("having constructed a WGCReduced graph.");	
	}
	
	public ArrayList<Integer> get_top_level_weights_wrt_colors()
	{
		return cliq_search.get_top_level_weights_wrt_colors();
	}
	
	public ArrayList<Boolean> get_can_exist_clique_above_at_color()
	{
		return cliq_search.get_can_exist_clique_above_at_color();
	}
	
	public ArrayList<ArrayList<Integer>> get_critical_clique_list()
	{
		return critical_clique_list;
	}
	
	public ArrayList<Integer> get_random_maximum_clique_found(boolean org_id_used)
	{
		int best_size = 0;
		ArrayList<ArrayList<Integer>> best_clq_list = new ArrayList<ArrayList<Integer>>();
		for(ArrayList<Integer> clq : critical_clique_list)
		{
			if(clq.size() > best_size)
			{
				best_size = clq.size();
				best_clq_list.clear();
				best_clq_list.add(clq);
			}
			else if(clq.size() == best_size)
			{
				best_clq_list.add(clq);
			}
		}
		ArrayList<Integer> temp_clq = best_clq_list.get(cliq_search.get_rand().nextInt(best_clq_list.size()));
		ArrayList<Integer> best_clq_with_shrunk_id = new ArrayList<Integer>();
		for(int v : temp_clq)
		{
			int id = org_id_used ? v : get_id_map_in_shrinking().get_new_id_from_original_id(v);
			best_clq_with_shrunk_id.add(id);
		}
		return best_clq_with_shrunk_id;
	}
	
	public int get_lower_bound_by_cliques()
	{
		int sum = 0;
		for(int w : cliq_search.get_top_level_weights_wrt_colors())
		{
// System.out.println(w);
			sum += w;
		}
		return sum;
	}
/*
	public void apply_reductions_with_constructed_cliques()
	{
		cliq_search.update_top_level_weights_wrt_weight_size_tuned_greedy_clique_list(critical_clique_list, vertices, new Adjacency(), new CliqueReductionWrtDegreeDecreaseConsideringSubsetReductions(), new CliqueReductionWrtTopLevelWeightUpdatesConsideringSubsetReductions(), dgr_based_partition, weight_based_partition);
System.out.println("at the end of clique constructions, renaming_vertex_num: " + remaining_vertex_set.size());
	}
*/
/*
	private ArrayList<Integer> random_elements_with_replacements(ArrayList<Integer> whole_list, int size_of_ret_list)
	{
		ArrayList<Integer> ret_list = new ArrayList<Integer>();
		Random rand = new Random();
		for(int i = 0; i < size_of_ret_list; i++)
		{
			ret_list.add(whole_list.get(rand.nextInt(whole_list.size())));
		}
		return ret_list;
	}
*/
	public void apply_reductions_with_enumerated_cliques_containing_top_degree_vertices()
	{
/*
System.out.println("about to enumerate cliques containing top-degree vertices and perform reductions");
System.out.println("dynamic max degree: " + dgr_based_partition.get_dynamic_max_degree());
*/
		ArrayList<Integer> starting_vertex_list = dgr_based_partition.get_vertices_of_maximum_degree();

/*
		// only consider one element when dealing with conventional graphs
starting_vertex_list = random_elements_with_replacements(starting_vertex_list, 1);
System.out.println(starting_vertex_list);
UsrPause.press_enter_to_continue();
UsrPause.press_enter_to_continue();
*/
		cliq_search.update_top_level_weights_wrt_enumerated_cliques_adhere_to_vertex_list(critical_clique_list,  cand_remove_vertices_for_clique_reductions_wrt_degree_decrease, vertices, starting_vertex_list, new Adjacency(), new CliqueReductionWrtDegreeDecreaseDisregardingSubsetReductions(), new CliqueReductionWrtTopLevelWeightUpdatesDisregardingSubsetReductions(), weight_based_partition);
	}

	public void apply_reductions_with_enumerated_cliques_containing_top_weight_vertices()
	{
// System.out.println("about to enumerate cliques containing top-weight vertices and perform reductions");
		ArrayList<Integer> starting_vertex_list = weight_based_partition.get_vertices_of_maximum_weight();
// System.out.println("*********************");

/*
		// only consider one element when dealing with conventional graphs
starting_vertex_list = random_elements_with_replacements(starting_vertex_list, 1);
System.out.println(starting_vertex_list);
UsrPause.press_enter_to_continue();
UsrPause.press_enter_to_continue();
*/
		cliq_search.update_top_level_weights_wrt_enumerated_cliques_adhere_to_vertex_list(critical_clique_list, cand_remove_vertices_for_clique_reductions_wrt_degree_decrease, vertices, starting_vertex_list, new Adjacency(), new CliqueReductionWrtDegreeDecreaseDisregardingSubsetReductions(), new CliqueReductionWrtTopLevelWeightUpdatesDisregardingSubsetReductions(), weight_based_partition);
	}
	
	public void apply_reductions_with_randomly_selected_vertices(int sampled_num, int k)
	{
System.out.println("to generate promising cliques from random " + sampled_num + " vertices");
		ArrayList<ArrayList<Integer>> start_clique_list = new ArrayList<ArrayList<Integer>>();
		for(int i = 0; i < sampled_num; i++)
		{
			// convert HashSet to an array
		    Integer[] array_numbers = remaining_vertex_set.toArray(new Integer[remaining_vertex_set.size()]);
	  
		    // this will generate a random number between 0 and HashSet.size - 1
		    int rand_num = cliq_search.get_rand().nextInt(remaining_vertex_set.size());
		    
			int u = array_numbers[rand_num];
			
			ArrayList<Integer> temp_list = new ArrayList<Integer>();
			temp_list.add(u);
			start_clique_list.add(temp_list);
		}
System.out.println("start_clique_list: " + start_clique_list);
		cliq_search.update_top_level_weights_by_clique_sampling_and_apply_reductions(start_clique_list, k, critical_clique_list, cand_remove_vertices_for_clique_reductions_wrt_degree_decrease, vertices, new Adjacency(), new CliqueReductionWrtDegreeDecreaseDisregardingSubsetReductions(), new CliqueReductionWrtTopLevelWeightUpdatesDisregardingSubsetReductions(), dgr_based_partition, weight_based_partition, remaining_vertex_set, remaining_vertex_set, true);
	}

	public void apply_reductions_with_new_critical_cliques()
	{
//System.out.println("about to look for new critical cliques and perform reductions");
		cliq_search.update_top_level_weights_wrt_new_critical_cliques(critical_clique_list, cand_remove_vertices_for_clique_reductions_wrt_degree_decrease, vertices, new Adjacency(), new CliqueReductionWrtDegreeDecreaseDisregardingSubsetReductions(), new CliqueReductionWrtTopLevelWeightUpdatesDisregardingSubsetReductions(), weight_based_partition);
/*
System.out.println("critical_clique_list: ");
for(ArrayList<Integer> clq : critical_clique_list)
{
System.out.println(clq);
}
*/
	}
	
	public void apply_reductions_with_sampled_cliques_containing_top_weight_and_degree_vertices(int k)
	{
		apply_initial_reductions_with_clique_sampling(k, vertices, new Adjacency(), new CliqueReductionWrtDegreeDecreaseDisregardingSubsetReductions(), new CliqueReductionWrtTopLevelWeightUpdatesDisregardingSubsetReductions());
	}
	
	private void apply_initial_reductions_with_clique_sampling(int k, WeightedVertex[] vertices, IntRelate int_relate_ob, ZeroParameterBehavior zero_param_behav_ob, TwoParametersBehavior two_param_behav_ob)
	{
		ArrayList<ArrayList<Integer>> start_clique_list = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> top_weight_vertices = weight_based_partition.get_vertices_of_maximum_weight();
		for(int u : top_weight_vertices)
		{
			ArrayList<Integer> tempAList = new ArrayList<Integer>();
			tempAList.add(u);
			start_clique_list.add(tempAList);
		}
		ArrayList<Integer> top_degree_vertices = dgr_based_partition.get_vertices_of_maximum_degree();
		for(int u : top_degree_vertices)
		{
			ArrayList<Integer> tempAList = new ArrayList<Integer>();
			tempAList.add(u);
			start_clique_list.add(tempAList);
		}
		cliq_search.update_top_level_weights_by_clique_sampling_and_apply_reductions(start_clique_list, k, critical_clique_list, cand_remove_vertices_for_clique_reductions_wrt_degree_decrease, vertices, new Adjacency(), new CliqueReductionWrtDegreeDecreaseDisregardingSubsetReductions(), new CliqueReductionWrtTopLevelWeightUpdatesDisregardingSubsetReductions(), dgr_based_partition, weight_based_partition, remaining_vertex_set, remaining_vertex_set, true);
	}
	
	public void apply_later_reductions_with_clique_sampling(int sample_num)
	{
System.out.println("later sampling");
		cliq_search.update_top_level_weights_wrt_colors_by_clique_sampling(sample_num, critical_clique_list, cand_remove_vertices_for_clique_reductions_wrt_degree_decrease, vertices, new Adjacency(), new CliqueReductionWrtDegreeDecreaseDisregardingSubsetReductions(), new CliqueReductionWrtTopLevelWeightUpdatesDisregardingSubsetReductions(), dgr_based_partition, weight_based_partition, remaining_vertex_set);
System.out.println("top_level_weights_wrt_colors: " + cliq_search.get_top_level_weights_wrt_colors());
	}
	

	private void apply_clique_reductions_wrt_top_level_weight_updates_considering_subset_reductions(ArrayList<Integer> last_top_level_weight_updated_colors, ArrayList<Integer> reserved_vertex_list)
	{		
// System.out.println("start to apply clique reductions wrt top_level_weight updates");

			for(int i : last_top_level_weight_updated_colors)
			{
				// obtain all vertices which are of degree(i)
				ArrayList<Integer> vertices_of_degree_i = dgr_based_partition.get_vertices_of_degree(i);

				// for each of these vertices
				for(int v : vertices_of_degree_i)
				{
					// determine whether it can be removed
					if(reserved_vertex_list.contains(v)) // not to break the subgraph in which we are enumerating cliques, avoid synchronization errors
					{
						continue;
					}
					if(cliq_search.get_top_level_weights_wrt_colors().get(i) <= vertices[v].get_weight()) // > must hold, not equal, avoid removing itself
					{
						continue;
					}

					remove_vertices_from_graph_because_of_shadow_reductions(v); // neighbors should be considered to delete!!!!!
// System.out.println(v + " is removed from graph by clique reductions wrt top_level_weight updates");
if(removed_vertex_list.size() % 10000 == 0)
{
	Date date = new Date();
	System.out.println("having removed " + removed_vertex_list.size() + " vertices at " + date + "******************************************************");
}
				}
			}
// System.out.println("clique reductions wrt top_level_weight updates completed.");			
//UsrPause.press_enter_to_continue();
	} 

	private void apply_clique_reductions_wrt_top_level_weight_updates_disregarding_subset_reductions(ArrayList<Integer> last_top_level_weight_updated_colors, ArrayList<Integer> reserved_vertex_list)
	{		
// System.out.println("start to apply clique reductions wrt top_level_weight updates");

			for(int i : last_top_level_weight_updated_colors)
			{
				// obtain all vertices which are of degree(i)
				ArrayList<Integer> vertices_of_degree_i = dgr_based_partition.get_vertices_of_degree(i);

				// for each of these vertices
				for(int v : vertices_of_degree_i)
				{
// System.out.println("considering removing " + v + " because of top-level weight updates");
					// determine whether it can be removed
					if(reserved_vertex_list.contains(v)) // not to break the subgraph in which we are enumerating cliques, avoid synchronization errors
					{
						continue;
					}
					if(cliq_search.get_top_level_weights_wrt_colors().get(i) <= vertices[v].get_weight()) // > must hold, not equal, avoid removing itself
					{
						continue;
					}

					remove_vertex_from_graph(v); // neighbors should be considered to delete!!!!!
					for(int nb : vertices[v].get_neighbors())
					{
						cand_remove_vertices_for_clique_reductions_wrt_degree_decrease.add(nb);
					}
// System.out.println(v + " has been removed from graph by clique reductions wrt top_level_weight updates");
if(removed_vertex_list.size() % 10000 == 0)
{
	Date date = new Date();
	System.out.println("having removed " + removed_vertex_list.size() + " vertices at " + date + "******************************************************");
}
				}
			}
// System.out.println("clique reductions wrt top_level_weight updates completed.");			
//UsrPause.press_enter_to_continue();
	} 

	private boolean apply_clique_reductions_wrt_degree_decrease_disregarding_subset_reductions()
	{		
// System.out.println("start to apply clique reductions wrt degree decrease");
		boolean clique_reductions_applied = false;
		while(!cand_remove_vertices_for_clique_reductions_wrt_degree_decrease.isEmpty())
		{
			int u = cand_remove_vertices_for_clique_reductions_wrt_degree_decrease.pollLast();
// System.out.println("considering " + u + " because of degree decrease");
			if(!remaining_vertex_set.contains(u))
			{
				continue;
			}
//System.out.println("considering " + u + " for clique reductions with degree " + vertices[u].get_degree());
//System.out.println("top_level_weights: " + cliq_search.get_top_level_weights_wrt_colors());
			if(cliq_search.get_top_level_weights_wrt_colors().size() <= vertices[u].get_degree()) // |C| must be bigger than d(u), otherwise continue
			{
				continue;
			}
			if(cliq_search.get_top_level_weights_wrt_colors().get(vertices[u].get_degree()) <= vertices[u].get_weight()) // > w(u) must hold, avoid removing itself
			{
				continue;
			}
			remove_vertex_from_graph(u); // neighbors should be considered to delete!!!!!
			for(int nb : vertices[u].get_neighbors())
			{
				cand_remove_vertices_for_clique_reductions_wrt_degree_decrease.add(nb);
			}
			clique_reductions_applied = true;
// System.out.println(u + " is removed from graph by degree decrease in clique reductions");
if(removed_vertex_list.size() % 10000 == 0)
{
	Date date = new Date();
	System.out.println("having removed " + removed_vertex_list.size() + " vertices at " + date + "******************************************************");
}
		} // while(!cand_remove_list.isEmpty())
// System.out.println("clique reductions completed wrt degree decrease.");
//UsrPause.press_enter_to_continue();
		return clique_reductions_applied;
	} // public void apply_clique_reductions_wrt_degree_decrease_considering_subset_reductions()

	private boolean apply_clique_reductions_wrt_degree_decrease_considering_subset_reductions()
	{		
// System.out.println("start to apply clique reductions wrt degree decrease");
		boolean clique_reductions_applied = false;
		while(!cand_remove_vertices_for_clique_reductions_wrt_degree_decrease.isEmpty())
		{
			int u = cand_remove_vertices_for_clique_reductions_wrt_degree_decrease.pollLast();
			if(!remaining_vertex_set.contains(u))
			{
				continue;
			}
//System.out.println("considering " + u + " for clique reductions with degree " + vertices[u].get_degree());
//System.out.println("top_level_weights: " + cliq_search.get_top_level_weights_wrt_colors());
			if(cliq_search.get_top_level_weights_wrt_colors().size() <= vertices[u].get_degree()) // |C| must be bigger than d(u), otherwise continue
			{
				continue;
			}
			if(cliq_search.get_top_level_weights_wrt_colors().get(vertices[u].get_degree()) <= vertices[u].get_weight()) // > w(u) must hold, avoid removing itself
			{
				continue;
			}
			remove_vertices_from_graph_because_of_shadow_reductions(u); // neighbors should be considered to delete!!!!!
			clique_reductions_applied = true;
// System.out.println(u + " is removed from graph by clique reductions");
if(removed_vertex_list.size() % 10000 == 0)
{
	Date date = new Date();
	System.out.println("having removed " + removed_vertex_list.size() + " vertices at " + date + "******************************************************");
}
		} // while(!cand_remove_list.isEmpty())
// System.out.println("clique reductions completed wrt degree decrease.");
//UsrPause.press_enter_to_continue();
		return clique_reductions_applied;
	} // public void apply_clique_reductions_wrt_degree_decrease_considering_subset_reductions()

	private void collect_cand_remove_vertices_and_edges_wrt_removal_of(int v)
	{
		//
		for(int nb : vertices[v].get_neighbors())
		{
			// clique reductions
			cand_remove_vertices_for_clique_reductions_wrt_degree_decrease.add(nb);
			// single-single
			cand_remove_vertices_for_single_single.add(nb);
			// double-double
			for(int adj_e_nb : vertices[nb].get_adj_edges())
			{
				cand_remove_edges_for_double_double.add(adj_e_nb);
			}
			// triple-double
			cand_remove_vertices_for_triple_double.add(nb);
			for(int dist_2_nb : vertices[nb].get_neighbors())
			{
				cand_remove_vertices_for_triple_double.add(dist_2_nb);
			}
			// quadruple-double
			for(int adj_e_nb : vertices[nb].get_adj_edges())
			{
				cand_remove_edges_for_quadruple_double.add(adj_e_nb);
			}
			// link-link
			cand_remove_vertices_for_link_link.add(nb);
			for(int dist_2_nb : vertices[nb].get_neighbors())
			{
				cand_remove_vertices_for_link_link.add(dist_2_nb);
			}
			// triple-triple
			for(int adj_e_nb : vertices[nb].get_adj_edges())
			{
				cand_remove_edges_for_triple_triple.add(adj_e_nb);
			}
		}
	}

	private void remove_vertices_from_graph_because_of_shadow_reductions(int... varargs)
	{
		for(int v : varargs)
		{
			remove_vertex_from_graph(v);
			collect_cand_remove_vertices_and_edges_wrt_removal_of(v);
		}
	}

	// single-single reductions	
	private boolean apply_single_single_coverage_dominance_reductions()
	// preconditions: 
	// postconditions: 
	{
//System.out.println("cand_remove_vertices_for_single_single.size() = " + cand_remove_vertices_for_single_single.size());
		boolean reductions_applied = false;
		while(!cand_remove_vertices_for_single_single.isEmpty())
		{
// if(cand_remove_vertices_for_single_single.size() % 1000 == 0) System.out.println("cand_remove_vertices_for_single_single.size() = " + cand_remove_vertices_for_single_single.size());
			// int x = cand_remove_vertices_for_single_single.get(cand_remove_vertices_for_single_single.size() - 1);
			// cand_remove_vertices_for_single_single.remove(cand_remove_vertices_for_single_single.size() - 1);
			int x = cand_remove_vertices_for_single_single.pollLast();
			if(!remaining_vertex_set.contains(x))
			{
				continue;
			}
			if(!nb_subset_search.is_single_single_dominated(x, vertices, new Adjacency(), weight_based_partition)) 
			{
				continue;
			}
			// subset relation holds, can safely remove x
			// nb_subset_reductions_applied = true;			
			remove_vertices_from_graph_because_of_shadow_reductions(x);
// System.out.println(x + " has been removed by single-single.");
			reductions_applied = true;
		} // while(!cand_remove_vertex_list.isEmpty())
		return reductions_applied;
	} // private void apply_single_single_coverage_dominance_reductions(ArrayList<Integer> cand_remove_vertex_list)

	private boolean apply_double_double_coverage_dominance_reductions()
	{
//System.out.println("cand_remove_edges_for_double_double.size() = " + cand_remove_edges_for_double_double.size());
		while(!cand_remove_edges_for_double_double.isEmpty())
		{
// if(cand_remove_edges_for_double_double.size() % 1000 == 0) System.out.println("cand_remove_edges_for_double_double.size() = " + cand_remove_edges_for_double_double.size());
			// int e = cand_remove_edges_for_double_double.get(cand_remove_edges_for_double_double.size() - 1);
			// cand_remove_edges_for_double_double.remove(cand_remove_edges_for_double_double.size() - 1);
			int e = cand_remove_edges_for_double_double.pollLast();
			if(!remaining_edge_set.contains(e))
			{
				continue;
			}

			int[] ia = new int[2];
			edges[e].get_vertices(ia);
			int x1 = ia[0], x2 = ia[1];
			// now we are sure that x1 and x2 are connected
			if(!nb_subset_search.is_double_double_dominated(x1, x2, vertices, new Adjacency(), weight_based_partition))
			{
				continue;
			}
			// subset relation holds, can safely remove x, y
			// nb_subset_reductions_applied = true;
			remove_vertices_from_graph_because_of_shadow_reductions(x1, x2);
			return true;
		} // while(!cand_remove_edge_list.isEmpty())
		return false;
	}

	private boolean apply_triple_double_coverage_dominance_reductions()
	// note: will take all input vertices as centers and considers possibilities
	{
//System.out.println("cand_remove_vertices_for_triple_double.size() = " + cand_remove_vertices_for_triple_double.size());
		while(!cand_remove_vertices_for_triple_double.isEmpty())
		{
// if(cand_remove_vertices_for_triple_double.size() % 1000 == 0) System.out.println("cand_remove_vertices_for_triple_double.size() = " + cand_remove_vertices_for_triple_double.size());
			// int x2 = cand_remove_vertices_for_triple_double.get(cand_remove_vertices_for_triple_double.size() - 1);
			// cand_remove_vertices_for_triple_double.remove(cand_remove_vertices_for_triple_double.size() - 1);
			int x2 = cand_remove_vertices_for_triple_double.pollLast();
			if(!remaining_vertex_set.contains(x2))
			{
				continue;
			}
			if(vertices[x2].get_degree() < 2) continue;

			//obtain_link_clinging_to_vertex_label: 
			for(int x1 : vertices[x2].get_neighbors())// note that we are obtaining a link rather than a triangle, the order matters
			{
				for(int x3 : vertices[x2].get_neighbors())
				{
					if(x3 <= x1) continue; // avoid repetitions
					if(is_connected(x1, x3)) continue; // x1 and x3 must not be connected
					// now we have obtain a 2-dist path x1-x2-x3 s.t. x1 and x3 are not connected
					if(!nb_subset_search.is_triple_double_dominated(x1, x2, x3, vertices, new Adjacency())) continue;
// System.out.println("about to remove " + x1 + ", " + x2 + ", " + x3 + "**************************************");
					// subset relation holds, can safely remove x1, x2, x3
					// nb_subset_reductions_applied = true;
					remove_vertices_from_graph_because_of_shadow_reductions(x1, x2, x3);
// System.out.println("about to break");
					// break obtain_link_clinging_to_vertex_label;
					return true;
				}
			} // for(int y : vertices[x1].get_neighbors())
// System.out.println("should have arrived here");
		} // while(!cand_remove_center_vertex_list.isEmpty())
		return false;
	}

	private boolean apply_quadruple_double_coverage_dominance_reductions()
	// note: will take all input edges and considers possibility
	{
//System.out.println("cand_remove_edges_for_quadruple_double.size() = " + cand_remove_edges_for_quadruple_double.size());
		while(!cand_remove_edges_for_quadruple_double.isEmpty())
		{
// if(cand_remove_edges_for_quadruple_double.size() % 1000 == 0) System.out.println("cand_remove_edges_for_quadruple_double.size() = " + cand_remove_edges_for_quadruple_double.size());
			// int e = cand_remove_edges_for_quadruple_double.get(cand_remove_edges_for_quadruple_double.size() - 1);
			// cand_remove_edges_for_quadruple_double.remove(cand_remove_edges_for_quadruple_double.size() - 1);
			int e = cand_remove_edges_for_quadruple_double.pollLast();
			if(!remaining_edge_set.contains(e))
				continue;
			
			int[] ia = new int[2];
			edges[e].get_vertices(ia);

			int x1 = ia[0], y1 = ia[1];
// System.out.println("build qua, starting from edge " + e + " with vertices " + x1 + " and " + y1);
			for(int nb_x1 : vertices[x1].get_neighbors())
			{
				if(nb_x1 == y1) continue; // avoid finding repeated
// System.out.println("having obtained " + nb_x1 + " as " + x1 + "\'s neighbor");
				for(int nb_y1 : vertices[y1].get_neighbors())
				{
					if(nb_y1 == x1) continue; // avoiding finding repeated
					if(nb_x1 == nb_y1) continue; // ensure that this is a qua.
// System.out.println("having obtained " + nb_y1 + " as " + y1 + "\'s neighbor");
// System.out.println("to check whether " + nb_x1 + " and " + nb_y1 + " are connected");
					if(!is_connected(nb_x1, nb_y1)) continue; // ensure connected
					if(is_connected(nb_x1, y1)) continue;
					if(is_connected(nb_y1, x1)) continue;
					int x2 = nb_y1, y2 = nb_x1;
					// having found a quadrilateral with edge e, no diagnals
					if(!nb_subset_search.is_quadruple_double_dominated(x1, x2, y1, y2, vertices, new Adjacency())) continue;
					// having found a quadruple-double pattern, about to remove vertices
					// nb_subset_reductions_applied = true;
					remove_vertices_from_graph_because_of_shadow_reductions(x1, x2, y1, y2);
					return true;
				}
			}
		} // while(!cand_remove_edges_for_quadruple_double.isEmpty())
		return false;
	}

	private boolean apply_link_link_coverage_dominance_reductions()
	// note: will take all input vertices as centers and considers possibilities
	{
		while(!cand_remove_vertices_for_link_link.isEmpty())
		{
			int x2 = cand_remove_vertices_for_link_link.pollLast();
			if(!remaining_vertex_set.contains(x2)) continue;

			if(vertices[x2].get_degree() < 2) continue; // must be of degree 2 at least

			// obtain_link_adhere_to_vertex_label: 
			for(int x1 : vertices[x2].get_neighbors())
			{
				for(int x3 : vertices[x2].get_neighbors())
				{
					if(x3 <= x1) continue; // must obtain different neighbors and x1 must be less, to avoid repetitions
					if(is_connected(x1, x3)) continue; // must not be connected, preconditions of link-link reductions
					// now a link is ready
					if(!nb_subset_search.is_link_link_dominated(x1, x2, x3, vertices, new Adjacency())) continue;
					// subset relation holds, ready to remove vertices
					// nb_subset_reductions_applied = true;
					// remove vertices
					remove_vertices_from_graph_because_of_shadow_reductions(x1, x2, x3);
					// break obtain_link_adhere_to_vertex_label; // x2 has been removed, should obtain another center
					return true;
				} // for(int x3 : vertices[x2].get_neighbors())
			} // for(int x1 : vertices[x2].get_neighbors())			
		} // while(!cand_remove_vertex_list.isEmpty())
		return false;
	}

	// private void apply_triple_triple_coverage_dominance_reductions(ArrayList<Integer> cand_remove_vertex_list)
	private boolean apply_triple_triple_coverage_dominance_reductions()
	// note: will take all input edges and considers possibilities
	{
//System.out.println("cand_remove_edges_for_triple_triple.size() = " + cand_remove_edges_for_triple_triple.size());
		while(!cand_remove_edges_for_triple_triple.isEmpty())
		{
//if(cand_remove_edges_for_triple_triple.size() % 1000 == 0) System.out.println("cand_remove_edges_for_triple_triple.size() = " + cand_remove_edges_for_triple_triple.size());
			// int e = cand_remove_edges_for_triple_triple.get(cand_remove_edges_for_triple_triple.size() - 1);
			// cand_remove_edges_for_triple_triple.remove(cand_remove_edges_for_triple_triple.size() - 1);
			int e = cand_remove_edges_for_triple_triple.pollLast();
			if(!remaining_edge_set.contains(e)) continue;

			int[] ia = new int[2];
			edges[e].get_vertices(ia);
			int x1 = ia[0], x2 = ia[1];

			for(int nb_x1 : vertices[x1].get_neighbors())
			{
				if(nb_x1 < x1 || nb_x1 < x2) continue; // apply this order constraint to avoid repetitive efforts
				if(nb_x1 == x2) continue; // different vertices so as to form a triangle
				if(!is_connected(nb_x1, x2)) continue; // must form a triangle
				int x3 = nb_x1;

				// now a triangle is ready
				if(!nb_subset_search.is_triple_triple_dominated(x1, x2, x3, vertices, new Adjacency(), weight_based_partition)) continue; 
				// subset reation holds, ready to remove vertices
				// nb_subset_reductions_applied = true;
				// remove vertices
				remove_vertices_from_graph_because_of_shadow_reductions(x1, x2, x3);
				return true;
			} // for(int nb_x1 : vertices[x1].get_neighbors())
		}
		return false;
	}

	public void apply_shadow_reductions()
	{
		cand_remove_vertices_for_single_single = new TreeSet<Integer>(remaining_vertex_set);
		cand_remove_edges_for_double_double = new TreeSet<Integer>(remaining_edge_set);
		cand_remove_vertices_for_triple_double = new TreeSet<Integer>(remaining_vertex_set);
		cand_remove_edges_for_quadruple_double = new TreeSet<Integer>(remaining_edge_set);
		cand_remove_vertices_for_link_link = new TreeSet<Integer>(remaining_vertex_set);
		cand_remove_edges_for_triple_triple = new TreeSet<Integer>(remaining_edge_set);

		boolean reductions_applicable = false;
		do{
			// boolean clique_reductions_wrt_degree_decrease_applied = false;
			boolean neighborhood_subset_reductions_applicable = false;
			// boolean post_reductions_applied = false;
			reductions_applicable = false;

			// clique_reductions_wrt_degree_decrease_applied = apply_clique_reductions_wrt_degree_decrease_considering_subset_reductions();
			// reductions_applied |= clique_reductions_wrt_degree_decrease_applied;
// System.out.println("clique reductions completed.");
			neighborhood_subset_reductions_applicable = apply_single_single_coverage_dominance_reductions();
			reductions_applicable |= neighborhood_subset_reductions_applicable;
System.out.println("single-single reductions completed.");
System.out.println("reductions applicable: " + reductions_applicable);

			if(neighborhood_subset_reductions_applicable == true) continue;
			neighborhood_subset_reductions_applicable = apply_double_double_coverage_dominance_reductions();
			reductions_applicable |= neighborhood_subset_reductions_applicable;
System.out.println("double-double reductions completed.");
System.out.println("reductions applicable: " + reductions_applicable);

			if(neighborhood_subset_reductions_applicable == true) continue;
			neighborhood_subset_reductions_applicable = apply_triple_double_coverage_dominance_reductions();
			reductions_applicable |= neighborhood_subset_reductions_applicable;
System.out.println("triple-double reductions completed.");
System.out.println("reductions applicable: " + reductions_applicable);

			if(neighborhood_subset_reductions_applicable == true) continue;
			neighborhood_subset_reductions_applicable = apply_quadruple_double_coverage_dominance_reductions(); 
			reductions_applicable |= neighborhood_subset_reductions_applicable;
System.out.println("quadruple-double reductions completed.");
System.out.println("reductions applicable: " + reductions_applicable);

			if(neighborhood_subset_reductions_applicable == true) continue;
			neighborhood_subset_reductions_applicable = apply_link_link_coverage_dominance_reductions();
			reductions_applicable |= neighborhood_subset_reductions_applicable;
System.out.println("link-link reductions completed.");
System.out.println("reductions applicable: " + reductions_applicable);

			if(neighborhood_subset_reductions_applicable == true) continue;
			neighborhood_subset_reductions_applicable = apply_triple_triple_coverage_dominance_reductions();
			reductions_applicable |= neighborhood_subset_reductions_applicable;
System.out.println("triple-triple reductions completed.");
System.out.println("reductions applicable: " + reductions_applicable);
		}while(reductions_applicable);

System.out.println("All shadow reductions have ended.");
System.out.println("remaining_vertex_num: " + remaining_vertex_set.size());
	}

	private boolean is_absored_by_another_clique(int v, ArrayList<Integer> clq)
	{
		if(clq.contains(v)) return false;
		if(clq.size() < vertices[v].get_degree() + 1) return false;
		if(vertices[clq.get(vertices[v].get_degree())].get_weight() < vertices[v].get_weight()) return false;
// System.out.println(v + ", of degree " + vertices[v].get_degree() + ", is absorbed by " + clq.get(vertices[v].get_degree()) + " in a clique: " + clq);
// System.out.println("its neighbors are: "); for(int nb : vertices[v].get_neighbors()) System.out.print(nb + "\t"); System.out.println();
// UsrPause.press_enter_to_continue();
		return true;
	}

	public boolean apply_post_reductions()
	{
		boolean post_reductions_applied = false;
		// ArrayList<Integer> cand_remove_vertex_list = new ArrayList<Integer>(remaining_vertex_list);
		cand_remove_vertices_for_clique_reductions_wrt_degree_decrease = new TreeSet<Integer>(remaining_vertex_set);
		// for(int v : cand_remove_vertex_list)
		while(!cand_remove_vertices_for_clique_reductions_wrt_degree_decrease.isEmpty())
		{
			int v = cand_remove_vertices_for_clique_reductions_wrt_degree_decrease.pollLast();
			
			if(!remaining_vertex_set.contains(v)) continue;

			for(ArrayList<Integer> critical_clq : critical_clique_list)
			{
				if(is_absored_by_another_clique(v, critical_clq))
				{
					remove_vertices_from_graph_because_of_shadow_reductions(v); // this will cause degree decrease and possible further reductions
// System.out.println(v + " has been removed in post reductions.");
					post_reductions_applied = true;
					for(int nb : vertices[v].get_neighbors())
					{
// System.out.println("add neighbor for further reductions: " + nb);
						cand_remove_vertices_for_clique_reductions_wrt_degree_decrease.add(nb);
					}

					break;
				}
			}
		}
// System.out.println("remaining vertex count: " + remaining_vertex_set.size());
		return post_reductions_applied;
	}
	
	public void show_vertex_info_and_top_level_weights()
	{
		System.out.println("cliq_search.get_top_level_weights_wrt_colors(): (size " + cliq_search.get_top_level_weights_wrt_colors().size() + ") " + cliq_search.get_top_level_weights_wrt_colors());
		// for(int v = 1; v <= get_vertex_num(); v++)
		for(int v : remaining_vertex_set)
		{
			// int v = remaining_vertex_list.get(i);
			int d = vertices[v].get_degree();
/*
			System.out.print("vertex " + v + ", degree: " + d + ", weight: " + vertices[v].get_weight() + ", top_level_weight at Color " + d);
			if(d < cliq_search.get_top_level_weights_wrt_colors().size())
				System.out.print(": " + cliq_search.get_top_level_weights_wrt_colors().get(d));
			else
				System.out.print(": 0");
			System.out.println();
*/
			if(d < cliq_search.get_top_level_weights_wrt_colors().size() && vertices[v].get_weight() <= cliq_search.get_top_level_weights_wrt_colors().get(d))
				System.out.println("it should have been considered to be removed.......................................");
		}
	}

} // public class WGCReducedGraph extends DynamicSimpleUndirectedVertexWeightedGraph
