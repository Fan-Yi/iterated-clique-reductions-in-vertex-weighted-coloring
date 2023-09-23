package NeighborhoodSubsetSearchPack;

import java.util.*;

// import DegreeBucketPack.DegreeBasedPartition;
import WeightBucketPack.WeightBasedPartition;
import GraphPack.WeightedVertexPack.VertexPack.Vertex;
import GraphPack.WeightedVertexPack.WeightedVertex;
import InterfacePack.IntRelate;
import UsrPausePack.UsrPause;

public class NeighborhoodSubsetSearch{

	private BitSet neighborhood_bits;
	private BitSet vertex_used_bits_1, vertex_used_bits_2, vertex_used_bits_3;

	public NeighborhoodSubsetSearch(int vertex_num)
	{
		// below is used for subset checking
		neighborhood_bits = new BitSet(vertex_num + 1);
		// below are for avoiding repetitions
		vertex_used_bits_1 = new BitSet(vertex_num + 1);
		vertex_used_bits_2 = new BitSet(vertex_num + 1);
		vertex_used_bits_3 = new BitSet(vertex_num + 1);
	}

	private boolean neighborhood_is_subset(int small_d_v, int big_d_v, Vertex[] vertices)
	{
		for(int v : vertices[big_d_v].get_neighbors())
		{
			neighborhood_bits.set(v);
		}

		for(int v : vertices[small_d_v].get_neighbors())
		{
			if(!neighborhood_bits.get(v))
			{
				// clean the sheet
				for(int x : vertices[big_d_v].get_neighbors()) 
				{
					neighborhood_bits.clear(x);
				}
				return false;
			}
		}

		// clean the sheet
		for(int x : vertices[big_d_v].get_neighbors())
		{
			neighborhood_bits.clear(x);
		}
		return true;
	} // private boolean neighborhood_is_subset(int small_d_v, int big_d_v, Vertex[] vertices)

	private boolean neighborhood_is_subset_except(int small_d_v, int big_d_v, Vertex[] vertices, int ... varargs)
	// S \backslash I \subseteq T <=> S \subseteq T \cup I
	{
		for(int v : vertices[big_d_v].get_neighbors())
		{
			neighborhood_bits.set(v);
		}

		for(int v : varargs)
		{
			neighborhood_bits.set(v);
		}

		for(int v : vertices[small_d_v].get_neighbors())
		{
			if(!neighborhood_bits.get(v))
			{
				// clean the sheet
				for(int x : vertices[big_d_v].get_neighbors())
				{
					neighborhood_bits.clear(x);
				}
				for(int x : varargs)
				{
					neighborhood_bits.clear(x);
				}
				return false;
			}
		}

		// clean the sheet
		for(int x : vertices[big_d_v].get_neighbors())
		{
			neighborhood_bits.clear(x);
		}
		for(int x : varargs)
		{
			neighborhood_bits.clear(x);
		}
		return true;
	} // private boolean neighborhood_is_subset(int small_d_v, int big_d_v, Vertex[] vertices)


	public boolean is_single_single_dominated(int u, WeightedVertex[] vertices, IntRelate int_relate_ob, WeightBasedPartition weight_based_partition)
	{
		// if u is isolated
		if(vertices[u].get_degree() == 0)
		{
// System.out.println(u + "\'s degree is zero.");
			if(vertices[u].get_weight() < weight_based_partition.get_dynamic_max_weight())
			{
// System.out.println(u + "\'s weight is not the max one, so return true.");
				return true;
			}
// System.out.println("max weight vertex count: " + weight_based_partition.maximum_weight_vertex_count() + ", true if > 1 and false otherwise");
			return weight_based_partition.maximum_weight_vertex_count() > 1;
		}
		for(int nb_u : vertices[u].get_neighbors())
		{
			for(int potential_covering_w : vertices[nb_u].get_neighbors())
			{
				if(vertex_used_bits_1.get(potential_covering_w)) continue; // avoid considering the same vertex-pair
				vertex_used_bits_1.set(potential_covering_w); // put this statement first because it is really efficient

				if(u == potential_covering_w) // not allowed to find itself
					continue;
				if(int_relate_ob.related(u, potential_covering_w)) // cannot be connected so can have the same color
					continue;
				if(vertices[potential_covering_w].get_degree() < vertices[u].get_degree()) // neighborhood must be subset and subset cannot be bigger
					continue;
				if(vertices[potential_covering_w].get_weight() < vertices[u].get_weight())
					continue;
				if(!neighborhood_is_subset(u, potential_covering_w, vertices)) // this operation is expensive, so I put weight relations and auxiliary relations before
					continue;
/*
System.out.print("potential_covering_w: " + potential_covering_w + ", neighbors: ");
for(int x : vertices[potential_covering_w].get_neighbors()) System.out.print(x + ", ");
System.out.println();
System.out.print("to cover " + u + ", neighbors: ");
for(int x : vertices[u].get_neighbors()) System.out.print(x + ", ");
System.out.println("single-single dominated by " + potential_covering_w);
*/
//System.out.println("single-single dominance relation holds, about to remove " + u);
				vertex_used_bits_1.clear();  // recovering for later use
				return true;
			}
		} // for(int nb_u : vertices[u].get_neighbors())
		vertex_used_bits_1.clear(); // recovering for later use
		return false;
	}

