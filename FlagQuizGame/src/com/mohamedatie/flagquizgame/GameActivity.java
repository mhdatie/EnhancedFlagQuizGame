package com.mohamedatie.flagquizgame;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.example.games.basegameutils.BaseGameActivity;
import com.mohamedatie.flagquizgame.R;

/** */

public class GameActivity extends BaseGameActivity { //extends BaseGameActivity in order to use google+ utilities 
	
private static final String TAG="FlagQuizGame Activity";
	
	private List<String> fileNameList; //flag file names
	private List<String> quizCountriesList; //names of countries in quiz
	private Map<String,Boolean> regionsMap; // which regions are enabled
	private String correctAnswer; //correct country for the current flag
	private int totalGuesses; //number of guesses made
	private int correctAnswers; //number of correct guesses
	private int guessRows; //number of rows displaying choices
	private boolean firstguess; //detect first guess
	private int firstguesses; //keep count of first guesses
	private double finalscore;//player's score
	private int guessesperflag; //number of guesses per flag
	private String linktocountry;//wiki link
	private String capitalGuess; //User's capital guess
	private String capital;//correct capital of a country
	private Random random; //random number generator
	//private Handler handler; //used to delay loading next flag
	private Animation shakeAnimation; //animation for incorrect guess
	
	//private TextView answerTextView;//displays Correct or Incorrect
	private TextView questionNumberTextView;//shows current question #
	private ImageView flagImageView; //displays a flag
	private TableLayout buttonTableLayout; //table of answer Buttons
	
	private int numberofwins;
	private int numberofcorrectcapitals;
	//shared preferences
	public static final String PREFS_NAME = "MyPreferences";
	
