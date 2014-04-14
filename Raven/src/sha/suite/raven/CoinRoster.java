package sha.suite.raven;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

/*
 * Exchanges to implement:
 * 		https://bitcointalk.org/index.php?topic=557931.msg6079112#msg6079112
 */

public class CoinRoster
{
	final String CRYPTSY_URL = "http://pubapi.cryptsy.com/api.php?method=marketdatav2";
	
	final String COINEX_URL = "https://coinex.pw/api/v2/trade_pairs";
	
	final String COINEDUP_URL = "https://api.coinedup.com/markets";
	
	final String BTER_PAIRS_URL = "http://data.bter.com/api/1/pairs";
	final String BTER_TICKERS_URL = "http://data.bter.com/api/1/tickers";
	
	final String BTC_E_URL = "https://btc-e.com/tapi";
	
	final String BITFINEX_URL = "https://api.bitfinex.com/v1/symbols";
	
	final String KRAKEN_URL_ASSET_PAIRS = "https://api.kraken.com/0/public/AssetPairs";
	final String KRAKEN_URL_TRADES = "https://api.kraken.com/0/public/Trades";
	
	final String BITTREX_URL = "https://bittrex.com/api/v1/public/getmarketsummaries";
	
	final String PROCESSING = "Processing individual coin info";
	
	/**
	 * <p>Holds each coin returned in JSON from the exchanges</p>
	 */
	Coin [] _roster;
	
	/**
	 * <p>Handles how many elements are actually filled</p>
	 */
	private int _numFilled = 0;
	
	/**
	 * <p>Holds the name of which exchange this CoinRoster is querying</p>
	 */
	private String _exch;
	
	/**
	 * <p>Set to false if the exchange we connected to did not return any JSON.</p>
	 */
	private boolean _validProcessing;
	
	/**
	 * <p>Default starting size of CoinRoster objects' _roster array</p>
	 */
	final int DEFAULT_SIZE = 10;
	
	/**
	 * <p>Default constructor</p>
	 */
	CoinRoster(){}
	
	/**
	 * <p>Creates CoinRoster object with a default internal array size and calls processMarketInfo using exchange as a parameter</p>
	 * @param exchange
	 */
 	CoinRoster(String exchange)
	{
		_exch = exchange.toUpperCase();
		processMarketInfo(_exch);
	}
	
	/**
	 * <p>Process current market info for a selected exchange</p>
	 * @param exchange
	 */
	void processMarketInfo(String exchange) //----------------------------------------
	{
		//Initialize roster to its default size
		_roster = new Coin[DEFAULT_SIZE];
		
		//Convert param to uppercase and set the exchange of this CoinRoster
		_exch = exchange.toUpperCase();
		
		try
		{
			if (_exch.contentEquals("CRYPTSY"))
				processCryptsy();
			else if (_exch.contentEquals("COINEX"))
				processCoinex();
			else if (_exch.contentEquals("COINEDUP"))
				processCoinedup();
			else if (_exch.contentEquals("BTER"))
				processBter();
			else if (_exch.contentEquals("BITTREX"))
				processBittrex();
			else if (_exch.contentEquals("KRAKEN"))
				processKraken();
			else if (_exch.contentEquals("BITFINEX"))
				processBitfinex();
		}
		catch (UnsupportedEncodingException uee){uee.printStackTrace();}
		catch (IllegalStateException ise){ise.printStackTrace();}
		catch (NullPointerException npe){npe.printStackTrace();}
		catch (ConnectException ce) {RavenGUI.log("--UPDATE: " + ce.getMessage());}
		catch (Exception e){e.printStackTrace();}
		
	}
	
	private void processCryptsy() throws ConnectException, UnsupportedEncodingException, IllegalStateException, NullPointerException
	{		
		//Connect to crypsty and download JSON string
		RavenGUI.log("CRYPTSY: Collecting market information");
		String JSON = GetAllMarketInfo(CRYPTSY_URL);

		Coin tempC = null;
		JSONObject j = new JSONObject(JSON);
		
		//Traverse down two steps into the JSON tree
		j = j.getJSONObject("return").getJSONObject("markets");
	
		JSONArray ja = j.toJSONArray(j.names());
		
		RavenGUI.log(this._exch + ": " + PROCESSING);
		for (int i = 0; i < ja.length(); i++)
		{
			j = ja.getJSONObject(i);
			tempC = new Coin();
			
			//Set coin info (using literals to decrease memory accesses)
			tempC.setExch("CRYPTSY"); 
			tempC.setPriCode(j.getString("primarycode"));
			tempC.setSecCode(j.getString("secondarycode"));
			tempC.setVolume(j.getDouble("volume"));
			tempC.setLastTrade(j.getDouble("lasttradeprice"));
			tempC.setBuy(j.getDouble("lasttradeprice")); //find out cryptsy's buy/sell
			tempC.setSell(j.getDouble("lasttradeprice")); //find out cryptsy's buy/sell
			
			//Add tempC to _roster so this CoinRoster object may track it properly
			addCoin(tempC);
		}
		_validProcessing = true;
	}
	
