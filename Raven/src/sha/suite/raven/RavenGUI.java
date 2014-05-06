package sha.suite.raven;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisSet;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;
import org.swtchart.ITitle;

//Build GUI according to user settings (GUI thread)
class RavenGUI
{
	/* ************************************************************************************************** *
	 * GUI Controls    																					  *
	 * ************************************************************************************************** */
	static Display display;
	Shell shell;
	
	List coinlist = null;
	List buyFromlist = null;
	List sellTolist = null;
	static List loglist = null;
	List chartlist = null;
	
	Table exchtable = null;
	
	Button updatebut = null;
	Button settingsbut = null;
	Button exchswapbut = null;
	
	Composite chartcomp = null;
	Composite buySellSelComp = null;
	Chart mainchart = null;
	
	Label buyFromLab = null;
	Label sellToLab = null;
	
	Menu menu = null;
	Menu controlsSubmenu = null;
	Menu defaultsSubmenu = null;
	
	MenuItem controlsItem = null;
	MenuItem settingsItem = null;
	MenuItem fullCSVreportItem = null;
	MenuItem infoItem = null;
	MenuItem defaultsItem = null;
	MenuItem resetConfigItem = null;
	MenuItem resetExchangesItem = null;
	MenuItem resetAllItem = null;
	
	ToolBar charttool = null;
	
	ToolItem chartfunclabitem = null;
	ToolItem coindistroitem = null; 
	ToolItem buyspectrumitem = null;
	ToolItem sellspectrumitem = null;
	ToolItem volspectrumitem = null;
	ToolItem priceovertimeitem = null;
	
	
	/* ************************************************************************************************** *
	 * GUI Colors				 																		  *
	 * ************************************************************************************************** */
	
	Color mainFormLight = null;
	Color mainFormDark = null;
	Color controlColor = null;
	Color textColor = null;
	
	/* ************************************************************************************************** *
	 * GUI Positioning Constants 																		  *
	 * ************************************************************************************************** */
	
	//Shells -------------------------------------------------------------
	final private int RAVEN_MAIN_WIDTH = 800;
	final private int RAVEN_MAIN_HEIGHT = 650;
	
	final private int RAVEN_SETTINGS_WIDTH = 485;
	final private int RAVEN_SETTINGS_HEIGHT = 270;
	
	//Layouts ------------------------------------------------------------
	GridLayout mainlayout = null;
	GridLayout chartcomplayout = null;
	GridLayout buySellSelLayout = null;
	GridData [] maindata = null;
	GridData [] tablechartdata = null;
	GridData [] buyselldata = null;
	final int MAINFORM_COUNT = 7;
	final int TABLECHART_COUNT = 2;
	final int BUYSELL_COUNT = 3;
		
	
	/* ************************************************************************************************** *
	 * GUI Global Listeners 																			  *
	 * ************************************************************************************************** */
	
	Listener settings_listener = null;
	Listener generate_full_csv_report = null;
	Listener open_popup_listener = null;
	Listener resetConfigListener = null;
	Listener resetExchangesListener = null;
	Listener resetAllListener = null;
	
	/* ************************************************************************************************** *
	 * GUI Dialogue Constants 																			  *
	 * ************************************************************************************************** */
	
	final String BUY_LABEL = "Buy from (lower = better)";
	final String SELL_LABEL = "Sell to (higher = better)";
	final String BUTTON_DEFAULT_MSG = "Update";
	final String BUTTON_UPDATE_MSG = "Updating...";
	final String LABELS_FONT = "Arial";
	final String DECIMAL_FORMATTING = "#0.0000000";
	final String RAVEN_MAINSHELL_TITLE = "Raven";
	
	
	/* ************************************************************************************************** *
	 * Working variables 																				  *
	 * ************************************************************************************************** */
	String [] coinnames = null;
	int [] commonsizes = null;
	int [] exchcount = null;
	Coin [] exchcoins = null;
	CommonCoins common = null;
	
	java.util.List<Coin> exchlist = null;
	java.util.List<String> marketNames = null;
	java.util.List<String> exchangeNames = null;
	
	java.util.List<String> buyFromNames = null;
	java.util.List<String> sellToNames = null;
	
	Map<String,String> exchangeUrls = null;
	
	boolean safeToUseCoinlist = true;
	String selectedCoinCode = "";
	
	String chartfunction = "coindistro"; //holds which method gets called for the chart when a coin is selected
	final String COIN_DISTRO = "coindistro";
	final String BUY_SPEC = "buyspectrum";
	final String SELL_SPEC = "sellspectrum";
	final String VOL_SPEC = "volspectrum";
	final String PRICE_TIME = "priceovertime";
	
	/* ************************************************************************************************** *
	 * Custom User Settings 																			  *
	 * ************************************************************************************************** */
	//For selected exchanges
	java.util.List<String> masterexchangelist = null;
	java.util.List<String> exchangelist = null;
	Map<String,Boolean> requestedExchs = null;
	int wantedExchs;
	boolean runPopupOnLoad = false;
	
	//For recurring schedule
	Timer updateTimer = null;
	boolean scheduleUpdates = false;
	long scheduledUpdateInterval = 0;
	boolean commitUpdates = false;
	EP ep = null;
	Popup popup = null;
	
	//config.txt and exchanges.txt filepaths
	final String EXCHANGE_FILE_PATH = "exchanges.txt";
	final String CONFIG_FILE_PATH = "config.txt";
	final String RAVEN_EXPORT_FOLDER = "\\Raven CSV Exports";
	
	//For updating the main coin list
	class EP extends Thread
	{
		public void run()
		{
			if (safeToUseCoinlist)
			{
				//Build exchange list and process their coins
				safeToUseCoinlist = false; //Lock the coin list from CSV generation
				display.asyncExec(new Runnable()
				{public void run()
					{
						//Change the button text to provide visual feedback to user
						updatebut.setText(BUTTON_UPDATE_MSG);					
					}
				});
				
				boolean processed = processMarkets();
				safeToUseCoinlist = true; //Unlock the coin list--it's finished its processing
				
				if (processed)
				{
					//Get the list of unique coins and assign them to coinlist
					coinnames = common.getCoinNames();
					commonsizes = common.commonSizes();
					exchcount = common.getAllUniqueExchCount();
					
					display.asyncExec(new Runnable()
					{
						public void run()
						{
							//Remove all list items for fresh output
							coinlist.removeAll();
							
							//Add newly processed coins to the list
							updateCoinList(coinnames, commonsizes, exchcount);
							
							//Change button text back to default to provide visual feedback
							updatebut.setText(BUTTON_DEFAULT_MSG);
							
							if (commitUpdates)
							{
								generateFullCSVReport();
							}
						}
					});
				}
				else
				{
					log("UPDATE: No exchanges were processed because none are selected.");
					display.asyncExec(new Runnable()
					{
						public void run()
						{
							updatebut.setText(BUTTON_DEFAULT_MSG);
						}
					});
				}
			}
		}
	}
	
	class IntervalUpdate extends TimerTask
	{
		@Override
		public void run()
		{
			if (safeToUseCoinlist)
				(ep = new EP()).start();
		}
	}
	
	/**
	 * <p>Displays a pop-up window for the user.</p>
	 * @param popup - Which pop-up to display {"chat"}
	 */
	class Popup extends Thread
	{
		String _popup = null;
		Popup(){}
		
		Popup(String popup)	{_popup = popup;}
		