	public boolean is_double_double_dominated(int x1, int x2, WeightedVertex[] vertices, IntRelate int_relate_ob, WeightBasedPartition weight_based_partition)
	// preconditions: x1, x2 must be connected
	{
		// isolated
		if(vertices[x1].get_neighbors().size() == 1 && vertices[x2].get_neighbors().size() == 1) // the edge {x1, x2} is isolated
		{
			// 
			int big_w = StrictMath.max(vertices[x1].get_weight(), vertices[x2].get_weight());
			int small_w = StrictMath.min(vertices[x1].get_weight(), vertices[x2].get_weight());
			int max_v_weight = weight_based_partition.get_dynamic_max_weight();
			int w = max_v_weight;
			while(w >= big_w)
			{
				ArrayList<Integer> cand_vertices_for_covering_big_w_v = weight_based_partition.get_vertices_of_weight(w);
				for(int y1 : cand_vertices_for_covering_big_w_v)
				{
					if(y1 == x1 || y1 == x2)
					{
						continue; // ensure outside
					}
					for(int y2 : vertices[y1].get_neighbors())
					{
						if(y2 == x1 || y2 == x2)
						{
							continue; // ensure outside
						}
						if(vertices[y2].get_weight() < small_w)
						{
							continue;
						}
						return true;
					}
				}
				w--;
			}
			return false;
		}
		
		// 
		if(vertices[x1].get_neighbors().size() == 1) // w(x1) = 1 and w(x2) > 1
		{
			// swap(x1, x2);
			int temp = x1;
			x1 = x2;
			x2 = temp;
		}
		
		// below w(x1) > 1
		
		// vertex_used_bits_1.clear();
		// vertex_used_bits_2.clear();
		for(int nb_x1 : vertices[x1].get_neighbors())
		{
			if(nb_x1 == x2) continue; // this case can be dealt with by single-single, so no need to be addressed
									  // in another aspect, this rule does not require to care this neighbor here
									  // ignoring this case will not lose any possibility
			for(int dist_2_nb_x1 : vertices[nb_x1].get_neighbors())
			{
				if(vertex_used_bits_1.get(dist_2_nb_x1)) continue; // avoid repetitions
				vertex_used_bits_1.set(dist_2_nb_x1); // put this statement first because it is really efficient

				if(dist_2_nb_x1 == x1 || dist_2_nb_x1 == x2) // not allowed to find itself, must find vertices outside
					continue;
				if(int_relate_ob.related(x1, dist_2_nb_x1)) // must not be connected so can have the same color
					continue;

				if(vertices[dist_2_nb_x1].get_weight() < vertices[x1].get_weight())
					continue;
				if(vertices[dist_2_nb_x1].get_degree() < vertices[x1].get_degree())
					continue;
				if(!neighborhood_is_subset_except(x1, dist_2_nb_x1, vertices, x2))
					continue;
// System.out.println("1. having found " + x1 + " whose neighborhood is a subset of that of " + dist_2_nb_x1 + ", except " + x2);

				// special cases
				if(vertices[x2].get_neighbors().size() == 1) // x2 has no other neighbors
				{
					for(int nb_dist_2_nb_x1_nb_hood_cov_x2 : vertices[dist_2_nb_x1].get_neighbors())
					{
						if(nb_dist_2_nb_x1_nb_hood_cov_x2 == x1 || nb_dist_2_nb_x1_nb_hood_cov_x2 == x2)
						{
							continue; // ensure outside
						}
						if(vertices[nb_dist_2_nb_x1_nb_hood_cov_x2].get_weight() < vertices[x2].get_weight())
						{
							continue;
						}
/*
System.out.print(x2 + " is dominated by " + nb_dist_2_nb_x1_nb_hood_cov_x2 + ", ");
System.out.println("special double-double reductions applicable***********************************");
*/
						vertex_used_bits_1.clear();
						return true;
					}
					vertex_used_bits_1.clear();
					return false;
				}

				// below w(x2) > 1
				for(int nb_x2 : vertices[x2].get_neighbors())
				{
					if(nb_x2 == x1) continue; // must obtain a neighbor outside
					for(int dist_2_nb_x2 : vertices[nb_x2].get_neighbors())
					{
						if(vertex_used_bits_2.get(dist_2_nb_x2)) continue; // avoid repetitions
						vertex_used_bits_2.set(dist_2_nb_x2); // put this statement first because it is really efficient

						if(dist_2_nb_x1 == dist_2_nb_x2) // should find different covering vertices
							continue;
						if(!int_relate_ob.related(dist_2_nb_x1, dist_2_nb_x2)) // must be connected to ensure different colors
							continue;
						if(dist_2_nb_x2 == x1 || dist_2_nb_x2 == x2)// not allowed find itself, must find outside
							continue;
						if(int_relate_ob.related(x2, dist_2_nb_x2)) // must not be connected to allow having the same color
							continue;
						if(vertices[dist_2_nb_x2].get_weight() < vertices[x2].get_weight())
							continue;
						if(vertices[dist_2_nb_x2].get_degree() < vertices[x2].get_degree())
							continue;
						if(!neighborhood_is_subset_except(x2, dist_2_nb_x2, vertices, x1))
							continue;
/*
System.out.print("neighborhood of " + x1 + ": "); for(int z : vertices[x1].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.print("neighborhood of " + dist_2_nb_x1 + ": "); for(int z : vertices[dist_2_nb_x1].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.println("weight of " + x1 + " and " + dist_2_nb_x1 + " are " + vertices[x1].get_weight() + " and " + vertices[dist_2_nb_x1].get_weight());

System.out.print("neighborhood of " + x2 + ": "); for(int z : vertices[x2].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.print("neighborhood of " + dist_2_nb_x2 + ": "); for(int z : vertices[dist_2_nb_x2].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.println("weight of " + x2 + " and " + dist_2_nb_x2 + " are " + vertices[x2].get_weight() + " and " + vertices[dist_2_nb_x2].get_weight());

System.out.println("1. having found " + x1 + " whose neighborhood is a subset of that of " + dist_2_nb_x1 + ", except " + x2);
System.out.println("2. having found " + x2 + " whose neighborhood is a subset of that of " + dist_2_nb_x2 + ", except " + x1);
*/
/*
if(vertices[x1].get_degree() > 5 || vertices[x2].get_degree() > 5 || vertices[x1].get_degree() != vertices[x2].get_degree())
{
System.out.println(x1 + " and " + x2 + " are to remove together, covered by " + dist_2_nb_x1 + " and " + dist_2_nb_x2 + ",");
System.out.println("the respective degrees of " + x1 + " and " + x2 + " are " + vertices[x1].get_degree() + " and " + vertices[x2].get_degree());
}
*/
/*
System.out.println("*********************************************************************");
int[] nbs; 
System.out.print("neighbor list of " + x1 + ": ");
nbs = vertices[x1].get_neighbors();
for(int nb : nbs) System.out.print(nb + "\t"); System.out.println();
nbs = vertices[dist_2_nb_x1].get_neighbors();
System.out.print("neighbor list of " + dist_2_nb_x1 + ": ");
for(int nb : nbs) System.out.print(nb + "\t"); System.out.println();
nbs = vertices[x2].get_neighbors();
System.out.print("neighbor list of " + x2 + ": ");
for(int nb : nbs) System.out.print(nb + "\t"); System.out.println();
nbs = vertices[dist_2_nb_x2].get_neighbors();
System.out.print("neighbor list of " + dist_2_nb_x2 + ": ");
for(int nb : nbs) System.out.print(nb + "\t"); System.out.println();
*/
						vertex_used_bits_1.clear();
						vertex_used_bits_2.clear();
						return true;
					} // for(int dist_2_nb_x2 : vertices[nb_x2].get_neighbors())
				} // for(int nb_x2 : vertices[x2].get_neighbors())
				vertex_used_bits_2.clear(); // since there is no return statement here, so the info in vertex_used_bits_1 should be kept
			}
		} // for(int nb_x1 : vertices[x1].get_neighbors())
		vertex_used_bits_1.clear(); 
		return false;
	} // public boolean is_double_double_dominated(int x1, int x2, WeightedVertex[] vertices, IntRelate int_relate_ob)


