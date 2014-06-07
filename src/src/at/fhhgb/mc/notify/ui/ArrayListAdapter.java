package at.fhhgb.mc.notify.ui;

import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;

public class ArrayListAdapter extends ArrayAdapter<String> {

	public ArrayListAdapter(Context context, int resource, int textViewResource, List<String> objects) {
		super(context, resource, textViewResource, objects);
		
	}
	
	

}