		public void run()
		{
			if (_popup != null)
			{
				if (_popup.toLowerCase().contentEquals("chat"))
				{
					display.asyncExec(new Runnable()
					{
						@Override
						public void run()
						{
							final Shell popshell = new Shell(display, SWT.SHELL_TRIM & ~SWT.RESIZE);
							
							popshell.setBounds(shell.getBounds().x + 52, shell.getBounds().y + 180, 450, 170);
						
							Link text = new Link(popshell, SWT.NONE);
							Button OK = new Button(popshell, SWT.PUSH);
							
							text.setLocation(10,10);
							OK.setBounds(popshell.getBounds().width / 2 - 50, popshell.getBounds().height - 70, 100, 30);
							
							String linktext = "Thanks for using Raven! If you haven't already, please tell us what you think at\n" +
									"<a href=\"https://bitcointalk.org/index.php?topic=557931.msg6079112#msg6079112\">our Bitcointalk thread</a> (don't be shy!!)\n\n" +
									"" +
									"<a href=\"http://shasoftware.com/OurWork/Software\">Raven official info page</a>\n" +
									"<a href=\"https://github.com/Sha-Software/Raven\">Raven github repository</a>";
							
							popshell.setText("Raven Info");
							if (runPopupOnLoad)
							{
								linktext += "\n\nThis popup is currently set to appear every time you launch Raven. This can be\n" +
									"disabled in the settings menu or config file. If you want to view this popup\n" +
									"without relaunching Raven, you can do so by clicking \"Show Raven Info\" in\n" +
									"the General tab on the menu bar.";
								popshell.setBounds(shell.getBounds().x + 52, shell.getBounds().y + 180, 450, 240);
								OK.setBounds(popshell.getBounds().width / 2 - 50, popshell.getBounds().height - 70, 100, 30);
							}
							
							text.setText(linktext);
							text.pack();
							
							text.addListener(SWT.Selection, new Listener() 
							{
								@Override
								public void handleEvent(Event e)
								{
									try
									{
										openInBrowser(new URI(e.text));
									} 
									catch (IOException ioe)
									{
										ioe.printStackTrace();
									} catch (URISyntaxException urise)
									{
										urise.printStackTrace();
									}
								}
							});
							
							OK.setText("OK");
							OK.addListener(SWT.MouseDown, new Listener()
							{
								@Override
								public void handleEvent(Event arg0)
								{
									popshell.dispose();
								}
							});
							
							popshell.open();
							while(!popshell.isDisposed())
								if (!display.readAndDispatch())
									display.sleep();
							popshell.dispose();
						}
					});
					
				}
			}
			else
				System.out.println("Pop-up choice has not been set (null)");
		}
		
		public void setPopup(String popup) {_popup = popup;}
	}
	
	
	/* ************************************************************************************************** *
	 * Constructor		 																				  *
	 * ************************************************************************************************** */
	
	public RavenGUI()
	{
		exchangelist = new ArrayList<String>();
		masterexchangelist = new ArrayList<String>();
		requestedExchs = new HashMap<String,Boolean>();
		
		exchangeUrls = new HashMap<String,String>();
		exchangeUrls.put("CRYPTSY", "https://www.cryptsy.com/");
		exchangeUrls.put("COINEX", "http://coinex.pw/");
		exchangeUrls.put("BTER", "https://bter.com/");
		exchangeUrls.put("BTC-E", "https://btc-e.com/");
//		exchangeUrls.put("okcoin", "");
		exchangeUrls.put("BITSTAMP", "https://www.bitstamp.net/");
		exchangeUrls.put("BITFINEX", "https://www.bitfinex.com/");
		exchangeUrls.put("MINTPAL", "https://www.mintpal.com/market");
//		exchangeUrls.put("fxbtc", "");
		exchangeUrls.put("KRAKEN", "https://www.kraken.com/market");
//		exchangeUrls.put("mcxnow", "");
		exchangeUrls.put("POLONIEX", "https://poloniex.com/exchange");
		exchangeUrls.put("PRELUDE", "https://prelude.io/");
//		exchangeUrls.put("vircurex", "");
//		exchangeUrls.put("the rock trading", "");
//		exchangeUrls.put("crypto-trade", "");
//		exchangeUrls.put("coinedup", "");
		exchangeUrls.put("BITTREX", "https://bittrex.com/");
//		exchangeUrls.put("atomic-trade", "");
//		exchangeUrls.put("coins-e", "");
//		exchangeUrls.put("cryptonit", "");
		
		maindata = new GridData[MAINFORM_COUNT];
		for (int i = 0; i < MAINFORM_COUNT; i++) maindata[i] = new GridData();
		
		tablechartdata = new GridData[TABLECHART_COUNT];
		for (int i = 0; i < TABLECHART_COUNT; i++) tablechartdata[i] = new GridData();
		
		buyselldata = new GridData[BUYSELL_COUNT];
		for (int i = 0; i < BUYSELL_COUNT; i++) buyselldata[i] = new GridData();
		
		popup = new Popup();
		popup.setPopup("chat");
	}

	
	/* ************************************************************************************************** * 
	 * GUI methods																						  *
	 * ************************************************************************************************** */
   
	/**
	 * <p>Builds the main screen (the first thing) the user sees on-launch.</p>
	 */
	public void buildMainGUI()
	{
		 buildAndPositionControls();
		 loadUserSettings();
		 buildAndAllocateListeners();
		 applyColoring();
		 
		 shell.open();
		 
		 if (runPopupOnLoad) popup.start();
		 
		 while (!shell.isDisposed())
			 if (!display.readAndDispatch())
				 display.sleep();
		 display.dispose();	
	}
	
