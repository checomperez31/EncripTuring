package com.turing.encripturing;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link KeysFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link KeysFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class KeysFragment extends Fragment {
    private Button btnGuardarLlaves;

    private EditText edtKey00;
    private EditText edtKey01;
    private EditText edtKey02;
    private EditText edtKey03;
    private EditText edtKey04;
    private EditText edtKey05;
    private EditText edtKey06;
    private EditText edtKey07;
    private EditText edtKey08;
    private EditText edtKeyInv00;
    private EditText edtKeyInv01;
    private EditText edtKeyInv02;
    private EditText edtKeyInv03;
    private EditText edtKeyInv04;
    private EditText edtKeyInv05;
    private EditText edtKeyInv06;
    private EditText edtKeyInv07;
    private EditText edtKeyInv08;

    private EditText edtKeyVid00;
    private EditText edtKeyVid01;
    private EditText edtKeyVid02;
    private EditText edtKeyVid03;
    private EditText edtKeyVid05;
    private EditText edtKeyVid04;
    private EditText edtKeyVid06;
    private EditText edtKeyVid07;
    private EditText edtKeyVid08;
    private EditText edtKeyVidInv00;
    private EditText edtKeyVidInv01;
    private EditText edtKeyVidInv02;
    private EditText edtKeyVidInv03;
    private EditText edtKeyVidInv04;
    private EditText edtKeyVidInv05;
    private EditText edtKeyVidInv06;
    private EditText edtKeyVidInv07;
    private EditText edtKeyVidInv08;


    int[][] llaveVideo, llaveDesVideo, llaveAudio, llaveDesAudio;

    private DatosEncriptar datosEncriptar;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public KeysFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment KeysFragment.
     */

    public static KeysFragment newInstance(String param1, String param2) {
        KeysFragment fragment = new KeysFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_keys, container, false);
        btnGuardarLlaves = view.findViewById(R.id.btn_guardar_matrices);

        edtKey00 = view.findViewById(R.id.edt_key00);
        edtKey01 = view.findViewById(R.id.edt_key01);
        edtKey02 = view.findViewById(R.id.edt_key02);
        edtKey03 = view.findViewById(R.id.edt_key10);
        edtKey04 = view.findViewById(R.id.edt_key11);
        edtKey05 = view.findViewById(R.id.edt_key12);
        edtKey06 = view.findViewById(R.id.edt_key20);
        edtKey07 = view.findViewById(R.id.edt_key21);
        edtKey08 = view.findViewById(R.id.edt_key22);
        edtKeyInv00 = view.findViewById(R.id.edt_keyinv00);
        edtKeyInv01 = view.findViewById(R.id.edt_keyinv01);
        edtKeyInv02 = view.findViewById(R.id.edt_keyinv02);
        edtKeyInv03 = view.findViewById(R.id.edt_keyinv10);
        edtKeyInv04 = view.findViewById(R.id.edt_keyinv11);
        edtKeyInv05 = view.findViewById(R.id.edt_keyinv12);
        edtKeyInv06 = view.findViewById(R.id.edt_keyinv20);
        edtKeyInv07 = view.findViewById(R.id.edt_keyinv21);
        edtKeyInv08 = view.findViewById(R.id.edt_keyinv22);

        edtKeyVid00 = view.findViewById(R.id.edt_keyvid00);
        edtKeyVid01 = view.findViewById(R.id.edt_keyvid01);
        edtKeyVid02 = view.findViewById(R.id.edt_keyvid02);
        edtKeyVid03 = view.findViewById(R.id.edt_keyvid10);
        edtKeyVid04 = view.findViewById(R.id.edt_keyvid11);
        edtKeyVid05 = view.findViewById(R.id.edt_keyvid12);
        edtKeyVid06 = view.findViewById(R.id.edt_keyvid20);
        edtKeyVid07 = view.findViewById(R.id.edt_keyvid21);
        edtKeyVid08 = view.findViewById(R.id.edt_keyvid22);
        edtKeyVidInv00 = view.findViewById(R.id.edt_keyvid00_inv);
        edtKeyVidInv01 = view.findViewById(R.id.edt_keyvid01_inv);
        edtKeyVidInv02 = view.findViewById(R.id.edt_keyvid02_inv);
        edtKeyVidInv03 = view.findViewById(R.id.edt_keyvid10_inv);
        edtKeyVidInv04 = view.findViewById(R.id.edt_keyvid11_inv);
        edtKeyVidInv05 = view.findViewById(R.id.edt_keyvid12_inv);
        edtKeyVidInv06 = view.findViewById(R.id.edt_keyvid20_inv);
        edtKeyVidInv07 = view.findViewById(R.id.edt_keyvid21_inv);
        edtKeyVidInv08 = view.findViewById(R.id.edt_keyvid22_inv);


        llaveVideo = new int[3][3];
        llaveDesVideo = new int[3][3];
        llaveAudio = new int[3][3];
        llaveDesAudio = new int[3][3];

        cargarListeners();
        return view;
    }


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public void cargarListeners(){
        btnGuardarLlaves.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarMatricesAudio();
                guardarMatricesVideo();
            }
        });
    }

    public void guardarMatricesAudio(){
        llaveAudio[0][0] = Integer.parseInt(edtKey00.getText().toString());
        llaveAudio[0][1] = Integer.parseInt(edtKey01.getText().toString());
        llaveAudio[0][2] = Integer.parseInt(edtKey02.getText().toString());
        llaveAudio[1][0] = Integer.parseInt(edtKey03.getText().toString());
        llaveAudio[1][1] = Integer.parseInt(edtKey04.getText().toString());
        llaveAudio[1][2] = Integer.parseInt(edtKey05.getText().toString());
        llaveAudio[2][0] = Integer.parseInt(edtKey06.getText().toString());
        llaveAudio[2][1] = Integer.parseInt(edtKey07.getText().toString());
        llaveAudio[2][2] = Integer.parseInt(edtKey08.getText().toString());


        llaveDesAudio[0][0] = Integer.parseInt(edtKeyInv00.getText().toString());
        llaveDesAudio[0][1] = Integer.parseInt(edtKeyInv01.getText().toString());
        llaveDesAudio[0][2] = Integer.parseInt(edtKeyInv02.getText().toString());
        llaveDesAudio[1][0] = Integer.parseInt(edtKeyInv03.getText().toString());
        llaveDesAudio[1][1] = Integer.parseInt(edtKeyInv04.getText().toString());
        llaveDesAudio[1][2] = Integer.parseInt(edtKeyInv05.getText().toString());
        llaveDesAudio[2][0] = Integer.parseInt(edtKeyInv06.getText().toString());
        llaveDesAudio[2][1] = Integer.parseInt(edtKeyInv07.getText().toString());
        llaveDesAudio[2][2] = Integer.parseInt(edtKeyInv08.getText().toString());

        DatosEncriptar.getInstance().setLlaveAudio(llaveAudio);
        DatosEncriptar.getInstance().setLlaveDesAudio(llaveDesAudio);
    }

    public void guardarMatricesVideo(){
        llaveVideo[0][0] = Integer.parseInt(edtKeyVid00.getText().toString());
        llaveVideo[0][1] = Integer.parseInt(edtKeyVid01.getText().toString());
        llaveVideo[0][2] = Integer.parseInt(edtKeyVid02.getText().toString());
        llaveVideo[1][0] = Integer.parseInt(edtKeyVid03.getText().toString());
        llaveVideo[1][1] = Integer.parseInt(edtKeyVid04.getText().toString());
        llaveVideo[1][2] = Integer.parseInt(edtKeyVid05.getText().toString());
        llaveVideo[2][0] = Integer.parseInt(edtKeyVid06.getText().toString());
        llaveVideo[2][1] = Integer.parseInt(edtKeyVid07.getText().toString());
        llaveVideo[2][2] = Integer.parseInt(edtKeyVid08.getText().toString());


        llaveDesVideo[0][0] = Integer.parseInt(edtKeyVidInv00.getText().toString());
        llaveDesVideo[0][1] = Integer.parseInt(edtKeyVidInv01.getText().toString());
        llaveDesVideo[0][2] = Integer.parseInt(edtKeyVidInv02.getText().toString());
        llaveDesVideo[1][0] = Integer.parseInt(edtKeyVidInv03.getText().toString());
        llaveDesVideo[1][1] = Integer.parseInt(edtKeyVidInv04.getText().toString());
        llaveDesVideo[1][2] = Integer.parseInt(edtKeyVidInv05.getText().toString());
        llaveDesVideo[2][0] = Integer.parseInt(edtKeyVidInv06.getText().toString());
        llaveDesVideo[2][1] = Integer.parseInt(edtKeyVidInv07.getText().toString());
        llaveDesVideo[2][2] = Integer.parseInt(edtKeyVidInv08.getText().toString());

        DatosEncriptar.getInstance().setLlaveVideo(llaveAudio);
        DatosEncriptar.getInstance().setLlaveDesVideo(llaveDesAudio);
    }
}