	public boolean is_triple_double_dominated(int x1, int y, int x3, WeightedVertex[] vertices, IntRelate int_relate_ob)
	// preconditions: x1, y, x3 form a 2-dist path, no circles, i.e., x, y must not be connected 
	{
		// vertex_used_bits_1.clear();
		// no need to deal with the case where d(x1) = d(x3) = 1, since it will be dealt with by single-single
/*
		if(vertices[x1].get_neighbors().size() == 1 && vertices[x3].get_neighbors().size() == 1)
		{
			if(vertices[y].get_neighbors().size() == 2)
			{
				// no need to deal with this case, since it can be dealt with by single-single	
			}
			else // if d(x1) = d(x3) = 1 and d(y) != 2
			{
				for(int nb_y : vertices[y].get_neighbors())
				{
					if(nb_y == x1 || nb_y == x3) continue; // must obtain a neighbor outside
					for(int dist_2_nb_y : vertices[nb_y].get_neighbors())
					{
						if(vertex_used_bits_1.get(dist_2_nb_y)) continue; // avoid repetitions
						vertex_used_bits_1.set(dist_2_nb_y); // 
						
						if(dist_2_nb_y == nb_y) continue; // must not obtain itself

						int v = dist_2_nb_y;
						if(int_relate_ob.related(y, v)) continue; // y and the covering vertex v must not be connected, so can have the same color
						if(vertices[v].get_weight() < vertices[y].get_weight()) continue; // weight constraint
						if(vertices[v].get_degree() < vertices[y].get_degree() - 1) continue; // v will SUBTRACT 2 and y will SUBTRACT to compare neighborhood
						if(!neighborhood_is_subset_except(y, v, vertices, x1, x3)) continue; // neighborhood subset constraint
						// having obtained a vertex which covers y

						for(int u : vertices[v].get_neighbors())
						{
							// constraints on x1
							// the constraint below is not needed, because d(x1)==1, u and x1 will never be connected
							// if(int_relate_ob.related(x1, u)) continue; // must not connect so can have the same color
							if(vertices[x1].get_weight() > vertices[u].get_weight()) continue; // weight constraint
							// constraints on x2
							// if(int_relate_ob.related(x2, u)) continue; // must not connect so can have the same color
							if(vertices[x3].get_weight() > vertices[u].get_weight()) continue; // weight constraint
System.out.println("only the center vertex in the dist-2 path has out neighbors, special triple-double reductions applicable*****************************");
							return true;
						}
					} // for(int dist_2_nb_y : vertices[nb_y].get_neighbors())
				} // for(int nb_y : vertices[y].get_neighbors())
				vertex_used_bits_1.clear();
			} // if d(x1) = d(x2) = 1, d(y) != 2			
			return false;
		} // if(vertices[x1].get_neighbors().size() == 1 && vertices[x2].get_neighbors().size() == 1)
*/
		// else 
		if(vertices[x1].get_neighbors().size() == 1) // d(x3) > 1
		{
// System.out.println("swap vertices for considering triple-double reductions");
			// swap(x1, x3);
			int temp = x1;
			x1 = x3;
			x3 = temp;
		}
		// now d(x1) > 1
		for(int nb_x1 : vertices[x1].get_neighbors())
		{
			if(nb_x1 == y) continue; // the subset constraint below ensures that this will not lose any possibility
			for(int dist_2_nb_x1 : vertices[nb_x1].get_neighbors())
			{
				if(vertex_used_bits_1.get(dist_2_nb_x1)) continue; // avoid repetitions
				vertex_used_bits_1.set(dist_2_nb_x1); // put this statement first because it is efficient

				if(dist_2_nb_x1 == x1) continue; // not itself
				if(dist_2_nb_x1 == y || dist_2_nb_x1 == x3) continue; // ensure to find a vertex outside the 2-dist path

				// below since u cannot be connected with either x1 or x3, so it cannot be y
				int u = dist_2_nb_x1;
				// below are constraints on x1
				if(int_relate_ob.related(x1, u)) continue; // x1 and the covering vertex u must not be connected, so can have the same color
				if(vertices[x1].get_weight() > vertices[u].get_weight()) continue; // weight constraint
				if(vertices[x1].get_degree() > vertices[u].get_degree()) continue; // degree constraint
				if(!neighborhood_is_subset_except(x1, u, vertices, y)) continue; // neighborhood subset constraint

				// below y and x3 can have no neighbors outside, the codes are still correct
				// below are constraints on x3
				if(int_relate_ob.related(x3, u)) continue; // x2 and u must not be connected
				if(vertices[x3].get_weight() > vertices[u].get_weight()) continue; // weight constraint
				if(vertices[x3].get_degree() > vertices[u].get_degree()) continue; // degree constraint
				if(!neighborhood_is_subset_except(x3, u, vertices, y)) continue; // neighborhood subset constraint
/*
System.out.println(x1 + " and " + u + " are not connected");
System.out.println(x3 + " and " + u + " are not connected");
System.out.print("neighborhood of " + x1 + ": "); for(int z : vertices[x1].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.print("neighborhood of " + x3 + ": "); for(int z : vertices[x3].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.print("neighborhood of " + u + ": "); for(int z : vertices[u].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.println("N(" + x1 + ") \\subseteq N(" + u + ") + except " + y);
System.out.println("N(" + x3 + ") \\subseteq N(" + u + ") + except " + y);
System.out.println("weight: " + vertices[x1].get_weight() + " and " + vertices[x3].get_weight() + " covered by " + vertices[u].get_weight());
System.out.println(x1 + " and " + x3 + " are covered by " + u);
*/
				for(int v : vertices[u].get_neighbors()) // v can be equal to dist_2_nb_x1 (or dist_2_nb_x2)
				{
					if(v == x1 || v == y || v == x3) continue; // must find vertices outside
					// constraints on y
					if(int_relate_ob.related(y, v)) continue; // must not be connected so can have the same color
					if(vertices[y].get_weight() > vertices[v].get_weight()) continue; // weight constraint
					/*BECAUSE y WILL SUBTRACT TWO NEIGHBORS AND v WILL ONLY SUBTRACT ONE*/
					if(vertices[y].get_degree() - 1 > vertices[v].get_degree()) continue; // degree constraint
					if(!neighborhood_is_subset_except(y, v, vertices, x1, x3)) continue; // neighborhood subset constraint
/*
System.out.println(y + " and " + v + " are not connected");
System.out.print("neighborhood of " + y + ": "); for(int z : vertices[y].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.print("neighborhood of " + v + ": "); for(int z : vertices[v].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.println("N(" + y + ") \\subseteq N(" + v + ") + except " + x1 + ", " + x2);
System.out.println("weight: " + vertices[y].get_weight() + " covered by " + vertices[v].get_weight());
System.out.println(y  + " is covered by " + v);
*/
System.out.println("triple-double reductions applicable#######################################");
// UsrPause.press_enter_to_continue();
					vertex_used_bits_1.clear();
					return true;
				}
			}
		}
		vertex_used_bits_1.clear();
		return false;
	} // public boolean is_triple_double_dominated(int x1, int y, int x3, WeightedVertex[] vertices, IntRelate int_relate_ob)