	/**
	 * <p>Creates, positions, and adds dialogue to all controls on the main form.<p>
	 */
	private void buildAndPositionControls()
	{
		/* *********************************************************************************** */
		 //Create Window and Controls -----------------------------------------------------------
 	 
		//Main window -------------------------------------------------------------------------
		display = new Display();
		shell = new Shell(display, SWT.SHELL_TRIM);
		
		buildMenuBarItems();
		buildToolBar();
		
		//Layouts ------------------------------------------------------------------------
		mainlayout = new GridLayout();
		mainlayout.numColumns = 3;
		
		shell.setLayout(mainlayout);
		setGridDataForControls(false);
		
		coinlist = new List(shell, SWT.BORDER | SWT.V_SCROLL);
		
		//Set up buy and sell selection lists with their composite --------------------------------------------
		buySellSelComp = new Composite(shell, SWT.NONE);
		buySellSelLayout = new GridLayout();
		buySellSelLayout.numColumns = 3;
		buySellSelComp.setLayout(buySellSelLayout);
		
		//buyFromLab = new Label(shell, SWT.NONE);
		buyFromlist = new List(buySellSelComp, SWT.BORDER | SWT.V_SCROLL);
		exchswapbut = new Button(buySellSelComp, SWT.PUSH);
		//sellToLab = new Label(shell, SWT.NONE);
		sellTolist = new List(buySellSelComp, SWT.BORDER | SWT.V_SCROLL);
		
		//Set up main table and chart with their composite ---------------------------------------------------
		chartcomp = new Composite(shell, SWT.NONE);
		chartcomplayout = new GridLayout();
		chartcomplayout.numColumns = 3;
		
		chartcomplayout.makeColumnsEqualWidth = true;
		chartcomp.setLayout(chartcomplayout);
		exchtable = new Table(chartcomp, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		mainchart = new Chart(chartcomp, SWT.NONE);
		mainchart.getTitle().setText("Coin Distribution");
		mainchart.getAxisSet().getXAxis(0).getGrid().setForeground(new Color(display, 255, 255, 255));
		mainchart.getAxisSet().getYAxis(0).getGrid().setForeground(new Color(display, 255, 255, 255));
		
		//Set up controlling buttons and the loglist ---------------------------------------------------------
		updatebut = new Button(shell, SWT.PUSH);
		settingsbut = new Button(shell, SWT.PUSH);
		loglist = new List(shell, SWT.BORDER | SWT.V_SCROLL);
				
		/* *********************************************************************************** */
		//Information and dialogue --------------------------------------------------------------
		//Shell -------------------------------------------------------------------------
		shell.setText(RAVEN_MAINSHELL_TITLE);
		 
		//Lists -------------------------------------------------------------------------
		coinlist.setToolTipText("COIN (# of coins found, # of exchanges found in)");
		buyFromlist.setToolTipText("Double-click an entry to open that exchange's website");
		sellTolist.setToolTipText("Double-click an entry to open that exchange's website");
		 
		//Table -------------------------------------------------------------------------
		exchtable.setLinesVisible(true);
		exchtable.setHeaderVisible(true);
 	 
		//Buttons -------------------------------------------------------------------------
		Font butFont = new Font(display, "", 9, SWT.NONE);
		updatebut.setFont(butFont);
		updatebut.setText("   Update   ");
		updatebut.setToolTipText("Connects to and parses information from selected exchanges");
		 
		settingsbut.setFont(butFont);
		settingsbut.setText("  Settings  ");
		settingsbut.setToolTipText("Opens a new window with settings you can modify");
		 
		exchswapbut.setFont(butFont);
		exchswapbut.setText("<~>");
		exchswapbut.setToolTipText("Swaps the selection of exchanges");
		
		 
		//Labels -------------------------------------------------------------------------
//		buyFromLab.setText(BUY_LABEL);
//		buyFromLab.pack();
//		sellToLab.setText(SELL_LABEL);
//		sellToLab.pack();
		 
		/* *********************************************************************************** */
		//Positioning and sizes -----------------------------------------------------------------
		 
		//Main window -------------------------------------------------------------------------
		shell.setSize(RAVEN_MAIN_WIDTH, RAVEN_MAIN_HEIGHT);
		 
		applyGridData();
		
		//Make the GridLayout properly size and position controls
		shell.layout(true, true);
	}
	
	/**
	 * <p>Creates and allocates listener objects for controls.</p>
	 */
	private void buildAndAllocateListeners()
	{
		 //Lists
		 coinlist.addListener(SWT.Selection, new Listener ()
		 {
		 	@Override
		 	public void handleEvent (Event e)
			{
				//Get a common coin list
				int selection = coinlist.getSelectionIndex();
				exchlist = common.getCommonCoinRow(selection);
				
				//Build a unique list of exchanges
				processNames(exchlist, true);
				
				//Build a unique list of markets
				processNames(exchlist, false);
				
				if (chartfunction.contentEquals(COIN_DISTRO))
					coindistro();
				else if (chartfunction.contentEquals(BUY_SPEC))
					numspectrum('b');
				else if (chartfunction.contentEquals(SELL_SPEC))
					numspectrum('s');
				else if (chartfunction.contentEquals(VOL_SPEC))
					numspectrum('v');
				else if (chartfunction.contentEquals(PRICE_TIME))
					priceovertime();
				
				display.asyncExec(new Runnable()
				{
					@Override
					public void run()
					{	
						//Change title to reflect the coin selected
						 
						shell.setText(RAVEN_MAINSHELL_TITLE + " - " + (selectedCoinCode = exchlist.get(0).getPriCode()));
						
						//Clean lists to prepare for fresh input
						buyFromlist.removeAll();
						sellTolist.removeAll();
						resetTable();
						
						//Update the buyFrom and sellTo lists
						for (String s : exchangeNames)
						{
							buyFromlist.add(s);
							sellTolist.add(s);
						}
					}
				});
			}
		 }); //end coinlist Selection event
	
		 buyFromlist.addListener(SWT.Selection, new Listener () //Single click
		 {
			@Override
			public void handleEvent(Event arg0)
			{
				if (sellTolist.getSelectionCount() > 0 && buyFromlist.getSelectionCount() > 0)
				{
					updateTable(buyFromlist.getItem(buyFromlist.getSelectionIndex()).toString(), sellTolist.getItem(sellTolist.getSelectionIndex()).toString());
				}
			}
		 });
		 
		 buyFromlist.addListener(SWT.MouseDoubleClick, new Listener ()
		 {
			@Override
			public void handleEvent(Event arg0)
			{
				if (buyFromlist.getSelectionCount() > 0)
				{
					try
					{
						openInBrowser(new URI(exchangeUrls.get(buyFromlist.getItem(buyFromlist.getSelectionIndex()))));
					}
					catch (URISyntaxException urise)
					{
						urise.printStackTrace();
					} catch (IOException ioe)
					{
						ioe.printStackTrace();
					}
				}
			}
		 });
		 
		 sellTolist.addListener(SWT.Selection, new Listener () //Single click
		 {
			@Override
			public void handleEvent(Event arg0)
			{
				if (buyFromlist.getSelectionCount() > 0 && sellTolist.getSelectionCount() > 0)
				{
					updateTable(buyFromlist.getItem(buyFromlist.getSelectionIndex()).toString(), sellTolist.getItem(sellTolist.getSelectionIndex()).toString());
				}
			}
		 });
		 
		 sellTolist.addListener(SWT.MouseDoubleClick, new Listener () 
		 {
			@Override
			public void handleEvent(Event arg0)
			{
				if (sellTolist.getSelectionCount() > 0)
				{
					try
					{
						openInBrowser(new URI(exchangeUrls.get(sellTolist.getItem(sellTolist.getSelectionIndex()))));
					}
					catch (URISyntaxException urise)
					{
						urise.printStackTrace();
					}
					catch (IOException ioe)
					{
						ioe.printStackTrace();
					}
				}
			}
		 });
		 
		
		 //Menu bar items
		 settings_listener = new Listener() 
		{
			@Override
			public void handleEvent(Event arg0)
			{
				buildSettingsMenuGUI(display, shell.getBounds().x, shell.getBounds().y);
			}
		};
			
		generate_full_csv_report = new Listener()
		{
			@Override
			public void handleEvent(Event arg0)
			{
				generateFullCSVReport();
			}
		};
		
		open_popup_listener = new Listener()
		{
			@Override
			public void handleEvent(Event arg0)
			{
				(popup = new Popup("chat")).start();
			}
		};
		
		resetConfigListener = new Listener () 
		{
			@Override
			public void handleEvent(Event arg0)
			{
				resetDefaults(0);
				log("DEFAULTS: config.txt has been reset");
			}
		};
		
		resetExchangesListener = new Listener () 
		{
			@Override
			public void handleEvent(Event arg0)
			{
				resetDefaults(1);
				log("DEFAULTS: exchanges.txt has been reset");
			}
		};
		
		resetAllListener = new Listener() 
		{
			@Override
			public void handleEvent(Event arg0)
			{
				resetDefaults(0);
				resetDefaults(1);
				log("DEFAULTS: config.txt & exchanges.txt have been reset");
			}
		};

		//Buttons
		updatebut.addListener(SWT.MouseDown, new Listener ()
		{
			@Override
			public void handleEvent (Event e)
			{
				if (safeToUseCoinlist)
					(ep = new EP()).start();
				else
					log("UPDATE: Raven is currently updating, please wait.");
			}
		});
		 
		exchswapbut.addListener(SWT.MouseDown, new Listener() 
		 {
			@Override
			public void handleEvent(Event arg0)
			{
				if (buyFromlist.getSelectionCount() > 0 && sellTolist.getSelectionCount() > 0)
				{
					int temp = buyFromlist.getSelectionIndex();
					buyFromlist.setSelection(sellTolist.getSelectionIndex());
					sellTolist.setSelection(temp);
					updateTable(buyFromlist.getItem(buyFromlist.getSelectionIndex()).toString(), sellTolist.getItem(sellTolist.getSelectionIndex()).toString());
				}
			}
		 });
		 
		//Set up Shell's Close event handler
		shell.addListener(SWT.Close, new Listener() 
		{
			@Override
			public void handleEvent(Event arg0)
			{
				if (updateTimer != null) updateTimer.cancel();
				killUpdate(false);
			}
		});
		
		coindistroitem.addListener(SWT.Selection, new Listener()
		{
			@Override
			public void handleEvent(Event arg0)
			{
				if (!chartfunction.contentEquals(COIN_DISTRO))
				{
					chartfunction = COIN_DISTRO;
					coindistro();
				}
			}
		});
		
		buyspectrumitem.addListener(SWT.Selection, new Listener()
		{
			@Override
			public void handleEvent(Event arg0)
			{
				if (!chartfunction.contentEquals(BUY_SPEC))
				{
					chartfunction = BUY_SPEC;
					numspectrum('b');
				}
			}
		});
		
		sellspectrumitem.addListener(SWT.Selection, new Listener()
		{
			@Override
			public void handleEvent(Event arg0)
			{
				if (!chartfunction.contentEquals(SELL_SPEC))
				{
					chartfunction = SELL_SPEC;
					numspectrum('s');
				}
			}
		}); 
		
		volspectrumitem.addListener(SWT.Selection, new Listener()
		{
			@Override
			public void handleEvent(Event arg0)
			{
				if (!chartfunction.contentEquals(VOL_SPEC))
				{
					chartfunction = VOL_SPEC;
					numspectrum('v');
				}
			}
		}); 
		
		priceovertimeitem.addListener(SWT.Selection, new Listener()
		{
			@Override
			public void handleEvent(Event arg0)
			{
				if (!chartfunction.contentEquals(PRICE_TIME))
				{
					chartfunction = PRICE_TIME;
					priceovertime();
				}
			}
		});
		
		//Add previously-made listeners
		settingsItem.addListener(SWT.Selection, settings_listener);
		fullCSVreportItem.addListener(SWT.Selection, generate_full_csv_report);
		infoItem.addListener(SWT.Selection, open_popup_listener);
		settingsbut.addListener(SWT.MouseDown, settings_listener);
		resetConfigItem.addListener(SWT.Selection, resetConfigListener);
		resetAllItem.addListener(SWT.Selection, resetAllListener);
		resetExchangesItem.addListener(SWT.Selection, resetExchangesListener);
	}
	
	/**
	 * <p>Creates and allocates menu bar items</p>
	 */
	private void buildMenuBarItems()
	{
		//Menu bar -------------------------------------------------------------------------
		menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);
		
		//Create menu bar buttons/lists
		//Extra controls--------------------------------------------------------------------
		controlsItem = new MenuItem(menu, SWT.CASCADE);
		controlsItem.setText("&General"); //Contains extra controls that don't have room for a button on the form
		controlsSubmenu = new Menu(shell, SWT.DROP_DOWN);
		controlsItem.setMenu(controlsSubmenu);
		
		//Create option to open the settings window
		settingsItem = new MenuItem(controlsSubmenu, SWT.PUSH);
		settingsItem.setText("&Settings");

		//Create option to generate a full CSV report
		fullCSVreportItem = new MenuItem(controlsSubmenu, SWT.PUSH);
		fullCSVreportItem.setText("Generate CSV &report");
		
		infoItem = new MenuItem(controlsSubmenu, SWT.PUSH);
		infoItem.setText("Raven info");
		
		
		//----------------------------------------------------------------------------------
		//Defaults
		defaultsItem = new MenuItem(menu, SWT.CASCADE);
		defaultsItem.setText("&Defaults");
		defaultsSubmenu = new Menu(shell, SWT.DROP_DOWN);
		defaultsItem.setMenu(defaultsSubmenu);
		
		resetConfigItem = new MenuItem(defaultsSubmenu, SWT.PUSH);
		resetConfigItem.setText("Reset config.txt");

		resetExchangesItem = new MenuItem(defaultsSubmenu, SWT.PUSH);
		resetExchangesItem.setText("Reset exchanges.txt");

		resetAllItem = new MenuItem(defaultsSubmenu, SWT.PUSH);
		resetAllItem.setText("Reset all");
	}
	
