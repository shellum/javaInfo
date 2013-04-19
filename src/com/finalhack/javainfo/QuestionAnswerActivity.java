package com.finalhack.javainfo;

import java.lang.reflect.Method;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.finalhack.javainfo.ActionBar.TouchPoint;

public class QuestionAnswerActivity extends Activity {

    public static final String STARRED_QUESTION_DELIMITER = ",";
    private static final String EMPTY_STRING = "";
    private static final String METHOD_STAR = "flipStarSetting";
    public static final String EXTRA_KEY_SUBJECT = "subject";

    //Shared preferences info
    public static final String SHARED_PREFS_LOCATION = "sharedPrefs";
    public static final String SHARED_PREFS_STARRED = "starred";
    
    //Save out subject specific question list
    private List<Question> questionList;
    
    private Question currentQuestion;
    
    //Our UI views
    private TextView questionView;
    private TextView answerView;
    private ActionBar actionBar;
    
    //Saved action bar touch points
    TouchPoint starQuestionTouchPoint;
    
    //Setup the initial screen
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.question_answer);
        
        questionView = (TextView)findViewById(R.id.question);
        answerView = (TextView)findViewById(R.id.answer);
        actionBar = (ActionBar)findViewById(R.id.action_bar);
        
        Method starMethod = null;
        try { starMethod = getClass().getMethod(METHOD_STAR, (Class[])(new Class[] {}));} catch(Exception e) { if (BuildConfig.DEBUG) Log.d("", e.getMessage(), e); }
        starQuestionTouchPoint = actionBar.addTouchPoint(starMethod, this, R.drawable.ic_action_star);
        
        

    }
   
    //Check to see if a question is starred
    private boolean isQuestionStarred(String questionId)
    {
        //By default, tag it as not being starred
        boolean starred = false;
        
        //Find which questions are starred
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_LOCATION, MODE_PRIVATE);
        String starredQuestionIdList = sharedPreferences.getString(SHARED_PREFS_STARRED, EMPTY_STRING);
        String[] starredQuestionIds = starredQuestionIdList.split(STARRED_QUESTION_DELIMITER);
        
        //Save the current question id for comparison
        String currentQuestionId = currentQuestion.index;
        
        //Determine if the current question is in the list of starred questions
        for (String starredQuestionId : starredQuestionIds) if (starredQuestionId.equals(currentQuestionId)) starred = true;

        return starred;
    }
    
    //Take care of updating the star in the action bar according to what star state has already been saved
    public void updateStarInActionBar()
    {
        //Choose what icon should be shown
        int starredIcon = R.drawable.ic_action_star;
        if (isQuestionStarred(currentQuestion.index)) starredIcon = R.drawable.ic_action_star_yellow;
        
        //Update the action bar
        actionBar.removeTouchPoint(starQuestionTouchPoint);
        Method starMethod = null;
        try { starMethod = getClass().getMethod(METHOD_STAR, (Class[])(new Class[] {}));} catch(Exception e) { if (BuildConfig.DEBUG) Log.d("", e.getMessage(), e); }
        starQuestionTouchPoint = actionBar.addTouchPoint(starMethod, this, starredIcon);
        actionBar.invalidate();

    }
    
    //Handle the starring and unstarring of questions
    public void flipStarSetting()
    {
        //Find out if the current question is starred
        String currentQuestionId = currentQuestion.index;
        boolean questionStarred = isQuestionStarred(currentQuestionId);
        
        //Find which questions are starred
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_LOCATION, MODE_PRIVATE);
        String starredQuestionIdList = sharedPreferences.getString(SHARED_PREFS_STARRED, EMPTY_STRING);
        String[] starredQuestionIds = starredQuestionIdList.split(STARRED_QUESTION_DELIMITER);
        
        //If the question is not currently starred, star it and add it to saved starred questions
        if (!questionStarred)
        {
            if (starredQuestionIdList.length() == 0) starredQuestionIdList = currentQuestionId;
            else starredQuestionIdList += STARRED_QUESTION_DELIMITER + currentQuestionId;
        }
        //If the question is currently starred, unstar it and remove it from saved starred questions
        else
        {
            //Our new string of starred questions
            String newStarredQuestionIds = "";
            
            //Remember if we have already added one (to determin if a leading comma is needed)
            boolean firstAdded = false; 
            
            //For all currently starred questions... 
            for (String questionId : starredQuestionIds)
            {
                //Don't do anything if we see the current question that is being unstarred
                if (questionId.equals(currentQuestionId)) continue;
                
                //If we need a comma...
                if (firstAdded)
                {
                    newStarredQuestionIds += STARRED_QUESTION_DELIMITER + questionId;
                }
                //If we don't need a comma...
                else
                {
                    firstAdded = true;
                    newStarredQuestionIds += questionId;
                }
            }
            
            starredQuestionIdList = newStarredQuestionIds;
        }
        
        Editor editor = getSharedPreferences(SHARED_PREFS_LOCATION, MODE_PRIVATE).edit();
        editor.putString(SHARED_PREFS_STARRED, starredQuestionIdList);
        editor.commit();
        
        updateStarInActionBar();
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        
        //Find out what subject matter we should be displaying
        String subject = getIntent().getExtras().getString(EXTRA_KEY_SUBJECT);
        
        //Lookup all questions for the subject
        List<String> allQuestions = QuestionUtil.getQuestionList();
        questionList = QuestionUtil.getQuestionList(allQuestions, subject, this);
        QuestionUtil.totalQuestions = questionList.size();
        
        populateTextView(questionView, getResources().getString(R.string.question_pre_text));
        populateTextView(answerView, getResources().getString(R.string.answer_pre_text));
        //Show the next (first) question
        //showNextQuestion(null);
    }
    
    public void showNextQuestion(View view)
    {
        if (questionList.isEmpty())
        {
            populateTextView(questionView, getResources().getString(R.string.question_post_text));
            populateTextView(answerView, getResources().getString(R.string.answer_pre_text));
            currentQuestion = null;
            QuestionUtil.CURRENT_QUESTION_ID = null;
        }
        else
        {
            //Pick a random question
            int randomIndex = ((int)(Math.random() * 1000)) % questionList.size();
            currentQuestion = questionList.get(randomIndex);
            //Remove the question so it cannot be picked again
            questionList.remove(currentQuestion);
        
            //Show the question
            populateTextView(questionView, currentQuestion.question);
            populateTextView(answerView, getResources().getString(R.string.answer_pre_text));
            updateStarInActionBar();
            
            QuestionUtil.CURRENT_QUESTION_ID = currentQuestion.index;
            
            actionBar.setTitle("JavaInfo " + (QuestionUtil.totalQuestions - questionList.size()) + "/" + QuestionUtil.totalQuestions);
        }

    }
    
    public void showAnswer(View view)
    {
        //Show the answer
        if (currentQuestion != null) populateTextView(answerView, currentQuestion.answer);
    }
    
    private void populateTextView(TextView textView, String text)
    {
        final String PERCENT_FROM = "%";
        final String PERCENT_TO = "%25";
        textView.setText(Html.fromHtml(text.replace(PERCENT_FROM, PERCENT_TO)));
    }
}
