package CliqueSearchPack;

import java.util.*;
import java.lang.StrictMath;

import GraphPack.WeightedVertexPack.WeightedVertex;
import InterfacePack.IntRelate;
import TwoParametersInterfacePack.TwoParametersBehavior;
import ZeroParameterInterfacePack.ZeroParameterBehavior;
import DegreeBucketPack.DegreeBasedPartition;
import WeightBucketPack.WeightBasedPartition;



public class CliqueSearch{
	private Random rand;
	private ArrayList<Integer> top_level_weights_wrt_colors;	// allow Color 0
	private ArrayList<Integer> last_top_level_weight_updated_colors; // those colors whose top-level weight was updated last time
	private ArrayList<Boolean> can_exist_clique_above_at_color;
	private ArrayList<ArrayList<Integer>> allowed_later_sampling_vertices_wrt_colors;
	int max_degree;
	long step;
	long further_search_step;
	long cut_off_step;
	long step_period;
	Date date;
	long start_elap_milliseconds_from_past;
	long time_limit;
	boolean is_time_out;

	// constructor
	public CliqueSearch(int vertex_num, int max_d, int seed, long start_elap_ms, long run_time_cut_off)
	{
		rand = new Random(seed);
		max_degree = max_d;
		step = 0;
		further_search_step = 10000 * max_degree;
		cut_off_step = further_search_step;
		// step_period = 1000000;
		step_period = 10000;
		date = new Date();
		start_elap_milliseconds_from_past = start_elap_ms;
		time_limit = run_time_cut_off;
		is_time_out = false; // to record whether an enumeration procedure is stopped by the cutoff
		top_level_weights_wrt_colors = new ArrayList<Integer>();
		last_top_level_weight_updated_colors = new ArrayList<Integer>();
		can_exist_clique_above_at_color = new ArrayList<Boolean>();
		
		// for later improvement of top level weights
		allowed_later_sampling_vertices_wrt_colors = new ArrayList<ArrayList<Integer>>();
	}

	public ArrayList<Integer> get_top_level_weights_wrt_colors()
	{
		return top_level_weights_wrt_colors;
	}
	
	public ArrayList<Boolean> get_can_exist_clique_above_at_color()
	{
		return can_exist_clique_above_at_color;
	}
	
	public Random get_rand()
	{
		return rand;
	}

	public void update_top_level_weights_wrt_new_critical_cliques(ArrayList<ArrayList<Integer>> critical_clique_list, TreeSet<Integer> cand_remove_vertices_for_clique_reductions_wrt_degree_decrease, WeightedVertex[] vertices, IntRelate int_relate_ob, ZeroParameterBehavior zero_param_behav_ob, TwoParametersBehavior two_param_behav_ob, WeightBasedPartition weight_based_partition) // 
	{
		for(int i = 0; i <= top_level_weights_wrt_colors.size(); i++) // need to consider one more color, because there may be cliques bigger than the size of top_level_weights
		{
System.out.println("update top level weights wrt color " + (i + 1));
			// focus on the (i+1)-th color, an existing color
			if(i < top_level_weights_wrt_colors.size())
			// only consider updating, not consider inserting
			{
				/* if the top-level weight of the current color is equal to that of the former, 
				and the top-level weight of the former color failed to be updated, 
				then the top-level weight of the current color will not be tried to update, 
				regardless of whether whether it is sure at the theoretical level
				 */
				if(i > 0 && top_level_weights_wrt_colors.get(i) == top_level_weights_wrt_colors.get(i-1)) // the top level weight of the current color is equal to that of the former
				{
						if(!can_exist_clique_above_at_color.get(i-1)) // the top-level weight of the former color failed to be updated at the theoretical level
						{
// System.out.println("set can_exist_clique of color " + i + " to be false");
							can_exist_clique_above_at_color.set(i, false);
							// no later sampling for this color is needed
							allowed_later_sampling_vertices_wrt_colors.set(i, new ArrayList<Integer>());
							continue;
						}

System.out.println("the top-level weight of the former color, i.e., the " + (i) + "-th color, failed to be updated,");
System.out.println("which implies that the top-level weight of this color, i.e., the " + (i+1) + "-th color will not (continue to) be updated either, since the weights are equal to each other\n");

						// fail to be updated but not at the theoretical level
						// collect vertices with weights greater than this top-level weight, hope to increase this top-level weight
						allowed_later_sampling_vertices_wrt_colors.set(i, weight_based_partition.get_vertex_list_of_weight_at_least(top_level_weights_wrt_colors.get(i) + 1));
						continue;
				}
			}

			// may not be an existing color, or may need to further check whether better cliques exist
			
			ArrayList<Integer> cand_vertices;
			
			// prepare cand_vertices and lower_bound for i
			if(i == top_level_weights_wrt_colors.size())
				cand_vertices = new ArrayList<Integer>(weight_based_partition.get_vertex_list_of_weight_at_least(1));
			else
				cand_vertices = weight_based_partition.get_vertex_list_of_weight_at_least(top_level_weights_wrt_colors.get(i) + 1);

			// during clique enumeration, cand_vertices will be changed
			ArrayList<Integer> cand_vertices_for_backup = new ArrayList<>(cand_vertices);

			int lower_bound_for_requested_clique = i + 1; // we should find a clique of at least this size, otherwise cannot improve this upper bound
			if(cand_vertices.size() < lower_bound_for_requested_clique) // no hope
			{
				can_exist_clique_above_at_color.set(i, false);
				allowed_later_sampling_vertices_wrt_colors.set(i, new ArrayList<Integer>());
				continue;
			}
			
			
			boolean[] enumeration_completed_arr = new boolean[1]; // to record whether depth-first enumeration is completed
			boolean lower_bound_improved = update_top_level_weights_wrt_new_critical_cliques_in_vertex_list(new LinkedList<>(cand_vertices), lower_bound_for_requested_clique, critical_clique_list, vertices, int_relate_ob, two_param_behav_ob, enumeration_completed_arr, weight_based_partition);
			
			if(is_time_out) return;
			
			boolean OK = zero_param_behav_ob.behavior(); // 
// System.out.println("enumeration_completed: " + enumeration_completed_arr[0]);
			if(i < top_level_weights_wrt_colors.size())
			{
				if(enumeration_completed_arr[0])
				{
					can_exist_clique_above_at_color.set(i, false);
					allowed_later_sampling_vertices_wrt_colors.set(i, new ArrayList<Integer>());
				}
				if(can_exist_clique_above_at_color.get(i))
				{
// System.out.println("collect vertices for later sampling");
					allowed_later_sampling_vertices_wrt_colors.set(i, new ArrayList<>(cand_vertices_for_backup));	
				}
			}
			else // i == top_level_weights_wrt_colors.size() which implies no improvement on the lower bound of the (i+1)-th color, 
				// otherwise, top_level_weights_wrt_colors.size() would be increased and i < top_level_weights_wrt_colors.size() would hold
			{
				if(enumeration_completed_arr[0])
				{
					can_exist_clique_above_at_color.add(Boolean.valueOf(false));
					if(lower_bound_improved)
						allowed_later_sampling_vertices_wrt_colors.set(i, new ArrayList<Integer>());
					else
						allowed_later_sampling_vertices_wrt_colors.add(new ArrayList<Integer>());
				}
				else
				{
					can_exist_clique_above_at_color.add(Boolean.valueOf(true));
// System.out.println("collect vertices for later sampling");
					allowed_later_sampling_vertices_wrt_colors.add(new ArrayList<>(cand_vertices_for_backup));	
				}
			}
			
			if(lower_bound_improved)
			{
				// top_level_weight_wrt_colors_updated_bits.set(i);
				i--; // in order to reconsider the current color
			}
// UsrPause.press_enter_to_continue();
		}
	}
	
