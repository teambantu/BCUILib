package id.teambantu.bcuilib;

import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import eightbitlab.com.blurview.RenderScriptBlur;
import id.teambantu.bcuilib.databinding.BcUiLayoutBinding;
import id.teambantu.bcuilib.event.BCDialogButton;
import id.teambantu.bcuilib.event.BCListener;
import id.teambantu.bcuilib.ui.BCFragment;
import id.teambantu.bcuilib.utils.BCBitmapTransform;
import id.teambantu.bcuilib.utils.BCImage;

public class BCUI {
    private BcUiLayoutBinding binding;
    private AppCompatActivity activity;
    private BottomSheetBehavior bottomSheet;

    //    Dialog
    private View currentDialog;
    private boolean dialogShow = false;
    private boolean bottomSheetShow = false;
    private boolean cancelable = true;

    public BCUI(AppCompatActivity activity, View v) {
        this.activity = activity;

        initLib(v);
        initBlurLib();
        initBottomSheet();
    }


    private void initBottomSheet() {
        bottomSheet = BottomSheetBehavior.from(binding.bottomSheet);
        bottomSheet.setHideable(true);
        bottomSheet.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        hideOverlay(binding.bottomSheet);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        showOverlay(binding.bottomSheet);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    private void initLib(View v) {
        binding = BcUiLayoutBinding.inflate(activity.getLayoutInflater());
        binding.frameLayout.addView(v);
    }

    private void initBlurLib() {
        binding.overlayBlur.setAlpha(0);
        binding.overlayBlur.setVisibility(View.GONE);
        binding.overlayBlur.setupWith(binding.root)
                .setFrameClearDrawable(activity.getWindow().getDecorView().getBackground())
                .setBlurAlgorithm(new RenderScriptBlur(activity))
                .setBlurRadius(2f)
                .setHasFixedTransformationMatrix(true);
        binding.overlayBlur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cancelable)
                    if (currentDialog != null) hideOverlay(currentDialog);
            }
        });
    }

    private void showOverlay(View v) {
        currentDialog = v;
        dialogShow = true;
        binding.overlayBlur.setVisibility(View.VISIBLE);
        binding.overlayBlur.animate().alpha(1).setDuration(300).start();
    }

    private void hideOverlay(final View v) {
        dialogShow = false;
        currentDialog = null;

        binding.overlayBlur.animate().alpha(0).setDuration(300).start();
        if (!bottomSheetShow) {
            v.animate().scaleY(0).scaleX(0).setDuration(300).start();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    binding.overlayBlur.setVisibility(View.GONE);
                    v.setVisibility(View.GONE);
                }
            }, 300);
        } else {
            bottomSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    binding.overlayBlur.setVisibility(View.GONE);
                }
            }, 300);
        }

    }

    private void showDialog(View v) {
        bottomSheetShow = false;
        v.setVisibility(View.VISIBLE);
        v.animate().scaleX(0).scaleY(0).setDuration(0).start();
        v.animate().scaleX(1).scaleY(1).setDuration(300).start();
    }

    //  Bottom Sheet Dialog
    public void showBottomSheet(BCFragment fragment) {
        bottomSheetShow = true;
        showOverlay(binding.bottomSheet);

        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.bottomSheetFragment, fragment).commit();
        bottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    //    Alert Dialog
    public void showAlert(String message, AlertType type, Time showTime) {
        showDialog(binding.alertDialog);
        binding.alertMessage.setText(message);
        switch (type) {
            case INFO:
                binding.alertDialog.setCardBackgroundColor(activity.getResources().getColor(R.color.info));
                break;
            case DANGER:
                binding.alertDialog.setCardBackgroundColor(activity.getResources().getColor(R.color.danger));
                break;
            case PRIMARY:
                binding.alertDialog.setCardBackgroundColor(activity.getResources().getColor(R.color.primary));
                break;
            case SECONDARY:
                binding.alertDialog.setCardBackgroundColor(activity.getResources().getColor(R.color.secondary));
                break;
            case SUCCESS:
                binding.alertDialog.setCardBackgroundColor(activity.getResources().getColor(R.color.success));
                break;
            case WARNING:
                binding.alertDialog.setCardBackgroundColor(activity.getResources().getColor(R.color.warning));
                break;
        }
        int delay = showTime.equals(Time.LONG) ? 5000 : 3000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                hideAlertDialog();
            }
        }, delay);
        binding.alertDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideAlertDialog();
            }
        });
    }

    //    Progress Dialog
    public void showProgressDialog(String title, String message, boolean cancelable) {
        showOverlay(binding.progressDialog);
        showDialog(binding.progressDialog);

        binding.progressDialogTitle.setText(title);
        binding.progressDialogMessage.setText(message);

        this.cancelable = cancelable;
    }

    //  Confirm Dialog
    public void showConfirmDialog(BCImage image, String title, String message, boolean cancelable, final BCDialogButton yesButton) {
        showOverlay(binding.confirmDialog);
        showDialog(binding.confirmDialog);

        binding.confirmDialogTitle.setText(title);
        binding.confirmDialogMessage.setText(message);
        binding.confirmDialogYesButton.setBackgroundTintList(activity.getResources().getColorStateList(yesButton.color()));
        binding.confirmDialogYesButton.setText(yesButton.getText().isEmpty() ? "Yes" : yesButton.getText());
        binding.confirmDialogYesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                yesButton.onClickListener();
                binding.confirmDialogYesButton.setOnClickListener(null);
                hideOverlay(binding.confirmDialog);
            }
        });

        binding.confirmDialogNoButton.setVisibility(View.INVISIBLE);

        if (image == null) {
            binding.confirmDialogImage.setVisibility(View.GONE);
        } else {
            int MAX_WIDTH = 1024;
            int MAX_HEIGHT = 768;
            int size = (int) Math.ceil(Math.sqrt(MAX_WIDTH * MAX_HEIGHT));
            image.getImage().transform(new BCBitmapTransform(MAX_WIDTH, MAX_HEIGHT)).resize(size, size).placeholder(R.drawable.banner_placeholder).centerInside().into(binding.confirmDialogImage);
        }

        this.cancelable = cancelable;
    }

    public void showConfirmDialog(BCImage image, String title, String message, boolean cancelable, final BCDialogButton yesButton, final BCDialogButton noButton) {
        showConfirmDialog(image, title, message, cancelable, yesButton);
        binding.confirmDialogNoButton.setVisibility(View.VISIBLE);
        binding.confirmDialogNoButton.setBackgroundTintList(activity.getResources().getColorStateList(noButton.color()));
        binding.confirmDialogNoButton.setText(noButton.getText().isEmpty() ? "No" : noButton.getText());
        binding.confirmDialogNoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noButton.onClickListener();
                binding.confirmDialogNoButton.setOnClickListener(null);
                hideOverlay(binding.confirmDialog);
            }
        });
    }

    //    Hiding event
    public void hideProgressDialog() {
        hideOverlay(binding.progressDialog);
    }

    private void hideAlertDialog() {
        binding.alertDialog.animate().scaleX(0).scaleY(0).setDuration(300).start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.alertDialog.setVisibility(View.GONE);
                binding.alertDialog.setOnClickListener(null);
            }
        }, 300);
    }

    public void hideDialog() {
        hideOverlay(currentDialog);
    }

    public void hideBottomSheet(BCListener listener){
        bottomSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
        listener.onCloseBottomSheet();
    }

    //    Basic function
    public View getRoot() {
        return binding.getRoot();
    }

    public boolean onBackPressed() {
        if (!cancelable) return false;
        if (dialogShow) {
            if (bottomSheetShow) {
                bottomSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
            } else {
                hideDialog();
            }
            return false;
        }
        return true;
    }

    public enum AlertType {
        DANGER, PRIMARY, SECONDARY, SUCCESS, WARNING, INFO
    }

    public enum Time {
        SHORT, LONG
    }
}