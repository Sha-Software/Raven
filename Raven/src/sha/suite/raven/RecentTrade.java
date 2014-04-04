package sha.suite.raven;

public class RecentTrade
{
	//Arrays for recent trades
	//{"id":"string","time":"string","price":"double","quantity":"double","total":"double"}
			
	private String _id;
	private String _time;
	private double _price;
	private double _quantity;
	private double _total;
	
	public RecentTrade()
	{
		_id = "N/A";
		_time = "N/A";
		_price = -1;
		_quantity = -1;
		_total = -1;
	}
	
	public RecentTrade(String id, String time, double price, double qty, double total)
	{
		_id = id;
		_time = time;
		_price = price;
		_quantity = qty;
		_total = total;
	}
	
	public String toString()
	{
		return "ID: " + _id + ", Time: " + _time + ", Price: " + _price + ", Quantity: " + _quantity + ", Total: " + _total;
	}
}