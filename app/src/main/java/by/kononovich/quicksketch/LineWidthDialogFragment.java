package by.kononovich.quicksketch;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

// Используется для выбора толщины линии
public class LineWidthDialogFragment extends DialogFragment {
    private ImageView widthImageView;

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        // Создание диалогового окна
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());
        View lineWidthDialogView =
                getActivity().getLayoutInflater().inflate(
                        R.layout.fragment_line_width, null);
        builder.setView(lineWidthDialogView);

        // Назначение сообщения AlertDialog
        builder.setTitle(R.string.title_line_width_dialog);

        widthImageView = (ImageView) lineWidthDialogView.findViewById(R.id.widthImageView);

        // Настройка widthSeekBar
        final QuickSketchView quickSketchView = getSketchFragment().getQuickSketchView();
        final SeekBar widthSeekBar = (SeekBar)
                lineWidthDialogView.findViewById(R.id.widthSeekBar);
        widthSeekBar.setOnSeekBarChangeListener(lineWidthChanged);
        widthSeekBar.setProgress(quickSketchView.getLineWidth());

        // Добавление кнопк
        builder.setPositiveButton(R.string.button_set_line_width,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        quickSketchView.setLineWidth(widthSeekBar.getProgress());
                    }
                }
        );

        return builder.create(); // Возвращение диалогового окна
    }

    // Возвращает ссылку на MainActivityFragment
    private MainActivityFragment getSketchFragment() {
        return (MainActivityFragment) getFragmentManager().findFragmentById(
                R.id.sketchFragment);

    }

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

    // OnSeekBarChangeListener для SeekBar в диалоговом окне толщины линии
    private final OnSeekBarChangeListener lineWidthChanged =
            new OnSeekBarChangeListener() {
                final Bitmap bitmap = Bitmap.createBitmap(
                        400, 100, Bitmap.Config.ARGB_8888);
                final Canvas canvas = new Canvas(bitmap);

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // Настройка объекта Paint для текущего значения SeekBar
                    Paint p = new Paint();
                    p.setColor(getSketchFragment().getQuickSketchView().getDrawingColor());
                    p.setStrokeCap(Paint.Cap.ROUND);
                    p.setStrokeWidth(progress);

                    // Стирание объекта Bitmap и перерисовка линии
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        bitmap.eraseColor(
                                getResources().getColor(android.R.color.transparent,
                                        getContext().getTheme()));
                    } else {
                        bitmap.eraseColor(
                                ContextCompat.getColor(getContext(), android.R.color.transparent));

                    }
                    canvas.drawLine(30, 50, 370, 50, p);
                    widthImageView.setImageBitmap(bitmap);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                } // required

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                } // required
            };
}


