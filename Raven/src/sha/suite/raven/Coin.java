package sha.suite.raven;

public class Coin
{
	private String _exch; //the exchange this coin belongs to
	private String _priCode; //primary code
	private String _secCode; //secondary code
	
	private double _buy; //buying price/fee
	private double _sell; //selling price/fee
	private double _last; //last price this was bought/sold at
	private double _volume; //volume of this coin
	
	
	/**
	 * Create object and set defaults
	 */
	public Coin()
	{
		_priCode = "<n/a>";
		_secCode = "<n/a>";
		_volume = 0;
		_last = 0;
		_exch = "";
	}
	
	/* mutators */
	public void setPriCode(String code) {_priCode = code.toUpperCase();}	
	public void setSecCode(String code){_secCode = code.toUpperCase();}
	public void setVolume(double volume){_volume = volume;}	
	public void setLastTrade(double last){_last = last;}	
	public void setExch(String exch){_exch = exch.toUpperCase();}
	public void setBuy(double buy){_buy = buy;}
	public void setSell(double sell){_sell = sell;}
	
	public void setAvg(double avg){}

	/* accessors */
	public String getPriCode() {return _priCode;}
	public String getSecCode(){return _secCode;}
	public double getVolume(){return _volume;}
	public double getLastTrade(){return _last;}
	public double getAvgTrade(){return 0;}	
	public String getExchange(){return _exch;}
	public double getBuy(){return _buy;}
	public double getSell(){return _sell;}
	
	public String toString()
	{
		String out = "";
		if (_exch.length() > 0)
		{
			out += "Pri-code: " + _priCode + ",";
			out += "Sec-code: " + _secCode + ",";
			out += "Volume: " + _volume + ",";
			out += "Last trade price: " + _last + ",";
			out += "Exchange: " + _exch;
		}
		else
		{
			out = "----<Exchange not set>----";
		}
		return out;
	}
}
