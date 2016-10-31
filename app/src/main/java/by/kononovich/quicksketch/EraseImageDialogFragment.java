package by.kononovich.quicksketch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

// Фрагмент для стирания изображения
public class EraseImageDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Назначение сообщения AlertDialog
        builder.setMessage(R.string.message_erase);

        // Добавление кнопки стирания
        builder.setPositiveButton(R.string.button_erase,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getSketchFragment().getQuickSketchView().clear();
                    }
                }
        );

        builder.setNegativeButton(R.string.button_cancel, null);

        return builder.create();
    }

    private MainActivityFragment getSketchFragment() {
        return (MainActivityFragment) getFragmentManager().findFragmentById(R.id.sketchFragment);
    }

    // Сообщает MainActivityFragment, что окно находится на экране
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MainActivityFragment fragment = getSketchFragment();

        if (fragment != null)
            fragment.setDialogOnScreen(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MainActivityFragment fragment = getSketchFragment();

        if (fragment != null)
            fragment.setDialogOnScreen(false);
    }
}

