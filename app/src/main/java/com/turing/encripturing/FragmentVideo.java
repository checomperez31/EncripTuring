package com.turing.encripturing;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.text.DecimalFormat;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentVideo.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentVideo#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentVideo extends Fragment {

    private OnFragmentInteractionListener mListener;
    private Context context;

    private final int SELECT_VIDEO = 301;
    private final int GRABAR_VIDEO = 302;

    private FloatingActionButton btnSeleccionarVideo;
    private CustomVideoView reproductor;
    private Button btnEncripttar;

    private String dataOfFile = null;
    private FragmentVideo.GraficaSonido graficaOriginal;
    private MediaController mc;

    //Instanciar WaveFormEncriptada
    private EditText editTitulo, editSize;
    private TextInputEditText startText, endText;
    private float density;
    private ImageButton playButton, rewindButton, ffwdButton, histButton;
    private WaveformView waveformView;
    private TextView info;
    private MarkerView startMarker, endMarker;

    public FragmentVideo() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentVideo.
     */

    public static FragmentVideo newInstance(String param1, String param2) {
        FragmentVideo fragment = new FragmentVideo();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        btnSeleccionarVideo = view.findViewById(R.id.fragment_video_btn_seleccionar_video);
        reproductor = view.findViewById(R.id.fragment_video_reproductor);
        editTitulo = view.findViewById(R.id.fragment_video_edit_titulo);
        editSize = view.findViewById(R.id.fragment_video_edit_tam);
        startText = view.findViewById(R.id.fragment_video_starttext);
        endText = view.findViewById(R.id.fragment_video_endtext);
        playButton = view.findViewById(R.id.fragment_video_play);
        rewindButton = view.findViewById(R.id.fragment_video_rew);
        ffwdButton = view.findViewById(R.id.fragment_video_ffwd);
        waveformView = view.findViewById(R.id.fragment_video_waveform);
        info = view.findViewById(R.id.fragment_video_info);
        startMarker = view.findViewById(R.id.fragment_video_startmarker);
        endMarker = view.findViewById(R.id.fragment_video_endmarker);
        histButton = view.findViewById(R.id.fragment_video_btn_histogramas);
        btnEncripttar = view.findViewById(R.id.fragment_video_button_encriptar);
        return view;
    }
    /**
     * Método para setear el reproductor y su contenido una vez se creó el fragment y la vista del mismo
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        density = metrics.density;
        btnSeleccionarVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOptions();
            }
        });
        reproductor.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    //Listener en caso de que el video se redimensione y posicionar de nuevo el media controller
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {
                        mc = new MediaController(getActivity());
                        //IMPORTANTE asignar el media controller al videoView antes de posicionarlo
                        //de lo contrario se colocará en la parte de abajo de la pantalla sobreponiendose
                        /*reproductor.setMediaController(mc);
                        mc.setAnchorView(reproductor);*/
                    }
                });
            }
        });

        histButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogHistogramas dialogHistogramas = new DialogHistogramas(context);
                dialogHistogramas.show();
            }
        });

        btnEncripttar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogProgress dp = new DialogProgress(context);
                dp.show();
            }
        });
    }


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public void showOptions()
    {
        final CharSequence[] option = {"Grabar video", "Elegir del Explorador", "Cancelar"};
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
        builder.setTitle("Elige una opción");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(option[which] == "Grabar video"){
                    Intent intent = new Intent();
                    intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
                    Log.i("VIDEO", "Llega hasta aqui");
                    getActivity().startActivityForResult(intent, GRABAR_VIDEO);
                }else if(option[which] == "Elegir del Explorador"){
                    Intent intent = new Intent();
                    intent.setType("video/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    Log.i("RUTA", "Llega hasta aqui");
                    getActivity().startActivityForResult(Intent.createChooser(intent,"Select Video "), SELECT_VIDEO);
                }else {
                    dialog.dismiss();
                }
            }
        });

        builder.show();
    }

    public void getPathFromUri(Uri path){
        String fileName;

        Log.i("FSENC", dataOfFile + "\n" + path + "\n" + path.getPath());
        Cursor cursor = null;
        try {
            final String docId = DocumentsContract.getDocumentId(path);
            final String[] split = docId.split(":");
            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";
            cursor = getActivity().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[]{
                    "_data",
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
            }, sel, new String[]{
                    split[1]
            }, null);

            if (cursor != null && cursor.moveToFirst()) {
                Log.i("FSENC", "Error en path" + cursor);
                fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME));
                dataOfFile = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
            }
        }
        catch (RuntimeException re){
            Log.i("FSENC", "Error en path");
            return;
        }
        finally {

            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void obtenerVideo(Uri uri){
        dataOfFile = null;
        if (dataOfFile == null){
            getPathFromUri(MediaStore.Files.getContentUri(uri.getPath()));
        }
        if (dataOfFile == null){
            getPathFromUri(MediaStore.Video.Media.getContentUri(uri.getPath()));
        }
        if (dataOfFile == null){
            getPathFromUri(MediaStore.getDocumentUri(context, uri));
        }
        if (dataOfFile != null){
            Log.i("IF", "Entra al 2do if " + dataOfFile);
            File file = new File(dataOfFile);
            graficaOriginal = new FragmentVideo.GraficaSonido(density, startText, endText, playButton, rewindButton, ffwdButton, waveformView, info, startMarker, endMarker, editTitulo, editSize, reproductor, dataOfFile);
            graficaOriginal.setmFile(file);
            graficaOriginal.generarGrafica();
        }
    }

    private class GraficaSonido implements com.turing.encripturing.MarkerView.MarkerListener, WaveformView.WaveformListener, VideoView.OnTouchListener{
        private FragmentVideo.GraficaSonido.BecomingNoisyReceiver myNoisyAudioStreamReceiver;
        private IntentFilter intentFilter;

        //Cosas del waveform
        private long mLoadingLastUpdateTime;
        private boolean mLoadingKeepGoing;
        private boolean mFinishActivity;
        private DialogProgress mProgressDialog;
        private SoundFile mSoundFile;
        private File mFile;
        private WaveformView mWaveformView;
        private MarkerView mStartMarker;
        private MarkerView mEndMarker;
        private TextInputEditText mStartText;
        private TextInputEditText mEndText;
        private TextView mInfo;
        private String mInfoContent;
        private ImageButton mPlayButton;
        private ImageButton mRewindButton;
        private ImageButton mFfwdButton;
        private boolean mKeyDown;
        private String mCaption = "";
        private int mWidth;
        private int mMaxPos;
        private int mStartPos;
        private int mEndPos;
        private boolean mStartVisible;
        private boolean mEndVisible;
        private int mLastDisplayedStartPos;
        private int mLastDisplayedEndPos;
        private int mOffset;
        private int mOffsetGoal;
        private int mFlingVelocity;
        private int mPlayStartMsec;
        private int mPlayEndMsec;
        private Handler mHandler;
        private boolean mIsPlaying;
        private SamplePlayer mPlayer;
        private boolean mTouchDragging;
        private float mTouchStart;
        private int mTouchInitialOffset;
        private int mTouchInitialStartPos;
        private int mTouchInitialEndPos;
        private long mWaveformTouchStartMsec;
        private float mDensity;
        private int mMarkerLeftInset;
        private int mMarkerRightInset;
        private int mMarkerTopOffset;
        private int mMarkerBottomOffset;

        private EditText mTitulo, mSize;
        private CustomVideoView mReproductor;
        private String mDataOfFile;

        private Thread mLoadSoundFileThread;


        public GraficaSonido(float mDensity, TextInputEditText mStartText, TextInputEditText mEndText, ImageButton mPlayButton, ImageButton mRewindButton, ImageButton mFfwdButton,
                             WaveformView mWaveformView, TextView mInfo, MarkerView mStartMarker, MarkerView mEndMarker, EditText titulo, EditText size, CustomVideoView mReproductor,
                             String dataOfFile){
            intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
            myNoisyAudioStreamReceiver = new FragmentVideo.GraficaSonido.BecomingNoisyReceiver();

            mTitulo = titulo;
            mSize = size;
            this.mReproductor = mReproductor;
            this.mReproductor.setOnTouchListener(this);
            this.mDataOfFile = dataOfFile;

            //Wave form
            mPlayer = null;
            mIsPlaying = false;
            mProgressDialog = null;
            mLoadSoundFileThread = null;
            mSoundFile = null;
            mKeyDown = false;

            mHandler = new Handler();

            this.mDensity = mDensity;

            mMarkerLeftInset = (int)(46 * mDensity);
            mMarkerRightInset = (int)(48 * mDensity);
            mMarkerTopOffset = (int)(10 * mDensity);
            mMarkerBottomOffset = (int)(10 * mDensity);

            this.mStartText = mStartText;
            mStartText.addTextChangedListener(mTextWatcher);

            this.mEndText = mEndText;
            mEndText.addTextChangedListener(mTextWatcher);

            this.mPlayButton = mPlayButton;
            mPlayButton.setOnClickListener(mPlayListener);
            this.mRewindButton = mRewindButton;
            mRewindButton.setOnClickListener(mRewindListener);
            this.mFfwdButton = mFfwdButton;
            mFfwdButton.setOnClickListener(mFfwdListener);

            this.mWaveformView = mWaveformView;
            mWaveformView.setListener(this);

            this.mInfo = mInfo;
            mInfo.setText(mCaption);

            this.mStartMarker = mStartMarker;
            mStartMarker.setListener(this);
            mStartMarker.setAlpha(1f);
            mStartMarker.setFocusable(true);
            mStartMarker.setFocusableInTouchMode(true);
            mStartVisible = true;

            this.mEndMarker = mEndMarker;
            mEndMarker.setListener(this);
            mEndMarker.setAlpha(1f);
            mEndMarker.setFocusable(true);
            mEndMarker.setFocusableInTouchMode(true);
            mEndVisible = true;

            mMaxPos = 0;
            mLastDisplayedStartPos = -1;
            mLastDisplayedEndPos = -1;


            mHandler.postDelayed(mTimerRunnable, 200);

            mWaveformView.mUnselectedBkgndLinePaint.setColor(ContextCompat.getColor(context, R.color.waveform_unselected_bkgnd_overlay_transparent));
        }

        public void setmFile(File mFile){
            this.mFile = mFile;
            mTitulo.setText(this.mFile.getName());
            DecimalFormat df = new DecimalFormat("#.00");
            mSize.setText(df.format(((double)this.mFile.length())/1024) + " KB");
        }

        public SoundFile getSoundFile(){
            return mSoundFile;
        }

        public void generarGrafica(){
            mLoadingLastUpdateTime = getCurrentTime();
            mLoadingKeepGoing = true;
            mFinishActivity = false;
            mProgressDialog = new DialogProgress(getActivity());
            mProgressDialog.setTitle(R.string.dialog_cargando_audio);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setOnCancelListener(
                    new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            mLoadingKeepGoing = false;
                            mFinishActivity = true;
                        }
                    });
            mProgressDialog.show();

            final SoundFile.ProgressListener listener =
                    new SoundFile.ProgressListener() {
                        public boolean reportProgress(final double fractionComplete) {
                            long now = getCurrentTime();
                            if (now - mLoadingLastUpdateTime > 100) {
                                final int progress = (int) (100 * fractionComplete * 4);
                                if (progress > mProgressDialog.getProgress()){
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mProgressDialog.setProgress(progress);
                                            Log.i("Fraction", fractionComplete + "");
                                        }
                                    });
                                }
                                mLoadingLastUpdateTime = now;
                            }
                            return mLoadingKeepGoing;
                        }
                    };

            // Load the sound file in a background thread
            mLoadSoundFileThread = new Thread() {
                public void run() {
                    try {
                        mSoundFile = SoundFile.create(mFile.getAbsolutePath(), listener);

                        if (mSoundFile == null) {
                            mProgressDialog.dismiss();
                            String name = mFile.getName().toLowerCase();
                            String[] components = name.split("\\.");
                            String err;
                            if (components.length < 2) {
                                err = "Error";
                            } else {
                                err = "Error de extension" + " " + components[components.length - 1];
                            }
                            final String finalErr = err;
                            Runnable runnable = new Runnable() {
                                public void run() {
                                    showFinalAlert(new Exception(), finalErr);
                                }
                            };
                            mHandler.post(runnable);
                            return;
                        }
                        mPlayer = new SamplePlayer(mSoundFile);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mReproductor.setVideoURI(Uri.parse(mDataOfFile));
                            }
                        });



                    } catch (final Exception e) {
                        mProgressDialog.dismiss();
                        e.printStackTrace();
                        mInfoContent = e.toString();
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                mInfo.setText(mInfoContent);
                            }
                        });

                        Runnable runnable = new Runnable() {
                            public void run() {
                                showFinalAlert(e, "Error");
                            }
                        };
                        mHandler.post(runnable);
                        return;
                    }
                    mProgressDialog.dismiss();
                    if (mLoadingKeepGoing) {
                        Runnable runnable = new Runnable() {
                            public void run() {
                                finishOpeningSoundFile();
                            }
                        };
                        mHandler.post(runnable);
                    } else if (mFinishActivity){

                    }
                }
            };
            mLoadSoundFileThread.start();
        }

        private Runnable mTimerRunnable = new Runnable() {
            public void run() {
                // Updating an EditText is slow on Android.  Make sure
                // we only do the update if the text has actually changed.
                if (mStartPos != mLastDisplayedStartPos &&
                        !mStartText.hasFocus()) {
                    mStartText.setText(formatTime(mStartPos));
                    mLastDisplayedStartPos = mStartPos;
                }

                if (mEndPos != mLastDisplayedEndPos &&
                        !mEndText.hasFocus()) {
                    mEndText.setText(formatTime(mEndPos));
                    mLastDisplayedEndPos = mEndPos;
                }

                mHandler.postDelayed(mTimerRunnable, 200);
            }
        };

        private TextWatcher mTextWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s,
                                      int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                if (mStartText.hasFocus()) {
                    try {
                        mStartPos = mWaveformView.secondsToPixels(
                                Double.parseDouble(
                                        mStartText.getText().toString()));
                        updateDisplay();
                    } catch (NumberFormatException e) {
                    }
                }
                if (mEndText.hasFocus()) {
                    try {
                        mEndPos = mWaveformView.secondsToPixels(
                                Double.parseDouble(
                                        mEndText.getText().toString()));
                        updateDisplay();
                    } catch (NumberFormatException e) {
                    }
                    catch(NullPointerException npe){

                    }
                }
            }
        };

        private void setOffsetGoalStart() {
            setOffsetGoal(mStartPos - mWidth / 2);
        }

        private void setOffsetGoalStartNoUpdate() {
            setOffsetGoalNoUpdate(mStartPos - mWidth / 2);
        }

        private void setOffsetGoalEnd() {
            setOffsetGoal(mEndPos - mWidth / 2);
        }

        private void setOffsetGoalEndNoUpdate() {
            setOffsetGoalNoUpdate(mEndPos - mWidth / 2);
        }

        private void setOffsetGoal(int offset) {
            setOffsetGoalNoUpdate(offset);
            updateDisplay();
        }

        private void setOffsetGoalNoUpdate(int offset) {
            if (mTouchDragging) {
                return;
            }

            mOffsetGoal = offset;
            if (mOffsetGoal + mWidth / 2 > mMaxPos)
                mOffsetGoal = mMaxPos - mWidth / 2;
            if (mOffsetGoal < 0)
                mOffsetGoal = 0;
        }

        private synchronized void handlePause() {
            if (mPlayer != null && mPlayer.isPlaying()) {
                mPlayer.pause();
                mReproductor.pause();
            }
            mWaveformView.setPlayback(-1);
            mIsPlaying = false;
            enableDisableButtons();
        }

        private void enableDisableButtons() {

            if (mIsPlaying) {
                mPlayButton.setImageResource(R.drawable.ic_media_stop);
            } else {
                mPlayButton.setImageResource(R.drawable.ic_media_play);
            }
        }

        private synchronized void updateDisplay() {
            if (mIsPlaying) {
                int now = mPlayer.getCurrentPosition();
                int frames = mWaveformView.millisecsToPixels(now);
                mWaveformView.setPlayback(frames);
                setOffsetGoalNoUpdate(frames - mWidth / 2);
                if (now >= mPlayEndMsec) {
                    handlePause();
                }
            }

            if (!mTouchDragging) {
                int offsetDelta;

                if (mFlingVelocity != 0) {
                    offsetDelta = mFlingVelocity / 30;
                    if (mFlingVelocity > 80) {
                        mFlingVelocity -= 80;
                    } else if (mFlingVelocity < -80) {
                        mFlingVelocity += 80;
                    } else {
                        mFlingVelocity = 0;
                    }

                    mOffset += offsetDelta;

                    if (mOffset + mWidth / 2 > mMaxPos) {
                        mOffset = mMaxPos - mWidth / 2;
                        mFlingVelocity = 0;
                    }
                    if (mOffset < 0) {
                        mOffset = 0;
                        mFlingVelocity = 0;
                    }
                    mOffsetGoal = mOffset;
                } else {
                    offsetDelta = mOffsetGoal - mOffset;

                    if (offsetDelta > 10)
                        offsetDelta = offsetDelta / 10;
                    else if (offsetDelta > 0)
                        offsetDelta = 1;
                    else if (offsetDelta < -10)
                        offsetDelta = offsetDelta / 10;
                    else if (offsetDelta < 0)
                        offsetDelta = -1;
                    else
                        offsetDelta = 0;

                    mOffset += offsetDelta;
                }
            }
            mWaveformView.setParameters(mStartPos, mEndPos, mOffset);
            mWaveformView.invalidate();

            mStartMarker.setContentDescription(
                    getResources().getText(R.string.start_marker) + " " +
                            formatTime(mStartPos));
            mEndMarker.setContentDescription(
                    getResources().getText(R.string.end_marker) + " " +
                            formatTime(mEndPos));

            int startX = mStartPos - mOffset - mMarkerLeftInset;
            if (startX + mStartMarker.getWidth() >= 0) {
                if (!mStartVisible) {
                    // Delay this to avoid flicker
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            mStartVisible = true;
                            mStartMarker.setAlpha(1f);
                        }
                    }, 0);
                }
            } else {
                if (mStartVisible) {
                    mStartMarker.setAlpha(0f);
                    mStartVisible = false;
                }
                startX = 0;
            }

            int endX = mEndPos - mOffset - mEndMarker.getWidth() + mMarkerRightInset;
            if (endX + mEndMarker.getWidth() >= 0) {
                if (!mEndVisible) {
                    // Delay this to avoid flicker
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            mEndVisible = true;
                            mEndMarker.setAlpha(1f);
                        }
                    }, 0);
                }
            } else {
                if (mEndVisible) {
                    mEndMarker.setAlpha(0f);
                    mEndVisible = false;
                }
                endX = 0;
            }

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(
                    startX,
                    mMarkerTopOffset,
                    -mStartMarker.getWidth(),
                    -mStartMarker.getHeight());
            mStartMarker.setLayoutParams(params);

            params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(
                    endX,
                    mWaveformView.getMeasuredHeight() - mEndMarker.getHeight() - mMarkerBottomOffset,
                    -mStartMarker.getWidth(),
                    -mStartMarker.getHeight());
            mEndMarker.setLayoutParams(params);
        }

        private String formatTime(int pixels) {
            if (mWaveformView != null && mWaveformView.isInitialized()) {
                return formatDecimal(mWaveformView.pixelsToSeconds(pixels));
            } else {
                return "";
            }
        }

        private String formatDecimal(double x) {
            int xWhole = (int)x;
            int xFrac = (int)(100 * (x - xWhole) + 0.5);

            if (xFrac >= 100) {
                xWhole++; //Round up
                xFrac -= 100; //Now we need the remainder after the round up
                if (xFrac < 10) {
                    xFrac *= 10; //we need a fraction that is 2 digits long
                }
            }

            if (xFrac < 10)
                return xWhole + ".0" + xFrac;
            else
                return xWhole + "." + xFrac;
        }

        @Override
        public void markerTouchStart(com.turing.encripturing.MarkerView marker, float x) {
            mTouchDragging = true;
            mTouchStart = x;
            mTouchInitialStartPos = mStartPos;
            mTouchInitialEndPos = mEndPos;
        }

        @Override
        public void markerTouchMove(com.turing.encripturing.MarkerView marker, float x) {
            float delta = x - mTouchStart;

            if (marker == mStartMarker) {
                mStartPos = trap((int)(mTouchInitialStartPos + delta));
                mEndPos = trap((int)(mTouchInitialEndPos + delta));
            } else {
                mEndPos = trap((int)(mTouchInitialEndPos + delta));
                if (mEndPos < mStartPos)
                    mEndPos = mStartPos;
            }

            updateDisplay();
        }

        private int trap(int pos) {
            if (pos < 0)
                return 0;
            if (pos > mMaxPos)
                return mMaxPos;
            return pos;
        }

        @Override
        public void markerTouchEnd(com.turing.encripturing.MarkerView marker) {
            mTouchDragging = false;
            if (marker == mStartMarker) {
                setOffsetGoalStart();
            } else {
                setOffsetGoalEnd();
            }
        }

        @Override
        public void markerFocus(com.turing.encripturing.MarkerView marker) {
            mKeyDown = false;
            if (marker == mStartMarker) {
                setOffsetGoalStartNoUpdate();
            } else {
                setOffsetGoalEndNoUpdate();
            }

            // Delay updaing the display because if this focus was in
            // response to a touch event, we want to receive the touch
            // event too before updating the display.
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    updateDisplay();
                }
            }, 100);
        }

        @Override
        public void markerLeft(com.turing.encripturing.MarkerView marker, int velocity) {
            mKeyDown = true;

            if (marker == mStartMarker) {
                int saveStart = mStartPos;
                mStartPos = trap(mStartPos - velocity);
                mEndPos = trap(mEndPos - (saveStart - mStartPos));
                setOffsetGoalStart();
            }

            if (marker == mEndMarker) {
                if (mEndPos == mStartPos) {
                    mStartPos = trap(mStartPos - velocity);
                    mEndPos = mStartPos;
                } else {
                    mEndPos = trap(mEndPos - velocity);
                }

                setOffsetGoalEnd();
            }

            updateDisplay();
        }

        @Override
        public void markerRight(com.turing.encripturing.MarkerView marker, int velocity) {
            mKeyDown = true;

            if (marker == mStartMarker) {
                int saveStart = mStartPos;
                mStartPos += velocity;
                if (mStartPos > mMaxPos)
                    mStartPos = mMaxPos;
                mEndPos += (mStartPos - saveStart);
                if (mEndPos > mMaxPos)
                    mEndPos = mMaxPos;

                setOffsetGoalStart();
            }

            if (marker == mEndMarker) {
                mEndPos += velocity;
                if (mEndPos > mMaxPos)
                    mEndPos = mMaxPos;

                setOffsetGoalEnd();
            }

            updateDisplay();
        }

        @Override
        public void markerEnter(com.turing.encripturing.MarkerView marker) {

        }

        @Override
        public void markerKeyUp() {
            mKeyDown = false;
            updateDisplay();
        }

        @Override
        public void markerDraw() {

        }

        @Override
        public void waveformTouchStart(float x) {
            mTouchDragging = true;
            mTouchStart = x;
            mTouchInitialOffset = mOffset;
            mFlingVelocity = 0;
            mWaveformTouchStartMsec = getCurrentTime();
        }

        @Override
        public void waveformTouchMove(float x) {
            mOffset = trap((int)(mTouchInitialOffset + (mTouchStart - x)));
            updateDisplay();
        }

        @Override
        public void waveformTouchEnd() {
            mTouchDragging = false;
            mOffsetGoal = mOffset;

            long elapsedMsec = getCurrentTime() - mWaveformTouchStartMsec;
            if (elapsedMsec < 300) {
                if (mIsPlaying) {
                    int seekMsec = mWaveformView.pixelsToMillisecs(
                            (int)(mTouchStart + mOffset));
                    if (seekMsec >= mPlayStartMsec &&
                            seekMsec < mPlayEndMsec) {
                        mPlayer.seekTo(seekMsec);
                        mReproductor.seekTo(seekMsec);
                    } else {
                        handlePause();
                    }
                } else {
                    onPlay((int)(mTouchStart + mOffset));
                }
            }
        }

        private synchronized void onPlay(int startPosition) {
            if (mIsPlaying) {
                context.unregisterReceiver(myNoisyAudioStreamReceiver);
                handlePause();
                return;
            }

            if (mPlayer == null) {
                // Not initialized yet
                return;
            }

            try {
                mPlayStartMsec = mWaveformView.pixelsToMillisecs(startPosition);
                if (startPosition < mStartPos) {
                    mPlayEndMsec = mWaveformView.pixelsToMillisecs(mStartPos);
                } else if (startPosition > mEndPos) {
                    mPlayEndMsec = mWaveformView.pixelsToMillisecs(mMaxPos);
                } else {
                    mPlayEndMsec = mWaveformView.pixelsToMillisecs(mEndPos);
                }
                mPlayer.setOnCompletionListener(new SamplePlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion() {
                        handlePause();
                    }
                });
                mIsPlaying = true;

                mPlayer.seekTo(mPlayStartMsec);
                mReproductor.seekTo(mPlayStartMsec);
                context.registerReceiver(myNoisyAudioStreamReceiver, intentFilter);
                mPlayer.start();//Se comienza a reproducir el audio
                mReproductor.start();
                mReproductor.requestFocus();//Se comienza a reproducir el video
                updateDisplay();
                enableDisableButtons();
            } catch (Exception e) {
                showFinalAlert(e, "Error al reproducir");
            }
        }

        @Override
        public void waveformFling(float vx) {
            mTouchDragging = false;
            mOffsetGoal = mOffset;
            mFlingVelocity = (int)(-vx);
            updateDisplay();
        }

        @Override
        public void waveformDraw() {
            mWidth = mWaveformView.getMeasuredWidth();
            if (mOffsetGoal != mOffset && !mKeyDown)
                updateDisplay();
            else if (mIsPlaying) {
                updateDisplay();
            } else if (mFlingVelocity != 0) {
                updateDisplay();
            }
        }

        @Override
        public void waveformZoomIn() {
            mWaveformView.zoomIn();
            mStartPos = mWaveformView.getStart();
            mEndPos = mWaveformView.getEnd();
            mMaxPos = mWaveformView.maxPos();
            mOffset = mWaveformView.getOffset();
            mOffsetGoal = mOffset;
            updateDisplay();
        }

        @Override
        public void waveformZoomOut() {
            mWaveformView.zoomOut();
            mStartPos = mWaveformView.getStart();
            mEndPos = mWaveformView.getEnd();
            mMaxPos = mWaveformView.maxPos();
            mOffset = mWaveformView.getOffset();
            mOffsetGoal = mOffset;
            updateDisplay();
        }

        private View.OnClickListener mPlayListener = new View.OnClickListener() {
            public void onClick(View sender) {
                onPlay(mStartPos);
            }
        };

        private View.OnClickListener mRewindListener = new View.OnClickListener() {
            public void onClick(View sender) {
                if (mIsPlaying) {
                    int newPos = mPlayer.getCurrentPosition() - 5000;
                    if (newPos < mPlayStartMsec)
                        newPos = mPlayStartMsec;
                    mPlayer.seekTo(newPos);
                    mReproductor.seekTo(newPos);
                } else {
                    mStartMarker.requestFocus();
                    markerFocus(mStartMarker);
                }
            }
        };

        private View.OnClickListener mFfwdListener = new View.OnClickListener() {
            public void onClick(View sender) {
                if (mIsPlaying) {
                    int newPos = 5000 + mPlayer.getCurrentPosition();
                    if (newPos > mPlayEndMsec)
                        newPos = mPlayEndMsec;
                    mPlayer.seekTo(newPos);
                    mReproductor.seekTo(newPos);
                } else {
                    mEndMarker.requestFocus();
                    markerFocus(mEndMarker);
                }
            }
        };

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if(mWaveformView.getVisibility() == View.GONE){
                mWaveformView.setVisibility(View.VISIBLE);
                mStartMarker.setVisibility(View.VISIBLE);
                mEndMarker.setVisibility(View.VISIBLE);
            }
            else
            {
                mWaveformView.setVisibility(View.GONE);
                mStartMarker.setVisibility(View.GONE);
                mEndMarker.setVisibility(View.GONE);
            }
            return false;
        }


        private class BecomingNoisyReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                    Log.i("Jack", "Sparrow");
                    if(mIsPlaying){
                        handlePause();
                    }
                }
            }
        }

        private void finishOpeningSoundFile() {
            mWaveformView.setSoundFile(mSoundFile);
            mWaveformView.recomputeHeights(mDensity);

            mMaxPos = mWaveformView.maxPos();
            mLastDisplayedStartPos = -1;
            mLastDisplayedEndPos = -1;

            mTouchDragging = false;

            mOffset = 0;
            mOffsetGoal = 0;
            mFlingVelocity = 0;
            resetPositions();
            /*if (mEndPos > mMaxPos)
                mEndPos = mMaxPos;*/
            mEndPos = mMaxPos;

            mCaption =
                    mSoundFile.getFiletype() + ", " +
                            mSoundFile.getSampleRate() + " Hz, " +
                            mSoundFile.getAvgBitrateKbps() + " kbps, " +
                            formatTime(mMaxPos) + " " +
                            "s";
            mInfo.setText(mCaption);

            updateDisplay();
        }

        public String getMaxTime()
        {
            return formatTime(mMaxPos);
        }

        private void resetPositions() {
            mStartPos = mWaveformView.secondsToPixels(0.0);
            mEndPos = mWaveformView.secondsToPixels(15.0);
        }

        private long getCurrentTime() {
            return System.nanoTime() / 1000000;
        }

        private void showFinalAlert(Exception e, CharSequence message) {
            CharSequence title;
            if (e != null) {
                Log.e("Encripturing", "Error: " + message);
                Log.e("Encripturing", "Error" + e.toString());
                title = "Falla";
                getActivity().setResult(100, new Intent());
            } else {
                Log.v("Encripturing", "Success: " + message);
                title = "Exito";
            }

            new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(
                            "Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    //finish();
                                }
                            })
                    .setCancelable(false)
                    .show();
        }


    }//fin de la clase

}