	public boolean is_triple_triple_dominated(int x1, int x2, int x3, WeightedVertex[] vertices, IntRelate int_relate_ob, WeightBasedPartition weight_based_partition)
	// preconditions: x1, x2, x3 must be a triangle
	{
// System.out.println("considering to cover x1: " + x1 + ", x2: " + x2 + ", x3: " + x3 + " as a whole");
		if(vertices[x1].get_neighbors().size() == 2 && vertices[x2].get_neighbors().size() == 2 && vertices[x3].get_neighbors().size() == 2)
		{
			// 
			int big_w, mid_w, small_w;
			int w1 = vertices[x1].get_weight();
			int w2 = vertices[x2].get_weight();
			int w3 = vertices[x3].get_weight();
			if(w1 > w2 && w1 > w3)
			{
				big_w = w1;
				if(w2 > w3) {mid_w = w2; small_w = w3;} else {mid_w = w3; small_w = w2;}
			}
			else if(w2 > w3)
			{
				big_w = w2;
				if(w1 > w3) {mid_w = w1; small_w = w3;} else {mid_w = w3; small_w = w1;}
			}
			else
			{
				big_w = w3;
				if(w1 > w2) {mid_w = w1; small_w = w2;} else {mid_w = w2; small_w = w1;}
			}
			
			int w = weight_based_partition.get_dynamic_max_weight();
			while(w >= big_w)
			{
				ArrayList<Integer> cand_vertices_for_covering_big_w_v = weight_based_partition.get_vertices_of_weight(w);
				for(int y1 : cand_vertices_for_covering_big_w_v)
				{
					if(y1 == x1 || y1 == x2 || y1 == x3)
					{
						continue; // ensure outside
					}
					for(int y2 : vertices[y1].get_neighbors())
					{
						if(y2 == x1 || y2 == x2 || y2 == x3)
						{
							continue; // ensure outside
						}
						if(vertices[y2].get_weight() < mid_w)
						{
							continue;
						}
						
						for(int y3 : vertices[y2].get_neighbors())
						{
							if(y3 == x1 || y3 == x2 || y3 == x3)
							{
								continue; // ensure outside
							}
							if(!int_relate_ob.related(y3, y1))
							{
								continue;
							}
							if(vertices[y3].get_weight() < small_w)
							{
								continue;
							}
System.out.println("sole triangle subsumed");
							return true;
						}
					}
				}
				w--;
			}
			return false;
		}
		// at least one of the three vertices has neighbors outside
		if(vertices[x1].get_neighbors().size() == 2)
		{
			// ensure that the first has out neighbors
			if(vertices[x2].get_neighbors().size() != 2)
			{
					// swap(x1, x2);
					int temp = x1;
					x1 = x2;
					x2 = temp;
			}
			else if(vertices[x3].get_neighbors().size() != 2)
			{
					// swap(x1, x3);
					int temp = x1;
					x1 = x3;
					x3 = temp;
			}
		}
		// ensure that x2 must have out neighbors if x3 has
		if(vertices[x2].get_neighbors().size() == 2 && vertices[x3].get_neighbors().size() != 2)
		{ 
			// swap(x2, x3);
			int temp = x2;
			x2 = x3;
			x3 = temp;
		}
		// at this point, d(x1) > 2, d(x2) >= d(x3) >= 2
// System.out.println("x1: " + x1);
		for(int nb_x1 : vertices[x1].get_neighbors())
		{
			if(nb_x1 == x2 || nb_x1 == x3) continue; // no need to find dist-2 neighbors via x2 or x3
// System.out.println("nb_x1: " + nb_x1);
			for(int dist_2_nb_x1 : vertices[nb_x1].get_neighbors())
			{
				if(vertex_used_bits_1.get(dist_2_nb_x1)) continue; // avoid repetitive work
				vertex_used_bits_1.set(dist_2_nb_x1); // 

				if(dist_2_nb_x1 == x1) continue; // not allowed to find itself
				if(dist_2_nb_x1 == x2 || dist_2_nb_x1 == x3) continue; // ensure outside
// System.out.println("dist_2_nb_x1: " + dist_2_nb_x1);
				int y1 = dist_2_nb_x1;
				if(int_relate_ob.related(x1, y1)) continue; // must not be connected so can have the same color
				// implies that y1!=x2, y1!=x3
				if(vertices[y1].get_weight() < vertices[x1].get_weight()) continue; // weight constraint
				if(vertices[y1].get_degree() < vertices[x1].get_degree()) continue; // degree constraint
				if(!neighborhood_is_subset_except(x1, y1, vertices, x2, x3)) continue; // neighborhood subset constraint
// System.out.println(y1 + " covers " + x1);
				// having found a covering vertex y1 for x1

				// special cases
				if(vertices[x2].get_neighbors().size() == 2) // and d(x3) == 2
				{
					// obtain a triangle y1y2y3 s.t. y2, y3 dominates x2, x3 respectively
					for(int nb_y1 : vertices[y1].get_neighbors())
					{
						if(nb_y1 == x1 || nb_y1 == x2 || nb_y1 == x3) continue; // ensure outside
						for(int dist_2_nb_y1 : vertices[nb_y1].get_neighbors())
						{
							// no need to use bitset to avoid repetitions, since we are now looking for triangles not just dist-2 neighbors
							if(dist_2_nb_y1 < nb_y1) continue; // avoid repetitions
							if(dist_2_nb_y1 == y1) continue; // cannot find itself
							if(dist_2_nb_y1 == x1 || dist_2_nb_y1 == x2 || dist_2_nb_y1 == x3) continue; // ensure outside
							if(!int_relate_ob.related(y1, dist_2_nb_y1)) continue; // must form a triangle
							int y2 = nb_y1, y3 = dist_2_nb_y1;
							// now we have obtained a triangle
							boolean y2_y3_cover_x2_x3 = vertices[y2].get_weight() >= vertices[x2].get_weight() && vertices[y3].get_weight() >= vertices[x3].get_weight();
							boolean y2_y3_cover_x3_x2 = vertices[y2].get_weight() >= vertices[x3].get_weight() && vertices[y3].get_weight() >= vertices[x2].get_weight();
							if(y2_y3_cover_x2_x3 || y2_y3_cover_x3_x2) 
							{
System.out.println(x2 + " and " + x3 + " have no out neighbors, special triple-triple reductions applicable************************************************");
System.out.println("triple-triple dominance relation holds, " + x1 + ", " + x2 + ", " + x3 + " covered by " + y1 + ", " + y2 + " and " + y3);
								vertex_used_bits_1.clear();
								return true;
							}
						}
					}
					vertex_used_bits_1.clear();
					return false;
				}

				for(int nb_x2 : vertices[x2].get_neighbors())
				{
					if(nb_x2 == x1 || nb_x2 == x3) continue; // no need to be via this neighbor
// System.out.println("nb_x2: " + nb_x2);
					for(int dist_2_nb_x2 : vertices[nb_x2].get_neighbors())
					{
						if(vertex_used_bits_2.get(dist_2_nb_x2)) continue; // avoid repetitions
						vertex_used_bits_2.set(dist_2_nb_x2);

						if(dist_2_nb_x2 == x2) continue; // not allowed to find itself
						if(dist_2_nb_x2 == x1 || dist_2_nb_x2 == x3) continue; // ensure outside
// System.out.println("dist_2_nb_x2: " + dist_2_nb_x2);
						int y2 = dist_2_nb_x2;
						if(int_relate_ob.related(x2, y2)) continue; // must not be connected, so can be assigned the same color
						if(!int_relate_ob.related(y1, y2)) continue; // must be connected to form a triangle
						// implies that y2!=x1, y2!=x3
						if(vertices[y2].get_weight() < vertices[x2].get_weight()) continue;
						if(vertices[y2].get_degree() < vertices[x2].get_degree()) continue;
						if(!neighborhood_is_subset_except(x2, y2, vertices, x3, x1)) continue;
						// having found the second vertex
// System.out.println(dist_2_nb_x2 + " covers " + x2);
						// special cases
						if(vertices[x3].get_neighbors().size() == 2)
						{
							// obtain a triangle y1y2y3 s.t. y3 dominates x3
							for(int nb_y2 : vertices[y2].get_neighbors())
							{
								if(nb_y2 == x1 || nb_y2 == x2 || nb_y2 == x3) continue; // ensure outside
								if(nb_y2 == y1) continue; // must find vertices other than y1, y2
								if(!int_relate_ob.related(y1, nb_y2)) continue; // must form a triangle
								int y3 = nb_y2;
								if(vertices[y3].get_weight() < vertices[x3].get_weight()) continue; // weight constraints
System.out.println(x3 + " has no out neighbors, special triple-triple reductions applicable************************************************");
System.out.println("triple-triple dominance relation holds, " + x1 + ", " + x2 + ", " + x3 + " covered by " + y1 + ", " + y2 + " and " + y3);
								vertex_used_bits_1.clear();
								vertex_used_bits_2.clear();
								return true;
							}
							vertex_used_bits_1.clear();
							vertex_used_bits_2.clear();
							return false;
						}

						for(int nb_x3 : vertices[x3].get_neighbors())
						{
							if(nb_x3 == x1 || nb_x3 == x2) continue; // must obtain a neighbor outside
// System.out.println("nb_x3: " + nb_x3);
							for(int dist_2_nb_x3 : vertices[nb_x3].get_neighbors())
							{
								if(dist_2_nb_x3 == x1 || dist_2_nb_x3 == x2 || dist_2_nb_x3 == x3) continue; // ensure outside
								if(vertex_used_bits_3.get(dist_2_nb_x3)) continue; // avoid repetitions
// System.out.println("dist_2_nb_x3: " + dist_2_nb_x3);
								vertex_used_bits_3.set(dist_2_nb_x3);

								if(dist_2_nb_x3 == x3) continue; // cannot find itself
								int y3 = dist_2_nb_x3;
								if(!int_relate_ob.related(y2, y3)) continue;
								if(!int_relate_ob.related(y3, y1)) continue; // finally form a triangle

								if(int_relate_ob.related(x3, y3)) continue; // must not be connected so can have the same color
								// implies y3!=x1, y3!=x2
								if(vertices[y3].get_weight() < vertices[x3].get_weight()) continue;
								if(vertices[y3].get_degree() < vertices[x3].get_degree()) continue;
								if(!neighborhood_is_subset_except(x3, y3, vertices, x1, x2)) continue;
								// having found the third vertex
// System.out.println(dist_2_nb_x3 + " covers " + x3);
								// now triple-triple subset relation holds
/*
System.out.print("neighborhood of " + x1 + ": "); for(int z : vertices[x1].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.print("neighborhood of " + x2 + ": "); for(int z : vertices[x2].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.print("neighborhood of " + x3 + ": "); for(int z : vertices[x3].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.println("weight of " + x1 + " and " + x2 + " and " + x3 + " are " + vertices[x1].get_weight() + " and " + vertices[x2].get_weight() + " and " + vertices[x3].get_weight());
System.out.print("neighborhood of " + y1 + ": "); for(int z : vertices[y1].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.print("neighborhood of " + y2 + ": "); for(int z : vertices[y2].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.print("neighborhood of " + y3 + ": "); for(int z : vertices[y3].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.println("weight of " + y1 + " and " + y2 + " and " + y3 + " are " + vertices[y1].get_weight() + " and " + vertices[y2].get_weight() + " and " + vertices[y3].get_weight());
System.out.println(x1 + "\'s neighborhood is a subset of that of " + y1 + " except: " + x2 + " and " + x3);
System.out.println(x2 + "\'s neighborhood is a subset of that of " + y2 + " except: " + x3 + " and " + x1);
System.out.println(x3 + "\'s neighborhood is a subset of that of " + y3 + " except: " + x1 + " and " + x2);
*/
System.out.println("triple-triple dominance relation holds, " + x1 + ", " + x2 + ", " + x3 + " covered by " + y1 + ", " + y2 + " and " + y3);
// System.out.println("*****************************************************");

//UsrPause.press_enter_to_continue();
								vertex_used_bits_1.clear();
								vertex_used_bits_2.clear();
								vertex_used_bits_3.clear();
								return true;
							}
						} // for(int nb_x3 : vertices[x3].get_neighbors())
						vertex_used_bits_3.clear();
					}
				} // for(int nb_x2 : vertices[x2].get_neighbors())
				vertex_used_bits_2.clear();
			}
		} // for(int nb_x1 : vertices[x1].get_neighbors())
		vertex_used_bits_1.clear();
		return false;
	} // public boolean is_triple_triple_dominated(int x1, int x2, int x3, WeightedVertex[] vertices, IntRelate int_relate_ob, WeightBasedPartition weight_based_partition)