	private void processCoinex()
	{
		RavenGUI.log("COINEX: Collecting market information");
		try
		{
			String JSON = GetAllMarketInfo(COINEX_URL);
			
			if (JSON.length() > 0)
			{
				Coin tempC = null;
				JSONObject j = new JSONObject(JSON);
				
				JSONObject.testValidity(JSON);
				
				JSONArray ja = j.toJSONArray(j.names());
				ja = j.getJSONArray("trade_pairs");
				
				RavenGUI.log(this._exch + ": " + PROCESSING);
				for (int i = 0; i < ja.length(); i++)
				{
					tempC = new Coin();
					//RavenGUI.log(ja.get(i));
					j = ja.getJSONObject(i);
					
					//Set coin info (using literals to decrease memory accesses)
					tempC.setExch(("COINEX"));
					
					String temp = j.getString("url_slug");
					tempC.setSecCode(temp.substring(temp.indexOf("_") + 1, temp.length() - 1)); //1st half of url_slug
					tempC.setPriCode(temp.substring(0, temp.indexOf("_"))); //2nd half of url_slug
					tempC.setVolume(j.getDouble("currency_volume"));
					tempC.setLastTrade(j.getDouble("last_price"));
					tempC.setBuy(j.getDouble("buy_fee"));
					tempC.setSell(j.getDouble("sell_fee"));
					
					if (tempC.getSecCode().contentEquals("BT"))
						tempC.setSecCode("BTC");
					else if (tempC.getSecCode().contentEquals("LT"))
						tempC.setSecCode("LTC");
					else if (tempC.getSecCode().contentEquals("DOG"))
						tempC.setSecCode("DOGE");
					
					//Add tempC to _roster so this CoinRoster object may track it properly
					addCoin(tempC);
				}
				_validProcessing = true;
			
			}
			else
			{
				_validProcessing = false;
			}
		}
		catch (Exception e)
		{
			RavenGUI.log("-COINEX: Unable to collect JSON");
		}
	}
	
	private void processCoinedup() throws ConnectException, UnsupportedEncodingException, IllegalStateException, NullPointerException
	{
		RavenGUI.log("COINEDUP: Collecting market information");
		String JSON = GetAllMarketInfo(COINEDUP_URL);
		RavenGUI.log(JSON);
		//JSONObject j = new JSONObject(JSON);
		//j = j.getJSONObject("trade_pairs");
	}
	