	public void update_top_level_weights_wrt_enumerated_cliques_adhere_to_vertex_list(ArrayList<ArrayList<Integer>> critical_clique_list, TreeSet<Integer> cand_remove_vertices_for_clique_reductions_wrt_degree_decrease, WeightedVertex[] vertices, ArrayList<Integer> starting_vertex_list, IntRelate int_relate_ob, ZeroParameterBehavior zero_param_behav_ob, TwoParametersBehavior two_param_behav_ob, WeightBasedPartition weight_based_partition) // apply clique reductions after each enumeration
	// zero_param_behav_ob: apply_clique_reductions_wrt_degree_decrease_disregarding_subset_reductions()
	// two_param_behav_ob: apply_clique_reductions_wrt_top_level_weight_updates_disregarding_subset_reductions()
	{
		for(int v : starting_vertex_list)
		{
// System.out.println("as to the " + (starting_vertex_list.indexOf(v) + 1) + "-th vertex " + v + " from the top weight/degree list++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			update_top_level_weights_wrt_enumerated_cliques_adhere_to_given_vertex(v, critical_clique_list, vertices, int_relate_ob, two_param_behav_ob, weight_based_partition);
			
			if(is_time_out) return;
			// add those vertices which were not considered in the previous clique enumeration procedure

			ArrayList<Integer> cand_vertices = new ArrayList<Integer>(vertices[v].get_neighbors());			
			cand_remove_vertices_for_clique_reductions_wrt_degree_decrease.addAll(cand_vertices);
			cand_remove_vertices_for_clique_reductions_wrt_degree_decrease.add(v);
			boolean OK = zero_param_behav_ob.behavior();
// UsrPause.press_enter_to_continue();
		}
	}

