package sha.suite.raven;

import java.util.ArrayList;
import java.util.List;

class CommonCoins
{
	//Following variables used to track processed exchange coins
	private List<List<Coin>> _coins;
	
	//Contains CoinRoster objects loaded into this object to be processed
	private List<CoinRoster> _cr;	
	
	CommonCoins()
	{
		_coins = new ArrayList<List<Coin>>();
		_cr = new ArrayList<CoinRoster>();
	}
	
	/**
	 * <p>Adds a Coin[] to hold more exch-coins.</p>
	 */
	private void addCoin() //------------------------------------------------------------
	{
		_coins.add(new ArrayList<Coin>());
	}
	
	/**
	 * <p>Adds a coin to one of the common indices--should have an exchange unique from those already in this Coin[] array.</p>
	 * @param coin
	 */
	private void addExchCoin(int coin, Coin c)
	{
		_coins.get(coin).add(c);
	}
	
	/**
	 * <p>Returns a List<Coin> object containing common coins over different exchanges</p>
	 * @return
	 */
	public List<Coin> getCommonCoinRow(int coin)
	{
		return _coins.get(coin);
	}
	
	/**
	 * <p>Returns a List<Coin> object containing common coins over different exchanges</p>
	 * @param coin
	 * @return
	 */
	public List<Coin> getCommonCoinRow(String coin)
	{
		for (int i = 0; i < _coins.size(); i++)
		{
			if (coin.toUpperCase().contentEquals(_coins.get(i).get(0).getPriCode().toUpperCase()))
			{
				return _coins.get(i);
			}
		}
		return null;
	}
	
	/**
	 * <p>Returns a String array containing all coin names.</p>
	 */
	public String [] getCoinNames()
	{
		String [] out = new String[_coins.size()];
		for (int i = 0; i < out.length; i++)
		{
			if (_coins.get(i) != null)
			{
				out[i] = _coins.get(i).get(0).getPriCode();
			}
		}
		return out;
	}
	
	/**
	 * <p>Returns the number of unique coins.</p>
	 * @return
	 */
	public int uniqueSize()
	{
		return _coins.size();
	}
	
	/**
	 * <p>Returns an int array containing the number of common coins per coin.</p>
	 */
	public int [] commonSizes()
	{
		int [] out = new int[_coins.size()];
		for (int i = 0; i < out.length; i++)
		{
			out[i] = _coins.get(i).size();
		}
		return out;
	}
	
	/**
	 * <p>Returns an integer array containing a unique count of exchanges per coin.</p>
	 * @return
	 */
	public int [] getAllUniqueExchCount()
	{
		int [] out = new int[_coins.size()];
		List<String> temp = new ArrayList<String>();
		
		//Loop through each unique coin
		for (int ucoin = 0; ucoin < out.length; ucoin++)
		{
			//Loop through each common coin
			for (int ccoin = 0; ccoin < _coins.get(ucoin).size(); ccoin++)
			{
				boolean valid = true;
				String etemp = _coins.get(ucoin).get(ccoin).getExchange();
				//Test each common coin against what we've already 
				//processed to find unique exchanges and count them
				for (int exch = 0; exch < temp.size(); exch++)
				{
					if (temp.get(exch).contentEquals(etemp))
						valid = false;
					
					etemp = _coins.get(ucoin).get(ccoin).getExchange();
				} //end exch loop
				if (valid)
				{
					//Add exchange to preserve its uniqueness and
					//increment counting array index at ucoin
					temp.add(etemp);
					out[ucoin]++;
				}
			} //end ccoin loop
			
			//Clear list for next coin
			temp = new ArrayList<String>();
		} //end ucoin loop
		return out;
	}
	
	/**
	 * <p>Returns the number of occurrences in exchanges a coin is in.</p>
	 * @param coin
	 * @return
	 */
	public int commonSize(int coin)
	{
		return _coins.get(coin).size();
	}
	