	/*We must always ensure that auxillary vertices and covering vertices are outside*/
	public boolean is_link_link_dominated(int x1, int x2, int x3, WeightedVertex[] vertices, IntRelate int_relate_ob)
	// preconditions: x1-x2-x3 is a 2-dist path, x1 and x3 must not be connected
	{
		// after single-single is applied, at most one endpoint is of degree 1
		if(vertices[x1].get_degree() == 1) // and x3 != 1
		{
			int temp = x1;
			x1 = x3;
			x3 = temp;
		}

		for(int nb_x1 : vertices[x1].get_neighbors())
		{
			if(nb_x1 == x2) // or nb_x1==x3, note that x1 and x3 are not connected
				continue;
			for(int dist_2_nb_x1 : vertices[nb_x1].get_neighbors())
			{
				if(vertex_used_bits_1.get(dist_2_nb_x1)) continue;
				vertex_used_bits_1.set(dist_2_nb_x1); // avoid repetitions

				if(dist_2_nb_x1 == x1) continue; // must find a neighbor outside
				if(dist_2_nb_x1 == x2 || dist_2_nb_x1 == x3) continue; // must obtain a dominating vertex outside
				int y1 = dist_2_nb_x1;
				if(int_relate_ob.related(y1, x1)) continue; // must not be connected so can have the same color
				if(vertices[y1].get_weight() < vertices[x1].get_weight()) continue; // weight constraint
				if(vertices[y1].get_degree() < vertices[x1].get_degree()) continue; // degree constraint
				if(!neighborhood_is_subset_except(x1, y1, vertices, x2)) continue; // subset constraint
				// having obtained a covering vertex for x1
				
				// enumerate all the links
				for(int nb_y1 : vertices[y1].get_neighbors())
				{
					if(nb_y1 == x2 || nb_y1 == x3) continue; // must obtain a neighbor outside, since y1 is not connected to x1, we have nb_y1 != x1
					int y2 = nb_y1;
					// constraints on y2
					if(int_relate_ob.related(y2, x2)) continue; // must not be connected so can have the same color
					if(vertices[y2].get_weight() < vertices[x2].get_weight()) continue; // weight constraint
					if(vertices[y2].get_degree() < vertices[x2].get_degree()) continue; // degree constraint
					if(!neighborhood_is_subset_except(x2, y2, vertices, x1, x3)) continue; // subset constraint			
		
					for(int dist_2_nb_y1 : vertices[nb_y1].get_neighbors())
					{
						if(dist_2_nb_y1 == y1) continue; // cannot find itself, must form a link (can be a triangle)
						if(dist_2_nb_y1 == x1 || dist_2_nb_y1 == x2 || dist_2_nb_y1 == x3) continue; // must obtain a neighbor outside the shadow
						int y3 = dist_2_nb_y1;
						// constraints on y3
						if(int_relate_ob.related(y3, x3)) continue; // must not be connected so can have the same color
						if(vertices[y3].get_weight() < vertices[x3].get_weight()) continue; // weight constraint
						if(vertices[y3].get_degree() < vertices[x3].get_degree()) continue; // degree constraint
						if(!neighborhood_is_subset_except(x3, y3, vertices, x2)) continue; // subset constraint
/*
System.out.println(x1 + " and " + x3 + " is connected? " + int_relate_ob.related(x1, x3));
System.out.println(x1 + " and " + x2 + " is connected? " + int_relate_ob.related(x1, x2));
System.out.println(x2 + " and " + x3 + " is connected? " + int_relate_ob.related(x2, x3));
System.out.print("neighborhood of " + x1 + ": "); for(int z : vertices[x1].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.print("neighborhood of " + x2 + ": "); for(int z : vertices[x2].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.print("neighborhood of " + x3 + ": "); for(int z : vertices[x3].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.println("weight of " + x1 + " and " + x2 + " and " + x3 + " are " + vertices[x1].get_weight() + " and " + vertices[x2].get_weight() + " and " + vertices[x3].get_weight());
System.out.println(y1 + " and " + y3 + " is connected? " + int_relate_ob.related(y1, y3));
System.out.println(y1 + " and " + y2 + " is connected? " + int_relate_ob.related(y1, y2));
System.out.println(y2 + " and " + y3 + " is connected? " + int_relate_ob.related(y2, y3));
System.out.print("neighborhood of " + y1 + ": "); for(int z : vertices[y1].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.print("neighborhood of " + y2 + ": "); for(int z : vertices[y2].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.print("neighborhood of " + y3 + ": "); for(int z : vertices[y3].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.println("weight of " + y1 + " and " + y2 + " and " + y3 + " are " + vertices[y1].get_weight() + " and " + vertices[y2].get_weight() + " and " + vertices[y3].get_weight());
System.out.println(x1 + "\'s neighborhood is a subset of that of " + y1 + " except: " + x2);
System.out.println(x2 + "\'s neighborhood is a subset of that of " + y2 + " except: " + x3 + " and " + x1);
System.out.println(x3 + "\'s neighborhood is a subset of that of " + y3 + " except: " + x2);
*/
System.out.println(x1 + ", " + x2 + ", " + x3 + " are dominated, link-link reductions applicable*****************************************************");
/*
System.out.println("dominated by " + y1 + ", " + y2 + ", " + y3);
UsrPause.press_enter_to_continue();
*/
						vertex_used_bits_1.clear();
						return true;
					}
				}
			}
		}
		vertex_used_bits_1.clear();
		return false;
	}

