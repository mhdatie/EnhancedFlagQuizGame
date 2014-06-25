package com.mohamedatie.flagquizgame;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.games.GamesClient;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.mohamedatie.flagquizgame.R;

public class MainActivity extends BaseGameActivity
implements View.OnClickListener {

	private Button startgameButton;//button to start game
	private Button top5button; //shared preferences 
	private Button leaderboardsButton, achievementsButton; // online leader boards & achievements
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); //remove title bar
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choicemenu); //inflate the GUI
		
		startgameButton = (Button) findViewById(R.id.startgameButton);
		top5button = (Button)findViewById(R.id.top5Button);
		leaderboardsButton = (Button)findViewById(R.id.LeaderboardsButton);
		achievementsButton = (Button)findViewById(R.id.achievementsButton);
		
		findViewById(R.id.sign_in_button).setOnClickListener(this);
	    findViewById(R.id.sign_out_button).setOnClickListener(this); 
	   
	    final int RC_UNUSED = 5001;
		
		startgameButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent highIntent = new Intent(getApplicationContext(), GameActivity.class);
				startActivity(highIntent);	
			}

		});
		
		top5button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent highIntent = new Intent(getApplicationContext(), TopFive.class);
				startActivity(highIntent);
			}
		});
		
		leaderboardsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(isSignedIn()){
					startActivityForResult(getGamesClient().getLeaderboardIntent(getResources().getString(R.string.lead_id)), RC_UNUSED);
				}else{
					Toast.makeText(getApplicationContext(), "Not Signed In" , Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		
		achievementsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(isSignedIn()){
					startActivityForResult(getGamesClient().getAchievementsIntent(), RC_UNUSED);
				}else{
					Toast.makeText(getApplicationContext(), "Not Signed In" , Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		
	}//end method onCreate
	
	@Override
	public void onSignInFailed() {
		 // Sign in has failed. So show the user the sign-in button.
	    findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
	    findViewById(R.id.sign_out_button).setVisibility(View.GONE);
	}

	@Override
	public void onSignInSucceeded() {
		 // show sign-out button, hide the sign-in button
	    findViewById(R.id.sign_in_button).setVisibility(View.GONE);
	    findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);

	    // Leaderboards check score
	    SharedPreferences scorePrefs = getSharedPreferences(GameActivity.PREFS_NAME, 0);
	    SharedPreferences.Editor scoreEdit = scorePrefs.edit();
		Long lead = scorePrefs.getLong("lead", 0);
		if(lead>0){
			getGamesClient().submitScore(getResources().getString(R.string.lead_id), lead);
			scoreEdit.putLong("lead", 0);
		}
		
		// Achievements check
		int correctcaps = scorePrefs.getInt("caps", 0);
		int wins = scorePrefs.getInt("wins", 0);
		
		if(wins == 5){
			getGamesClient().unlockAchievement(getResources().getString(R.string.ach_id1)); //win 5 games
		}
		if(wins == 10){
			getGamesClient().unlockAchievement(getResources().getString(R.string.ach_id2)); //win 10 games
		}
			
		if(correctcaps == 2){
			getGamesClient().unlockAchievement(getResources().getString(R.string.ach_id3)); //guess 2 capitals
		}else if(correctcaps == 5){
			getGamesClient().unlockAchievement(getResources().getString(R.string.ach_id4)); //guess 5 capitals
		}else if(correctcaps == 10){
			getGamesClient().unlockAchievement(getResources().getString(R.string.ach_id5)); //guess 10 capitals
		}
		
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.sign_in_button) {
	        // start the asynchronous sign in flow
	        beginUserInitiatedSignIn();
	    }
	    else if (v.getId() == R.id.sign_out_button) {
	        // sign out.
	        signOut();

	        // show sign-in button, hide the sign-out button
	        findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
	        findViewById(R.id.sign_out_button).setVisibility(View.GONE);
	    }
		
	}
	

	
}//end MainActivity