	private SharedPreferences settings;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); //remove title bar
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main); //inflate the GUI
		settings = getSharedPreferences(PREFS_NAME,0);
	
		ConstructActivityGame(); //sets up activity_main layout
	}
	
	//overrides the onBackPressed to confirm the user whether or not they want to leave the current game...
	@Override
		public void onBackPressed() {
			
			AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(this);
			confirmBuilder.setTitle("Proceeding ends this current game");
			confirmBuilder.setIcon(android.R.drawable.ic_dialog_alert);
			confirmBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();//kills the current Activity "GameActivity"
				}
			});
			
			confirmBuilder.setNegativeButton(R.string.stay, null);
			
			AlertDialog confirmDialog = confirmBuilder.create();
			confirmDialog.show();
			
		}
	
	private void ConstructActivityGame(){
		fileNameList = new ArrayList<String>();
		quizCountriesList = new ArrayList<String>();
		regionsMap = new HashMap<String, Boolean>();
		guessRows = 1;//default to one row of choices
		random = new Random();
		
		//load the shake animation that's used for incorrect answers
		shakeAnimation=
				AnimationUtils.loadAnimation(this, R.anim.incorrect_shake);
		shakeAnimation.setRepeatCount(3); //animation repeats 3 times
		
		//get array of world regions from strings.xml
		String[] regionNames=getResources().getStringArray(R.array.regionsList);
		
		//by default, countries are chosen from all regions
		for(String region:regionNames)
			regionsMap.put(region, true);
		
		//get references to GUI components
		questionNumberTextView=
				(TextView)findViewById(R.id.questionNumberTextView);
		flagImageView=(ImageView)findViewById(R.id.flagImageView);
		buttonTableLayout=(TableLayout)findViewById(R.id.buttonTableLayout);
		
		//set questionNumberTextView's text
		questionNumberTextView.setText(
				getResources().getString(R.string.question) + " 1 " +
		getResources().getString(R.string.of)+ " 10" );
		
		resetQuiz(); //start a new quiz	
	}

	private void resetQuiz() {
		// use the AssetManager to get the image flag
		//file names for only the enabled regions
		AssetManager assets = getAssets();
		fileNameList.clear(); //empty the list
		
		try{
			Set<String> regions = regionsMap.keySet(); //get Set of regions
			
			//loop through each region
			for(String region: regions){
				if(regionsMap.get(region)){ //if region is enabled
					// get a list of all flag image files in this region
					String[] paths = assets.list(region);
					for(String path: paths)
						fileNameList.add(path.replace(".png", ""));
				}//end if
			}//end for
		}//end try
		catch (IOException e){
			Log.e(TAG, "Error loading image file names",e);
		}//end catch
		
		correctAnswers =0;//reset the number of correct answers made
		totalGuesses = 0;//reset the total number of guesses the user made
		firstguesses=0;//count of first guesses
		finalscore=0; //reset final score
		capital="";
		numberofcorrectcapitals = 0;
		numberofwins = settings.getInt("wins", 0); //get the stored number of wins
		
		quizCountriesList.clear(); //clear prior list of quiz countries
		
		//add 10 random file names to the quizCountriesList
		int flagCounter=1;
		int numberOfFlags = fileNameList.size();//get number of flags
		
		while(flagCounter <=10){
			int randomIndex = random.nextInt(numberOfFlags); //random index
			
			//get the random file name
			String fileName = fileNameList.get(randomIndex);
			
			//if the regions is enabled and it hasn't already been chosen
			if(!quizCountriesList.contains(fileName)){
				quizCountriesList.add(fileName);
				++flagCounter;
			}//end if
		}//end while
		
		
		loadNextFlag(); //start the quiz by loading the first flag
	}//end method resetQuiz

	//after the user guesses a correct flag,load the next flag
	private void loadNextFlag() {
		firstguess = true; //set it true
		guessesperflag = 0;
		linktocountry="";
		
		// get file name of the next flag and remove it from the list
		String nextImageName = quizCountriesList.remove(0);
		correctAnswer = nextImageName; //update the correct answer
		
		//answerTextView.setText(""); //clear answerTextView
		
		//display the number of the current question in the quiz
		questionNumberTextView.setText(getResources().getString(R.string.question) + " " +
		(correctAnswers + 1) + " " + 
				getResources().getString(R.string.of)+ " 10");
		
		//extract the region from the next image's name
		String region = nextImageName.substring(0, nextImageName.indexOf('-'));
		
		//use AssetManager to load next image from assets folder
		AssetManager assets = getAssets(); //get app's AssetManager
		InputStream stream; //used to read in flag images
		
		try{
			//get an inputstream to the asset representing the next flag
			stream = assets.open(region + "/" + nextImageName + ".png");
			//load the asset as a Drawable and display on the flagImageView
			Drawable flag = Drawable.createFromStream(stream, nextImageName);
			flagImageView.setImageDrawable(flag);
		}//end try
		catch(IOException e){
			Log.e(TAG,"Error loading " + nextImageName, e);
		} //end catch
		
		//clear prior answer Buttons from TableRows
		for(int row = 0;row<buttonTableLayout.getChildCount();++row){
			((TableRow) buttonTableLayout.getChildAt(row)).removeAllViews();
		}
		
		Collections.shuffle(fileNameList); //shuffle file names
		
		//put the correct answer to the end of fileNameList
		int correct = fileNameList.indexOf(correctAnswer);
		fileNameList.add(fileNameList.remove(correct));
		
		//get a reference to the LayoutInflater service
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		//add 3,6, or 9 answer Buttons based on the value of guessRows
		for(int row=0;row<guessRows;row++){
			TableRow currentTableRow = getTableRow(row);
			
			//place Buttons in currentTableRow
			for(int column=0;column < 3;column++){
				//inflate guess_button.xml to create new Button
				Button newGuessButton=
						(Button)inflater.inflate(R.layout.guess_button, null); 
				
				//get country name and set it as new GuessButton's text
				String fileName = fileNameList.get((row *3) + column);
				newGuessButton.setText(getCountryName(fileName));
				
				//register answerButtonListener to respond to button clicks
				newGuessButton.setOnClickListener(guessButtonListener);
				currentTableRow.addView(newGuessButton);
			}//end for
		}//end for
		
		//randomly replace one Button with the correct answer
		int row = random.nextInt(guessRows);//pick random row
		int column = random.nextInt(3);//pick random column
		TableRow randomTableRow = getTableRow(row);
		String countryName = getCountryName(correctAnswer);
		((Button)randomTableRow.getChildAt(column)).setText(countryName);
		
		linktocountry = "http://en.wikipedia.org/wiki/"+countryName; //creating a wiki link for the answer
	}//end method loadNextFlag

	//parses the country flag file name and returns name only
	private String getCountryName(String name) {
		return name.substring(name.indexOf('-') + 1).replace('-', ' ');
	}//end method getCountryName

	private TableRow getTableRow(int row) {
		return (TableRow) buttonTableLayout.getChildAt(row);
	}//end method getTableRow
	
	private void submitGuess(Button guessButton){
		String guess = guessButton.getText().toString();
		String answer = getCountryName(correctAnswer);
		++totalGuesses; //increment the number of guesses the user has made
		++guessesperflag; //increment number of guesses per flag
		
		//if the guess is correct
		if(guess.equals(answer)){
			++correctAnswers; //increment the # of correct answers
			
			capital = getCountryCapital(answer);
			
			if(firstguess==true){ //detect a first guess
				firstguesses++;
			}
			
			finalscore += (50/guessesperflag); //add on existing score
		
			disableButtons();//disable all answer Buttons
			createBonusQuestion(); //creates the bonus question dialog
		}//end if
		else{ //guess was incorrect
			firstguess=false; //not first guess
			// play the animation
			flagImageView.startAnimation(shakeAnimation);
			
			guessButton.setEnabled(false); //disable the incorrect answer
		}//end else
	}//end method submitGuess

	private void updateDialog() {
		AlertDialog.Builder nextBuilder = new AlertDialog.Builder(this);
		
		View linkxml = getLayoutInflater().inflate(R.layout.link, null);
		nextBuilder.setView(linkxml);
		nextBuilder.setCancelable(false);
		TextView link = (TextView) linkxml.findViewById(R.id.wikilinkTextView); 
		link.setText(getResources().getString(R.string.LearnMore) + " " + linktocountry);
		link.setTextColor(getResources().getColor(R.color.text_color));
		
		String header="";
		if(correctAnswers==10){
			header= getResources().getString(R.string.ok);
		}else{
			header= getResources().getString(R.string.nextFlag);
		}
		
		nextBuilder.setPositiveButton(header, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(correctAnswers < 10){
					loadNextFlag();
				}else{
					EndGameDialog(); //correctAnswers = 10 , end of game
				}
			}
		});
		AlertDialog temp = nextBuilder.create();
		temp.show();
	}

	protected void EndGameDialog() {
			numberofwins++; //increment # of wins
			
			setHighScore();
			
			//if the user has correctly identified 10 flags
			//create a new AlertDialog Builder
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			
			
			//set the AlertDialog's message to display game results
			builder.setMessage(String.format("%d %s, %.02f%% %s, %d %s, %s %.02f",totalGuesses, getResources().getString(R.string.guesses),
			(1000/ (double) correctAnswers),getResources().getString(R.string.correct),
			firstguesses,getResources().getString(R.string.first_guess), getResources().getString(R.string.final_score), finalscore));
			
			builder.setCancelable(false);
			
			//add "Reset Quiz" Button
			builder.setPositiveButton(R.string.reset_quiz, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					resetQuiz();
				}//end method onCLick
			}//end anonymous inner class
		);//end call to setPositiveButton
			
			//only show button when there is a new highscore
			String newhs = settings.getString("new", "");
			if(newhs.length()>0){
				builder.setNegativeButton("New Highscore!", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent highIntent = new Intent(getApplicationContext(), TopFive.class);
						startActivity(highIntent);
					}
				});
			}
			
			//create AlertDialog from the Builder
			AlertDialog resetDialog = builder.create();
			resetDialog.show(); //display the Dialog
			
			
	}

	private void setHighScore() {//setting and checking local and online scores.
		if(finalscore>0){
		
			DateFormat dateForm = new SimpleDateFormat("dd MMMM yyyy");
			String dateOutput = dateForm.format(new Date());
			
			SharedPreferences.Editor scoreEdit = settings.edit();
			
			String scores = settings.getString("highScores", "");
		
			checkLeaderboards(scoreEdit); //check boards
			checkAchievements(scoreEdit); //check achievements
			
			if(scores.length()>0){
				//we have existing scores
				Map<Double,String> scoresList = new HashMap<Double, String>();
				String[] new_scores = scores.split("\\|");
				for(String x : new_scores){
				    String[] parts = x.split(" - ");
				    scoresList.put(Double.parseDouble(parts[1]), parts[0]); //parts[0]=date
				}
				
				//check if this score is the highest in the top 5
				String[] firstkey = new_scores[0].split(" - ");
				Double highest_score = Double.parseDouble(firstkey[1]); //get the key and parse
				
				if(finalscore > highest_score){
					scoreEdit.putString("new", "You have a new highscore!"); //if yes, use this to show it at the end of game
				}else{
					scoreEdit.putString("new","");
				}
				
				
				scoresList.put(finalscore,dateOutput); //add current game score
				
				Object[] keys = scoresList.keySet().toArray(); //create an array from the key set values = finalscore variables
				Arrays.sort(keys); //sort them (Later, display them from last to first element)
				
				StringBuilder scoreBuild = new StringBuilder(""); //from greatest to 5th
				for(int s=keys.length-1; s >=0 ; s--){
				    if(s<=keys.length-6) break;//only want five
				    if(s<keys.length-1) scoreBuild.append("|");//pipe separate the score strings
				    scoreBuild.append(scoresList.get(keys[s]) + " - " + keys[s]);
				}
				//write to prefs
				scoreEdit.putString("highScores", scoreBuild.toString());
				scoreEdit.commit();
				
			}else{
				//no existing scores
				scoreEdit.putString("highScores", ""+dateOutput+" - "+finalscore);
				scoreEdit.commit();
				
			}
			
		}
		
		
	}

	private void checkAchievements(Editor scoreEdit) {
		scoreEdit.putInt("wins", numberofwins); //save number of wins
		scoreEdit.commit();
		if(isSignedIn()){ //check if signed in
			if(numberofwins == 5){
				getGamesClient().unlockAchievement(getResources().getString(R.string.ach_id1)); //win 5 games
			}
			if(numberofwins == 10){
				getGamesClient().unlockAchievement(getResources().getString(R.string.ach_id2)); //win 10 games
			}
			
			if(numberofcorrectcapitals == 2){
				getGamesClient().unlockAchievement(getResources().getString(R.string.ach_id3)); //guess 2 capitals
			}else if(numberofcorrectcapitals == 5){
				getGamesClient().unlockAchievement(getResources().getString(R.string.ach_id4)); //guess 5 capitals
			}else if(numberofcorrectcapitals == 10){
				getGamesClient().unlockAchievement(getResources().getString(R.string.ach_id5)); //guess 10 capitals
			}
		}else{
			scoreEdit.putInt("wins", numberofwins); //save number of wins
			scoreEdit.putInt("caps", numberofcorrectcapitals); //save number of correct capitals
			scoreEdit.commit();
		}
	}

	private void checkLeaderboards(Editor scoreEdit) {
		if(isSignedIn()){
		getGamesClient().submitScore(getResources().getString(R.string.lead_id), (long) finalscore);//submit to google leaderboards
		}else{
			scoreEdit.putLong("lead",(long)finalscore); //else, save it for when they sign in
			scoreEdit.commit();
		}
	}

	private String getCountryCapital(String answer) {
		String capital = "";
		answer = answer.replaceAll("_", " ");
		Pattern pattern = Pattern.compile("^"+answer);
		AssetManager assets = getAssets();
		try {
			InputStream is = assets.open("capitals");
			Scanner scan = new Scanner(is); //scan through the file
			
			while(scan.hasNext()){
				String line = scan.nextLine();
				String temp = line;
				Matcher matcher = pattern.matcher(temp);
				while(matcher.find()){
					capital = temp.substring(temp.indexOf("-"),temp.length());
				}
			}
			
		} catch (IOException e) {
			Log.e(TAG,"Error loading " + "capitals.txt", e);
		}
		return capital;
	}

	private void createBonusQuestion() {
		AlertDialog.Builder capitalbuilder = new AlertDialog.Builder(this);
		capitalbuilder.setTitle(R.string.bonusquestion);
		capitalbuilder.setCancelable(false);
		final View bonusLayout = getLayoutInflater().inflate(R.layout.bonus_layout, null); // why final?
		capitalbuilder.setView(bonusLayout);
		capitalbuilder.setPositiveButton(R.string.capitalButton, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				EditText answerview = (EditText)bonusLayout.findViewById(R.id.capitaltext);
				capitalGuess = answerview.getText().toString();
				
				capitalGuess = capitalGuess.toLowerCase().replaceAll("[^A-Za-z0-9]", "");//making answer and possible answer identical
				String temp = capital.toLowerCase().replaceAll("[^A-Za-z0-9]", "");
				
				if(capitalGuess.equals(temp)){
					finalscore+=10;
					numberofcorrectcapitals++; //increment number of correct capitals in a single game
					Toast.makeText(getApplicationContext(), "Correct! +10" , Toast.LENGTH_SHORT).show();
					SharedPreferences.Editor scoreEdit = settings.edit();
					checkAchievements(scoreEdit); //check achievements
				}else{
					Toast.makeText(getApplicationContext(), "Incorrect! " + capital , Toast.LENGTH_SHORT).show();
				}
			}
			
		});
		
		//create AlertDialog from the Builder
		AlertDialog capitalDialog = capitalbuilder.create();
		capitalDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				updateDialog();
			}
		});
		
		capitalDialog.show();
		
	}

	//utility method that disables all answer buttons
	private void disableButtons() {
		for(int row = 0; row < buttonTableLayout.getChildCount();++row){
			TableRow tableRow = (TableRow) buttonTableLayout.getChildAt(row);
			for(int i=0; i < tableRow.getChildCount();++i){
				tableRow.getChildAt(i).setEnabled(false);
			}//end inner for
		}// end outer for
	}//end method disableButtons
	
	private final int CHOICES_MENU_ID = Menu.FIRST;
	private final int REGIONS_MENU_ID = Menu.FIRST + 1;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		menu.add(Menu.NONE,CHOICES_MENU_ID,Menu.NONE,R.string.choices);
		menu.add(Menu.NONE,REGIONS_MENU_ID,Menu.NONE,R.string.regions);
		
		return true; //display menu
	}//end method onCreateOptionsMenu
	
