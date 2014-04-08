package sha.suite.raven;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

/*
 * exchanges to implement:
 * 
 * -OkCoin 
 * -btc-e 
 * -bitfinex 
 * 			bter 
 * -fxbtc 
 * 			cryptsy 
 * -kraken 
 * -mcxnow 
 * -poloniex 
 * -justcoin 
 * -vircurex 
 * -the rock trading 
 * -crypto-trade 
 * -coinedup 
 * -bittrex 
 * -atomic trade (no api??)
 * -coins-e 
 * cryptonit
 * 			coinex
 */

public class CoinRoster
{
	final String CRYPTSY_URL = "http://pubapi.cryptsy.com/api.php?method=marketdatav2";
	final String COINEX_URL = "https://coinex.pw/api/v2/trade_pairs";
	final String COINEDUP_URL = "https://api.coinedup.com/markets";
	
	final String BTER_PAIRS_URL = "http://data.bter.com/api/1/pairs";
	final String BTER_TICKERS_URL = "http://data.bter.com/api/1/tickers";
	final String BTC_E_URL = "https://btc-e.com/tapi";
	
	final String OKCOIN_URL = "";
	final String BITFINEX_URL = "https://api.bitfinex.com/v1";
	final String FXBTC_URL = "";
	
	final String KRAKEN_URL_ASSET_PAIRS = "https://api.kraken.com/0/public/AssetPairs";
	final String KRAKEN_URL_TRADES = "https://api.kraken.com/0/public/Trades";
	final String MCXNOW_URL = "";
	final String POLONIEX_URL = "";
	
	final String JUSTCOIN_URL = "";
	final String VIRCUREX_URL = "";
	final String THE_ROCK_TRADING_URL = "";
	
	final String CRYPTO_TRADE_URL = "";
	final String BITTREX_URL = "";
	final String ATOMIC_TRADE_URL = "";
	
	final String COINS_E_URL = "";
	final String CRYPTONIT = "";
	
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
		
