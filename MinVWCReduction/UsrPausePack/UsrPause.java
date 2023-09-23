package UsrPausePack; 

import java.io.*;

public class UsrPause {
/**
 * Request key press from users.
 * no param
 */
  public static void press_enter_to_continue()
	{
		try{
			System.out.println("Press enter to continue...");
			char ch = (char)System.in.read();
		}catch(IOException e)
		{
			System.out.println("Input error occurs when reading a character from the user: " + e);
			return;
		}
	}
}
