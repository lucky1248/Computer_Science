package ub.edu.moneysplitter.view;

import static com.google.android.material.internal.ContextUtils.getActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import ub.edu.moneysplitter.BuildConfig;
import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.viewmodel.UserViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewBillConfigFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewBillConfigFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final int REQUEST_READ_STORAGE = 2;

    private static final int REQUEST_CAMERA = 3;

    private Uri uri;
    private String opciones[] = {"Archivos", "Cámara"};
    private File photoFile;
    NewBillActivity nba;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private static final String REPARTIR_EQUIT = "Repartir equitativamente";
    private static final String REPARTIR_PERSON = "Introducir las quantias obtenidas";
    private Spinner mSpinnerPayer;
    private Spinner mSpinnerPayMethod;
    private Button mNext;
    private Button mImage;
    private EditText mConcepto;
    public NewBillConfigFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NewBillConfigFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NewBillConfigFragment newInstance(String param1, String param2) {
        NewBillConfigFragment fragment = new NewBillConfigFragment();
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
        View view = inflater.inflate(R.layout.fragment_new_bill_config, container, false);
        nba = (NewBillActivity) getActivity();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_item, passToContacts(nba.getgroup().getMembers().getArray()));

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerPayer = (Spinner) view.findViewById(R.id.spinnerPayer);
        mSpinnerPayer.setAdapter(adapter);


        ArrayList<String> al = new ArrayList<>();
        al.add(REPARTIR_PERSON);
        al.add(REPARTIR_EQUIT); //hacer un array global asi mes adaptable a nouevos cambios
        ArrayAdapter<String> adapterPayM = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_item, al);
        adapterPayM.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerPayMethod = (Spinner) view.findViewById(R.id.spinnerPayWay);
        mSpinnerPayMethod.setAdapter(adapterPayM);

        mNext = view.findViewById(R.id.buttonNewBill);

        mConcepto = view.findViewById(R.id.editTextConceptoBill);
        TextView ConceptoSupportText = view.findViewById(R.id.textConceptoBill);
        mImage = view.findViewById(R.id.addBillImageButton);
        mImage.setOnClickListener(v -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialog_rounded);

            builder.setTitle("Escoge cómo añadir la imagen").
                    setItems(opciones, new DialogInterface.OnClickListener() {
                        @SuppressLint("RestrictedApi")
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    if ((ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getContext(),
                                            Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)) {
                                        ActivityCompat.requestPermissions(getActivity(),
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                        Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_READ_STORAGE);
                                    } else {
                                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                        intent.setType("image/*");
                                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                                        startActivityForResult(intent, REQUEST_READ_STORAGE);
                                    }
                                    break;
                                case 1:
                                    if ((ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getContext(),
                                            Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)) {
                                        ActivityCompat.requestPermissions(getActivity(),
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                        Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CAMERA);
                                    } else {
                                        dispatchTakePictureIntent();
                                    }
                                    break;
                            }
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        });
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mConcepto.getText().toString().equals("")){

                    ConceptoSupportText.setTextColor(Color.parseColor("#FF0000"));
                    mConcepto.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_corners_plain_text_error));
                } else {
                    nba.newBillDataUpdate(uri, mConcepto.getText().toString(), UserViewModel.getUserID(mSpinnerPayer.getSelectedItem().toString()));

                    if (mSpinnerPayMethod.getSelectedItem().toString().equals(REPARTIR_EQUIT))
                        nba.changeFragmentUsers();
                    if (mSpinnerPayMethod.getSelectedItem().toString().equals(REPARTIR_PERSON))
                        nba.changeFragmentPersonalized();
                }
            }
        });

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_STORAGE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El usuario ha concedido el permiso
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_READ_STORAGE);
            } else {
                // entramos aquí si no se denega el READ_EXTERNAL_STORAGE, que pasa en android 13
                if(grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    //tenemos el permiso de media de android 13
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.setType("image/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, REQUEST_READ_STORAGE);
                }
                else{
                    Toast.makeText(getContext(), "Se necesita permiso de acceso al almacenamiento",
                            Toast.LENGTH_SHORT);
                }
            }
        }
        else if(requestCode == REQUEST_CAMERA){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El usuario ha concedido el permiso
                dispatchTakePictureIntent();
            } else {
                // entramos aquí si no se denega el READ_EXTERNAL_STORAGE, que pasa en android 13
                if(grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    //tenemos el permiso de media de android 13
                    dispatchTakePictureIntent();
                }
                else{
                    Toast.makeText(getContext(), "Se necesita permiso de acceso al almacenamiento",
                            Toast.LENGTH_SHORT);
                }
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_READ_STORAGE:
                    uri = data.getData();
                    mImage.setText(uri.toString());
                    break;
                case REQUEST_CAMERA:
                    mImage.setText(uri.toString());
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(nba.getContentResolver(), uri);
                        Bitmap bitmap1 = rotateIfNecessary(bitmap);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap1.compress(Bitmap.CompressFormat.JPEG, 40, baos);
                        byte[] bitmapdata = baos.toByteArray();
                        //write the bytes in file
                        FileOutputStream fos = new FileOutputStream(photoFile);
                        fos.write(bitmapdata);
                        fos.flush();
                        fos.close();
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
            }
        }
        else{
            switch (requestCode) {
                case REQUEST_READ_STORAGE:
                    Toast.makeText(getContext(), "No se ha podido abrir la imagen seleccionada", Toast.LENGTH_SHORT).show();
                    break;
                case REQUEST_CAMERA:
                    Toast.makeText(getContext(), "No se ha podido hacer la fotografía", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }
    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            // Create the File where the photo should go
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                uri = FileProvider.getUriForFile(requireContext(),
                        BuildConfig.APPLICATION_ID + ".provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                takePictureIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                startActivityForResult(takePictureIntent, REQUEST_CAMERA);

            }
        }
    }

    protected Bitmap rotateIfNecessary(Bitmap bitmap) {
        return rotateImage(bitmap, getCameraPhotoOrientation(getContext(),photoFile.toString()));
    }

    public static int getCameraPhotoOrientation(Context context, String imagePath)
    {
        int rotate = 0;
        try {
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(
                    imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rotate;
    }
    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        return rotatedImg;
    }

    public ArrayList<String> passToContacts(ArrayList<String> users){
        ArrayList<String> contacts = new ArrayList<>();
        for(String uID: users){
            contacts.add(UserViewModel.getContact(uID));
        }
        return contacts;
    }
}