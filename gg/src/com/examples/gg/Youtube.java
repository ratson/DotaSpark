package com.examples.gg;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
 
public class Youtube extends SherlockListFragment {
  
	
	static ArrayList<String> MOBILE_OS;
	private ArrayList<String> titles;
	private ArrayList<Video> videolist;
	private String q2 = "https://gdata.youtube.com/feeds/api/users/dotacinema/playlists?v=2&max-results=50&alt=json";
	private String q3 = "https://gdata.youtube.com/feeds/api/users/noobfromua/playlists?v=2&max-results=50&alt=json";
	private VideoArrayAdapter vaa;
	private String section;
	private SherlockFragmentActivity sfa;

	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		
		
		sfa = this.getSherlockActivity();
		savedInstanceState = this.getArguments();
		section = savedInstanceState.getString("SECTION");
		String[] Options = new String[] {};
		titles = new ArrayList<String>();
		videolist  = new ArrayList<Video>();
		
		MOBILE_OS = new ArrayList<String>(Arrays.asList(Options));
		
		View view = inflater.inflate(android.R.layout.list_content, null);

	    ListView ls = (ListView) view.findViewById(android.R.id.list);
	    vaa = new VideoArrayAdapter(inflater.getContext(), titles, videolist);
	    //ls.setAdapter(new MobileArrayAdapter(inflater.getContext(), MOBILE_OS));
	    //we are in section which contains uploaders only
	    
	    if(section.equals("UPLOADER")){
	    	titles.add("DotaCinema");
	    	titles.add("noobfromua");
	    	Video dotacinema = new Video();
	    	dotacinema.setAuthor("DotaCinema");
	    	dotacinema.setPlaylistUrl("http://gdata.youtube.com/feeds/api/users/dotacinema/uploads?start-index=1&max-results=10&v=2&alt=json");
	    	dotacinema.setThumbnailUrl("https://i1.ytimg.com/i/NRQ-DWUXf4UVN9L31Y9f3Q/1.jpg?v=5067cf3b");
	    	dotacinema.setTitle("Videos from DotaCinema");
	    	dotacinema.setUploaderThumUrl("https://i1.ytimg.com/i/NRQ-DWUXf4UVN9L31Y9f3Q/1.jpg?v=5067cf3b");
	    	
	    	Video noobfromua = new Video();
	    	noobfromua.setAuthor("noobfromua");
	    	noobfromua.setPlaylistUrl("http://gdata.youtube.com/feeds/api/users/noobfromua/uploads?start-index=1&max-results=10&v=2&alt=json");
	    	noobfromua.setThumbnailUrl("https://i1.ytimg.com/i/fsOfLvadg89Bx8Sv_6WERg/1.jpg?v=515d687f");
	    	noobfromua.setTitle("Videos from noobfromua");
	    	noobfromua.setUploaderThumUrl("https://i1.ytimg.com/i/fsOfLvadg89Bx8Sv_6WERg/1.jpg?v=515d687f");
	    	
	    	
	    	videolist.add(dotacinema);
	    	videolist.add(noobfromua);
	    	
	    }
	    
	    
	    
	    ls.setAdapter(vaa);
	    ls.setDivider(null);
	    ls.setDividerHeight(0);

		if(section.equals("PLAYLIST")){
			//sfa.findViewById(R.id.content_frame).setVisibility(View.GONE);
			sfa.findViewById(R.id.fullscreen_loading_indicator).setVisibility(View.VISIBLE);
			new YoutubeGetRequest2().execute(q2);
			new YoutubeGetRequest2().execute(q3);
		}
		return view;
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
 
		//get selected items
		//String selectedValue = (String) getListAdapter().getItem(position);
		//Toast.makeText(this, selectedValue, Toast.LENGTH_SHORT).show();
		//pd = ProgressDialog.show(appContext,"Loading","Dota spark is working hard to load videos for you!",true,false,null);



