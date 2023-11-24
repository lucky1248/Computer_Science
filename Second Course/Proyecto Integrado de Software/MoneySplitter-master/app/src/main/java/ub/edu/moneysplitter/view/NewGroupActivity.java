package ub.edu.moneysplitter.view;

import static android.telephony.PhoneNumberUtils.formatNumber;

import static com.google.android.material.internal.ContextUtils.getActivity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

import ub.edu.moneysplitter.BuildConfig;
import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.model.UserContactsLocal;
import ub.edu.moneysplitter.viewmodel.GroupViewModel;



public class NewGroupActivity extends AppCompatActivity {

    private static final int REQUEST_READ_CONTACTS = 1;
    private static final int REQUEST_READ_STORAGE = 2;
    private static final int REQUEST_CAMERA = 3;
    private LinearLayout mNewMembersLayout;
    private ImageButton mAddMemberButton;
    private EditText mEditTextNombre;
    private EditText mEditTextDescGrupo;
    private EditText mEditTextMiembros;
    private Button mButtonNewGroup;
    private Button mImageGroup;
    private Button mAddFromContactButton;
    private ArrayList<String> newGroupMembers;
    private GroupViewModel mGroupViewModel;
    private String userID;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private ProgressBar mProgressBar;
    private Uri uri;
    private String opciones[] = {"Archivos", "Cámara"};
    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.dark_blue)));
        getSupportActionBar().setTitle("Nuevo Grupo");

        Intent i = getIntent();
        userID = i.getStringExtra("userID");
        mGroupViewModel = new ViewModelProvider(this)
                .get(GroupViewModel.class);
        newGroupMembers = new ArrayList<>();

        getViewItems();

        mProgressBar = findViewById(R.id.progress_loader_new_group);
        mButtonNewGroup.setOnClickListener(view -> {
            if(mEditTextNombre.getText().toString().equals("")){
                ((TextView) findViewById(R.id.textNombreGrupo)).setTextColor(Color.parseColor("#FF0000"));
                mEditTextNombre.setBackground(ContextCompat.getDrawable(NewGroupActivity.this, R.drawable.rounded_corners_plain_text_error));
            } else {
                mButtonNewGroup.setTextScaleX(0);
                mProgressBar.setVisibility(View.VISIBLE);

                newGroupMembers.add(userID);

                mGroupViewModel.addGroup(String.valueOf(mEditTextNombre.getText()),
                        String.valueOf(mEditTextDescGrupo.getText()), newGroupMembers, userID, uri, new GroupViewModel.CustomCallBack() {
                            @Override
                            public void onComplete(int success) {
                                if (success == -1) {
                                    ((TextView) findViewById(R.id.textNombreGrupo)).setTextColor(Color.parseColor("#FF0000"));
                                    mEditTextNombre.setBackground(ContextCompat.getDrawable(NewGroupActivity.this, R.drawable.rounded_corners_plain_text_error));
                                    Toast.makeText(getApplicationContext(), "Ya existe un grupo con el mismo nombre", Toast.LENGTH_SHORT).show();
                                    mButtonNewGroup.setTextScaleX(1);
                                    mProgressBar.setVisibility(View.INVISIBLE);
                                } else if (success == -2) {
                                    // Handle unsuccessful uploads
                                    Toast.makeText(getApplicationContext(), "No se ha podido guardar la imagen", Toast.LENGTH_SHORT).show();
                                    mButtonNewGroup.setTextScaleX(1);
                                    mProgressBar.setVisibility(View.INVISIBLE);
                                } else if (success == 1) {
                                    finish();
                                }
                            }
                        });
            }



            //check this stuff order
            /*ArrayList<String> tokens = new ArrayList<>();
            UserRepository.getInstance().addOnLoadUserTokensListeners(new UserRepository.OnLoadUserTokens() {
                @Override
                public void onLoadUserTokens() {
                    sendNotifications(tokens);
                }
            });
            UserRepository.getInstance().getUsersTokens(tokens, newGroupMembers);
            */
            //o ficquem un nounintent i tal

        });

        mImageGroup.setOnClickListener(view -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_rounded);

            builder.setTitle("Escoge cómo añadir la imagen").
                    setItems(opciones, new DialogInterface.OnClickListener() {
                        @SuppressLint("RestrictedApi")
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    if ((ContextCompat.checkSelfPermission(NewGroupActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(NewGroupActivity.this,
                                            Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)) {
                                        ActivityCompat.requestPermissions(getActivity(NewGroupActivity.this),
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
                                    if ((ContextCompat.checkSelfPermission(NewGroupActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(NewGroupActivity.this,
                                            Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)) {
                                        ActivityCompat.requestPermissions(getActivity(NewGroupActivity.this),
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
        mAddFromContactButton.setOnClickListener(view -> {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        REQUEST_READ_CONTACTS);
            } else {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, REQUEST_READ_CONTACTS);
            }


        });

        mAddMemberButton.setOnClickListener(view -> {
            String tlf = String.valueOf(mEditTextMiembros.getText());
            if (numberValid(tlf)) {
                mEditTextMiembros.setText("");
                addMember(tlf);
            }
        });
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                uri = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
                        BuildConfig.APPLICATION_ID + ".provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                takePictureIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                startActivityForResult(takePictureIntent, REQUEST_CAMERA);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El usuario ha concedido el permiso, podemos proceder a leer los contactos
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, REQUEST_READ_CONTACTS);
            } else {
                // El usuario ha denegado el permiso, podemos mostrar un mensaje o desactivar la funcionalidad relacionada

            }
        }
        else if (requestCode == REQUEST_READ_STORAGE){
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
                    Toast.makeText(getApplicationContext(), "Se necesita permiso de acceso al almacenamiento",
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
                    Toast.makeText(getApplicationContext(), "Se necesita permiso de acceso al almacenamiento",
                            Toast.LENGTH_SHORT);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_READ_CONTACTS:
                    // Obtenemos el ID del contacto seleccionado
                    String contactId = data.getData().getLastPathSegment();
                    // Creamos una proyección para obtener el número de teléfono del contacto
                    String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                    // Hacemos una consulta para obtener el número de teléfono del contacto
                    Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            projection,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{contactId},
                            null);
                    // Si la consulta devuelve resultados, obtenemos el primer número de teléfono
                    if (cursor != null && cursor.moveToFirst()) {
                        int phoneNumberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        String selectedPhoneNumber = cursor.getString(phoneNumberIndex);
                        addMember(formatPhoneNum(selectedPhoneNumber));//afegim el nou numero a la llista
                    }
                    // Cerramos el cursor
                    if (cursor != null) {
                        cursor.close();
                    }
                    // Mostramos el número de teléfono seleccionado en un TextView
                    //TextView textView = findViewById(R.id.text_view);
                    //textView.setText(selectedPhoneNumber);
                    break;
                case REQUEST_READ_STORAGE:
                    uri = data.getData();

                    mImageGroup.setText(uri.toString());
                    break;
                case REQUEST_CAMERA:
                    mImageGroup.setText(uri.toString());
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
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
                    }
            }
        }else{
            switch (requestCode) {
                case REQUEST_READ_STORAGE:
                    Toast.makeText(getApplicationContext(), "No se ha podido abrir la imagen seleccionada", Toast.LENGTH_SHORT).show();
                    break;
                case REQUEST_CAMERA:
                    Toast.makeText(getApplicationContext(), "No se ha podido hacer la fotografía", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private Bitmap rotateIfNecessary(Bitmap bitmap) {
        return rotateImage(bitmap, getCameraPhotoOrientation(getApplicationContext(),photoFile.toString()));
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


    public String formatPhoneNum(String num) {

        ArrayList<Character> allowed = new ArrayList<Character>(Arrays.asList('1', '2', '3', '4', '5', '6', '7', '8', '9', '0'));
        String formatedNum = "";
        for (int i = 0; i < num.length(); i++) {
            if (allowed.contains(num.charAt(i)))
                formatedNum += num.charAt(i);
        }

        if (formatedNum.length() > 8)
            formatedNum = formatedNum.substring(formatedNum.length() - 9);

        return formatedNum;
    }


    private void getViewItems() {
        mAddMemberButton = findViewById(R.id.addMemberButton);
        mEditTextNombre = findViewById(R.id.editTextNombre);
        mEditTextDescGrupo = findViewById(R.id.editTextDescGrupo);
        mEditTextMiembros = findViewById(R.id.editTextMiembros);
        mNewMembersLayout = findViewById(R.id.newMembersLayout);
        mAddFromContactButton = findViewById(R.id.addFromContactButton);
        mButtonNewGroup = findViewById(R.id.buttonNewGroup);
        mImageGroup = findViewById(R.id.addGroupImageButton);
    }


    private void addMember(String tlf) {

        newGroupMembers.add(tlf);

        tlf = UserContactsLocal.getInstance().getContact(tlf);

        LayoutInflater inflater = LayoutInflater.from(mNewMembersLayout.getContext());

        //Inflate crea una view genèrica definida pel layout que l'hi passem (l'user_card_layout)
        View view = inflater.inflate(R.layout.new_member_preview_layout, mNewMembersLayout, false); //le pasociamos al padre

        TextView number = view.findViewById(R.id.textViewMiembros);
        number.setText(tlf);

        mNewMembersLayout.addView(view);
    }

    private boolean numberValid(String tlf) {
        return true;
    }


}