import java.util.Random;

public class ConnGenerator
{
    //methods that return x and y coordinate of square above current square (assuming vertical & horizontal wrap-around)
    private static int xUp(int x, int y, int width, int height)
    {
        if (x == width - 1)
            return 0;
        else
            return x + 1;
    }
    private static int xDown(int x, int y, int width, int height)
    {
        if (x == 0)
            return width - 1;
        else
            return x - 1;
    }
    private static int yUp(int x, int y, int width, int height)
    {
        if (y == height - 1)
            return 0;
        else
            return y + 1;
    }
    private static int yDown(int x, int y, int width, int height)
    {
        if (y == 0)
            return height - 1;
        else
            return y - 1;
    }
    
    //takes a 1D square number and returns the x-coordinate of 2D location
    private static int xOf(int sqNum, int width, int height)
    {
        return sqNum / width;
    }
    
    //takes a 1D square number and returns the y-coordinate of 2D location
    private static int yOf(int sqNum, int width, int height)
    {
        return sqNum % height;
    }
    
    //takes a 2D xy-coordinate and returns the 1D square number
    private static int square(int x, int y, int width, int height)
    {
        return y * width + x;
    }
    
    //returns the nearest neighbor adjacency matrix of a lattice
    //assumes horizontal and vertical wraparounds
    public static boolean[][] allNoWall(int height, int width)
    {
        //declare and initialize variables
        boolean[][] ans = new boolean[width * height][width * height];
        int currX, currY;
        int newX, newY;
        
        //each iteration finds set of neighbors for one square
        for (int i = 0; i < ans.length; i++)
        {
            //current location
            currX = xOf(i, width, height);
            currY = yOf(i, width, height);
            
            //if (currX > 0)
            {
                newY = currY;
                newX = xDown(currX, currY, width, height);
                ans[square(currX, currY, width, height)][square(newX, newY, width, height)] = true;
            }
            //if (currY > 0)
            {
                newY = yDown(currX, currY, width, height);
                newX = currX;
                ans[square(currX, currY, width, height)][square(newX, newY, width, height)] = true;
            }
            //if (currX < width - 1)
            {
                newY = currY;
                newX = xUp(currX, currY, width, height);
                ans[square(currX, currY, width, height)][square(newX, newY, width, height)] = true;
            }
            //if (currY < height - 1)
            {
                newY = yUp(currX, currY, width, height);
                newX = currX;
                ans[square(currX, currY, width, height)][square(newX, newY, width, height)] = true;
            }
        }
        
        return ans;
    }
    public static boolean[][] allWall(int height, int width)
    {
        //declare and initialize variables
        boolean[][] ans = new boolean[width * height][width * height];
        int currX, currY;
        int newX, newY;
        
        //each iteration finds set of neighbors for one square
        for (int i = 0; i < ans.length; i++)
        {
            //current location
            currX = xOf(i, width, height);
            currY = yOf(i, width, height);
            
            if (currX > 0)
            {
                newY = currY;
                newX = xDown(currX, currY, width, height);
                ans[square(currX, currY, width, height)][square(newX, newY, width, height)] = true;
            }
            if (currY > 0)
            {
                newY = yDown(currX, currY, width, height);
                newX = currX;
                ans[square(currX, currY, width, height)][square(newX, newY, width, height)] = true;
            }
            if (currX < width - 1)
            {
                newY = currY;
                newX = xUp(currX, currY, width, height);
                ans[square(currX, currY, width, height)][square(newX, newY, width, height)] = true;
            }
            if (currY < height - 1)
            {
                newY = yUp(currX, currY, width, height);
                newX = currX;
                ans[square(currX, currY, width, height)][square(newX, newY, width, height)] = true;
            }
        }
        
        return ans;
    }
    
    public static boolean[][] wattsStrogatz(int height, int width, boolean wall, double beta)
    {
        boolean[][] initial = (wall ? allWall(height, width) : allNoWall(height, width));
        int numEmpty = height * width - 5;  //at max 4 connections (so 4 must be filled)
        Random generator = new Random(42);
        
        //move around connections
        int counter, tempAt, numMove;
        for (int from = 0; from < initial.length; from++)
        {
            for (int to = 0; to < initial[from].length; to++)
            {
                if (!initial[from][to] || generator.nextDouble() > beta)
                    continue;
                    
                //need to randomize this edge
                counter = 0;
                tempAt = (to + 1) % initial[from].length;
                numMove = generator.nextInt(numEmpty);
                for (; counter < numMove || initial[from][tempAt] || from == tempAt; tempAt = (tempAt + 1) % initial[from].length)
                {
                    if (initial[from][tempAt] || from == tempAt)
                        continue;
                    else
                        counter++;
                }
                
                //set curr edge to false and make new edge (keep number of edges constant)
                initial[from][to] = false;
                initial[from][tempAt] = true;
            }
        }
        
        return initial;
    }
}