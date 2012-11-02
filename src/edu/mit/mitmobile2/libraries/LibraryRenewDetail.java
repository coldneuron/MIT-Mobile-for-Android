package edu.mit.mitmobile2.libraries;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import edu.mit.mitmobile2.Module;
import edu.mit.mitmobile2.ModuleActivity;
import edu.mit.mitmobile2.R;
import edu.mit.mitmobile2.objs.LoanListItem;

public class LibraryRenewDetail extends ModuleActivity{
	public static final String TAG = "LibraryRenewDetail";

    Context mContext;
	private TextView renewTitleTV;
    private TextView renewAuthorTV;
	private TextView renewOverdueTV;
	private TextView renewMessageTV;
	private Button renewDoneButton;
	
    private int index;
    private String errorMsg = "";
    @SuppressWarnings("unused")
	private String successMsg = "";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.library_renew_detail);
        Bundle extras = getIntent().getExtras();
        index = extras.getInt("index");
        successMsg = extras.getString("successMsg");
        errorMsg = extras.getString("errorMsg");
        
        LoanListItem item = LibraryYourAccount.getLoanData().getLoans().get(index);
        
        renewTitleTV = (TextView)findViewById(R.id.renewTitleTV);
        renewTitleTV.setText(item.getTitle());

        renewAuthorTV = (TextView)findViewById(R.id.renewAuthorTV);
        renewAuthorTV.setText(item.getYear() + "; " + item.getAuthor());

        renewOverdueTV = (TextView)findViewById(R.id.renewOverdueTV);
        renewOverdueTV.setText(Html.fromHtml(item.getDueText()));
        
        renewMessageTV = (TextView)findViewById(R.id.renewMessageTV);
        if (errorMsg.length() > 0) {
        	renewMessageTV.setText("1 could not be renewed.");
        }
        else {
        	renewMessageTV.setText("1 renewed successfully!");
        	renewMessageTV.setTextColor(R.color.result_text);
        }
        
        renewDoneButton  = (Button)findViewById(R.id.renewDoneButton);
		renewDoneButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				finish();
			}
		});

        //mLoadingView = (FullScreenLoader) findViewById(R.id.librarySearchLoading);

        
    }
/*
    private void doSearch(String barcode) {

        mLoadingView.setVisibility(View.VISIBLE);
        mLoadingView.showLoading();
        LibraryModel.renewBook(this, uiHandler,barcode);
    }

    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	Log.d(TAG,"handleMessage");
        	Log.d(TAG,"arg1 = " + msg.arg1);
            
            mLoadingView.setVisibility(View.GONE);

            RenewBookResponse response = (RenewBookResponse)msg.obj;
            Log.d(TAG,"error = " + response.getRenewResponse().get(0).getErrorMsg());
            if (msg.arg1 == MobileWebApi.SUCCESS) {
            	Log.d(TAG,"MobileWebApi success");
            } else if (msg.arg1 == MobileWebApi.ERROR) {
                mLoadingView.showError();
            } else if (msg.arg1 == MobileWebApi.CANCELLED) {
                mLoadingView.showError();
            }
        }
    };
 */
    
	@Override
	protected Module getModule() {
		return new LibrariesModule();
	}

	@Override
	public boolean isModuleHomeActivity() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void prepareActivityOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		
	}
}
