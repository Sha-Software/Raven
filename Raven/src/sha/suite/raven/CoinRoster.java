package sha.suite.raven;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;

import org.json.JSONArray;
import org.json.JSONException;
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
	final String BTER_TICKERS_URL = "https://data.bter.com/api/1/tickers";
	final String BTC_E_URL = "https://btc-e.com/tapi";
	
	final String BITFINEX_URL = "https://api.bitfinex.com/v1/symbols";
	final String BITSTAMP_TICKER = "https://www.bitstamp.net/api/ticker/";
	final String KRAKEN_URL_ASSET_PAIRS = "https://api.kraken.com/0/public/AssetPairs";
	final String KRAKEN_URL_TRADES = "https://api.kraken.com/0/public/Trades";
	
	final String BITTREX_URL = "https://bittrex.com/api/v1/public/getmarketsummaries";
	final String POLONIEX_TICKER = "https://poloniex.com/public?command=returnTicker";
	final String MINTPAL_URL = "https://api.mintpal.com/v1/market/summary/";
	final String PRELUDE_MARKET_BTC = "https://api.prelude.io/statistics/";
	final String PRELUDE_MARKET_USD = "https://api.prelude.io/statistics-usd/";
	
	final String FXBTC_1 = "https://data.fxbtc.com/api?op=query_ticker&symbol=btc_cny";
	final String FXBTC_2 = "https://data.fxbtc.com/api?op=query_ticker&symbol=ltc_cny";
	final String FXBTC_3 = "https://data.fxbtc.com/api?op=query_ticker&symbol=ltc_btc";
	
	final String ROCK_TRADING_URL = "https://www.therocktrading.com/api/tickers";
	final String ATOMIC_TRADE_URL = "https://www.atomic-trade.com/SimpleAPI?a=marketsv2";
	final String BITKONAN_URL = "https://bitkonan.com/api/";
	final String VIRCUREX_URL = "https://api.vircurex.com/api/get_info_for_currency.json";
	
	final String COLLECTING_STRING = ": Collecting market information";
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
		
		_validProcessing = false;
		
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
			else if (_exch.contentEquals("BITSTAMP"))
				processBitstamp();
			else if (_exch.contentEquals("POLONIEX"))
				processPoloniex();
			else if (_exch.contentEquals("MINTPAL"))
				processMintpal();
			else if (_exch.contentEquals("PRELUDE"))
				processPrelude();
			else if (_exch.contentEquals("FXBTC"))
				processFxbtc();
			else if (_exch.contentEquals("THE ROCK TRADING"))
				processRockTrading();
			else if (_exch.contentEquals("ATOMIC TRADE"))
				processAtomicTrade();
			else if (_exch.contentEquals("BITKONAN"))
				processBitkonan();
			else if (_exch.contentEquals("BTC-E"))
				processBtc_e();
			else if (_exch.contentEquals("OKCOIN"))
				processOKCoin();
			else if (_exch.contentEquals("VIRCUREX"))
				processVircurex();
			else
				RavenGUI.log("Exchange \"" + _exch + "\" is not supported by Raven.");
		}
		catch (UnsupportedEncodingException uee){uee.printStackTrace();}
		catch (IllegalStateException ise){ise.printStackTrace();}
		catch (NullPointerException npe){npe.printStackTrace();}
		catch (ConnectException ce) {RavenGUI.log("--UPDATE: " + ce.getMessage());}
		catch (Exception e){e.printStackTrace();}
		
	}
	
	/*
	 * process<Exchange>() methods and GetAllMarketInfo(<exchange>) are distinct functions in order to
	 * separate HTML/JSON collection and JSON processing.
	 */
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
	
	private void processCoinex() throws ConnectException, UnsupportedEncodingException, IllegalStateException, NullPointerException
	{
		RavenGUI.log("COINEX: Collecting market information");
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
	}
	
	//disabled
	private void processCoinedup() throws ConnectException, UnsupportedEncodingException, IllegalStateException, NullPointerException
	{
		RavenGUI.log("--COINEDUP: Functionality currently disabled");
//		RavenGUI.log("COINEDUP: Collecting market information");
//		String JSON = GetAllMarketInfo(COINEDUP_URL);
//		if (JSON.length() > 0)
//		{
//			
//		}
	}
	
	private void processBter() throws ConnectException, UnsupportedEncodingException, IllegalStateException, NullPointerException
	{
		RavenGUI.log(_exch + COLLECTING_STRING);
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
		RavenGUI.log(_exch + COLLECTING_STRING);
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
						tempC.setSecCode(coincode.substring(0, coincode.indexOf("-"))); //get first half of label
						tempC.setPriCode(coincode.substring(coincode.indexOf("-") + 1)); //get second half of label
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
//		RavenGUI.log(_exch + COLLECTING_STRING);
//		
//		String JSON = GetAllMarketInfo(BTC_E_URL);
//		try
//		{
//			if (JSON.length() > 0)
//			{
//				RavenGUI.log(this._exch + ": " + PROCESSING);
//				Coin tempC = new Coin();
//				JSONObject j = new JSONObject(JSON);
//				
//				//Traverse down JSON tree
//				j = j.getJSONObject("return");
//				
//				JSONArray ja = j.toJSONArray(j.names());
//				
//			}
//			else
//			{
//				_validProcessing = false;
//			}
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//			RavenGUI.log("--BTC-E: Unable to collect response");
//		}
	}
	
	private void processOKCoin()
	{
		RavenGUI.log("--" + _exch + "Functionality currently disabled");
//		RavenGUI.log(_exch + COLLECTING_STRING);
	}
	
	private void processBitfinex() throws ConnectException, UnsupportedEncodingException, IllegalStateException, NullPointerException
	{
		RavenGUI.log(_exch + COLLECTING_STRING);
		String JSON = GetAllMarketInfo(BITFINEX_URL);
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
		
	}
	
	private void processKraken() throws ConnectException, UnsupportedEncodingException, IllegalStateException, NullPointerException
	{
		RavenGUI.log(_exch + COLLECTING_STRING);
		
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
	
	private void processBitstamp() throws ConnectException, UnsupportedEncodingException, IllegalStateException, NullPointerException
	{
		//Connect to crypsty and download JSON string
		RavenGUI.log(_exch + COLLECTING_STRING);
		String JSON = GetAllMarketInfo(BITSTAMP_TICKER);
		
		if (JSON.length() > 0)
		{
			Coin tempC = null;
			JSONObject j = new JSONObject(JSON);
						
			RavenGUI.log(this._exch + ": " + PROCESSING);
			tempC = new Coin();
			
			//Set coin info (using literals to decrease memory accesses)
			tempC.setExch("BITSTAMP"); 
			tempC.setPriCode("BTC");
			tempC.setSecCode("USD");
			tempC.setVolume(j.getDouble("volume"));
			tempC.setLastTrade(j.getDouble("last"));
			tempC.setBuy(tempC.getLastTrade()); //find out cryptsy's buy/sell
			tempC.setSell(tempC.getLastTrade()); //find out cryptsy's buy/sell
			
			//Add tempC to _roster so this CoinRoster object may track it properly
			addCoin(tempC);
			_validProcessing = true;
		}
		
	}
	
	//disabled
	private void processPoloniex() throws ConnectException, UnsupportedEncodingException, IllegalStateException, NullPointerException
	{
		RavenGUI.log("--POLONIEX: Functionality currently disabled");
//		RavenGUI.log(_exch + COLLECTING_STRING);
//		String JSON = GetAllMarketInfo(POLONIEX_TICKER);
//		
//		if (JSON.length() > 0)
//		{
//			Coin tempC = null;
//			JSONObject j = new JSONObject(JSON);
//			JSONArray jn = j.names();
//			JSONArray ja = j.toJSONArray(jn);
//			
//			for (int i = 0; i < ja.length(); i++)
//			{
//				tempC = new Coin();
//				j = ja.getJSONObject(i);
//				String code = jn.getString(i);
//				
//				tempC.setPriCode(code.substring(0, code.indexOf("_")));
//				tempC.setSecCode(code.substring(code.indexOf(("_") + 1)));
//				tempC.setVolume(j.getDouble("quoteVolume")); //baseVolume as an alt key?
//				tempC.setLastTrade(j.getDouble("last"));
//				tempC.setBuy(tempC.getLastTrade());
//				tempC.setSell(tempC.getSell());
//				
//				addCoin(tempC);
//			}
//			_validProcessing = true;
//		}
	}
	
	private void processMintpal() throws ConnectException, UnsupportedEncodingException, IllegalStateException, NullPointerException
	{
		RavenGUI.log(_exch + COLLECTING_STRING);
		String JSON = GetAllMarketInfo(MINTPAL_URL);
		
		if (JSON.length() > 0)
		{
			JSONArray ja = new JSONArray(JSON);
			Coin tc = null;
			RavenGUI.log(this._exch + ": " + PROCESSING);
			
			for (int i = 0; i < ja.length(); i++)
			{
				tc = new Coin();
				JSONObject j = ja.getJSONObject(i);
				tc.setExch(_exch);
				tc.setPriCode(j.getString("code"));
				tc.setSecCode(j.getString("exchange"));
				tc.setVolume(j.getDouble("24hvol"));
				tc.setLastTrade(j.getDouble("last_price"));
				tc.setBuy(tc.getLastTrade());
				tc.setSell(tc.getLastTrade());
				
				addCoin(tc);
			}
			_validProcessing = true;
		}
	}
	
	private void processPrelude() throws ConnectException, UnsupportedEncodingException, IllegalStateException, NullPointerException
	{
		RavenGUI.log(_exch + COLLECTING_STRING + " (1/2)");
		String BTCJSON = GetAllMarketInfo(PRELUDE_MARKET_BTC);
		
		if (BTCJSON.length() > 0)
		{
			JSONArray ja = new JSONArray(BTCJSON);
			Coin tc = null;
			RavenGUI.log(this._exch + ": " + PROCESSING);
			
			for (int i = 0; i < ja.length(); i++)
			{
				tc = new Coin();
				JSONObject j = ja.getJSONObject(i);
				String code = j.names().getString(0).toString();
				j = j.getJSONObject(code);
				
				tc.setExch(_exch);
				tc.setPriCode(code);
				tc.setSecCode("BTC");
				
				String volume = j.getString("volume");
				//Remove each comma if they exist
				while (volume.indexOf(",") != -1)
					volume = volume.substring(0, volume.indexOf(",")) + volume.substring(volume.indexOf(",") + 1);
				
				tc.setVolume(Double.parseDouble(volume));
				tc.setLastTrade((j.getDouble("high") + j.getDouble("low")) / 2);
				tc.setBuy(tc.getLastTrade());
				tc.setSell(tc.getLastTrade());
				
				addCoin(tc);
			}
			_validProcessing = true;
		}
		
		RavenGUI.log(_exch + COLLECTING_STRING + " (2/2)");
		String USDJSON = GetAllMarketInfo(PRELUDE_MARKET_USD);
		if (USDJSON.length() > 0)
		{			
			JSONArray ja = new JSONArray(USDJSON);
			Coin tc = null;
			RavenGUI.log(this._exch + ": " + PROCESSING);
			
			for (int i = 0; i < ja.length(); i++)
			{
				tc = new Coin();
				JSONObject j = ja.getJSONObject(i);
				
				tc.setExch(_exch);
				//tc.setPriCode(j.getString(j.names().get(0).toString()));]
				tc.setPriCode(j.names().get(0).toString());
				tc.setSecCode("USD");

				String volume = j.getString("volume");
				//Remove each comma if they exist
				while (volume.indexOf(",") != -1)
					volume = volume.substring(0, volume.indexOf(",")) + volume.substring(volume.indexOf(",") + 1);
				
				tc.setVolume(Double.parseDouble(volume));
				tc.setLastTrade((j.getDouble("high") + j.getDouble("low")) / 2);
				tc.setBuy(tc.getLastTrade());
				tc.setSell(tc.getLastTrade());
				
				addCoin(tc);
			}
			_validProcessing = true;
		}
	}
	
	private void processFxbtc() throws ConnectException, UnsupportedEncodingException, IllegalStateException, NullPointerException
	{
		RavenGUI.log(_exch + COLLECTING_STRING);
		String [] JSON = {GetAllMarketInfo(FXBTC_1), GetAllMarketInfo(FXBTC_2), GetAllMarketInfo(FXBTC_3)};
		
		JSONObject j = null;
		Coin t = null;
		if (JSON[0].length() > 0)
		{
			RavenGUI.log(_exch + ": " + PROCESSING);
			j = new JSONObject(JSON[0]);
			if (j.getBoolean("result"))
			{
				j = j.getJSONObject("ticker");
				t = new Coin();
				
				t.setExch(_exch);
				t.setPriCode("BTC");
				t.setSecCode("CNY");
				t.setVolume(j.getDouble("vol"));
				t.setLastTrade(j.getDouble("last_rate"));
				t.setBuy(j.getDouble("ask"));
				t.setSell(t.getBuy());
				
				addCoin(t);
				
				_validProcessing = true;
			}
		}
		
		if (JSON[1].length() > 0)
		{
			j = new JSONObject(JSON[1]);
			if (j.getBoolean("result"))
			{
				j = j.getJSONObject("ticker");
				t = new Coin();
				
				t.setExch(_exch);
				t.setPriCode("LTC");
				t.setSecCode("CNY");
				t.setVolume(j.getDouble("vol"));
				t.setLastTrade(j.getDouble("last_rate"));
				t.setBuy(j.getDouble("ask"));
				t.setSell(t.getBuy());
				
				addCoin(t);
				
				_validProcessing = true;
			}
		}
		
		if (JSON[2].length() > 0)
		{
			j = new JSONObject(JSON[2]);
			if (j.getBoolean("result"))
			{
				j = j.getJSONObject("ticker");
				t = new Coin();
				
				t.setExch(_exch);
				t.setPriCode("LTC");
				t.setSecCode("BTC");
				t.setVolume(j.getDouble("vol"));
				t.setLastTrade(j.getDouble("last_rate"));
				t.setBuy(j.getDouble("ask"));
				t.setSell(t.getBuy());
				
				addCoin(t);
				
				_validProcessing = true;
			}
		}
	}
	
	private void processRockTrading() throws ConnectException, UnsupportedEncodingException, IllegalStateException, NullPointerException
	{
		RavenGUI.log(_exch + COLLECTING_STRING);
		String JSON = GetAllMarketInfo(ROCK_TRADING_URL);
		if (JSON.length() > 0)
		{
			RavenGUI.log(_exch + ": " + PROCESSING);
			//Remove "&quot;" if it exists
			if ((JSON = strrep(JSON, "&quot;", "\"")) != null)
			{
				//JSON sanitized, process using JSONObjects
				JSONObject j = new JSONObject(JSON);
				j = j.getJSONObject("result");
				if (j.getString("errorCode").toUpperCase().contentEquals("OK"))
				{
					j = j.getJSONObject("tickers");
					JSONArray jn = j.names();
					JSONArray ja = j.toJSONArray(jn);
					Coin t = null;
					for (int i = 0; i < ja.length(); i++)
					{
						String code = jn.getString(i);
						
						if (code.length() == 6)
						{
							t = new Coin();
							j = ja.getJSONObject(i);
							
							t.setExch(_exch);
							t.setPriCode(code.substring(0, 3));
							t.setSecCode(code.substring(3));
							t.setLastTrade(j.getDouble("last"));
							t.setVolume(j.getDouble("volume"));
							t.setBuy(j.getDouble("ask"));
							t.setSell(t.getBuy());
							
							addCoin(t);
						}
					}
				}
				_validProcessing = true;
			}
		}
	}
	
	//disabled
	private void processAtomicTrade() throws ConnectException, UnsupportedEncodingException, IllegalStateException, NullPointerException
	{
		RavenGUI.log("--ATOMIC TRADE: Functionality currently disabled");
//		RavenGUI.log(_exch + COLLECTING_STRING);
//		String JSON = GetAllMarketInfo(ATOMIC_TRADE_URL);
//		
//		if (JSON.length() > 0)
//		{
//			RavenGUI.log(_exch + ": " + PROCESSING);
//			JSONArray ja = new JSONArray(JSON);
//			Coin t = null;
//			for (int i = 0; i < ja.length(); i++)
//			{
//				JSONObject j = ja.getJSONObject(i);
//				String code = j.getString("market");
//				t = new Coin();
//				
//				t.setExch(_exch);
//				t.setPriCode(code.substring(0, code.indexOf("/")));
//				t.setSecCode(code.substring(code.indexOf("/") + 1));
//				t.setLastTrade(j.getDouble("price"));
//				t.setVolume(j.getDouble("volume"));
//				t.setBuy(j.getDouble("price"));
//				t.setSell(t.getBuy());
//				
//				addCoin(t);
//			}
//			
//			_validProcessing = true;
//		}
	}
	
	private void processBitkonan() throws ConnectException, UnsupportedEncodingException, IllegalStateException, NullPointerException
	{
		RavenGUI.log(_exch + COLLECTING_STRING);
		String JSON = GetAllMarketInfo(BITKONAN_URL);
		
		if (JSON.length() > 0)
		{
			RavenGUI.log(_exch + ": " + PROCESSING);
			JSONArray ja = new JSONArray(JSON);
			Coin t = new Coin();
			
			if (ja.length() > 0)
			{
				t.setExch(_exch);
				t.setPriCode("BTC");
				t.setSecCode("USD");
				t.setLastTrade(ja.getJSONObject(0).getDouble("last"));
				t.setVolume(ja.getJSONObject(0).getDouble("volume"));
				t.setBuy(ja.getJSONObject(0).getDouble("ask"));
				t.setSell(t.getBuy());
				
				addCoin(t);
				_validProcessing = true;
				
				if (ja.length() > 1)
				{
					t = new Coin();
					t.setExch(_exch);
					t.setPriCode("LTC");
					t.setSecCode("USD");
					t.setLastTrade(ja.getJSONObject(1).getDouble("last"));
					t.setVolume(ja.getJSONObject(1).getDouble("volume"));
					t.setBuy(ja.getJSONObject(1).getDouble("ask"));
					t.setSell(t.getBuy());
					
					addCoin(t);
				}
			}
		}
	}
	
	private void processVircurex() throws ConnectException, UnsupportedEncodingException, IllegalStateException, NullPointerException
	{
		RavenGUI.log(_exch + COLLECTING_STRING);
		String JSON = GetAllMarketInfo(VIRCUREX_URL);
		
		if (JSON.length() > 0)
		{
			RavenGUI.log(_exch + ": " + PROCESSING);
			JSONObject j = new JSONObject(JSON);
			JSONArray jn = j.names();
			JSONArray ja = j.toJSONArray(jn);
			
			Coin t = null;
			String code = null;
			
			//Loop through each coin
			for (int coin = 0; coin < ja.length() - 1; coin++) //minus 1 to ignore the integer at the end
			{
				j = ja.getJSONObject(coin);
				JSONArray tjn = j.names(); //tjn == tempJsonNames
				code = jn.getString(coin);
				
				JSONArray markets = j.toJSONArray(tjn);
				
				//Loop through each coin's markets
				for (int mkt = 0; mkt < markets.length(); mkt++)
				{
					j = markets.getJSONObject(mkt);
					t = new Coin();
					
					t.setExch(_exch);
					t.setPriCode(code);
					t.setSecCode(tjn.getString(mkt));
					t.setLastTrade(j.getDouble("last_trade"));
					t.setVolume(j.getDouble("volume"));
					t.setBuy(j.getDouble("lowest_ask"));
					t.setSell(t.getBuy());
					
					addCoin(t);
				}
			}
			_validProcessing = true;
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
		/*
		 * CTRL+F "<Exchange> CONNECT" to find a specific exchange's code for collecting JSON
		 * eg: search "CRYPTSY CONNECT" to find CRYPTSY's collection code
		 */
		URL exchurl = null;
		BufferedReader in = null;
		
		String JSON = "";
		String temp = "";
		String charset = null;
		
		//Construct URL
		try
		{
			exchurl = new URL(url);
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
				
		
		try
		{
			charset = null;
			if (url.contains(CRYPTSY_URL) || url.contains(BITSTAMP_TICKER) || url.contains(MINTPAL_URL)) //CRYPTSY CONNECT BITSTAMP CONNECT MINTPAL CONNECT
			{
				in = new BufferedReader(new InputStreamReader(exchurl.openStream()));
				
				while ((temp = in.readLine()) != null)
					JSON += temp;
			}
			else if (url.contains(COINEX_URL) || url.contains(BTER_TICKERS_URL) || url.contentEquals(FXBTC_1) || url.contentEquals(FXBTC_2) || url.contentEquals(FXBTC_3) || url.contentEquals(ROCK_TRADING_URL) || url.contentEquals(ATOMIC_TRADE_URL) || url.contentEquals(VIRCUREX_URL)) 
			//COINEX CONNECT, BTER CONNECT, FXBTC CONNECT, ROCK TRADING CONNECT, ATOMIC TRADE CONNECT, VIRCUREX CONNECT
			{
				//Open connection
				URLConnection conn = exchurl.openConnection();

				conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
				conn.setRequestProperty("Accept-Charset", "utf-8");
				
				//Connect to page
				in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				while ((temp = in.readLine()) != null)
					JSON += temp;
			}
			else if (url.contains("btce")) //BTC-E CONNECT
			{
				
			}
			else if (url.contains("api.kraken.com/0/public/Trades")) //KRAKEN CONNECT
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
						conn = exchurl.openConnection();
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
						in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						
						while ((temp = in.readLine()) != null)
						{
							JSONObject jt = new JSONObject(temp);
							
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
			else if (url.contains("api.kraken.com/0/public/AssetPairs")) //KRAKEN CONNECT
			{
				URLConnection conn = exchurl.openConnection();
				conn.setDoOutput(true);
				
				conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
				conn.setRequestProperty("Accept-Charset", "utf-8");
				
				in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				
				if (in != null)
				{
					while ((temp = in.readLine()) != null)
						JSON += temp;
					in.close();
				}
			}
			else if (url.contains("bitfinex")) //BITFINEX CONNECT
			{
				//   https://api.bitfinex.com/v1
				//   /book/:symbol - get full orderbook
				
				URLConnection conn = exchurl.openConnection();
				conn.setDoOutput(true);
				conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
				conn.setRequestProperty("Accept-Charset", "utf-8");
				
				in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				
				String pairs = "";
				while ((temp = in.readLine()) != null)
				{
					pairs += temp;
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
										
					in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					
					while ((temp = in.readLine()) != null)
					{
						JSON += "{\"" + j.get(i) + "\":" + temp + "},";
					}
					
				}
				
				if (JSON.length() >= 2)
				{
					JSON = JSON.substring(0, JSON.length() - 2); //remove ending comma
					JSON += "}]"; //close JSON syntax
				}
			}
			else if (url.contains(BITTREX_URL)) //BITTREX CONNECT
			{
				URLConnection conn = exchurl.openConnection();
				
				conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
				conn.setRequestProperty("Accept-Charset", "utf-8");
				
				in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				
				while ((temp = in.readLine()) != null)
				{
					JSON += temp;
				}
			}
			else if (url.contains(POLONIEX_TICKER)) //POLONIEX CONNECT
			{
				HttpURLConnection conn = (HttpsURLConnection)exchurl.openConnection();
				conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
				conn.setRequestProperty("Accept-Charset", "utf-8");
				conn.setRequestProperty("Referer", "https://poloniex.com/api");
				
				// Create a trust manager that does not validate certificate chains
				TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager()
				{
				    public X509Certificate[] getAcceptedIssuers(){return null;}
				    public void checkClientTrusted(X509Certificate[] certs, String authType){}
				    public void checkServerTrusted(X509Certificate[] certs, String authType){}
				}};

				// Install the all-trusting trust manager
				try {
				    SSLContext sc = SSLContext.getInstance("TLS");
				    sc.init(null, trustAllCerts, new SecureRandom());
				    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
				} catch (Exception e) {}
				
				in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				
				while((temp = in.readLine()) != null)
					JSON += temp;
			}
			
			else if (url.contains(PRELUDE_MARKET_BTC)) //PRELUDE CONNECT
			{
				String [] tickers = {"888", "AUR", "BC", "DGB", "DGC", "DOGE", "DRK", "EMC2", "LTC", "MAX", "MEOW", "MINT", "PPC", "VTC"};
				
				
				//prep JSON as a JSONArray
				JSON = "[";
				for (int ticker = 0; ticker < tickers.length; ticker++)
				{
					URLConnection conn = new URL(PRELUDE_MARKET_BTC + tickers[ticker]).openConnection();
					
					conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
					conn.setRequestProperty("Accept-Charset", "utf-8");
					in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				
					//Collect server response
					for (String line; (line = in.readLine()) != null;)
					{
						JSONObject j = new JSONObject(line);
						JSONArray ja = null;
						boolean array = false;
						
						try
						{
							j = j.getJSONObject("statistics");
						}
						catch (JSONException je)
						{
							ja = j.getJSONArray("statistics");
							array = true;
						}
						
						if (!array)
						{
							JSON += "{\"" + tickers[ticker] + "\":" + j.toString() + "},";
						}
							
						else
							if (ja.length() > 0) 
								JSON += "{\"" + tickers[ticker] + "\":" + ja.toString() + "},";
					}
				}
				JSON = JSON.substring(0, JSON.length() - 2);
				JSON += "}]"; //close JSONArray
			}
			else if (url.contains(PRELUDE_MARKET_USD)) //PRELUDE CONNECT
			{
				String [] tickers = {"888", "AUR", "BC", "DGB", "DGC", "DOGE", "DRK", "EMC2", "LTC", "MAX", "MEOW", "MINT", "PPC", "VTC"};
				
				
				//prep JSON as a JSONArray
				JSON = "[";
				for (int ticker = 0; ticker < tickers.length; ticker++)
				{
					URLConnection conn = new URL(PRELUDE_MARKET_USD + tickers[ticker]).openConnection();
					
					conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
					conn.setRequestProperty("Accept-Charset", "utf-8");
					in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				
					//Collect server response
					for (String line; (line = in.readLine()) != null;)
					{
						JSONObject j = new JSONObject(line);
						JSONArray ja = null;
						boolean array = false;
						
						try
						{
							j = j.getJSONObject("statistics");
						}
						catch (JSONException je)
						{
							ja = j.getJSONArray("statistics");
							array = true;
						}
						
						if (!array)
						{
							JSON += "{\"" + tickers[ticker] + "\":" + j.toString() + "},";
						}
							
						else
							if (ja.length() > 0) 
								JSON += "{\"" + tickers[ticker] + "\":" + ja.toString() + "},";
					}
				}
				JSON = JSON.substring(0, JSON.length() - 2);
				JSON += "}]"; //close JSONArray
			}
			else if (url.contentEquals(BITKONAN_URL)) //BITKONAN CONNECT
			{
				URLConnection con = new URL(BITKONAN_URL + "ticker").openConnection();
				con.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
				con.setRequestProperty("Accept-Charset", "utf-8");
				
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				
				//Set up JSON to be a JSONArray
				JSON = "[";
				while ((temp = in.readLine()) != null)
					JSON += temp;
				
				con = new URL(BITKONAN_URL + "ltc_ticker").openConnection();
				con.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
				con.setRequestProperty("Accept-Charset", "utf-8");
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				
				JSON += ",";
				while ((temp = in.readLine()) != null)
					JSON += temp;
				
				JSON += "]";
			}
		}
		
		catch (IOException ioe)
		{
			if (ioe.getMessage().toUpperCase().contains("403 FOR URL"))
			{
				RavenGUI.log("--" + _exch + ": HTTP 403 Response (Forbidden)");
			}
			else
			{
				RavenGUI.log("--" + _exch + ": " + ioe.getMessage());
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
	
	/**
	 * <p>Replaces instances of <b>stringToReplace</b> with <b>newString</b> inside the <b>container</b> String.</p>
	 * @param container - The main string where occurences take place
	 * @param stringToReplace - String to be replaced
	 * @param newString - String to be inserted
	 * @return <b>null</b> if operation could not be completed. If successful, returns <b>container</b> String after processing.
	 */
	public String strrep(String container, String stringToReplace, String newString)
	{
		int start = 0;
		int length = stringToReplace.length();
		
		if (container.length() > 0 && length > 0 && newString.length() > 0)
			if (container.contains(stringToReplace))
			{
				while (container.contains(stringToReplace))
				{
					start = container.indexOf(stringToReplace);
					String rightsplit = container.substring(start + length);
					container = container.substring(0, start) + newString + rightsplit;
				}
				return container;
			}
			else
				return null;
		else
			return null;
	}
}
