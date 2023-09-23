package DebugUtil;

public interface Debug    
{    
    public final boolean ENABLE = true;    
} 

public class Hello    
{    
    if(Debug.ENABLE)    
    {    
        System.out.println("This is debug message.");    
    }    
}  