	public boolean is_quadruple_double_dominated(int x1, int x2, int y1, int y2, WeightedVertex[] vertices, IntRelate int_relate_ob)
	// preconditions: x1 and x2 are not connected, neither are y1 and y2, not necessarily be a quadraliteral
	{
		// with single-single applied before, 
		// we can have at least two vertices s.t.
		// (1) they are connected to vertices other than x1, x2, y1, y2; (2) one is x1 or x2 and the other is y1 or y2
		// vertex_used_bits_1.clear();
		// vertex_used_bits_2.clear();
// System.out.println("having obtained a qua. " + x1 + ", " + x2 + ", " + y1 + ", " + y2);
		
		// 
		if(vertices[x1].get_degree() == 2) // then d(x2) > 2 must hold
		{
			int temp = x1; x1 = x2; x2 = temp; // swap so that d(x1) > 2
		}
		if(vertices[y1].get_degree() == 2) // then d(y2) > 2 must hold
		{
			int temp = y1; y1 = y2; y2 = temp; // swap so that d(y1) > 2
		}

		for(int nb_x : vertices[x1].get_neighbors())
		{
			if(nb_x == y1 || nb_x == y2) continue; // ignoring this case will not lose any possibility
			for(int dist_2_nb_x : vertices[nb_x].get_neighbors())
			{
				if(vertex_used_bits_1.get(dist_2_nb_x)) continue; // avoid repetitions
				vertex_used_bits_1.set(dist_2_nb_x);

				if(dist_2_nb_x == x1) continue; // 
				if(dist_2_nb_x == x2) continue; // must find a vertex outside the quadr.

				int u = dist_2_nb_x;
				if(u == x1 || u == x2) continue;
				if(u == y1 || u == y2) continue; // must be a vertex outside this quadr
				if(int_relate_ob.related(x1, u) || int_relate_ob.related(x2, u)) continue; // not connected so can have the same color
				if(vertices[u].get_weight() < vertices[x1].get_weight() || vertices[u].get_weight() < vertices[x2].get_weight()) continue; // weight constraint
				if(vertices[u].get_degree() < vertices[x1].get_degree() - 1 || vertices[u].get_degree() < vertices[x2].get_degree() - 1)
				{
					continue; // degree constraint, consider the extreme case where x1, x2, y1, y2 form a triangle
				}
				if(!neighborhood_is_subset_except(x1, u, vertices, y1, y2)) continue; // neighborhood subset constraints
				if(!neighborhood_is_subset_except(x2, u, vertices, y1, y2)) continue;

				// special cases, no need to deal with this case, see above
/*
				if(vertices[y1].get_degree() == 2) // and d(y2) = 2
				{
					// obtain a vertex v s.t. u and v are neighbors
					for(int v : vertices[u].get_neighbors())
					{
						if(int_relate_ob.related(y1, v) || int_relate_ob.related(y2, v)) continue; // not connected so can have the same color
						// implies v!=x1 and v!=x2
						if(vertices[v].get_weight() < vertices[y1].get_weight() || vertices[v].get_weight() < vertices[y2].get_weight()) continue; // weight constraints
System.out.println(y1 + " and " + y2 + " have no out neighbors, special quadruple-double reductions applicable*********************************************");
UsrPause.press_enter_to_continue();
						return true;
					}					

					return false;
				}
*/

				for(int nb_y : vertices[y1].get_neighbors())
				{
					if(nb_y == x1 || nb_y == x2) continue; // ignoring this case will not lose any possibility
					for(int dist_2_nb_y : vertices[nb_y].get_neighbors())
					{
						if(dist_2_nb_y == x1 || dist_2_nb_y == x2 || dist_2_nb_y == y1 || dist_2_nb_y == y2) continue; // ensure outside
						if(vertex_used_bits_2.get(dist_2_nb_y)) continue; // avoid repetitions
						vertex_used_bits_2.set(dist_2_nb_y);

						if(dist_2_nb_y == y1) continue; // 
						if(dist_2_nb_y == y2) continue; // must find a vertex outside qua.

						if(dist_2_nb_x == dist_2_nb_y) continue; // should use different vertices for covering

						int v = dist_2_nb_y;
						if(v == x1 || v == x2) continue; // must be a vertex outside this quadr
						if(v == y1 || v == y2) continue;
						if(int_relate_ob.related(y1, v) || int_relate_ob.related(y2, v)) continue; // not connected so can have the same color
						if(vertices[v].get_weight() < vertices[y1].get_weight() || vertices[v].get_weight() < vertices[y2].get_weight()) continue; // weight constraint
						if(vertices[v].get_degree() < vertices[y1].get_degree() - 1 || vertices[v].get_degree() < vertices[y2].get_degree() - 1) 
						{
							continue; // // degree constraint, consider the extreme case where x1, x2, y1, y2 form a triangle
						}
						if(!neighborhood_is_subset_except(y1, v, vertices, x1, x2)) continue; // neighborhood subset constraints
						if(!neighborhood_is_subset_except(y2, v, vertices, x1, x2)) continue;

						if(!int_relate_ob.related(u, v)) continue; // must be connected to ensure different colors
						//
/*
System.out.print("neighborhood of " + x1 + ": "); for(int z : vertices[x1].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.print("neighborhood of " + x2 + ": "); for(int z : vertices[x2].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.print("neighborhood of " + u + ": "); for(int z : vertices[u].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.println("weight of " + x1 + " and " + x2 + " are " + vertices[x1].get_weight() + " and " + vertices[x2].get_weight());
System.out.println("weight of " + u + " is " + vertices[u].get_weight());
System.out.print("neighborhood of " + y1 + ": "); for(int z : vertices[y1].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.print("neighborhood of " + y2 + ": "); for(int z : vertices[y2].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.print("neighborhood of " + v + ": "); for(int z : vertices[v].get_neighbors()) System.out.print(z + "\t"); System.out.println();
System.out.println("weight of " + y1 + " and " + y2 + " are " + vertices[y1].get_weight() + " and " + vertices[y2].get_weight());
System.out.println("weight of " + v + " is " + vertices[v].get_weight());
System.out.println(x1 + "\'s neighborhood is a subset of that of " + u + " except: " + y1 + " and " + y2);
System.out.println(x2 + "\'s neighborhood is a subset of that of " + u + " except: " + y1 + " and " + y2);
System.out.println(y1 + "\'s neighborhood is a subset of that of " + v + " except: " + x1 + " and " + x2);
System.out.println(y2 + "\'s neighborhood is a subset of that of " + v + " except: " + x1 + " and " + x2);

System.out.println("quadruple-double dominance relation holds, " + x1 + ", " + x2 + ", " + y1 + ", " + y2 + " covered by " + u + " and " + v);
// UsrPause.press_enter_to_continue();
*/
						vertex_used_bits_1.clear();
						vertex_used_bits_2.clear();
						return true;						
					}
				} // for(int nb_y : vertices[y1].get_neighbors())
				vertex_used_bits_2.clear();
			}
		} // for(int nb_x : vertices[x1].get_neighbors())
		vertex_used_bits_1.clear();
		return false;
	} // public boolean is_quadruple_double_dominated(int x1, int x2, int y1, int y2, WeightedVertex[] vertices, IntRelate int_relate_ob)


} //