	/**
	 * <p>Creates and allocates the Toolbar</p>
	 */
	private void buildToolBar()
	{
		charttool = new ToolBar(shell, SWT.BORDER | SWT.WRAP);
		
		chartfunclabitem = new ToolItem(charttool, SWT.None);
		chartfunclabitem.setText("Chart function: ");
		
		coindistroitem = new ToolItem(charttool, SWT.PUSH);
		coindistroitem.setText("Coin distribution");
		
		buyspectrumitem = new ToolItem(charttool, SWT.PUSH);
		buyspectrumitem.setText("Buy prices");
		
		sellspectrumitem = new ToolItem(charttool, SWT.PUSH);
		sellspectrumitem.setText("Sell prices");
		
		volspectrumitem = new ToolItem(charttool, SWT.PUSH);
		volspectrumitem.setText("Volume");
		
		priceovertimeitem = new ToolItem(charttool, SWT.PUSH);
		priceovertimeitem.setText("Prices over time");
	}
	
	/**
	 * <p>Builds and applies layout data to controls on Raven's main form</p>
	 */
	private void setGridDataForControls(boolean apply)
	{
		//Coinlist ------------------------------------------------
		maindata[0].verticalAlignment = GridData.FILL;
		maindata[0].verticalSpan = 2;
		maindata[0].grabExcessVerticalSpace = true;
		maindata[0].widthHint = 70;
		
		//buySellSelComp ------------------------------------------
		maindata[1].horizontalAlignment = GridData.CENTER;
		maindata[1].horizontalSpan = 2;
		
		//loglist -------------------------------------------------
		maindata[2].horizontalAlignment = GridData.FILL;
		maindata[2].grabExcessHorizontalSpace = true;
		maindata[2].horizontalSpan = 3;
		maindata[2].heightHint = 70;
		
		//updatebut -----------------------------------------------
		maindata[3].horizontalAlignment = GridData.CENTER;
		maindata[3].minimumWidth = 100;
		maindata[3].minimumHeight = 35;
		
		//settingsbut ---------------------------------------------
		maindata[4].horizontalAlignment = GridData.BEGINNING;
		maindata[4].minimumWidth = 100;
		maindata[4].minimumHeight = 35;
		
		//chartcomp -----------------------------------------------
		maindata[5].horizontalAlignment = GridData.FILL;
		maindata[5].verticalAlignment = GridData.FILL;
		maindata[5].grabExcessHorizontalSpace = true;
		maindata[5].grabExcessVerticalSpace = true;
		maindata[5].horizontalSpan = 2;
		
		//charttool -----------------------------------------------
		maindata[6].horizontalSpan = 3;
		maindata[6].horizontalAlignment = GridData.END;
		
		//---------------------------------------------------------
		//exchtable -----------------------------------------------
		tablechartdata[0].verticalAlignment = GridData.FILL; 
		tablechartdata[0].horizontalAlignment = GridData.FILL;
		tablechartdata[0].grabExcessHorizontalSpace = true;
		tablechartdata[0].grabExcessVerticalSpace = true;
		tablechartdata[0].horizontalSpan = 1;
		
		//chart ---------------------------------------------------
		tablechartdata[1].horizontalAlignment = GridData.FILL;
		tablechartdata[1].verticalAlignment = GridData.FILL;
		tablechartdata[1].grabExcessHorizontalSpace = true;
		tablechartdata[1].grabExcessVerticalSpace = true;
		tablechartdata[1].horizontalSpan = 2;
		
		//---------------------------------------------------------
		//buyFromList ---------------------------------------------
		buyselldata[0].heightHint = 90;
		buyselldata[0].widthHint = 80;
		
		//exchSwapButton ------------------------------------------
		buyselldata[1].widthHint = 35;
		buyselldata[1].heightHint = 20;
		
		//sellToList ----------------------------------------------
		buyselldata[2].heightHint = 90;
		buyselldata[2].widthHint = 80;
		
		if (apply) applyGridData();
	}
	
	private void applyGridData()
	{
		//Apply layout data to controls
		 coinlist.setLayoutData(maindata[0]);
		 buySellSelComp.setLayoutData(maindata[1]);
		 loglist.setLayoutData(maindata[2]);
		 updatebut.setLayoutData(maindata[3]);
		 settingsbut.setLayoutData(maindata[4]);
		 chartcomp.setLayoutData(maindata[5]);
		 charttool.setLayoutData(maindata[6]);
		
		 exchtable.setLayoutData(tablechartdata[0]);
		 mainchart.setLayoutData(tablechartdata[1]);
		 
		 buyFromlist.setLayoutData(buyselldata[0]);
		 exchswapbut.setLayoutData(buyselldata[1]);
		 sellTolist.setLayoutData(buyselldata[2]);
		 
		 chartcomp.layout(true, true);
	}
	
	private void applyColoring()
	{
		mainFormLight = new Color(display, 89, 89, 89);
		mainFormDark = new Color(display, 64, 64, 64);
		controlColor = new Color(display, 120, 120, 120);
		textColor = new Color(display, 0, 0, 0);
		
		//Apply colors
//		shell.setBackground(mainFormDark);
//		shell.setForeground(textColor);
//		
//		//Chart 
//		chartcomp.setBackground(mainFormLight);
//		chartcomp.setForeground(textColor);
//		
		//mainchart.setBackground(mainFormLight);
		mainchart.setForeground(textColor);
		//mainchart.setBackgroundInPlotArea(controlColor);
		//mainchart.getLegend().setBackground(mainFormLight);
		mainchart.getLegend().setForeground(textColor);
		mainchart.getTitle().setForeground(textColor);
		mainchart.getAxisSet().getXAxis(0).getTitle().setForeground(textColor);
		mainchart.getAxisSet().getYAxis(0).getTitle().setForeground(textColor);
		mainchart.getAxisSet().getXAxis(0).getTick().setForeground(textColor);
		mainchart.getAxisSet().getYAxis(0).getTick().setForeground(textColor);
//		
//		//Container for exchange selection lists
//		buySellSelComp.setBackground(mainFormLight);
//		
//		//Exchange selection lists
//		buyFromlist.setBackground(controlColor);
//		buyFromlist.setForeground(textColor);
//		
//		sellTolist.setBackground(controlColor);
//		sellTolist.setForeground(textColor);
//		
//		//Chart control toolbar
//		charttool.setBackground(controlColor);
//		charttool.setForeground(textColor);
//		
//		//Coin information table
//		exchtable.setBackground(controlColor);
//		exchtable.setForeground(textColor);
//		
//		//Main coin list
//		coinlist.setBackground(controlColor);
//		coinlist.setForeground(textColor);
//		
//		//Log
//		loglist.setBackground(controlColor);
//		loglist.setForeground(textColor);
		
		
		
	}
	
