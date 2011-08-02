package cc.rainwave.android;

import cc.rainwave.android.api.Session;
import cc.rainwave.android.api.types.ScheduleOrganizer;
import cc.rainwave.android.api.types.Song;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.MalformedURLException;

public class NowPlayingActivity extends Activity {
	private static final String TAG = "NowPlaying";
	
	private ScheduleOrganizer mOrganizer;
	
	private Session mSession;
	
	private FetchInfo mFetchInfo;
	
	private LongPollTask mLongPoll;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_nowplaying);
    }
    
    @Override
    public void onStart() {
        super.onStart();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if(mSession == null) {
            initializeSession();
        }
        
        fetchSchedules();
    }

    public void onPause() {
    	super.onPause();
    	stopTasks();
    }
    
    public void onStop() {
    	super.onStop();
    }
    
    public void onDestroy() {
    	super.onDestroy();
    }
    
    private void stopTasks() {
    	if(mFetchInfo != null) {
    		mFetchInfo.cancel(true);
    		mFetchInfo = null;
    	}
    	
    	if(mLongPoll != null) {
    		mLongPoll.cancel(true);
    		mLongPoll = null;
    	}
    }
    
    private void fetchSchedules() {
        if(mSession == null) {
            // TODO: Some error here.
            return;
        }
        
        if(mFetchInfo == null) {
            mFetchInfo = new FetchInfo();
            mFetchInfo.execute();
        }
    }
    
    private void startLongPoll() {
    	if(mFetchInfo == null && mLongPoll == null) {
    		mLongPoll = new LongPollTask();
    		mLongPoll.execute();
    	}
    }
    
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_preferences:
			Intent i = new Intent(this, RainwavePreferenceActivity.class);
			startActivity(i);
			break;
		}
		return false;
	}
    
    private void initializeSession() {
        try {
            mSession = Session.makeSession(this);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    
    private void updateSchedule() {
    	if(mOrganizer == null) {
    	    // TODO: Some error here.
    	    return;
    	}
    	
    	Song current = mOrganizer.getCurrentSong();
    	((TextView) findViewById(R.id.np_songTitle)).setText(current.song_title);
    	((TextView) findViewById(R.id.np_albumTitle)).setText(current.album_name);
    	((TextView) findViewById(R.id.np_artist)).setText(current.collapseArtists());
    }
    
    private void updateAlbumArt(Bitmap art) {
        if(art == null) {
            // TODO: Some error here.
            art = BitmapFactory.decodeResource(getResources(), R.drawable.noart);
        }
        
        ((ImageView) findViewById(R.id.np_albumArt)).setImageBitmap(art);
    }
    
    /**
     * Fetches the now playing info.
     * @author pkilgo
     *
     */
    protected class FetchInfo extends AsyncTask<String, Integer, Bundle> {

        @Override
        protected Bundle doInBackground(String... s) {
        	Log.d(TAG, "Starting initial fetch.");
        	
            Bundle b = new Bundle();
            try {
                ScheduleOrganizer organizer = mSession.asyncGet();
                b.putParcelable(SCHEDULE, organizer);
                
                Song song = organizer.getCurrentSong();
                Bitmap art = mSession.fetchAlbumArt(song.album_art);
                b.putParcelable(ART, art);

                return b;
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            return b;
        }
        
        protected void onPostExecute(Bundle result) {
            super.onPostExecute(result);
            mFetchInfo = null;
            
            if(result == null) {
            	// TODO: Error ?
            	Log.e(TAG, "Initial fetch finished with error.");
            	return;
            }
            
            mOrganizer = result.getParcelable(SCHEDULE);
            updateSchedule();
            updateAlbumArt( (Bitmap) result.getParcelable(ART) );
            
            startLongPoll();
            
            Log.d(TAG, "Initial fetch finished.");
        }
    }
    
    protected class LongPollTask extends AsyncTask<String, Integer, Bundle> {
        @Override
        protected Bundle doInBackground(String... s) {
            if(!mSession.isAuthenticated()) {
            	Log.d(TAG, "Not starting long poll because of no auth details.");
            	return null;
            }
            
            Log.d(TAG, "Starting long poll.");
            
            Bundle b = new Bundle();
            try {
                ScheduleOrganizer organizer;
                organizer = mSession.syncGet();
                b.putParcelable(SCHEDULE, organizer);
                
                Song song = organizer.getCurrentSong();
                Bitmap art = mSession.fetchAlbumArt(song.album_art);
                b.putParcelable(ART, art);
                
                return b;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        
        protected void onPostExecute(Bundle result) {
            super.onPostExecute(result);
            
            if(result == null) {
                // TODO: Error here.
            	Log.e(TAG, "Long poll finished with error.");
            	mLongPoll = null;
                return;
            }
            
            mOrganizer = result.getParcelable(SCHEDULE);
            updateSchedule();
            updateAlbumArt( (Bitmap) result.getParcelable(ART) );
            
            mLongPoll = new LongPollTask();
            mLongPoll.execute();
            
            Log.d(TAG, "Long poll finished.");
        }
    }
    
    public static final String
        SCHEDULE = "schedule",
        ART = "art";
}