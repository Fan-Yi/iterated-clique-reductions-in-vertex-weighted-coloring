// using directories
import java.io.*;
import java.util.*;

import DelFolderPack.DeleteFolder;
import UsrPausePack.UsrPause;
import GraphPack.WGCReducedGraph;
import GraphPack.WeightedVertexPack.VertexPack.Vertex;

import CliqueSearchPack.CliqueSearch;
import DegreeBucketPack.DegreeBasedPartition;

import UsrPausePack.UsrPause;


class MinWGCPreprocessing
{
	final static boolean REQUEST_SHRUNK_GRAPH = true;

	private static boolean check_and_deal_with_preexisting_folder(File out_folder)
	// return false if a folder with naming conflicts exists but users choose to keep it,
	// so cannot do anything further
	// return true otherwise
	{
/*
		// below ask users whether to continue if the given folder name exists
		if(!out_folder.exists())
			return true;

		System.out.println(out_folder.getName() + " exists");
		System.out.println("Do you want to delete this folder together with all its contents? (y/n)");

		char c = 0;
		do{
			try{
				c = (char)System.in.read();					
			}catch(IOException e)
			{
				System.out.println("Input error occurs when reading a character from the user: " + e);
			}		
		}while(c != 'y' && c != 'n');

		if(c == 'y')
		{
			DeleteFolder df = new DeleteFolder();
			df.rmdir(out_folder);
		}
		else
		{
			// keep
			return false;
		}

		return true;	
*/	
		if(!out_folder.exists())
			return true;
			
		DeleteFolder df = new DeleteFolder();
		df.rmdir(out_folder);	
		return true;
	}

