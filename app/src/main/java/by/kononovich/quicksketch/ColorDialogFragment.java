package by.kononovich.quicksketch;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

// Класс диалогового окна выбора цвета
public class ColorDialogFragment extends DialogFragment {
   private SeekBar alphaSeekBar;
   private SeekBar redSeekBar;
   private SeekBar greenSeekBar;
   private SeekBar blueSeekBar;
   private View colorView;
   private int color;

   // Создание и возвращение объекта AlertDialog
   @Override
   public Dialog onCreateDialog(Bundle bundle) {
      // Создание диалогового окна
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      View colorDialogView = getActivity().getLayoutInflater().inflate(
         R.layout.fragment_color, null);
      builder.setView(colorDialogView);

      // Назначение сообщения AlertDialog
      builder.setTitle(R.string.title_color_dialog);

      // Получение значений SeekBar и назначение слушателей onChange
      alphaSeekBar = (SeekBar) colorDialogView.findViewById(
         R.id.alphaSeekBar);
      redSeekBar = (SeekBar) colorDialogView.findViewById(
         R.id.redSeekBar);
      greenSeekBar = (SeekBar) colorDialogView.findViewById(
         R.id.greenSeekBar);
      blueSeekBar = (SeekBar) colorDialogView.findViewById(
         R.id.blueSeekBar);
      colorView = colorDialogView.findViewById(R.id.colorView);

      // Регистрация слушателей событий SeekBar
      alphaSeekBar.setOnSeekBarChangeListener(colorChangedListener);
      redSeekBar.setOnSeekBarChangeListener(colorChangedListener);
      greenSeekBar.setOnSeekBarChangeListener(colorChangedListener);
      blueSeekBar.setOnSeekBarChangeListener(colorChangedListener);

      // Использование текущего цвета линии для инициализации
      final QuickSketchView quickSketchView = getSketchFragment().getQuickSketchView();
      color = quickSketchView.getDrawingColor();
      alphaSeekBar.setProgress(Color.alpha(color));
      redSeekBar.setProgress(Color.red(color));
      greenSeekBar.setProgress(Color.green(color));
      blueSeekBar.setProgress(Color.blue(color));

      // Добавление кнопки назначения цвета
      builder.setPositiveButton(R.string.button_set_color,
         new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               quickSketchView.setDrawingColor(color);
            }
         }
      );

      return builder.create(); // Возвращение диалогового окна
   }

   // Получение ссылки на MainActivityFragment
   private MainActivityFragment getSketchFragment() {
      return (MainActivityFragment) getFragmentManager().findFragmentById(
         R.id.sketchFragment);
   }


   // сообщает MainActivityFragment, что диалоговое окно находится на экране
   @Override
   public void onAttach(Context context) {
      super.onAttach(context);
      MainActivityFragment fragment = getSketchFragment();

      if (fragment != null)
         fragment.setDialogOnScreen(true);
   }

   // Сообщает MainActivityFragment, что диалоговое окно не отображается
   @Override
   public void onDetach() {
      super.onDetach();
      MainActivityFragment fragment = getSketchFragment();

      if (fragment != null)
         fragment.setDialogOnScreen(false);
   }

   // OnSeekBarChangeListener для компонентов SeekBar в диалоговом окне
   private final OnSeekBarChangeListener colorChangedListener =
      new OnSeekBarChangeListener() {
         // Отображение обновленного цвета
         @Override
         public void onProgressChanged(SeekBar seekBar, int progress,
                                       boolean fromUser) {

            if (fromUser)  // Изменено пользователем (не программой)
               color = Color.argb(alphaSeekBar.getProgress(),
                  redSeekBar.getProgress(), greenSeekBar.getProgress(),
                  blueSeekBar.getProgress());
            colorView.setBackgroundColor(color);
         }

         @Override
         public void onStartTrackingTouch(SeekBar seekBar) {}

         @Override
         public void onStopTrackingTouch(SeekBar seekBar) {}
      };
}