	private boolean update_top_level_weights_wrt_new_critical_cliques_in_vertex_list(LinkedList<Integer> cand_vertices, int lower_bound_for_requested_clique, ArrayList<ArrayList<Integer>> critical_clique_list, WeightedVertex[] vertices, IntRelate int_relate_ob, TwoParametersBehavior two_param_behav_ob, boolean[] enumeration_completed_arr, WeightBasedPartition weight_based_partition)
	// int_relate_ob: is_connected
	{
System.out.println("start to look for cliques containing at least " + lower_bound_for_requested_clique + " vertices, dfs");
		ArrayDeque<Integer> decision_vertex_stack = new ArrayDeque<Integer>();
		ArrayDeque<Integer> excluded_vertex_stack = new ArrayDeque<Integer>();
		cut_off_step = step + further_search_step;
		enumeration_completed_arr[0] = false; // enumeration not started yet, so not completed
		Collections.shuffle(cand_vertices);
		// enumerating cliques, 
		// the first branch is the one that excludes all vertices which does not connect to all other remaining vertices
		while(true)	
		{
			// shrink cand_vertices to obtain a clique
			int v_to_exclude = 0;
			obtain_undesired_vertex_label:
			// no need to deal with the special case when cand_vertices.size() == 1
			for(int x1 : cand_vertices)
			{
				for(int x2 : cand_vertices)
				{
					if(x1 == x2) continue; 
					if(!int_relate_ob.related(x1, x2)) // x1 and x2 is not connected
					{
						v_to_exclude = x1; // having obtained a vertex not connected to all others
						break obtain_undesired_vertex_label;
					} 
				}
			}
			
			// if cand_vertices is itself a clique, this the condition below will be false, 
			// no decision variables, no further excluding, no recovering needed
			if(v_to_exclude != 0) // at least one vertex has to be excluded in order for a clique
			{
				// for making a decision, i.e., choosing one branch			
				decision_vertex_stack.push(v_to_exclude); 
				// the first tested brach is the one that excludes all vertices which does not connect to all other remaining vertices
				cand_vertices.remove(Integer.valueOf(v_to_exclude));
				excluded_vertex_stack.push(v_to_exclude);
				
				step++;
				if(step % step_period == 0)
				{
					date = new Date();
					long end_elap_milliseconds_from_past = date.getTime();
					long elap_time = end_elap_milliseconds_from_past - start_elap_milliseconds_from_past;
					if(elap_time > time_limit)
					{
						System.out.println("Time out of the program-------------------------------");
						is_time_out = true;
						return false;
					}
				}
				
				if(step > cut_off_step) 
				{
					// step = 0;
					cut_off_step = step + further_search_step;
System.out.println("time out and stop enumerating cliques for lifting weight of the current color--------------------");
					return false;
				}
				// if the following condition does not hold, no further excluding because of no possibility for a better clique
				if(cand_vertices.size() >= lower_bound_for_requested_clique) // there is still an opportunity for this branch to trigger reductions, still promising, 
																			 // otherwise no further probing or excluding
				{
					continue; // (1) having not obtained a clique, (2) still exists opportunity to trigger reductions -->> continue to exclude vertices
				}
				else
				{
				}
			}
			else
			{
			}


			// after excluding vertices, a small clique could be obtained
			// v_to_exclude == 0 means we have obtained a clique (no vertices needed to be removed to obtain a clique)
			if(v_to_exclude == 0 && cand_vertices.size() >= lower_bound_for_requested_clique) // exists a clque which improves top-level weights
			{
				// utilize this clique
				ArrayList<Integer> cand_clique = new ArrayList<Integer>(cand_vertices);
/*
				Collections.sort(cand_clique, new Comparator<Integer>(){
					public int compare(Integer n1, Integer n2){
						if(vertices[n1].get_weight() < vertices[n2].get_weight() || (vertices[n1].get_weight() == vertices[n2].get_weight() && n1 < n2)){
							return 1;
						}
						else{
							return -1;
						}
					}
				});
*/
				boolean lower_bound_improved = update_top_level_weights_wrt_one_enumerated_clique(critical_clique_list, cand_clique, vertices, weight_based_partition); // I think it must return true
				if(lower_bound_improved)
				{
					// apply_clique_reductions_wrt_top_level_weight_updates
					two_param_behav_ob.behavior(last_top_level_weight_updated_colors, new ArrayList<Integer>());
					// step = 0;
					cut_off_step = step + further_search_step;
System.out.println("complete reductions");
// System.out.println("step, cut_off_step: " + step + ", " + cut_off_step);
System.out.println("top_level_weights_wrt_vertices: " + top_level_weights_wrt_colors);
					return true;
				}				
			}

			do{
				// below change the last decision
				if(decision_vertex_stack.isEmpty()) // all branches covered
				{
					enumeration_completed_arr[0] = true;
/*
System.out.println("all branches covered, enumerations completed********");
System.out.println("completed at step: " + step);
*/
					// step = 0;
					cut_off_step = step + further_search_step;
					return false;
				}
				int decision_v = decision_vertex_stack.pop(); // erase last decision		
				// back to a decision point
				int v_to_include;
				do{
					v_to_include = excluded_vertex_stack.pop();
					cand_vertices.add(v_to_include);
				}while(v_to_include != decision_v);
				// enter "the" other branch
				ArrayList<Integer> removed_list = new ArrayList<Integer>();
				for(int x : cand_vertices)
				{
					if(x == decision_v)
					{
						continue;
					}
					if(int_relate_ob.related(x, decision_v)) // x and decision_v is connected
					{
						continue;
					}
					removed_list.add(x);
				}
				for(int x : removed_list)
				{
					cand_vertices.remove(Integer.valueOf(x)); 
					excluded_vertex_stack.push(x);
				}
			}while(cand_vertices.size() < lower_bound_for_requested_clique); // obtain a promising branch
		} // while(true)
	}
	
