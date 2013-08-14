package marketdatapersistor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import listeners.DefaultCTPListener;
import nativeinterfaces.MarketDataNativeInterface;
import properties.PropertiesManager;
import bardatamanager.BarDataManager;
import bardatamanager.EntryNotInitializedException;
import bo.BarData;
import bo.LoginResponse;
import bo.MarketDataResponse;
import dao.BarDataDAO;


public class Test {
	static{
		System.loadLibrary("CTPDLL");
	}
	
	private static class ICThread implements Runnable{
		
		@Override
		public void run() {
	        MarketDataNativeInterface nativeInterface = new MarketDataNativeInterface();
	         nativeInterface.subscribeListener(new ICMarketDataListener());
	        String[] quote2 = {"IF1309"};
	        barDataManger.initializeEntry("IF1309", 10000);
	        nativeInterface.sendLoginMessage("1013", "123321", "00000008", PropertiesManager.getInstance().getProperty("marketdataurl"));
	        nativeInterface.sendQuoteRequest(quote2);
	        while(true){
	        	try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
			
		}
		
	}
	
	private BarDataDAO barDao = new BarDataDAO();
	private static BarDataManager barDataManger = new BarDataManager();
	private static class ICMarketDataListener extends DefaultCTPListener{
		private SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHH:mm:ss");
		@Override
		public void onRspUserLogin(LoginResponse loginResponse) {
			System.out.println("logged in successfully");
		}

		@Override
		public void onRtnDepthMarketData(MarketDataResponse response) {
			

			
			try {
				Date updateTime = formatter.parse(response.getTradingDay() + response.getUpdateTime());
				response.setMillisecConversionTime(updateTime.getTime());
				BarData compiledData = barDataManger.sendMarketData(response);
				if(compiledData != null){
					BarDataDAO bao = new BarDataDAO();
					bao.addBarData(compiledData);
				}
			} catch (EntryNotInitializedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		
	}
	
	public static void main(String args[]){
		new Thread(new ICThread()).start();
	}
}
