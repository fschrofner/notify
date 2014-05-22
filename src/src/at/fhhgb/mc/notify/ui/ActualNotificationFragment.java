package at.fhhgb.mc.notify.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import at.fhhgb.mc.notify.R;

public class ActualNotificationFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater _inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = _inflater.inflate(R.layout.fragment_actual_notification, null);
		
		return view;
	}

	
}
