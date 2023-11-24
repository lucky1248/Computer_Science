package ub.edu.moneysplitter.view;

import static android.content.ContentValues.TAG;
import static com.google.android.material.internal.ContextUtils.getActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

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
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

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
import ub.edu.moneysplitter.model.GroupRepository;
import ub.edu.moneysplitter.model.SaveSharedPreference;
import ub.edu.moneysplitter.model.User;
import ub.edu.moneysplitter.viewmodel.GroupViewModel;
import ub.edu.moneysplitter.viewmodel.UserViewModel;

public class EditProfileActivity extends AppCompatActivity {

    private static final int REQUEST_READ_STORAGE = 2;
    private static final int REQUEST_CAMERA = 3;
    private Uri uri;
    private String opciones[] = {"Archivos", "Cámara"};
    private File photoFile;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private UserViewModel mUvm;
    private User mUser;
    private ImageButton mBack;
    private Button mNewImage;
    private Button mContra;
    private Button mNotificacions;
    private Button mClose;
    private Button mEliminate;
    private ImageView mImage;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.dark_blue)));
        getSupportActionBar().setTitle("Editar Perfil");
        mBack = findViewById(R.id.imageButtonBack);
        mNewImage = findViewById(R.id.newImageButton);
        mContra = findViewById(R.id.contraseñaButton);
        mNotificacions = findViewById(R.id.notificacionesButton);
        mClose = findViewById(R.id.closeButton);
        mEliminate = findViewById(R.id.eliminateButton);
        mImage = findViewById(R.id.imageView);

        mUser = (User)getIntent().getSerializableExtra("mUser");
        mUvm = new ViewModelProvider(this).get(UserViewModel.class);

        paintImageView();

        mBack.setOnClickListener(view -> {
            Intent returnIntent = new Intent();
            returnIntent.putExtra ("mUser", mUser);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        });


        mNewImage.setOnClickListener(view -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_rounded);

            builder.setTitle("Escoge cómo añadir la imagen").
                    setItems(opciones, new DialogInterface.OnClickListener() {
                        @SuppressLint("RestrictedApi")
                        public void onClick(DialogInterface dialog, int which) {

                            switch (which) {
                                case 0:
                                    if ((ContextCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(EditProfileActivity.this,
                                            Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)) {
                                        ActivityCompat.requestPermissions(getActivity(EditProfileActivity.this),
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
                                        if ((ContextCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(EditProfileActivity.this,
                                            Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)) {
                                        ActivityCompat.requestPermissions(getActivity(EditProfileActivity.this),
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

        mContra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditProfileActivity.this, ChangePasswordActivity.class);
                intent.putExtra("mUser", mUser);
                startActivity(intent);
            }
        });

        mNotificacions.setOnClickListener(view -> {
            if(mNotificacions.getText().toString().equals("Desactivar notificaciones")){
                mNotificacions.setText("Activar notificaciones");
            }
            else{
                mNotificacions.setText("Desactivar notificaciones");
            }
        });
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveSharedPreference.setLoggedIn(EditProfileActivity.this, false, "", "");
                Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


        mEliminate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SaveSharedPreference.setLoggedIn(EditProfileActivity.this, false, "", "");
                Toast.makeText(getApplicationContext(), "Coming soon. Stay tuned",
                        Toast.LENGTH_SHORT);
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
                case REQUEST_READ_STORAGE:
                    uri = data.getData();
                    saveImage();
                    break;
                case REQUEST_CAMERA:
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
                        saveImage();
                    } catch (IOException e) {
                    }
            }
        }
        else{
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
    private void saveImage(){
        if(uri != null){
            StorageReference imageRef = storage.getReference().child(
                    "images/profiles/" + mUser.getId());
            UploadTask uploadTask = imageRef.putFile(uri);
            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@android.support.annotation.NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Log.d(TAG, "Error uploading image");
                    Toast.makeText(getApplicationContext(), "No se ha podido guardar la imagen", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    Log.d(TAG, "Image uploaded");
                    getURL(imageRef);
                }
            });
        }
    }
    public interface CustomCallBack {
        void onComplete();
    }
    private void getURL(StorageReference imageRef){
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri1) {
                mUvm.updateURL(uri1, mUser.getId(), new EditProfileActivity.CustomCallBack() {
                    @Override
                    public void onComplete() {
                        mUser.setUrl(String.valueOf(uri1));
                        paintImageView();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "Error uploading image");
                Toast.makeText(getApplicationContext(), "No se ha podido guardar la imagen", Toast.LENGTH_SHORT).show();

            }
        });
    }
    private void paintImageView(){
        if (mUser.getURL()!=null && !mUser.getURL().equals("")) {
            Picasso.get().load(mUser.getURL()).into(mImage);
        }
    }
    @Override
    public void onBackPressed(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("mUser", mUser);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

}