	private void update_top_level_weights_wrt_enumerated_cliques_adhere_to_given_vertex(int u, ArrayList<ArrayList<Integer>> critical_clique_list, WeightedVertex[] vertices, IntRelate int_relate_ob, TwoParametersBehavior two_param_behav_ob, WeightBasedPartition weight_based_partition)
	{
		ArrayList<Integer> cand_vertices = new ArrayList<Integer>(vertices[u].get_neighbors());
		ArrayList<Integer> reserved_vertex_list = new ArrayList<Integer>(cand_vertices);
		reserved_vertex_list.add(u);
		ArrayDeque<Integer> decision_vertex_stack = new ArrayDeque<Integer>();
		ArrayDeque<Integer> excluded_vertex_stack = new ArrayDeque<Integer>();
		Collections.shuffle(cand_vertices);
		// step = 0;
		cut_off_step = step + further_search_step;
		while(true)	
		{
			// shrink cand_vertices to obtain a clique
			int v_to_exclude = 0;
			
			// no need to deal with the special case when cand_vertices.size() == 1
			obtain_undesired_vertex_label:
			for(int x1 : cand_vertices)
			{
				for(int x2 : cand_vertices)
				{
					if(x1 == x2) continue;
					if(!int_relate_ob.related(x1, x2))
					{
						v_to_exclude = x1; 
						break obtain_undesired_vertex_label;
					} 
				}
			}
			if(v_to_exclude != 0)
			{ 
				// make a decision, i.e., choose one branch			
				decision_vertex_stack.push(v_to_exclude); 
				cand_vertices.remove(Integer.valueOf(v_to_exclude));
				excluded_vertex_stack.push(v_to_exclude);
				step++;
				if(step % step_period == 0)
				{
					date = new Date();
					long end_elap_milliseconds_from_past = date.getTime();
					long elap_time = end_elap_milliseconds_from_past - start_elap_milliseconds_from_past;
					if(elap_time > time_limit)
					{
						System.out.println("Time out of the program-------------------------------");
						is_time_out = true;
						return;
					}
				}
				if(step > cut_off_step) 
				{
					// step = 0;
					cut_off_step = step + further_search_step; // reset cut_off_step
					return;
				}
				continue;
			}

			// a maximal clique found
			// utilize this clique
			ArrayList<Integer> cand_clique = new ArrayList<Integer>(cand_vertices);
			cand_clique.add(u);
// System.out.println("step, cut_off_step: " + step + ", " + cut_off_step);
// System.out.println("having obtained a maximal clique of size " + cand_clique.size() + "**, decision_stack_size: " + decision_vertex_stack.size());
			boolean lower_bound_improved = update_top_level_weights_wrt_one_enumerated_clique(critical_clique_list, cand_clique, vertices, weight_based_partition);
			if(lower_bound_improved)
			{
// System.out.println("step, cut_off_step: " + step + ", " + cut_off_step);
// System.out.println("start reductions");
				// apply clique reductions
				two_param_behav_ob.behavior(last_top_level_weight_updated_colors, reserved_vertex_list);
				// step = 0;
				cut_off_step = step + further_search_step;
System.out.println("top_level_weights_wrt_vertices: " + top_level_weights_wrt_colors);
System.out.println("complete reductions");
// System.out.println("step, cut_off_step: " + step + ", " + cut_off_step);
				
// System.out.println("complete reductions");
// System.out.println("top_level_weights_wrt_colors: " + top_level_weights_wrt_colors);
// UsrPause.press_enter_to_continue();
			}
			// below change the last decision
			if(decision_vertex_stack.isEmpty())
			{
// System.out.println("enumerations completed");
				// step = 0;
				cut_off_step = step + further_search_step; // reset cut_off_step
				return;
			}
			int decision_v = decision_vertex_stack.pop(); // erase last decision		
			// back to a decision point
			int v_to_include;
			do{
				v_to_include = excluded_vertex_stack.pop();
				cand_vertices.add(v_to_include);
			}while(v_to_include != decision_v);
			// enter the other branch
			ArrayList<Integer> removed_list = new ArrayList<Integer>();
			for(int x : cand_vertices)
			{
				if(x == decision_v)
				{
					continue;
				}
				if(int_relate_ob.related(x, decision_v))
				{
					continue;
				}
				removed_list.add(x);
			}
			for(int x : removed_list)
			{
				cand_vertices.remove(Integer.valueOf(x)); 
				excluded_vertex_stack.push(x);
			}
		} // while(true)
	}
	
	public boolean update_top_level_weights_wrt_vertex_weight_decrease_clique(ArrayList<Integer> cand_clique, WeightedVertex[] vertices, WeightBasedPartition weight_based_partition)
	{
// System.out.println("current top_level_weights_wrt_colors: " + top_level_weights_wrt_colors);
		last_top_level_weight_updated_colors.clear();

		// update top_level_weights
		boolean lower_bound_improved = false;
		for(int i = 0; i < cand_clique.size(); i++)
		{
			int v = cand_clique.get(i);
			if(i < top_level_weights_wrt_colors.size())
			{
				if(top_level_weights_wrt_colors.get(i) < vertices[v].get_weight())
				{
					top_level_weights_wrt_colors.set(i, vertices[v].get_weight());
					allowed_later_sampling_vertices_wrt_colors.set(i, weight_based_partition.get_vertex_list_of_weight_at_least(vertices[v].get_weight() + 1));
					if(i > 0 && vertices[v].get_weight() == top_level_weights_wrt_colors.get(i-1) && !can_exist_clique_above_at_color.get(i-1))
					{
						can_exist_clique_above_at_color.set(i, false);
						allowed_later_sampling_vertices_wrt_colors.set(i, new ArrayList<Integer>());
					}
					last_top_level_weight_updated_colors.add(i);
					lower_bound_improved = true;
				}
			}
			else
			{
				top_level_weights_wrt_colors.add(vertices[v].get_weight());
				can_exist_clique_above_at_color.add(Boolean.valueOf(true));
				allowed_later_sampling_vertices_wrt_colors.add(weight_based_partition.get_vertex_list_of_weight_at_least(vertices[v].get_weight() + 1));
				if(i > 0 && vertices[v].get_weight() == top_level_weights_wrt_colors.get(i-1) && !can_exist_clique_above_at_color.get(i-1))
				{
					can_exist_clique_above_at_color.set(i, false);
					allowed_later_sampling_vertices_wrt_colors.set(i, new ArrayList<Integer>());
				}
				last_top_level_weight_updated_colors.add(i);
				lower_bound_improved = true;
			}
		}

		return lower_bound_improved;		
	}