//called when the user selects an option form the menu
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		//switch the menu id of the user-selected option
		switch(item.getItemId()){
		case CHOICES_MENU_ID:
			//create a list of the possible numbers of answer choices
			final String[] possibleChoices=
			getResources().getStringArray(R.array.guessesList);
			
			//create a new AlertDialog Builder and set its title
			AlertDialog.Builder choicesBuilder = new AlertDialog.Builder(this);
			choicesBuilder.setTitle(R.string.choices);
			
			//add possibleChoices items to the Dialog and set the
			//behavior when one of the items is clicked
			choicesBuilder.setItems(R.array.guessesList, 
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int item) {
							//update guessRows to match the user's choice
							guessRows = Integer.parseInt(
									possibleChoices[item].toString())/3;
							resetQuiz(); //reset the quiz
						}//end onClick
					}); //end setItems
			
			//create an AlertDialog from the Builder
			AlertDialog choicesDialog = choicesBuilder.create();
			choicesDialog.show(); //show the dialog
			return true;
			
			case REGIONS_MENU_ID:
				//get array of world regions
				final String[] regionNames = 
				regionsMap.keySet().toArray(new String[regionsMap.size()]);
				
				//boolean array representing whether each region is enabled
				boolean[] regionsEnabled = new boolean[regionsMap.size()];
				for(int i=0;i<regionsEnabled.length;++i){
					regionsEnabled[i] = regionsMap.get(regionNames[i]);
				}
				
				//create an AlertDialog Builder and set the dialog's title
				AlertDialog.Builder regionsBuilder = 
						new AlertDialog.Builder(this);
				regionsBuilder.setTitle(R.string.regions);
				
				//replace _ with space in region names for display purposes
				String[] displayNames = new String[regionNames.length];
				for(int i=0; i<regionNames.length;++i){
					displayNames[i] = regionNames[i].replace('_',' ');
				}
				
				//add displayNames to the Dialog and set the behavior
				//when one of the items is clicked
				regionsBuilder.setMultiChoiceItems(displayNames, regionsEnabled, new DialogInterface.OnMultiChoiceClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						//include or exclude the clicked region
						//depending on whether or not it's checked
						regionsMap.put(regionNames[which].toString(), isChecked);
					}//end method onClick
				});//end call to setMultiChoicesItems
				
				//resets quiz when user presses the Reset Quiz button
				regionsBuilder.setPositiveButton(R.string.reset_quiz, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						resetQuiz();//reset the quiz
					}//end onClick
				});//set PositiveButton
				//create a dialog form the Builder
				AlertDialog regionsDialog = regionsBuilder.create();
				regionsDialog.show(); //display the Dialog
				return true;
		}//end switch		
		
		return super.onOptionsItemSelected(item);
	}// end method onOptionsItemSelected
	
	//called when a guess Button is touched
	private OnClickListener guessButtonListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			submitGuess((Button) v); //pass selected Button to submitGuess
		}//end method onClick
	};//end guessButtonListener



	@Override
	public void onSignInFailed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSignInSucceeded() {
		// TODO Auto-generated method stub
		
	}


}