		if (_exch.contentEquals("CRYPTSY"))
			processCryptsy();
		else if (_exch.contentEquals("COINEX"))
			processCoinex();
		else if (_exch.contentEquals("COINEDUP"))
			processCoinedup();
		else if (_exch.contentEquals("BTER"))
			processBter();
//		else if (_exch.contentEquals("BTC-E"))
//			processBtc_e();
//		else if (_exch.contentEquals("OKCOIN"))
//			processOKCoin();
//		else if (_exch.contentEquals("BITFINEX"))
//			processBitfinex();
		else if (_exch.contentEquals("KRAKEN"))
			processKraken();		
	}
	
	private void processCryptsy()
	{		
		//Connect to crypsty and download JSON string
		RavenGUI.log("Collecting Cryptsy's market information");
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
		RavenGUI.log("Collecting Coinex's market info");
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
	
	private void processCoinedup()
	{
		RavenGUI.log("Collecting Coinedup's market info");
		String JSON = GetAllMarketInfo(COINEDUP_URL);
		RavenGUI.log(JSON);
		//JSONObject j = new JSONObject(JSON);
		//j = j.getJSONObject("trade_pairs");
	}
	
	private void processBter()
	{
		RavenGUI.log("Collecting Bter's market info");
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
	
	private void processBtc_e()
	{
		RavenGUI.log("Collecting BTC-E's market info");
		
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
	
	private void processBitfinex()
	{
		
		
	}
	
	private void processKraken()
	{
		RavenGUI.log("Collecting Kraken's market info");
		
		String JSON = GetAllMarketInfo(KRAKEN_URL_TRADES);
		try
		{
			if (JSON.length() > 0)
			{
				RavenGUI.log(this._exch + ": " + PROCESSING);
				
				
				JSONObject j = new JSONObject(JSON);
				JSONArray ja = j.toJSONArray(j.names());
				
				for (int i = 0; i < ja.length(); i++)
				{
					Coin tempC = new Coin();
					
					//JSONObject temp = (JSONObject)ja.get(i);
					JSONArray tempa = (JSONArray)ja.get(i);
					tempa = (JSONArray)tempa.get(0);
					
					System.out.println(tempa.toString());
					String code = j.names().get(i).toString();
					
					tempC.setExch(this._exch);
					
					tempC.setPriCode(code.substring(1, code.indexOf("X", 1)));
					tempC.setSecCode(code.substring(code.indexOf("X", 1), code.indexOf("X", tempC.getPriCode().length()+1)));
					tempC.setVolume(Double.parseDouble(tempa.get(1).toString()));
					tempC.setBuy(Double.parseDouble(tempa.get(0).toString()));
					tempC.setSell(tempC.getBuy());
					tempC.setLastTrade(tempC.getBuy());
					
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
	public String GetAllMarketInfo(String url) //------------------------------------------
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
				 Map<String, String> args = new HashMap<String, String>();
				 SecretKeySpec key = null;
				 Mac mac;
				 
				 args.put("method","TradeHistory");
				 args.put("nonce", Long.toString(System.nanoTime()));
				 
				 String postdata = "";
				 
				 for (Iterator argIt = args.entrySet().iterator(); argIt.hasNext();)
				 {
					 Map.Entry argument = (Map.Entry)argIt.next();
					 
					 if (postdata.length() > 0)
					 {
						 postdata += "&";
					 }
					 
					 postdata += argument.getKey() + "=" + argument.getValue();
				 }
				 
				 // Create a new secret key
			        try
			        {
			            key = new SecretKeySpec(postdata.getBytes("UTF-8"), "HmacSHA512");
			        }
			        catch( UnsupportedEncodingException uee)
			        {
			            System.err.println( "Unsupported encoding exception: " + uee.toString());
			            return null;
			        }
			 
			        // Create a new mac
			        try
			        {
			            mac = Mac.getInstance("HmacSHA512");
			        }
			        catch( NoSuchAlgorithmException nsae)
			        {
			            System.err.println( "No such algorithm exception: " + nsae.toString());
			            return null;
			        }
			 
			        // Init mac with key.
			        try
			        {
			            mac.init(key);
			        }
			        catch( InvalidKeyException ike)
			        {
			            System.err.println( "Invalid key exception: " + ike.toString());
			            return null;
			        }
			 
			        // Add the key to the header lines.
			        //headerLines.put( "Key", key);
			 
			        // Encode the post data by the secret and encode the result as base64.
			        /*try
			        {
			            headerLines.put( "Sign", Hex.encodeHexString( mac.doFinal( postData.getBytes( "UTF-8"))));
			        }
			        catch( UnsupportedEncodingException uee)
			        {
			            System.err.println( "Unsupported encoding exception: " + uee.toString());
			            return null;
			        } */
				 
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
								JSON += jt.toString() + ",";
							}
							else
								code = jn.length();
						}
						
						
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
				
				URLConnection conn = null;
				conn = apiResponse.openConnection();
				conn.setDoOutput(true);
									
				//Build a string of the JSON required to make the request and encode it
				//into base64
				byte [] encoded = Base64.encodeBase64(new String("{request:" + "stuff " +
						"nonce:" + System.currentTimeMillis() +
						"options:{}}").getBytes());
				
				String payload = null;
				for (byte b : encoded) { payload += Byte.toString(b); }
				
				conn.setRequestProperty("X-BFX-APIKEY", /*authenticating API key goes here*/"");
				conn.setRequestProperty("X-BFX-PAYLOAD", payload);
				conn.setRequestProperty("X-BFX-SIGNATURE", "");
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
		catch (UnsupportedEncodingException e){e.printStackTrace();}
		catch (IllegalStateException e){e.printStackTrace();}
		catch (NullPointerException e){e.printStackTrace();}
		catch (IOException e)
		{
			if (e.getMessage().contains("403 for URL"))
			{
				RavenGUI.log("UPDATE: 403 error");
			}
			else
			{
				e.printStackTrace();
			}
		}
		catch (Exception e){e.printStackTrace();}
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