	/**
	 * <p>Builds the options window.</p>
	 */
	private void buildSettingsMenuGUI(Display disp, int x, int y)
	{
		final Shell setshell = new Shell(disp, (SWT.SHELL_TRIM & ~SWT.RESIZE & ~SWT.MAX) | SWT.APPLICATION_MODAL);
		final Table exchChecklist = new Table(setshell, SWT.CHECK | SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE);
		
		Button checkall = new Button(setshell, SWT.PUSH);
		Button uncheckall = new Button(setshell, SWT.PUSH);
		Button savebut = new Button(setshell, SWT.PUSH);
		final Button commitUpdatesBut = new Button(setshell, SWT.CHECK);
		final Button recurringUpdatesBut = new Button(setshell, SWT.CHECK);
		final Button showPopupOnLoadBut = new Button(setshell, SWT.CHECK);
		
		final Text recurringUpdatesInterval = new Text(setshell, SWT.BORDER);
		
		Label recurringLab = new Label(setshell, SWT.NONE);
		
		Group exchangeGroup = new Group(setshell, SWT.SHADOW_NONE);
		Group updateScheduling = new Group(setshell, SWT.SHADOW_NONE);
		Group popupcheck = new Group(setshell, SWT.SHADOW_NONE);
		
		//Positioning constants
		final Rectangle EXCH_GROUP = new Rectangle(10, 10, 255, 200);
		final Rectangle UPDATE_GROUP = new Rectangle(270, 10, 200, 100);
		final Rectangle POPUP_GROUP = new Rectangle(270, 110, 200, 50);
		
		//Sizing and positioning ---------------------------------------------------------
		setshell.setBounds(x - (RAVEN_SETTINGS_WIDTH / 2) + (RAVEN_MAIN_WIDTH / 2), y - (RAVEN_SETTINGS_HEIGHT / 2) + (RAVEN_MAIN_HEIGHT / 2), RAVEN_SETTINGS_WIDTH, RAVEN_SETTINGS_HEIGHT);
		
		//Buttons
		savebut.setBounds(RAVEN_SETTINGS_WIDTH - 150, RAVEN_SETTINGS_HEIGHT - 80, 120, 30);
		checkall.setBounds(EXCH_GROUP.x + 170, EXCH_GROUP.y + 20, 75, 50);
		uncheckall.setBounds(EXCH_GROUP.x + 170, EXCH_GROUP.y + 80, 75, 50);
		commitUpdatesBut.setLocation(280,30);
		recurringUpdatesBut.setLocation(280,50);
		showPopupOnLoadBut.setLocation(POPUP_GROUP.x + 10, POPUP_GROUP.y + 20);
		
		//Textboxes
		recurringUpdatesInterval.setBounds(UPDATE_GROUP.x + 26, UPDATE_GROUP.y + 60, 60, 30);
		
		//Labels
		recurringLab.setLocation(UPDATE_GROUP.x + 90, UPDATE_GROUP.y + 70);
		
		//Lists
		exchChecklist.setBounds(EXCH_GROUP.x + 10, EXCH_GROUP.y + 20, 150, 170);
		
		//Group
		exchangeGroup.setBounds(EXCH_GROUP);
		updateScheduling.setBounds(UPDATE_GROUP);
		popupcheck.setBounds(POPUP_GROUP);
		
		//Information and dialogue -------------------------------------------------------
		setshell.setText("Settings");
		
		//Buttons
		savebut.setText("Save");
		checkall.setText("Select all");
		uncheckall.setText("Deselect all");
		
		commitUpdatesBut.setText("Commit each update to CSV");
		commitUpdatesBut.pack();
		commitUpdatesBut.setToolTipText("Generates a new CSV file every time Raven collects new exchange information");
		
		recurringUpdatesBut.setText("Schedule recurring updates");
		recurringUpdatesBut.pack();
		recurringUpdatesBut.setToolTipText("When checked, Raven automatically updates the coin list at an interval that you specify");
		
		showPopupOnLoadBut.setText("Show popup when Raven starts");
		showPopupOnLoadBut.pack();
		showPopupOnLoadBut.setToolTipText("When checked, a popup for information will appear every time Raven is launched.");
		
		//Textboxes
		recurringUpdatesInterval.setFont(new Font(disp, LABELS_FONT, 16, SWT.NONE));
		
		//Labels
		recurringLab.setText("min"); recurringLab.pack();
		
		//Group
		exchangeGroup.setText("Select exchanges");
		updateScheduling.setText("Update and Scheduling");
		popupcheck.setText("Popups and notifications");
		
		//Exchange group processing
		for (int i = 0; i < masterexchangelist.size(); i++)
		{
			TableItem t = new TableItem(exchChecklist, SWT.NONE);
			t.setText(masterexchangelist.get(i));
			t.setChecked(requestedExchs.get(masterexchangelist.get(i)));
		}
		
		//Update and scheduling group processing
		commitUpdatesBut.setSelection(commitUpdates);
		recurringUpdatesBut.setSelection(scheduleUpdates);
		recurringUpdatesInterval.setText(Long.toString(scheduledUpdateInterval));
		showPopupOnLoadBut.setSelection(runPopupOnLoad);
		
		
		//Event handlers -----------------------------------------------------------------
		checkall.addListener(SWT.MouseDown, new Listener () //------------------------
		{
			@Override
			public void handleEvent(Event e)
			{
				//Check all items in exchChecklist
				TableItem [] temp = exchChecklist.getItems();
				for (int i = 0; i < temp.length; i++)
				{
					temp[i].setChecked(true);
				}
			}
		});
		
		uncheckall.addListener(SWT.MouseDown, new Listener () //------------------------
		{
			@Override
			public void handleEvent(Event e)
			{
				//Check all items in exchChecklist
				TableItem [] temp = exchChecklist.getItems();
				for (int i = 0; i < temp.length; i++)
				{
					temp[i].setChecked(false);
				}
			}
		});
		
		savebut.addListener(SWT.MouseDown, new Listener() //------------------------
		{
			@Override
			public void handleEvent(Event e)
			{
				try
				{
					commitUpdates = commitUpdatesBut.getSelection();
					scheduleUpdates = recurringUpdatesBut.getSelection();
					scheduledUpdateInterval = Long.parseLong(recurringUpdatesInterval.getText());
					runPopupOnLoad = showPopupOnLoadBut.getSelection();
					
					commitUpdatesBut.setText("Commit each update to CSV");
					commitUpdatesBut.pack();
					saveUserSettings(setshell, exchChecklist);
					
				}
				catch (NumberFormatException nfe)
				{
					log("SETTINGS: Interval must be an integer greater than 0");
				}
			}
		}); //end of savebut.addListener() -------------------------------------------
		
		setshell.addListener(SWT.Close, new Listener()
		{
			@Override
			public void handleEvent(Event arg0)
			{
				exchChecklist.dispose();
			}
		});
		setshell.open();
		
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
			{
				display.sleep();
			}	
		}
		 display.dispose();
	}
	
