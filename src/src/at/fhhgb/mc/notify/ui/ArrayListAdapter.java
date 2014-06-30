package at.fhhgb.mc.notify.ui;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import at.fhhgb.mc.notify.R;

public class ArrayListAdapter extends ArrayAdapter<String> {

	private static final String TAG = "ArrayListAdapter";
	private List<String> mMessageList;
	private boolean[] mSelectedItemsIds;

	public ArrayListAdapter(Context _context, int _resource,
			int textViewResource, List<String> _titleList,
			List<String> _messageList) {
		super(_context, _resource, textViewResource, _titleList);

		mSelectedItemsIds = new boolean[_titleList.size()];
		mMessageList = _messageList;
	}

	@Override
	public View getView(int _position, View _convertView, ViewGroup _parent) {

		if (_convertView == null) {
			Context c = getContext();
			LayoutInflater inflater = (LayoutInflater) c
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			_convertView = inflater.inflate(R.layout.fragment_list_item, null);
		}

		Log.i(TAG, "size: " + mMessageList.size());
		for (int i = 0; i < mMessageList.size(); i++) {
			Log.i(TAG, "message: " + mMessageList.get(i));
		}
		
		if (_convertView != null) {
			TextView message = (TextView) _convertView
					.findViewById(R.id.item_message);
			if(mMessageList != null && mMessageList.get(_position) != null){
				if (mMessageList.get(_position).equals("")) {
					message.setText(R.string.no_message);
					message.setTextColor(Color.GRAY);
					message.setTypeface(message.getTypeface(), Typeface.ITALIC);
				} else {
					message.setTextColor(Color.DKGRAY);
					message.setText(mMessageList.get(_position));
				}
			}

		}

		_convertView
				.setBackgroundColor(mSelectedItemsIds[_position] ? 0x9934B5E4
						: Color.TRANSPARENT);

		return super.getView(_position, _convertView, _parent);
	}

	 public void setNewSelection(int position) {
	 mSelectedItemsIds[position] = true;
	 Log.i(TAG, position + " true");
	 notifyDataSetChanged();
	 }
	
	 public void removeSelection(int position) {
	 mSelectedItemsIds[position] = false;
	 notifyDataSetChanged();
	 }

//	public void selectView(int position, boolean value) {
//		if (value)
//			mSelectedItemsIds.put(position, value);
//		else
//			mSelectedItemsIds.delete(position);
//
//		notifyDataSetChanged();
//	}

//	public void toggleSelection(int position) {
//		selectView(position, !mSelectedItemsIds.get(position));
//	}

	public void noSelection() {
		for (int i = 0; i < mSelectedItemsIds.length; i++) {
			mSelectedItemsIds[i] = false;
		}
		notifyDataSetChanged();
	}
//
//	public int getSelectedCount() {
//		return mSelectedItemsIds.size();
//	}

	public boolean[] getSelectedIds() {
		return mSelectedItemsIds;
	}

}
