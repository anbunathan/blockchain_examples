/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package org.tensorflow.lite.examples.facemqtt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import java.math.BigInteger;
import java.util.concurrent.TimeoutException;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/** Basic fragments for the Camera. */
public class Camera2BasicFragment extends Fragment
    implements ActivityCompat.OnRequestPermissionsResultCallback {

  /** Tag for the {@link Log}. */
  private static final String TAG = "TfLiteCameraDemo";

  private static final String FRAGMENT_DIALOG = "dialog";

  private static final String HANDLE_THREAD_NAME = "CameraBackground";

  private static final int PERMISSIONS_REQUEST_CODE = 1;
//  //IP address of miner1(Lenova)
//  private static String geth_url = "http://192.168.0.150:8042";
//  //Smart contract address built from Lenova
//  private static String CONTRACT_ADDRESS = "0xa5c53be143769d625c1185cc67c705f039a1f876";
//  //Private key of miner1 (Lenova)
//  private static String PRIVATE_KEY_GETH = "0x51f3dcb980c734f39f6d4dd001e3edb8c3c3da80691d62806c12369de09f7adb";
//  //RPI - coinbase public address
//  private static String recipient = "0x8f87dbf765be30e1bc65361c99252cd33b67bd0d";

  //GCP VM miner1
  private static String geth_url = "http://34.125.69.101:8042";
  //Smart contract address
  private static String CONTRACT_ADDRESS = "0xa5c53be143769d625c1185cc67c705f039a1f876";
  //private key for GCP VM miner1
  private static String PRIVATE_KEY_GETH = "0x2111fcd09cea159533555affc87fb024f7dc5917062e96ba475dbcc8c31867d9";
  //Account (coinbase) of RPI
  private static String recipient = "0x8f87dbf765be30e1bc65361c99252cd33b67bd0d";

  private static Web3j web3j = null;
  private static Credentials credentials = Credentials.create(PRIVATE_KEY_GETH);

  private final Object lock = new Object();
  private boolean runClassifier = false;
  private boolean checkedPermissions = false;
  private TextView textView;
  private ToggleButton toggle;
  private NumberPicker np;
  private ImageClassifier classifier;
  private LinearLayout upLayout,
      downLayout;
//      leftLayout,
//      rightLayout,
//      leftClickLayout,
//      rightClickLayout,
//      scrollUpLayout,
//      scrollDownLayout;
  private BottomSheetBehavior<LinearLayout> sheetBehavior;
  private LinearLayout bottomSheetLayout;
  private LinearLayout gestureLayout;
  private TextView resultTextView;
  /** Max preview width that is guaranteed by Camera2 API */
  private static final int MAX_PREVIEW_WIDTH = 640;

  /** Max preview height that is guaranteed by Camera2 API */
  private static final int MAX_PREVIEW_HEIGHT = 480;

  private String lastSelectedGesture;
  MqttAndroidClient client = null;
  boolean validMQTT=false;
  boolean allowStateChange = true;
  Timer timer = new Timer();
  boolean stateChanged = false;
  String lastState = "close";
  boolean initialCondition = true;

  /**
   * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a {@link
   * TextureView}.
   */
  private final TextureView.SurfaceTextureListener surfaceTextureListener =
      new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
          openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
          configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
          return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {}
      };

  /** ID of the current {@link CameraDevice}. */
  private String cameraId;

  /** An {@link AutoFitTextureView} for camera preview. */
  private AutoFitTextureView textureView;

  /** A {@link CameraCaptureSession } for camera preview. */
  private CameraCaptureSession captureSession;

  /** A reference to the opened {@link CameraDevice}. */
  private CameraDevice cameraDevice;

  /** The {@link android.util.Size} of camera preview. */
  private Size previewSize;

  /** {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state. */
  private final CameraDevice.StateCallback stateCallback =
      new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice currentCameraDevice) {
          // This method is called when the camera is opened.  We start camera preview here.
          cameraOpenCloseLock.release();
          cameraDevice = currentCameraDevice;
          createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice currentCameraDevice) {
          cameraOpenCloseLock.release();
          currentCameraDevice.close();
          cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice currentCameraDevice, int error) {
          cameraOpenCloseLock.release();
          currentCameraDevice.close();
          cameraDevice = null;
          Activity activity = getActivity();
          if (null != activity) {
            activity.finish();
          }
        }
      };

  /** An additional thread for running tasks that shouldn't block the UI. */
  private HandlerThread backgroundThread;

  /** A {@link Handler} for running tasks in the background. */
  private Handler backgroundHandler;

  /** An {@link ImageReader} that handles image capture. */
  private ImageReader imageReader;

  /** {@link CaptureRequest.Builder} for the camera preview */
  private CaptureRequest.Builder previewRequestBuilder;

  /** {@link CaptureRequest} generated by {@link #previewRequestBuilder} */
  private CaptureRequest previewRequest;

  /** A {@link Semaphore} to prevent the app from exiting before closing the camera. */
  private Semaphore cameraOpenCloseLock = new Semaphore(1);

  /** A {@link CameraCaptureSession.CaptureCallback} that handles events related to capture. */
  private CameraCaptureSession.CaptureCallback captureCallback =
      new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureProgressed(
            @NonNull CameraCaptureSession session,
            @NonNull CaptureRequest request,
            @NonNull CaptureResult partialResult) {}

        @Override
        public void onCaptureCompleted(
            @NonNull CameraCaptureSession session,
            @NonNull CaptureRequest request,
            @NonNull TotalCaptureResult result) {}
      };

  /**
   * Shows a {@link Toast} on the UI thread for the classification results.
   *
   *
   */
  private void showToast(String s) {
    SpannableStringBuilder builder = new SpannableStringBuilder();
    SpannableString str1 = new SpannableString(s);
    builder.append(str1);
    showToast(builder);
  }

  private void showToast(SpannableStringBuilder builder) {
    final Activity activity = getActivity();
    if (activity != null) {
      activity.runOnUiThread(
          new Runnable() {
            @Override
            public void run() {
              textView.setText(builder, TextView.BufferType.SPANNABLE);
              resultTextView.setText(builder, TextView.BufferType.SPANNABLE);
            }
          });
    }
  }

  /**
   * Resizes image.
   *
   * <p>Attempting to use too large a preview size could exceed the camera bus' bandwidth
   * limitation, resulting in gorgeous previews but the storage of garbage capture data.
   *
   * <p>Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
   * is at least as large as the respective texture view size, and that is at most as large as the
   * respective max size, and whose aspect ratio matches with the specified value. If such size
   * doesn't exist, choose the largest one that is at most as large as the respective max size, and
   * whose aspect ratio matches with the specified value.
   *
   * @param choices The list of sizes that the camera supports for the intended output class
   * @param textureViewWidth The width of the texture view relative to sensor coordinate
   * @param textureViewHeight The height of the texture view relative to sensor coordinate
   * @param maxWidth The maximum width that can be chosen
   * @param maxHeight The maximum height that can be chosen
   * @param aspectRatio The aspect ratio
   * @return The optimal {@code Size}, or an arbitrary one if none were big enough
   */
  private static Size chooseOptimalSize(
      Size[] choices,
      int textureViewWidth,
      int textureViewHeight,
      int maxWidth,
      int maxHeight,
      Size aspectRatio) {

    // Collect the supported resolutions that are at least as big as the preview Surface
    List<Size> bigEnough = new ArrayList<>();
    // Collect the supported resolutions that are smaller than the preview Surface
    List<Size> notBigEnough = new ArrayList<>();
    int w = aspectRatio.getWidth();
    int h = aspectRatio.getHeight();
    for (Size option : choices) {
      if (option.getWidth() <= maxWidth
          && option.getHeight() <= maxHeight
          && option.getHeight() == option.getWidth() * h / w) {
        if (option.getWidth() >= textureViewWidth && option.getHeight() >= textureViewHeight) {
          bigEnough.add(option);
        } else {
          notBigEnough.add(option);
        }
      }
    }

    // Pick the smallest of those big enough. If there is no one big enough, pick the
    // largest of those not big enough.
    if (bigEnough.size() > 0) {
      return Collections.min(bigEnough, new CompareSizesByArea());
    } else if (notBigEnough.size() > 0) {
      return Collections.max(notBigEnough, new CompareSizesByArea());
    } else {
      Log.e(TAG, "Couldn't find any suitable preview size");
      return choices[0];
    }
  }

  public static Camera2BasicFragment newInstance() {
    return new Camera2BasicFragment();
  }

  /** Layout the preview and buttons. */
  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    return inflater.inflate(R.layout.fragment_camera2_basic, container, false);
  }

  /** Connect the buttons to their event handler. */
  @Override
  public void onViewCreated(final View view, Bundle savedInstanceState) {
    textureView = (AutoFitTextureView) view.findViewById(R.id.texture);
    textView = (TextView) view.findViewById(R.id.text);
    toggle = (ToggleButton) view.findViewById(R.id.button);

    toggle.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            classifier.setUseNNAPI(isChecked);
          }
        });

    np = (NumberPicker) view.findViewById(R.id.np);
    np.setMinValue(1);
    np.setMaxValue(10);
    np.setWrapSelectorWheel(true);
    np.setOnValueChangedListener(
        new NumberPicker.OnValueChangeListener() {
          @Override
          public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            classifier.setNumThreads(newVal);
          }
        });

    resultTextView = view.findViewById(R.id.result_text_view);
    bottomSheetLayout = view.findViewById(R.id.bottom_sheet_layout);
    gestureLayout = view.findViewById(R.id.gesture_layout);
    sheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);

    ViewTreeObserver vto = gestureLayout.getViewTreeObserver();
    vto.addOnGlobalLayoutListener(
        new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
              gestureLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            } else {
              gestureLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
            //                int width = bottomSheetLayout.getMeasuredWidth();
            int height = gestureLayout.getMeasuredHeight();

            sheetBehavior.setPeekHeight(height);
          }
        });
    sheetBehavior.setHideable(false);

    upLayout = view.findViewById(R.id.open_layout);
    downLayout = view.findViewById(R.id.close_layout);
