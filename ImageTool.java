package com.arashafsharpour.musicplatform.lib.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import id.zelory.compressor.Compressor;
import com.arashafsharpour.musicplatform.R;
import com.arashafsharpour.musicplatform.lib.activity.PermissionManager;
import com.arashafsharpour.musicplatform.lib.activity.ViewManager;
import com.arashafsharpour.musicplatform.lib.params.LinearParams;
import com.arashafsharpour.musicplatform.lib.params.RelativeParams;
import com.arashafsharpour.musicplatform.lib.ui.AppButton;
import com.arashafsharpour.musicplatform.lib.ui.dialog.AppAlertDialog;
import com.arashafsharpour.musicplatform.lib.ui.text.AppEditText;
import com.arashafsharpour.musicplatform.lib.ui.text.AppText;

public class ImageTool {

    private static final int CAMERA_REQUEST_CODE = 2099;
    private static final int GALLERY_REQUEST_CODE = 3099;

    private static final int CAPTION = +34675534;

    private static final ImageTool instance = new ImageTool();
    private static final String SUFFIX = ".jpg";

    private String selectedImagePath, caption;
    private ImageCallBack imageCallBack;
    private ImageView imageView;
    private boolean customView;
    private AppButton button;

    private static ImageTool getInstance() {
        return instance;
    }

    private ImageTool() {

    }

    public interface ImageCallBack {
        void onResult(String path, String caption);
    }

    public static void pickImage(final ViewManager context, ImageCallBack imageCallBack) {
        pickImage(context, imageCallBack, false);
    }

