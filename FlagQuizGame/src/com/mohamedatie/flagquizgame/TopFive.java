package com.mohamedatie.flagquizgame;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.mohamedatie.flagquizgame.R;

public class TopFive extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); //remove title bar
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.top5view);
	   //---------GET LOCAL SCORES AND DISPLAY TO END USER------------------------------//
	    TextView scoreView = (TextView)findViewById(R.id.top5text);
	    SharedPreferences scorePrefs = getSharedPreferences(GameActivity.PREFS_NAME, 0);//get the sharedprefs saved from other thr other activity
	    String scores = scorePrefs.getString("highScores", "");
	    
	    if(scores.length()>0){
	    	String[] savedScores = scores.split("\\|");
	    	StringBuilder scoreBuild = new StringBuilder("Highscores:\n");
	  	   
	  	    for(String score : savedScores){
	  	        scoreBuild.append(score+"\n");
	  	    }
	  	  
	  	    scoreView.setText(scoreBuild.toString());
	    	
	    }else{
	    	scoreView.setText("No Highscores");
	    }
	   
	}

}