	public static void main(String args[])
	{

		// First, ensure there are 3 inputs
	    if (args.length != 4) {
	      throw new IllegalArgumentException("Exactly 3 arguments required!");
	    }

		// deal with input folder
				
		String instance_file_name_with_path = args[0];
		System.out.println("Instance File Name with Paths: " + instance_file_name_with_path);
/*
		File instance_folder = new File(instance_folder_name);
		if(!instance_folder.isDirectory())
		{
			System.out.println(instance_folder_name + " is not a directory.");
			return;
		}
*/
		
		String str = args[1];
		int seed = 1;
        try{
            seed = Integer.parseInt(str);
        }
        catch(NumberFormatException ex){
            ex.printStackTrace();
        }

		// String instance_file_name_list_str[] = instance_folder.list();

		// deal with output folder		
		if(instance_file_name_with_path.substring(instance_file_name_with_path.length() - 5, instance_file_name_with_path.length()).equals(".mtx"))
			instance_file_name_with_path = instance_file_name_with_path.substring(0, instance_file_name_with_path.length() - 5);
		String instance_file_name = instance_file_name_with_path.substring(instance_file_name_with_path.lastIndexOf("/") + 1, instance_file_name_with_path.length());

		String output_instance_folder_name = args[2] + "/" + instance_file_name;
		File output_instance_folder = new File(output_instance_folder_name);
		if(!check_and_deal_with_preexisting_folder(output_instance_folder))
		{
			System.out.println(output_instance_folder_name + " keeps unchanged.");
			return;
		}
		
		if(!output_instance_folder.mkdir())
		{
			System.out.println(output_instance_folder_name + " cannot be created");
			return;
		}
		
		str = args[3];
		long run_time_cut_off = 0;
		try{
            run_time_cut_off = Long.parseLong(str) * 1000;
        }
        catch(NumberFormatException ex){
            ex.printStackTrace();
        }

		// enumerate each instance file in the input folder
		// for(String instance_file_name : instance_file_name_list_str)
		//{
			// mark starting time
			Date date = new Date();
			long start_elap_milliseconds_from_past = date.getTime();

			// construct a graph
			WGCReducedGraph instance_graph = new WGCReducedGraph(instance_file_name_with_path, seed, start_elap_milliseconds_from_past, run_time_cut_off);

			// Below are clique-based algorithms
///*
// System.out.println("**********************************************************");
			// instance_graph.apply_reductions_with_randomly_selected_vertices(instance_graph.get_vertex_num() / 100, 3);
// System.out.println("**********************************************************");
			// instance_graph.apply_reductions_with_sampled_cliques_containing_top_weight_and_degree_vertices(1);
			instance_graph.apply_reductions_with_enumerated_cliques_containing_top_weight_vertices();
			instance_graph.apply_reductions_with_enumerated_cliques_containing_top_degree_vertices();	
System.out.println("after enumerating cliques with top weight/degree vertices, remaining vertex count: " + instance_graph.get_remaining_vertex_set().size());
// System.out.println("**********************************************************");		
			instance_graph.apply_reductions_with_new_critical_cliques();
System.out.println("after enumerating cliques wrt each possible color, remaining vertex count: " + instance_graph.get_remaining_vertex_set().size());
// System.out.println("**********************************************************");	
			// instance_graph.apply_later_reductions_with_clique_sampling(instance_graph.get_vertex_num() / 100);
System.out.println("eventual top_level_weights_wrt_colors: " + "(size " + instance_graph.get_top_level_weights_wrt_colors().size() + ") " + instance_graph.get_top_level_weights_wrt_colors());
System.out.println("can_exist_clique_above_at_color: " + instance_graph.get_can_exist_clique_above_at_color());
if(!instance_graph.get_can_exist_clique_above_at_color().contains(true))
System.out.println("This bound is perfect in terms of clique reductions.");
// System.out.println("**********************************************************");		
			instance_graph.apply_post_reductions();
System.out.println("after post reductions, remaining vertex count: " + instance_graph.get_remaining_vertex_set().size());
System.out.println("remaining edge count: " + instance_graph.get_remaining_edge_set().size());
//*/

// System.out.println("**********************************************************");

// instance_graph.show_vertex_info_and_top_level_weights();

			// Below are shadow-based algorithms

			// instance_graph.apply_shadow_reductions();

			instance_graph.set_id_map_in_shrinking();

// instance_graph.check_graph();

System.out.println("critical clique list: (" + instance_graph.get_critical_clique_list().size() + " cliques)");
for(ArrayList<Integer> clq : instance_graph.get_critical_clique_list())
{
System.out.println(clq);
}
System.out.println("random maximum clique found: " + instance_graph.get_random_maximum_clique_found(true));

			double rate = (double)instance_graph.get_remaining_vertex_set().size() / (instance_graph.get_remaining_vertex_set().size() + instance_graph.get_removed_vertex_list().size());
			System.out.println("remain rate is " + String.format("%.4f", rate));
			
			date = new Date();
			long end_elap_milliseconds_from_past = date.getTime();
			long elap_time = end_elap_milliseconds_from_past - start_elap_milliseconds_from_past;

			System.out.println("The time past is " + (double)elap_time / 1000 + "s");
			
///*
			String output_instance_file_name = output_instance_folder_name + "/" + instance_file_name;
			// open output file
			File output_instance_file = new File(output_instance_file_name);
			try{
				FileWriter fw = new FileWriter(output_instance_file);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(instance_graph.toString(REQUEST_SHRUNK_GRAPH));
				bw.flush();
				fw.close();
			}catch(IOException e)
			{
				System.out.println("Exception occurs when writing a line to file: " + e);
			}
			
System.out.println("having output the reduced instance to files");
			date = new Date();
			end_elap_milliseconds_from_past = date.getTime();
			elap_time = end_elap_milliseconds_from_past - start_elap_milliseconds_from_past;
			System.out.println("The time past is " + (double)elap_time / 1000 + "s");
			
			String bijection_file_name = output_instance_folder_name + "/" + instance_file_name;
			if(bijection_file_name.substring(bijection_file_name.length() - 4, bijection_file_name.length()).equals(".txt"))
			{
				bijection_file_name = bijection_file_name.substring(0, bijection_file_name.length() - 4) + "_bijection.txt";
			}
			else
			{
				bijection_file_name = bijection_file_name + "_bijection.txt";
			}
			File bijection_file = new File(bijection_file_name);
			try{
				FileWriter fw = new FileWriter(bijection_file);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(instance_graph.get_id_map_in_shrinking().toString());
				bw.flush();
				fw.close();
			}catch(IOException e)
			{
				System.out.println("Exception occurs when writing a line to file: " + e);
			}
			
System.out.println("having output the bijection to files");		
			date = new Date();
			end_elap_milliseconds_from_past = date.getTime();
			elap_time = end_elap_milliseconds_from_past - start_elap_milliseconds_from_past;
			System.out.println("The time past is " + (double)elap_time / 1000 + "s");
				
	
			String max_clq_file_name = output_instance_folder_name + "/" + instance_file_name;
			if(max_clq_file_name.substring(max_clq_file_name.length() - 4, max_clq_file_name.length()).equals(".txt"))
			{
				max_clq_file_name = max_clq_file_name.substring(0, max_clq_file_name.length() - 4) + "_max_clq.txt";
			}
			else
			{
				max_clq_file_name = max_clq_file_name + "_max_clq.txt";
			}
			
			File max_clq_file = new File(max_clq_file_name);
			try{
				FileWriter fw = new FileWriter(max_clq_file);
				BufferedWriter bw = new BufferedWriter(fw);
				ArrayList<Integer> temp_list = instance_graph.get_random_maximum_clique_found(false);
				for(int v : temp_list)
					bw.write(Integer.toString(v) + "\n");
				bw.flush();
				fw.close();
			}catch(IOException e)
			{
				System.out.println("Exception occurs when writing a line to file: " + e);
			}
			
System.out.println("having output a critical clique to files");			
			date = new Date();
			end_elap_milliseconds_from_past = date.getTime();
			elap_time = end_elap_milliseconds_from_past - start_elap_milliseconds_from_past;
			System.out.println("The time past is " + (double)elap_time / 1000 + "s");
			
			
			String lower_bound_file_name = output_instance_folder_name + "/" + instance_file_name;
			if(lower_bound_file_name.substring(lower_bound_file_name.length() - 4, lower_bound_file_name.length()).equals(".txt"))
			{
				lower_bound_file_name = lower_bound_file_name.substring(0, lower_bound_file_name.length() - 4) + "_lower_bound.txt";
			}
			else
			{
				lower_bound_file_name = lower_bound_file_name + "_lower_bound.txt";
			}
			
			File lower_bound_file = new File(lower_bound_file_name);
			try{
				FileWriter fw = new FileWriter(lower_bound_file);
				BufferedWriter bw = new BufferedWriter(fw);
				int lb = instance_graph.get_lower_bound_by_cliques();
				bw.write(Integer.toString(lb));
				bw.flush();
				fw.close();
			}catch(IOException e)
			{
				System.out.println("Exception occurs when writing a line to file: " + e);
			}
			
System.out.println("having output a lower bound to files");		
			date = new Date();
			end_elap_milliseconds_from_past = date.getTime();
			elap_time = end_elap_milliseconds_from_past - start_elap_milliseconds_from_past;
			System.out.println("The time past is " + (double)elap_time / 1000 + "s");
				
//*/

			String latex_source_string = new String();
			if(instance_file_name.substring(instance_file_name.length() - 5, instance_file_name.length()).equals(".dimw"))
				latex_source_string += instance_file_name.substring(0, instance_file_name.length() - 5);
			else
				latex_source_string += instance_file_name;
			latex_source_string += instance_graph.vertex_and_edge_num_change_info_to_string();
			latex_source_string += " & " + String.format("%.4f", rate);
			latex_source_string += " & " + (double)elap_time / 1000;
			System.out.println(latex_source_string);
			
			date = new Date();
			end_elap_milliseconds_from_past = date.getTime();
			elap_time = end_elap_milliseconds_from_past - start_elap_milliseconds_from_past;

			System.out.println("After executing the last statement, the time past is " + (double)elap_time / 1000 + "s");			
		//} // for(String instance_file_name : instance_file_name_list_str)			
	} // for(String instance_file_name : instance_file_name_list_str)
}