    public static void pickImage(final ViewManager context, ImageCallBack imageCallBack, boolean customView) {
        getInstance().imageCallBack = imageCallBack;
        getInstance().customView = customView;

        getInstance().selectedImagePath = null;
        getInstance().imageView = null;
        getInstance().caption = null;
        getInstance().button = null;

        AppAlertDialog dialog = new AppAlertDialog(context);
        dialog.setCanceledOnTouchOutside(true);
//        dialog.setImageResource(R.drawable.dialog_upload);
        dialog.setCancelable(true);
        dialog.setMessage("عکس مورد نظر را انتخاب کنید");
        dialog.setNeutralButton("بازگشت", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        if (customView) {
            dialog.setView(createView(context));
            dialog.setPositiveButton("ثبت", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (redirect(context)) {
                        dialog.dismiss();
                    }
                }
            });
        } else {
            dialog.setPositiveButton("دوربین", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    requestFromCamera(context);
                }
            });
            dialog.setNegativeButton("گالری", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    requestFromGallery(context);
                }
            });
        }
        dialog.show();
    }

    private static View createView(ViewManager context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(line(context));
        layout.addView(platform(context));
        layout.addView(line(context));
        layout.addView(field(context, CAPTION));
        layout.addView(line(context));
        return layout;
    }

    private static View field(ViewManager context, final int id) {
        final AppEditText editText = new AppEditText(context);
        editText.setId(id);
        editText.setTextColor(Color.DKGRAY);
        editText.setHintTextColor(Color.GRAY);
        editText.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        editText.setLayoutParams(LinearParams.get(-1, -2
                , new int[]{context.medium, 0, context.medium, 0}));
        editText.setTextSize(1, 12);
        editText.setBackgroundResource(R.color.transparent);
        editText.setSingleLine();
        editText.setHint("توضیحات (اختیاری)");
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    editText.setTextSize(1, 12);
                } else {
                    editText.setTextSize(1, 14);
                }
                if (id == CAPTION) {
                    getInstance().caption = s.toString();
                }
            }
        });
        return editText;
    }

    private static View line(ViewManager context) {
        View view = new View(context);
        view.setLayoutParams(LinearParams.get(-1, context.line));
        view.setBackgroundColor(Color.LTGRAY);
        return view;
    }

    private static View platform(ViewManager context) {
        RelativeLayout box = new RelativeLayout(context);
        box.setLayoutParams(LinearParams.get(-1, context.toPx(120)));
        box.addView(buttons(context));
        box.addView(banner(context));
        box.addView(delete(context));
        return box;
    }

    private static View delete(ViewManager context) {
        getInstance().button = new AppButton(context);
        getInstance().button.setLayoutParams(RelativeParams.get(context.toolbar_size, context.toolbar_size
                , RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_TOP));
//        getInstance().button.setImageResource(R.drawable.ic_close_dark);
        if (context.isMaterial) {
            getInstance().button.setElevation(5);
        }
        getInstance().button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getInstance().button.setVisibility(View.GONE);
                getInstance().imageView.setImageResource(R.color.transparent);
                getInstance().imageView.setBackgroundResource(R.color.transparent);
                getInstance().imageView.setClickable(false);
                getInstance().selectedImagePath = null;
            }
        });
        getInstance().button.setVisibility(View.GONE);
        return getInstance().button;
    }

    private static View banner(ViewManager context) {
        getInstance().imageView = new ImageView(context);
        getInstance().imageView.setLayoutParams(RelativeParams.get(-1, -1));
        getInstance().imageView.setImageResource(R.color.transparent);
        getInstance().imageView.setBackgroundResource(R.color.transparent);
        getInstance().imageView.setClickable(false);
        if (context.isMaterial) {
            getInstance().imageView.setElevation(3);
        }
        return getInstance().imageView;
    }

    private static View buttons(ViewManager context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setLayoutParams(RelativeParams.get(-1, -1));
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.addView(button(context, CAMERA_REQUEST_CODE));
        layout.addView(button(context, GALLERY_REQUEST_CODE));
        return layout;
    }

    private static View button(final ViewManager context, final int code) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(LinearParams.get(0, -2, 1f, Gravity.CENTER_VERTICAL));
        layout.setGravity(Gravity.CENTER);
        layout.addView(icon(context, code));
        layout.addView(text(context, code));
        if (!context.isMaterial) {
            layout.setBackgroundResource(context.getBackground());
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleClick(context, code);
                }
            });
        }
        return layout;
    }

    private static View text(ViewManager context, int code) {
        AppText text = new AppText(context);
        text.setSingleLine();
        text.setTextColor(Color.DKGRAY);
        text.setTextSize(1, 12);
        text.setLayoutParams(LinearParams.get(-2, -2, Gravity.CENTER));
        if (code == CAMERA_REQUEST_CODE) {
            text.setText("دوربین");
        } else {
            text.setText("گالری");
        }
        return text;
    }

    private static View icon(final ViewManager context, final int code) {
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(LinearParams.get(context.toolbar_size, context.toolbar_size, Gravity.CENTER));
        if (code == CAMERA_REQUEST_CODE) {
//            imageView.setImageResource(R.drawable.image_tool_camera);
        } else {
//            imageView.setImageResource(R.drawable.image_tool_gallery);
        }
        if (context.isMaterial) {
            imageView.setBackground(context.getWideRipple());
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleClick(context, code);
                }
            });
        }
        return imageView;
    }

    private static void handleClick(ViewManager context, int code) {
        if (code == CAMERA_REQUEST_CODE) {
            requestFromCamera(context);
        } else {
            requestFromGallery(context);
        }
    }

    private static void requestFromGallery(final ViewManager context) {
        context.requestPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                , new PermissionManager.PermissionCallBack() {
                    @Override
                    public void onGranted() {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        context.startActivityForResult(intent, GALLERY_REQUEST_CODE);
                    }

                    @Override
                    public void onDenied() {

                    }

                    @Override
                    public void onDismissed() {

                    }
                });
    }

    private static void requestFromCamera(final ViewManager context) {
        context.requestPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                , new PermissionManager.PermissionCallBack() {
                    @Override
                    public void onGranted() {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
                            File photoFile = createImageFile(context);
                            if (photoFile != null) {
                                Uri photoURI;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    photoURI = GenericFileProvider.getUriForFile(context
                                            , context.getPackageName() + ".provider", photoFile);
                                } else {
                                    photoURI = Uri.fromFile(photoFile);
                                }
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                context.startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
                                getInstance().selectedImagePath = photoFile.getAbsolutePath();
                            } else {
                                getInstance().selectedImagePath = null;
                                Toast.makeText(context, "اجازه دسترسی به دوربین ندارید", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onDenied() {

                    }

                    @Override
                    public void onDismissed() {

                    }
                });
    }

    private static File createImageFile(Context context) {
        File dir = new File(Environment.getExternalStorageDirectory()
                , context.getResources().getString(R.string.app_name));
        boolean isValid = true;
        if (!dir.exists()) {
            isValid = dir.mkdir();
        }
        if (isValid) {
            try {
                String timeStamp = String.valueOf(Calendar.getInstance().getTimeInMillis());
                return File.createTempFile(timeStamp, SUFFIX, dir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static String getPath(Uri uri, Activity context) {
        String result = null;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor == null) {
                result = uri.getPath();
            } else {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                result = cursor.getString(idx);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return result;
    }

    public static void handle(Activity context, int requestCode, int resultCode, Intent data) {
        if (requestCode != CAMERA_REQUEST_CODE && requestCode != GALLERY_REQUEST_CODE) {
            return;
        }
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && getInstance().selectedImagePath != null) {
                File image = new File(getInstance().selectedImagePath);
                if (image.exists()) {
                    getInstance().selectedImagePath = image.getAbsolutePath();
                } else {
                    getInstance().selectedImagePath = null;
                }
            } else {
                getInstance().selectedImagePath = null;
            }
        } else {
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                File image = new File(getPath(data.getData(), context));
                if (image.exists()) {
                    getInstance().selectedImagePath = image.getAbsolutePath();
                }
            }
        }
        if (getInstance().customView) {
            if (getInstance().imageView != null
                    && getInstance().button != null
                    && getInstance().selectedImagePath != null) {
                getInstance().imageView.setClickable(true);
                getInstance().imageView.setBackgroundResource(R.color.lite);
                getInstance().button.setVisibility(View.VISIBLE);
                try {
                    Picasso.with(context).load(new File(getInstance().selectedImagePath))
                            .fit().centerInside().into(getInstance().imageView);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } else {
            redirect(context);
        }
    }

    private static boolean redirect(Activity context) {
        if (getInstance().selectedImagePath != null
                && getInstance().imageCallBack != null) {
            compress(context);
            return true;
        } else {
            if (getInstance().customView) {
                Toast.makeText(context, "عکسی انتخاب نشده است", Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    private static void compress(final Context context) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg != null && msg.what == 5) {
                    if (getInstance().selectedImagePath != null
                            && getInstance().imageCallBack != null) {
                        if (new File(getInstance().selectedImagePath).exists()) {
                            getInstance().imageCallBack.onResult(getInstance().selectedImagePath
                                    , getInstance().caption);
                        }
                    }
                }
            }
        };
        new Thread() {
            @Override
            public void run() {
                try {
                    File file = new File(getInstance().selectedImagePath);
                    File dir = new File(Environment.getExternalStorageDirectory()
                            , context.getResources().getString(R.string.app_name));
                    File parent = new File(dir, "images");
                    if (!parent.exists()) {
                        if (!parent.mkdirs()) {
                            handler.sendEmptyMessage(5);
                            return;
                        }
                    }
                    Compressor compressor = new Compressor(context);
                    compressor.setQuality(90);
                    compressor.setMaxWidth(1500);
                    compressor.setMaxHeight(1500);
                    compressor.setCompressFormat(Bitmap.CompressFormat.JPEG);
                    compressor.setDestinationDirectoryPath(parent.getAbsolutePath());
                    compressor.compressToFile(file);
                    getInstance().selectedImagePath = parent.getAbsolutePath() + File.separator + file.getName();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(5);
            }
        }.start();
    }
}