	/**
	 * <p>Adds a CoinRoster exchange to be processed.</p>
	 * @param exch
	 */
	public void addExchange(CoinRoster exch)
	{
		_cr.add(exch);
	}
	
	/**
	 * <p>Processes the coins tracked by each CoinRoster object commited via addExchange().</p>
	 */
	public String processExchanges()
	{
		//Check if _cr has any elements in it. If not, print an error message
		if (_cr.size() > 0)
		{
			//Add the first coin from the first exchange
			addCoin();
			addExchCoin(0, _cr.get(0).get(0));
			
			//coin starts at 1 because we added the first coin above
			int coin = 1;
			
			//Unique coins have been filled from first exchange, now we have to cross-check every incoming coin 
			//to make sure it's unique or not
			for (int exch = 0; exch < _cr.size(); exch++)
			{
				//Loop through each coin in the incoming exchange
				while (coin < _cr.get(exch).size())
				{
					boolean addUnique = false;
					
					//Test this coin against every coin currently in _coins
					String inCandidate = _cr.get(exch).get(coin).getPriCode();
					for (int test = 0; test < _coins.size(); test++)
					{
						String tester = _coins.get(test).get(0).getPriCode();
						
						//We're able to test _coins 2nd dim at the constant [0] because every coin in this array will be the same coin
						if (inCandidate.contentEquals(tester))
						{
							//If true then inCandidate is not a unique coin, so add it as an exchcoin
							addExchCoin(test, _cr.get(exch).get(coin));
							
							//We've determined it's not unique so we can end this for-loop
							test = _coins.size();
							
							addUnique = false;
						}
						else
						{
							addUnique = true;
						}
					}
					
					//If the coin we just processed did not equal any of _coins elements it is unique
					//Add it to _coins main list
					if (addUnique)
					{
						addCoin();
						addExchCoin(_coins.size() - 1, _cr.get(exch).get(coin));
					}
					coin++;
				}
				coin = 0; //reset coin so we can process the next exchange properly
			}
			
			//Sort coins (sorts by primary code)
			if (_coins.size() > 0)
				quicksort(0, _coins.size() - 1);
			else
				System.out.println("QUICKSORT ERROR - Nothing to sort! _coins length == 0");
			
			return "UPDATE: Finished!";
		}
		else
			return "--UPDATE: processing error occured";
	}
	  
	private void quicksort(int low, int high)
	{
		int i = low, j = high;
		String pivot = _coins.get(low + (high-low)/2).get(0).getPriCode();

		while (i <= j)
		{
			while (_coins.get(i).get(0).getPriCode().compareToIgnoreCase(pivot) < 0)
			{
				i++;
			}

			while (_coins.get(j).get(0).getPriCode().compareToIgnoreCase(pivot) > 0)
			{
				j--;
			}

			if (i <= j)
			{
				exchange(i, j);
				i++;
				j--;
			}
		}

		if (low < j)
		{
			quicksort(low, j);
		}
			
		if (i < high)
		{
			quicksort(i, high);
		}
	  }

	  private void exchange(int i, int j)
	  {
		  //Save temp List<Coin>
		  Coin[] temp = new Coin[_coins.get(i).size()];
		  for (int run = 0; run < temp.length; run++)
		  {
			  temp[run] = _coins.get(i).get(run);
		  }
		  //----------------------------------------------------------------
	
		  
		  //Move swapping List<Coin> into the one we saved
		  _coins.remove(i);
		  _coins.add(i, new ArrayList<Coin>());
		  
		  for (int run = 0; run < _coins.get(j).size(); run++)
		  {
			  _coins.get(i).add(_coins.get(j).get(run));
		  }
		  //----------------------------------------------------------------
	
		  //Move temp into the swapping List<Coin>
		  _coins.remove(j);
		  _coins.add(j, new ArrayList<Coin>());
		  for (int run = 0; run < temp.length; run++)
		  {
			  _coins.get(j).add(temp[run]);
		  }
	  }
}
