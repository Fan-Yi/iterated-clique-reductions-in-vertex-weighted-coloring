package GraphPack.GraphFormatParserPack;

import java.io.*;
import java.util.*;


public class SimpleUndirectedVertexWeightedGraphDIMACSParser{
	public static boolean deal_with_head_line(Scanner con_s_in, int[] a_int)
	{
				if(con_s_in.findInLine("p edge ") == null && con_s_in.findInLine("p Edge ") == null && con_s_in.findInLine("p col ") == null && con_s_in.findInLine("p edges ") == null)
				{
					System.out.println("Format error occurs when reading the head line: the first two strings are invalid.");
					return false;
				}
				if(con_s_in.hasNextInt())
				{
					try{
						a_int[0] = Integer.valueOf(con_s_in.next());
					}catch(NumberFormatException e)
					{
						System.out.println("Format error occurs when reading the head line: 3rd token is invalid.");
						return false;
					}
					if(con_s_in.hasNextInt())
					{
						try{
							a_int[1] = Integer.valueOf(con_s_in.next());
						}catch(NumberFormatException e)
						{
							System.out.println("Format error occurs when reading the head line: 4th token is invalid.");
							return false;
						}
					}
					else
					{
						System.out.println("Format error occurs when reading the head line: not enough arguments.");
						return false;
					}
				}
				else
				{
					System.out.println("Format error occurs when reading the head line: not enough arguments.");
					return false;
				}
				return true;
	}

	public static boolean deal_with_vertex_line(Scanner con_s_in, int[] a_int)
	{
				if(con_s_in.findInLine("v ") == null && con_s_in.findInLine("n ") == null)
				{
					System.out.println("Format error occurs when reading the v line: the first two chars are invalid.");
					return false;
				}

				if(con_s_in.hasNextInt())
				{
					try{
						a_int[0] = Integer.valueOf(con_s_in.next());
					}catch(NumberFormatException e)
					{
						System.out.println("Format error occurs when reading the head line: the 2nd token is invalid.");
						return false;
					}
					if(con_s_in.hasNextInt())
					{
						try{
							a_int[1] = Integer.valueOf(con_s_in.next());
						}catch(NumberFormatException e)
						{
							System.out.println("Format error occurs when reading the head line: the 3rd token is invalid.");
							return false;
						}
					}
					else
					{
						System.out.println("Format error occurs when reading the head line: not enough arguments.");
						return false;
					}
				}
				else
				{
					System.out.println("Format error occurs when reading the head line: not enough arguments.");
					return false;
				}
				return true;
	}

	public static boolean deal_with_edge_line(Scanner con_s_in, int[] a_int)
	{
				if(con_s_in.findInLine("e ") == null)
				{
					System.out.println("Format error occurs when reading the edge line: the first two chars are invalid.");
					return false;
				}

				if(con_s_in.hasNextInt())
				{
					try{
						a_int[0] = Integer.valueOf(con_s_in.next());
					}catch(NumberFormatException e)
					{
						System.out.println("Format error occurs when reading the head line: the 2nd token is invalid.");
						return false;
					}
					if(con_s_in.hasNextInt())
					{
						try{
							a_int[1] = Integer.valueOf(con_s_in.next());
						}catch(NumberFormatException e)
						{
							System.out.println("Format error occurs when reading the head line: the 3rd token is invalid.");
							return false;
						}
					}
					else
					{
						System.out.println("Format error occurs when reading the head line: not enough arguments.");
						return false;
					}
				}
				else
				{
					System.out.println("Format error occurs when reading the head line: not enough arguments.");
					return false;
				}
				return true;		
	}


}