	public boolean update_top_level_weights_wrt_vertex_weight_decrease_clique_and_maintain_critical_clique_list(ArrayList<ArrayList<Integer>> critical_clique_list, ArrayList<Integer> cand_clique, WeightedVertex[] vertices, WeightBasedPartition weight_based_partition)
	// preconditions: cand_clique must be sorted, vertex weight must decrease
	// return true of lower-bound actually improved, false otherwise
	{
		last_top_level_weight_updated_colors.clear();

		// update top_level_weights
		boolean lower_bound_improved = false;
		for(int i = 0; i < cand_clique.size(); i++)
		{
			int v = cand_clique.get(i);
			if(i < top_level_weights_wrt_colors.size())
			{
				if(top_level_weights_wrt_colors.get(i) < vertices[v].get_weight())
				{
					top_level_weights_wrt_colors.set(i, vertices[v].get_weight());
					allowed_later_sampling_vertices_wrt_colors.set(i, weight_based_partition.get_vertex_list_of_weight_at_least(vertices[v].get_weight() + 1));
					if(i > 0 && vertices[v].get_weight() == top_level_weights_wrt_colors.get(i-1) && !can_exist_clique_above_at_color.get(i-1))
					{
						can_exist_clique_above_at_color.set(i, false);
						allowed_later_sampling_vertices_wrt_colors.set(i, new ArrayList<Integer>());
					}
					last_top_level_weight_updated_colors.add(i);
					lower_bound_improved = true;
				}
			}
			else
			{
				top_level_weights_wrt_colors.add(vertices[v].get_weight());
				can_exist_clique_above_at_color.add(Boolean.valueOf(true));
				allowed_later_sampling_vertices_wrt_colors.add(weight_based_partition.get_vertex_list_of_weight_at_least(vertices[v].get_weight() + 1));
				if(i > 0 && vertices[v].get_weight() == top_level_weights_wrt_colors.get(i-1) && !can_exist_clique_above_at_color.get(i-1))
				{
					can_exist_clique_above_at_color.set(i, false);
					allowed_later_sampling_vertices_wrt_colors.set(i, new ArrayList<Integer>());
				}
				last_top_level_weight_updated_colors.add(i);
				lower_bound_improved = true;
// System.out.println("insert a new weight: " + vertices[v].get_weight() + " into top level weights");
			}
		}
		if(lower_bound_improved)
		{
			// delete redundant critical cliques
			for(int i = critical_clique_list.size() - 1; i >= 0; i--)
			{
				// ArrayList<Integer> critical_clq = critical_clique_list.get(i);
				if(sorted_clique_dominate_eq(cand_clique, critical_clique_list.get(i), vertices))
				{
// System.out.println("about to delete a clique because of being dominated: " + critical_clique_list.get(i));
					critical_clique_list.remove(i);// delete worse cliques
// System.out.println("after deletion, critical_clique_list: " + critical_clique_list);
				}
			}
			for(int i = critical_clique_list.size() - 1; i >= 0; i--)
			{
				if(!sorted_clique_intersect_with_top_level_weights(critical_clique_list.get(i), vertices))
				{
// System.out.println("about to delete a clique because of strictly below top-level weights: " + critical_clique_list.get(i));
					critical_clique_list.remove(i);// delete redundant cliques
// System.out.println("after deletion, critical_clique_list: " + critical_clique_list);
				}
			}
		}
		
		// place cand_clique into critical_clique_list
		// currently I think the conditions below must hold...
		if(!sorted_clique_strictly_below_top_level_weights(cand_clique, vertices) && !sorted_clique_covered_by_critical_clique_list(cand_clique, critical_clique_list, vertices))
		{

			critical_clique_list.add(cand_clique); // insert better ones
		}

		return lower_bound_improved;
	}

	boolean sorted_clique_covered_by_critical_clique_list(ArrayList<Integer> cand_clique, ArrayList<ArrayList<Integer>> critical_clique_list, WeightedVertex[] vertices)
	{
		for(int i = 0; i < critical_clique_list.size(); i++)
		{
			if(sorted_clique_dominate_eq(critical_clique_list.get(i), cand_clique, vertices))
				return true;
		}
		return false;
	}

	boolean sorted_clique_strictly_below_top_level_weights(ArrayList<Integer> tested_clique, WeightedVertex[] vertices)
	// preconditions: tested_clique should be sorted, vertex weights from big to small
	{
		if(tested_clique.size() > top_level_weights_wrt_colors.size()) return false;
		for(int i = 0; i < tested_clique.size(); i++)
			if(vertices[tested_clique.get(i)].get_weight() >= top_level_weights_wrt_colors.get(i)) return false;
		return true;
	}

	boolean sorted_clique_intersect_with_top_level_weights(ArrayList<Integer> tested_clique, WeightedVertex[] vertices)
	// preconditions: tested_clique should be sorted, vertex weights from big to small
	// preconditions: |C| <= |top_level_weights|
	{
		for(int i = 0; i < tested_clique.size(); i++)
		{
			if(vertices[tested_clique.get(i)].get_weight() == top_level_weights_wrt_colors.get(i)) return true;
		}
		return false;
	}

	//
	boolean sorted_clique_dominate_eq(ArrayList<Integer> clq_1, ArrayList<Integer> clq_2, WeightedVertex[] vertices)
	// preconditions: both cliques should be sorted, vertex weights from big to small
	{
		// if(clq_1.equals(clq_2)) return false;
		if(clq_1.size() < clq_2.size()) return false;
		for(int i = 0; i < clq_2.size(); i++)
		{
			if(vertices[clq_1.get(i)].get_weight() < vertices[clq_2.get(i)].get_weight())
			{
				return false;
			}
		}
		return true;		
	}
	// public void update_top_level_weights_wrt_degree_greedy_clique(ArrayList<Integer> top_level_weights_wrt_colors, WeightedVertex[] vertices, LinkedList<Integer> remaining_vertex_set, IntRelate int_relate_ob, int seed)


	public boolean update_top_level_weights_wrt_one_enumerated_clique(ArrayList<ArrayList<Integer>> critical_clique_list, ArrayList<Integer> enumerated_clique, WeightedVertex[] vertices, WeightBasedPartition weight_based_partition)
	{
			Collections.sort(enumerated_clique, new Comparator<Integer>(){
				public int compare(Integer n1, Integer n2){
					if(vertices[n1].get_weight() < vertices[n2].get_weight() || (vertices[n1].get_weight() == vertices[n2].get_weight() && n1 < n2)){
						return 1;
					}
					else{
						return -1;
					}
				}
			});
			return update_top_level_weights_wrt_vertex_weight_decrease_clique_and_maintain_critical_clique_list(critical_clique_list, enumerated_clique, vertices, weight_based_partition);
	} // public void update_top_level_weights_wrt_one_enumerated_clique(ArrayList<Integer> cand_clique, WeightedVertex[] vertices)
	
