package com.ctthosting.sbutransit;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.HttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.glass.app.Card;
import com.google.android.glass.media.Sounds;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.speech.RecognizerIntent;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends Activity {
	
	public Card card1 = null;
	Map<String, String> busRoutesByID = new HashMap<String, String>();
	Map<String, String> busRoutesByName = new HashMap<String, String>();
	
	private AudioManager mAudioManager;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_main);
        
        ArrayList<String> voiceResults = getIntent().getExtras().getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
		String spokenText = voiceResults.get(0);
		
		if(spokenText == "" || spokenText == null)
			spokenText = "none";
		
        card1 = new Card(this);
        card1.setText("Sorry, we could not register that command. Try again.");
        card1.setFootnote(spokenText);
        View card1View = card1.toView();
        setContentView(card1View);
        
        setup();
        parseFunction(spokenText);
    }

    public void setup()
    {
    	//Bus routes with ID
    	busRoutesByID.put("Express Route", "7");
    	busRoutesByID.put("Hospital/Chapin Route", "3");
    	busRoutesByID.put("Inner Loop", "5");
    	busRoutesByID.put("Outer Loop", "4");
    	busRoutesByID.put("R&D Park Shuttle", "1");
    	busRoutesByID.put("Railroad Route", "6");
    	busRoutesByID.put("Southampton Shuttle", "8");
    	busRoutesByID.put("Shopping Route East - Sundays", "2");
    	busRoutesByID.put("Shopping Route West - Sundays", "13");   	
    	//busRoutesByID.put("Railroad Route 1", "11");
    	//busRoutesByID.put("Railroad Route 2", "12");
    	
    	//Bus routes by name
    	busRoutesByName.put("Express", "Express Route");
    	busRoutesByName.put("Hospital", "Hospital/Chapin Route");
    	busRoutesByName.put("Chapin", "Hospital/Chapin Route");
    	busRoutesByName.put("Inner", "Inner Loop");
    	busRoutesByName.put("Outer", "Outer Loop");
    	busRoutesByName.put("R&D", "R&D Park Shuttle");
    	busRoutesByName.put("Park Shuttle", "R&D Park Shuttle");
    	busRoutesByName.put("Railroad", "Railroad Route");
    	busRoutesByName.put("Southampton", "Southampton Shuttle");
    	busRoutesByName.put("Shopping Route East", "Shopping Route East - Sundays");
    	busRoutesByName.put("Shopping Route West", "Shopping Route West - Sundays");   	
    	//busRoutesByName.put("", "Railroad Route 1");
    	//busRoutesByName.put("", "Railroad Route 2");
    	
    }
    
    public void parseFunction(String spokenText)
    {
    	if(spokenText.indexOf("where is") > -1)
    		parseRoute(spokenText.replace("where is ", ""), "where");
    	if(spokenText.indexOf("when is") > -1)
    		parseRoute(spokenText.replace("when is ", ""), "when");
    }
    
    public void parseRoute(String spokenText, String additional)
    {
    	for (Map.Entry<String, String> entry : busRoutesByID.entrySet()) {
    	    String key = entry.getKey();
    	    String value = entry.getValue();
    	    
    	    System.out.println("[Testing] - Found key \"" + key + "\" and value \"" + value + "\" for spoken text " + spokenText);
    	    
    	    if(spokenText.indexOf(key.toLowerCase()) > -1)
    	    {
    	    	card1.setText("Found route '" + value + "'");
    	    	View card1View = card1.toView();
                setContentView(card1View);
                
                System.out.println("[Testing] - found match: " + key.toLowerCase());
                
                showMapForRoute(value);
    	    }
    	    
    	}
    }
    
    public void showMapForRoute(String routeNumber)
    {
    	card1.setText("show Map for route");
	    View card1View = card1.toView();
        setContentView(card1View);
        
    	String requestURL = "http://local.h.cttapp.com/sbu/list_buses/?id=" + routeNumber;
    	System.out.println("URL: ");
    	System.out.println(requestURL);
    	
    	AsyncHttpClient client = new AsyncHttpClient();
		client.get(requestURL, new AsyncHttpResponseHandler() {
			@Override
			public void onStart()
			{
				System.out.println("[Testing] - On Start");
				card1.setText("Loading route information...");
			    View card1View = card1.toView();
		        setContentView(card1View);
			}

			@Override
			public void onFinish()
			{
				System.out.println("[Testing] - On Finish");
			}
			
			@Override
			public void onFailure(Throwable e) {
				System.out.println("[testing] - Is failure, " + e.getMessage());
				
				mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				mAudioManager.playSoundEffect(Sounds.ERROR);
				
				card1.setText("Sorry, we could not connect.");
		        card1.setFootnote(e.getMessage());
			    View card1View = card1.toView();
		        setContentView(card1View);
		        
			    if (e instanceof HttpResponseException) {
			        HttpResponseException ex = (HttpResponseException) e;
			        int statusCode = ex.getStatusCode();
			        System.out.println("[Testing] - On Failure: Status code =" + statusCode);
			      
			    }
			}
			/*public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.Throwable error, java.lang.String content) {
				System.out.println("[Testing] - On Failure: Status code =" + statusCode + "\nContent: \n" + content);
            }
            */
			
		    @Override
		    public void onSuccess(String response) {
		        
		        System.out.println(response);
		        JSONArray routeResult = null;
				try {
					routeResult = new JSONArray(response);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				JSONObject bus1 = null;
		        try {
					bus1 = routeResult.getJSONObject(0);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        
		        String lat = null, lon = null;
				try {
					lat = bus1.getString("lat");
					lon = bus1.getString("lon");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        
		        String mapURL = getMapUrl(Double.parseDouble(lat), Double.parseDouble(lon), 640, 360);
		        
		        
		        System.out.println("Map URL: " + mapURL);
		        
		        card1.setText("Bus");
		        card1.setImageLayout(Card.ImageLayout.FULL);
		        card1.addImage(Uri.parse("android.resource://com.ctthosting.sbutransit/raw/staticmap"));
			    View card1View = card1.toView();
		        //setContentView(card1View);
			    
			    Drawable mapImg = LoadImageFromWebOperations(mapURL);
			    
			    ImageView img = (ImageView) findViewById(R.id.imageView1);
			    //img.setImageDrawable();
			    setContentView(R.layout.activity_main);
		    }
		});
    }
    
    public static String getMapUrl(double latitude, double longitude, double currentLat, double currentLon, int width, int height) {
        try {
            String raw = "https://maps.googleapis.com/maps/api/staticmap?sensor=false&size=" + width + "x" + height +
                "&style=feature:all|element:all|saturation:-100|lightness:-25|gamma:0.5|visibility:simplified" +
                "&style=feature:roads|element:geometry&style=feature:landscape|element:geometry|lightness:-25" +
                "&markers=icon:" + URLEncoder.encode("http://mirror-api.appspot.com/glass/images/map_dot.png",
                "UTF-8") + "|shadow:false|" + currentLat + "," + "" + currentLon+"&markers=color:0xF7594A|" + latitude + "," + longitude;
            return raw.replace("|", "%7C");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
    
    public static String getMapUrl(double latitude, double longitude, int width, int height) {
        String raw = "https://maps.googleapis.com/maps/api/staticmap?sensor=false&size=" + width + "x" + height +
		    "&style=feature:all|element:all|saturation:-100|lightness:-25|gamma:0.5|visibility:simplified" +
		    "&style=feature:roads|element:geometry&style=feature:landscape|element:geometry|lightness:-25" +
		    "&markers=color:0xF7594A|" + latitude + "," + longitude + "&zoom=17";
		return raw.replace("|", "%7C");
    }
    
    public static Drawable LoadImageFromWebOperations(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        } catch (Exception e) {
            return null;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
