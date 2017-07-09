import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.List;

//SimQ = simulation queue
public class SimQ
{
	//holds all simulations
	private BlockingQueue<ParamSet> q;	//needs to be a BlockingQueue (using take())
	private int size;	//keep track manually to avoid issues due to concurrency
	
	//empty constructor (just initialization)
	public SimQ()
	{
		q = new LinkedBlockingQueue<ParamSet>();
		size = 0;
	}
	
	//add simulations
	public void addSimulations(List<ParamSet> sims)
	{
		for (ParamSet curr : sims)
		{
			q.add(curr);
			size++;
		}
	}
	
	//get next ParamSet (thread-safe w/ BlockingQueue for multiple threads to access)
	//returns null if q is empty
	public ParamSet nextSim() throws InterruptedException
	{
		if (size > 0)
		{
			ParamSet temp = q.take();
			size--;
			return temp;
		}
		else if (size == 0)
			return null;
		else
			throw new RuntimeException("Something wrong if the code reached here.");
	}
}