package com.finalhack.javainfo;

import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SubjectActivity extends ListActivity {

	public static final String EMPTY_STRING = "";
	
	private Button searchButton;
	private EditText searchText;
	private TextView resultCountText;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.subject_chooser);
	    
	    searchButton = (Button) findViewById(R.id.search_button);
	    searchText = (EditText) findViewById(R.id.search_terms);
	    resultCountText = (TextView) findViewById(R.id.result_count);
	}
	
	@Override
	public void onResume()
	{
	    super.onResume();
	    
	    QuestionUtil.CURRENT_QUESTION_ID = null;
	    
	    //Create our subject list
	    List<String> questionList = QuestionUtil.getQuestionList();
	    List<String> subjectList = QuestionUtil.getSubjectList(questionList);
	    
        setListAdapter(new SubjectAdapter(this, subjectList));
        getListView().setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String subject = (String) SubjectActivity.this.getListAdapter().getItem(position);
                Intent intent = new Intent(SubjectActivity.this, QuestionAnswerActivity.class);
                intent.putExtra(QuestionAnswerActivity.EXTRA_KEY_SUBJECT, subject);
                startActivity(intent);
            }
        });
        
        searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                Intent intent = new Intent(SubjectActivity.this, QuestionAnswerActivity.class);
                intent.putExtra(QuestionAnswerActivity.EXTRA_KEY_SEARCH, searchText.getText().toString());
                startActivity(intent);			
            }
		});
        
        searchText.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				resultCountText.setText(""+getSearchResultsCount(arg0.toString()) + " " + getResources().getString(R.string.result_suffix));
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
			}
        	
        });
	}
	
	// Get the number of QAs matching the serach string
	private int getSearchResultsCount(String searchText) {
		// Get all the questions for later filtering
		List<String> allQuestions = QuestionUtil.getQuestionList();
		
		List<Question> questionList = QuestionUtil.getSearchedQuestions(allQuestions, searchText.toLowerCase(), this);
		
		return questionList.size();
	}
	
	public class SubjectAdapter extends ArrayAdapter<String>
	{
	    public SubjectAdapter(Context context, List<String> subjectList)
	    {
	        super(context, R.layout.subject_row, R.id.subject_text, subjectList);
	    }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            //Try to reuse the row...
            View row = convertView;
            if (row == null) row = getLayoutInflater().inflate(R.layout.subject_row, parent, false);
          
            //Set the subject
            TextView subjectText = (TextView)row.findViewById(R.id.subject_text);
            String subject = getItem(position);
            subjectText.setText(subject);
            
            return row;
        }
	    
	}
}