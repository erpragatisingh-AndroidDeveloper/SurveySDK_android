package com.survey.android.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.survey.android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("unused")
public class Gallery extends Activity {
	public ImageAdapter imageAdapter;
	private final static int TAKE_IMAGE = 1;
	private final static int UPLOAD_IMAGES = 2;
	private final static int VIEW_IMAGE = 3;
	private Uri imageUri;
	private MediaScannerConnection mScanner;
	public GridView imagegrid;

	private long lastId;
	private List<ImageItem> images;
	private ImageItem currentSelectedII;
	private ViewHolder currentSelectedH;

	private Runnable alertRunnable;
	private AlertDialog alert;
	private Handler handler;
	private ProgressDialog pd;
	private TextView txtTitle;
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);

		// *******************************************************************************
		handler = new Handler();
		alertRunnable = null; // explicit null
		images = new ArrayList<ImageItem>();
		// *******************************************************************************

		txtTitle=(TextView)findViewById(R.id.txtTitle);
		txtTitle.setText(R.string.gallery);
		
		final Button btnBack = (Button) findViewById(R.id.btnBack);
		final Button btnSelect = (Button) findViewById(R.id.selectBtn);

		(new ImageLoaderTask()).execute();

		btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// finish();
				returnFromGallery();
			}
		});

		btnSelect.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (currentSelectedII == null || currentSelectedII.selection==false) {
					Toast toast = Toast
							.makeText(Gallery.this,
									R.string.please_select_one_photo,
									Toast.LENGTH_LONG);
					toast.show();
				} else {
					returnFromGallery();
				}
			}
		});
	}

	public void updateUI() {
		imageAdapter.checkForNewImages();
	}

	private void returnFromGallery() {
		Intent resultData = new Intent();
		if (currentSelectedII != null) {
			resultData.putExtra("image_path", currentSelectedII.path);
			setResult(Activity.RESULT_OK, resultData);
		} else {
			setResult(RESULT_CANCELED, resultData);
		}
		finish();
	}

	public class ImageAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public ImageAdapter() {
			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@SuppressWarnings("deprecation")
		public void initialize() {
			images.clear();
			final String[] columns = { MediaStore.Images.Thumbnails._ID,
					MediaStore.Images.Media.DATA };
			final String orderBy = MediaStore.Images.Media._ID;
			Cursor imagecursor = managedQuery(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
					null, null, orderBy);
			if (imagecursor != null) {
				int image_column_index = imagecursor
						.getColumnIndex(MediaStore.Images.Media._ID);

				int count = imagecursor.getCount();
				for (int i = 0; i < count-1; i++) {
					imagecursor.moveToPosition(i);
					int id = imagecursor.getInt(image_column_index);
					ImageItem imageItem = new ImageItem();
					imageItem.id = id;
					imageItem.path = imagecursor
							.getString(imagecursor
									.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
					lastId = id;
					imageItem.img = MediaStore.Images.Thumbnails.getThumbnail(
							getApplicationContext().getContentResolver(), id,
							MediaStore.Images.Thumbnails.MICRO_KIND, null);
					images.add(imageItem);
				}
				imagecursor.close();
			}
			notifyDataSetChanged();
		}

		@SuppressWarnings("deprecation")
		public void checkForNewImages() {
			// Here we'll only check for newer images
			final String[] columns = { MediaStore.Images.Thumbnails._ID,
					MediaStore.Images.Media.DATA };
			final String orderBy = MediaStore.Images.Media._ID;
			Cursor imagecursor = managedQuery(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
					MediaStore.Images.Media._ID + " > " + lastId, null, orderBy);
			int image_column_index = imagecursor
					.getColumnIndex(MediaStore.Images.Media._ID);
			int count = imagecursor.getCount();
			for (int i = 0; i < count; i++) {
				imagecursor.moveToPosition(i);
				int id = imagecursor.getInt(image_column_index);
				ImageItem imageItem = new ImageItem();
				imageItem.id = id;
				imageItem.path = imagecursor.getString(imagecursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
				lastId = id;
				imageItem.img = MediaStore.Images.Thumbnails.getThumbnail(
						getApplicationContext().getContentResolver(), id,
						MediaStore.Images.Thumbnails.MICRO_KIND, null);
				imageItem.selection = true; // newly added item will be selected
											// by default
				images.add(imageItem);
			}
			imagecursor.close();
			notifyDataSetChanged();
		}

		public int getCount() {
			return images.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView,
				ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.gallery_item, null);
				holder.imageview = (ImageView) convertView
						.findViewById(R.id.ivThumb);
				holder.checkbox = (CheckBox) convertView
						.findViewById(R.id.cbThumb);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final ImageItem item = images.get(position);
			holder.checkbox.setId(position);
			holder.imageview.setId(position);
			holder.checkbox.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					CheckBox cb = (CheckBox) v;
					int id = cb.getId();
					if (currentSelectedH != null
							&& currentSelectedH.checkbox.getId() != position) {
						currentSelectedH.checkbox.setChecked(false);
						holder.checkbox.setChecked(true);
						images.get(holder.checkbox.getId()).selection=true;
						
					} else {
						if (images.get(id).selection) {
							cb.setChecked(false);
							images.get(id).selection = false;
						} else {
							cb.setChecked(true);
							images.get(id).selection = true;
						}
					}

					currentSelectedH = holder;
					currentSelectedII = item;

					// if (images.get(id).selection) {
					// cb.setChecked(false);
					// images.get(id).selection = false;
					// } else {
					// cb.setChecked(true);
					// images.get(id).selection = true;
					// }
				}
			});
			holder.imageview.setOnClickListener(new OnClickListener() {

				@SuppressWarnings("deprecation")
				public void onClick(View v) {
					int id = v.getId();
					ImageItem item = images.get(id);
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_VIEW);
					final String[] columns = { MediaStore.Images.Media.DATA };
					Cursor imagecursor = managedQuery(
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							columns, MediaStore.Images.Media._ID + " = "
									+ item.id, null,
							MediaStore.Images.Media._ID);
					if (imagecursor != null && imagecursor.getCount() > 0) {
						imagecursor.moveToPosition(0);
						String path = imagecursor.getString(imagecursor
								.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
						File file = new File(path);
						imagecursor.close();
						intent.setDataAndType(Uri.fromFile(file), "image/*");
						startActivityForResult(intent, VIEW_IMAGE);
					}
				}
			});
			holder.imageview.setImageBitmap(item.img);
			holder.checkbox.setChecked(item.selection);
			return convertView;
		}
	}

	private boolean isAnySelected(List<ImageItem> images) {
		if (images != null && images.size() != 0) {
			for (ImageItem img : images) {
				if (img.selection) {
					return true;
				}
			}
		}
		return false;
	}

	class ViewHolder {
		ImageView imageview;
		CheckBox checkbox;
	}

	class ImageItem {
		boolean selection;
		int id;
		String path;
		Bitmap img;
	}

	/**
	 * Loads local images and call adapter initialization
	 * 
	 * @author dominum
	 * 
	 */
	private class ImageLoaderTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			pd = ProgressDialog.show(Gallery.this,
					getResources().getString(R.string.loading), getResources()
							.getString(R.string.please_wait), true, false);
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				imageAdapter = new ImageAdapter();
				imageAdapter.initialize();
				imagegrid = (GridView) findViewById(R.id.PhoneImageGrid);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						imagegrid.setAdapter(imageAdapter);
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (pd != null && pd.isShowing()) {
				pd.dismiss();
			}
			super.onPostExecute(result);
		}
	}
}