	private void processBter() throws ConnectException, UnsupportedEncodingException, IllegalStateException, NullPointerException
	{
		RavenGUI.log("BTER: Collecting market information");
		String JSON_tickers = GetAllMarketInfo(BTER_TICKERS_URL);
		try
		{
			if (JSON_tickers.length() > 0)
			{
				Coin tempC;
				JSONObject j = new JSONObject(JSON_tickers);
				JSONArray jnames = j.names();
				
				JSONArray ja = j.toJSONArray(j.names());
				
				RavenGUI.log(this._exch + ": " + PROCESSING);
				for (int i = 0; i < ja.length(); i++)
				{
					tempC = new Coin();
					String coin_code = jnames.get(i).toString();
					
					j = ja.getJSONObject(i);
					
					tempC.setExch("BTER");
					tempC.setLastTrade(j.getDouble("last"));
					tempC.setPriCode(coin_code.substring(0, coin_code.indexOf("_"))); //get first half of label
					tempC.setSecCode(coin_code.substring(coin_code.indexOf("_") + 1, coin_code.length())); //get second half of label
					tempC.setVolume(j.getDouble("vol_" + tempC.getPriCode().toLowerCase()));
					tempC.setBuy(j.getDouble("buy"));
					tempC.setSell(j.getDouble("sell"));
					
					//Add tempC to _roster so this CoinRoster object may track it properly
					addCoin(tempC);
				}
				_validProcessing = true;
			}
			else
			{
				_validProcessing = false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			RavenGUI.log("--BTER: Unable to collect response");
		}
	}
	
	private void processBittrex() throws ConnectException, UnsupportedEncodingException, IllegalStateException, NullPointerException
	{
		RavenGUI.log("BITTREX: Collecting market information");
		String JSON = GetAllMarketInfo(BITTREX_URL);
		try
		{
			if (JSON.length() > 0)
			{
				RavenGUI.log(this._exch + ": " + PROCESSING);
				
				Coin tempC = null;
				
				JSONObject j = new JSONObject(JSON);
				if (j.getBoolean("success"))
				{
					JSONArray ja = j.getJSONArray("result");
					
					for (int i = 0; i < ja.length(); i++)
					{
						tempC = new Coin();
						JSONObject jt = ja.getJSONObject(i);
						String coincode = jt.getString("MarketName");
						
						tempC.setExch("BITTREX");
						tempC.setLastTrade((jt.get("Last").toString() != "null") ? jt.getDouble("Last") : -1);
						tempC.setPriCode(coincode.substring(0, coincode.indexOf("-"))); //get first half of label
						tempC.setSecCode(coincode.substring(coincode.indexOf("-") + 1)); //get second half of label
						tempC.setVolume((jt.get("Volume").toString() != "null") ? jt.getDouble("Volume") : -1);
						tempC.setBuy(tempC.getLastTrade());
						tempC.setSell(tempC.getLastTrade());
						
						//Add tempC to _roster so this CoinRoster object may track it properly
						addCoin(tempC);
					}
					_validProcessing = true;
				}
			}
			else
			{
				_validProcessing = false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			RavenGUI.log("--BTER: Unable to collect response");
		}
	}
	
	private void processBtc_e() throws ConnectException, UnsupportedEncodingException, IllegalStateException, NullPointerException
	{
		RavenGUI.log("BTC-E: Collecting market information");
		
		String JSON = GetAllMarketInfo(BTC_E_URL);
		try
		{
			if (JSON.length() > 0)
			{
				RavenGUI.log(this._exch + ": " + PROCESSING);
				Coin tempC = new Coin();
				JSONObject j = new JSONObject(JSON);
				
				//Traverse down JSON tree
				j = j.getJSONObject("return");
				
				JSONArray ja = j.toJSONArray(j.names());
				
			}
			else
			{
				_validProcessing = false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			RavenGUI.log("--BTC-E: Unable to collect response");
		}
	}
	
	private void processOKCoin()
	{
		
	}
	
	private void processBitfinex() throws ConnectException, UnsupportedEncodingException, IllegalStateException, NullPointerException
	{
		RavenGUI.log("BITFINEX: Collecting market information");
		String JSON = GetAllMarketInfo(BITFINEX_URL);
		try
		{
			if (JSON.length() > 0)
			{
				RavenGUI.log(this._exch + ": " + PROCESSING);
				
				Coin tempC = null;
				
				JSONArray j = new JSONArray(JSON);
				
				for (int i = 0; i < j.length(); i++)
				{
					tempC = new Coin();
					JSONObject jt = j.getJSONObject(i);
					String code = jt.names().get(0).toString();
					jt = jt.getJSONObject(code);
					
					tempC.setExch("BITFINEX");
					tempC.setLastTrade(jt.getDouble("last_price"));
					tempC.setPriCode(code.substring(0, 3));
					tempC.setSecCode(code.substring(3));
					tempC.setVolume(jt.getDouble("volume"));
					tempC.setBuy(tempC.getLastTrade());
					tempC.setSell(tempC.getLastTrade());
					
					addCoin(tempC);
				}
				
				
				_validProcessing = true;
			}
			else
			{
				_validProcessing = false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			RavenGUI.log("--BITTREX: Unable to collect response");
		}
		
	}
	
	private void processKraken() throws ConnectException, UnsupportedEncodingException, IllegalStateException, NullPointerException
	{
		RavenGUI.log("KRAKEN: Collecting market information");
		
		String JSON = GetAllMarketInfo(KRAKEN_URL_TRADES);
		try
		{
			if (JSON.length() > 0)
			{
				RavenGUI.log(this._exch + ": " + PROCESSING);
				
				//JSONObject j = new JSONObject(JSON);
				JSONArray ja = new JSONArray(JSON);
				//JSONArray ja = j.toJSONArray(j.names());
				
				for (int i = 0; i < ja.length(); i++)
				{
					Coin tempC = new Coin();
					
					//JSONObject temp = (JSONObject)ja.get(i);
					
					JSONObject temp = (JSONObject)ja.get(i);
					JSONArray tempa = (JSONArray)temp.get(temp.names().get(0).toString());
				
					String code = temp.names().get(0).toString();
					
					tempC.setExch(this._exch);
					
					//tempC.setPriCode(code.substring(1, code.indexOf("X", 1)));
					//tempC.setSecCode(code.substring(code.indexOf("X", 1), code.indexOf("X", tempC.getPriCode().length()+1)));
					tempC.setPriCode(code.substring(1, 4));
					tempC.setSecCode(code.substring(5));
					tempC.setVolume(Double.parseDouble(tempa.get(1).toString()));
					tempC.setBuy(Double.parseDouble(tempa.get(0).toString()));
					tempC.setSell(tempC.getBuy());
					tempC.setLastTrade(tempC.getBuy());
					
					addCoin(tempC);
				}
				_validProcessing = true;
			}
			else
			{
				_validProcessing = false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			RavenGUI.log("--Kraken: Unable to collect response");
		}
	}
	
	private void addCoin(Coin c) //------------------------------------------------------------
	{
		//Check if array is full (increase size by 10 if true)
		if (_numFilled >= _roster.length - 1)
		{
			increaseRosterSize();
			_roster[_numFilled++] = c;
		}
		else
		{
			_roster[_numFilled++] = c;
		}
	}
	
	/**
	 * <p>Returns null if index requested exceeds the internal array's highest index</p>
	 * @param i
	 * @return
	 */
	Coin get(int i)
	{
		//Do not access array if i exceeds its highest index
		return (i < _roster.length - 1) ? _roster[i] : null;
	}
	
	/**
	 * <p>Returns number of actual Coin objects in the internal array</p>
	 * @return
	 */
	int size()
	{
		return _numFilled;
	}
	
	private void increaseRosterSize() //------------------------------------------------------
	{
		Coin [] temp = new Coin[_roster.length];
		
		//Copy roster's items into temp
		for (int i = 0; i < _roster.length; i++)
		{
			temp[i] = _roster[i];
		}
		
		//Increase roster's size
		_roster = new Coin[_roster.length + DEFAULT_SIZE];
		
		//Copy elements back over to roster
		for (int i = 0; i < temp.length; i++)
		{
			_roster[i] = temp[i];
		}
	}
	
	/**
	 * Returns a String containing JSON from exchanges' API
	 * @return
	 */
	
	
	public String GetAllMarketInfo(String url) throws UnsupportedEncodingException, IllegalStateException, NullPointerException, ConnectException//------------------------------------------
	{
		URL apiResponse = null;
		BufferedReader in = null;
		
		String JSON = "";
		String temp = "";
		String charset = null;
		String contentType = null;
		
		//Construct URL
		try
		{
			apiResponse = new URL(url);
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
				
		
		try
		{
			charset = null;
			if (url.contains("cryptsy")) //----------------------------------------------------------------------
			{
				in = new BufferedReader(new InputStreamReader(apiResponse.openStream()));
				
				while ((temp = in.readLine()) != null)
				{
					JSON += temp;
				}
			}
			else if (url.contains("btce"))
			{
			}
			else if (url.contains("api.kraken.com/0/public/Trades"))
			{
				String tempJ = null;
				//Get asset pairs from kraken
				if ((tempJ = GetAllMarketInfo(KRAKEN_URL_ASSET_PAIRS)).length() > 0)
				{
					URLConnection conn = null;
					OutputStreamWriter out = null;
					
					//Collect coin codes
					JSONObject j = new JSONObject(tempJ).getJSONObject("result");
					JSONArray jn = j.names();
					JSONArray ja = j.toJSONArray(jn);
					
					JSON = "[";
					
					for (int code = 0; code < jn.length(); code++)
					{
						conn = apiResponse.openConnection();
						conn.setDoOutput(true);
						
						conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
						conn.setRequestProperty("Accept-Charset", "utf-8");
						
						//Set which coin we want to query
						String coincode = jn.getString(code);
						
						//Open the webpage connection
						out = new OutputStreamWriter(conn.getOutputStream());
						out.write("pair=" + coincode);
						out.close();
						
						//Collect the coin info and append it to the JSON String
						BufferedReader pairIn = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						
						String pairtemp = null;
						while ((pairtemp = pairIn.readLine()) != null)
						{
							JSONObject jt = new JSONObject(pairtemp);
							
							if (jt.getJSONArray("error").length() < 1)
							{
								jt = jt.getJSONObject("result");
								if (code > 1)
								{
									JSONArray jat = jt.getJSONArray(coincode);
									JSON += "{\"" + coincode + "\":" + jat.get(0).toString() + "},";
								}
								else
									JSON += "{\"" + coincode + "\":" + jt.getJSONArray(coincode).get(0).toString() + "},";
							}
							else
								code = jn.length();
						}
					}
					if (JSON.length() >= 2)
					{
						JSON = JSON.substring(0, JSON.length() - 2); //remove ending comma
						JSON += "}]"; //close JSON syntax
					}
				}
			}
			else if (url.contains("api.kraken.com/0/public/AssetPairs"))
			{
				URLConnection conn = apiResponse.openConnection();
				conn.setDoOutput(true);
				
				conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
				conn.setRequestProperty("Accept-Charset", "utf-8");
				
				BufferedReader assetIn = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				
				if (assetIn != null)
				{
					String inTemp = null;
					while ((inTemp = assetIn.readLine()) != null)
						JSON += inTemp;
					assetIn.close();
				}
			}
			else if (url.contains("bitfinex"))
			{
				//   https://api.bitfinex.com/v1
				//   /book/:symbol - get full orderbook
				
				URLConnection conn = apiResponse.openConnection();
				conn.setDoOutput(true);
				conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
				conn.setRequestProperty("Accept-Charset", "utf-8");
				
				BufferedReader pairsIn = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				
				String tpair = "";
				String pairs = "";
				while ((tpair = pairsIn.readLine()) != null)
				{
					pairs += tpair;
				}
				
				JSONArray j = new JSONArray(pairs);
				
				//Prepare JSON string as a JSON array
				JSON = "[";
				for (int i = 0; i < j.length(); i++)
				{
					conn = new URL("https://api.bitfinex.com/v1/pubticker/" + j.getString(i)).openConnection();
					conn.setDoOutput(true);
					conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
					conn.setRequestProperty("Accept-Charset", "utf-8");
										
					pairsIn = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					tpair = null;
					while ((tpair = pairsIn.readLine()) != null)
					{
						JSON += "{\"" + j.get(i) + "\":" + tpair + "},";
					}
					
				}
				
				if (JSON.length() >= 2)
				{
					JSON = JSON.substring(0, JSON.length() - 2); //remove ending comma
					JSON += "}]"; //close JSON syntax
				}
			}
			else if (url.contains("bittrex.com/api/v1/public/getmarketsummaries"))
			{
				URLConnection conn = apiResponse.openConnection();
				
				conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
				conn.setRequestProperty("Accept-Charset", "utf-8");
				
				BufferedReader pairsIn = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				
				String tIn = null;
				while ((tIn = pairsIn.readLine()) != null)
				{
					JSON += tIn;
				}
			}
			else //-----------------------------------------------------------------------------------------------
			{
				URLConnection conn = null;
				
				//Open connection to web page
				conn = apiResponse.openConnection();
				
				conn.setDoOutput(true); //Triggers post
				
				//Set HTTP request headers to fool coinex/bter into thinking we're Firefox (they reject our request otherwise)

				conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
				conn.setRequestProperty("Accept-Charset", "utf-8");
				//conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=");
				
				
				//Collect the type of HTML content so we can decode it
				contentType = conn.getHeaderField("Content-Type");
				//
				for (String param : contentType.replace(" ", "").split(";"))
				{
				    if (param.startsWith("charset=")) 
				    {
				        charset = param.split("=", 2)[1];
				        break;
				    }
				}
				
				//if (charset != null)
				{
					in = new BufferedReader(new InputStreamReader(conn.getInputStream()/*, charset*/));
					for (String line; (line = in.readLine()) != null;)
					{
						JSON += line;
					}
				}
				
			} //end of else
		}
		
		catch (IOException ioe)
		{
			if (ioe.getMessage().contains("403 for URL"))
			{
				RavenGUI.log("UPDATE: 403 error");
			}
			else
			{
				ioe.printStackTrace();
			}
		}
		
		finally 
		{
			try
			{
				if (charset != null) {in.close();}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		return JSON;
	}
	
	public String getExch()
	{
		return _exch;
	}
	
	public boolean verified()
	{
		return _validProcessing;
	}
}