		//sfa.findViewById(R.id.content_frame).setVisibility(View.GONE);
		
		
		InternetConnection ic = new InternetConnection();
		if(ic.isOnline(sfa)){
			//internet is ok
			//start loading
			sfa.findViewById(R.id.fullscreen_loading_indicator).setVisibility(View.VISIBLE);
			new YoutubeGetRequest().execute(videolist.get(position).getPlaylistUrl());		
		}else{
			ic.networkToast(sfa);
		}

	
	}
	
	
	
	private class YoutubeGetRequest extends AsyncTask<String, String, String>{
	    private JSONObject feed;
	    public YoutubeGetRequest(){
	        ;
	    }
	    @Override
	    protected String doInBackground(String... uri) {
	    	
	    
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        String responseString = null;
	        try {
	            response = httpclient.execute(new HttpGet(uri[0]));
	            StatusLine statusLine = response.getStatusLine();
	            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                response.getEntity().writeTo(out);
	                out.close();
	                responseString = out.toString();
	            } else{
	                //Closes the connection.
	                response.getEntity().getContent().close();
	                throw new IOException(statusLine.getReasonPhrase());
	            }
	        } catch (ClientProtocolException e) {
	            //TODO Handle problems..
	        } catch (IOException e) {
	            //TODO Handle problems..
	        }
	        return responseString;
	    }

	    @Override
	    protected void onPostExecute(String result) {
	        super.onPostExecute(result);
	        //Do anything with response..
	        //System.out.println(result);
	        
	        try
	        {   
	            processJSON(result);
	        } catch (JSONException e)
	        {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	        
	        List<String> titles=new ArrayList<String>();   
	        List<String> ids = new ArrayList<String>();
	        
	        YoutubeFeed ytf = null;
			try {
				ytf = new YoutubeFeed(result);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        ArrayList<Video> videos = ytf.getVideoPlaylist();
	        for(Video v:videos){
//	            System.out.println(v.getVideoId());
	        	titles.add(v.getTitle());
	        	ids.add(v.getVideoId());

	        }
	        
	        String[] mStringArray = new String[titles.size()];
			mStringArray = titles.toArray(mStringArray);
			
	        String[] idsArray = new String[ids.size()];
	        idsArray = ids.toArray(idsArray);

			for(int i = 0; i < mStringArray.length ; i++){
			    System.out.println(mStringArray[i]);
			}
	        
	        //we can get more videos from this playlist if there are more
	        try
	        {
	            System.out.println("***************************Next api: "+ytf.getNextApi());
	        } catch (JSONException e)
	        {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	        
	        //pd.dismiss();
			FragmentTransaction ft = getFragmentManager().beginTransaction();
		// Locate Position

//==========================================================================================================
			getSherlockActivity().getSupportActionBar().setTitle("Videolist");
			Fragment videolist = new videolist();
			
	        Bundle bundle = new Bundle();
	        bundle.putParcelableArrayList("videolist", videos);
	        
	        try {
	        	bundle.putString("query", ytf.getNextApi());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        videolist.setArguments(bundle);
			
			ft.replace(R.id.content_frame, videolist);
			ft.commit();
			
//=========================================================================================================


	    }
	    
	    private void processJSON(String json) throws JSONException{
	        JSONTokener jsonParser = new JSONTokener(json);  
	        // 闆庯拷锝斤饯闅撮瘔锝斤蕉楂村彴锝匡蕉璎旓酱楂礁锟斤交闄凤娇閭忓叿锝斤交锟斤交闂栵酱锠栵浆son闅达拷锝匡浇璎旓浇锟斤浇璁欎富锝筹僵闅版殾锝斤渐楂礁锟斤交闄凤娇楂拷锝斤桨锟斤奖闅存摼锝斤蒋闂曪匠锟斤浇锟斤礁锟斤姜JSONObject闄濓拷锝斤焦楂叮锝斤健閭碉讲锟斤浇 
	        // 闄烇蒋绻у煙锝ｏ健闆庯拷锝斤饯闅撮瘔锝斤蕉楱撅涧锟斤浇锟斤蒋锟斤交闄凤娇閭忓叿锝斤浇铚ワ讲锟斤浇锟斤疆闄滐建锟斤建"name" : 闂旓拷锝匡浇锟斤郊閬掆垰鈮﹂棔锝碉拷锝絜xtValue闄濓拷锝斤奖闅存摼锝斤蒋"yuanzhifei89"锟斤浇锟斤浇tring锟斤浇锟斤浇 
	        JSONObject wholeJson = (JSONObject) jsonParser.nextValue();  
	        // 闅版殾锝斤渐闂曪匠铔癸胶璎倠锟斤拷锝斤拷锝帮拷锝遍毚鎿撅浇锝疛SON闄濓拷锝斤焦楂叮锝斤健楱撅涧锟斤浇璀拷鎶勮珱浼氾浇锝猴拷锝�
	        this.feed = wholeJson.getJSONObject("feed");
	        
	        
	    }
	    
	   
	}
	
	private class YoutubeGetRequest2 extends AsyncTask<String, String, String>{
	    private JSONObject feed;
	    public YoutubeGetRequest2(){
	        ;
	    }
	    @Override
	    protected String doInBackground(String... uri) {
	    	
	    
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        String responseString = null;
	        try {
	            response = httpclient.execute(new HttpGet(uri[0]));
	            StatusLine statusLine = response.getStatusLine();
	            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                response.getEntity().writeTo(out);
	                out.close();
	                responseString = out.toString();
	            } else{
	                //Closes the connection.
	                response.getEntity().getContent().close();
	                throw new IOException(statusLine.getReasonPhrase());
	            }
	        } catch (ClientProtocolException e) {
	            //TODO Handle problems..
	        } catch (IOException e) {
	            //TODO Handle problems..
	        }
	        return responseString;
	    }

	    @Override
	    protected void onPostExecute(String result) {
	        //Do anything with response..
	        //System.out.println(result);
	    	YoutubeFeed ytf = null;
	        try
	        {   
	            ytf = new YoutubeFeed(result);
	        } catch (JSONException e)
	        {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	        
	        
	        
	        List<Video> newVideos = ytf.getVideoPlaylist2();
	        for(Video v:newVideos){
//	            System.out.println(v.getVideoId());
	        	String theTitle = v.getTitle();
	        	if((theTitle.toUpperCase().contains("DOTA") || theTitle.toUpperCase().contains("GAMEPLAY"))
	        			&& !theTitle.toUpperCase().contains("ASSASSIN")){
		        	titles.add(v.getTitle());
		        	//videos.add(v.getVideoId());
		        	
		        	videolist.add(v);
	        	}
	        }
	        
//	        for(String s:titles){
//	        	System.out.println(s);
//	        }

//	        ListFragment lFrag = (ListFragment) getFragmentManager().findFragmentById(android.R.id.list);
//	        BaseAdapter adapter = (BaseAdapter) lFrag.getListAdapter();
//	        adapter.notifyDataSetChanged();
	        vaa.notifyDataSetChanged();
	        
	        //Make the loading view invisible
	        sfa.findViewById(R.id.fullscreen_loading_indicator).setVisibility(View.GONE);
	        
	        //show content view
	        //sfa.findViewById(R.id.content_frame).setVisibility(View.VISIBLE);
	        //pd.dismiss();
	        

	        //rl.setVisibility(View.GONE);
			


	    }
	    
	    private void processJSON(String json) throws JSONException{
	        JSONTokener jsonParser = new JSONTokener(json);  

	        JSONObject wholeJson = (JSONObject) jsonParser.nextValue();  

	        this.feed = wholeJson.getJSONObject("feed");
	        
	        
	    }
	    
	   
	}
 
}