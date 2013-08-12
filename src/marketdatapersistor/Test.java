package marketdatapersistor;

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
	private BarDataDAO barDao = new BarDataDAO();
	private static BarDataManager barDataManger = new BarDataManager();
	private static class ICMarketDataListener extends DefaultCTPListener{

		@Override
		public void onRspUserLogin(LoginResponse loginResponse) {
			System.out.println("logged in successfully");
		}

		@Override
		public void onRtnDepthMarketData(MarketDataResponse response) {
			try {
				
				BarData compiledData = barDataManger.sendMarketData(response);
				if(compiledData != null){
					BarDataDAO bao = new BarDataDAO();
					bao.addBarData(compiledData);
				}
			} catch (EntryNotInitializedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		
	}
	
	public static void main(String args[]){
        MarketDataNativeInterface nativeInterface = new MarketDataNativeInterface();
         nativeInterface.subscribeListener(new ICMarketDataListener());
        String[] quote2 = {"IF1309"};
        nativeInterface.sendLoginMessage("1013", "123321", "00000008", PropertiesManager.getInstance().getProperty("marketdataurl"));
        nativeInterface.sendQuoteRequest(quote2);
	}
}
