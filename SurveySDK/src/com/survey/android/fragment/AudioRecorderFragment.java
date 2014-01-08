package com.survey.android.fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.survey.android.R;
import com.survey.android.util.ConstantData;

public class AudioRecorderFragment extends Fragment implements OnInfoListener, OnCompletionListener {
	private static final String TAG = AudioRecorderFragment.class.getSimpleName();
	private String mFileName = null;
	private MediaRecorder mRecorder = null;
	private MediaPlayer mPlayer = null;
	// Whether we have any recorded data.
	private boolean mHasRecorded = false;
	private boolean mIsRecording = false;
	private boolean mIsPlaying = false;
	private int mDurationLimitSeconds;

	private ImageButton getRecordButton() {
		return (ImageButton) getView().findViewById(R.id.button_record);
	}

	private ImageButton getPlayButton() {
		return (ImageButton) getView().findViewById(R.id.button_play);
	}

	private ImageButton getStopButton() {
		return (ImageButton) getView().findViewById(R.id.button_stop);
	}

	private void onStopClicked() {
		if (mIsRecording) {
			stopRecording();
		} else {
			stopPlaying();
		}
	}

	@SuppressWarnings("resource")
	private void startPlaying() {
		if (mIsRecording) {
			stopRecording();
		}

		mPlayer = new MediaPlayer();

		try {
			// Use an FD to get around permission issues here
			File audioFile = new File(mFileName);
			mPlayer.setDataSource(new FileInputStream(audioFile).getFD());
			mPlayer.setOnCompletionListener(this);
			mPlayer.prepare();
			mPlayer.start();
			getRecordButton().setEnabled(false);
			getPlayButton().setEnabled(false);
			getStopButton().setVisibility(View.VISIBLE);
			getStopButton().setEnabled(true);

			Chronometer chronometer = (Chronometer) getView().findViewById(R.id.current_position);
			chronometer.setBase(SystemClock.elapsedRealtime());
			chronometer.start();
			mIsPlaying = true;
		} catch (IOException e) {
			Log.e(TAG, "prepare() failed", e);
			Toast.makeText(getActivity(), R.string.audio_recording_error, Toast.LENGTH_LONG).show();
		}

	}

	private void stopPlaying() {
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}

		getRecordButton().setEnabled(true);
		getStopButton().setEnabled(false);

		if (mHasRecorded) {
			getPlayButton().setVisibility(View.VISIBLE);
			getPlayButton().setEnabled(true);
		}

		Chronometer chronometer = (Chronometer) getView().findViewById(R.id.current_position);
		chronometer.stop();
		mIsPlaying = false;
	}

	@SuppressWarnings("deprecation")
	private void startRecording() {
		if (mIsPlaying) {
			stopPlaying();
		}

		getRecordButton().setEnabled(false);
		getRecordButton().setImageResource(R.drawable.recording_led);
		getPlayButton().setEnabled(false);
		getStopButton().setVisibility(View.VISIBLE);
		getStopButton().setEnabled(true);

		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);

		try {
			mRecorder.setOutputFile(mFileName);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mRecorder.setMaxDuration(mDurationLimitSeconds * 1000);
			mRecorder.setOnInfoListener(this);
			Chronometer chronometer = (Chronometer) getView().findViewById(R.id.current_position);
			chronometer.setBase(SystemClock.elapsedRealtime());
			chronometer.start();
			mIsRecording = true;
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}

		try {
			mRecorder.prepare();
			mRecorder.start();
		} catch (IOException e) {
			Log.e(TAG, "prepare()/start() failed", e);
			Toast.makeText(getActivity(), R.string.audio_recording_error, Toast.LENGTH_LONG).show();
		}

	}

	public void stopRecording() {
		if (mRecorder != null) {
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
			mHasRecorded = true;

			getPlayButton().setVisibility(View.VISIBLE);
			getPlayButton().setEnabled(true);
		}

		Chronometer chronometer = (Chronometer) getView().findViewById(R.id.current_position);
		chronometer.stop();
		getRecordButton().setEnabled(true);
		getStopButton().setEnabled(false);
		getRecordButton().setImageResource(R.drawable.record);
		mIsRecording = false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if (mFileName == null) {
			File appImagesFolder = getActivity().getDir(
				            "capturedPhotos", Context.MODE_WORLD_WRITEABLE);
			File mFile = new File(appImagesFolder, "audio_answer"
							+ SystemClock.elapsedRealtime() + "."
							+ ConstantData.AUDIO_QUESTION_FILE_EXTENSION);
			mFileName = mFile.getAbsolutePath();
		}

		View view = inflater.inflate(R.layout.fragment_audio_recorder, container, false);

		final ImageButton recordButton = (ImageButton) view.findViewById(R.id.button_record);

		recordButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startRecording();
			}
		});

		final ImageButton playButton = (ImageButton) view.findViewById(R.id.button_play);

		playButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startPlaying();
			}
		});

		playButton.setEnabled(false);

		final ImageButton stopButton = (ImageButton) view.findViewById(R.id.button_stop);

		stopButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onStopClicked();
			}
		});

		stopButton.setEnabled(false);

		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		updateDurationView();
		updateExistingRecordingView();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mRecorder != null) {
			mRecorder.release();
			mRecorder = null;
		}

		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}

	public String getRecordedAudioPath() {
		return mHasRecorded ? mFileName : "";
	}

	public void setDurationLimitSeconds(int durationSeconds) {
		mDurationLimitSeconds = durationSeconds;
		if (getView() != null) {
			updateDurationView();
		}
	}

	private void updateDurationView() {
		TextView length = (TextView) getView().findViewById(R.id.length);
		length.setText(String.format(" / %02d:%02d", mDurationLimitSeconds / 60, mDurationLimitSeconds % 60));
	}

	public void setExistingRecordingFileName(String filename) {
		mFileName = filename;
		mHasRecorded = true;
		if (getView() != null) {
			updateExistingRecordingView();
		}
	}

	private void updateExistingRecordingView() {
		if (mHasRecorded) {
			getStopButton().setVisibility(View.VISIBLE);
			getStopButton().setEnabled(false);
			getPlayButton().setVisibility(View.VISIBLE);
			getPlayButton().setEnabled(true);
		}
	}

	@Override
	public void onInfo(MediaRecorder mr, int what, int extra) {
		// Update the UI when we hit the duration limit to reflect the recording stopping.
		if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
			stopRecording();
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		stopPlaying();
	}

	public boolean isRecording() {
		return mIsRecording;
	}

	public boolean hasRecorded() {
		return mHasRecorded;
	}
}