//	private void buildSplash(String welcomeMsg)
//	{
//		
//	}
	
	public static void log(final String msg)
	{
		display.asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				loglist.add(msg);
				
				//Keep the list focused on the last item added
				loglist.setSelection(loglist.getItemCount() - 1);
				loglist.deselectAll();
			}
		});
		
	}
	
	private void resetTable()
	{
		exchtable.removeAll();
		while (exchtable.getColumnCount() > 0) exchtable.getColumn(0).dispose();
		exchtable.setLinesVisible(true);
		exchtable.setHeaderVisible(true);
		exchtable.setLayoutData(tablechartdata[0]);
	}
	
	private void updateTable(String exchSell, String exchBuy)
	{
		DecimalFormat d = new DecimalFormat(DECIMAL_FORMATTING);
		resetTable();
		
		//Create columns and the column header text
		java.util.List<String> colheads = new ArrayList<String>();
		colheads.add("Market");
		colheads.add(exchSell + " Sell Price");
		colheads.add(exchBuy + " Buy Price");
		colheads.add("Difference");
		for (int i = 0; i < colheads.size(); i++)
		{
			TableColumn column = new TableColumn(exchtable, SWT.BORDER);
			column.setResizable(true);
			column.setText(colheads.get(i));
		}//end of column creation ---------------------------------------------------------------------
		
		
		boolean uncommonMarkets = true;
		TableItem i = null;
		//Fill exchtable according to the two selected exchanges
		for (int record = 0; record < marketNames.size(); record++)
		{
			Coin buyer = getCoin(exchSell, marketNames.get(record));
			Coin seller = getCoin(exchBuy, marketNames.get(record));
			
			if (buyer != null && seller != null)
			{
				i = new TableItem(exchtable, SWT.NONE);
				
				i.setText(0, marketNames.get(record));
				i.setText(1, d.format(buyer.getBuy()));
				i.setText(2, d.format(seller.getSell()));
				i.setText(3, d.format(seller.getSell() - buyer.getBuy()));
				
				uncommonMarkets = false;
			}
		}
		
		if (uncommonMarkets)
		{
			//Remove columns and add 1 item explaining the error
			while (exchtable.getColumnCount() > 0) exchtable.getColumn(0).dispose();
			i = new TableItem(exchtable, SWT.NONE);
			i.setText(0, "No common markets were found between " + buyFromlist.getItem(buyFromlist.getSelectionIndex()) + " and " + sellTolist.getItem(sellTolist.getSelectionIndex()));
		}
		
		//Size columns
		for (int idx = 0; idx < exchtable.getColumnCount(); idx++)
			exchtable.getColumn(idx).pack();
	}
	
	private void updateCoinList(String [] coins, int [] numcoins, int [] numexchs)
	{
		for (int i = 0; i < coins.length; i++)
		{
			coinlist.add(coins[i] + " (" + numcoins[i] + "," + numexchs[i] + ")");
		}
	}
	
	/* ******************************************************************************************************* *
	 * Non-GUI methods																						   *
	 * ******************************************************************************************************* */
	
	private Coin getCoin(String exchange, String market)
	{
		for (int i = 0; i < exchlist.size(); i++)
		{
			if (exchlist.get(i).getExchange().contentEquals(exchange))
				if (exchlist.get(i).getSecCode().contentEquals(market))
					return exchlist.get(i);
		}
		return null;
	}
	
	/**
	 * 
	 * @param coins - use <b>true</b> to process a list of exchanges. Use <b>false</b> for market names.</b>
	 * @param processExchanges
	 */
	private void processNames(java.util.List<Coin> coins, boolean processExchanges)
	{
		if (processExchanges)
		{
			exchangeNames = new ArrayList<String>();
			for (int coin = 0; coin < coins.size(); coin++)
			{
				boolean valid = true;
				for (int test = 0; test < exchangeNames.size(); test++)
				{
					if (exchangeNames.get(test).contentEquals(coins.get(coin).getExchange()))
					{
						//Duplicate found, skip to next coin
						valid = false;
						test = exchangeNames.size();
					}
					else
						valid = true;
				}
				if (valid)
					exchangeNames.add(coins.get(coin).getExchange());
			}
		}
		else
		{
			marketNames = new ArrayList<String>();
			for (int coin = 0; coin < coins.size(); coin++)
			{
				boolean valid = true;
				for (int test = 0; test < marketNames.size(); test++)
				{
					if (marketNames.get(test).contentEquals(coins.get(coin).getSecCode()))
					{
						//Duplicate found, skip to next coin
						valid = false;
						test = marketNames.size();
					}
					else
						valid = true;
				}
				if (valid)
					marketNames.add(coins.get(coin).getSecCode());
			}
		}
	}
	
	/**
	 * <p>Writes the entirety of coins parsed from exchanges to a CSV file.</p>
	 */
	private void generateFullCSVReport()
	{
		if (coinlist.getItemCount() > 0)
		{
			if (safeToUseCoinlist)
			{
				log("GENERATE CSV: Stamping export with today's date");
			
				Date d = new Date();
				DateFormat df = new SimpleDateFormat("YYYY-MM-DD hh;mm;ss");
				String filename = null;
				
				String basepath = new File("").getAbsolutePath();
				
				File path = new File(basepath + RAVEN_EXPORT_FOLDER);
				path.mkdirs();
				
				filename = path.getAbsolutePath() + "\\Raven_AllCoinsExport " + df.format(d) + ".csv";
				
				FileWriter fw = null;
				BufferedWriter bw = null;
				PrintWriter pw = null;
				try
				{
					fw = new FileWriter(filename);
					bw = new BufferedWriter(fw);
					pw = new PrintWriter(bw);
					
					log("GENERATE CSV: Writing coin information to file...");
					pw.println("Coin Code,Exchange,Market,Buy,Sell,Last,Volume");
					for (int coin = 0; coin < common.uniqueSize(); coin++)
					{	
						for (int exchcoin = 0; exchcoin < common.commonSize(coin); exchcoin++)
						{
							Coin c = common.getCommonCoinRow(coin).get(exchcoin);
							pw.println(c.getPriCode() + "," + c.getExchange() + "," + c.getSecCode() + "," + c.getBuy() + "," + c.getSell() + "," + c.getVolume() + "," + c.getLastTrade() + "");
						}
						pw.println("");
					}
					log("GENERATE CSV: Finished!");
				}
				catch (IOException e)
				{
					log("--GENERATE CSV: Unable to generate CSV report.");
				}
				finally
				{
					try
					{
						if (pw != null) pw.close();
						if (bw != null) bw.close();
						if (fw != null) fw.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
			else
			{
				log("--GENERATE CSV: The unique coin list is processing--please wait.");
			}
		}
		else
		{
			log("--GENERATE CSV: No coins have been processed yet!");
		}	
	}

	/**
	 * <p>Creates a recurring schedule to update the main coin list.</p>
	 * @param active TRUE to enable the schedule
	 * @param interval How long the interval is (expects input as milliseconds)
	 * @param commitPerInterval TRUE to generate a full CSV report per update
	 */
	private void setRecurringUpdate(boolean active, long interval, boolean commitPerInterval)
	{
		if (active)
		{
			if (updateTimer == null)
			{
				//Enable the recurring schedule
				updateTimer = new Timer();
				updateTimer.schedule(new IntervalUpdate(), 0, interval*60000);
			}
		}
		else
		{
			//Disable the recurring schedule
			if (updateTimer != null)
			{
				updateTimer.cancel(); 
				updateTimer = null;
			}
		}
		//Commit settings to memory
		scheduleUpdates = active;
		scheduledUpdateInterval = interval;
		commitUpdates = commitPerInterval;
	}
	
	public void resetDefaults(int file)
	{
		if (file == 0)
		{
			//Reset config.txt to defaults
			FileWriter fw = null;
			BufferedWriter bw = null;
			PrintWriter pw = null;
			try
			{
				fw = new FileWriter(CONFIG_FILE_PATH);
				bw = new BufferedWriter(fw);
				pw = new PrintWriter(bw);
				
				pw.println("# RAVEN CONFIGURATION FILE");
				pw.println("#");
				pw.println("# This config.txt file contains all settings excluding which exchanges will be processed (re: exchanges.txt).");
				pw.println("#");
				pw.println("# If this file becomes corrupted or moved, Raven will generate a new one either on launch or when settings");
				pw.println("# are saved. Raven can generate a default config.txt or exchanges.txt for you whenver you like--just click one");
				pw.println("# of the drop-down options in the main screen (Defaults).");
				pw.println("#");
				pw.println("# UPDATE_COMMIT controls if Raven generates a full CSV report every time it updates automatically");
				pw.println("#");
				pw.println("# INTERVAL_UPDATE controls if Raven automatically updates the coin list");
				pw.println("# INTERVAL_UPDATE_TIME controls the amount of time Raven waits to update (in minutes, integer values (> 0) only)");
				pw.println("#");
				pw.println("# RUN_POPUP_ONLOAD controls if the Raven info popup displays when Raven is launched");
				pw.println("UPDATE_COMMIT:false");
				pw.println("INTERVAL_UPDATE:false");
				pw.println("INTERVAL_UPDATE_TIME:5");
				pw.println("RUN_POPUP_ONLOAD:true");
				
				//Reset settings currently in memory
				commitUpdates = false;
				scheduleUpdates = false;
				scheduledUpdateInterval = 5;
				updateTimer = null;
			}
			catch (IOException e){e.printStackTrace();}
			finally
			{
				try
				{
					pw.close();
					bw.close();
					fw.close();
				}
				catch (IOException e){e.printStackTrace();}
			}
		}
		else if (file == 1)
		{
			//Reset exchanges.txt to defaults
			FileWriter fw = null;
			BufferedWriter bw = null;
			PrintWriter pw = null;
			try
			{
				fw = new FileWriter(EXCHANGE_FILE_PATH);
				bw = new BufferedWriter(fw);
				pw = new PrintWriter(bw);
				
				pw.println("# RAVEN EXCHANGES SELECTION");
				pw.println("# This file lists each exchange that Raven can connect to and parse from.");
				pw.println("# Each listed exchange has the option if you want its info or not.");
				pw.println("# The settings menu in Raven modifies this file with a nice checklist");
				pw.println("# but you can still modify it by doing exactly what you're doing right now.");
				pw.println("#");
				pw.println("# If you add an exchange that isn't here by default, Raven will ignore it when");
				pw.println("# you try to run the exchange update.");
				pw.println("# The format is <exchange name, parseboolean>");
				pw.println("#");
				pw.println("# exchange name = The exchange to parse info from");
				pw.println("# parseboolean = Set to 'T' if you wants this exchange's info; F otherwise.");
				pw.println("#");
				pw.println("# Use the # symbol if you want to write a comment in this file.");
				pw.println("");
				
				//RESET DEFAULT EXCHANGES
				pw.println("atomic trade,F");
				pw.println("bitfinex,T");
				pw.println("bitstamp,T");
				pw.println("bittrex,T");
				pw.println("btc-e,F");
				pw.println("bter,T");
				pw.println("crypto-trade,F");
				pw.println("cryptonit,F");
				pw.println("cryptsy,T");
				pw.println("coinedup,F");
				pw.println("coinex,T");
				pw.println("coins-e,F");
				pw.println("fxbtc,F");
				pw.println("kraken,T");
				pw.println("mcxnow,F");
				pw.println("mintpal,T");
				pw.println("okcoin,F");
				pw.println("prelude,T");
				pw.println("poloniex,T");
				pw.println("the rock trading,F");
				pw.println("vircurex,F");
				
				//Reset settings currently in memory
				exchangelist = new ArrayList<String>();
				exchangelist.add("bitfinex");
				exchangelist.add("bitstamp");
				exchangelist.add("bittrex");
				exchangelist.add("bter");
				exchangelist.add("cryptsy");
				exchangelist.add("coinex");
				exchangelist.add("kraken");
				exchangelist.add("mintpal");
				exchangelist.add("prelude");
				exchangelist.add("poloniex");
			}
			catch (IOException e){e.printStackTrace();}
			finally
			{
				try
				{
					pw.close();
					bw.close();
					fw.close();
				}
				catch (IOException e){e.printStackTrace();}
			}
		}
		if (file == 0 || file == 1)
			loadUserSettings();
	}
	
	public void loadUserSettings()
	{
		System.out.println("Loading user settings");
		FileReader frIn = null;
		BufferedReader settings = null;
		
		//Load config --------------------------------------------------------------
		try
		{
			//Check if exchanges.txt exists
			frIn = new FileReader(CONFIG_FILE_PATH);
		}
		catch (FileNotFoundException e)
		{
			System.out.println("Raven has lost track of " + CONFIG_FILE_PATH + " or this is the first time running the program.");
			System.out.println("Creating a new config.txt file.");
			resetDefaults(0);
			try
			{
				frIn = new FileReader(CONFIG_FILE_PATH);
			}catch (IOException e1){e1.printStackTrace();}
		}//end of config.txt allocation/creation
		
		settings = new BufferedReader(frIn);
		String temp;
		try
		{
			Map<String, Object> userSettings = new HashMap<String, Object>();
			
			temp = settings.readLine();
			while (temp != null)
			{
				if (!temp.startsWith("#") && temp.trim().length() > 0)
				{
					//Read settings into a HashMap
					userSettings.put(temp.substring(0, temp.indexOf(":")), temp.substring(temp.indexOf(":") + 1));
				}
				//Get next line
				temp = settings.readLine();
			}
			
			//Apply settings ------------------------------------------------------------------------------
			//---------------------------------------------------------------------------------------------
			
			//Recurring schedule settings
			boolean activeSchedule = false;
			try {activeSchedule = (userSettings.get("INTERVAL_UPDATE").toString().equalsIgnoreCase("true")) ? true : false;}
			catch (NullPointerException npe)
			{
				log("SETTINGS: Error loading INTERVAL_UPDATE boolean, using default (false)");
			}
			boolean commitPerUpdate = false;
			try {commitPerUpdate = (userSettings.get("UPDATE_COMMIT").toString().equalsIgnoreCase("true")) ? true : false;}
			catch (NullPointerException npe)
			{
				log("SETTINGS: Error loading UPDATE_COMMIT boolean, using default (false)");
			}
			
			long interval = 0;
			try
			{
				interval = Long.parseLong(userSettings.get("INTERVAL_UPDATE_TIME").toString());
				setRecurringUpdate(activeSchedule, interval, commitPerUpdate);
			}
			catch (NumberFormatException nfe)
			{
				log("SETTINGS: Error loading INTERVAL_UPDATE_TIME number, using default (5 min) instead.");
				setRecurringUpdate(activeSchedule, 60000*5, commitPerUpdate);
			}
			catch (NullPointerException npe)
			{
				log("SETTINGS: Error loading INTERVAL_UPDATE_TIME number, using default (5 min) instead.");
				setRecurringUpdate(activeSchedule, 60000*5, commitPerUpdate);
			}
			
			//Popup setting
			try	{runPopupOnLoad = (userSettings.get("RUN_POPUP_ONLOAD").toString().equalsIgnoreCase("true")) ? true : false;}
			catch (NullPointerException npe)
			{
				log("SETTINGS: Error loading RUN_POPUP_ONLOAD, using default (true)");
				runPopupOnLoad = true;
			}
			
			
			
			//Other settings
		}
		catch (IOException e2)
		{
			e2.printStackTrace();
		}
		finally
		{
			try
			{
				settings.close();
				frIn.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		
		//Load exchanges ------------------------------------------------------------
		try
		{
			//Check if exchanges.txt exists
			frIn = new FileReader(EXCHANGE_FILE_PATH);
		}
		catch (FileNotFoundException e)
		{
			System.out.println("Raven has lost track of " + EXCHANGE_FILE_PATH + " or this is the first time running the program.");
			System.out.println("Creating a new exchanges.txt file.");
			resetDefaults(1);
			try
			{
				frIn = new FileReader(EXCHANGE_FILE_PATH);
			}catch (IOException e1){e1.printStackTrace();}
		}//end of exchanges.txt allocation/creation
		
		try
		{
			settings = new BufferedReader(frIn);
			temp = settings.readLine();
			
			masterexchangelist = new ArrayList<String>();
			
			while (temp != null)
			{
				temp = temp.trim();
				if (!temp.startsWith("#") && temp.length() > 0)
				{
					String exch = temp.substring(0, temp.indexOf(","));
					boolean bool = temp.substring(temp.indexOf(",") + 1).toUpperCase().contentEquals("T") ? true : false;
					
					masterexchangelist.add(exch);
					
					if (bool)
						exchangelist.add(exch);
					requestedExchs.put(exch, bool);
					
					temp = settings.readLine();
				}
				else
				{
					temp = settings.readLine();
				}
			}
		}
		catch (IOException e){e.printStackTrace();}
		finally
		{
			try
			{
				settings.close();
				frIn.close();
			}catch (IOException e){e.printStackTrace();}
		}
		System.out.println("Exchange settings loaded");
		
		//Load other settings -----------------------------------------------------------------
		
		System.out.println("...done!");
	}
	
	public void saveUserSettings(Shell setshell, Table exchChecklist)
	{
		log("SETTINGS: Writing settings to " + CONFIG_FILE_PATH);
		FileReader fr = null;
		BufferedReader br = null;
		try
		{
			fr = new FileReader(CONFIG_FILE_PATH);
		}
		//In case the exchanges file was moved while Raven is running
		catch (IOException e1)
		{
			resetDefaults(0);
			try
			{
				fr = new FileReader(CONFIG_FILE_PATH);
			}
			catch (FileNotFoundException e2)
			{
				e2.printStackTrace();
			}
		}
		
		//Save any comments written in the text file
		br = new BufferedReader(fr);
		java.util.List<String> comments = new ArrayList<String>();
		try
		{
			String temp = br.readLine();
			while (temp != null)
			{
				if (temp.startsWith("#"))
				{
					comments.add(temp);
				}
				temp = br.readLine();
			}
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		finally
		{
			try
			{
				br.close();
				fr.close();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}
		
		FileWriter fw = null;
		BufferedWriter bw = null;
		PrintWriter pw = null;
		try
		{
			fw = new FileWriter(CONFIG_FILE_PATH);
			bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			
			for (int i = 0; i < comments.size(); i++)
			{
				pw.println(comments.get(i));
			}
			
			pw.println("UPDATE_COMMIT:" + Boolean.toString(commitUpdates));
			pw.println("INTERVAL_UPDATE:" + Boolean.toString(scheduleUpdates));
			pw.println("INTERVAL_UPDATE_TIME:" + Long.toString(scheduledUpdateInterval));
			pw.println("RUN_POPUP_ONLOAD:" + Boolean.toString(runPopupOnLoad));
			
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		finally
		{
			pw.close();
			try
			{
				bw.close();
				fw.close();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}//finished saving config settings ---------------------------------------------------------
		
		log("SETTINGS: Writing settings to " + EXCHANGE_FILE_PATH);
		requestedExchs = new HashMap<String,Boolean>();

		
		try
		{
			fr = new FileReader(EXCHANGE_FILE_PATH);
		}
		//In case the exchanges file was moved while Raven is running
		catch (IOException e1)
		{
			resetDefaults(0);
			try
			{
				fr = new FileReader(EXCHANGE_FILE_PATH);
			}
			catch (FileNotFoundException e2)
			{
				e2.printStackTrace();
			}
		}
		
		//Save any comments written in the text file
		br = new BufferedReader(fr);
		comments = new ArrayList<String>();
		try
		{
			String temp = br.readLine();
			while (temp != null)
			{
				if (temp.startsWith("#"))
				{
					comments.add(temp);
				}
				temp = br.readLine();
			}
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		finally
		{
			try
			{
				br.close();
				fr.close();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}
		
		try
		{
			fw = new FileWriter(EXCHANGE_FILE_PATH);
			bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			
			for (int i = 0; i < comments.size(); i++)
			{
				pw.println(comments.get(i));
			}
			
			exchangelist = new ArrayList<String>();
			
			for (int i = 0; i < masterexchangelist.size(); i++)
			{
				String bool = (exchChecklist.getItem(i).getChecked()) ? "T" : "F";
				pw.println(exchChecklist.getItem(i).getText() + "," + bool);
			}
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		finally
		{
			pw.close();
			try
			{
				bw.close();
				fw.close();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
			
		}//finished saving exchanges -----------------------------------------------------------------
		RavenGUI.log("SETTINGS: Done!");
		setRecurringUpdate(scheduleUpdates, scheduledUpdateInterval, commitUpdates);		
		loadUserSettings();
		setshell.dispose();
	
	}
	
	/**
	 * <p>Allocates exchange information and then finds common coins across exchanges and lists them together.</p>
	 * <p>Returns the number of exchanges that were processed.</p>
	 * @return
	 */
	public boolean processMarkets()
	{
		if (exchangelist.size() > 0)
		{
			java.util.List<CoinRoster> exchanges = new ArrayList<CoinRoster>();
				
			//Fill each requested element with exchange info
			//for (int i = 0; i < requestedExchs.size(); i++)
			for (int i = 0; i < exchangelist.size(); i++)
			{
				if (requestedExchs.get(exchangelist.get(i)))
				{
					exchanges.add(new CoinRoster(exchangelist.get(i)));
					
					//Verify the exchange was properly processed.
					if (!exchanges.get(exchanges.size() - 1).verified())
					{
						exchanges.remove(exchanges.size() - 1);
					}
				}
			}
			
			common = new CommonCoins();
			
			//Add all exchs to common for common-coin processing
			for (int i = 0; i < exchanges.size(); i++)
			{
				common.addExchange(exchanges.get(i));
			}
			
			//Process the coins
			RavenGUI.log(common.processExchanges());
			
			return true;
		}
		else
		{
			return false;
		}
	}

	private boolean openInBrowser(URI uri) throws IOException
	{
		if (Desktop.isDesktopSupported())
		{
			Desktop.getDesktop().browse(uri);
			return true;
		}
		return false;
	}
	
	/**
	 * <p>Kills the EP thread which updates the coinlist if it's running.</p>
	 * 
	 * @param outputReason - provide <b>true</b> if you want to know why this method fails, if it does.
	 */
	@SuppressWarnings({"deprecation"})
	private void killUpdate(boolean outputReason)
	{
		if (ep != null && !safeToUseCoinlist) 
			ep.stop();
		else 
			if (outputReason && ep == null)
				System.out.println("Unable to stop Update thread because the thread instantiation does not exist.");
			else if (outputReason && safeToUseCoinlist)
				System.out.println("Unable to stop Update thread because it has finished executing.");
	}
	
	
	public void coindistro()
	{
		mainchart.getTitle().setText("Coin Distribution");
		int sel = coinlist.getSelectionIndex();
		if (sel != -1)
		{
			Map<String, Integer>exchcount = new HashMap<String, Integer>();
			for (int i = 0; i < exchangeNames.size(); i++)
				exchcount.put(exchangeNames.get(i), 0);
			
			java.util.List<Coin> coins = common.getCommonCoinRow(sel);
			
			for (int coin = 0; coin < coins.size(); coin++)
				exchcount.put(coins.get(coin).getExchange(), exchcount.get(coins.get(coin).getExchange()) + 1);
			
			double [] yseries = new double[exchcount.size()];
			
			//Output the HashMap's values to a Double array
			int i = 0;
			for (Entry<String, Integer> entry : exchcount.entrySet())
			{
				 String key = entry.getKey();
				 yseries[i] = exchcount.get(key);
				 i++;
			}
			
			//Apply series to the chart
			ISeriesSet seriesset = mainchart.getSeriesSet();
			IBarSeries series = (IBarSeries) seriesset.createSeries(SeriesType.BAR, "Coin");
			
			series.getLabel().setFormat("#");
			series.getLabel().setVisible(true);
			series.setYSeries(yseries);
			series.setBarColor(mainFormDark);
			series.setBarPadding(40);
			
			IAxisSet xset = mainchart.getAxisSet();
			IAxis xAxis = xset.getXAxis(0);
			String [] names = exchangeNames.toArray(new String[exchangeNames.size()]);
			xAxis.setCategorySeries(names);
			xAxis.enableCategory(true);
			
			//Color the data
			series.getLabel().setForeground(textColor);
						
			//Adjust the range so all the data can be seen at the same time
			mainchart.getAxisSet().adjustRange();
		}
		mainchart.redraw(); //visually update the chart
	}
	
	/**
	 * 
	 * @param operation - <ul><li>char '<b>b</b>': collects buy price over selected coin</li><li>char '<b>s</b>': collects sell price over selected coin</li><li>char '<b>v</b>': collects volume numbers over selected coin</li></ul>
	 */
	public void numspectrum(char operation)
	{
		String title = "";
		switch (operation)
		{
			case 'b':
				title = "Buy Prices";
				break;
			case 's':
				title = "Sell Prices";
				break;
			case 'v':
				title = "Volume";
				break;
			default:
				
		}
			
		mainchart.getTitle().setText(title);
		int sel = coinlist.getSelectionIndex();
		if (sel != -1)
		{
			//Set up working lists for buy info and coin codes (for labeling)
			java.util.List<Double> nums = new ArrayList<Double>();
			java.util.List<String> codes = new ArrayList<String>();
			java.util.List<Coin> coins = common.getCommonCoinRow(sel);
			
			for (int coin = 0; coin < coins.size(); coin++)
			{
				Coin t = coins.get(coin);
				switch (operation)
				{
					case 'b':
						//Add value to nums list
						nums.add(t.getBuy());
						break;
					case 's':
						nums.add(t.getSell());
						break;
					case 'v':
						nums.add(t.getVolume());
						break;
				}
				
				//Add the priCode (Coin code) and secCode (Market code) in the sister codes List<String>
				codes.add(t.getExchange().substring(0, 4) + "_" + t.getPriCode() + "_" + t.getSecCode());
			}
			
			//once collected, convert into a double []
			double[] yseries = new double[nums.size()];
			for (int i = 0; i < yseries.length; i++)
				yseries[i] = nums.get(i);
			
			//Apply the double [] series to the chart, appropriately labeling
			ISeriesSet seriesset = mainchart.getSeriesSet();
			IBarSeries ser = (IBarSeries) seriesset.createSeries(SeriesType.BAR, "Coin");
			ser.setYSeries(yseries);
			
			ser.getLabel().setFormat("##.0000#");
			ser.getLabel().setVisible(true);
			ser.setBarColor(mainFormDark);
			ser.setBarPadding(40);
			
			IAxisSet xset = mainchart.getAxisSet();
			IAxis xAxis = xset.getXAxis(0);
			String [] names = codes.toArray(new String[codes.size()]);
			xAxis.setCategorySeries(names);
			xAxis.enableCategory(true);
		}
		
		mainchart.getAxisSet().adjustRange();
		mainchart.redraw();
	}
	
	public void priceovertime()
	{
		//mainchart.getTitle().setText("Prices Over Time");
		mainchart.redraw();
		RavenGUI.log("--CHART: \"Prices over time\" function currently unavailable");
	}
}