//    leftLayout = view.findViewById(R.id.none_layout);
//    rightLayout = view.findViewById(R.id.right_layout);
//    leftClickLayout = view.findViewById(R.id.left_click_layout);
//    rightClickLayout = view.findViewById(R.id.right_click_layout);
//    scrollUpLayout = view.findViewById(R.id.scroll_up_layout);
//    scrollDownLayout = view.findViewById(R.id.scroll_down_layout);

    sheetBehavior.setBottomSheetCallback(
        new BottomSheetBehavior.BottomSheetCallback() {
          @Override
          public void onStateChanged(@NonNull View bottomSheet, int newState) {
            switch (newState) {
              case BottomSheetBehavior.STATE_HIDDEN:
                break;
              case BottomSheetBehavior.STATE_EXPANDED:
                {
                }
                break;
              case BottomSheetBehavior.STATE_COLLAPSED:
                {
                }
                break;
              case BottomSheetBehavior.STATE_DRAGGING:
                break;
              case BottomSheetBehavior.STATE_SETTLING:
                break;
            }
          }

          @Override
          public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });

    enableDisableButtons();
  }

  private String initializeWeb3J(){
    String result;
    String account;
    try {
      HttpService gethHttpService = new HttpService(geth_url);
      web3j = Web3j.build(gethHttpService);
      result = "web3j is successfully initialized";
      Log.e("init", "web3j is successfully initialized...");
    }catch(Exception e){
      e.printStackTrace();
      result =  "Error initializing web3j/infura";
      Log.e("init", "Error in web3j initialization!!!");
    }
    return result;
  }

  private BigInteger getGasPrice(){
    BigInteger gasPriceWei = BigInteger.valueOf(1000000001);
    return gasPriceWei;
  }

  private BigInteger getGasLimit(){
    BigInteger gasLimit = BigInteger.valueOf(80000);
    return gasLimit;
  }

  private String withdrawFromContract() throws ExecutionException, InterruptedException, TimeoutException {
    int greetingToWrite = 10;
    BigInteger tokentowithdraw = BigInteger.valueOf(greetingToWrite);
    String result = "Failure...";
    TransactionReceipt transactionReceipt;
    BigInteger existingTokens = getTokens();
    int tokens = (existingTokens).intValue();
    if(tokens>0) {
      try {
        SmartToken_sol_SmartToken greeter = SmartToken_sol_SmartToken.load(CONTRACT_ADDRESS, web3j, credentials, getGasPrice(), getGasLimit());
        transactionReceipt = greeter.withdrawToken(recipient, tokentowithdraw).sendAsync().get(3, TimeUnit.MINUTES);
        result = "Successful transaction. Gas used: " + transactionReceipt.getGasUsed();
        Log.e("amlan", "Successful transaction. Gas used: " + transactionReceipt.getGasUsed());
      } catch (Exception e) {
        e.printStackTrace();
        result = "Error during transaction. Error " + e.getMessage();
        Log.e("amlan", "Error in withdrawFromContract. Error " + e.getMessage());
      }
    }
    return result;
  }

  private String depositToContract() throws ExecutionException, InterruptedException, TimeoutException {
    int greetingToWrite = 10;
    BigInteger tokentodeposit = BigInteger.valueOf(greetingToWrite);
    String result = "failure";
    TransactionReceipt transactionReceipt;
    BigInteger existingTokens = getTokens();
    int tokens = (existingTokens).intValue();
    if(tokens==0){
      try {
        SmartToken_sol_SmartToken greeter = SmartToken_sol_SmartToken.load(CONTRACT_ADDRESS, web3j, credentials, getGasPrice(), getGasLimit());
        transactionReceipt = greeter.depositToken(recipient, tokentodeposit).sendAsync().get(3, TimeUnit.MINUTES);
        result = "Successful transaction. Gas used: "+transactionReceipt.getGasUsed();
        Log.e("amlan", "Successful transaction. Gas used: " + transactionReceipt.getGasUsed());
      } catch(Exception e){
        e.printStackTrace();
        result = "Error during transaction. Error "+e.getMessage();
        Log.e("amlan", "Error in depositToContract. Error " + e.getMessage());
      }
    }    
    return result;
  }

  private BigInteger getTokens() throws ExecutionException, InterruptedException, TimeoutException {
    String result;
    BigInteger tokens = null;
    try {
      SmartToken_sol_SmartToken greeter = SmartToken_sol_SmartToken.load(CONTRACT_ADDRESS, web3j, credentials, getGasPrice(), getGasLimit());
      tokens = greeter.getTokens(recipient).sendAsync().get(3, TimeUnit.MINUTES);
      result = "Tokens in contract: "+tokens;
      Log.e("amlan", "Tokens in contract: " + tokens);
    } catch(Exception e){
      e.printStackTrace();
      result = "Error during transaction. Error "+e.getMessage();
      Log.e("amlan", "Error in getTokens. Error " + e.getMessage());
    }
    return tokens;
  }

  private void enableDisableButtons() {
    Log.e("amlan", "Connect with MQTT");
    connect();
    String content = null;

    try {
      InputStream inputStream = getActivity().getAssets().open("labels.txt");

      int size = inputStream.available();
      byte[] buffer = new byte[size];
      inputStream.read(buffer);
      inputStream.close();
      content = new String(buffer);
    } catch (IOException e) {
      e.printStackTrace();
    }
    Log.e("amlan", "content = "+content);
    if (content == null) return;

    upLayout.setEnabled(false);
    downLayout.setEnabled(false);
//    leftLayout.setEnabled(false);
//    rightLayout.setEnabled(false);
//    leftClickLayout.setEnabled(false);
//    rightClickLayout.setEnabled(false);
//    scrollUpLayout.setEnabled(false);
//    scrollDownLayout.setEnabled(false);

    upLayout.setAlpha(0.4f);
    downLayout.setAlpha(0.4f);
//    leftLayout.setAlpha(0.4f);
//    rightLayout.setAlpha(0.4f);
//    leftClickLayout.setAlpha(0.4f);
//    rightClickLayout.setAlpha(0.4f);
//    scrollUpLayout.setAlpha(0.4f);
//    scrollDownLayout.setAlpha(0.4f);

    StringTokenizer tokenizer = new StringTokenizer(content, "\n");
    Log.e("amlan", "tokenizer = "+tokenizer);
    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();
      Log.e("amlan", "tokenizer token = "+token);
//      if (token.contains("leftclick")) {
//        leftClickLayout.setEnabled(true);
//        leftClickLayout.setAlpha(1.0f);
//        Log.e("amlan", "leftclickLayout enabled ");
//      } else if (token.contains("rightclick")) {
//        rightClickLayout.setEnabled(true);
//        rightClickLayout.setAlpha(1.0f);
//        Log.e("amlan", "rightclickLayout enabled ");
//      } else if (token.contains("scrollup")) {
//        scrollUpLayout.setEnabled(true);
//        scrollUpLayout.setAlpha(1.0f);
//        Log.e("amlan", "scrollupLayout enabled ");
////      } else if (token.equalsIgnoreCase("scrolldown")) {
//      } else if (token.contains("scrolldown")) {
//        scrollDownLayout.setEnabled(true);
//        scrollDownLayout.setAlpha(1.0f);
//        Log.e("amlan", "scrolldownLayout enabled ");
//      }else if (token.contains("up")) {
      if (token.contains("open")) {
//      if (token.equalsIgnoreCase("up")) {
        upLayout.setEnabled(true);
        upLayout.setAlpha(1.0f);
        Log.e("amlan", "upLayout enabled ");
//      } else if (token.equalsIgnoreCase("down")) {
      } else if (token.contains("close")) {
        downLayout.setEnabled(true);
        downLayout.setAlpha(1.0f);
        Log.e("amlan", "downLayout enabled ");
//      } else if (token.equalsIgnoreCase("left")) {
      }
//      else if (token.contains("none")) {
//        leftLayout.setEnabled(true);
//        leftLayout.setAlpha(1.0f);
//        Log.e("amlan", "leftLayout enabled ");
////      } else if (token.equalsIgnoreCase("right")) {
//      }
//      else if (token.contains("right")) {
//        rightLayout.setEnabled(true);
//        rightLayout.setAlpha(1.0f);
//        Log.e("amlan", "rightLayout enabled ");
////      } else if (token.equalsIgnoreCase("leftclick")) {
//
//      }
    }
  }

  /** Load the model and labels. */
  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    try {
      // create either a new ImageClassifierQuantizedMobileNet or an ImageClassifierFloatInception
      //      classifier = new ImageClassifierQuantizedMobileNet(getActivity());
      classifier = new ImageClassifierFloatInception(getActivity());
    } catch (IOException e) {
      Log.e(TAG, "Failed to initialize an image classifier.", e);
    }

    startBackgroundThread();
  }

  @Override
  public void onResume() {
    super.onResume();
    startBackgroundThread();

    // When the screen is turned off and turned back on, the SurfaceTexture is already
    // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
    // a camera and start preview from here (otherwise, we wait until the surface is ready in
    // the SurfaceTextureListener).
    if (textureView.isAvailable()) {
      openCamera(textureView.getWidth(), textureView.getHeight());
    } else {
      textureView.setSurfaceTextureListener(surfaceTextureListener);
    }
  }

  @Override
  public void onPause() {
    closeCamera();
    stopBackgroundThread();
    super.onPause();
  }

  @Override
  public void onDestroy() {
    classifier.close();
    super.onDestroy();
  }

  /**
   * Sets up member variables related to camera.
   *
   * @param width The width of available size for camera preview
   * @param height The height of available size for camera preview
   */
  private void setUpCameraOutputs(int width, int height) {
    Activity activity = getActivity();
    CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
    try {
      String camId = chooseCamera(manager);
      // Front and back camera is not present or not accessible
      if (camId == null) {
        throw new IllegalStateException("Camera Not Found");
      }
      CameraCharacteristics characteristics = manager.getCameraCharacteristics(camId);

      StreamConfigurationMap map =
          characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

      // // For still image captures, we use the largest available size.
      Size largest =
          Collections.max(
              Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizesByArea());
      imageReader =
          ImageReader.newInstance(
              largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, /*maxImages*/ 2);

      // Find out if we need to swap dimension to get the preview size relative to sensor
      // coordinate.
      int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
      // noinspection ConstantConditions
      /* Orientation of the camera sensor */
      int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
      boolean swappedDimensions = false;
      switch (displayRotation) {
        case Surface.ROTATION_0:
        case Surface.ROTATION_180:
          if (sensorOrientation == 90 || sensorOrientation == 270) {
            swappedDimensions = true;
          }
          break;
        case Surface.ROTATION_90:
        case Surface.ROTATION_270:
          if (sensorOrientation == 0 || sensorOrientation == 180) {
            swappedDimensions = true;
          }
          break;
        default:
          Log.e(TAG, "Display rotation is invalid: " + displayRotation);
      }

      Point displaySize = new Point();
      activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
      int rotatedPreviewWidth = width;
      int rotatedPreviewHeight = height;
      int maxPreviewWidth = displaySize.x;
      int maxPreviewHeight = displaySize.y;

      if (swappedDimensions) {
        rotatedPreviewWidth = height;
        rotatedPreviewHeight = width;
        maxPreviewWidth = displaySize.y;
        maxPreviewHeight = displaySize.x;
      }

      if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
        maxPreviewWidth = MAX_PREVIEW_WIDTH;
      }

      if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
        maxPreviewHeight = MAX_PREVIEW_HEIGHT;
      }

      previewSize =
          chooseOptimalSize(
              map.getOutputSizes(SurfaceTexture.class),
              rotatedPreviewWidth,
              rotatedPreviewHeight,
              maxPreviewWidth,
              maxPreviewHeight,
              largest);

      // We fit the aspect ratio of TextureView to the size of preview we picked.
      int orientation = getResources().getConfiguration().orientation;
      if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        textureView.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
      } else {
        textureView.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
      }

      this.cameraId = camId;
    } catch (CameraAccessException e) {
      Log.e(TAG, "Failed to access Camera", e);
    } catch (NullPointerException e) {
      // Currently an NPE is thrown when the Camera2API is used but not supported on the
      // device this code runs.
      ErrorDialog.newInstance(getString(R.string.camera_error))
          .show(getChildFragmentManager(), FRAGMENT_DIALOG);
    }
  }

  /**
   * Choose the Camera from the list of available cameras. Priority goes to front camera, if its
   * present then use the front camera, else switch to back camera.
   *
   * @param manager CameraManager
   * @return ID of the Camera
   * @throws CameraAccessException
   */
  private String chooseCamera(CameraManager manager) throws CameraAccessException {
    String frontCameraId = null;
    String backCameraId = null;
    if (manager != null && manager.getCameraIdList().length > 0) {
      for (String camId : manager.getCameraIdList()) {
        CameraCharacteristics characteristics = manager.getCameraCharacteristics(camId);
        StreamConfigurationMap map =
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
        if (facing != null && map != null) {
          if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
            frontCameraId = camId;
            break;
          } else if (facing == CameraCharacteristics.LENS_FACING_BACK) {
            backCameraId = camId;
          }
        }
      }

      return frontCameraId != null ? frontCameraId : backCameraId;
//      return backCameraId;
    }
    return null;
  }

  private String[] getRequiredPermissions() {
    return new String[] {Manifest.permission.CAMERA};
  }

  /** Opens the camera specified by {@link Camera2BasicFragment#cameraId}. */
  @SuppressLint("MissingPermission")
  private void openCamera(int width, int height) {
    if (!checkedPermissions && !allPermissionsGranted()) {
      requestPermissions(getRequiredPermissions(), PERMISSIONS_REQUEST_CODE);
      return;
    } else {
      checkedPermissions = true;
    }
    setUpCameraOutputs(width, height);

    if (cameraId == null) {
      throw new IllegalStateException("No front camera available.");
    }

    configureTransform(width, height);
    Activity activity = getActivity();
    CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
    try {
      if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
        throw new RuntimeException("Time out waiting to lock camera opening.");
      }

      if (!allPermissionsGranted()) {
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        return;
      }

      manager.openCamera(cameraId,
              stateCallback,
              backgroundHandler);
    } catch (CameraAccessException e) {
      Log.e(TAG, "Failed to open Camera", e);
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
    }
  }

  private boolean allPermissionsGranted() {
    for (String permission : getRequiredPermissions()) {
      if (getContext().checkPermission(permission, Process.myPid(), Process.myUid())
          != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  /** Closes the current {@link CameraDevice}. */
  private void closeCamera() {
    try {
      cameraOpenCloseLock.acquire();
      if (null != captureSession) {
        captureSession.close();
        captureSession = null;
      }
      if (null != cameraDevice) {
        cameraDevice.close();
        cameraDevice = null;
      }
      if (null != imageReader) {
        imageReader.close();
        imageReader = null;
      }
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
    } finally {
      cameraOpenCloseLock.release();
    }
  }

  /** Starts a background thread and its {@link Handler}. */
  private void startBackgroundThread() {
    backgroundThread = new HandlerThread(HANDLE_THREAD_NAME);
    backgroundThread.start();
    backgroundHandler = new Handler(backgroundThread.getLooper());
    synchronized (lock) {
      runClassifier = true;
    }
    backgroundHandler.post(periodicClassify);
  }

  /** Stops the background thread and its {@link Handler}. */
  private void stopBackgroundThread() {
    backgroundThread.quitSafely();
    try {
      backgroundThread.join();
      backgroundThread = null;
      backgroundHandler = null;
      synchronized (lock) {
        runClassifier = false;
      }
    } catch (InterruptedException e) {
      Log.e(TAG, "Interrupted when stopping background thread", e);
    }
  }

  /** Takes photos and classify them periodically. */
  private Runnable periodicClassify =
      new Runnable() {
        @Override
        public void run() {
          synchronized (lock) {
            if (runClassifier) {
              classifyFrame();
            }
          }
          backgroundHandler.post(periodicClassify);
        }
      };

  /** Creates a new {@link CameraCaptureSession} for camera preview. */
  private void createCameraPreviewSession() {
    try {
      SurfaceTexture texture = textureView.getSurfaceTexture();
      assert texture != null;

      // We configure the size of default buffer to be the size of camera preview we want.
      texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

      // This is the output Surface we need to start preview.
      Surface surface = new Surface(texture);

      // We set up a CaptureRequest.Builder with the output Surface.
      previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
      previewRequestBuilder.addTarget(surface);

      // Here, we create a CameraCaptureSession for camera preview.
      cameraDevice.createCaptureSession(
          Arrays.asList(surface),
          new CameraCaptureSession.StateCallback() {

            @Override
            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
              // The camera is already closed
              if (null == cameraDevice) {
                return;
              }

              // When the session is ready, we start displaying the preview.
              captureSession = cameraCaptureSession;
              try {
                // Auto focus should be continuous for camera preview.
                previewRequestBuilder.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                // Finally, we start displaying the camera preview.
                previewRequest = previewRequestBuilder.build();
                captureSession.setRepeatingRequest(
                    previewRequest, captureCallback, backgroundHandler);
              } catch (CameraAccessException e) {
                Log.e(TAG, "Failed to set up config to capture Camera", e);
              }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
              showToast("Failed");
            }
          },
          null);
    } catch (CameraAccessException e) {
      Log.e(TAG, "Failed to preview Camera", e);
    }
  }

  /**
   * Configures the necessary {@link android.graphics.Matrix} transformation to `textureView`. This
   * method should be called after the camera preview size is determined in setUpCameraOutputs and
   * also the size of `textureView` is fixed.
   *
   * @param viewWidth The width of `textureView`
   * @param viewHeight The height of `textureView`
   */
  private void configureTransform(int viewWidth, int viewHeight) {
    Activity activity = getActivity();
    if (null == textureView || null == previewSize || null == activity) {
      return;
    }
    int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
    Matrix matrix = new Matrix();
    RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
    RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
    float centerX = viewRect.centerX();
    float centerY = viewRect.centerY();
    if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
      bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
      matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
      float scale =
          Math.max(
              (float) viewHeight / previewSize.getHeight(),
              (float) viewWidth / previewSize.getWidth());
      matrix.postScale(scale, scale, centerX, centerY);
      matrix.postRotate(90 * (rotation - 2), centerX, centerY);
    } else if (Surface.ROTATION_180 == rotation) {
      matrix.postRotate(180, centerX, centerY);
    }
    textureView.setTransform(matrix);
  }

  /** Classifies a frame from the preview stream. */
  private void classifyFrame() {

    if (classifier == null || getActivity() == null || cameraDevice == null) {
      showToast("Uninitialized Classifier or invalid context.");
      return;
    }
    SpannableStringBuilder textToShow = new SpannableStringBuilder();

    Bitmap origionalBitmal = textureView.getBitmap();

    Bitmap bitmap = ThumbnailUtils.extractThumbnail(origionalBitmal, 224, 224);
//    Bitmap bitmap = ThumbnailUtils.extractThumbnail(origionalBitmal, 128, 128);

    //        Bitmap bitmap = textureView.getBitmap(classifier.getImageSizeX(),
    // classifier.getImageSizeY());
    classifier.classifyFrame(bitmap, textToShow);
    bitmap.recycle();
    Log.e("amlan", "Running in Loop");
    Log.e("amlan", textToShow.toString());

    if (textToShow.toString().indexOf(":") != -1) {
      String token = textToShow.toString().substring(0, textToShow.toString().indexOf(":"));
      String probabilityStr = textToShow.toString().substring(textToShow.toString().indexOf(":") + 1,
              textToShow.toString().indexOf(":") + 7);
      Log.e("amlan", "probabilityStr = " + probabilityStr.trim());
      double probability = 0.0;
      try {
        probability = Double.parseDouble(probabilityStr.trim());
      } catch (NumberFormatException e) {
        Log.e("amlan", "numberStr is not a number");
      }
      Log.e("amlan", "probability = " + probability);
      Log.e("amlan", "token = " + token);
      Activity activity = getActivity();
      double finalProbability = probability;
      String finalToken = token;
//      if (probability < 0.95) {
//        finalToken = "CLOSE";
//      }

      if(stateChanged == false) {
        if (token.contains("open") && probability >= 0.95) {
          if (lastState.contains("close")) {
            lastState = "open";
            finalToken = "OPEN";
            allowStateChange = false;
            stateChanged = true;
            try {
              String finalToken1 = finalToken;
              activity.runOnUiThread(
                      new Runnable() {
                        @Override
                        public void run() {
                          highLightDirectionButton(finalToken1);
                        }
                      });
//              depositToContract();
              if(validMQTT==true) {
                publish(client, finalToken);
              }
            }catch(Exception e){
              e.printStackTrace();
            }
          }
        }
        else {
          if (lastState.contains("open")) {
            lastState = "close";
            finalToken = "CLOSE";
            allowStateChange = false;
            stateChanged = true;
            try {
              String finalToken1 = finalToken;
              activity.runOnUiThread(
                      new Runnable() {
                        @Override
                        public void run() {
                          highLightDirectionButton(finalToken1);
                        }
                      });
//              withdrawFromContract();
              if(validMQTT==true) {
                publish(client, finalToken);
              }
            }catch(Exception e){
              e.printStackTrace();
            }
          }
          if(initialCondition == true){
            initialCondition = false;
            allowStateChange = true;
            lastState = "close";
            finalToken = "CLOSE";
            String finalToken1 = finalToken;
            activity.runOnUiThread(
                    new Runnable() {
                      @Override
                      public void run() {
                        highLightDirectionButton(finalToken1);
                      }
                    });
//            initializeWeb3J();
            try {
//              withdrawFromContract();
            }catch(Exception e){
              e.printStackTrace();
            }
          }
        }
      }
      if(stateChanged == true){
        try {
          new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
              // this code will be executed after 10 seconds
              allowStateChange = true;
              stateChanged = false;
            }
          }, 2000);
        }catch(Exception e){
          e.printStackTrace();
        }
      }
    }
    showToast(textToShow);
  }

  private void highLightDirectionButton(String token) {

    if (lastSelectedGesture != null && !token.equalsIgnoreCase(lastSelectedGesture)) {


      if (lastSelectedGesture.equalsIgnoreCase("OPEN") ) {
        upLayout.setBackgroundResource(R.drawable.base);
      } else if (lastSelectedGesture.equalsIgnoreCase("CLOSE") ) {
        downLayout.setBackgroundResource(R.drawable.base);
      }
//      else if (lastSelectedGesture.equalsIgnoreCase("NONE")) {
//        leftLayout.setBackgroundResource(R.drawable.base);
//      }
//      else{
//        leftLayout.setBackgroundResource(R.drawable.base);
//      }
//      else if (lastSelectedGesture.equalsIgnoreCase("RIGHT")) {
//        rightLayout.setBackgroundResource(R.drawable.base);
//      } else if (lastSelectedGesture.equalsIgnoreCase("LEFTCLICK")) {
//        leftClickLayout.setBackgroundResource(R.drawable.base);
//      } else if (lastSelectedGesture.equalsIgnoreCase("RIGHTCLICK")) {
//        rightClickLayout.setBackgroundResource(R.drawable.base);
//      } else if (lastSelectedGesture.equalsIgnoreCase("SCROLLUP")) {
//        scrollUpLayout.setBackgroundResource(R.drawable.base);
//      } else if (lastSelectedGesture.equalsIgnoreCase("SCROLLDOWN")) {
//        scrollDownLayout.setBackgroundResource(R.drawable.base);
//      }
    }

    if (true || lastSelectedGesture == null || !lastSelectedGesture.equalsIgnoreCase(token)) {

      if (token.equalsIgnoreCase("OPEN")) {
        if (upLayout.isEnabled()) upLayout.setBackgroundResource(R.drawable.selection_base);
      } else if (token.equalsIgnoreCase("CLOSE")) {
        if (downLayout.isEnabled()) downLayout.setBackgroundResource(R.drawable.selection_base);
      }
//      else if (token.equalsIgnoreCase("NONE")) {
//        if (leftLayout.isEnabled()) leftLayout.setBackgroundResource(R.drawable.selection_base);
//      }
//      else if (token.equalsIgnoreCase("RIGHT")) {
//        if (rightLayout.isEnabled()) rightLayout.setBackgroundResource(R.drawable.selection_base);
//      } else if (token.equalsIgnoreCase("LEFTCLICK")) {
//        if (leftClickLayout.isEnabled())
//          leftClickLayout.setBackgroundResource(R.drawable.selection_base);
//      } else if (token.equalsIgnoreCase("RIGHTCLICK")) {
//        if (rightClickLayout.isEnabled())
//          rightClickLayout.setBackgroundResource(R.drawable.selection_base);
//      } else if (token.equalsIgnoreCase("SCROLLUP")) {
//        if (scrollUpLayout.isEnabled())
//          scrollUpLayout.setBackgroundResource(R.drawable.selection_base);
//      } else if (token.equalsIgnoreCase("SCROLLDOWN")) {
//        if (scrollDownLayout.isEnabled())
//          scrollDownLayout.setBackgroundResource(R.drawable.selection_base);
//      }

      lastSelectedGesture = token;
    }
  }

  /** Compares two {@code Size}s based on their areas. */
  private static class CompareSizesByArea implements Comparator<Size> {

    @Override
    public int compare(Size lhs, Size rhs) {
      // We cast here to ensure the multiplications won't overflow
      return Long.signum(
          (long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
    }
  }

  /** Shows an error message dialog. */
  public static class ErrorDialog extends DialogFragment {

    private static final String ARG_MESSAGE = "message";

    public static ErrorDialog newInstance(String message) {
      ErrorDialog dialog = new ErrorDialog();
      Bundle args = new Bundle();
      args.putString(ARG_MESSAGE, message);
      dialog.setArguments(args);
      return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      final Activity activity = getActivity();
      return new AlertDialog.Builder(activity)
          .setMessage(getArguments().getString(ARG_MESSAGE))
          .setPositiveButton(
              android.R.string.ok,
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                  activity.finish();
                }
              })
          .create();
    }
  }

  public void connect(){
    Log.e("file", "Start Connecting with MQTT");
    String clientId = MqttClient.generateClientId();
//    final MqttAndroidClient client =

//    client = new MqttAndroidClient(getActivity().getApplicationContext(), "tcp://sphinx-mqtt.tk:1883",	clientId);
    client = new MqttAndroidClient(getActivity().getApplicationContext(), "tcp://broker.hivemq.com:1883",	clientId);
    MqttConnectOptions options = new MqttConnectOptions();
    options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
    options.setCleanSession(false);
//        options.setUserName("no");
//        options.setPassword("no".toCharArray());
    try {
      IMqttToken token = client.connect(options);
      Log.e("file", "Set action listener callback for MQTT");
      //IMqttToken token = client.connect();
      token.setActionCallback(new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
          // We are connected
          Log.e("file", "onSuccess");
          validMQTT=true;
          //publish(client,"payloadd");
          subscribe(client,"dht");
          subscribe(client,"bmp");
          client.setCallback(new MqttCallback() {
            //            TextView tt = (TextView) findViewById(R.id.tt);
//            TextView th = (TextView) findViewById(R.id.th);
            @Override
            public void connectionLost(Throwable cause) {

            }
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
              Log.e("file", message.toString());

              if (topic.equals("dht")){
//                tt.setText(message.toString());
                Log.e("file", "dht received");
              }

              if (topic.equals("bmp")){
//                th.setText(message.toString());
                Log.e("file", "bmp received");
              }

            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
          });
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
          // Something went wrong e.g. connection timeout or firewall problems
          Log.e("file", "onFailure");
          validMQTT=false;

        }
      });
    } catch (MqttException e) {
      Log.d("amlan", "MQTT Exception");
      e.printStackTrace();
    }
  }

  public void publish(MqttAndroidClient client, String payload){
    String topic = "gesture";
    byte[] encodedPayload = new byte[0];
    try {
      encodedPayload = payload.getBytes("UTF-8");
      MqttMessage message = new MqttMessage(encodedPayload);
      client.publish(topic, message);
      Log.e("file", "published message = "+message);
    } catch (UnsupportedEncodingException | MqttException e) {
      e.printStackTrace();
    }
  }

  public void subscribe(MqttAndroidClient client , String topic){
    int qos = 1;
    try {
      IMqttToken subToken = client.subscribe(topic, qos);
      subToken.setActionCallback(new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
          // The message was published
          Log.e("file", "subscribed to  = "+topic);
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken,
                              Throwable exception) {
          Log.e("file", "subscribtion falied  = "+topic);
          // The subscription could not be performed, maybe the user was not
          // authorized to subscribe on the specified topic e.g. using wildcards

        }
      });
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }

}