	private int connection_strength(ArrayList<Integer> cand_vertices, WeightedVertex[] vertices, IntRelate int_relate_ob)
	{
		int weight_sum = 0;
		for(int i = 0; i < cand_vertices.size(); i++)
		{
			for(int j = 0; j < cand_vertices.size(); j++)
			{
				if(i == j) continue;
				int u = cand_vertices.get(i);
				int v = cand_vertices.get(j);
				if(!int_relate_ob.related(u, v)) continue;
				weight_sum += vertices[v].get_weight();	
			}
		}
		return weight_sum;
	}
	
	private int rand_vertex_with_greatest_neighbor_weight_sum(ArrayList<Integer> cand_vertices, WeightedVertex[] vertices, IntRelate int_relate_ob, boolean weight_considered)
	{
		int best_nb_weight_sum = 0;
		ArrayList<Integer> best_vertices = new ArrayList<Integer>();
		for(int u : cand_vertices)
		{
			int nb_weight_sum = 0;
			for(int v : cand_vertices)
			{
				if(u == v) continue;
				if(!int_relate_ob.related(u, v)) continue;
				nb_weight_sum += weight_considered ? vertices[v].get_weight() : 1;
			}
			if(best_nb_weight_sum < nb_weight_sum)
			{
				best_nb_weight_sum = nb_weight_sum;
				best_vertices.clear();
				best_vertices.add(Integer.valueOf(u));
			}
			else if(best_nb_weight_sum == nb_weight_sum)
			{
				best_vertices.add(Integer.valueOf(u));
			}
		}
		return best_vertices.get(rand.nextInt(best_vertices.size()));
	}
	
	private int rand_vertex_with_greatest_connection_strength(ArrayList<Integer> cand_vertices, WeightedVertex[] vertices, IntRelate int_relate_ob)
	{
		ArrayList<Integer> tempAList = new ArrayList<Integer>();
		int best_connection_strength = 0;
		ArrayList<Integer> best_vertices = new ArrayList<Integer>();
		for(int u : cand_vertices)
		{
			tempAList.clear();
			for(int v : cand_vertices)
			{
				if(u == v) continue;
				if(!int_relate_ob.related(u, v)) continue;
				tempAList.add(Integer.valueOf(v));
			}
			int cscore = connection_strength(tempAList, vertices, int_relate_ob);
			if(best_connection_strength < cscore)
			{
				best_vertices.clear();
				best_vertices.add(Integer.valueOf(u));
				best_connection_strength = cscore;
			}
			else if(best_connection_strength == cscore)
			{
				best_vertices.add(Integer.valueOf(u));
			}
		}
		if(best_connection_strength > 0)
		{
			return best_vertices.get(rand.nextInt(best_vertices.size()));
		}
		
		best_vertices.clear();
		int best_max_min_weight = 0;
		for(int u : cand_vertices)
		{
			int neighbor_greatest_weight = 0;
			for(int v : cand_vertices)
			{
				if(u == v) continue;
				if(!int_relate_ob.related(u, v)) continue;
				neighbor_greatest_weight = StrictMath.max(neighbor_greatest_weight, vertices[v].get_weight());
			}
			int max_min_weight = StrictMath.min(neighbor_greatest_weight, vertices[u].get_weight());
			if(best_max_min_weight < max_min_weight)
			{
				best_vertices.clear();
				best_vertices.add(u);
				best_max_min_weight = max_min_weight;
			}
			else if(best_max_min_weight == max_min_weight)
			{
				best_vertices.add(Integer.valueOf(u));
			}
		}
// System.out.println("to choose a random one: " + best_vertices);		
		return best_vertices.get(rand.nextInt(best_vertices.size()));
	}
	
	private ArrayList<Integer> common_neighbors(ArrayList<Integer> clq, WeightedVertex[] vertices, IntRelate int_relate_ob, HashSet<Integer> allowed_vertices)
	{
		ArrayList<Integer> common_nbs = new ArrayList<Integer>();
		int u = clq.get(0);
		for(int nb : vertices[u].get_neighbors())
		{
			if(!allowed_vertices.contains(Integer.valueOf(nb))) continue;
			boolean is_common_nb = true;
			for(int i = 1; i < clq.size(); i++)
			{
				if(!int_relate_ob.related(nb, clq.get(i)))
				{
					is_common_nb = false;
					break;
				}
			}
			if(is_common_nb)
			{
				common_nbs.add(Integer.valueOf(nb));
			}
		}
		return common_nbs;
	}
	
	private void add_sorted_clique_into_set_of_sorted_cliques(ArrayList<Integer> clq, ArrayList<ArrayList<Integer>> clq_list)
	{
		for(ArrayList<Integer> clq_in_list : clq_list)
		{
			if(clq_in_list.equals(clq))
			{
				return;
			}
		}
		clq_list.add(clq);
	}
	
	public boolean all_vertices_removed(ArrayList<Integer> clq, HashSet<Integer> remaining_vertex_set)
	{
		for(int v : clq)
		{
			if(remaining_vertex_set.contains(v))
			return false;
		}
		return true;
	}
	
	public void update_top_level_weights_wrt_colors_by_clique_sampling(int sample_num, ArrayList<ArrayList<Integer>> critical_clique_list, TreeSet<Integer> cand_remove_vertices_for_clique_reductions_wrt_degree_decrease, WeightedVertex[] vertices, IntRelate int_relate_ob, ZeroParameterBehavior zero_param_behav_ob, TwoParametersBehavior two_param_behav_ob, DegreeBasedPartition dgr_based_partition, WeightBasedPartition weight_based_partition, HashSet<Integer> remaining_vertex_set)
	{
		for(int c = 0; c <= top_level_weights_wrt_colors.size(); c++)
		{
// System.out.println("as to Color " + c);
			if(!can_exist_clique_above_at_color.get(c)) continue;
// System.out.println("start clique sampling");
			update_top_level_weights_wrt_given_color_by_clique_sampling(sample_num, c, critical_clique_list, cand_remove_vertices_for_clique_reductions_wrt_degree_decrease, vertices, int_relate_ob, zero_param_behav_ob, two_param_behav_ob, dgr_based_partition, weight_based_partition, remaining_vertex_set);
		}
	}
	
	public void update_top_level_weights_wrt_given_color_by_clique_sampling(int sample_num, int color, ArrayList<ArrayList<Integer>> critical_clique_list, TreeSet<Integer> cand_remove_vertices_for_clique_reductions_wrt_degree_decrease, WeightedVertex[] vertices, IntRelate int_relate_ob, ZeroParameterBehavior zero_param_behav_ob, TwoParametersBehavior two_param_behav_ob, DegreeBasedPartition dgr_based_partition, WeightBasedPartition weight_based_partition, HashSet<Integer> remaining_vertex_set)
	{
		ArrayList<ArrayList<Integer>> start_clique_list = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> allowed_vertices = allowed_later_sampling_vertices_wrt_colors.get(color);

		ArrayList<Integer> heuristic_vertices = new ArrayList<Integer>();
		boolean weight_considered = false; // focuse on increasing clique size
		if(color == 0)
		{
			heuristic_vertices = new ArrayList<Integer>(allowed_vertices);
		}
		else if(color == top_level_weights_wrt_colors.size())
		{
			heuristic_vertices = weight_based_partition.get_vertex_list_of_weight_between(1, top_level_weights_wrt_colors.get(color - 1));
		}
		else if(top_level_weights_wrt_colors.get(color) == top_level_weights_wrt_colors.get(color - 1))
		{
			return; // no need because of failure before, now the problem has become more difficult
		}
		else
		{
			for(int u : allowed_vertices)
			{
				if(vertices[u].get_weight() <= top_level_weights_wrt_colors.get(color - 1))
					heuristic_vertices.add(Integer.valueOf(u));
			}
		}
System.out.println("size of heuristic_vertices: " + heuristic_vertices.size());
		if(heuristic_vertices.isEmpty())
			return;
System.out.println("weights of heuristic_vertices:");
for(int i = 0; i < heuristic_vertices.size(); i++)
System.out.print(vertices[heuristic_vertices.get(i)].get_weight() + ", ");
System.out.println();
		if(sample_num >= heuristic_vertices.size())
		{
			for(int i = 0; i < heuristic_vertices.size(); i++)
			{
				ArrayList<Integer> temp_list = new ArrayList<Integer>();
				int u = heuristic_vertices.get(i);
				temp_list.add(u);
				start_clique_list.add(temp_list);
			}
		}
		else
		{
			for(int i = 0; i < sample_num; i++)
			{
				ArrayList<Integer> temp_list = new ArrayList<Integer>();
				int u = heuristic_vertices.get(rand.nextInt(heuristic_vertices.size()));
				temp_list.add(u);
				start_clique_list.add(temp_list);
			}
		}
System.out.println("sampled vertices for promising cliques: " + start_clique_list);
		update_top_level_weights_by_clique_sampling_and_apply_reductions(start_clique_list, 0, critical_clique_list, cand_remove_vertices_for_clique_reductions_wrt_degree_decrease, vertices, int_relate_ob, zero_param_behav_ob, two_param_behav_ob, dgr_based_partition, weight_based_partition, remaining_vertex_set, new HashSet<>(allowed_later_sampling_vertices_wrt_colors.get(color)), weight_considered);
	}
	
	public void update_top_level_weights_by_clique_sampling_and_apply_reductions(ArrayList<ArrayList<Integer>> start_clique_list, int k, ArrayList<ArrayList<Integer>> critical_clique_list, TreeSet<Integer> cand_remove_vertices_for_clique_reductions_wrt_degree_decrease, WeightedVertex[] vertices, IntRelate int_relate_ob, ZeroParameterBehavior zero_param_behav_ob, TwoParametersBehavior two_param_behav_ob, DegreeBasedPartition dgr_based_partition, WeightBasedPartition weight_based_partition, HashSet<Integer> remaining_vertex_set, HashSet<Integer> allowed_vertices, boolean weight_considered) // 
	{
		int j = 1;
		while(!start_clique_list.isEmpty())
		{
			ArrayList<Integer> start_clique = start_clique_list.get(rand.nextInt(start_clique_list.size())); // Assume all starting cliques are sorted by vertex ids
System.out.println("potential start clique: " + start_clique);
			if(all_vertices_removed(start_clique, remaining_vertex_set))
			{
				start_clique_list.remove(start_clique);
				continue;
			}
			if(j > k)
			{
				start_clique_list.remove(start_clique);
			}
			ArrayList<Integer> clq = new ArrayList<Integer>();
			for(int v : start_clique)
			{
				if(remaining_vertex_set.contains(v)) clq.add(Integer.valueOf(v));
			}
System.out.println("actual start clique: " + clq);
			ArrayList<Integer> cand_vertices = common_neighbors(clq, vertices, int_relate_ob, allowed_vertices);
			ArrayList<Integer> good_vertices = new ArrayList<Integer>();
			while(!cand_vertices.isEmpty())
			{
				boolean lower_bound_improved = false;
				if(j > k)
				{
					if(clq.size() >= top_level_weights_wrt_colors.size())
					{
						good_vertices = new ArrayList<>(cand_vertices);
System.out.println("good vertices found because of clique sizes");
					}
					else
					{
						Collections.sort(clq, new Comparator<Integer>(){
								public int compare(Integer n1, Integer n2){
									if(vertices[n1].get_weight() < vertices[n2].get_weight() || (vertices[n1].get_weight() == vertices[n2].get_weight() && n1 < n2)){
										return 1;
								}
								else{
									return -1;
								}
							}
						});
						if(vertices[clq.get(clq.size() - 1)].get_weight() > top_level_weights_wrt_colors.get(clq.size())) 
						{
							boolean good_vertex_found = false;
							for(int v : cand_vertices)
							{
								if(vertices[v].get_weight() > top_level_weights_wrt_colors.get(clq.size()))
								{
									good_vertex_found = true;
									good_vertices.add(Integer.valueOf(v));
								}
							}
						}
					}	
				}
				if(j > k && !good_vertices.isEmpty())
				{
					// add this clique back, allow it to lookhead later
					add_sorted_clique_into_set_of_sorted_cliques(clq, start_clique_list);
					for(int u : good_vertices)
					{
						ArrayList<Integer> good_clique = new ArrayList<>(clq);
						good_clique.add(u); // greedy
						Collections.sort(good_clique, new Comparator<Integer>(){
								public int compare(Integer n1, Integer n2){
									if(vertices[n1].get_weight() < vertices[n2].get_weight() || (vertices[n1].get_weight() == vertices[n2].get_weight() && n1 < n2)){
										return 1;
								}
								else{
									return -1;
								}
							}
						});
						add_sorted_clique_into_set_of_sorted_cliques(good_clique, start_clique_list);
						lower_bound_improved = update_top_level_weights_wrt_vertex_weight_decrease_clique(good_clique, vertices, weight_based_partition);
						if(lower_bound_improved)
						{
							// apply reductions wrt. top-level weight updates
							two_param_behav_ob.behavior(last_top_level_weight_updated_colors, new ArrayList<Integer>());
							boolean OK = zero_param_behav_ob.behavior();
						}
					}
					good_vertices.clear();
					break;
				}
				else
				{
					int u = rand_vertex_with_greatest_neighbor_weight_sum(cand_vertices, vertices, int_relate_ob, weight_considered);
					clq.add(u);
					cand_vertices.remove(Integer.valueOf(u));
					for(int l = cand_vertices.size() - 1; l >= 0; l--)
					{
						int v = cand_vertices.get(l);
						if(int_relate_ob.related(u, v)) continue;
						cand_vertices.remove(Integer.valueOf(v));
					}
				}
			}
			Collections.sort(clq, new Comparator<Integer>(){
								public int compare(Integer n1, Integer n2){
									if(vertices[n1].get_weight() < vertices[n2].get_weight() || (vertices[n1].get_weight() == vertices[n2].get_weight() && n1 < n2)){
										return 1;
								}
								else{
									return -1;
								}
							}
						});	
			// need to update top level dates and apply reductions
			boolean lower_bound_improved = update_top_level_weights_wrt_vertex_weight_decrease_clique(clq, vertices, weight_based_partition);
			if(lower_bound_improved)
			{
System.out.println("top_level_weights updated to be: " + top_level_weights_wrt_colors);
				// apply reductions wrt. top-level weight updates
				two_param_behav_ob.behavior(last_top_level_weight_updated_colors, new ArrayList<Integer>());
				boolean OK = zero_param_behav_ob.behavior();
			}

			for(int l = critical_clique_list.size() - 1; l >= 0; l--)
			{
				// ArrayList<Integer> critical_clq = critical_clique_list.get(i);
				if(sorted_clique_dominate_eq(clq, critical_clique_list.get(l), vertices))
				{
// System.out.println("about to delete a clique because of being dominated: " + critical_clique_list.get(i));
					critical_clique_list.remove(l);// delete worse cliques
// System.out.println("after deletion, critical_clique_list: " + critical_clique_list);
				}
			}
			for(int l = critical_clique_list.size() - 1; l >= 0; l--)
			{
				if(!sorted_clique_intersect_with_top_level_weights(critical_clique_list.get(l), vertices))
				{
// System.out.println("about to delete a clique because of strictly below top-level weights: " + critical_clique_list.get(i));
					critical_clique_list.remove(l);// delete redundant cliques
// System.out.println("after deletion, critical_clique_list: " + critical_clique_list);
				}
			}
			// place cand_clique into critical_clique_list
			// currently I think the conditions below must hold...
			if(!sorted_clique_strictly_below_top_level_weights(clq, vertices) && !sorted_clique_covered_by_critical_clique_list(clq, critical_clique_list, vertices))
			{

				critical_clique_list.add(clq); // insert better ones
			}
			j++;
		}
	}
	
	private void show_restricted_neighborhood(ArrayList<Integer> cand_vertices, WeightedVertex[] vertices)
	{
		for(int i = 0; i < cand_vertices.size(); i++)
		{
			int u = cand_vertices.get(i);
			System.out.println("vertex: " + u);
			System.out.println("weight: " + vertices[u].get_weight());
			System.out.print("neighbors: ");
			for(int v : vertices[u].get_neighbors())
			{
				if(!cand_vertices.contains(Integer.valueOf(v))) continue;
				System.out.print(v + "\t");
			}
			System.out.println();
		}
	}
	
	private void show_decreasing_weights_in_clique(ArrayList<Integer> cand_clique, WeightedVertex[] vertices)
	{
		for(int v : cand_clique)
		{
			System.out.print(vertices[v].get_weight() + "\t");
		}
		System.out.println();
	}
	
	private boolean check_clique(ArrayList<Integer> cand_clique, IntRelate int_relate_ob)
	{
		for(int i = 0; i < cand_clique.size(); i++)
		{
			for(int j = i + 1; j < cand_clique.size(); j++)
			{
				if(!int_relate_ob.related(cand_clique.get(i), cand_clique.get(j)))
					return false;
			}
		}
		return true;
	}

} // class CliqueSearch
